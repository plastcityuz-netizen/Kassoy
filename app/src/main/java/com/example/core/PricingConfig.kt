package com.example.core

enum class PlanId(val id: String) {
    START("start"),
    BUSINESS("business"),
    PREMIUM("premium"),
    ENTERPRISE("enterprise");

    companion object {
        fun fromId(raw: String?): PlanId =
            entries.firstOrNull { it.id.equals(raw, ignoreCase = true) } ?: PREMIUM
    }
}

enum class BillingCycle(val id: String) {
    MONTHLY("monthly"),
    YEARLY("yearly");

    companion object {
        fun fromId(raw: String?): BillingCycle =
            entries.firstOrNull { it.id.equals(raw, ignoreCase = true) } ?: MONTHLY
    }
}

enum class SubscriptionStatus(val id: String) {
    TRIAL("trial"),
    ACTIVE("active"),
    PAST_DUE("past_due"),
    READ_ONLY("read_only");

    companion object {
        fun fromId(raw: String?): SubscriptionStatus =
            entries.firstOrNull { it.id.equals(raw, ignoreCase = true) } ?: TRIAL
    }
}

data class PlanSpec(
    val planId: PlanId,
    val title: String,
    val subtitleUz: String,
    val subtitleRu: String,
    val monthlyPriceUzs: Long?, // null = custom agreement ("Kelishuv asosida")
    val maxCompanies: Int?,     // null = unlimited
    val maxUsers: Int?,
    val aiQuestionsPerMonth: Int?,
    val isPopular: Boolean = false,
    val includedModules: Set<String>,
    val featuresUz: List<String>,
    val featuresRu: List<String>
) {
    val yearlyPriceUzs: Long?
        get() = monthlyPriceUzs?.times(10L)
}

object PricingConfig {
    const val TRIAL_DAYS_TOTAL = 30
    const val TRIAL_AI_LIMIT = 100
    const val GRACE_PERIOD_DAYS = 3
    const val EXTRA_USER_MONTHLY_UZS = 59_000L
    const val EXTRA_COMPANY_MONTHLY_UZS = 290_000L
    const val MIGRATION_SERVICE_ONE_TIME_UZS = 990_000L

    val plans: List<PlanSpec> = listOf(
        PlanSpec(
            planId = PlanId.START,
            title = "Start",
            subtitleUz = "YaTT va kichik do‘konlar",
            subtitleRu = "ИП и небольшие магазины",
            monthlyPriceUzs = 449_000L,
            maxCompanies = 1,
            maxUsers = 3,
            aiQuestionsPerMonth = 30,
            isPopular = false,
            includedModules = setOf("dashboard", "ai_cfo", "sales", "inventory", "accounting", "reports"),
            featuresUz = listOf(
                "1 ta kompaniya, 3 ta foydalanuvchi",
                "AI CFO: 30 savol/oy",
                "Buxgalteriya, Savdo, Ombor, asosiy hisobotlar"
            ),
            featuresRu = listOf(
                "1 компания, 3 пользователя",
                "AI CFO: 30 вопросов/мес",
                "Бухгалтерия, Продажи, Склад, базовые отчёты"
            )
        ),
        PlanSpec(
            planId = PlanId.BUSINESS,
            title = "Business",
            subtitleUz = "Rivojlanayotgan MCHJ va savdo kompaniyalari",
            subtitleRu = "Растущие ООО и торговые компании",
            monthlyPriceUzs = 990_000L,
            maxCompanies = 1,
            maxUsers = 10,
            aiQuestionsPerMonth = 300,
            isPopular = true,
            includedModules = setOf(
                "dashboard", "ai_cfo", "sales", "inventory", "purchases",
                "finance", "hr", "production", "accounting", "reports"
            ),
            featuresUz = listOf(
                "1 ta kompaniya, 10 ta foydalanuvchi",
                "AI CFO: 300 savol/oy + Ertalabki brif",
                "Xarid, Moliya & Cash Flow, HR, RBAC, Balans Score, BOM"
            ),
            featuresRu = listOf(
                "1 компания, 10 пользователей",
                "AI CFO: 300 вопросов/мес + Утренний бриф",
                "Закупки, Финансы & Cash Flow, HR, RBAC, Balans Score, BOM"
            )
        ),
        PlanSpec(
            planId = PlanId.PREMIUM,
            title = "Premium",
            subtitleUz = "Zavodlar va ko‘p tarmoqli kompaniyalar",
            subtitleRu = "Заводы и многопрофильные компании",
            monthlyPriceUzs = 1_790_000L,
            maxCompanies = 3,
            maxUsers = 30,
            aiQuestionsPerMonth = null,
            isPopular = false,
            includedModules = setOf(
                "dashboard", "ai_cfo", "sales", "inventory", "purchases",
                "finance", "hr", "production", "accounting", "reports", "audit"
            ),
            featuresUz = listOf(
                "3 ta kompaniya, 30 ta foydalanuvchi",
                "AI CFO: Cheksiz + “Agar … bo‘lsa?” stsenariylari",
                "To‘liq ishlab chiqarish, tannarx (brak), Audit log"
            ),
            featuresRu = listOf(
                "3 компании, 30 пользователей",
                "AI CFO: Безлимит + сценарии «Что если…?»",
                "Полное производство, себестоимость (брак), Audit log"
            )
        ),
        PlanSpec(
            planId = PlanId.ENTERPRISE,
            title = "Enterprise",
            subtitleUz = "Yirik kompaniyalar va xoldinglar",
            subtitleRu = "Крупные компании и холдинги",
            monthlyPriceUzs = null,
            maxCompanies = null,
            maxUsers = null,
            aiQuestionsPerMonth = null,
            isPopular = false,
            includedModules = setOf(
                "dashboard", "ai_cfo", "sales", "inventory", "purchases",
                "finance", "hr", "production", "accounting", "reports", "audit"
            ),
            featuresUz = listOf(
                "Cheksiz kompaniya va foydalanuvchi",
                "Maxsus AI CFO va shaxsiy menejer",
                "SLA, maxsus integratsiyalar, jamoani o‘qitish"
            ),
            featuresRu = listOf(
                "Безлимит компаний и пользователей",
                "Выделенный AI CFO и персональный менеджер",
                "SLA, кастомные интеграции, обучение команды"
            )
        )
    )

    fun specFor(planId: PlanId): PlanSpec =
        plans.firstOrNull { it.planId == planId } ?: plans[2]

    fun priceFor(planId: PlanId, cycle: BillingCycle): Long? {
        val spec = specFor(planId)
        return when (cycle) {
            BillingCycle.MONTHLY -> spec.monthlyPriceUzs
            BillingCycle.YEARLY -> spec.yearlyPriceUzs
        }
    }

    fun isModuleUnlocked(planId: PlanId, status: SubscriptionStatus, moduleKey: String): Boolean {
        if (status == SubscriptionStatus.TRIAL) return true
        return specFor(planId).includedModules.contains(moduleKey)
    }

    fun shouldShowTrialBanner(status: SubscriptionStatus, daysRemaining: Int): Boolean =
        status == SubscriptionStatus.TRIAL && daysRemaining in 0..7
}

enum class PaymentMethod(val id: String, val labelUz: String, val labelRu: String) {
    PAYME("payme", "Payme", "Payme"),
    CLICK("click", "Click", "Click"),
    UZUM("uzum", "Uzum Bank", "Uzum Bank"),
    BANK_INVOICE("invoice", "Hisob-faktura (Yuridik shaxs)", "Счёт на оплату (Юр. лицо)")
}

data class PaymentReceipt(
    val invoiceNumber: String,
    val planId: PlanId,
    val cycle: BillingCycle,
    val method: PaymentMethod,
    val amountUzs: Long,
    val dateDisplay: String,
    val statusLabel: String
)

interface PaymentProvider {
    suspend fun processPayment(
        planId: PlanId,
        cycle: BillingCycle,
        method: PaymentMethod,
        promoCode: String?
    ): Result<PaymentReceipt>
}

class MockPaymentProvider : PaymentProvider {
    override suspend fun processPayment(
        planId: PlanId,
        cycle: BillingCycle,
        method: PaymentMethod,
        promoCode: String?
    ): Result<PaymentReceipt> {
        val basePrice = PricingConfig.priceFor(planId, cycle) ?: 3_500_000L
        val discount = if (promoCode?.trim()?.uppercase() == "BALANS10") (basePrice * 0.10).toLong() else 0L
        val finalAmount = (basePrice - discount).coerceAtLeast(0L)
        return Result.success(
            PaymentReceipt(
                invoiceNumber = "INV-${(100000..999999).random()}",
                planId = planId,
                cycle = cycle,
                method = method,
                amountUzs = finalAmount,
                dateDisplay = Formatters.formatDateDdMmYyyy(System.currentTimeMillis()),
                statusLabel = if (method == PaymentMethod.BANK_INVOICE) "Hisob yaratildi (Demo)" else "To‘landi (Demo)"
            )
        )
    }
}
