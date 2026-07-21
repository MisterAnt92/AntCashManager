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
    val selectedPresetIndex: Int = HomeConstant.DEFAULT_PRESET_INDEX,
    val dateRangeFrom: Long = System.currentTimeMillis() - HomeConstant.ONE_WEEK_MS,
    val dateRangeTo: Long = System.currentTimeMillis(),
    val selectedTransaction: Transaction? = null,
    val searchQuery: String = "",
    val isSearchExpanded: Boolean = false,
    val searchSuggestions: List<String> = emptyList(),
) {
    companion object {
        val PRESETS = HomeConstant.PRESETS

        fun getDateFromForPreset(index: Int): Long = when (index) {
            0 -> System.currentTimeMillis() - HomeConstant.ONE_DAY_MS
            1 -> System.currentTimeMillis() - HomeConstant.ONE_WEEK_MS
            2 -> System.currentTimeMillis() - HomeConstant.THIRTY_DAYS_MS
            3 -> System.currentTimeMillis() - HomeConstant.ONE_YEAR_MS
            4 -> System.currentTimeMillis() - HomeConstant.TWO_YEARS_MS
            5 -> System.currentTimeMillis() - HomeConstant.THREE_YEARS_MS
            6 -> System.currentTimeMillis() - HomeConstant.FIVE_YEARS_MS
            7 -> System.currentTimeMillis() - HomeConstant.SIX_YEARS_MS
            8 -> System.currentTimeMillis() - HomeConstant.ALL_TIME_MS
            else -> System.currentTimeMillis() - HomeConstant.ONE_WEEK_MS
        }
    }
}
