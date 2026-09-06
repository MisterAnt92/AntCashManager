package com.antcashmanager.android.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.layout.HorizontalSpacer
import com.antcashmanager.android.ui.components.layout.LocalDisplayFeatures
import com.antcashmanager.android.ui.components.layout.FoldableAwareLayout
import com.antcashmanager.android.ui.base.LocalMultiPaneCoordinator
import androidx.window.layout.FoldingFeature
import kotlinx.coroutines.flow.first
import co.touchlab.kermit.Logger
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.animation.AntEasterEggAnimation
import com.antcashmanager.android.ui.components.dialog.HelpButton
import com.antcashmanager.android.ui.components.filter.DateRangeFilter
import com.antcashmanager.android.ui.components.filter.SearchComponent
import com.antcashmanager.android.ui.components.layout.rememberAdaptiveLayoutInfo
import com.antcashmanager.android.ui.components.overlay.TutorialOverlay
import com.antcashmanager.android.ui.components.state.AntEmptyState
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.home.event.HomeEvent
import com.antcashmanager.android.ui.screen.home.model.HomeTopCardType
import com.antcashmanager.android.ui.screen.home.transactionDetail.TransactionDetailsDialog
import com.antcashmanager.android.navigation.LocalScreenHeaderConfigCallback
import com.antcashmanager.android.navigation.ScreenHeaderConfig
import com.antcashmanager.android.ui.screen.home.view.BalanceCard
import com.antcashmanager.android.ui.screen.home.view.HelpDialog
import com.antcashmanager.android.ui.screen.home.view.HomeTopCardsOrderDialog
import com.antcashmanager.android.ui.screen.home.view.IncomeExpenseRow
import com.antcashmanager.android.ui.screen.home.view.LoadingState
import com.antcashmanager.android.ui.screen.home.view.QuickInsightsCard
import com.antcashmanager.android.ui.screen.home.view.RecentTransactionItem
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.model.TransactionType
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

// ══════════════════════════════════════════════════════════════════════════════
// SCREEN
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun HomeScreen(
    navController: androidx.navigation.NavController,
    modifier: Modifier = Modifier,
) {
    Logger.d(tag = "HomeScreen") { "Displaying HomeScreen" }

    val viewModel: HomeViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeContent(
        state = state,
        onEvent = { event ->
            viewModel.onEvent(event)
        },
        navController = navController,
        modifier = modifier,
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// CONTENT
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeContent(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
    navController: androidx.navigation.NavController,
    modifier: Modifier = Modifier,
) {

    val analyticsManager: com.antcashmanager.android.analytics.AnalyticsManager = koinInject()

    // Date picker state
    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showTopCardsOrderDialog by remember { mutableStateOf(false) }
    var showVersionDialog by remember { mutableStateOf(false) }
    var topCardsOrderRaw by rememberSaveable { mutableStateOf(HomeConstant.DEFAULT_TOP_CARDS_ORDER) }
    var editingTopCardsOrder by remember { mutableStateOf(HomeTopCardType.parse(topCardsOrderRaw)) }
    val topCardsOrder = remember(topCardsOrderRaw) { HomeTopCardType.parse(topCardsOrderRaw) }

    // Load persisted card order on composition from state (populated by ViewModel)
    LaunchedEffect(state.homeTopCardsOrder) {
        if (state.homeTopCardsOrder.isNotEmpty()) {
            topCardsOrderRaw = state.homeTopCardsOrder.joinToString(",")
        }
    }

    // Settings from state (populated by HomeViewModel)
    val dateFilterExpanded = state.dateFilterExpanded
    val showPaymentTypeBreakdown = state.showPaymentTypeBreakdown
    val showQuickInsightsCard = state.showQuickInsightsCard
    val reduceMotion = state.reduceMotion
    val transactionDisplayType = try {
        TransactionDisplayType.valueOf(state.transactionDisplayType)
    } catch (e: Exception) {
        TransactionDisplayType.TREND
    }
    val isTutorialCompleted = state.isTutorialCompleted
    val isLoading = state.isLoading

    val coroutineScope = rememberCoroutineScope()

    // Foldable device support
    val displayFeatures = LocalDisplayFeatures.current
    val adaptiveLayoutInfo = rememberAdaptiveLayoutInfo(displayFeatures = displayFeatures)
    val multiPaneCoordinator = LocalMultiPaneCoordinator.current
    val foldingFeature = adaptiveLayoutInfo.foldingFeature

    // Preserva scroll position durante navigazione back/forward
    val listState = rememberSaveable(saver = androidx.compose.foundation.lazy.LazyListState.Saver) {
        androidx.compose.foundation.lazy.LazyListState()
    }
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 2 }
    }
    val biggestExpense = remember(state.filteredTransactions) {
        state.filteredTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .maxByOrNull { kotlin.math.abs(it.amount) }
    }

    // Calcoli per Quick Insights Card
    val netBalance = remember(state.totalIncome, state.totalExpense) {
        state.totalIncome - state.totalExpense
    }
    val dailyAverageExpense = remember(state.totalExpense, state.dateRangeFrom, state.dateRangeTo) {
        val daysInPeriod =
            (state.dateRangeTo - state.dateRangeFrom) / 86400000.0 // Convert ms to days
        if (daysInPeriod > 0) {
            kotlin.math.abs(state.totalExpense) / daysInPeriod
        } else {
            0.0
        }
    }

    // Elenco delle top card effettivamente visibili: esclude Quick Insights quando
    // l'impostazione corrispondente è disattivata. Riusato sia per il rendering
    // (visibleTopCardsOrder) sia come base per il dialog di riordino
    // (editableTopCardsOrder) — prima erano due `remember` con logica identica.
    val visibleTopCardsOrder = remember(topCardsOrder, showQuickInsightsCard) {
        if (showQuickInsightsCard) {
            topCardsOrder
        } else {
            topCardsOrder.filterNot { it == HomeTopCardType.QUICK_INSIGHTS }
        }
    }
    val editableTopCardsOrder = visibleTopCardsOrder

    // Configure screen header with actions
    val headerConfigCallback = LocalScreenHeaderConfigCallback.current
    val dashboardTitle = stringResource(R.string.common_dashboard)
    LaunchedEffect(Unit) {
        headerConfigCallback?.invoke(
            ScreenHeaderConfig(
                title = dashboardTitle,
                showSearchIcon = true,
                hasOrderOption = true,
                onSearchClick = {
                    onEvent(HomeEvent.ToggleSearchExpanded)
                },
                onOrderClick = {
                    showTopCardsOrderDialog = true
                },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Help button
                        HelpButton(
                            onHelpClick = { showHelpDialog = true },
                        )
                    }
                }
            )
        )
    }

    // Tutorial full-screen (non mostrare durante il caricamento iniziale)
    // Questo previene il flickering causato dalla race condition dello stato asincrono
    if (!isTutorialCompleted && !isLoading) {
        TutorialOverlay(
            onDismiss = {
                onEvent(HomeEvent.SetIsTutorialCompleted(true))
            },
        )
        return
    }

    // From date picker dialog
    if (showFromDatePicker) {
        val datePickerState =
            rememberDatePickerState(initialSelectedDateMillis = state.dateRangeFrom)
        DatePickerDialog(
            onDismissRequest = { showFromDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate ->
                            onEvent(HomeEvent.SetDateRange(selectedDate, state.dateRangeTo))
                        }
                        showFromDatePicker = false
                    },
                ) {
                    AppText(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showFromDatePicker = false }) {
                    AppText(stringResource(R.string.common_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // To date picker dialog
    if (showToDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.dateRangeTo)
        DatePickerDialog(
            onDismissRequest = { showToDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate ->
                            onEvent(HomeEvent.SetDateRange(state.dateRangeFrom, selectedDate))
                        }
                        showToDatePicker = false
                    },
                ) {
                    AppText(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showToDatePicker = false }) {
                    AppText(stringResource(R.string.common_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Help dialog
    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }

    if (showTopCardsOrderDialog) {
        HomeTopCardsOrderDialog(
            order = editingTopCardsOrder,
            onMoveUp = { index ->
                if (index > 0) {
                    editingTopCardsOrder = editingTopCardsOrder.toMutableList().apply {
                        add(index - 1, removeAt(index))
                    }
                }
            },
            onMoveDown = { index ->
                if (index < editingTopCardsOrder.lastIndex) {
                    editingTopCardsOrder = editingTopCardsOrder.toMutableList().apply {
                        add(index + 1, removeAt(index))
                    }
                }
            },
            onDismiss = {
                showTopCardsOrderDialog = false
                editingTopCardsOrder = editableTopCardsOrder
            },
            onConfirm = {
                val updatedOrder = if (showQuickInsightsCard) {
                    editingTopCardsOrder
                } else {
                    val lockedIndex = topCardsOrder.indexOf(HomeTopCardType.QUICK_INSIGHTS)
                    if (lockedIndex >= 0) {
                        editingTopCardsOrder
                            .toMutableList()
                            .apply {
                                val targetIndex = lockedIndex.coerceAtMost(size)
                                add(targetIndex, HomeTopCardType.QUICK_INSIGHTS)
                            }
                    } else {
                        editingTopCardsOrder
                    }
                }
                topCardsOrderRaw = HomeTopCardType.serialize(updatedOrder)
                // Persist card order to settings for backup/restore
                onEvent(HomeEvent.SetHomeTopCardsOrder(HomeTopCardType.serialize(updatedOrder)))
                showTopCardsOrderDialog = false
                analyticsManager.logEvent("home_top_cards_reordered")
            },
        )
    }

    // Transaction Details Dialog
    if (state.selectedTransaction != null) {
        TransactionDetailsDialog(
            transaction = state.selectedTransaction,
            onDismiss = { onEvent(HomeEvent.DismissTransactionDetails) },
        )
    }

    // Version Dialog
    if (showVersionDialog) {
        AntEasterEggAnimation(
            versionName = com.antcashmanager.android.BuildConfig.VERSION_NAME,
            onDismiss = { showVersionDialog = false }
        )
    }

    // FASE 1: Composable for list pane (used in both single-pane and split-pane layouts)
    @Composable
    fun HomeListPane() {
        Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("home_screen"),
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                floatingActionButton = {
                    // Scroll to top button
                    AnimatedVisibility(
                        visible = showScrollToTop,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        FloatingActionButton(
                            onClick = {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(0)
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(bottom = 8.dp) // Extra padding to avoid bottom bar
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = stringResource(R.string.home_scroll_to_top)
                            )
                        }
                    }
                },
            ) { innerPadding ->
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(
                            horizontal = adaptiveLayoutInfo.horizontalPadding,
                            vertical = if (adaptiveLayoutInfo.isExpanded) 16.dp else 12.dp,
                        ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Date Range Filter - nascosto quando la ricerca è attiva
                    if (!state.isSearchExpanded) {
                        item {
                            DateRangeFilter(
                                selectedPresetIndex = state.selectedPresetIndex,
                                presets = HomeState.PRESETS,
                                dateRangeFrom = state.dateRangeFrom,
                                dateRangeTo = state.dateRangeTo,
                                expanded = dateFilterExpanded,
                                onExpandedChange = { expanded ->
                                    onEvent(HomeEvent.SetDateFilterExpanded(expanded))
                                },
                                onPresetSelected = { presetIndex ->
                                    val params = android.os.Bundle().apply {
                                        putString("preset", presetIndex.toString())
                                    }
                                    analyticsManager.logEvent("home_date_filter_changed", params)
                                    onEvent(HomeEvent.SelectPreset(presetIndex))
                                },
                                onFromDateEdit = { showFromDatePicker = true },
                                onToDateEdit = { showToDatePicker = true },
                            )
                        }
                    }

                    // Top Cards (Saldo, Entrate/Uscite, Quick Insights) - nascosti quando la ricerca è attiva
                    if (!state.isSearchExpanded) {
                        visibleTopCardsOrder.forEach { topCardType ->
                            when (topCardType) {
                                HomeTopCardType.BALANCE -> item(key = topCardType.storageKey) {
                                    BalanceCard(
                                        balance = state.balance,
                                        showPaymentTypeBreakdown = showPaymentTypeBreakdown,
                                        balanceByPaymentType = state.balanceByPaymentType,
                                        reduceMotion = reduceMotion,
                                    )
                                }

                                HomeTopCardType.INCOME_EXPENSE -> item(key = topCardType.storageKey) {
                                    IncomeExpenseRow(
                                        totalIncome = state.totalIncome,
                                        totalExpense = state.totalExpense,
                                    )
                                }

                                HomeTopCardType.QUICK_INSIGHTS -> item(key = topCardType.storageKey) {
                                    QuickInsightsCard(
                                        totalIncome = state.totalIncome,
                                        totalExpense = state.totalExpense,
                                        transactionCount = state.filteredTransactions.size,
                                        netBalance = netBalance,
                                        dailyAverageExpense = dailyAverageExpense,
                                        biggestExpenseCategory = biggestExpense?.category,
                                        biggestExpenseAmount = biggestExpense?.amount,
                                    )
                                }
                            }
                        }
                    }

                    // Recent Transactions header
                    item {
                        AppText(
                            text = stringResource(
                                R.string.home_recent_transactions_count,
                                state.filteredTransactions.size,
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.testTag("recent_transactions_count"),
                        )
                    }

                    // Search Component - renderizzato condizionalmente come item separato
                    // Pattern replicato da TransactionsScreen per fix timing issue del FocusRequester
                    if (state.isSearchExpanded) {
                        item {
                            SearchComponent(
                                isVisible = true,
                                searchQuery = state.searchQuery,
                                onSearchQueryChange = { newQuery ->
                                    if (newQuery.isNotEmpty() && state.searchQuery.isEmpty()) {
                                        analyticsManager.logEvent("home_search_submitted")
                                        // Track search query initiated
                                        analyticsManager.logEvent("search_query_initiated", android.os.Bundle().apply {
                                            putInt("query_length", newQuery.length)
                                            putBoolean("filters_active", false)
                                        })
                                    } else if (newQuery.isEmpty() && state.searchQuery.isNotEmpty()) {
                                        analyticsManager.logEvent("home_search_cleared")
                                    }
                                    onEvent(HomeEvent.UpdateSearchQuery(newQuery))
                                },
                                searchSuggestions = state.searchSuggestions,
                                modifier = Modifier.testTag("search_component"),
                            )
                        }
                    }

                    // Transactions content
                    if (state.filteredTransactions.isEmpty()) {
                        item {
                            AntEmptyState(
                                mascotRes = R.drawable.ic_piggy_bank,
                                title = stringResource(R.string.empty_state_no_transactions),
                                subtitle = stringResource(R.string.empty_state_no_transactions_subtitle),
                            )
                        }
                    } else {
                        items(
                            items = state.recentTransactions,
                            key = { it.id },
                        ) { transaction ->
                            RecentTransactionItem(
                                transaction = transaction,
                                onClick = {
                                    val params = android.os.Bundle().apply {
                                        putInt("index", state.recentTransactions.indexOf(transaction))
                                        putString("type", transaction.type.name)
                                    }
                                    analyticsManager.logEvent("home_transaction_clicked", params)
                                    analyticsManager.logEvent("home_transaction_detail_opened")
                                    // Notify multi-pane coordinator for foldable split-view sync
                                    multiPaneCoordinator?.selectTransaction(
                                        transaction = transaction,
                                        navigateToDetailsPane = foldingFeature?.isSeparating == true
                                    )
                                    onEvent(HomeEvent.ShowTransactionDetails(transaction))
                                },
                                displayType = transactionDisplayType,
                            )
                        }
                    }

                    // Bottom spacer
                    item { VerticalSpacer(SpacingSize.XS) }
                }
            }
    }

    // FASE 1: Composable for transaction details pane (used in split-pane layout on foldable)
    @Composable
    fun TransactionDetailsPane(transaction: Transaction) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppText(
                text = "Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            // Transaction title
            AppText(
                text = transaction.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            // Category and type
            AppText(
                text = "${transaction.category} • ${transaction.type.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Amount (formatted)
            AppText(
                text = "€ ${String.format("%.2f", kotlin.math.abs(transaction.amount))}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            // Notes if present
            if (transaction.notes.isNotEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                AppText(
                    text = "Notes",
                    style = MaterialTheme.typography.labelMedium,
                )
                AppText(
                    text = transaction.notes,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }

    // FASE 1: Main layout logic - choose between split-pane and single-pane
    when {
        state.isLoading -> LoadingState()
        else -> {
            if (adaptiveLayoutInfo.hasFold && adaptiveLayoutInfo.foldingFeature != null) {
                // Split-pane layout for foldable devices
                FoldableAwareLayout(
                    foldingFeature = adaptiveLayoutInfo.foldingFeature,
                    modifier = Modifier.fillMaxSize(),
                    topContent = { _, _ ->
                        HomeListPane()
                    },
                    bottomContent = { _, _ ->
                        // Details pane shown when transaction is selected on foldable
                        if (multiPaneCoordinator?.showDetailsPane?.value == true && state.selectedTransaction != null) {
                            TransactionDetailsPane(state.selectedTransaction!!)
                        }
                    }
                )
            } else {
                // Single-pane layout for phones and tablets without fold
                HomeListPane()
            }
        }
    }
}
