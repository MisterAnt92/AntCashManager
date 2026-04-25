package com.antcashmanager.android.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.unit.LayoutDirection
import co.touchlab.kermit.Logger
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.AntEmptyState
import com.antcashmanager.android.ui.components.DateRangeFilter
import com.antcashmanager.android.ui.components.HelpButton
import com.antcashmanager.android.ui.components.ScreenHeader
import com.antcashmanager.android.ui.components.SearchComponent
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.home.view.BalanceCard
import com.antcashmanager.android.ui.screen.home.view.HelpDialog
import com.antcashmanager.android.ui.screen.home.view.IncomeExpenseRow
import com.antcashmanager.android.ui.screen.home.view.LoadingState
import com.antcashmanager.android.ui.screen.home.view.RecentTransactionItem
import com.antcashmanager.android.ui.screen.homeTransactionDetail.TransactionDetailsDialog
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.launch

// ══════════════════════════════════════════════════════════════════════════════
// SCREEN
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun HomeScreen(
    transactionRepository: TransactionRepository,
    settingsRepository: SettingsRepository,
    categoryRepository: com.antcashmanager.domain.repository.CategoryRepository,
) {
    Logger.d("HomeScreen") { "Displaying HomeScreen" }

    val coroutineScope = rememberCoroutineScope()
    val viewModel: HomeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HomeViewModel(transactionRepository, categoryRepository) as T
        },
    )

    val state by viewModel.state.collectAsState()

    // Screen specific date filter preset from settings
    val screenDateFilterPreset by settingsRepository.getHomeDateFilterPreset()
        .collectAsState(initial = 1)

    // Sync screenDateFilterPreset with viewModel
    androidx.compose.runtime.LaunchedEffect(screenDateFilterPreset) {
        viewModel.onEvent(HomeEvent.SelectPreset(screenDateFilterPreset))
    }

    HomeContent(
        state = state,
        onEvent = { event ->
            if (event is HomeEvent.SelectPreset) {
                coroutineScope.launch {
                    settingsRepository.setHomeDateFilterPreset(event.index)
                }
            }
            viewModel.onEvent(event)
        },
        settingsRepository = settingsRepository,
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
) {

    // Date picker state
    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    // DateRangeFilter expanded state from settings
    val dateFilterExpanded by settingsRepository.getDateFilterExpanded()
        .collectAsState(initial = true)
    val showPaymentTypeBreakdown by settingsRepository.getShowPaymentTypeBreakdown()
        .collectAsState(initial = false)
    val reduceMotion by settingsRepository.getReduceMotion()
        .collectAsState(initial = false)
    val transactionDisplayType by settingsRepository.getTransactionDisplayType()
        .collectAsState(initial = TransactionDisplayType.TREND)
    val coroutineScope = rememberCoroutineScope()

    val listState = rememberLazyListState()
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 2 }
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
                    Text(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showFromDatePicker = false }) {
                    Text(stringResource(R.string.common_cancel))
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
                    Text(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showToDatePicker = false }) {
                    Text(stringResource(R.string.common_cancel))
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
                                contentDescription = null
                            )
                        }
                    }
                }
            ) { padding ->
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = padding.calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                            top = 0.dp,
                            end = padding.calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
                            bottom = padding.calculateBottomPadding(),
                        )
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Header with Help Button
                    item {
                        ScreenHeader(
                            title = stringResource(R.string.home_dashboard),
                            actions = { HelpButton(onHelpClick = { showHelpDialog = true }) },
                        )
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
                            onPresetSelected = { onEvent(HomeEvent.SelectPreset(it)) },
                            onFromDateEdit = { showFromDatePicker = true },
                            onToDateEdit = { showToDatePicker = true },
                        )
                    }

                    // Balance Card
                    item {
                        BalanceCard(
                            balance = state.balance,
                            showPaymentTypeBreakdown = showPaymentTypeBreakdown,
                            balanceByPaymentType = state.balanceByPaymentType,
                            reduceMotion = reduceMotion,
                        )
                    }

                    // Income / Expense Row
                    item {
                        IncomeExpenseRow(
                            totalIncome = state.totalIncome,
                            totalExpense = state.totalExpense,
                        )
                    }

                    // Recent Transactions header with Search toggle
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppText(
                                text = stringResource(R.string.home_recent_transactions),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            IconButton(onClick = { onEvent(HomeEvent.ToggleSearchExpanded) }) {
                                Icon(
                                    imageVector = if (state.isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = stringResource(R.string.transactions_search),
                                    tint = if (state.searchQuery.isNotEmpty()) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    // Search Bar
                    item {
                        SearchComponent(
                            isVisible = state.isSearchExpanded,
                            searchQuery = state.searchQuery,
                            onSearchQueryChange = { onEvent(HomeEvent.UpdateSearchQuery(it)) },
                            searchSuggestions = state.searchSuggestions,
                        )
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
                                onClick = { onEvent(HomeEvent.ShowTransactionDetails(transaction)) },
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
    override fun getCurrencySymbol() = kotlinx.coroutines.flow.flowOf("€")
    override suspend fun setCurrencySymbol(symbol: String) {}
    override fun getDecimalDigits() = kotlinx.coroutines.flow.flowOf(2)
    override suspend fun setDecimalDigits(digits: Int) {}
    override fun getDecimalSeparator() = kotlinx.coroutines.flow.flowOf(",")
    override suspend fun setDecimalSeparator(separator: String) {}
    override fun getThousandsSeparator() = kotlinx.coroutines.flow.flowOf("")
    override suspend fun setThousandsSeparator(separator: String) {}
    override fun getDateFormat() = kotlinx.coroutines.flow.flowOf("dd/MM/yyyy")
    override suspend fun setDateFormat(pattern: String) {}

    override fun getDateFilterExpanded() = kotlinx.coroutines.flow.flowOf(true)
    override suspend fun setDateFilterExpanded(expanded: Boolean) {}

    override fun getHomeDateFilterPreset() = kotlinx.coroutines.flow.flowOf(1)
    override suspend fun setHomeDateFilterPreset(index: Int) {}

    override fun getTransactionsDateFilterPreset() = kotlinx.coroutines.flow.flowOf(1)
    override suspend fun setTransactionsDateFilterPreset(index: Int) {}

    override fun getChartsDateFilterPreset() = kotlinx.coroutines.flow.flowOf(1)
    override suspend fun setChartsDateFilterPreset(index: Int) {}

    override fun getChartsZoomEnabled() = kotlinx.coroutines.flow.flowOf(true)
    override suspend fun setChartsZoomEnabled(enabled: Boolean) {}

    override fun getShowPaymentTypeBreakdown() = kotlinx.coroutines.flow.flowOf(true)
    override suspend fun setShowPaymentTypeBreakdown(show: Boolean) {}
    override fun getTransactionDisplayType() =
        kotlinx.coroutines.flow.flowOf(TransactionDisplayType.TREND)

    override suspend fun setTransactionDisplayType(displayType: TransactionDisplayType) {}

    override fun getTransactionsTransactionDisplayType() =
        kotlinx.coroutines.flow.flowOf(TransactionDisplayType.TREND)

    override suspend fun setTransactionsTransactionDisplayType(displayType: TransactionDisplayType) {}

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
@Composable
private fun HomeContentPreview() {
    AntCashManagerTheme(dynamicColor = false) {
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
        )
    }
}

@Preview(showBackground = true, name = "HomeScreen - Empty")
@Composable
private fun HomeContentEmptyPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        HomeContent(
            state = HomeState(),
            onEvent = {},
            settingsRepository = MockHomeSettingsRepository(),
        )
    }
}

@Preview(showBackground = true, name = "HomeScreen - Loading")
@Composable
private fun HomeContentLoadingPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        HomeContent(
            state = HomeState(isLoading = true),
            onEvent = {},
            settingsRepository = MockHomeSettingsRepository(),
        )
    }
}

@Preview(showBackground = true, name = "HomeScreen - Dark Theme")
@Composable
private fun HomeContentDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        HomeContent(
            state = HomeState(
                transactions = sampleTransactions,
                filteredTransactions = sampleTransactions,
                recentTransactions = sampleTransactions,
                totalIncome = 2500.0,
                totalExpense = 205.5,
                balance = 2294.5,
                balanceByPaymentType = mapOf(
                    PaymentType.ELECTRONIC to 1800.0,
                    PaymentType.CASH to 494.5,
                ),
            ),
            onEvent = {},
            settingsRepository = MockHomeSettingsRepository(),
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
