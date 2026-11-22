package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.Formatters
import com.example.core.PricingConfig
import com.example.core.RbacPolicy
import com.example.data.AiInsightItem
import com.example.data.DebtRecord
import com.example.ui.BalansCockpitState
import com.example.ui.components.BalansLineBeam
import com.example.ui.components.BalansScoreGauge
import com.example.ui.components.CashFlowAreaChart
import com.example.ui.components.ExpenseDonutChart
import com.example.ui.components.GlassCard
import com.example.ui.components.MetricDeltaBadge
import com.example.ui.components.SolidCard
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.LocalBalansPalette
import com.example.ui.theme.TabularNumberStyle

private data class KpiCardData(
    val title: String,
    val amountUzs: Long,
    val delta: Double?,
    val sensitiveProfit: Boolean = false,
    val highlightPrimary: Boolean = false
)

@Composable
fun DashboardScreen(
    state: BalansCockpitState,
    isExpandedWidth: Boolean,
    onOpenAiCfo: () -> Unit,
    onOpenPricing: () -> Unit,
    onConfirmDebtReminder: (DebtRecord) -> Unit
) {
    val palette = LocalBalansPalette.current
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    var selectedCashFlowWindow by remember { mutableIntStateOf(30) }
    var explainingInsight by remember { mutableStateOf<AiInsightItem?>(null) }
    var pendingConfirmDebt by remember { mutableStateOf<DebtRecord?>(null) }

    val canSeeProfit = RbacPolicy.canViewSensitiveProfit(state.activeRole)
    val canApprove = RbacPolicy.canApproveFinancialActions(state.activeRole)
    val metrics = state.metrics

    val kpiCards = listOf(
        KpiCardData(strings.kpiNetProfit, metrics.netProfitUzs, metrics.profitDeltaPercent, sensitiveProfit = true, highlightPrimary = true),
        KpiCardData(strings.kpiRevenue, metrics.totalRevenueUzs, metrics.revenueDeltaPercent),
        KpiCardData(strings.kpiExpense, metrics.totalExpenseUzs, metrics.expenseDeltaPercent),
        KpiCardData(strings.kpiCashAndBank, metrics.cashAndBankUzs, null),
        KpiCardData(strings.kpiReceivable, metrics.receivableUzs - metrics.payableUzs, null),
        KpiCardData(strings.kpiInventoryValue, metrics.inventoryValueUzs, null)
    )

    val daysLeft = state.workspace?.trialDaysRemaining ?: 23

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Trial countdown banner
        if (state.currentStatus.id == "trial") {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = palette.emerald.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, palette.emerald.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenPricing)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${strings.trialBannerPrefix} $daysLeft kun qoldi (${state.currentPlan.name})",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.emeraldText
                        )
                        Text(
                            text = strings.selectPlan,
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.textPrimary
                        )
                    }
                }
            }
        }

        // Top Row: Balans Score + Sub-metrics
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentBorder = palette.emerald.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = strings.balansScoreTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.textPrimary
                        )
                        Text(
                            text = state.activeCompany?.name ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textMuted
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = palette.elevated
                    ) {
                        Text(
                            text = state.activeRole.label(lang),
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.emeraldText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                BalansLineBeam(
                    modifier = Modifier.fillMaxWidth(),
                    tiltDegrees = metrics.balansScore.tiltDegrees
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BalansScoreGauge(score = metrics.balansScore.overallScore)
                    Spacer(Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScoreSubMetricRow(strings.liquidityLabel, metrics.balansScore.liquidityScore)
                        ScoreSubMetricRow(strings.profitabilityLabel, if (canSeeProfit) metrics.balansScore.profitabilityScore else 0, hidden = !canSeeProfit)
                        ScoreSubMetricRow(strings.debtHealthLabel, metrics.balansScore.debtHealthScore)
                        ScoreSubMetricRow(strings.inventoryTurnLabel, metrics.balansScore.inventoryTurnScore)
                    }
                }
            }
        }

        // KPI Cards: Swipe carousel on compact phone, 3-column Bento grid on expanded screen
        item {
            if (isExpandedWidth) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    kpiCards.chunked(3).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { card ->
                                KpiTileCard(
                                    card = card,
                                    canSeeProfit = canSeeProfit,
                                    hiddenLabel = strings.hiddenByRole,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(kpiCards) { card ->
                        KpiTileCard(
                            card = card,
                            canSeeProfit = canSeeProfit,
                            hiddenLabel = strings.hiddenByRole,
                            modifier = Modifier.width(210.dp)
                        )
                    }
                }
            }
        }

        // Cash Flow Area Chart (30 / 90 / 365 days)
        item {
            SolidCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.cashFlowTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.textPrimary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(30, 90, 365).forEach { days ->
                            val selected = selectedCashFlowWindow == days
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) palette.emerald.copy(alpha = 0.16f) else palette.elevated,
                                modifier = Modifier.clickable { selectedCashFlowWindow = days }
                            ) {
                                Text(
                                    text = "${days}k",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) palette.emeraldText else palette.textMuted,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                val scaledPoints = remember(metrics.cashFlow30d, selectedCashFlowWindow) {
                    val factor = when (selectedCashFlowWindow) {
                        90 -> 2.8
                        365 -> 11.2
                        else -> 1.0
                    }
                    metrics.cashFlow30d.map {
                        it.copy(
                            inflowUzs = (it.inflowUzs * factor).toLong(),
                            outflowUzs = (it.outflowUzs * factor).toLong()
                        )
                    }
                }
                CashFlowAreaChart(points = scaledPoints)
            }
        }

        // Expense Breakdown Donut Top-5
        item {
            SolidCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = strings.expenseBreakdownTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.textPrimary
                )
                Spacer(Modifier.height(12.dp))
                ExpenseDonutChart(slices = metrics.expenseTop5)
            }
        }

        // AI CFO Today Insights
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentBorder = palette.violet.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.AutoAwesome, null, tint = palette.violet)
                        Text(
                            text = strings.aiCfoTodayTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.textPrimary
                        )
                    }
                    TextButton(onClick = onOpenAiCfo) {
                        Text("AI CFO →", color = palette.emeraldText)
                    }
                }
                Spacer(Modifier.height(8.dp))
                metrics.insights.forEach { insight ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = palette.surface.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, palette.border),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (lang == AppLanguage.RU) insight.titleRu else insight.titleUz,
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.textPrimary
                            )
                            Text(
                                text = if (lang == AppLanguage.RU) insight.bodyRu else insight.bodyUz,
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textMuted
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { explainingInsight = insight },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(40.dp).testTag("insight_why_${insight.id}")
                                ) {
                                    Icon(Icons.Outlined.HelpOutline, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(strings.whyButton)
                                }
                                val debtTarget = insight.targetDebtId?.let { tid ->
                                    state.debts.firstOrNull { it.id == tid }
                                }
                                if (debtTarget != null && canApprove) {
                                    Button(
                                        onClick = { pendingConfirmDebt = debtTarget },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = palette.emerald,
                                            contentColor = palette.onEmerald
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.height(40.dp).testTag("insight_send_reminder")
                                    ) {
                                        Icon(Icons.Outlined.NotificationsActive, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = if (lang == AppLanguage.RU) insight.actionLabelRu ?: strings.sendReminderAction
                                            else insight.actionLabelUz ?: strings.sendReminderAction
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Upcoming Payments & Tax Deadlines
        item {
            SolidCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.Event, null, tint = palette.warning)
                    Text(
                        text = strings.upcomingPaymentsTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.textPrimary
                    )
                }
                Spacer(Modifier.height(10.dp))
                UpcomingPaymentRow(
                    title = if (lang == AppLanguage.RU) "Расчёт ${state.activeCompany?.taxRegime ?: "НДС"}" else "${state.activeCompany?.taxRegime ?: "QQS"} soliq to‘lovi",
                    dueDate = "20.12.2026",
                    amountUzs = metrics.estimatedTaxUzs,
                    badge = strings.accountantReviewBadge
                )
                Spacer(Modifier.height(8.dp))
                UpcomingPaymentRow(
                    title = if (lang == AppLanguage.RU) "Фонд оплаты труда" else "Oylik ish haqi fondi",
                    dueDate = "05.12.2026",
                    amountUzs = if (RbacPolicy.canViewPayroll(state.activeRole)) metrics.totalPayrollUzs else 0L,
                    hidden = !RbacPolicy.canViewPayroll(state.activeRole)
                )
            }
        }
    }

    // "Nega?" Source Explanation Dialog
    explainingInsight?.let { insight ->
        AlertDialog(
            onDismissRequest = { explainingInsight = null },
            title = { Text(if (lang == AppLanguage.RU) insight.titleRu else insight.titleUz) },
            text = {
                Text(if (lang == AppLanguage.RU) insight.sourceCalcRu else insight.sourceCalcUz)
            },
            confirmButton = {
                TextButton(onClick = { explainingInsight = null }) {
                    Text("OK")
                }
            }
        )
    }

    // Confirmation-gated financial/communication action dialog
    pendingConfirmDebt?.let { debt ->
        AlertDialog(
            onDismissRequest = { pendingConfirmDebt = null },
            title = { Text(strings.confirmActionTitle) },
            text = {
                Text(
                    if (lang == AppLanguage.RU) {
                        "Отправить напоминание о долге ${Formatters.formatUzs(debt.amountUzs)} клиенту ${debt.counterparty}?"
                    } else {
                        "${debt.counterparty} mijoziga ${Formatters.formatUzs(debt.amountUzs)} qarz bo‘yicha eslatma yuborilsinmi?"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirmDebtReminder(debt)
                        pendingConfirmDebt = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.emerald,
                        contentColor = palette.onEmerald
                    )
                ) {
                    Text(strings.sendReminderAction)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingConfirmDebt = null }) {
                    Text(strings.undoLabel)
                }
            }
        )
    }
}

@Composable
private fun ScoreSubMetricRow(
    label: String,
    score: Int,
    hidden: Boolean = false
) {
    val palette = LocalBalansPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
            Text(
                text = if (hidden) "•••" else "$score%",
                style = MaterialTheme.typography.labelSmall.merge(TabularNumberStyle),
                color = palette.textPrimary
            )
        }
        LinearProgressIndicator(
            progress = { if (hidden) 0f else (score / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(5.dp),
            color = if (score >= 70) palette.emerald else palette.warning,
            trackColor = palette.border
        )
    }
}

@Composable
private fun KpiTileCard(
    card: KpiCardData,
    canSeeProfit: Boolean,
    hiddenLabel: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalBalansPalette.current
    val isLocked = card.sensitiveProfit && !canSeeProfit
    val borderColor = if (card.highlightPrimary) palette.emerald.copy(alpha = 0.45f) else palette.border

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = palette.surface,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = card.title,
                style = MaterialTheme.typography.labelMedium,
                color = palette.textMuted
            )
            if (isLocked) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Outlined.Lock, null, tint = palette.textMuted, modifier = Modifier.size(16.dp))
                    Text(
                        text = hiddenLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted
                    )
                }
            } else {
                Text(
                    text = Formatters.formatUzs(card.amountUzs, compact = true),
                    style = if (card.highlightPrimary) MaterialTheme.typography.headlineMedium.merge(TabularNumberStyle)
                    else MaterialTheme.typography.titleLarge.merge(TabularNumberStyle),
                    color = palette.textPrimary
                )
                if (card.delta != null) {
                    MetricDeltaBadge(deltaPercent = card.delta)
                } else {
                    Text(
                        text = Formatters.formatUsdFromUzs(card.amountUzs),
                        style = MaterialTheme.typography.labelSmall.merge(TabularNumberStyle),
                        color = palette.textMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun UpcomingPaymentRow(
    title: String,
    dueDate: String,
    amountUzs: Long,
    badge: String? = null,
    hidden: Boolean = false
) {
    val palette = LocalBalansPalette.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
            Text(dueDate, style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
            if (badge != null) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = palette.warning.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.warning,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Text(
            text = if (hidden) "•••" else Formatters.formatUzs(amountUzs, compact = true),
            style = MaterialTheme.typography.titleMedium.merge(TabularNumberStyle),
            color = palette.textPrimary
        )
    }
}
