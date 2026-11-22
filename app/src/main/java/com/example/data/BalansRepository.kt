package com.example.data

import android.content.Context
import com.example.R
import com.example.core.BillingCycle
import com.example.core.EmployeeRole
import com.example.core.Formatters
import com.example.core.PlanId
import com.example.core.SubscriptionStatus
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class BalansRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("Foydalanuvchi Google orqali tizimga kirishi shart.")

    private fun userDoc(uid: String) = db.collection("users").document(uid)

    fun observeWorkspace(uid: String): Flow<UserWorkspace?> =
        userDoc(uid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObject(UserWorkspace::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                unwrapAndRethrowOrLog(error, OperationType.GET, "users/$uid", uid)
            }

    fun observeCompanies(uid: String): Flow<List<Company>> =
        userDoc(uid).collection("companies")
            .snapshots()
            .map { snap -> snap.toObjects(Company::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .catch { error -> unwrapAndRethrowOrLog(error, OperationType.LIST, "users/$uid/companies", uid) }

    fun observeTransactions(uid: String): Flow<List<BusinessTransaction>> =
        userDoc(uid).collection("transactions")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snap -> snap.toObjects(BusinessTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .catch { error -> unwrapAndRethrowOrLog(error, OperationType.LIST, "users/$uid/transactions", uid) }

    fun observeInventory(uid: String): Flow<List<InventoryItem>> =
        userDoc(uid).collection("inventory")
            .snapshots()
            .map { snap -> snap.toObjects(InventoryItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .catch { error -> unwrapAndRethrowOrLog(error, OperationType.LIST, "users/$uid/inventory", uid) }

    fun observeProduction(uid: String): Flow<List<ProductionOrder>> =
        userDoc(uid).collection("production")
            .snapshots()
            .map { snap -> snap.toObjects(ProductionOrder::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .catch { error -> unwrapAndRethrowOrLog(error, OperationType.LIST, "users/$uid/production", uid) }

    fun observeEmployees(uid: String): Flow<List<EmployeeRecord>> =
        userDoc(uid).collection("employees")
            .snapshots()
            .map { snap -> snap.toObjects(EmployeeRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .catch { error -> unwrapAndRethrowOrLog(error, OperationType.LIST, "users/$uid/employees", uid) }

    fun observeDebts(uid: String): Flow<List<DebtRecord>> =
        userDoc(uid).collection("debts")
            .snapshots()
            .map { snap -> snap.toObjects(DebtRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .catch { error -> unwrapAndRethrowOrLog(error, OperationType.LIST, "users/$uid/debts", uid) }

    fun observeAuditLogs(uid: String): Flow<List<AuditLog>> =
        userDoc(uid).collection("auditLogs")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(40)
            .snapshots()
            .map { snap -> snap.toObjects(AuditLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .catch { error -> unwrapAndRethrowOrLog(error, OperationType.LIST, "users/$uid/auditLogs", uid) }

    private fun unwrapAndRethrowOrLog(
        error: Throwable,
        op: OperationType,
        path: String,
        uid: String
    ): Nothing {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, op, path)
        }
        throw (firestoreError ?: error)
    }

    suspend fun completeOnboarding(
        companyName: String,
        stir: String,
        city: String,
        taxRegime: TaxRegime,
        industry: IndustryType,
        seedDemoData: Boolean
    ): Result<String> {
        var currentPath = "users"
        return try {
            val uid = requireUserId()
            require(companyName.trim().length in 2..120) { "Kompaniya nomi 2-120 belgi bo‘lishi kerak." }
            require(Formatters.isValidStir(stir)) { "STIR aniq 9 raqam bo‘lishi kerak." }
            require(city.trim().length in 2..60) { "Shahar nomi 2-60 belgi bo‘lishi kerak." }

            val user = auth.currentUser
            val primaryCompanyId = "comp_primary"
            currentPath = "users/$uid"

            val primaryCompany = Company(
                userId = uid,
                name = companyName.trim(),
                stir = stir.trim(),
                taxRegime = taxRegime.key,
                industry = industry.key,
                city = city.trim(),
                qqsRate = if (taxRegime == TaxRegime.QQS) 12.0 else 0.0,
                inpsRate = 1.0,
                incomeTaxRate = if (taxRegime == TaxRegime.QQS) 12.0 else 4.0,
                cashBalanceUzs = if (seedDemoData) 168_000_000L else 0L,
                bankBalanceUzs = if (seedDemoData) 384_500_000L else 0L
            )

            val batch = db.batch()
            val wsSnap = userDoc(uid).get().await()
            if (!wsSnap.exists()) {
                val wsPayload = mapOf(
                    "userId" to uid,
                    "displayName" to (user?.displayName?.takeIf { it.isNotBlank() } ?: "Biznes egasi"),
                    "email" to (user?.email ?: ""),
                    "activeCompanyId" to primaryCompanyId,
                    "activeRole" to EmployeeRole.OWNER.key,
                    "planId" to PlanId.PREMIUM.id,
                    "billingCycle" to BillingCycle.MONTHLY.id,
                    "subscriptionStatus" to SubscriptionStatus.TRIAL.id,
                    "trialDaysRemaining" to 23,
                    "aiQuestionsUsed" to 4,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                batch.set(userDoc(uid), wsPayload)
            } else {
                batch.update(
                    userDoc(uid),
                    mapOf(
                        "activeCompanyId" to primaryCompanyId,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            if (seedDemoData) {
                val bundle = SeedDataFactory.createUzbekDemoBundle(primaryCompanyId, primaryCompany)
                bundle.companies.forEach { (compId, comp) ->
                    batch.set(userDoc(uid).collection("companies").document(compId), comp.toCreateMap(uid))
                }
                bundle.transactions.forEach { tx ->
                    batch.set(userDoc(uid).collection("transactions").document(), tx.toCreateMap(uid))
                }
                bundle.inventory.forEach { item ->
                    batch.set(userDoc(uid).collection("inventory").document(), item.toCreateMap(uid))
                }
                bundle.production.forEach { order ->
                    batch.set(userDoc(uid).collection("production").document(), order.toCreateMap(uid))
                }
                bundle.employees.forEach { emp ->
                    batch.set(userDoc(uid).collection("employees").document(), emp.toCreateMap(uid))
                }
                bundle.debts.forEach { debt ->
                    batch.set(userDoc(uid).collection("debts").document(), debt.toCreateMap(uid))
                }
            } else {
                batch.set(
                    userDoc(uid).collection("companies").document(primaryCompanyId),
                    primaryCompany.toCreateMap(uid)
                )
            }

            val logRef = userDoc(uid).collection("auditLogs").document()
            batch.set(
                logRef,
                mapOf(
                    "userId" to uid,
                    "companyId" to primaryCompanyId,
                    "actorRole" to EmployeeRole.OWNER.key,
                    "actionTitle" to "Kompaniya ishga tushirildi: ${primaryCompany.name}",
                    "moduleKey" to "ONBOARDING",
                    "amountUzs" to 0L,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )

            batch.commit().await()
            Result.success(primaryCompanyId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, currentPath)
            Result.failure(e)
        }
    }

    suspend fun switchActiveCompany(companyId: String): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            require(companyId.isNotBlank())
            path = "users/$uid"
            userDoc(uid).update(
                mapOf(
                    "activeCompanyId" to companyId,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun switchActiveRole(role: EmployeeRole): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            path = "users/$uid"
            userDoc(uid).update(
                mapOf(
                    "activeRole" to role.key,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateSubscriptionPlan(
        planId: PlanId,
        cycle: BillingCycle,
        status: SubscriptionStatus
    ): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            path = "users/$uid"
            userDoc(uid).update(
                mapOf(
                    "planId" to planId.id,
                    "billingCycle" to cycle.id,
                    "subscriptionStatus" to status.id,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun incrementAiQuestionsUsed(nextCount: Int): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            path = "users/$uid"
            userDoc(uid).update(
                mapOf(
                    "aiQuestionsUsed" to nextCount.coerceIn(0, 100_000),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateTaxRates(
        companyId: String,
        qqsRate: Double,
        inpsRate: Double,
        incomeTaxRate: Double,
        actorRole: EmployeeRole
    ): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            require(qqsRate in 0.0..50.0 && inpsRate in 0.0..50.0 && incomeTaxRate in 0.0..50.0) {
                "Soliq stavkasi 0% dan 50% gacha bo‘lishi kerak."
            }
            path = "users/$uid/companies/$companyId"
            userDoc(uid).collection("companies").document(companyId).update(
                mapOf(
                    "qqsRate" to qqsRate,
                    "inpsRate" to inpsRate,
                    "incomeTaxRate" to incomeTaxRate,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            appendAuditLog(uid, companyId, actorRole, "Soliq stavkalari yangilandi (QQS $qqsRate%)", "SETTINGS", 0L)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun addTransaction(tx: BusinessTransaction, actorRole: EmployeeRole): Result<String> {
        var path = "users"
        return try {
            val uid = requireUserId()
            require(tx.counterparty.trim().length in 1..120) { "Kontragent nomi majburiy." }
            require(tx.category.trim().length in 1..80) { "Toifa majburiy." }
            require(tx.amountUzs in 0L..1_000_000_000_000L) { "Summa noto‘g‘ri." }

            val ref = userDoc(uid).collection("transactions").document()
            path = ref.path
            ref.set(tx.toCreateMap(uid)).await()
            appendAuditLog(uid, tx.companyId, actorRole, "Yangi ${tx.type}: ${tx.counterparty}", "TRANSACTIONS", tx.amountUzs)
            Result.success(ref.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateTransactionStatus(txId: String, newStatus: TxStatus): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            require(txId.isNotBlank())
            path = "users/$uid/transactions/$txId"
            userDoc(uid).collection("transactions").document(txId).update(
                mapOf(
                    "status" to newStatus.key,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun addInventoryItem(item: InventoryItem, actorRole: EmployeeRole): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            require(item.sku.trim().length in 2..40) { "SKU 2-40 belgi bo‘lishi kerak." }
            require(item.name.trim().length in 2..120) { "Mahsulot nomi 2-120 belgi bo‘lishi kerak." }
            val ref = userDoc(uid).collection("inventory").document()
            path = ref.path
            ref.set(item.toCreateMap(uid)).await()
            appendAuditLog(uid, item.companyId, actorRole, "Yangi ombor SKU: ${item.name}", "INVENTORY", item.totalValueUzs)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateInventoryQuantity(itemId: String, newQty: Long): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            require(itemId.isNotBlank())
            val bounded = newQty.coerceIn(0L, 10_000_000L)
            path = "users/$uid/inventory/$itemId"
            userDoc(uid).collection("inventory").document(itemId).update(
                mapOf(
                    "quantity" to bounded,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun addProductionOrder(order: ProductionOrder, actorRole: EmployeeRole): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            require(order.productName.trim().length in 2..120)
            require(order.targetQty >= 1L)
            val ref = userDoc(uid).collection("production").document()
            path = ref.path
            ref.set(order.toCreateMap(uid)).await()
            appendAuditLog(uid, order.companyId, actorRole, "Ishlab chiqarish buyurtmasi: ${order.productName}", "PRODUCTION", order.targetQty * order.unitCostUzs)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun advanceProductionOrder(order: ProductionOrder): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            require(order.id.isNotBlank())
            if (order.status == ProductionStatus.COMPLETED.key) {
                return Result.success(Unit)
            }
            val nextCompleted = (order.completedQty + (order.targetQty / 4L).coerceAtLeast(1L)).coerceAtMost(order.targetQty)
            val nextStatus = if (nextCompleted >= order.targetQty) ProductionStatus.COMPLETED.key else ProductionStatus.IN_PROGRESS.key
            path = "users/$uid/production/${order.id}"
            userDoc(uid).collection("production").document(order.id).update(
                mapOf(
                    "completedQty" to nextCompleted,
                    "status" to nextStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun addEmployee(emp: EmployeeRecord, actorRole: EmployeeRole): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            require(emp.fullName.trim().length in 2..100)
            require(Formatters.isValidUzPhone(emp.phone)) { "Telefon +998 XX XXX XX XX formatida bo‘lishi kerak." }
            val ref = userDoc(uid).collection("employees").document()
            path = ref.path
            ref.set(emp.toCreateMap(uid)).await()
            appendAuditLog(uid, emp.companyId, actorRole, "Yangi xodim qo‘shildi: ${emp.fullName}", "HR", emp.salaryUzs)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun markDebtReminderSent(debt: DebtRecord, actorRole: EmployeeRole): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            require(debt.id.isNotBlank())
            path = "users/$uid/debts/${debt.id}"
            userDoc(uid).collection("debts").document(debt.id).update(
                mapOf(
                    "reminderSent" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            appendAuditLog(uid, debt.companyId, actorRole, "Qarzdorlik eslatmasi yuborildi: ${debt.counterparty}", "FINANCE", debt.amountUzs)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    private suspend fun appendAuditLog(
        uid: String,
        companyId: String,
        actorRole: EmployeeRole,
        actionTitle: String,
        moduleKey: String,
        amountUzs: Long
    ) {
        try {
            userDoc(uid).collection("auditLogs").document().set(
                mapOf(
                    "userId" to uid,
                    "companyId" to companyId,
                    "actorRole" to actorRole.key,
                    "actionTitle" to actionTitle.take(160),
                    "moduleKey" to moduleKey,
                    "amountUzs" to amountUzs.coerceAtLeast(0L),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (_: Exception) {
            // Non-blocking audit trail write
        }
    }
}
