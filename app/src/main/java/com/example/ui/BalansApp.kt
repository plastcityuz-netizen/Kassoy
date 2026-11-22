package com.example.ui

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.auth.attemptAutoSignIn
import com.example.auth.authStateFlow
import com.example.auth.signOutUser
import com.example.core.AppLanguage
import com.example.core.Formatters
import com.example.data.AiCfoRepository
import com.example.data.AppPreferences
import com.example.data.BalansRepository
import com.example.data.ThemePreference
import com.example.data.UiState
import com.example.data.UserPrefs
import com.example.ui.components.BalansLogoMark
import com.example.ui.components.ErrorStateCard
import com.example.ui.components.SkeletonLoadingState
import com.example.ui.screens.AiCfoScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FinanceScreen
import com.example.ui.screens.ModulesHubScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsPricingScreen
import com.example.ui.screens.WelcomeAuthScreen
import com.example.ui.theme.BalansTheme
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.LocalBalansPalette
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun BalansApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val appPreferences = remember(context) { AppPreferences(context.applicationContext) }
    val userPrefs by appPreferences.prefsFlow.collectAsStateWithLifecycle(initialValue = UserPrefs())
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val currentUser by Firebase.auth.authStateFlow()
        .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

    LaunchedEffect(Unit) {
        if (Firebase.auth.currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {},
                onUnauthenticated = {},
                scope = scope
            )
        }
    }

    val onToggleTheme: () -> Unit = {
        scope.launch {
            val next = if (userPrefs.themePreference == ThemePreference.DARK) {
                ThemePreference.LIGHT
            } else {
                ThemePreference.DARK
            }
            appPreferences.setTheme(next)
        }
    }

    val onToggleLanguage: () -> Unit = {
        scope.launch {
            val next = if (userPrefs.language == AppLanguage.UZ) AppLanguage.RU else AppLanguage.UZ
            appPreferences.setLanguage(next)
        }
    }

    BalansTheme(
        themePreference = userPrefs.themePreference,
        language = userPrefs.language
    ) {
        val user = currentUser
        if (user == null) {
            WelcomeAuthScreen(
                themePreference = userPrefs.themePreference,
                onToggleTheme = onToggleTheme,
                onToggleLanguage = onToggleLanguage,
                onAuthSuccess = {}
            )
        } else {
            val uid = user.uid
            key(uid) {
                val vm: MainViewModel = viewModel(
                    key = uid,
                    factory = viewModelFactory {
                        initializer {
                            val app = checkNotNull(this[APPLICATION_KEY]) as Application
                            val dbId = app.getString(R.string.firestore_database_id)
                            val db = FirebaseFirestore.getInstance(dbId)
                            MainViewModel(
                                repository = BalansRepository(db),
                                aiRepository = AiCfoRepository(),
                                currentUserId = uid
                            )
                        }
                    }
                )
                AuthenticatedShell(
                    viewModel = vm,
                    userPrefs = userPrefs,
                    onToggleTheme = onToggleTheme,
                    onToggleLanguage = onToggleLanguage,
                    onToggleDenseMode = { dense ->
                        scope.launch { appPreferences.setDenseTableMode(dense) }
                    },
                    onSignOut = {
                        signOutUser(credentialManager, scope)
                    }
                )
            }
        }
    }
}

@Composable
private fun AuthenticatedShell(
    viewModel: MainViewModel,
    userPrefs: UserPrefs,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onToggleDenseMode: (Boolean) -> Unit,
    onSignOut: () -> Unit
) {
    val palette = LocalBalansPalette.current
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    val cockpitUiState by viewModel.cockpitState.collectAsStateWithLifecycle()
    val toastMsg by viewModel.toastMessage.collectAsStateWithLifecycle()
    val undoPair by viewModel.undoTxState.collectAsStateWithLifecycle()
    val isMutating by viewModel.isMutating.collectAsStateWithLifecycle()

    val aiMessages by viewModel.aiMessages.collectAsStateWithLifecycle()
    val aiChatState by viewModel.aiChatState.collectAsStateWithLifecycle()
    val morningBriefState by viewModel.morningBriefState.collectAsStateWithLifecycle()
    val whatIfState by viewModel.whatIfState.collectAsStateWithLifecycle()
    val receipts by viewModel.paymentReceipts.collectAsStateWithLifecycle()

    var currentSection by rememberSaveable { mutableStateOf(AppSection.DASHBOARD) }
    var selectedSubModule by rememberSaveable { mutableStateOf(SubModuleTab.SALES) }
    var showCommandSearch by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMsg, undoPair) {
        val msg = toastMsg ?: return@LaunchedEffect
        val actionLabel = if (undoPair != null) strings.undoLabel else null
        val result = snackbarHostState.showSnackbar(
            message = msg,
            actionLabel = actionLabel
        )
        if (result == SnackbarResult.ActionPerformed && undoPair != null) {
            viewModel.undoLastTxStatus()
        } else {
            viewModel.clearUndo()
        }
        viewModel.clearToast()
    }

    if (currentSection != AppSection.DASHBOARD) {
        BackHandler { currentSection = AppSection.DASHBOARD }
    }

    when (val ui = cockpitUiState) {
        is UiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(palette.background)
                    .statusBarsPadding()
            ) {
                SkeletonLoadingState(label = strings.loadingLabel)
            }
        }

        is UiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(palette.background)
                    .statusBarsPadding()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                ErrorStateCard(
                    message = ui.message,
                    retryLabel = strings.retryLabel,
                    onRetry = { /* Snapshot listeners auto-recover when network/rules resolve */ }
                )
            }
        }

        is UiState.Success -> {
            val state = ui.data
            if (state.needsOnboarding) {
                OnboardingScreen(
                    isSaving = isMutating,
                    onComplete = { name, stir, city, taxRegime, industry, seedDemo ->
                        viewModel.completeOnboarding(name, stir, city, taxRegime, industry, seedDemo)
                    }
                )
            } else {
                BoxWithConstraints(modifier = Modifier.fillMaxSize().background(palette.background)) {
                    val isMediumOrExpanded = maxWidth >= 640.dp
                    val showSplitAiPane = maxWidth >= 1024.dp

                    Scaffold(
                        containerColor = palette.background,
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        topBar = {
                            Surface(
                                color = palette.surface.copy(alpha = 0.94f),
                                border = BorderStroke(1.dp, palette.border),
                                modifier = Modifier.fillMaxWidth().statusBarsPadding()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        BalansLogoMark(size = 32.dp)
                                        Column {
                                            Text(
                                                text = state.activeCompany?.name ?: strings.appName,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = palette.textPrimary,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "${state.activeRole.label(lang)} · ${state.currentPlan.name}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = palette.emeraldText
                                            )
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { showCommandSearch = true },
                                            modifier = Modifier.testTag("global_search_btn")
                                        ) {
                                            Icon(Icons.Outlined.Search, contentDescription = strings.searchPlaceholder, tint = palette.textPrimary)
                                        }
                                        IconButton(
                                            onClick = onToggleLanguage,
                                            modifier = Modifier.testTag("app_lang_btn")
                                        ) {
                                            Icon(Icons.Outlined.Translate, contentDescription = "Til", tint = palette.emeraldText)
                                        }
                                        IconButton(
                                            onClick = onToggleTheme,
                                            modifier = Modifier.testTag("app_theme_btn")
                                        ) {
                                            Icon(
                                                imageVector = if (userPrefs.themePreference == ThemePreference.DARK) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                                                contentDescription = "Tema",
                                                tint = palette.textPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        bottomBar = {
                            if (!isMediumOrExpanded) {
                                NavigationBar(
                                    containerColor = palette.surface,
                                    modifier = Modifier.navigationBarsPadding()
                                ) {
                                    val navItems = listOf(
                                        Triple(AppSection.DASHBOARD, Icons.Outlined.Dashboard, strings.navDashboard),
                                        Triple(AppSection.MODULES, Icons.Outlined.Apps, strings.navModules),
                                        Triple(AppSection.AI_CFO, Icons.Outlined.AutoAwesome, strings.navAiCfo),
                                        Triple(AppSection.FINANCE, Icons.Outlined.AccountBalanceWallet, strings.navFinance),
                                        Triple(AppSection.SETTINGS, Icons.Outlined.Settings, strings.navSettings)
                                    )
                                    navItems.forEach { (sec, icon, label) ->
                                        NavigationBarItem(
                                            selected = currentSection == sec,
                                            onClick = { currentSection = sec },
                                            icon = { Icon(icon, contentDescription = label) },
                                            label = { Text(label, maxLines = 1) },
                                            modifier = Modifier.testTag("nav_${sec.name.lowercase()}")
                                        )
                                    }
                                }
                            }
                        },
                        floatingActionButton = {
                            if (!showSplitAiPane && currentSection != AppSection.AI_CFO) {
                                FloatingActionButton(
                                    onClick = { currentSection = AppSection.AI_CFO },
                                    containerColor = palette.emerald,
                                    contentColor = palette.onEmerald,
                                    modifier = Modifier.testTag("floating_ai_cfo_fab")
                                ) {
                                    Icon(Icons.Outlined.AutoAwesome, contentDescription = "AI CFO")
                                }
                            }
                        }
                    ) { innerPadding ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (isMediumOrExpanded) {
                                NavigationRail(containerColor = palette.surface) {
                                    val railItems = listOf(
                                        Triple(AppSection.DASHBOARD, Icons.Outlined.Dashboard, strings.navDashboard),
                                        Triple(AppSection.MODULES, Icons.Outlined.Apps, strings.navModules),
                                        Triple(AppSection.AI_CFO, Icons.Outlined.AutoAwesome, strings.navAiCfo),
                                        Triple(AppSection.FINANCE, Icons.Outlined.AccountBalanceWallet, strings.navFinance),
                                        Triple(AppSection.SETTINGS, Icons.Outlined.Settings, strings.navSettings)
                                    )
                                    railItems.forEach { (sec, icon, label) ->
                                        NavigationRailItem(
                                            selected = currentSection == sec,
                                            onClick = { currentSection = sec },
                                            icon = { Icon(icon, contentDescription = label) },
                                            label = { Text(label) }
                                        )
                                    }
                                }
                                VerticalDivider(color = palette.border)
                            }

                            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                when (currentSection) {
                                    AppSection.DASHBOARD -> DashboardScreen(
                                        state = state,
                                        isExpandedWidth = isMediumOrExpanded,
                                        onOpenAiCfo = { currentSection = AppSection.AI_CFO },
                                        onOpenPricing = { currentSection = AppSection.SETTINGS },
                                        onConfirmDebtReminder = { debt ->
                                            viewModel.confirmSendDebtReminder(debt, state.activeRole)
                                        }
                                    )

                                    AppSection.MODULES -> ModulesHubScreen(
                                        state = state,
                                        selectedTab = selectedSubModule,
                                        denseMode = userPrefs.denseTableMode,
                                        onSelectTab = { selectedSubModule = it },
                                        onAddTransaction = { type, cat, cp, amt, status ->
                                            viewModel.addTransaction(
                                                companyId = state.activeCompany?.id ?: "",
                                                type = type,
                                                category = cat,
                                                counterparty = cp,
                                                amountUzs = amt,
                                                status = status,
                                                dateDisplay = Formatters.formatDateDdMmYyyy(System.currentTimeMillis()),
                                                note = null,
                                                actorRole = state.activeRole
                                            )
                                        },
                                        onToggleTxPaid = { tx -> viewModel.toggleTransactionPaid(tx) },
                                        onAddInventory = { sku, name, cat, qty, unit, cost, min ->
                                            viewModel.addInventoryItem(
                                                companyId = state.activeCompany?.id ?: "",
                                                sku = sku,
                                                name = name,
                                                category = cat,
                                                quantity = qty,
                                                unit = unit,
                                                unitCostUzs = cost,
                                                minThreshold = min,
                                                actorRole = state.activeRole
                                            )
                                        },
                                        onAdjustInventory = { item, delta -> viewModel.adjustInventory(item, delta) },
                                        onAddProduction = { prodName, bom, target, cost ->
                                            viewModel.addProductionOrder(
                                                companyId = state.activeCompany?.id ?: "",
                                                productName = prodName,
                                                bomSummary = bom,
                                                targetQty = target,
                                                unitCostUzs = cost,
                                                actorRole = state.activeRole
                                            )
                                        },
                                        onAdvanceProduction = { order -> viewModel.advanceProduction(order) },
                                        onAddEmployee = { fullName, roleKey, dept, phone, sal ->
                                            viewModel.addEmployee(
                                                companyId = state.activeCompany?.id ?: "",
                                                fullName = fullName,
                                                roleKey = roleKey,
                                                department = dept,
                                                phone = phone,
                                                salaryUzs = sal,
                                                actorRole = state.activeRole
                                            )
                                        },
                                        onShowToast = { msg -> viewModel.showToast(msg) },
                                        onOpenPricing = { currentSection = AppSection.SETTINGS }
                                    )

                                    AppSection.AI_CFO -> AiCfoScreen(
                                        state = state,
                                        messages = aiMessages,
                                        chatState = aiChatState,
                                        morningBriefState = morningBriefState,
                                        whatIfState = whatIfState,
                                        onAskQuestion = { q -> viewModel.askAiCfo(q, lang) },
                                        onLoadMorningBrief = { viewModel.loadMorningBrief(lang) },
                                        onRunWhatIf = { sc -> viewModel.runWhatIf(sc, lang) }
                                    )

                                    AppSection.FINANCE -> FinanceScreen(
                                        state = state,
                                        onConfirmDebtReminder = { debt ->
                                            viewModel.confirmSendDebtReminder(debt, state.activeRole)
                                        }
                                    )

                                    AppSection.SETTINGS -> SettingsPricingScreen(
                                        state = state,
                                        themePreference = userPrefs.themePreference,
                                        denseTableMode = userPrefs.denseTableMode,
                                        receipts = receipts,
                                        onToggleTheme = onToggleTheme,
                                        onToggleLanguage = onToggleLanguage,
                                        onToggleDenseMode = onToggleDenseMode,
                                        onSwitchCompany = { cid -> viewModel.switchCompany(cid) },
                                        onSwitchRole = { role -> viewModel.switchRole(role) },
                                        onUpdateTaxRates = { qqs, inps, inc ->
                                            viewModel.updateTaxRates(
                                                companyId = state.activeCompany?.id ?: "",
                                                qqsRate = qqs,
                                                inpsRate = inps,
                                                incomeTaxRate = inc,
                                                actorRole = state.activeRole
                                            )
                                        },
                                        onSubscribePlan = { plan, cycle, method, promo ->
                                            viewModel.subscribePlan(plan, cycle, method, promo)
                                        },
                                        onSignOut = onSignOut
                                    )
                                }
                            }

                            // On large tablets / landscape >= 1024dp, keep AI CFO panel docked on the right
                            if (showSplitAiPane && currentSection != AppSection.AI_CFO) {
                                VerticalDivider(color = palette.border)
                                Box(modifier = Modifier.width(360.dp).fillMaxHeight()) {
                                    AiCfoScreen(
                                        state = state,
                                        messages = aiMessages,
                                        chatState = aiChatState,
                                        morningBriefState = morningBriefState,
                                        whatIfState = whatIfState,
                                        onAskQuestion = { q -> viewModel.askAiCfo(q, lang) },
                                        onLoadMorningBrief = { viewModel.loadMorningBrief(lang) },
                                        onRunWhatIf = { sc -> viewModel.runWhatIf(sc, lang) }
                                    )
                                }
                            }
                        }
                    }
                }

                if (showCommandSearch) {
                    CommandSearchDialog(
                        state = state,
                        onDismiss = { showCommandSearch = false },
                        onNavigate = { sec, sub ->
                            currentSection = sec
                            if (sub != null) selectedSubModule = sub
                            showCommandSearch = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CommandSearchDialog(
    state: BalansCockpitState,
    onDismiss: () -> Unit,
    onNavigate: (AppSection, SubModuleTab?) -> Unit
) {
    val palette = LocalBalansPalette.current
    val strings = LocalAppStrings.current
    var query by remember { mutableStateOf("") }

    val quickActions = remember {
        listOf(
            Triple("Kokpit / Balans Score", AppSection.DASHBOARD, null),
            Triple("AI CFO Copilot", AppSection.AI_CFO, null),
            Triple("Savdo (Sales)", AppSection.MODULES, SubModuleTab.SALES),
            Triple("Ombor (Inventory)", AppSection.MODULES, SubModuleTab.INVENTORY),
            Triple("Xarid (Purchases)", AppSection.MODULES, SubModuleTab.PURCHASES),
            Triple("Ishlab chiqarish (BOM)", AppSection.MODULES, SubModuleTab.PRODUCTION),
            Triple("HR & Ish haqi", AppSection.MODULES, SubModuleTab.HR),
            Triple("Buxgalteriya & Soliq", AppSection.MODULES, SubModuleTab.ACCOUNTING),
            Triple("Hisobotlar (PDF / Excel)", AppSection.MODULES, SubModuleTab.REPORTS),
            Triple("Moliya & Qarzdorlik", AppSection.FINANCE, null),
            Triple("Tariflar & RBAC & Sozlamalar", AppSection.SETTINGS, null)
        )
    }

    val matchedActions = quickActions.filter { query.isBlank() || it.first.contains(query, ignoreCase = true) }
    val matchedTx = state.transactions.filter {
        query.isNotBlank() && (it.counterparty.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true))
    }.take(5)
    val matchedInv = state.inventory.filter {
        query.isNotBlank() && (it.name.contains(query, ignoreCase = true) || it.sku.contains(query, ignoreCase = true))
    }.take(5)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cmd/Ctrl+K · Tezkor qidiruv") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(strings.searchPlaceholder) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("command_search_input")
                )
                LazyColumn(modifier = Modifier.height(260.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(matchedActions) { (label, sec, sub) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = palette.elevated,
                            modifier = Modifier.fillMaxWidth().clickable { onNavigate(sec, sub) }
                        ) {
                            Text(
                                text = "→ $label",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textPrimary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    items(matchedTx) { tx ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = palette.surface,
                            modifier = Modifier.fillMaxWidth().clickable { onNavigate(AppSection.MODULES, SubModuleTab.SALES) }
                        ) {
                            Text(
                                text = "${tx.counterparty} · ${Formatters.formatUzs(tx.amountUzs, compact = true)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.emeraldText,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    items(matchedInv) { inv ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = palette.surface,
                            modifier = Modifier.fillMaxWidth().clickable { onNavigate(AppSection.MODULES, SubModuleTab.INVENTORY) }
                        ) {
                            Text(
                                text = "${inv.sku} · ${inv.name} (${inv.quantity} ${inv.unit})",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.cyan,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Yopish") }
        }
    )
}
