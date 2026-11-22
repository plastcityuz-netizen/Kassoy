package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.Formatters
import com.example.core.RbacPolicy
import com.example.data.DebtDirection
import com.example.data.DebtRecord
import com.example.ui.BalansCockpitState
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SolidCard
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.LocalBalansPalette
import com.example.ui.theme.TabularNumberStyle

@Composable
fun FinanceScreen(
    state: BalansCockpitState,
    onConfirmDebtReminder: (DebtRecord) -> Unit
) {
    val palette = LocalBalansPalette.current
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    var confirmingDebt by remember { mutableStateOf<DebtRecord?>(null) }
    val canApprove = RbacPolicy.canApproveFinancialActions(state.activeRole)

    if (!RbacPolicy.canAccessModule(state.activeRole, "finance")) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            EmptyStateCard(
                title = if (lang == AppLanguage.RU) "Доступ ограничен ролью" else "Rol cheklovi",
                subtitle = "${state.activeRole.label(lang)} roli Moliya bo‘limiga kira olmaydi."
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SolidCard(modifier = Modifier.weight(1f)) {
                    Text(if (lang == AppLanguage.RU) "Касса (Наличные)" else "Kassa (Naqd)", style = MaterialTheme.typography.labelMedium, color = palette.textMuted)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = Formatters.formatUzs(state.activeCompany?.cashBalanceUzs ?: 0L, compact = true),
                        style = MaterialTheme.typography.titleLarge.merge(TabularNumberStyle),
                        color = palette.textPrimary
                    )
                }
                SolidCard(modifier = Modifier.weight(1f)) {
                    Text(if (lang == AppLanguage.RU) "Расчётный счёт" else "Bank hisob raqami", style = MaterialTheme.typography.labelMedium, color = palette.textMuted)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = Formatters.formatUzs(state.activeCompany?.bankBalanceUzs ?: 0L, compact = true),
                        style = MaterialTheme.typography.titleLarge.merge(TabularNumberStyle),
                        color = palette.emeraldText
                    )
                }
            }
        }

        item {
            Text(
                text = strings.kpiReceivable,
                style = MaterialTheme.typography.headlineMedium,
                color = palette.textPrimary
            )
        }

        if (state.debts.isEmpty()) {
            item {
                EmptyStateCard(strings.emptyTitle, strings.emptySubtitle)
            }
        } else {
            items(state.debts, key = { it.id }) { debt ->
                val isReceivable = debt.direction == DebtDirection.RECEIVABLE.key
                SolidCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(debt.counterparty, style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                            Text(
                                text = "${if (isReceivable) "Debitor" else "Kreditor"} · Muddat: ${debt.dueDateDisplay}",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textMuted
                            )
                            if (debt.overdueDays > 0) {
                                Text(
                                    text = "${debt.overdueDays} kun kechikish",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.danger
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = Formatters.formatUzs(debt.amountUzs, compact = true),
                                style = MaterialTheme.typography.titleMedium.merge(TabularNumberStyle),
                                color = if (isReceivable) palette.emeraldText else palette.warning
                            )
                            Spacer(Modifier.height(6.dp))
                            if (isReceivable && canApprove) {
                                if (debt.reminderSent) {
                                    Surface(shape = RoundedCornerShape(8.dp), color = palette.success.copy(alpha = 0.14f)) {
                                        Text(
                                            text = strings.reminderSentBadge,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = palette.success,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = { confirmingDebt = debt },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = palette.emerald,
                                            contentColor = palette.onEmerald
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(strings.sendReminderAction, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    confirmingDebt?.let { debt ->
        AlertDialog(
            onDismissRequest = { confirmingDebt = null },
            title = { Text(strings.confirmActionTitle) },
            text = { Text("${debt.counterparty} — ${Formatters.formatUzs(debt.amountUzs)}") },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirmDebtReminder(debt)
                        confirmingDebt = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.emerald, contentColor = palette.onEmerald)
                ) { Text(strings.sendReminderAction) }
            },
            dismissButton = {
                TextButton(onClick = { confirmingDebt = null }) { Text(strings.undoLabel) }
            }
        )
    }
}
