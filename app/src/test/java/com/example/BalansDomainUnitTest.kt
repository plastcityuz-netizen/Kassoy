package com.example

import com.example.core.AppStrings
import com.example.core.BillingCycle
import com.example.core.EmployeeRole
import com.example.core.Formatters
import com.example.core.PlanId
import com.example.core.PricingConfig
import com.example.core.RbacPolicy
import com.example.core.SubscriptionStatus
import com.example.data.Company
import com.example.data.DashboardMetrics
import com.example.data.SeedDataFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BalansDomainUnitTest {

    @Test
    fun pricingConfig_yearlyIsTenMonthsAndModulesGateProperly() {
        assertEquals(4_490_000L, PricingConfig.priceFor(PlanId.START, BillingCycle.YEARLY))
        assertEquals(9_900_000L, PricingConfig.priceFor(PlanId.BUSINESS, BillingCycle.YEARLY))
        assertEquals(17_900_000L, PricingConfig.priceFor(PlanId.PREMIUM, BillingCycle.YEARLY))

        assertTrue(PricingConfig.isModuleUnlocked(PlanId.START, SubscriptionStatus.ACTIVE, "sales"))
        assertFalse(PricingConfig.isModuleUnlocked(PlanId.START, SubscriptionStatus.ACTIVE, "production"))
        assertTrue(PricingConfig.isModuleUnlocked(PlanId.START, SubscriptionStatus.TRIAL, "production"))
        assertTrue(PricingConfig.shouldShowTrialBanner(SubscriptionStatus.TRIAL, 7))
        assertTrue(PricingConfig.shouldShowTrialBanner(SubscriptionStatus.TRIAL, 1))
        assertFalse(PricingConfig.shouldShowTrialBanner(SubscriptionStatus.TRIAL, 23))
    }

    @Test
    fun rbacPolicy_restrictsSensitiveProfitAndPayroll() {
        assertTrue(RbacPolicy.canViewSensitiveProfit(EmployeeRole.OWNER))
        assertTrue(RbacPolicy.canViewSensitiveProfit(EmployeeRole.CHIEF_ACCOUNTANT))
        assertFalse(RbacPolicy.canViewSensitiveProfit(EmployeeRole.WAREHOUSE))
        assertFalse(RbacPolicy.canViewSensitiveProfit(EmployeeRole.SALES_MANAGER))
        assertFalse(RbacPolicy.canViewPayroll(EmployeeRole.WAREHOUSE))
        assertTrue(RbacPolicy.canViewPayroll(EmployeeRole.HR))
        assertFalse(RbacPolicy.canMutateData(SubscriptionStatus.READ_ONLY))
    }

    @Test
    fun formatters_uzsStirPhoneValidation() {
        assertEquals("1\u00A0250\u00A0000 so‘m", Formatters.formatUzs(1_250_000L))
        assertEquals("1,2 mlrd so‘m", Formatters.formatUzs(1_200_000_000L, compact = true))
        assertTrue(Formatters.formatDeltaPercent(14.6).startsWith("▲"))
        assertTrue(Formatters.formatDeltaPercent(-4.2).startsWith("▼"))
        assertTrue(Formatters.isValidStir("305412890"))
        assertFalse(Formatters.isValidStir("12345"))
        assertTrue(Formatters.isValidUzPhone("+998 90 123 45 67"))
        assertFalse(Formatters.isValidUzPhone("12345"))
        assertTrue(Formatters.isValidEmail("owner@balans.uz"))
    }

    @Test
    fun dashboardMetrics_reconcilesTotalsAndSeedData() {
        val company = Company(
            userId = "u1",
            name = "Test MCHJ",
            stir = "305412890",
            cashBalanceUzs = 100_000_000L,
            bankBalanceUzs = 200_000_000L
        )
        val bundle = SeedDataFactory.createUzbekDemoBundle("comp_1", company)
        val txs = bundle.transactions.filter { it.companyId == "comp_1" }
        val inv = bundle.inventory.filter { it.companyId == "comp_1" }
        val emps = bundle.employees.filter { it.companyId == "comp_1" }
        val debts = bundle.debts.filter { it.companyId == "comp_1" }

        val metrics = DashboardMetrics.compute(company, txs, inv, emps, debts)
        assertEquals(metrics.totalRevenueUzs - metrics.totalExpenseUzs, metrics.netProfitUzs)
        assertEquals(300_000_000L, metrics.cashAndBankUzs)
        assertEquals(metrics.expenseTop5.sumOf { it.amountUzs }, metrics.totalExpenseUzs)
    }

    @Test
    fun localizationStrings_bothUzAndRuArePopulated() {
        assertTrue(AppStrings.uz.heroTitle.isNotBlank())
        assertTrue(AppStrings.ru.heroTitle.isNotBlank())
        assertEquals(4, AppStrings.uz.flowSteps.size)
        assertEquals(4, AppStrings.ru.flowSteps.size)
    }
}
