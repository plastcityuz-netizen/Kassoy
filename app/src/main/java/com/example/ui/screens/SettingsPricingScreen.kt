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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.BillingCycle
import com.example.core.EmployeeRole
import com.example.core.Formatters
import com.example.core.PaymentMethod
import com.example.core.PaymentReceipt
import com.example.core.PlanId
import com.example.core.PlanSpec
import com.example.core.PricingConfig
import com.example.core.RbacPolicy
import com.example.data.ThemePreference
import com.example.ui.BalansCockpitState
import com.example.ui.components.SolidCard
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.LocalBalansPalette
import com.example.ui.theme.TabularNumberStyle

@Composable
fun SettingsPricingScreen(
    state: BalansCockpitState,
    themePreference: ThemePreference,
    denseTableMode: Boolean,
    receipts: List<PaymentReceipt>,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onToggleDenseMode: (Boolean) -> Unit,
    onSwitchCompany: (String) -> Unit,
    onSwitchRole: (EmployeeRole) -> Unit,
    onUpdateTaxRates: (Double, Double, Double) -> Unit,
    onSubscribePlan: (PlanId, BillingCycle, PaymentMethod, String?) -> Unit,
    onSignOut: () -> Unit
) {
    val palette = LocalBalansPalette.current
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    var billingCycle by remember { mutableStateOf(state.currentCycle) }
    var checkoutPlan by remember { mutableStateOf<PlanSpec?>(null) }

    val company = state.activeCompany
    var qqsInput by remember(company?.id) { mutableStateOf((company?.qqsRate ?: 12.0).toString()) }
    var inpsInput by remember(company?.id) { mutableStateOf((company?.inpsRate ?: 1.0).toString()) }
    var incomeInput by remember(company?.id) { mutableStateOf((company?.incomeTaxRate ?: 12.0).toString()) }

    val integrations = remember {
        listOf(
            "Elektron hisob-faktura (E-Faktura / Soliq)",
            "Payme Business & Click & Uzum integratsiyasi",
            "Bank-klient (Ipoteka, Kapitalbank, NBU)",
            "Telegram Bot hisobotlar",
            "1C / Excel ommaviy import"
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Multi-Company Switcher
        item {
            SolidCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (lang == AppLanguage.RU) "Компании (Multi-tenant)" else "Kompaniyalar (Multi-company)",
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.textPrimary
                )
                Spacer(Modifier.height(10.dp))
                state.companies.forEach { comp ->
                    val active = comp.id == company?.id
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (active) palette.emerald.copy(alpha = 0.14f) else palette.elevated,
                        border = BorderStroke(1.dp, if (active) palette.emerald else palette.border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSwitchCompany(comp.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(comp.name, style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                                Text("STIR: ${comp.stir} · ${comp.city}", style = MaterialTheme.typography.bodySmall, color = palette.textMuted)
                            }
                            if (active) {
                                Text("✓", color = palette.emeraldText, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // RBAC Role Switcher & Permissions Preview
        item {
            SolidCard(modifier = Modifier.fillMaxWidth()) {
                Text(strings.rbacTitle, style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(EmployeeRole.entries) { role ->
                        val active = state.activeRole == role
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) palette.emerald.copy(alpha = 0.16f) else palette.elevated,
                            border = BorderStroke(1.dp, if (active) palette.emerald else palette.border),
                            modifier = Modifier.clickable { onSwitchRole(role) }.testTag("role_chip_${role.key}")
                        ) {
                            Text(
                                text = role.label(lang),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (active) palette.emeraldText else palette.textPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Sof foyda ko‘rish: ${if (RbacPolicy.canViewSensitiveProfit(state.activeRole)) "Ruxsat bor" else "Yashirilgan"} · Ish haqi: ${if (RbacPolicy.canViewPayroll(state.activeRole)) "Ruxsat bor" else "Yashirilgan"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textMuted
                )
            }
        }

        // Pricing & Subscription Plans (Section 11)
        item {
            SolidCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.pricingTitle, style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(BillingCycle.MONTHLY to strings.monthlyLabel, BillingCycle.YEARLY to strings.yearlyLabel).forEach { (cyc, label) ->
                            val sel = billingCycle == cyc
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (sel) palette.emerald.copy(alpha = 0.16f) else palette.elevated,
                                modifier = Modifier.clickable { billingCycle = cyc }
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (sel) palette.emeraldText else palette.textMuted,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                PricingConfig.plans.forEach { plan ->
                    val isCurrent = state.currentPlan == plan.planId
                    val price = PricingConfig.priceFor(plan.planId, billingCycle)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = palette.elevated,
                        border = BorderStroke(1.dp, if (isCurrent) palette.emerald else palette.border),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(plan.title, style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                                if (isCurrent) {
                                    Text(strings.activePlanBadge, style = MaterialTheme.typography.labelSmall, color = palette.emeraldText)
                                } else if (plan.isPopular) {
                                    Text(strings.popularBadge, style = MaterialTheme.typography.labelSmall, color = palette.cyan)
                                }
                            }
                            Text(
                                text = price?.let { Formatters.formatUzs(it) } ?: "Kelishuv asosida",
                                style = MaterialTheme.typography.titleMedium.merge(TabularNumberStyle),
                                color = palette.emeraldText
                            )
                            val feats = if (lang == AppLanguage.RU) plan.featuresRu else plan.featuresUz
                            feats.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall, color = palette.textMuted) }
                            Button(
                                onClick = { checkoutPlan = plan },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCurrent) palette.surface else palette.emerald,
                                    contentColor = if (isCurrent) palette.textPrimary else palette.onEmerald
                                ),
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            ) {
                                Text(if (isCurrent) "Yangilash / To‘lash" else strings.selectPlan)
                            }
                        }
                    }
                }
                if (receipts.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("To‘lov tarixi:", style = MaterialTheme.typography.labelLarge, color = palette.textPrimary)
                    receipts.forEach { r ->
                        Text(
                            text = "${r.invoiceNumber} · ${r.planId.name} · ${Formatters.formatUzs(r.amountUzs)} (${r.statusLabel})",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textMuted
                        )
                    }
                }
            }
        }

        // Configurable Tax Rates
        if (RbacPolicy.canManageSettings(state.activeRole)) {
            item {
                SolidCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(strings.taxSettingsTitle, style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(strings.accountantReviewBadge, style = MaterialTheme.typography.labelSmall, color = palette.warning)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = qqsInput,
                            onValueChange = { qqsInput = it },
                            label = { Text("QQS %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = inpsInput,
                            onValueChange = { inpsInput = it },
                            label = { Text("INPS %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = incomeInput,
                            onValueChange = { incomeInput = it },
                            label = { Text("Daromad %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onUpdateTaxRates(
                                qqsInput.toDoubleOrNull() ?: 12.0,
                                inpsInput.toDoubleOrNull() ?: 1.0,
                                incomeInput.toDoubleOrNull() ?: 12.0
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = palette.emerald, contentColor = palette.onEmerald),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Soliq stavkalarini saqlash")
                    }
                }
            }
        }

        // Integrations (Explicitly labeled "Tez orada")
        item {
            SolidCard(modifier = Modifier.fillMaxWidth()) {
                Text(strings.integrationsTitle, style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                Spacer(Modifier.height(8.dp))
                integrations.forEach { name ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name, style = MaterialTheme.typography.bodySmall, color = palette.textPrimary, modifier = Modifier.weight(1f))
                        Surface(shape = RoundedCornerShape(6.dp), color = palette.info.copy(alpha = 0.14f)) {
                            Text(
                                text = strings.comingSoonBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.info,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // UI & Account Controls
        item {
            SolidCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Jadval zich rejimi (Qulay / Zich)", style = MaterialTheme.typography.bodyMedium, color = palette.textPrimary)
                    Switch(checked = denseTableMode, onCheckedChange = onToggleDenseMode)
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onToggleTheme, modifier = Modifier.weight(1f)) {
                        Text(if (themePreference == ThemePreference.DARK) "Light tema" else "Dark tema")
                    }
                    OutlinedButton(onClick = onToggleLanguage, modifier = Modifier.weight(1f)) {
                        Text(if (lang == AppLanguage.UZ) "Русский язык" else "O‘zbekcha")
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth().testTag("sign_out_button")
                ) {
                    Text(strings.signOut, color = palette.danger)
                }
            }
        }

        // Audit Log
        item {
            SolidCard(modifier = Modifier.fillMaxWidth()) {
                Text(strings.auditLogTitle, style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                Spacer(Modifier.height(8.dp))
                if (state.auditLogs.isEmpty()) {
                    Text("Hozircha audit yozuvlari yo‘q", style = MaterialTheme.typography.bodySmall, color = palette.textMuted)
                } else {
                    state.auditLogs.take(10).forEach { log ->
                        Text(
                            text = "• [${log.actorRole}] ${log.actionTitle}",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textMuted,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }

    // Checkout Payment Provider Dialog (Payme / Click / Uzum / Bank Invoice)
    checkoutPlan?.let { plan ->
        var selectedMethod by remember { mutableStateOf(PaymentMethod.PAYME) }
        var promo by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { checkoutPlan = null },
            title = { Text("${plan.title} · ${if (billingCycle == BillingCycle.YEARLY) strings.yearlyLabel else strings.monthlyLabel}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentMethod.entries.forEach { method ->
                        val sel = selectedMethod == method
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (sel) palette.emerald.copy(alpha = 0.14f) else palette.elevated,
                            border = BorderStroke(1.dp, if (sel) palette.emerald else palette.border),
                            modifier = Modifier.fillMaxWidth().clickable { selectedMethod = method }
                        ) {
                            Text(
                                text = if (lang == AppLanguage.RU) method.labelRu else method.labelUz,
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textPrimary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                    OutlinedTextField(
                        value = promo,
                        onValueChange = { promo = it },
                        label = { Text("Promo-kod (masalan: BALANS10)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSubscribePlan(plan.planId, billingCycle, selectedMethod, promo.ifBlank { null })
                        checkoutPlan = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.emerald, contentColor = palette.onEmerald)
                ) {
                    Text("Tasdiqlash (Demo)")
                }
            },
            dismissButton = {
                TextButton(onClick = { checkoutPlan = null }) { Text(strings.undoLabel) }
            }
        )
    }
}
