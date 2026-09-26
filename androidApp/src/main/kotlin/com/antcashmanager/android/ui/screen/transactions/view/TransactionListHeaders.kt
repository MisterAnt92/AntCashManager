package com.antcashmanager.android.ui.screen.transactions.view

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.filter.DateRangeFilter
import com.antcashmanager.android.ui.components.filter.SearchComponent
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.transactions.TransactionsState
import com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

/**
 * Renders transaction list headers including search, filters, and date range filter.
 * Shared between grid and single-column layouts.
 */
@Composable
fun TransactionListHeaders(
    state: TransactionsState,
    onEvent: (TransactionsEvent) -> Unit,
    dateFilterExpanded: Boolean,
    onShowFromDatePicker: () -> Unit,
    onShowToDatePicker: () -> Unit,
) {
    if (state.isSearchExpanded) {
        SearchComponent(
            isVisible = true,
            searchQuery = state.searchQuery,
            onSearchQueryChange = {
                onEvent(
                    TransactionsEvent.UpdateSearchQuery(it),
                )
            },
            searchSuggestions = state.searchSuggestions,
        )
    }
    if (state.isFiltersExpanded) {
        VerticalSpacer(SpacingSize.SM)
        FilterCard(
            categories = state.categories,
            selectedCategory = state.pendingCategory,
            selectedTransactionType = state.pendingTransactionType,
            selectedPaymentType = state.pendingPaymentType,
            onCategorySelected = {
                onEvent(
                    TransactionsEvent.UpdateCategoryFilter(
                        it,
                    ),
                )
            },
            onTransactionTypeSelected = {
                onEvent(
                    TransactionsEvent.UpdateTransactionTypeFilter(
                        it,
                    ),
                )
            },
            onPaymentTypeSelected = {
                onEvent(
                    TransactionsEvent.UpdatePaymentTypeFilter(
                        it,
                    ),
                )
            },
            onClearFilters = {
                onEvent(
                    TransactionsEvent.ClearAllFilters,
                )
            },
            hasFilterChanges = state.hasFilterChanges,
            onApplyFilters = {
                onEvent(TransactionsEvent.ApplyFilters)
                onEvent(
                    TransactionsEvent.ToggleFiltersExpanded,
                )
            },
            onCancelFilters = {
                onEvent(
                    TransactionsEvent.CancelFilterChanges,
                )
            },
        )
    }
    if (state.hasActiveFilters && !state.isFiltersExpanded) {
        VerticalSpacer(SpacingSize.XS)
        ActiveFiltersRow(
            searchQuery = state.searchQuery,
            selectedCategory = state.selectedCategory,
            selectedTransactionType = state.selectedTransactionType,
            selectedPaymentType = state.selectedPaymentType,
            onClearAll = {
                onEvent(
                    TransactionsEvent.ClearAllFilters,
                )
            },
        )
    }
    VerticalSpacer(SpacingSize.SM)
    DateRangeFilter(
        selectedPresetIndex = state.selectedPresetIndex,
        presets = TransactionsState.Companion.PRESETS,
        dateRangeFrom = state.dateRangeFrom,
        dateRangeTo = state.dateRangeTo,
        expanded = dateFilterExpanded,
        onExpandedChange = { expanded ->
            onEvent(
                TransactionsEvent.SetDateFilterExpanded(
                    expanded,
                ),
            )
        },
        onPresetSelected = {
            onEvent(
                TransactionsEvent.SelectPreset(it),
            )
        },
        onFromDateEdit = { onShowFromDatePicker() },
        onToDateEdit = { onShowToDatePicker() },
    )
    VerticalSpacer(SpacingSize.SM)
    if (state.hasActiveFilters) {
        AppText(
            text = stringResource(R.string.transactions_results_count, state.filteredTransactions.size),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        VerticalSpacer(SpacingSize.XS)
    }
}

@Preview(showBackground = true, name = "TransactionListHeaders - Light")
@Composable
private fun TransactionListHeadersLightPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        TransactionListHeaders(
            state = TransactionsState(),
            onEvent = {},
            dateFilterExpanded = false,
            onShowFromDatePicker = {},
            onShowToDatePicker = {},
        )
    }
}

@Preview(showBackground = true, name = "TransactionListHeaders - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TransactionListHeadersDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        TransactionListHeaders(
            state = TransactionsState(),
            onEvent = {},
            dateFilterExpanded = false,
            onShowFromDatePicker = {},
            onShowToDatePicker = {},
        )
    }
}
