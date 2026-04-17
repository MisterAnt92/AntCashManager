package com.antcashmanager.android.ui.screen.transactions

import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType

/**
 * UI State for Transactions screen.
 */
data class TransactionsState(
    // Transaction data
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),

    // Loading/Error states
    val isLoading: Boolean = false,
    val error: String? = null,

    // Date range filter
    val selectedPresetIndex: Int = 1,
    val dateRangeFrom: Long = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000),
    val dateRangeTo: Long = System.currentTimeMillis(),

    // Search & Filters
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val selectedTransactionType: TransactionType? = null,
    val selectedPaymentType: PaymentType? = null,

    // UI state for collapsible sections
    val isSearchExpanded: Boolean = false,
    val isFiltersExpanded: Boolean = false,
) {
    /**
     * Computed property: true if any filter is active (excluding date range).
     */
    val hasActiveFilters: Boolean
        get() = searchQuery.isNotBlank() ||
                selectedCategory != null ||
                selectedTransactionType != null ||
                selectedPaymentType != null

    companion object {
        val PRESETS = listOf(
            "Oggi" to "today",
            "7 giorni" to "week",
            "Mese" to "month",
            "Anno" to "year",
        )

        fun getDateFromForPreset(index: Int): Long = when (index) {
            0 -> System.currentTimeMillis() - (24 * 60 * 60 * 1000)
            1 -> System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            2 -> System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            3 -> System.currentTimeMillis() - (365L * 24 * 60 * 60 * 1000)
            else -> System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        }
    }
}
