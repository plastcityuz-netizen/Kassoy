package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AppLanguage
import com.example.core.BillingCycle
import com.example.core.EmployeeRole
import com.example.core.MockPaymentProvider
import com.example.core.PaymentMethod
import com.example.core.PaymentReceipt
import com.example.core.PlanId
import com.example.core.RbacPolicy
import com.example.core.SubscriptionStatus
import com.example.data.AiChatMessage
import com.example.data.AiCfoRepository
import com.example.data.AuditLog
import com.example.data.BalansRepository
import com.example.data.BusinessTransaction
import com.example.data.Company
import com.example.data.DashboardMetrics
import com.example.data.DebtRecord
import com.example.data.EmployeeRecord
import com.example.data.IndustryType
import com.example.data.InventoryItem
import com.example.data.ProductionOrder
import com.example.data.TaxRegime
import com.example.data.TransactionType
import com.example.data.TxStatus
import com.example.data.UiState
import com.example.data.UserWorkspace
import com.example.data.WhatIfScenarioResult
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppSection {
    DASHBOARD,
    MODULES,
    AI_CFO,
    FINANCE,
    SETTINGS
}

enum class SubModuleTab {
    SALES,
    INVENTORY,
    PURCHASES,
    PRODUCTION,
    HR,
    ACCOUNTING,
    REPORTS
}

data class BalansCockpitState(
    val workspace: UserWorkspace? = null,
    val companies: List<Company> = emptyList(),
    val activeCompany: Company? = null,
    val activeRole: EmployeeRole = EmployeeRole.OWNER,
    val transactions: List<BusinessTransaction> = emptyList(),
    val inventory: List<InventoryItem> = emptyList(),
    val production: List<ProductionOrder> = emptyList(),
    val employees: List<EmployeeRecord> = emptyList(),
    val debts: List<DebtRecord> = emptyList(),
    val auditLogs: List<AuditLog> = emptyList(),
    val metrics: DashboardMetrics = DashboardMetrics.compute(null, emptyList(), emptyList(), emptyList(), emptyList())
) {
    val needsOnboarding: Boolean get() = companies.isEmpty()
    val currentPlan: PlanId get() = PlanId.fromId(workspace?.planId)
    val currentStatus: SubscriptionStatus get() = SubscriptionStatus.fromId(workspace?.subscriptionStatus)
    val currentCycle: BillingCycle get() = BillingCycle.fromId(workspace?.billingCycle)
}

class MainViewModel(
    private val repository: BalansRepository,
    private val aiRepository: AiCfoRepository,
    private val currentUserId: String
) : ViewModel() {

    private companion object {
        const val TAG = "MainViewModel"
    }

    private val paymentProvider = MockPaymentProvider()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _undoTxId = MutableStateFlow<Pair<String, TxStatus>?>(null)
    val undoTxState: StateFlow<Pair<String, TxStatus>?> = _undoTxId.asStateFlow()

    private val _isMutating = MutableStateFlow(false)
    val isMutating: StateFlow<Boolean> = _isMutating.asStateFlow()

    private val _aiMessages = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val aiMessages: StateFlow<List<AiChatMessage>> = _aiMessages.asStateFlow()

    private val _aiChatState = MutableStateFlow<UiState<Unit>>(UiState.Success(Unit))
    val aiChatState: StateFlow<UiState<Unit>> = _aiChatState.asStateFlow()

    private val _morningBriefState = MutableStateFlow<UiState<List<String>>?>(null)
    val morningBriefState: StateFlow<UiState<List<String>>?> = _morningBriefState.asStateFlow()

    private val _whatIfState = MutableStateFlow<UiState<WhatIfScenarioResult>?>(null)
    val whatIfState: StateFlow<UiState<WhatIfScenarioResult>?> = _whatIfState.asStateFlow()

    private val _paymentReceipts = MutableStateFlow<List<PaymentReceipt>>(emptyList())
    val paymentReceipts: StateFlow<List<PaymentReceipt>> = _paymentReceipts.asStateFlow()

    private data class CoreStreams(
        val workspace: UserWorkspace?,
        val companies: List<Company>,
        val transactions: List<BusinessTransaction>,
        val inventory: List<InventoryItem>
    )

    private data class SecondaryStreams(
        val production: List<ProductionOrder>,
        val employees: List<EmployeeRecord>,
        val debts: List<DebtRecord>,
        val auditLogs: List<AuditLog>
    )

    private val coreFlow = combine(
        repository.observeWorkspace(currentUserId),
        repository.observeCompanies(currentUserId),
        repository.observeTransactions(currentUserId),
        repository.observeInventory(currentUserId)
    ) { ws, comps, txs, inv ->
        CoreStreams(ws, comps, txs, inv)
    }

    private val secondaryFlow = combine(
        repository.observeProduction(currentUserId),
        repository.observeEmployees(currentUserId),
        repository.observeDebts(currentUserId),
        repository.observeAuditLogs(currentUserId)
    ) { prod, emps, debts, logs ->
        SecondaryStreams(prod, emps, debts, logs)
    }

    val cockpitState: StateFlow<UiState<BalansCockpitState>> = combine(coreFlow, secondaryFlow) { core, sec ->
        val activeCompany = core.companies.firstOrNull { it.id == core.workspace?.activeCompanyId }
            ?: core.companies.firstOrNull()
        val companyId = activeCompany?.id ?: ""
        val filteredTx = core.transactions.filter { it.companyId == companyId }
        val filteredInv = core.inventory.filter { it.companyId == companyId }
        val filteredProd = sec.production.filter { it.companyId == companyId }
        val filteredEmp = sec.employees.filter { it.companyId == companyId }
        val filteredDebts = sec.debts.filter { it.companyId == companyId }
        val filteredLogs = sec.auditLogs.filter { it.companyId == companyId }

        val role = EmployeeRole.fromKey(core.workspace?.activeRole)
        val metrics = DashboardMetrics.compute(
            company = activeCompany,
            transactions = filteredTx,
            inventory = filteredInv,
            employees = filteredEmp,
            debts = filteredDebts
        )

        BalansCockpitState(
            workspace = core.workspace,
            companies = core.companies,
            activeCompany = activeCompany,
            activeRole = role,
            transactions = filteredTx,
            inventory = filteredInv,
            production = filteredProd,
            employees = filteredEmp,
            debts = filteredDebts,
            auditLogs = filteredLogs,
            metrics = metrics
        )
    }
        .map<BalansCockpitState, UiState<BalansCockpitState>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing cockpit data", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Ma’lumotlarni yuklashda xatolik"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = UiState.Loading
        )

    fun clearToast() {
        _toastMessage.value = null
    }

    fun clearUndo() {
        _undoTxId.value = null
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun completeOnboarding(
        companyName: String,
        stir: String,
        city: String,
        taxRegime: TaxRegime,
        industry: IndustryType,
        seedDemoData: Boolean
    ) {
        if (_isMutating.value) return
        viewModelScope.launch {
            _isMutating.value = true
            val res = repository.completeOnboarding(companyName, stir, city, taxRegime, industry, seedDemoData)
            _isMutating.value = false
            res.onFailure { _toastMessage.value = it.localizedMessage ?: "Saqlashda xatolik" }
        }
    }

    fun switchCompany(companyId: String) {
        viewModelScope.launch {
            repository.switchActiveCompany(companyId)
                .onFailure { _toastMessage.value = it.localizedMessage }
        }
    }

    fun switchRole(role: EmployeeRole) {
        viewModelScope.launch {
            repository.switchActiveRole(role)
                .onFailure { _toastMessage.value = it.localizedMessage }
        }
    }

    fun addTransaction(
        companyId: String,
        type: TransactionType,
        category: String,
        counterparty: String,
        amountUzs: Long,
        status: TxStatus,
        dateDisplay: String,
        note: String?,
        actorRole: EmployeeRole
    ) {
        val statusState = (cockpitState.value as? UiState.Success)?.data?.currentStatus ?: SubscriptionStatus.TRIAL
        if (!RbacPolicy.canMutateData(statusState)) {
            _toastMessage.value = "Obuna faqat ko‘rish rejimida (read-only)."
            return
        }
        viewModelScope.launch {
            _isMutating.value = true
            val tx = BusinessTransaction(
                companyId = companyId,
                type = type.key,
                category = category,
                counterparty = counterparty,
                amountUzs = amountUzs,
                status = status.key,
                dateDisplay = dateDisplay,
                note = note
            )
            repository.addTransaction(tx, actorRole)
                .onSuccess { _toastMessage.value = "Operatsiya saqlandi" }
                .onFailure { _toastMessage.value = it.localizedMessage ?: "Xatolik" }
            _isMutating.value = false
        }
    }

    fun toggleTransactionPaid(tx: BusinessTransaction) {
        val prev = TxStatus.fromKey(tx.status)
        val next = if (prev == TxStatus.PAID) TxStatus.PENDING else TxStatus.PAID
        viewModelScope.launch {
            repository.updateTransactionStatus(tx.id, next)
                .onSuccess {
                    _undoTxId.value = tx.id to prev
                    _toastMessage.value = "Holat o‘zgartirildi: ${next.labelUz}"
                }
                .onFailure { _toastMessage.value = it.localizedMessage }
        }
    }

    fun undoLastTxStatus() {
        val (id, prev) = _undoTxId.value ?: return
        _undoTxId.value = null
        viewModelScope.launch {
            repository.updateTransactionStatus(id, prev)
        }
    }

    fun addInventoryItem(
        companyId: String,
        sku: String,
        name: String,
        category: String,
        quantity: Long,
        unit: String,
        unitCostUzs: Long,
        minThreshold: Long,
        actorRole: EmployeeRole
    ) {
        viewModelScope.launch {
            _isMutating.value = true
            val item = InventoryItem(
                companyId = companyId,
                sku = sku,
                name = name,
                category = category,
                quantity = quantity,
                unit = unit,
                unitCostUzs = unitCostUzs,
                minThreshold = minThreshold
            )
            repository.addInventoryItem(item, actorRole)
                .onSuccess { _toastMessage.value = "Ombor mahsuloti qo‘shildi" }
                .onFailure { _toastMessage.value = it.localizedMessage }
            _isMutating.value = false
        }
    }

    fun adjustInventory(item: InventoryItem, delta: Long) {
        viewModelScope.launch {
            val next = (item.quantity + delta).coerceAtLeast(0L)
            repository.updateInventoryQuantity(item.id, next)
                .onFailure { _toastMessage.value = it.localizedMessage }
        }
    }

    fun addProductionOrder(
        companyId: String,
        productName: String,
        bomSummary: String,
        targetQty: Long,
        unitCostUzs: Long,
        actorRole: EmployeeRole
    ) {
        viewModelScope.launch {
            _isMutating.value = true
            val order = ProductionOrder(
                companyId = companyId,
                productName = productName,
                bomSummary = bomSummary,
                targetQty = targetQty,
                completedQty = 0L,
                unitCostUzs = unitCostUzs,
                defectPercent = 1.0,
                status = "PLANNED"
            )
            repository.addProductionOrder(order, actorRole)
                .onSuccess { _toastMessage.value = "Ishlab chiqarish buyurtmasi yaratildi" }
                .onFailure { _toastMessage.value = it.localizedMessage }
            _isMutating.value = false
        }
    }

    fun advanceProduction(order: ProductionOrder) {
        viewModelScope.launch {
            repository.advanceProductionOrder(order)
                .onSuccess { _toastMessage.value = "Partiya jarayoni yangilandi" }
                .onFailure { _toastMessage.value = it.localizedMessage }
        }
    }

    fun addEmployee(
        companyId: String,
        fullName: String,
        roleKey: String,
        department: String,
        phone: String,
        salaryUzs: Long,
        actorRole: EmployeeRole
    ) {
        viewModelScope.launch {
            _isMutating.value = true
            val emp = EmployeeRecord(
                companyId = companyId,
                fullName = fullName,
                roleKey = roleKey,
                department = department,
                phone = phone,
                attendancePercent = 100,
                salaryUzs = salaryUzs,
                status = "ACTIVE"
            )
            repository.addEmployee(emp, actorRole)
                .onSuccess { _toastMessage.value = "Xodim qo‘shildi" }
                .onFailure { _toastMessage.value = it.localizedMessage }
            _isMutating.value = false
        }
    }

    fun confirmSendDebtReminder(debt: DebtRecord, actorRole: EmployeeRole) {
        viewModelScope.launch {
            repository.markDebtReminderSent(debt, actorRole)
                .onSuccess { _toastMessage.value = "Eslatma yuborildi: ${debt.counterparty}" }
                .onFailure { _toastMessage.value = it.localizedMessage }
        }
    }

    fun updateTaxRates(
        companyId: String,
        qqsRate: Double,
        inpsRate: Double,
        incomeTaxRate: Double,
        actorRole: EmployeeRole
    ) {
        viewModelScope.launch {
            repository.updateTaxRates(companyId, qqsRate, inpsRate, incomeTaxRate, actorRole)
                .onSuccess { _toastMessage.value = "Soliq stavkalari saqlandi" }
                .onFailure { _toastMessage.value = it.localizedMessage }
        }
    }

    fun subscribePlan(
        planId: PlanId,
        cycle: BillingCycle,
        method: PaymentMethod,
        promoCode: String?
    ) {
        viewModelScope.launch {
            _isMutating.value = true
            val payRes = paymentProvider.processPayment(planId, cycle, method, promoCode)
            payRes.onSuccess { receipt ->
                _paymentReceipts.value = listOf(receipt) + _paymentReceipts.value
                repository.updateSubscriptionPlan(planId, cycle, SubscriptionStatus.ACTIVE)
                    .onSuccess {
                        _toastMessage.value = "${planId.name} tarifi faollashtirildi (${receipt.invoiceNumber})"
                    }
                    .onFailure { _toastMessage.value = it.localizedMessage }
            }.onFailure {
                _toastMessage.value = it.localizedMessage ?: "To‘lov xatosi"
            }
            _isMutating.value = false
        }
    }

    // --- AI CFO (Firebase AI Logic) ---

    fun askAiCfo(question: String, language: AppLanguage) {
        val state = (cockpitState.value as? UiState.Success)?.data ?: return
        if (question.isBlank()) return

        val userMsg = AiChatMessage(id = UUID.randomUUID().toString(), isUser = true, text = question.trim())
        val botId = UUID.randomUUID().toString()
        _aiMessages.value = _aiMessages.value + userMsg + AiChatMessage(
            id = botId,
            isUser = false,
            text = "",
            isStreaming = true
        )
        _aiChatState.value = UiState.Loading

        viewModelScope.launch {
            try {
                aiRepository.streamCfoAnswer(
                    question = question,
                    company = state.activeCompany,
                    metrics = state.metrics,
                    language = language
                ).collect { partial ->
                    _aiMessages.value = _aiMessages.value.map { msg ->
                        if (msg.id == botId) msg.copy(text = partial, isStreaming = true) else msg
                    }
                }
                _aiMessages.value = _aiMessages.value.map { msg ->
                    if (msg.id == botId) msg.copy(isStreaming = false) else msg
                }
                _aiChatState.value = UiState.Success(Unit)
                val used = (state.workspace?.aiQuestionsUsed ?: 0) + 1
                repository.incrementAiQuestionsUsed(used)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI CFO timeout", e)
                _aiMessages.value = _aiMessages.value.filterNot { it.id == botId }
                _aiChatState.value = UiState.Error("AI CFO javobi vaqt chegarasidan oshdi (60s). Qayta urinib ko‘ring.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI CFO error", e)
                _aiMessages.value = _aiMessages.value.filterNot { it.id == botId }
                _aiChatState.value = UiState.Error(e.localizedMessage ?: "AI CFO ulanishida xatolik")
            }
        }
    }

    fun loadMorningBrief(language: AppLanguage) {
        val state = (cockpitState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            _morningBriefState.value = UiState.Loading
            val res = aiRepository.generateMorningBrief(state.activeCompany, state.metrics, language)
            res.onSuccess { items ->
                _morningBriefState.value = UiState.Success(items)
            }.onFailure { err ->
                _morningBriefState.value = UiState.Error(err.localizedMessage ?: "Ertalabki brif yuklanmadi")
            }
        }
    }

    fun runWhatIf(scenarioText: String, language: AppLanguage) {
        val state = (cockpitState.value as? UiState.Success)?.data ?: return
        if (scenarioText.isBlank()) return
        viewModelScope.launch {
            _whatIfState.value = UiState.Loading
            val res = aiRepository.runWhatIfScenario(scenarioText, state.activeCompany, state.metrics, language)
            res.onSuccess { result ->
                _whatIfState.value = UiState.Success(result)
            }.onFailure { err ->
                _whatIfState.value = UiState.Error(err.localizedMessage ?: "Stsenariy hisoblanmadi")
            }
        }
    }
}
