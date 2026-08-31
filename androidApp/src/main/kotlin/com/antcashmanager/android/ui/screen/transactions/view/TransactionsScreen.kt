package com.antcashmanager.android.ui.screen.transactions.view

import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.layout.HorizontalSpacer
import com.antcashmanager.android.ui.components.layout.LocalDisplayFeatures
import com.antcashmanager.android.ui.base.LocalMultiPaneCoordinator
import androidx.window.layout.FoldingFeature
import androidx.navigation.NavController
import co.touchlab.kermit.Logger
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.animation.AnimatedCard
import com.antcashmanager.android.ui.components.animation.AnimatedListItem
import com.antcashmanager.android.ui.components.animation.SkeletonLoader
import com.antcashmanager.android.ui.components.button.AppButton
import com.antcashmanager.android.ui.components.dialog.AppHelpDialog
import com.antcashmanager.android.ui.components.dialog.HelpButton
import com.antcashmanager.android.ui.components.dialog.HelpDialogFeatureSpec
import com.antcashmanager.android.ui.components.filter.DateRangeFilter
import com.antcashmanager.android.ui.components.filter.SearchComponent
import com.antcashmanager.android.navigation.AppRoute
import com.antcashmanager.android.navigation.LocalScreenHeaderConfigCallback
import com.antcashmanager.android.navigation.ScreenHeaderConfig
import com.antcashmanager.android.ui.components.layout.rememberAdaptiveLayoutInfo
import com.antcashmanager.android.ui.components.state.AntEmptyState
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.components.text.TransactionAmountText
import com.antcashmanager.android.ui.screen.categories.view.categoryIconMap
import com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.ui.theme.ExpenseRed
import com.antcashmanager.android.ui.theme.IncomeGreen
import com.antcashmanager.android.util.LocalAmountsMasked
import com.antcashmanager.android.util.isProtectedSalaryTransaction
import com.antcashmanager.android.util.isValidNote
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ══════════════════════════════════════════════════════════════════════════════
// SCREEN
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun TransactionsScreen(
    navController: NavController? = null,
    modifier: Modifier = Modifier,
) {
    Logger.d(tag = "TransactionsScreen") { "Displaying TransactionsScreen" }
    val analyticsManager: com.antcashmanager.android.analytics.AnalyticsManager = koinInject()

    val viewModel: com.antcashmanager.android.ui.screen.transactions.TransactionsViewModel =
        koinViewModel()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val transactionDisplayType = try {
        TransactionDisplayType.valueOf(state.transactionDisplayType)
    } catch (e: Exception) {
        TransactionDisplayType.TREND
    }

    TransactionsContent(
        params = TransactionsContentParams(
            state = state,
            onEvent = { event ->
                when (event) {
                    is com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.ApplyFilters -> {
                        val params = Bundle().apply {
                            putString(
                                "has_search_query",
                                if (state.pendingSearchQuery.isNotBlank()) "yes" else "no",
                            )
                            putString(
                                "has_category_filter",
                                if (state.pendingCategory != null) "yes" else "no",
                            )
                            putString(
                                "transaction_type",
                                state.pendingTransactionType?.name ?: "all"
                            )
                            putString("payment_type", state.pendingPaymentType?.name ?: "all")
                        }
                        analyticsManager.logEvent("transactions_filter_applied", params)

                        // Track filter combination
                        val filterTypes = mutableListOf<String>()
                        if (state.pendingSearchQuery.isNotBlank()) filterTypes.add("search")
                        if (state.pendingCategory != null) filterTypes.add("category")
                        if (state.pendingTransactionType != null) filterTypes.add("type")
                        if (state.pendingPaymentType != null) filterTypes.add("payment_type")

                        if (filterTypes.isNotEmpty()) {
                            analyticsManager.logEvent("filter_combination_applied", Bundle().apply {
                                putInt("filter_count", filterTypes.size)
                                putString("types", filterTypes.joinToString("|"))
                            })
                        }
                    }

                    is com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.ClearAllFilters -> {
                        analyticsManager.logEvent("transactions_filter_cleared")
                    }

                    else -> Unit
                }
                viewModel.onEvent(event)
            },
            settingsRepository = settingsRepository,
            navController = navController,
            transactionDisplayType = transactionDisplayType,
            modifier = modifier,
        ),
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// CONTENT
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun TransactionsContent(
    params: TransactionsContentParams,
) {
    val analyticsManager: com.antcashmanager.android.analytics.AnalyticsManager = koinInject()
    val state = params.state
    val onEvent = params.onEvent
    val settingsRepository = params.settingsRepository
    val navController = params.navController
    val transactionDisplayType = params.transactionDisplayType
    val modifier = params.modifier

    // Local UI state for dialogs only (not business logic)
    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    // DateRangeFilter expanded state from settings
    val dateFilterExpanded by settingsRepository.getDateFilterExpanded()
        .collectAsStateWithLifecycle(initialValue = true)
    val coroutineScope = rememberCoroutineScope()
    val adaptiveLayoutInfo = rememberAdaptiveLayoutInfo()

    // Foldable device support
    val displayFeatures = LocalDisplayFeatures.current
    val multiPaneCoordinator = LocalMultiPaneCoordinator.current
    val foldingFeature = displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()

    val listState = rememberLazyListState()
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 3 }
    }

    // Dialogs
    if (showFromDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.dateRangeFrom
        )
        DatePickerDialog(
            onDismissRequest = { showFromDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onEvent(
                            com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.SetDateRange(
                                from = it,
                                to = state.dateRangeTo
                            )
                        )
                    }
                    showFromDatePicker = false
                }) {
                    AppText(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showFromDatePicker = false }) {
                    AppText(stringResource(R.string.common_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showToDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.dateRangeTo
        )
        DatePickerDialog(
            onDismissRequest = { showToDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onEvent(
                            com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.SetDateRange(
                                from = state.dateRangeFrom,
                                to = it
                            )
                        )
                    }
                    showToDatePicker = false
                }) {
                    AppText(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showToDatePicker = false }) {
                    AppText(stringResource(R.string.common_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }

    // Configure screen header with actions
    val headerConfigCallback = LocalScreenHeaderConfigCallback.current
    val transactionsTitle = stringResource(R.string.common_transactions)

    LaunchedEffect(Unit) {
        headerConfigCallback?.invoke(
            ScreenHeaderConfig(
                title = transactionsTitle,
                filterCount = if (state.hasActiveFilters) 1 else 0,
                onFilterClick = {
                    if (!state.isFiltersExpanded) {
                        analyticsManager.logEvent("transactions_filter_opened")
                    }
                    onEvent(com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.ToggleFiltersExpanded)
                },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = { onEvent(com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.ToggleSearchExpanded) },
                        ) {
                            Icon(
                                imageVector = if (state.isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = stringResource(R.string.transactions_search),
                                tint = if (state.searchQuery.isNotEmpty()) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        HelpButton(
                            onHelpClick = {
                                analyticsManager.logEvent("transactions_help_opened")
                                showHelpDialog = true
                            },
                        )
                    }
                }
            )
        )
    }

    Box(modifier = modifier
        .fillMaxSize()
        .testTag("transactions_screen")) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = adaptiveLayoutInfo.horizontalPadding,
                    vertical = if (adaptiveLayoutInfo.isExpanded) 16.dp else 12.dp,
                ),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(TransactionsScreenDefaults.CardSpacing),
            contentPadding = PaddingValues(bottom = TransactionsScreenDefaults.ListBottomSpacer)
        ) {
            // Search bar
            if (state.isSearchExpanded) {
                item {
                    SearchComponent(
                        isVisible = true,
                        searchQuery = state.searchQuery,
                        onSearchQueryChange = {
                            onEvent(
                                com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.UpdateSearchQuery(
                                    it
                                )
                            )
                        },
                        searchSuggestions = state.searchSuggestions,
                    )
                }
            }
            // Filters
            if (state.isFiltersExpanded) {
                item { VerticalSpacer(SpacingSize.SM) }
                item {
                    FilterCard(
                        categories = state.categories,
                        selectedCategory = state.pendingCategory,
                        selectedTransactionType = state.pendingTransactionType,
                        selectedPaymentType = state.pendingPaymentType,
                        onCategorySelected = {
                            onEvent(
                                com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.UpdateCategoryFilter(
                                    it
                                )
                            )
                        },
                        onTransactionTypeSelected = {
                            onEvent(
                                com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.UpdateTransactionTypeFilter(
                                    it
                                )
                            )
                        },
                        onPaymentTypeSelected = {
                            onEvent(
                                com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.UpdatePaymentTypeFilter(
                                    it
                                )
                            )
                        },
                        onClearFilters = { onEvent(com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.ClearAllFilters) },
                        hasFilterChanges = state.hasFilterChanges,
                        onApplyFilters = {
                            onEvent(com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.ApplyFilters)
                            onEvent(com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.ToggleFiltersExpanded)
                        },
                        onCancelFilters = { onEvent(com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.CancelFilterChanges) },
                    )
                }
            }
            // Active filters indicator (compact)
            if (state.hasActiveFilters && !state.isFiltersExpanded) {
                item { VerticalSpacer(SpacingSize.XS) }
                item {
                    ActiveFiltersRow(
                        searchQuery = state.searchQuery,
                        selectedCategory = state.selectedCategory,
                        selectedTransactionType = state.selectedTransactionType,
                        selectedPaymentType = state.selectedPaymentType,
                        onClearAll = { onEvent(com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.ClearAllFilters) },
                    )
                }
            }
            item { VerticalSpacer(SpacingSize.SM) }
            // Date Range Filter
            item {
                DateRangeFilter(
                    selectedPresetIndex = state.selectedPresetIndex,
                    presets = com.antcashmanager.android.ui.screen.transactions.TransactionsState.Companion.PRESETS,
                    dateRangeFrom = state.dateRangeFrom,
                    dateRangeTo = state.dateRangeTo,
                    expanded = dateFilterExpanded,
                    onExpandedChange = { expanded ->
                        coroutineScope.launch {
                            settingsRepository.setDateFilterExpanded(expanded)
                        }
                    },
                    onPresetSelected = {
                        onEvent(
                            com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.SelectPreset(
                                it
                            )
                        )
                    },
                    onFromDateEdit = { showFromDatePicker = true },
                    onToDateEdit = { showToDatePicker = true },
                )
            }
            item { VerticalSpacer(SpacingSize.SM) }
            // Results count
            if (state.hasActiveFilters) {
                item {
                    AppText(
                        text = stringResource(
                            R.string.transactions_results_count,
                            state.filteredTransactions.size
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                item { VerticalSpacer(SpacingSize.XS) }
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
                            VerticalSpacer(SpacingSize.XS)
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
                            VerticalSpacer(SpacingSize.XS)
                            SkeletonLoader(height = 20.dp, cornerRadius = 8)
                        }
                    }
                    item { VerticalSpacer(SpacingSize.XXXL) }
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
                    items(state.filteredTransactions, key = { it.id }) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onClick = {
                                val params = Bundle().apply {
                                    putInt("index", state.filteredTransactions.indexOf(transaction))
                                    putString("type", transaction.type.name)
                                    putString("date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(transaction.timestamp)))
                                }
                                analyticsManager.logEvent("transactions_list_item_clicked", params)
                                // Notify multi-pane coordinator for foldable split-view sync
                                multiPaneCoordinator?.selectTransaction(
                                    transaction = transaction,
                                    navigateToDetailsPane = foldingFeature?.isSeparating == true
                                )
                                navController?.navigate(AppRoute.TransactionRoute.Edit.createRoute(transaction.id))
                            },
                            displayType = transactionDisplayType,
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Compare solo quando serve, sopra i FAB principali senza lasciare "buchi" in basso.
            AnimatedVisibility(
                visible = showScrollToTop,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
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

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                // FloatingActionButton scan receipt (sempre visibile)
                FloatingActionButton(
                    onClick = {
                        analyticsManager.logEvent("receipt_scan_opened")
                        navController?.navigate(AppRoute.TransactionRoute.ReceiptScan.route)
                    },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = stringResource(R.string.receipt_scan_nav_label),
                        modifier = Modifier.size(24.dp),
                    )
                }

                // FloatingActionButton add transaction (sempre visibile)
                FloatingActionButton(
                    onClick = {
                        analyticsManager.logEvent("transaction_add_opened")
                        navController?.navigate(AppRoute.TransactionRoute.Add.route)
                    },
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
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// PARAMS & COSTANTI
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Parametri raggruppati per TransactionsContent per ridurre la lunghezza della signature.
 */
data class TransactionsContentParams(
    val state: com.antcashmanager.android.ui.screen.transactions.TransactionsState,
    val onEvent: (com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent) -> Unit,
    val navController: NavController? = null,
    val transactionDisplayType: TransactionDisplayType = TransactionDisplayType.TREND,
    val modifier: Modifier = Modifier,
)

private object TransactionsScreenDefaults {
    val ScreenHorizontalPadding = 16.dp
    val ScreenVerticalPadding = 12.dp
    val CardSpacing = 12.dp
    val SectionSpacing = 8.dp
    val FilterChipSpacing = 8.dp
    val FilterChipVerticalSpacing = 4.dp
    val FilterCardPadding = 12.dp
    val SkeletonLoaderHeight = 16.dp
    val SkeletonLoaderCornerRadius = 8
    val SkeletonLoaderRowHeight = 12.dp
    val SkeletonLoaderRowCornerRadius = 6
    val SkeletonLoaderBottomHeight = 20.dp
    val ScrollToTopButtonSize = 44.dp
    val ScrollToTopIconSize = 20.dp
    val ReceiptButtonIconSize = 24.dp
    val AddButtonIconSize = 28.dp
    val FilterHeaderSpacing = 4.dp
    val FilterHeaderFontWeight = FontWeight.SemiBold
    val FilterSectionSpacing = 6.dp
    val ListBottomSpacer = 80.dp
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
                    AppText(
                        text = stringResource(R.string.common_clear),
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
                VerticalSpacer(SpacingSize.XXS)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    FilterChip(
                        selected = selectedTransactionType == null,
                        onClick = { onTransactionTypeSelected(null) },
                        label = { AppText(stringResource(R.string.common_all), maxLines = 1) },
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
                            AppText(
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
                            AppText(
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
                VerticalSpacer(SpacingSize.XXS)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    FilterChip(
                        selected = selectedPaymentType == null,
                        onClick = { onPaymentTypeSelected(null) },
                        label = { AppText(stringResource(R.string.common_all), maxLines = 1) },
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
                                AppText(
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
                    VerticalSpacer(SpacingSize.XXS)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { onCategorySelected(null) },
                            label = { AppText(stringResource(R.string.common_all), maxLines = 1) },
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
                                label = { AppText(category.name, maxLines = 1) },
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
                VerticalSpacer(SpacingSize.XS)
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
                        AppText(
                            text = stringResource(R.string.common_cancel),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    AppButton(
                        onClick = onApplyFilters,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                    ) {
                        AppText(
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
                        AppText(
                            text = stringResource(
                                R.string.transactions_search_query_preview,
                                searchQuery
                            ),
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
                        AppText(
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
                        AppText(
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
                    label = { AppText(category, maxLines = 1) },
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
                contentDescription = stringResource(R.string.common_clear),
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
                                imageVector = if (isIncome) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = if (isIncome) IncomeGreen else ExpenseRed,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        HorizontalSpacer(SpacingSize.SM)
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
                        HorizontalSpacer(SpacingSize.SM)
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
                            HorizontalSpacer(SpacingSize.XXXS)
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
                        masked = LocalAmountsMasked.current && isProtectedSalaryTransaction(
                            transaction
                        ),
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

    override fun getShowPaymentTypeBreakdown() = kotlinx.coroutines.flow.flowOf(false)
    override suspend fun setShowPaymentTypeBreakdown(show: Boolean) {}
    override fun getShowQuickInsightsCard() = kotlinx.coroutines.flow.flowOf(true)
    override suspend fun setShowQuickInsightsCard(show: Boolean) {}
    override fun getDefaultPaymentType() = kotlinx.coroutines.flow.flowOf("ELECTRONIC")
    override suspend fun setDefaultPaymentType(paymentType: String) {}

    override fun getTransactionDisplayType(): Flow<TransactionDisplayType> =
        kotlinx.coroutines.flow.flowOf(TransactionDisplayType.TREND)

    override suspend fun setTransactionDisplayType(displayType: TransactionDisplayType) {}

    override fun getTransactionsTransactionDisplayType(): Flow<TransactionDisplayType> =
        kotlinx.coroutines.flow.flowOf(TransactionDisplayType.TREND)

    override suspend fun setTransactionsTransactionDisplayType(displayType: TransactionDisplayType) {}

    override fun getIsTutorialCompleted(): Flow<Boolean> = kotlinx.coroutines.flow.flowOf(true)
    override suspend fun setIsTutorialCompleted(completed: Boolean) {}

    override fun getDataEncryptionEnabled(): Flow<Boolean> = kotlinx.coroutines.flow.flowOf(false)

    override suspend fun setDataEncryptionEnabled(enabled: Boolean) {}

    override fun getCategorySortOrderInitialized(): Flow<Boolean> =
        kotlinx.coroutines.flow.flowOf(true)

    override suspend fun setCategorySortOrderInitialized(initialized: Boolean) {}

    override fun getLastBackupTimestamp(): Flow<Long?> = kotlinx.coroutines.flow.flowOf(null)
    override suspend fun setLastBackupTimestamp(timestamp: Long) {}
    override fun getLastRestoreTimestamp(): Flow<Long?> = kotlinx.coroutines.flow.flowOf(null)
    override suspend fun setLastRestoreTimestamp(timestamp: Long) {}

    override fun getSuggestionsEnabled(): Flow<Boolean> = kotlinx.coroutines.flow.flowOf(true)
    override suspend fun setSuggestionsEnabled(enabled: Boolean) {}
    override fun getSuggestionsClearedAt(): Flow<Long?> = kotlinx.coroutines.flow.flowOf(null)
    override suspend fun setSuggestionsClearedAt(timestamp: Long) {}

    override fun getWidgetBackgroundColor(): Flow<Long> =
        kotlinx.coroutines.flow.flowOf(0xFFFFFFFFL)

    override suspend fun setWidgetBackgroundColor(color: Long) {}
    override fun getWidgetOpacity(): Flow<Int> = kotlinx.coroutines.flow.flowOf(100)
    override suspend fun setWidgetOpacity(opacity: Int) {}

    override fun getChartCardsOrder(): Flow<String> = kotlinx.coroutines.flow.flowOf("")
    override suspend fun setChartCardsOrder(order: String) {}
    override fun getHomeTopCardsOrder(): Flow<String> = kotlinx.coroutines.flow.flowOf("")
    override suspend fun setHomeTopCardsOrder(order: String) {}

    // ── Google Drive Backup Configuration ──
    override fun getAutoBackupEnabled(): Flow<Boolean> = kotlinx.coroutines.flow.flowOf(false)
    override suspend fun setAutoBackupEnabled(enabled: Boolean) {}
    override fun getAutoBackupFolderUri(): Flow<String?> = kotlinx.coroutines.flow.flowOf(null)
    override suspend fun setAutoBackupFolderUri(uri: String?) {}
    override fun getAutoBackupDestination(): Flow<com.antcashmanager.domain.model.BackupDestination> =
        kotlinx.coroutines.flow.flowOf(com.antcashmanager.domain.model.BackupDestination.LOCAL)

    override suspend fun setAutoBackupDestination(destination: com.antcashmanager.domain.model.BackupDestination) {}
    override fun getGoogleDriveFolderId(): Flow<String?> = kotlinx.coroutines.flow.flowOf(null)
    override suspend fun setGoogleDriveFolderId(folderId: String?) {}
    override fun getGoogleDriveFolderName(): Flow<String?> = kotlinx.coroutines.flow.flowOf(null)
    override suspend fun setGoogleDriveFolderName(folderName: String?) {}
    override fun getGoogleDriveAuthToken(): Flow<String?> = kotlinx.coroutines.flow.flowOf(null)
    override suspend fun setGoogleDriveAuthToken(token: String?) {}
    override fun getGoogleDriveRefreshToken(): Flow<String?> = kotlinx.coroutines.flow.flowOf(null)
    override suspend fun setGoogleDriveRefreshToken(token: String?) {}
    override fun getGoogleDriveUserEmail(): Flow<String?> = kotlinx.coroutines.flow.flowOf(null)
    override suspend fun setGoogleDriveUserEmail(email: String?) {}

    override suspend fun resetAllPreferences() {}
}

@Preview(showBackground = true, name = "TransactionsScreen - With Data")
@Preview(showBackground = true, name = "TransactionsScreen - 7 inch", widthDp = 600, heightDp = 960)
@Preview(
    showBackground = true,
    name = "TransactionsScreen - 10 inch",
    widthDp = 840,
    heightDp = 1280
)
@Composable
private fun TransactionsContentPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        TransactionsContent(
            params = TransactionsContentParams(
                state = com.antcashmanager.android.ui.screen.transactions.TransactionsState(
                    transactions = sampleTransactions,
                    filteredTransactions = sampleTransactions,
                ),
                onEvent = {},
                settingsRepository = MockSettingsRepository(),
                modifier = Modifier,
            ),
        )
    }
}

@Preview(showBackground = true, name = "TransactionsScreen - Empty")
@Composable
private fun TransactionsContentEmptyPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        TransactionsContent(
            params = TransactionsContentParams(
                state = com.antcashmanager.android.ui.screen.transactions.TransactionsState(),
                onEvent = {},
                settingsRepository = MockSettingsRepository(),
                modifier = Modifier,
            ),
        )
    }
}

@Preview(showBackground = true, name = "TransactionsScreen - Loading")
@Composable
private fun TransactionsContentLoadingPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        TransactionsContent(
            params = TransactionsContentParams(
                state = com.antcashmanager.android.ui.screen.transactions.TransactionsState(
                    isLoading = true
                ),
                onEvent = {},
                settingsRepository = MockSettingsRepository(),
                modifier = Modifier,
            ),
        )
    }
}

@Preview(showBackground = true, name = "TransactionsScreen - Dark Theme")
@Composable
private fun TransactionsContentDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        TransactionsContent(
            params = TransactionsContentParams(
                state = com.antcashmanager.android.ui.screen.transactions.TransactionsState(
                    transactions = sampleTransactions,
                    filteredTransactions = sampleTransactions,
                ),
                onEvent = {},
                settingsRepository = MockSettingsRepository(),
                modifier = Modifier,
            ),
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
        HelpDialogFeatureSpec(
            titleResId = R.string.help_transactions_feature_view_title,
            descriptionResId = R.string.help_transactions_feature_view_desc,
            icon = Icons.Default.ArrowDownward,
        ),
        HelpDialogFeatureSpec(
            titleResId = R.string.help_transactions_feature_filters_title,
            descriptionResId = R.string.help_transactions_feature_filters_desc,
            icon = Icons.Default.ArrowUpward,
        ),
        HelpDialogFeatureSpec(
            titleResId = R.string.help_transactions_feature_search_title,
            descriptionResId = R.string.help_transactions_feature_search_desc,
            icon = Icons.Default.Repeat,
        ),
    )

    AppHelpDialog(
        titleResId = R.string.help_transactions_title,
        descriptionResId = R.string.help_transactions_desc,
        features = helpFeatures,
        onDismiss = onDismiss,
    )
}
