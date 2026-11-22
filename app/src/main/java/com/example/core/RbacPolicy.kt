package com.example.core

enum class EmployeeRole(
    val key: String,
    val titleUz: String,
    val titleRu: String
) {
    OWNER("OWNER", "Egasi", "Владелец"),
    DIRECTOR("DIRECTOR", "Direktor", "Директор"),
    CHIEF_ACCOUNTANT("CHIEF_ACCOUNTANT", "Bosh buxgalter", "Главный бухгалтер"),
    ACCOUNTANT("ACCOUNTANT", "Buxgalter", "Бухгалтер"),
    SALES_MANAGER("SALES_MANAGER", "Sotuv menejeri", "Менеджер продаж"),
    WAREHOUSE("WAREHOUSE", "Omborchi", "Кладовщик"),
    PURCHASER("PURCHASER", "Xaridchi", "Закупщик"),
    HR("HR", "HR menejer", "HR менеджер"),
    PRODUCTION_HEAD("PRODUCTION_HEAD", "Ishlab chiqarish boshlig‘i", "Начальник производства");

    fun label(lang: AppLanguage): String = if (lang == AppLanguage.RU) titleRu else titleUz

    companion object {
        fun fromKey(raw: String?): EmployeeRole =
            entries.firstOrNull { it.key.equals(raw, ignoreCase = true) } ?: OWNER
    }
}

object RbacPolicy {

    fun canAccessModule(role: EmployeeRole, moduleKey: String): Boolean {
        return when (role) {
            EmployeeRole.OWNER, EmployeeRole.DIRECTOR -> true
            EmployeeRole.CHIEF_ACCOUNTANT -> moduleKey in setOf(
                "dashboard", "ai_cfo", "finance", "accounting", "reports", "sales", "purchases", "hr", "settings"
            )
            EmployeeRole.ACCOUNTANT -> moduleKey in setOf(
                "dashboard", "accounting", "finance", "reports", "sales", "purchases"
            )
            EmployeeRole.SALES_MANAGER -> moduleKey in setOf(
                "dashboard", "sales", "inventory", "reports"
            )
            EmployeeRole.WAREHOUSE -> moduleKey in setOf(
                "dashboard", "inventory", "purchases", "production"
            )
            EmployeeRole.PURCHASER -> moduleKey in setOf(
                "dashboard", "purchases", "inventory", "finance"
            )
            EmployeeRole.HR -> moduleKey in setOf(
                "dashboard", "hr", "reports"
            )
            EmployeeRole.PRODUCTION_HEAD -> moduleKey in setOf(
                "dashboard", "production", "inventory", "reports"
            )
        }
    }

    fun canViewSensitiveProfit(role: EmployeeRole): Boolean =
        role in setOf(EmployeeRole.OWNER, EmployeeRole.DIRECTOR, EmployeeRole.CHIEF_ACCOUNTANT)

    fun canViewPayroll(role: EmployeeRole): Boolean =
        role in setOf(EmployeeRole.OWNER, EmployeeRole.DIRECTOR, EmployeeRole.CHIEF_ACCOUNTANT, EmployeeRole.HR)

    fun canApproveFinancialActions(role: EmployeeRole): Boolean =
        role in setOf(EmployeeRole.OWNER, EmployeeRole.DIRECTOR, EmployeeRole.CHIEF_ACCOUNTANT)

    fun canManageSettings(role: EmployeeRole): Boolean =
        role in setOf(EmployeeRole.OWNER, EmployeeRole.DIRECTOR, EmployeeRole.CHIEF_ACCOUNTANT)

    fun canMutateData(status: SubscriptionStatus): Boolean =
        status != SubscriptionStatus.READ_ONLY
}
