package com.antcashmanager.android.ui.screen.transactions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import co.touchlab.kermit.Logger
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.AnimatedCard
import com.antcashmanager.android.ui.components.AnimatedListItem
import com.antcashmanager.android.ui.components.AntEmptyState
import com.antcashmanager.android.ui.components.DateRangeFilter
import com.antcashmanager.android.ui.components.HelpButton
import com.antcashmanager.android.ui.components.ScreenHeader
import com.antcashmanager.android.ui.components.HelpDialogContent
import com.antcashmanager.android.ui.components.SearchComponent
import com.antcashmanager.android.ui.components.SimpleHelpFeature
import com.antcashmanager.android.ui.components.SkeletonLoader
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.components.text.TransactionAmountText
import com.antcashmanager.android.ui.screen.categories.categoryIconMap
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.ui.theme.ExpenseRed
import com.antcashmanager.android.ui.theme.IncomeGreen
import com.antcashmanager.android.util.isValidNote
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ══════════════════════════════════════════════════════════════════════════════
// SCREEN
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun TransactionsScreen(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    settingsRepository: SettingsRepository,
    navController: NavController? = null,
) {
    Logger.d("TransactionsScreen") { "Displaying TransactionsScreen" }

    val viewModel: TransactionsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                TransactionsViewModel(transactionRepository, categoryRepository) as T
        },
    )

    val state by viewModel.state.collectAsState()

    val transactionDisplayType by settingsRepository.getTransactionsTransactionDisplayType()
        .collectAsState(initial = TransactionDisplayType.TREND)

    val coroutineScope = rememberCoroutineScope()
    val screenDateFilterPreset by settingsRepository.getTransactionsDateFilterPreset()
        .collectAsState(initial = 1)

    androidx.compose.runtime.LaunchedEffect(screenDateFilterPreset) {
        viewModel.onEvent(TransactionsEvent.SelectPreset(screenDateFilterPreset))
    }

    TransactionsContent(
        state = state,
        onEvent = { event ->
            if (event is TransactionsEvent.SelectPreset) {
                coroutineScope.launch {
                    settingsRepository.setTransactionsDateFilterPreset(event.index)
                }
            }
            viewModel.onEvent(event)
        },
        settingsRepository = settingsRepository,
        navController = navController,
        transactionDisplayType = transactionDisplayType,
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// CONTENT
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun TransactionsContent(
    state: TransactionsState,
    onEvent: (TransactionsEvent) -> Unit,
    settingsRepository: SettingsRepository,
    navController: NavController? = null,
    transactionDisplayType: TransactionDisplayType = TransactionDisplayType.TREND,
) {
    // Local UI state for dialogs only (not business logic)
    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    // DateRangeFilter expanded state from settings
    val dateFilterExpanded by settingsRepository.getDateFilterExpanded()
        .collectAsState(initial = true)
    val coroutineScope = rememberCoroutineScope()

    val listState = rememberLazyListState()
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 3 }
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
                            onEvent(TransactionsEvent.SetDateRange(selectedDate, state.dateRangeTo))
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
                            onEvent(
                                TransactionsEvent.SetDateRange(
                                    state.dateRangeFrom,
                                    selectedDate
                                )
                            )
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

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Add transaction button
                FloatingActionButton(
                    onClick = { navController?.navigate("add_transaction") },
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.transactions_add),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        },
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Header with action icons
            item {
                ScreenHeader(
                    title = stringResource(R.string.transactions_title),
                    actions = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Search toggle button
                            IconButton(
                                onClick = { onEvent(TransactionsEvent.ToggleSearchExpanded) },
                            ) {
                                Icon(
                                    imageVector = if (state.isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = stringResource(R.string.transactions_search),
                                    tint = if (state.searchQuery.isNotEmpty()) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            // Filter toggle button
                            IconButton(
                                onClick = { onEvent(TransactionsEvent.ToggleFiltersExpanded) },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = stringResource(R.string.transactions_filter),
                                    tint = if (state.hasActiveFilters) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            HelpButton(onHelpClick = { showHelpDialog = true })
                        }
                    },
                )
            }

            // Collapsible Search Bar
            item {
                SearchComponent(
                    isVisible = state.isSearchExpanded,
                    searchQuery = state.searchQuery,
                    onSearchQueryChange = { onEvent(TransactionsEvent.UpdateSearchQuery(it)) },
                    searchSuggestions = state.searchSuggestions,
                )
            }

            // Collapsible Filters Card
            item {
                AnimatedVisibility(
                    visible = state.isFiltersExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        FilterCard(
                            categories = state.categories,
                            selectedCategory = state.pendingCategory,
                            selectedTransactionType = state.pendingTransactionType,
                            selectedPaymentType = state.pendingPaymentType,
                            onCategorySelected = { onEvent(TransactionsEvent.UpdateCategoryFilter(it)) },
                            onTransactionTypeSelected = {
                                onEvent(
                                    TransactionsEvent.UpdateTransactionTypeFilter(
                                        it
                                    )
                                )
                            },
                            onPaymentTypeSelected = {
                                onEvent(
                                    TransactionsEvent.UpdatePaymentTypeFilter(
                                        it
                                    )
                                )
                            },
                            onClearFilters = { onEvent(TransactionsEvent.ClearAllFilters) },
                            hasFilterChanges = state.hasFilterChanges,
                            onApplyFilters = {
                                onEvent(TransactionsEvent.ApplyFilters)
                                onEvent(TransactionsEvent.ToggleFiltersExpanded)
                            },
                            onCancelFilters = { onEvent(TransactionsEvent.CancelFilterChanges) },
                        )
                    }
                }
            }

            // Active filters indicator (compact) when filters collapsed
            item {
                if (state.hasActiveFilters && !state.isFiltersExpanded) {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        ActiveFiltersRow(
                            searchQuery = state.searchQuery,
                            selectedCategory = state.selectedCategory,
                            selectedTransactionType = state.selectedTransactionType,
                            selectedPaymentType = state.selectedPaymentType,
                            onClearAll = { onEvent(TransactionsEvent.ClearAllFilters) },
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // Date Range Filter
            item {
                DateRangeFilter(
                    selectedPresetIndex = state.selectedPresetIndex,
                    presets = TransactionsState.PRESETS,
                    dateRangeFrom = state.dateRangeFrom,
                    dateRangeTo = state.dateRangeTo,
                    expanded = dateFilterExpanded,
                    onExpandedChange = { expanded ->
                        coroutineScope.launch {
                            settingsRepository.setDateFilterExpanded(expanded)
                        }
                    },
                    onPresetSelected = { onEvent(TransactionsEvent.SelectPreset(it)) },
                    onFromDateEdit = { showFromDatePicker = true },
                    onToDateEdit = { showToDatePicker = true },
                )
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // Results count when filters active
            item {
                if (state.hasActiveFilters) {
                    Column {
                        AppText(
                            text = stringResource(
                                R.string.transactions_results_count,
                                state.filteredTransactions.size
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Content based on state
            when {
                state.isLoading -> {
                    items(5) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                        ) {
                            SkeletonLoader(height = 16.dp, cornerRadius = 8)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                SkeletonLoader(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(12.dp),
                                    cornerRadius = 6,
                                )
                                SkeletonLoader(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(12.dp),
                                    cornerRadius = 6,
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            SkeletonLoader(height = 20.dp, cornerRadius = 8)
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
                state.filteredTransactions.isEmpty() -> {
                    item {
                        AntEmptyState(
                            mascotRes = R.drawable.ic_piggy_bank,
                            title = stringResource(R.string.empty_state_no_transactions),
                            subtitle = stringResource(R.string.empty_state_no_transactions_subtitle),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                else -> {
                    items(
                        items = state.filteredTransactions,
                        key = { it.id },
                    ) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onClick = {
                                navController?.navigate("add_transaction?transactionId=${transaction.id}")
                            },
                            displayType = transactionDisplayType,
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// FILTER COMPONENTS
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterCard(
    categories: List<Category>,
    selectedCategory: String?,
    selectedTransactionType: TransactionType?,
    selectedPaymentType: PaymentType?,
    onCategorySelected: (String?) -> Unit,
    onTransactionTypeSelected: (TransactionType?) -> Unit,
    onPaymentTypeSelected: (PaymentType?) -> Unit,
    onClearFilters: () -> Unit,
    hasFilterChanges: Boolean = false,
    onApplyFilters: () -> Unit = {},
    onCancelFilters: () -> Unit = {},
) {
    AnimatedCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header with clear button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppText(
                    text = stringResource(R.string.transactions_advanced_filters),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                TextButton(onClick = onClearFilters) {
                    Text(
                        text = stringResource(R.string.transactions_clear_filters),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }

            // Transaction Type Filter
            Column {
                AppText(
                    text = stringResource(R.string.transactions_filter_type),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    FilterChip(
                        selected = selectedTransactionType == null,
                        onClick = { onTransactionTypeSelected(null) },
                        label = { Text(stringResource(R.string.common_all), maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    )
                    FilterChip(
                        selected = selectedTransactionType == TransactionType.INCOME,
                        onClick = {
                            onTransactionTypeSelected(
                                if (selectedTransactionType == TransactionType.INCOME) null
                                else TransactionType.INCOME
                            )
                        },
                        label = {
                            Text(
                                stringResource(R.string.transaction_type_income),
                                maxLines = 1
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = IncomeGreen,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IncomeGreen.copy(alpha = 0.2f),
                        ),
                    )
                    FilterChip(
                        selected = selectedTransactionType == TransactionType.EXPENSE,
                        onClick = {
                            onTransactionTypeSelected(
                                if (selectedTransactionType == TransactionType.EXPENSE) null
                                else TransactionType.EXPENSE
                            )
                        },
                        label = {
                            Text(
                                stringResource(R.string.transaction_type_expense),
                                maxLines = 1
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = ExpenseRed,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ExpenseRed.copy(alpha = 0.2f),
                        ),
                    )
                }
            }

            // Payment Type Filter
            Column {
                AppText(
                    text = stringResource(R.string.transactions_filter_payment),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    FilterChip(
                        selected = selectedPaymentType == null,
                        onClick = { onPaymentTypeSelected(null) },
                        label = { Text(stringResource(R.string.common_all), maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    )
                    PaymentType.values().forEach { paymentType ->
                        FilterChip(
                            selected = selectedPaymentType == paymentType,
                            onClick = {
                                onPaymentTypeSelected(
                                    if (selectedPaymentType == paymentType) null else paymentType
                                )
                            },
                            label = {
                                Text(
                                    text = when (paymentType) {
                                        PaymentType.ELECTRONIC -> stringResource(R.string.payment_type_electronic)
                                        PaymentType.CASH -> stringResource(R.string.payment_type_cash)
                                        PaymentType.MEAL_VOUCHERS -> stringResource(R.string.payment_type_meal_vouchers)
                                    },
                                    maxLines = 1,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            ),
                        )
                    }
                }
            }

            // Category Filter (only show if categories available)
            if (categories.isNotEmpty()) {
                Column {
                    AppText(
                        text = stringResource(R.string.transactions_filter_category),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { onCategorySelected(null) },
                            label = { Text(stringResource(R.string.common_all), maxLines = 1) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )
                        categories.take(8).forEach { category ->
                            FilterChip(
                                selected = selectedCategory == category.name,
                                onClick = {
                                    onCategorySelected(
                                        if (selectedCategory == category.name) null else category.name
                                    )
                                },
                                label = { Text(category.name, maxLines = 1) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                ),
                            )
                        }
                    }
                }
            }

            // Action buttons footer
            if (hasFilterChanges) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = onCancelFilters,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.common_cancel),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    Button(
                        onClick = onApplyFilters,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.common_confirm),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveFiltersRow(
    searchQuery: String,
    selectedCategory: String?,
    selectedTransactionType: TransactionType?,
    selectedPaymentType: PaymentType?,
    onClearAll: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (searchQuery.isNotEmpty()) {
                FilterChip(
                    selected = true,
                    onClick = { },
                    label = {
                        Text(
                            text = "\"$searchQuery\"",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    modifier = Modifier.height(28.dp),
                )
            }
            selectedTransactionType?.let { type ->
                FilterChip(
                    selected = true,
                    onClick = { },
                    label = {
                        Text(
                            text = when (type) {
                                TransactionType.INCOME -> stringResource(R.string.transaction_type_income)
                                TransactionType.EXPENSE -> stringResource(R.string.transaction_type_expense)
                            },
                            maxLines = 1,
                        )
                    },
                    modifier = Modifier.height(28.dp),
                )
            }
            selectedPaymentType?.let { payment ->
                FilterChip(
                    selected = true,
                    onClick = { },
                    label = {
                        Text(
                            text = when (payment) {
                                PaymentType.ELECTRONIC -> stringResource(R.string.payment_type_electronic)
                                PaymentType.CASH -> stringResource(R.string.payment_type_cash)
                                PaymentType.MEAL_VOUCHERS -> stringResource(R.string.payment_type_meal_vouchers)
                            },
                            maxLines = 1,
                        )
                    },
                    modifier = Modifier.height(28.dp),
                )
            }
            selectedCategory?.let { category ->
                FilterChip(
                    selected = true,
                    onClick = { },
                    label = { Text(category, maxLines = 1) },
                    modifier = Modifier.height(28.dp),
                )
            }
        }

        IconButton(
            onClick = onClearAll,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.transactions_clear_filters),
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// COMPONENTS
// ══════════════════════════════════════════════════════════════════════════════


private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

@Composable
private fun getRecurrenceIntervalLabel(interval: String): String {
    return when (interval.lowercase()) {
        "daily" -> stringResource(R.string.transactions_interval_daily)
        "weekly" -> stringResource(R.string.transactions_interval_weekly)
        "monthly" -> stringResource(R.string.transactions_interval_monthly)
        "yearly" -> stringResource(R.string.transactions_interval_yearly)
        else -> stringResource(R.string.transactions_recurring)
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    onClick: (() -> Unit)? = null,
    displayType: TransactionDisplayType = TransactionDisplayType.TREND,
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val cardBackgroundColor =
        if (isIncome) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer

    AnimatedListItem(index = transaction.id.toInt()) {
        AnimatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .let { if (onClick != null) it.clickable { onClick() } else it },
            backgroundColor = cardBackgroundColor,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Icon based on display type
                when (displayType) {
                    TransactionDisplayType.TREND -> {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (isIncome) IncomeGreen.copy(alpha = 0.25f) else ExpenseRed.copy(
                                        alpha = 0.25f
                                    ),
                                    shape = RoundedCornerShape(32.dp),
                                )
                                .padding(8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (isIncome) IncomeGreen else ExpenseRed,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    TransactionDisplayType.CATEGORY -> {
                        val categoryIconVector = categoryIconMap[transaction.categoryIcon]
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    color = androidx.compose.ui.graphics.Color(transaction.categoryColor),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (categoryIconVector != null) {
                                Icon(
                                    imageVector = categoryIconVector,
                                    contentDescription = transaction.category,
                                    tint = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.size(24.dp),
                                )
                            } else {
                                AppText(
                                    text = transaction.category.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = androidx.compose.ui.graphics.Color.White,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    TransactionDisplayType.NONE -> {
                        // No icon
                    }
                }

                // Content
                Column(modifier = Modifier.weight(1f)) {
                    AppText(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isIncome) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                    )

                    // Subtitle
                    val subtitleParts = buildList {
                        add(transaction.category)
                        add(dateFormat.format(Date(transaction.timestamp)))
                        if (transaction.payee.isNotBlank()) add(transaction.payee)
                        if (transaction.location.isNotBlank()) add(transaction.location)
                    }
                    AppText(
                        text = subtitleParts.joinToString(" • "),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isIncome) MaterialTheme.colorScheme.onSecondaryContainer.copy(
                            alpha = 0.7f
                        ) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                    )

                    // Notes
                    if (transaction.notes.isValidNote()) {
                        AppText(
                            text = transaction.notes,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isIncome) MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                alpha = 0.6f
                            ) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    // Tags
                    if (transaction.tags.isNotBlank()) {
                        AppText(
                            text = transaction.tags.split(",")
                                .joinToString(" ") { "#${it.trim()}" },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isIncome) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    // Recurring indicator
                    if (transaction.isRecurring) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = stringResource(R.string.transactions_recurring),
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.tertiary,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            AppText(
                                text = if (transaction.recurrenceInterval.isNotBlank()) {
                                    getRecurrenceIntervalLabel(transaction.recurrenceInterval)
                                } else {
                                    stringResource(R.string.transactions_recurring)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                // Amount with background
                Box(
                    modifier = Modifier
                        .padding(8.dp),
                ) {
                    TransactionAmountText(
                        amount = transaction.amount, // Amount will already be negative for expenses
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// PREVIEWS
// ══════════════════════════════════════════════════════════════════════════════

class MockSettingsRepository : SettingsRepository {
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

    override fun getShowPaymentTypeBreakdown() = kotlinx.coroutines.flow.flowOf(false)
    override suspend fun setShowPaymentTypeBreakdown(show: Boolean) {}
    override fun getTransactionDisplayType(): Flow<TransactionDisplayType> =
        kotlinx.coroutines.flow.flowOf(TransactionDisplayType.TREND)

    override suspend fun setTransactionDisplayType(displayType: TransactionDisplayType) {}

    override fun getTransactionsTransactionDisplayType(): Flow<TransactionDisplayType> =
        kotlinx.coroutines.flow.flowOf(TransactionDisplayType.TREND)

    override suspend fun setTransactionsTransactionDisplayType(displayType: TransactionDisplayType) {}

    override fun getIsTutorialCompleted(): Flow<Boolean> = kotlinx.coroutines.flow.flowOf(true)
    override suspend fun setIsTutorialCompleted(completed: Boolean) {}

    override suspend fun resetAllPreferences() {}
}

@Preview(showBackground = true, name = "TransactionsScreen - With Data")
@Composable
private fun TransactionsContentPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        TransactionsContent(
            state = TransactionsState(
                transactions = sampleTransactions,
                filteredTransactions = sampleTransactions,
            ),
            onEvent = {},
            settingsRepository = MockSettingsRepository(),
        )
    }
}

@Preview(showBackground = true, name = "TransactionsScreen - Empty")
@Composable
private fun TransactionsContentEmptyPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        TransactionsContent(
            state = TransactionsState(),
            onEvent = {},
            settingsRepository = MockSettingsRepository(),
        )
    }
}

@Preview(showBackground = true, name = "TransactionsScreen - Loading")
@Composable
private fun TransactionsContentLoadingPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        TransactionsContent(
            state = TransactionsState(isLoading = true),
            onEvent = {},
            settingsRepository = MockSettingsRepository(),
        )
    }
}

@Preview(showBackground = true, name = "TransactionsScreen - Dark Theme")
@Composable
private fun TransactionsContentDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        TransactionsContent(
            state = TransactionsState(
                transactions = sampleTransactions,
                filteredTransactions = sampleTransactions,
            ),
            onEvent = {},
            settingsRepository = MockSettingsRepository(),
        )
    }
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

// ══════════════════════════════════════════════════════════════════════════════
// HELP DIALOG
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun HelpDialog(onDismiss: () -> Unit) {
    val helpFeatures = listOf(
        SimpleHelpFeature(
            title = "Visualizza Transazioni",
            description = "Vedi tutte le tue transazioni di entrata e uscita con dettagli completi.",
            icon = Icons.Default.ArrowDownward,
        ),
        SimpleHelpFeature(
            title = "Filtri Data",
            description = "Filtra le transazioni per intervallo di date o usa i preset predefiniti.",
            icon = Icons.Default.ArrowUpward,
        ),
        SimpleHelpFeature(
            title = "Cerca Transazioni",
            description = "Cerca transazioni per nome, importo, categoria o altre proprietà.",
            icon = Icons.Default.Repeat,
        ),
    )

    HelpDialogContent(
        isVisible = true,
        title = "Guida Transazioni",
        description = "Gestisci tutte le tue transazioni! Puoi visualizzare, cercare e filtrare le tue operazioni finanziarie.",
        features = helpFeatures,
        onDismiss = onDismiss,
    )
}
