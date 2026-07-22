package com.antcashmanager.android.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.dialog.HelpButton
import com.antcashmanager.android.ui.components.filter.DateRangeFilter
import com.antcashmanager.android.ui.components.filter.SearchComponent
import com.antcashmanager.android.ui.components.layout.rememberAdaptiveLayoutInfo
import com.antcashmanager.android.ui.components.overlay.TutorialOverlay
import com.antcashmanager.android.ui.components.state.AntEmptyState
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.home.model.HomeTopCardType
import com.antcashmanager.android.ui.screen.home.transactionDetail.TransactionDetailsDialog
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
import com.antcashmanager.domain.repository.SettingsRepository
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
    val settingsRepository: SettingsRepository = koinInject()

    val state by viewModel.state.collectAsState()

    HomeContent(
        state = state,
        onEvent = { event ->
            viewModel.onEvent(event)
        },
        settingsRepository = settingsRepository,
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
    settingsRepository: SettingsRepository,
    navController: androidx.navigation.NavController,
    modifier: Modifier = Modifier,
) {

    val analyticsManager: com.antcashmanager.android.analytics.AnalyticsManager = koinInject()

    // Date picker state
    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showTopCardsOrderDialog by remember { mutableStateOf(false) }
    var topCardsOrderRaw by rememberSaveable { mutableStateOf(HomeConstant.DEFAULT_TOP_CARDS_ORDER) }
    var editingTopCardsOrder by remember { mutableStateOf(HomeTopCardType.parse(topCardsOrderRaw)) }
    val topCardsOrder = remember(topCardsOrderRaw) { HomeTopCardType.parse(topCardsOrderRaw) }

    // DateRangeFilter expanded state from settings
    val dateFilterExpanded by settingsRepository.getDateFilterExpanded()
        .collectAsState(initial = true)
    val showPaymentTypeBreakdown by settingsRepository.getShowPaymentTypeBreakdown()
        .collectAsState(initial = false)
    val showQuickInsightsCard by settingsRepository.getShowQuickInsightsCard()
        .collectAsState(initial = false)
    val reduceMotion by settingsRepository.getReduceMotion()
        .collectAsState(initial = false)
    val transactionDisplayType by settingsRepository.getTransactionDisplayType()
        .collectAsState(initial = TransactionDisplayType.TREND)
    val isTutorialCompleted by settingsRepository.getIsTutorialCompleted()
        .collectAsState(initial = true)

    val coroutineScope = rememberCoroutineScope()
    val adaptiveLayoutInfo = rememberAdaptiveLayoutInfo()

    val listState = rememberLazyListState()
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 2 }
    }
    val biggestExpense = remember(state.filteredTransactions) {
        state.filteredTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .maxByOrNull { kotlin.math.abs(it.amount) }
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

    // Tutorial full-screen
    if (!isTutorialCompleted) {
        TutorialOverlay(
            onDismiss = {
                coroutineScope.launch {
                    settingsRepository.setIsTutorialCompleted(true)
                }
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

    when {
        state.isLoading -> LoadingState()
        else -> {
            Scaffold(
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
                modifier = modifier.fillMaxSize()
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
                    // Header with action buttons in order: Search, Filter, Settings, Help
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_ant_mascot),
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                AppText(
                                    text = stringResource(R.string.common_dashboard),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Search button
                                IconButton(onClick = {
                                    if (!state.isSearchExpanded) {
                                        analyticsManager.logEvent("home_search_opened")
                                    }
                                    onEvent(HomeEvent.ToggleSearchExpanded)
                                }) {
                                    Icon(
                                        imageVector = if (state.isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                        contentDescription = stringResource(R.string.transactions_search),
                                        tint = if (state.searchQuery.isNotEmpty()) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                // Filter button (Tune icon for top cards customization)
                                IconButton(
                                    onClick = {
                                        editingTopCardsOrder = editableTopCardsOrder
                                        showTopCardsOrderDialog = true
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = stringResource(
                                            R.string.home_customize_top_cards_action,
                                        ),
                                    )
                                }
                                // Help button
                                HelpButton(
                                    onHelpClick = {
                                        analyticsManager.logEvent("home_help_opened")
                                        showHelpDialog = true
                                    },
                                )
                            }
                        }
                    }

                    // Date Range Filter
                    item {
                        DateRangeFilter(
                            selectedPresetIndex = state.selectedPresetIndex,
                            presets = HomeState.PRESETS,
                            dateRangeFrom = state.dateRangeFrom,
                            dateRangeTo = state.dateRangeTo,
                            expanded = dateFilterExpanded,
                            onExpandedChange = { expanded ->
                                coroutineScope.launch {
                                    settingsRepository.setDateFilterExpanded(expanded)
                                }
                            },
                            onPresetSelected = {
                                analyticsManager.logEvent("home_date_filter_changed")
                                onEvent(HomeEvent.SelectPreset(it))
                            },
                            onFromDateEdit = { showFromDatePicker = true },
                            onToDateEdit = { showToDatePicker = true },
                        )
                    }

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
                                    biggestExpenseCategory = biggestExpense?.category,
                                    biggestExpenseAmount = biggestExpense?.amount,
                                )
                            }
                        }
                    }

                    // Recent Transactions header + Search bar
                    // SearchComponent appare sotto il titolo quando expanded dalla top bar
                    item {
                        Column {
                            AppText(
                                text = stringResource(
                                    R.string.home_recent_transactions_count,
                                    state.filteredTransactions.size,
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            SearchComponent(
                                isVisible = state.isSearchExpanded,
                                searchQuery = state.searchQuery,
                                onSearchQueryChange = { newQuery ->
                                    if (newQuery.isNotEmpty() && state.searchQuery.isEmpty()) {
                                        analyticsManager.logEvent("home_search_submitted")
                                    } else if (newQuery.isEmpty() && state.searchQuery.isNotEmpty()) {
                                        analyticsManager.logEvent("home_search_cleared")
                                    }
                                    onEvent(HomeEvent.UpdateSearchQuery(newQuery))
                                },
                                searchSuggestions = state.searchSuggestions,
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
                                    analyticsManager.logEvent("home_transaction_detail_opened")
                                    onEvent(HomeEvent.ShowTransactionDetails(transaction))
                                },
                                displayType = transactionDisplayType,
                            )
                        }
                    }

                    // Bottom spacer
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}


// ══════════════════════════════════════════════════════════════════════════════
// PREVIEWS
// ══════════════════════════════════════════════════════════════════════════════

class MockHomeSettingsRepository : SettingsRepository {
    override fun getTheme() =
        kotlinx.coroutines.flow.flowOf(com.antcashmanager.domain.model.AppTheme.SYSTEM)

    override suspend fun setTheme(theme: com.antcashmanager.domain.model.AppTheme) {}
    override fun getLanguage() =
        kotlinx.coroutines.flow.flowOf(com.antcashmanager.domain.model.AppLanguage.SYSTEM)

    override suspend fun setLanguage(language: com.antcashmanager.domain.model.AppLanguage) {}
    override fun getShowCharts() = kotlinx.coroutines.flow.flowOf(true)
    override suspend fun setShowCharts(show: Boolean) {}
    override fun getHighContrast() = kotlinx.coroutines.flow.flowOf(false)
    override suspend fun setHighContrast(enabled: Boolean) {}
    override fun getLargeText() = kotlinx.coroutines.flow.flowOf(false)
    override suspend fun setLargeText(enabled: Boolean) {}
    override fun getReduceMotion() = kotlinx.coroutines.flow.flowOf(false)
    override suspend fun setReduceMotion(enabled: Boolean) {}
    override fun getShowTransactionNotes() = kotlinx.coroutines.flow.flowOf(true)
    override suspend fun setShowTransactionNotes(show: Boolean) {}
    override fun getMaskAmounts() = kotlinx.coroutines.flow.flowOf(false)
    override suspend fun setMaskAmounts(mask: Boolean) {}
    override fun getCurrencySymbol() = kotlinx.coroutines.flow.flowOf("€")
    override suspend fun setCurrencySymbol(symbol: String) {}
    override fun getDecimalDigits() = kotlinx.coroutines.flow.flowOf(2)
    override suspend fun setDecimalDigits(digits: Int) {}
    override fun getDecimalSeparator() = kotlinx.coroutines.flow.flowOf(",")
    override suspend fun setDecimalSeparator(separator: String) {}
    override fun getThousandsSeparator() = kotlinx.coroutines.flow.flowOf("")
    override suspend fun setThousandsSeparator(separator: String) {}
    override fun getMealVoucherValue() = kotlinx.coroutines.flow.flowOf(5.29)
    override suspend fun setMealVoucherValue(value: Double) {}
    override fun getDateFormat() = kotlinx.coroutines.flow.flowOf("dd/MM/yyyy")
    override suspend fun setDateFormat(pattern: String) {}

    override fun getDateFilterExpanded() = kotlinx.coroutines.flow.flowOf(true)
    override suspend fun setDateFilterExpanded(expanded: Boolean) {}

    override fun getHomeDateFilterPreset() = kotlinx.coroutines.flow.flowOf(1)
    override suspend fun setHomeDateFilterPreset(index: Int) {}
    override fun getHomeDateFilterState() = kotlinx.coroutines.flow.flowOf(
        SavedDateFilter(
            presetIndex = 1,
            from = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000),
            to = System.currentTimeMillis(),
        ),
    )

    override suspend fun setHomeDateFilterState(filter: SavedDateFilter) {}

    override fun getTransactionsDateFilterPreset() = kotlinx.coroutines.flow.flowOf(1)
    override suspend fun setTransactionsDateFilterPreset(index: Int) {}
    override fun getTransactionsDateFilterState() = kotlinx.coroutines.flow.flowOf(
        SavedDateFilter(
            presetIndex = 1,
            from = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000),
            to = System.currentTimeMillis(),
        ),
    )

    override suspend fun setTransactionsDateFilterState(filter: SavedDateFilter) {}

    override fun getChartsDateFilterPreset() = kotlinx.coroutines.flow.flowOf(1)
    override suspend fun setChartsDateFilterPreset(index: Int) {}
    override fun getChartsDateFilterState() = kotlinx.coroutines.flow.flowOf(
        SavedDateFilter(
            presetIndex = 1,
            from = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000),
            to = System.currentTimeMillis(),
        ),
    )

    override suspend fun setChartsDateFilterState(filter: SavedDateFilter) {}

    override fun getChartsZoomEnabled() = kotlinx.coroutines.flow.flowOf(true)
    override suspend fun setChartsZoomEnabled(enabled: Boolean) {}

    override fun getShowPaymentTypeBreakdown() = kotlinx.coroutines.flow.flowOf(true)
    override suspend fun setShowPaymentTypeBreakdown(show: Boolean) {}
    override fun getShowQuickInsightsCard() = kotlinx.coroutines.flow.flowOf(true)
    override suspend fun setShowQuickInsightsCard(show: Boolean) {}

    override fun getShowInitialAnimation(): kotlinx.coroutines.flow.Flow<Boolean> =
        kotlinx.coroutines.flow.flowOf(true)

    override suspend fun setShowInitialAnimation(show: Boolean) {}

    override fun getTransactionDisplayType() =
        kotlinx.coroutines.flow.flowOf(TransactionDisplayType.TREND)

    override suspend fun setTransactionDisplayType(displayType: TransactionDisplayType) {}

    override fun getTransactionsTransactionDisplayType() =
        kotlinx.coroutines.flow.flowOf(TransactionDisplayType.TREND)

    override suspend fun setTransactionsTransactionDisplayType(displayType: TransactionDisplayType) {}

    override fun getIsTutorialCompleted(): kotlinx.coroutines.flow.Flow<Boolean> =
        kotlinx.coroutines.flow.flowOf(true)

    override suspend fun setIsTutorialCompleted(completed: Boolean) {}

    override fun getDataEncryptionEnabled(): kotlinx.coroutines.flow.Flow<Boolean> =
        kotlinx.coroutines.flow.flowOf(false)

    override suspend fun setDataEncryptionEnabled(enabled: Boolean) {}

    override fun getCategorySortOrderInitialized(): kotlinx.coroutines.flow.Flow<Boolean> =
        kotlinx.coroutines.flow.flowOf(true)

    override suspend fun setCategorySortOrderInitialized(initialized: Boolean) {}

    override fun getLastBackupTimestamp(): kotlinx.coroutines.flow.Flow<Long?> =
        kotlinx.coroutines.flow.flowOf(null)

    override suspend fun setLastBackupTimestamp(timestamp: Long) {}
    override fun getLastRestoreTimestamp(): kotlinx.coroutines.flow.Flow<Long?> =
        kotlinx.coroutines.flow.flowOf(null)

    override suspend fun setLastRestoreTimestamp(timestamp: Long) {}

    override fun getSuggestionsEnabled(): kotlinx.coroutines.flow.Flow<Boolean> =
        kotlinx.coroutines.flow.flowOf(true)

    override suspend fun setSuggestionsEnabled(enabled: Boolean) {}
    override fun getSuggestionsClearedAt(): kotlinx.coroutines.flow.Flow<Long?> =
        kotlinx.coroutines.flow.flowOf(null)

    override suspend fun setSuggestionsClearedAt(timestamp: Long) {}

    override fun getWidgetBackgroundColor(): kotlinx.coroutines.flow.Flow<Long> =
        kotlinx.coroutines.flow.flowOf(0xFFFFFFFFL)

    override suspend fun setWidgetBackgroundColor(color: Long) {}
    override fun getWidgetOpacity(): kotlinx.coroutines.flow.Flow<Int> =
        kotlinx.coroutines.flow.flowOf(100)

    override suspend fun setWidgetOpacity(opacity: Int) {}

    override suspend fun resetAllPreferences() {}
}

private val sampleTransactions = listOf(
    Transaction(
        id = 1,
        title = "Salary",
        amount = 2500.0,
        category = "Work",
        type = TransactionType.INCOME,
        timestamp = System.currentTimeMillis(),
    ),
    Transaction(
        id = 2,
        title = "Groceries",
        amount = 85.50,
        category = "Food",
        type = TransactionType.EXPENSE,
        timestamp = System.currentTimeMillis(),
    ),
    Transaction(
        id = 3,
        title = "Electric Bill",
        amount = 120.0,
        category = "Utilities",
        type = TransactionType.EXPENSE,
        timestamp = System.currentTimeMillis(),
    ),
)

@Preview(showBackground = true, name = "HomeScreen - With Transactions")
@Preview(showBackground = true, name = "HomeScreen - 7 inch", widthDp = 600, heightDp = 960)
@Preview(showBackground = true, name = "HomeScreen - 10 inch", widthDp = 840, heightDp = 1280)
@Composable
private fun HomeContentPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        val navController = androidx.navigation.compose.rememberNavController()
        HomeContent(
            state = HomeState(
                transactions = sampleTransactions,
                filteredTransactions = sampleTransactions,
                recentTransactions = sampleTransactions,
                totalIncome = 2500.0,
                totalExpense = 205.5,
                balance = 2294.5,
                balanceByPaymentType = mapOf(
                    PaymentType.ELECTRONIC to 1500.0,
                    PaymentType.CASH to 794.5,
                ),
            ),
            onEvent = {},
            settingsRepository = MockHomeSettingsRepository(),
            navController = navController,
            modifier = Modifier,
        )
    }
}

@Preview(showBackground = true, name = "HomeScreen - Empty")
@Composable
private fun HomeContentEmptyPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        val navController = androidx.navigation.compose.rememberNavController()
        HomeContent(
            state = HomeState(),
            onEvent = {},
            settingsRepository = MockHomeSettingsRepository(),
            navController = navController,
            modifier = Modifier,
        )
    }
}

@Preview(showBackground = true, name = "HomeScreen - Loading")
@Composable
private fun HomeContentLoadingPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        val navController = androidx.navigation.compose.rememberNavController()
        HomeContent(
            state = HomeState(isLoading = true),
            onEvent = {},
            settingsRepository = MockHomeSettingsRepository(),
            navController = navController,
            modifier = Modifier,
        )
    }
}

@Preview(showBackground = true, name = "HomeScreen - Dark Theme")
@Composable
private fun HomeContentDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        val navController = androidx.navigation.compose.rememberNavController()
        HomeContent(
            state = HomeState(
                transactions = sampleTransactions,
                filteredTransactions = sampleTransactions,
                recentTransactions = sampleTransactions,
                totalIncome = 2500.0,
                totalExpense = 205.5,
                balance = 2294.5,
                balanceByPaymentType = mapOf(
                    PaymentType.ELECTRONIC to 1500.0,
                    PaymentType.CASH to 794.5,
                ),
            ),
            onEvent = {},
            settingsRepository = MockHomeSettingsRepository(),
            navController = navController,
            modifier = Modifier,
        )
    }
}

@Preview(showBackground = true, name = "Transaction Details Dialog - Income")
@Composable
private fun TransactionDetailsDialogIncomePreview() {
    AntCashManagerTheme(dynamicColor = false) {
        TransactionDetailsDialog(
            transaction = Transaction(
                id = 1,
                title = "Salary Payment",
                amount = 2500.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = System.currentTimeMillis(),
                notes = "Monthly salary",
                payee = "Acme Corp",
                location = "Office",
                isRecurring = true,
                recurrenceInterval = "monthly",
                tags = "salary,income",
            ),
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true, name = "Transaction Details Dialog - Expense")
@Composable
private fun TransactionDetailsDialogExpensePreview() {
    AntCashManagerTheme(dynamicColor = false) {
        TransactionDetailsDialog(
            transaction = Transaction(
                id = 2,
                title = "Groceries",
                amount = 85.50,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis(),
                notes = "Weekly shopping",
                tags = "food,groceries",
            ),
            onDismiss = {},
        )
    }
}
