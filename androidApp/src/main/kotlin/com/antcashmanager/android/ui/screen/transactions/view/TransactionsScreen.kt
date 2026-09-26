package com.antcashmanager.android.ui.screen.transactions.view

import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import co.touchlab.kermit.Logger
import com.antcashmanager.android.R
import com.antcashmanager.android.navigation.AppRoute
import com.antcashmanager.android.navigation.LocalScreenHeaderConfigCallback
import com.antcashmanager.android.navigation.ScreenHeaderConfig
import com.antcashmanager.android.ui.base.LocalMultiPaneCoordinator
import com.antcashmanager.android.ui.components.animation.SkeletonLoader
import com.antcashmanager.android.ui.components.dialog.HelpButton
import com.antcashmanager.android.ui.components.filter.DateRangeFilter
import com.antcashmanager.android.ui.components.filter.SearchComponent
import com.antcashmanager.android.ui.components.layout.FoldableAwareLayout
import com.antcashmanager.android.ui.components.layout.LocalDisplayFeatures
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.layout.rememberAdaptiveLayoutInfo
import com.antcashmanager.android.ui.components.state.AntEmptyState
import com.antcashmanager.android.ui.components.state.AntErrorState
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionDisplayType
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
    val transactionDisplayType = state.transactionDisplayType

    TransactionsContent(
        params =
            TransactionsContentParams(
                state = state,
                onEvent = { event ->
                    when (event) {
                        is com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.ApplyFilters -> {
                            val params =
                                Bundle().apply {
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
                                        state.pendingTransactionType?.name ?: "all",
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
                                analyticsManager.logEvent(
                                    "filter_combination_applied",
                                    Bundle().apply {
                                        putInt("filter_count", filterTypes.size)
                                        putString("types", filterTypes.joinToString("|"))
                                    },
                                )
                            }
                        }

                        is com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.ClearAllFilters -> {
                            analyticsManager.logEvent("transactions_filter_cleared")
                        }

                        else -> Unit
                    }
                    viewModel.onEvent(event)
                },
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
internal fun TransactionsContent(params: TransactionsContentParams) {
    val analyticsManager: com.antcashmanager.android.analytics.AnalyticsManager = koinInject()
    val state = params.state
    val onEvent = params.onEvent
    val navController = params.navController
    val transactionDisplayType = params.transactionDisplayType
    val modifier = params.modifier

    // Local UI state for dialogs only (not business logic)
    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    // DateRangeFilter expanded state from state (UDF)
    val dateFilterExpanded = state.dateFilterExpanded
    val coroutineScope = rememberCoroutineScope()

    // Foldable device support
    val displayFeatures = LocalDisplayFeatures.current
    val adaptiveLayoutInfo = rememberAdaptiveLayoutInfo(displayFeatures = displayFeatures)
    val multiPaneCoordinator = LocalMultiPaneCoordinator.current
    val foldingFeature = adaptiveLayoutInfo.foldingFeature

    val listState = rememberLazyListState()
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 3 }
    }

    // Dialogs
    if (showFromDatePicker) {
        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = state.dateRangeFrom,
            )
        DatePickerDialog(
            onDismissRequest = { showFromDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onEvent(
                                com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.SetDateRange(
                                    from = it,
                                    to = state.dateRangeTo,
                                ),
                            )
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

    if (showToDatePicker) {
        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = state.dateRangeTo,
            )
        DatePickerDialog(
            onDismissRequest = { showToDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onEvent(
                                com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.SetDateRange(
                                    from = state.dateRangeFrom,
                                    to = it,
                                ),
                            )
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
                    onEvent(
                        com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.ToggleFiltersExpanded,
                    )
                },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = {
                                onEvent(
                                    com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.ToggleSearchExpanded,
                                )
                            },
                        ) {
                            Icon(
                                imageVector = if (state.isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = stringResource(R.string.transactions_search),
                                tint =
                                    if (state.searchQuery.isNotEmpty()) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                            )
                        }
                        HelpButton(
                            onHelpClick = {
                                analyticsManager.logEvent("transactions_help_opened")
                                showHelpDialog = true
                            },
                        )
                    }
                },
            ),
        )
    }

    // FASE 1: Composable for list pane (used in both single-pane and split-pane layouts)
    @Composable
    fun TransactionListPane() {
        val isGridLayout = adaptiveLayoutInfo.isExpanded && !adaptiveLayoutInfo.hasFold
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .testTag("transactions_screen"),
        ) {
            if (isGridLayout) {
                // FASE 2: Tablet grid layout (2 columns) for expanded screens without fold
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = adaptiveLayoutInfo.horizontalPadding,
                                vertical = 16.dp,
                            ),
                    horizontalArrangement = Arrangement.spacedBy(TransactionsScreenDefaults.CardSpacing),
                    verticalArrangement = Arrangement.spacedBy(TransactionsScreenDefaults.CardSpacing),
                    contentPadding = PaddingValues(bottom = TransactionsScreenDefaults.ListBottomSpacer),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column {
                            TransactionListHeaders(
                                state = state,
                                onEvent = onEvent,
                                dateFilterExpanded = dateFilterExpanded,
                                onShowFromDatePicker = { showFromDatePicker = true },
                                onShowToDatePicker = { showToDatePicker = true },
                            )
                        }
                    }
                    when {
                        state.isLoading -> {
                            items(6) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp),
                                ) {
                                    SkeletonLoader(height = 16.dp, cornerRadius = 8)
                                    VerticalSpacer(SpacingSize.XS)
                                    SkeletonLoader(height = 20.dp, cornerRadius = 8)
                                }
                            }
                        }

                        state.filteredTransactions.isEmpty() -> {
                            item(span = { GridItemSpan(maxLineSpan) }) {
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
                                        multiPaneCoordinator?.selectTransaction(
                                            transaction = transaction,
                                            navigateToDetailsPane = foldingFeature?.isSeparating == true,
                                        )
                                        navController?.navigate(
                                            AppRoute.TransactionRoute.Edit.createRoute(transaction.id),
                                        )
                                    },
                                    displayType = transactionDisplayType,
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = adaptiveLayoutInfo.horizontalPadding,
                                vertical = if (adaptiveLayoutInfo.isExpanded) 16.dp else 12.dp,
                            ),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(TransactionsScreenDefaults.CardSpacing),
                    contentPadding = PaddingValues(bottom = TransactionsScreenDefaults.ListBottomSpacer),
                ) {
                    // Search bar
                    if (state.isSearchExpanded) {
                        item {
                            SearchComponent(
                                isVisible = true,
                                searchQuery = state.searchQuery,
                                onSearchQueryChange = {
                                    onEvent(
                                        com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent
                                            .UpdateSearchQuery(
                                                it,
                                            ),
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
                                        com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent
                                            .UpdateCategoryFilter(
                                                it,
                                            ),
                                    )
                                },
                                onTransactionTypeSelected = {
                                    onEvent(
                                        com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent
                                            .UpdateTransactionTypeFilter(
                                                it,
                                            ),
                                    )
                                },
                                onPaymentTypeSelected = {
                                    onEvent(
                                        com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent
                                            .UpdatePaymentTypeFilter(
                                                it,
                                            ),
                                    )
                                },
                                onClearFilters = {
                                    onEvent(
                                        com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.ClearAllFilters,
                                    )
                                },
                                hasFilterChanges = state.hasFilterChanges,
                                onApplyFilters = {
                                    onEvent(
                                        com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.ApplyFilters,
                                    )
                                    onEvent(
                                        com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.ToggleFiltersExpanded,
                                    )
                                },
                                onCancelFilters = {
                                    onEvent(
                                        com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.CancelFilterChanges,
                                    )
                                },
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
                                onClearAll = {
                                    onEvent(
                                        com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent.ClearAllFilters,
                                    )
                                },
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
                                onEvent(
                                    com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent
                                        .SetDateFilterExpanded(
                                            expanded,
                                        ),
                                )
                            },
                            onPresetSelected = {
                                onEvent(
                                    com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent
                                        .SelectPreset(
                                            it,
                                        ),
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
                                text =
                                    stringResource(
                                        R.string.transactions_results_count,
                                        state.filteredTransactions.size,
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
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp),
                                ) {
                                    SkeletonLoader(height = 16.dp, cornerRadius = 8)
                                    VerticalSpacer(SpacingSize.XS)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        SkeletonLoader(
                                            modifier =
                                                Modifier
                                                    .weight(1f)
                                                    .height(12.dp),
                                            cornerRadius = 6,
                                        )
                                        SkeletonLoader(
                                            modifier =
                                                Modifier
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
                                        val params =
                                            Bundle().apply {
                                                putInt("index", state.filteredTransactions.indexOf(transaction))
                                                putString("type", transaction.type.name)
                                                putString(
                                                    "date",
                                                    SimpleDateFormat(
                                                        "yyyy-MM-dd",
                                                        Locale.getDefault(),
                                                    ).format(Date(transaction.timestamp)),
                                                )
                                            }
                                        analyticsManager.logEvent("transactions_list_item_clicked", params)
                                        // Notify multi-pane coordinator for foldable split-view sync
                                        multiPaneCoordinator?.selectTransaction(
                                            transaction = transaction,
                                            navigateToDetailsPane = foldingFeature?.isSeparating == true,
                                        )
                                        navController?.navigate(
                                            AppRoute.TransactionRoute.Edit.createRoute(transaction.id),
                                        )
                                    },
                                    displayType = transactionDisplayType,
                                )
                            }
                        }
                    }
                }
            } // end else (single-column LazyColumn)
            Column(
                modifier =
                    Modifier
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
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
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

    // FASE 1: Composable for transaction details pane (used in split-pane layout on foldable)
    @Composable
    fun TransactionDetailsPane(transaction: Transaction) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppText(
                text = "Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            AppText(
                text = transaction.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            AppText(
                text = "${transaction.category} • ${transaction.type.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            AppText(
                text = "€ ${String.format("%.2f", kotlin.math.abs(transaction.amount))}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

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

    // Error state overlay (FASE 5: Error Feedback UX)
    if (state.errorState.isError) {
        AntErrorState(
            mascotRes = R.drawable.ic_piggy_bank,
            title = state.errorState.message ?: "An error occurred",
            subtitle = "Please try again",
            modifier = modifier.fillMaxSize(),
            retryLabel = null, // TODO: add RetryLastOperation event to TransactionsEvent
            onRetry = null,
        )
        return
    }

    // FASE 1: Main layout logic - choose between split-pane and single-pane
    if (adaptiveLayoutInfo.hasFold && adaptiveLayoutInfo.foldingFeature != null) {
        // Split-pane layout for foldable devices
        FoldableAwareLayout(
            foldingFeature = adaptiveLayoutInfo.foldingFeature,
            modifier = Modifier.fillMaxSize(),
            topContent = { _, _ ->
                TransactionListPane()
            },
            bottomContent = { _, _ ->
                // Details pane (MVP: placeholder for foldable devices)
                // Note: TransactionsScreen uses navigation for details, not split-pane
                // This can be enhanced later when selectedTransaction is added to state
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AppText(
                        text = "Select a transaction to view details",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
        )
    } else {
        // Single-pane layout for phones and tablets without fold
        TransactionListPane()
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
