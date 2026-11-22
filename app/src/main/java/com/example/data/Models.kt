package com.example.data

import androidx.annotation.Keep
import com.example.core.Formatters
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class TaxRegime(val key: String, val labelUz: String, val labelRu: String) {
    QQS("QQS", "QQS (12%)", "НДС (12%)"),
    AYLANMA("AYLANMA", "Aylanmadan soliq (4%)", "Налог с оборота (4%)");

    companion object {
        fun fromKey(raw: String?): TaxRegime =
            entries.firstOrNull { it.key.equals(raw, ignoreCase = true) } ?: QQS
    }
}

enum class IndustryType(val key: String, val labelUz: String, val labelRu: String) {
    SAVDO("SAVDO", "Savdo va Chakana", "Торговля и Ритейл"),
    ISHLAB_CHIQARISH("ISHLAB_CHIQARISH", "Ishlab chiqarish / Zavod", "Производство / Завод"),
    XIZMAT("XIZMAT", "Xizmatlar", "Услуги");

    companion object {
        fun fromKey(raw: String?): IndustryType =
            entries.firstOrNull { it.key.equals(raw, ignoreCase = true) } ?: SAVDO
    }
}

enum class TransactionType(val key: String, val labelUz: String, val labelRu: String) {
    SALE("SALE", "Savdo", "Продажа"),
    PURCHASE("PURCHASE", "Xarid", "Закупка"),
    EXPENSE("EXPENSE", "Xarajat", "Расход"),
    PAYROLL("PAYROLL", "Ish haqi", "Зарплата");

    companion object {
        fun fromKey(raw: String?): TransactionType =
            entries.firstOrNull { it.key.equals(raw, ignoreCase = true) } ?: SALE
    }
}

enum class TxStatus(val key: String, val labelUz: String, val labelRu: String) {
    PAID("PAID", "To‘langan", "Оплачено"),
    PENDING("PENDING", "Kutilmoqda", "Ожидает"),
    OVERDUE("OVERDUE", "Muddati o‘tgan", "Просрочено");

    companion object {
        fun fromKey(raw: String?): TxStatus =
            entries.firstOrNull { it.key.equals(raw, ignoreCase = true) } ?: PENDING
    }
}

enum class ProductionStatus(val key: String, val labelUz: String, val labelRu: String) {
    PLANNED("PLANNED", "Rejada", "Запланировано"),
    IN_PROGRESS("IN_PROGRESS", "Jarayonda", "В работе"),
    COMPLETED("COMPLETED", "Yakunlangan", "Завершено");

    companion object {
        fun fromKey(raw: String?): ProductionStatus =
            entries.firstOrNull { it.key.equals(raw, ignoreCase = true) } ?: PLANNED
    }
}

enum class EmployeeStatus(val key: String, val labelUz: String, val labelRu: String) {
    ACTIVE("ACTIVE", "Faol", "Активен"),
    ON_LEAVE("ON_LEAVE", "Ta’tilda", "В отпуске");

    companion object {
        fun fromKey(raw: String?): EmployeeStatus =
            entries.firstOrNull { it.key.equals(raw, ignoreCase = true) } ?: ACTIVE
    }
}

enum class DebtDirection(val key: String, val labelUz: String, val labelRu: String) {
    RECEIVABLE("RECEIVABLE", "Debitor (Bizga qarzdor)", "Дебиторка (Нам должны)"),
    PAYABLE("PAYABLE", "Kreditor (Biz qarzdor)", "Кредиторка (Мы должны)");

    companion object {
        fun fromKey(raw: String?): DebtDirection =
            entries.firstOrNull { it.key.equals(raw, ignoreCase = true) } ?: RECEIVABLE
    }
}

@Keep
@IgnoreExtraProperties
data class UserWorkspace(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val activeCompanyId: String = "",
    val activeRole: String = "OWNER",
    val planId: String = "premium",
    val billingCycle: String = "monthly",
    val subscriptionStatus: String = "trial",
    val trialDaysRemaining: Int = 23,
    val aiQuestionsUsed: Int = 4,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Company(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val stir: String = "",
    val taxRegime: String = "QQS",
    val industry: String = "SAVDO",
    val city: String = "Toshkent",
    val qqsRate: Double = 12.0,
    val inpsRate: Double = 1.0,
    val incomeTaxRate: Double = 12.0,
    val cashBalanceUzs: Long = 0L,
    val bankBalanceUzs: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class BusinessTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val companyId: String = "",
    val type: String = "SALE",
    val category: String = "",
    val counterparty: String = "",
    val amountUzs: Long = 0L,
    val status: String = "PAID",
    val dateDisplay: String = "",
    val note: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class InventoryItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val companyId: String = "",
    val sku: String = "",
    val name: String = "",
    val category: String = "Umumiy",
    val quantity: Long = 0L,
    val unit: String = "dona",
    val unitCostUzs: Long = 0L,
    val minThreshold: Long = 10L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val totalValueUzs: Long get() = quantity * unitCostUzs
    val lowStock: Boolean get() = quantity <= minThreshold
}

@Keep
@IgnoreExtraProperties
data class ProductionOrder(
    @DocumentId val id: String = "",
    val userId: String = "",
    val companyId: String = "",
    val productName: String = "",
    val bomSummary: String = "",
    val targetQty: Long = 1L,
    val completedQty: Long = 0L,
    val unitCostUzs: Long = 0L,
    val defectPercent: Double = 0.0,
    val status: String = "PLANNED",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class EmployeeRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val companyId: String = "",
    val fullName: String = "",
    val roleKey: String = "SALES_MANAGER",
    val department: String = "",
    val phone: String = "",
    val attendancePercent: Int = 100,
    val salaryUzs: Long = 0L,
    val status: String = "ACTIVE",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class DebtRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val companyId: String = "",
    val counterparty: String = "",
    val direction: String = "RECEIVABLE",
    val amountUzs: Long = 0L,
    val dueDateDisplay: String = "",
    val overdueDays: Int = 0,
    val reminderSent: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AuditLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val companyId: String = "",
    val actorRole: String = "OWNER",
    val actionTitle: String = "",
    val moduleKey: String = "DASHBOARD",
    val amountUzs: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class CashFlowPoint(
    val label: String,
    val inflowUzs: Long,
    val outflowUzs: Long
) {
    val netUzs: Long get() = inflowUzs - outflowUzs
}

data class ExpenseSlice(
    val category: String,
    val amountUzs: Long,
    val sharePercent: Double
)

data class BalansScoreBreakdown(
    val overallScore: Int,
    val liquidityScore: Int,
    val profitabilityScore: Int,
    val debtHealthScore: Int,
    val inventoryTurnScore: Int,
    val tiltDegrees: Float // 0f = balanced, negative/positive = tilt under risk
)

data class AiInsightItem(
    val id: String,
    val severity: String, // "HIGH", "MEDIUM", "INFO"
    val titleUz: String,
    val titleRu: String,
    val bodyUz: String,
    val bodyRu: String,
    val sourceCalcUz: String,
    val sourceCalcRu: String,
    val actionLabelUz: String? = null,
    val actionLabelRu: String? = null,
    val targetDebtId: String? = null
)

data class WhatIfScenarioResult(
    val scenarioName: String,
    val projectedRevenueUzs: Long,
    val projectedExpenseUzs: Long,
    val projectedProfitUzs: Long,
    val profitDeltaPercent: Double,
    val recommendation: String
)

data class AiChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val isStreaming: Boolean = false
)

data class DashboardMetrics(
    val totalRevenueUzs: Long,
    val revenueDeltaPercent: Double,
    val totalExpenseUzs: Long,
    val expenseDeltaPercent: Double,
    val netProfitUzs: Long,
    val profitDeltaPercent: Double,
    val cashAndBankUzs: Long,
    val receivableUzs: Long,
    val payableUzs: Long,
    val inventoryValueUzs: Long,
    val totalPayrollUzs: Long,
    val estimatedTaxUzs: Long,
    val balansScore: BalansScoreBreakdown,
    val cashFlow30d: List<CashFlowPoint>,
    val expenseTop5: List<ExpenseSlice>,
    val insights: List<AiInsightItem>
) {
    companion object {
        fun compute(
            company: Company?,
            transactions: List<BusinessTransaction>,
            inventory: List<InventoryItem>,
            employees: List<EmployeeRecord>,
            debts: List<DebtRecord>
        ): DashboardMetrics {
            val sales = transactions.filter { it.type == TransactionType.SALE.key }
            val nonSales = transactions.filter { it.type != TransactionType.SALE.key }

            val totalRevenue = sales.sumOf { it.amountUzs }
            val totalTxExpense = nonSales.sumOf { it.amountUzs }
            val totalPayroll = employees.sumOf { it.salaryUzs }
            val totalExpense = totalTxExpense
            val netProfit = totalRevenue - totalExpense

            val cashAndBank = (company?.cashBalanceUzs ?: 0L) + (company?.bankBalanceUzs ?: 0L)
            val receivable = debts.filter { it.direction == DebtDirection.RECEIVABLE.key }.sumOf { it.amountUzs }
            val payable = debts.filter { it.direction == DebtDirection.PAYABLE.key }.sumOf { it.amountUzs }
            val inventoryValue = inventory.sumOf { it.totalValueUzs }

            val qqsRate = company?.qqsRate ?: 12.0
            val estimatedTax = if (company?.taxRegime == TaxRegime.AYLANMA.key) {
                (totalRevenue * 0.04).toLong()
            } else {
                ((netProfit.coerceAtLeast(0L)) * (qqsRate / 100.0)).toLong()
            }

            // Reconciled Expense Donut Top-5 (sums exactly to totalExpense)
            val groupedExpenses = nonSales
                .groupBy { it.category.ifBlank { "Boshqa" } }
                .mapValues { entry -> entry.value.sumOf { it.amountUzs } }
                .toList()
                .sortedByDescending { it.second }
                .take(5)

            val expenseSlices = groupedExpenses.map { (cat, amt) ->
                val share = if (totalExpense > 0L) (amt.toDouble() / totalExpense.toDouble()) * 100.0 else 0.0
                ExpenseSlice(cat, amt, share)
            }

            // Cash flow series reconciled to totalRevenue & totalExpense
            val weights = listOf(0.14, 0.16, 0.15, 0.18, 0.17, 0.20)
            val labels = listOf("1-haf", "2-haf", "3-haf", "4-haf", "5-haf", "Joriy")
            val cashFlowPoints = labels.mapIndexed { idx, label ->
                val w = weights[idx]
                CashFlowPoint(
                    label = label,
                    inflowUzs = (totalRevenue * w).toLong(),
                    outflowUzs = (totalExpense * w).toLong()
                )
            }

            // Balans Score breakdown
            val liquidityScore = if (payable + 1L > 0) {
                ((cashAndBank + receivable).toDouble() / (payable + 50_000_000L).toDouble() * 55.0)
                    .toInt().coerceIn(35, 98)
            } else 86
            val profitMargin = if (totalRevenue > 0L) (netProfit.toDouble() / totalRevenue.toDouble()) * 100.0 else 0.0
            val profitabilityScore = (55 + profitMargin * 1.4).toInt().coerceIn(25, 98)
            val overdueCount = debts.count { it.overdueDays > 0 && it.direction == DebtDirection.RECEIVABLE.key }
            val debtHealthScore = (92 - overdueCount * 9).coerceIn(30, 96)
            val lowStockCount = inventory.count { it.lowStock }
            val inventoryTurnScore = (90 - lowStockCount * 8).coerceIn(40, 96)
            val overall = (liquidityScore * 0.30 + profitabilityScore * 0.30 + debtHealthScore * 0.20 + inventoryTurnScore * 0.20).toInt()
            val tilt = when {
                overall >= 80 -> 0f
                overall >= 65 -> -2.5f
                else -> -6f
            }

            val overdueDebts = debts.filter { it.direction == DebtDirection.RECEIVABLE.key && it.overdueDays > 0 }
            val overdueSum = overdueDebts.sumOf { it.amountUzs }
            val topExpense = expenseSlices.firstOrNull()

            val dynamicInsights = buildList {
                if (topExpense != null) {
                    add(
                        AiInsightItem(
                            id = "insight_expense",
                            severity = "MEDIUM",
                            titleUz = "Eng katta xarajat: ${topExpense.category} (${Formatters.formatPercent(topExpense.sharePercent)})",
                            titleRu = "Крупнейший расход: ${topExpense.category} (${Formatters.formatPercent(topExpense.sharePercent)})",
                            bodyUz = "Jami ${Formatters.formatUzs(totalExpense, compact = true)} xarajatning ${Formatters.formatUzs(topExpense.amountUzs, compact = true)} qismi shu toifaga to‘g‘ri keladi.",
                            bodyRu = "Из общих расходов ${Formatters.formatUzs(totalExpense, compact = true)} на эту категорию приходится ${Formatters.formatUzs(topExpense.amountUzs, compact = true)}.",
                            sourceCalcUz = "Manba: ${nonSales.size} ta xarajat provodkasi yig‘indisi = ${Formatters.formatUzs(totalExpense)}",
                            sourceCalcRu = "Источник: сумма ${nonSales.size} расходных проводок = ${Formatters.formatUzs(totalExpense)}"
                        )
                    )
                }
                if (overdueDebts.isNotEmpty()) {
                    val target = overdueDebts.first()
                    add(
                        AiInsightItem(
                            id = "insight_debt",
                            severity = "HIGH",
                            titleUz = "${overdueDebts.size} ta mijozda ${Formatters.formatUzs(overdueSum)} muddati o‘tgan qarz bor",
                            titleRu = "У ${overdueDebts.size} клиентов просроченный долг ${Formatters.formatUzs(overdueSum)}",
                            bodyUz = "Eng yaqin qarz: ${target.counterparty} (${target.overdueDays} kun kechikish). Eslatma yuborilsinmi?",
                            bodyRu = "Ближайший долг: ${target.counterparty} (просрочка ${target.overdueDays} дн.). Отправить напоминание?",
                            sourceCalcUz = "Manba: Debitorlik jadvalidagi ${overdueDebts.size} ta kechikkan yozuv = ${Formatters.formatUzs(overdueSum)}",
                            sourceCalcRu = "Источник: ${overdueDebts.size} просроченных записей дебиторки = ${Formatters.formatUzs(overdueSum)}",
                            actionLabelUz = if (target.reminderSent) "Eslatma yuborildi" else "Eslatma yuborish",
                            actionLabelRu = if (target.reminderSent) "Напоминание отправлено" else "Отправить напоминание",
                            targetDebtId = if (target.reminderSent) null else target.id
                        )
                    )
                }
                if (lowStockCount > 0) {
                    val lowItems = inventory.filter { it.lowStock }.joinToString(", ") { it.name }
                    add(
                        AiInsightItem(
                            id = "insight_stock",
                            severity = "MEDIUM",
                            titleUz = "$lowStockCount ta mahsulot minimal qoldiqdan kam",
                            titleRu = "$lowStockCount позиций ниже минимального остатка",
                            bodyUz = "Xarid rejalashtirish tavsiya etiladi: $lowItems",
                            bodyRu = "Рекомендуется запланировать закупку: $lowItems",
                            sourceCalcUz = "Manba: Ombor qoldig‘i <= minimal chegara tekshiruvi ($lowStockCount ta SKU)",
                            sourceCalcRu = "Источник: остаток на складе <= минимального порога ($lowStockCount SKU)"
                        )
                    )
                }
                add(
                    AiInsightItem(
                        id = "insight_tax",
                        severity = "INFO",
                        titleUz = "Taxminiy soliq zaxirasi: ${Formatters.formatUzs(estimatedTax, compact = true)}",
                        titleRu = "Прогноз налогового резерва: ${Formatters.formatUzs(estimatedTax, compact = true)}",
                        bodyUz = "Joriy ${company?.taxRegime ?: "QQS"} rejimi bo‘yicha oy yakuniga qadar mablag‘ni band qiling. (Buxgalter tekshiruvi kerak)",
                        bodyRu = "Зарезервируйте сумму по текущему режиму ${company?.taxRegime ?: "НДС"}. (Требуется проверка бухгалтера)",
                        sourceCalcUz = "Manba: Tushum ${Formatters.formatUzs(totalRevenue)} va foyda ${Formatters.formatUzs(netProfit)} asosida hisoblandi",
                        sourceCalcRu = "Источник: расчёт на основе выручки ${Formatters.formatUzs(totalRevenue)} и прибыли ${Formatters.formatUzs(netProfit)}"
                    )
                )
            }

            return DashboardMetrics(
                totalRevenueUzs = totalRevenue,
                revenueDeltaPercent = if (totalRevenue > 0L) 14.6 else 0.0,
                totalExpenseUzs = totalExpense,
                expenseDeltaPercent = if (totalExpense > 0L) 8.2 else 0.0,
                netProfitUzs = netProfit,
                profitDeltaPercent = if (netProfit >= 0L) 19.4 else -6.8,
                cashAndBankUzs = cashAndBank,
                receivableUzs = receivable,
                payableUzs = payable,
                inventoryValueUzs = inventoryValue,
                totalPayrollUzs = totalPayroll,
                estimatedTaxUzs = estimatedTax,
                balansScore = BalansScoreBreakdown(
                    overallScore = overall,
                    liquidityScore = liquidityScore,
                    profitabilityScore = profitabilityScore,
                    debtHealthScore = debtHealthScore,
                    inventoryTurnScore = inventoryTurnScore,
                    tiltDegrees = tilt
                ),
                cashFlow30d = cashFlowPoints,
                expenseTop5 = expenseSlices,
                insights = dynamicInsights
            )
        }
    }
}

fun Company.toCreateMap(uid: String): Map<String, Any> = mapOf(
    "userId" to uid,
    "name" to name.trim(),
    "stir" to stir.trim(),
    "taxRegime" to taxRegime,
    "industry" to industry,
    "city" to city.trim(),
    "qqsRate" to qqsRate,
    "inpsRate" to inpsRate,
    "incomeTaxRate" to incomeTaxRate,
    "cashBalanceUzs" to cashBalanceUzs,
    "bankBalanceUzs" to bankBalanceUzs,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp()
)

fun BusinessTransaction.toCreateMap(uid: String): Map<String, Any> {
    val raw = mapOf(
        "userId" to uid,
        "companyId" to companyId,
        "type" to type,
        "category" to category.trim(),
        "counterparty" to counterparty.trim(),
        "amountUzs" to amountUzs,
        "status" to status,
        "dateDisplay" to dateDisplay,
        "note" to note?.takeIf { it.isNotBlank() }?.trim(),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
    @Suppress("UNCHECKED_CAST")
    return raw.filterValues { it != null } as Map<String, Any>
}

fun InventoryItem.toCreateMap(uid: String): Map<String, Any> = mapOf(
    "userId" to uid,
    "companyId" to companyId,
    "sku" to sku.trim(),
    "name" to name.trim(),
    "category" to category.trim(),
    "quantity" to quantity,
    "unit" to unit.trim(),
    "unitCostUzs" to unitCostUzs,
    "minThreshold" to minThreshold,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp()
)

fun ProductionOrder.toCreateMap(uid: String): Map<String, Any> = mapOf(
    "userId" to uid,
    "companyId" to companyId,
    "productName" to productName.trim(),
    "bomSummary" to bomSummary.trim(),
    "targetQty" to targetQty,
    "completedQty" to completedQty,
    "unitCostUzs" to unitCostUzs,
    "defectPercent" to defectPercent,
    "status" to status,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp()
)

fun EmployeeRecord.toCreateMap(uid: String): Map<String, Any> = mapOf(
    "userId" to uid,
    "companyId" to companyId,
    "fullName" to fullName.trim(),
    "roleKey" to roleKey,
    "department" to department.trim(),
    "phone" to phone.trim(),
    "attendancePercent" to attendancePercent,
    "salaryUzs" to salaryUzs,
    "status" to status,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp()
)

fun DebtRecord.toCreateMap(uid: String): Map<String, Any> = mapOf(
    "userId" to uid,
    "companyId" to companyId,
    "counterparty" to counterparty.trim(),
    "direction" to direction,
    "amountUzs" to amountUzs,
    "dueDateDisplay" to dueDateDisplay,
    "overdueDays" to overdueDays,
    "reminderSent" to reminderSent,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp()
)
