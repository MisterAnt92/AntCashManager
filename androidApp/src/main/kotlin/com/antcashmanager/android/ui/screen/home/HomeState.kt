package com.antcashmanager.android.ui.screen.home

import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction

/**
 * UI State for Home screen.
 */
data class HomeState(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val balanceByPaymentType: Map<PaymentType, Double> = emptyMap(),
    val isLoading: Boolean = false,
    val isSyncingCategories: Boolean = false,
    val error: String? = null,
    val selectedPresetIndex: Int = HomeConstants.DEFAULT_PRESET_INDEX,
    val dateRangeFrom: Long = System.currentTimeMillis() - HomeConstants.ONE_WEEK_MS,
    val dateRangeTo: Long = System.currentTimeMillis(),
    val selectedTransaction: Transaction? = null,
    val searchQuery: String = "",
    val isSearchExpanded: Boolean = false,
    val searchSuggestions: List<String> = emptyList(),
) {
    companion object {
        val PRESETS = HomeConstants.PRESETS

        fun getDateFromForPreset(index: Int): Long = when (index) {
            0 -> System.currentTimeMillis() - HomeConstants.ONE_DAY_MS
            1 -> System.currentTimeMillis() - HomeConstants.ONE_WEEK_MS
            2 -> System.currentTimeMillis() - HomeConstants.THIRTY_DAYS_MS
            3 -> System.currentTimeMillis() - HomeConstants.ONE_YEAR_MS
            else -> System.currentTimeMillis() - HomeConstants.ONE_WEEK_MS
        }
    }
}
