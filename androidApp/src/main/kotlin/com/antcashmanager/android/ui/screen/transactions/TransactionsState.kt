package com.antcashmanager.android.ui.screen.transactions

import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType

/**
 * UI State for Transactions screen.
 */
data class TransactionsState(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedPresetIndex: Int = TransactionsConstants.DEFAULT_PRESET_INDEX,
    val dateRangeFrom: Long = System.currentTimeMillis() - TransactionsConstants.ONE_WEEK_MS,
    val dateRangeTo: Long = System.currentTimeMillis(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val selectedTransactionType: TransactionType? = null,
    val selectedPaymentType: PaymentType? = null,
    val pendingSearchQuery: String = "",
    val pendingCategory: String? = null,
    val pendingTransactionType: TransactionType? = null,
    val pendingPaymentType: PaymentType? = null,
    val isSearchExpanded: Boolean = false,
    val isFiltersExpanded: Boolean = false,
    val searchSuggestions: List<String> = emptyList(),
) {
    val hasActiveFilters: Boolean
        get() = searchQuery.isNotBlank() ||
                selectedCategory != null ||
                selectedTransactionType != null ||
                selectedPaymentType != null

    val hasFilterChanges: Boolean
        get() = pendingSearchQuery != searchQuery ||
                pendingCategory != selectedCategory ||
                pendingTransactionType != selectedTransactionType ||
                pendingPaymentType != selectedPaymentType

    val activeFilterCount: Int
        get() {
            var count = 0
            if (searchQuery.isNotBlank()) count++
            if (selectedCategory != null) count++
            if (selectedTransactionType != null) count++
            if (selectedPaymentType != null) count++
            return count
        }

    val pendingFilterCount: Int
        get() {
            var count = 0
            if (pendingSearchQuery.isNotBlank()) count++
            if (pendingCategory != null) count++
            if (pendingTransactionType != null) count++
            if (pendingPaymentType != null) count++
            return count
        }

    companion object {
        val PRESETS = TransactionsConstants.PRESETS

        fun getDateFromForPreset(index: Int): Long = when (index) {
            0 -> System.currentTimeMillis() - TransactionsConstants.ONE_DAY_MS
            1 -> System.currentTimeMillis() - TransactionsConstants.ONE_WEEK_MS
            2 -> System.currentTimeMillis() - TransactionsConstants.THIRTY_DAYS_MS
            3 -> System.currentTimeMillis() - TransactionsConstants.ONE_YEAR_MS
            else -> System.currentTimeMillis() - TransactionsConstants.ONE_WEEK_MS
        }
    }
}
