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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Factory
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.Formatters
import com.example.core.PricingConfig
import com.example.core.RbacPolicy
import com.example.data.BusinessTransaction
import com.example.data.InventoryItem
import com.example.data.ProductionOrder
import com.example.data.TransactionType
import com.example.data.TxStatus
import com.example.ui.BalansCockpitState
import com.example.ui.SubModuleTab
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SolidCard
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.LocalBalansPalette
import com.example.ui.theme.TabularNumberStyle

private data class ModuleTabMeta(
    val tab: SubModuleTab,
    val moduleKey: String,
    val icon: ImageVector,
    val labelUz: String,
    val labelRu: String
)

@Composable
fun ModulesHubScreen(
    state: BalansCockpitState,
    selectedTab: SubModuleTab,
    denseMode: Boolean,
    onSelectTab: (SubModuleTab) -> Unit,
    onAddTransaction: (TransactionType, String, String, Long, TxStatus) -> Unit,
    onToggleTxPaid: (BusinessTransaction) -> Unit,
    onAddInventory: (String, String, String, Long, String, Long, Long) -> Unit,
    onAdjustInventory: (InventoryItem, Long) -> Unit,
    onAddProduction: (String, String, Long, Long) -> Unit,
    onAdvanceProduction: (ProductionOrder) -> Unit,
    onAddEmployee: (String, String, String, String, Long) -> Unit,
    onShowToast: (String) -> Unit,
    onOpenPricing: () -> Unit
) {
    val palette = LocalBalansPalette.current
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    val cardPadding = if (denseMode) 10.dp else 16.dp

    val tabs = remember {
        listOf(
            ModuleTabMeta(SubModuleTab.SALES, "sales", Icons.Outlined.ShoppingBag, "Savdo", "Продажи"),
            ModuleTabMeta(SubModuleTab.INVENTORY, "inventory", Icons.Outlined.Inventory2, "Ombor", "Склад"),
            ModuleTabMeta(SubModuleTab.PURCHASES, "purchases", Icons.Outlined.ShoppingCart, "Xarid", "Закупки"),
            ModuleTabMeta(SubModuleTab.PRODUCTION, "production", Icons.Outlined.Factory, "Ishlab chiqarish", "Производство"),
            ModuleTabMeta(SubModuleTab.HR, "hr", Icons.Outlined.Group, "HR & Ish haqi", "HR и зарплата"),
            ModuleTabMeta(SubModuleTab.ACCOUNTING, "accounting", Icons.Outlined.ReceiptLong, "Buxgalteriya", "Бухгалтерия"),
            ModuleTabMeta(SubModuleTab.REPORTS, "reports", Icons.Outlined.Summarize, "Hisobotlar", "Отчёты")
        )
    }

    var showCreateTxDialog by remember { mutableStateOf<TransactionType?>(null) }
    var showCreateInvDialog by remember { mutableStateOf(false) }
    var showCreateProdDialog by remember { mutableStateOf(false) }
    var showCreateEmpDialog by remember { mutableStateOf(false) }
    var txStatusFilter by remember { mutableStateOf<TxStatus?>(null) }

    val activeMeta = tabs.first { it.tab == selectedTab }
    val hasRoleAccess = RbacPolicy.canAccessModule(state.activeRole, activeMeta.moduleKey)
    val hasPlanAccess = PricingConfig.isModuleUnlocked(state.currentPlan, state.currentStatus, activeMeta.moduleKey)

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal Module Selector
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tabs) { meta ->
                val active = meta.tab == selectedTab
                val lockedByPlan = !PricingConfig.isModuleUnlocked(state.currentPlan, state.currentStatus, meta.moduleKey)
                val lockedByRole = !RbacPolicy.canAccessModule(state.activeRole, meta.moduleKey)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (active) palette.emerald.copy(alpha = 0.16f) else palette.surface,
                    border = BorderStroke(1.dp, if (active) palette.emerald else palette.border),
                    modifier = Modifier
                        .clickable { onSelectTab(meta.tab) }
                        .testTag("module_tab_${meta.moduleKey}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (lockedByPlan || lockedByRole) Icons.Outlined.Lock else meta.icon,
                            contentDescription = null,
                            tint = if (active) palette.emeraldText else palette.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (lang == AppLanguage.RU) meta.labelRu else meta.labelUz,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (active) palette.emeraldText else palette.textPrimary
                        )
                    }
                }
            }
        }

        if (!hasRoleAccess) {
            Column(modifier = Modifier.padding(16.dp)) {
                EmptyStateCard(
                    title = if (lang == AppLanguage.RU) "Доступ ограничен ролью" else "Rol cheklovi",
                    subtitle = "${state.activeRole.label(lang)} roli bu bo‘limni ko‘ra olmaydi.",
                    actionLabel = null,
                    onAction = null
                )
            }
        } else if (!hasPlanAccess) {
            Column(modifier = Modifier.padding(16.dp)) {
                EmptyStateCard(
                    title = if (lang == AppLanguage.RU) "Модуль недоступен на текущем тарифе" else "Tarif cheklovi",
                    subtitle = if (lang == AppLanguage.RU) "Перейдите на тариф Business или Premium для доступа." else "Bu modulni ochish uchun Business yoki Premium tarifini tanlang.",
                    actionLabel = strings.selectPlan,
                    onAction = onOpenPricing
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    SubModuleTab.SALES, SubModuleTab.PURCHASES -> {
                        val targetType = if (selectedTab == SubModuleTab.SALES) TransactionType.SALE else TransactionType.PURCHASE
                        val filtered = state.transactions
                            .filter { it.type == targetType.key }
                            .filter { txStatusFilter == null || it.status == txStatusFilter!!.key }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(null, TxStatus.PAID, TxStatus.PENDING, TxStatus.OVERDUE).forEach { st ->
                                        val sel = txStatusFilter == st
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (sel) palette.emerald.copy(alpha = 0.16f) else palette.elevated,
                                            modifier = Modifier.clickable { txStatusFilter = st }
                                        ) {
                                            Text(
                                                text = st?.let { if (lang == AppLanguage.RU) it.labelRu else it.labelUz } ?: "Barchasi",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (sel) palette.emeraldText else palette.textMuted,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                                Button(
                                    onClick = { showCreateTxDialog = targetType },
                                    colors = ButtonDefaults.buttonColors(containerColor = palette.emerald, contentColor = palette.onEmerald),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("add_tx_btn")
                                ) {
                                    Icon(Icons.Outlined.Add, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(strings.addRecord)
                                }
                            }
                        }

                        if (filtered.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    title = strings.emptyTitle,
                                    subtitle = strings.emptySubtitle,
                                    actionLabel = strings.addRecord,
                                    onAction = { showCreateTxDialog = targetType }
                                )
                            }
                        } else {
                            items(filtered, key = { it.id }) { tx ->
                                SolidCard(contentPadding = cardPadding) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(tx.counterparty, style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                                            Text("${tx.category} · ${tx.dateDisplay}", style = MaterialTheme.typography.bodySmall, color = palette.textMuted)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = Formatters.formatUzs(tx.amountUzs, compact = true),
                                                style = MaterialTheme.typography.titleMedium.merge(TabularNumberStyle),
                                                color = if (targetType == TransactionType.SALE) palette.emeraldText else palette.textPrimary
                                            )
                                            val st = TxStatus.fromKey(tx.status)
                                            val stColor = when (st) {
                                                TxStatus.PAID -> palette.success
                                                TxStatus.PENDING -> palette.warning
                                                TxStatus.OVERDUE -> palette.danger
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = stColor.copy(alpha = 0.14f),
                                                modifier = Modifier.clickable { onToggleTxPaid(tx) }
                                            ) {
                                                Text(
                                                    text = if (lang == AppLanguage.RU) st.labelRu else st.labelUz,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = stColor,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    SubModuleTab.INVENTORY -> {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${state.inventory.size} SKU · ${Formatters.formatUzs(state.metrics.inventoryValueUzs, compact = true)}",
                                    style = MaterialTheme.typography.titleMedium.merge(TabularNumberStyle),
                                    color = palette.textPrimary
                                )
                                Button(
                                    onClick = { showCreateInvDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = palette.emerald, contentColor = palette.onEmerald),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("add_inventory_btn")
                                ) {
                                    Icon(Icons.Outlined.Add, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(strings.addRecord)
                                }
                            }
                        }

                        if (state.inventory.isEmpty()) {
                            item {
                                EmptyStateCard(strings.emptyTitle, strings.emptySubtitle, strings.addRecord, { showCreateInvDialog = true })
                            }
                        } else {
                            items(state.inventory, key = { it.id }) { item ->
                                SolidCard(contentPadding = cardPadding) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.name, style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                                            Text("${item.sku} · ${Formatters.formatUzs(item.unitCostUzs)}/${item.unit}", style = MaterialTheme.typography.bodySmall, color = palette.textMuted)
                                            if (item.lowStock) {
                                                Spacer(Modifier.height(4.dp))
                                                Surface(shape = RoundedCornerShape(6.dp), color = palette.danger.copy(alpha = 0.14f)) {
                                                    Text(
                                                        text = if (lang == AppLanguage.RU) "Мало на складе" else "Qoldiq kam",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = palette.danger,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { onAdjustInventory(item, -10L) }) {
                                                Text("-", style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                                            }
                                            Text(
                                                text = "${item.quantity} ${item.unit}",
                                                style = MaterialTheme.typography.titleMedium.merge(TabularNumberStyle),
                                                color = palette.emeraldText
                                            )
                                            IconButton(onClick = { onAdjustInventory(item, 10L) }) {
                                                Text("+", style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    SubModuleTab.PRODUCTION -> {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.RU) "Заказы производства (BOM)" else "Ishlab chiqarish va BOM",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = palette.textPrimary
                                )
                                Button(
                                    onClick = { showCreateProdDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = palette.emerald, contentColor = palette.onEmerald),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Outlined.Add, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(strings.addRecord)
                                }
                            }
                        }
                        if (state.production.isEmpty()) {
                            item {
                                EmptyStateCard(strings.emptyTitle, strings.emptySubtitle, strings.addRecord, { showCreateProdDialog = true })
                            }
                        } else {
                            items(state.production, key = { it.id }) { order ->
                                SolidCard(contentPadding = cardPadding) {
                                    Text(order.productName, style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                                    Text("BOM: ${order.bomSummary}", style = MaterialTheme.typography.bodySmall, color = palette.textMuted)
                                    Spacer(Modifier.height(6.dp))
                                    val progress = (order.completedQty.toFloat() / order.targetQty.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier.fillMaxWidth().height(6.dp),
                                        color = palette.emerald,
                                        trackColor = palette.border
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${order.completedQty}/${order.targetQty} · Tannarx: ${Formatters.formatUzs(order.unitCostUzs)} · Brak: ${Formatters.formatPercent(order.defectPercent)}",
                                            style = MaterialTheme.typography.labelSmall.merge(TabularNumberStyle),
                                            color = palette.textMuted,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (order.status != "COMPLETED") {
                                            TextButton(onClick = { onAdvanceProduction(order) }) {
                                                Text("+25%", color = palette.emeraldText)
                                            }
                                        } else {
                                            Text("✓", color = palette.success, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    SubModuleTab.HR -> {
                        val canSeePayroll = RbacPolicy.canViewPayroll(state.activeRole)
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${state.employees.size} xodim · ${if (canSeePayroll) Formatters.formatUzs(state.metrics.totalPayrollUzs, compact = true) else strings.hiddenByRole}",
                                    style = MaterialTheme.typography.titleMedium.merge(TabularNumberStyle),
                                    color = palette.textPrimary
                                )
                                Button(
                                    onClick = { showCreateEmpDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = palette.emerald, contentColor = palette.onEmerald),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Outlined.Add, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(strings.addRecord)
                                }
                            }
                        }
                        if (state.employees.isEmpty()) {
                            item {
                                EmptyStateCard(strings.emptyTitle, strings.emptySubtitle, strings.addRecord, { showCreateEmpDialog = true })
                            }
                        } else {
                            items(state.employees, key = { it.id }) { emp ->
                                SolidCard(contentPadding = cardPadding) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(emp.fullName, style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                                            Text("${emp.department} · ${emp.phone}", style = MaterialTheme.typography.bodySmall, color = palette.textMuted)
                                            Text("Davomat: ${emp.attendancePercent}%", style = MaterialTheme.typography.labelSmall, color = palette.emeraldText)
                                        }
                                        Text(
                                            text = if (canSeePayroll) Formatters.formatUzs(emp.salaryUzs, compact = true) else "•••",
                                            style = MaterialTheme.typography.titleMedium.merge(TabularNumberStyle),
                                            color = palette.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    SubModuleTab.ACCOUNTING -> {
                        item {
                            SolidCard(contentPadding = cardPadding) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (lang == AppLanguage.RU) "Баланс и P&L" else "Balans va Foyda-Zarar (P&L)",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = palette.textPrimary
                                    )
                                    Surface(shape = RoundedCornerShape(6.dp), color = palette.warning.copy(alpha = 0.14f)) {
                                        Text(
                                            text = strings.accountantReviewBadge,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = palette.warning,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                                AccountingLine("1000 · Kassa va Bank (5010/5110)", state.metrics.cashAndBankUzs)
                                AccountingLine("1100 · Ombor aktivlari (1010/2910)", state.metrics.inventoryValueUzs)
                                AccountingLine("4010 · Xaridorlardan qarzdorlik", state.metrics.receivableUzs)
                                AccountingLine("6010 · Yetkazib beruvchilarga qarz", -state.metrics.payableUzs)
                                AccountingLine("6410 · Hisoblangan soliq zaxirasi", -state.metrics.estimatedTaxUzs)
                            }
                        }
                        item {
                            SolidCard(contentPadding = cardPadding) {
                                Text(
                                    text = if (lang == AppLanguage.RU) "Налоговый календарь (Узбекистан)" else "Soliq kalendari (O‘zbekiston)",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = palette.textPrimary
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("• 05.12.2026 — Daromad solig‘i & INPS (${state.activeCompany?.inpsRate ?: 1.0}%)", style = MaterialTheme.typography.bodySmall, color = palette.textPrimary)
                                Text("• 15.12.2026 — Aylanma / QQS hisoboti (${state.activeCompany?.qqsRate ?: 12.0}%)", style = MaterialTheme.typography.bodySmall, color = palette.textPrimary)
                                Text("• 20.12.2026 — Budjetga to‘lov muddati", style = MaterialTheme.typography.bodySmall, color = palette.warning)
                            }
                        }
                    }

                    SubModuleTab.REPORTS -> {
                        item {
                            SolidCard(contentPadding = cardPadding) {
                                Text(
                                    text = if (lang == AppLanguage.RU) "Экспорт отчётов" else "Hisobotlarni eksport qilish",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = palette.textPrimary
                                )
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = { onShowToast("PDF hisobot tayyorlandi (${state.activeCompany?.name ?: "BALANS"})") },
                                        colors = ButtonDefaults.buttonColors(containerColor = palette.emerald, contentColor = palette.onEmerald),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Outlined.Download, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("PDF")
                                    }
                                    OutlinedButton(
                                        onClick = { onShowToast("Excel (.xlsx) hisobot tayyorlandi") },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Outlined.Download, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Excel")
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (lang == AppLanguage.RU) "Автоотправка в Telegram" else "Telegram orqali jadval yuborish",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = palette.textMuted
                                    )
                                    Surface(shape = RoundedCornerShape(6.dp), color = palette.info.copy(alpha = 0.14f)) {
                                        Text(
                                            text = strings.comingSoonBadge,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = palette.info,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Transaction Dialog
    showCreateTxDialog?.let { txType ->
        var counterparty by remember { mutableStateOf("") }
        var category by remember { mutableStateOf(if (txType == TransactionType.SALE) "Ulgurji savdo" else "Xomashyo xaridi") }
        var amountStr by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateTxDialog = null },
            title = { Text(if (txType == TransactionType.SALE) "Yangi savdo" else "Yangi xarid") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = counterparty,
                        onValueChange = { counterparty = it },
                        label = { Text("Kontragent / Mijoz") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Toifa") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Summa (so‘m)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                val amt = amountStr.toLongOrNull() ?: 0L
                Button(
                    onClick = {
                        onAddTransaction(txType, category, counterparty, amt, TxStatus.PENDING)
                        showCreateTxDialog = null
                    },
                    enabled = counterparty.trim().isNotEmpty() && amt > 0L
                ) {
                    Text(strings.addRecord)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTxDialog = null }) { Text(strings.undoLabel) }
            }
        )
    }

    // Create Inventory Dialog
    if (showCreateInvDialog) {
        var sku by remember { mutableStateOf("SKU-101") }
        var name by remember { mutableStateOf("") }
        var qtyStr by remember { mutableStateOf("100") }
        var costStr by remember { mutableStateOf("25000") }
        AlertDialog(
            onDismissRequest = { showCreateInvDialog = false },
            title = { Text("Yangi ombor mahsuloti") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("SKU") }, singleLine = true)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Mahsulot nomi") }, singleLine = true)
                    OutlinedTextField(
                        value = qtyStr,
                        onValueChange = { qtyStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Miqdor") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = costStr,
                        onValueChange = { costStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Birlik narxi (so‘m)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                val q = qtyStr.toLongOrNull() ?: 0L
                val c = costStr.toLongOrNull() ?: 0L
                Button(
                    onClick = {
                        onAddInventory(sku, name, "Umumiy", q, "dona", c, 20L)
                        showCreateInvDialog = false
                    },
                    enabled = sku.trim().length >= 2 && name.trim().length >= 2 && q >= 0L
                ) {
                    Text(strings.addRecord)
                }
            },
            dismissButton = { TextButton(onClick = { showCreateInvDialog = false }) { Text(strings.undoLabel) } }
        )
    }

    // Create Production Dialog
    if (showCreateProdDialog) {
        var productName by remember { mutableStateOf("") }
        var bom by remember { mutableStateOf("Paxta ipi + Bo‘yoq") }
        var qtyStr by remember { mutableStateOf("500") }
        var costStr by remember { mutableStateOf("35000") }
        AlertDialog(
            onDismissRequest = { showCreateProdDialog = false },
            title = { Text("Yangi ishlab chiqarish partiyasi") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = productName, onValueChange = { productName = it }, label = { Text("Mahsulot") }, singleLine = true)
                    OutlinedTextField(value = bom, onValueChange = { bom = it }, label = { Text("Retsept (BOM)") }, singleLine = true)
                    OutlinedTextField(
                        value = qtyStr,
                        onValueChange = { qtyStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Reja miqdori") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = costStr,
                        onValueChange = { costStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Tannarx / birlik (so‘m)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                val q = qtyStr.toLongOrNull() ?: 0L
                val c = costStr.toLongOrNull() ?: 0L
                Button(
                    onClick = {
                        onAddProduction(productName, bom, q, c)
                        showCreateProdDialog = false
                    },
                    enabled = productName.trim().length >= 2 && q >= 1L
                ) { Text(strings.addRecord) }
            },
            dismissButton = { TextButton(onClick = { showCreateProdDialog = false }) { Text(strings.undoLabel) } }
        )
    }

    // Create Employee Dialog
    if (showCreateEmpDialog) {
        var fullName by remember { mutableStateOf("") }
        var dept by remember { mutableStateOf("Sotuv bo‘limi") }
        var phone by remember { mutableStateOf("+998 90 123 45 67") }
        var salaryStr by remember { mutableStateOf("8000000") }
        val phoneValid = Formatters.isValidUzPhone(phone)
        AlertDialog(
            onDismissRequest = { showCreateEmpDialog = false },
            title = { Text("Yangi xodim qo‘shish") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("F.I.Sh.") }, singleLine = true)
                    OutlinedTextField(value = dept, onValueChange = { dept = it }, label = { Text("Bo‘lim") }, singleLine = true)
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Telefon (+998 XX XXX XX XX)") },
                        isError = !phoneValid,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = salaryStr,
                        onValueChange = { salaryStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Oylik maosh (so‘m)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                val sal = salaryStr.toLongOrNull() ?: 0L
                Button(
                    onClick = {
                        onAddEmployee(fullName, "SALES_MANAGER", dept, phone, sal)
                        showCreateEmpDialog = false
                    },
                    enabled = fullName.trim().length >= 2 && phoneValid && sal > 0L
                ) { Text(strings.addRecord) }
            },
            dismissButton = { TextButton(onClick = { showCreateEmpDialog = false }) { Text(strings.undoLabel) } }
        )
    }
}

@Composable
private fun AccountingLine(label: String, amountUzs: Long) {
    val palette = LocalBalansPalette.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = palette.textPrimary)
        Text(
            text = Formatters.formatUzs(amountUzs, compact = true, includeSign = true),
            style = MaterialTheme.typography.titleMedium.merge(TabularNumberStyle),
            color = if (amountUzs >= 0L) palette.emeraldText else palette.danger
        )
    }
}
