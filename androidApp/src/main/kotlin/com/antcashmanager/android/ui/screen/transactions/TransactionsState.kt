package com.antcashmanager.android.ui.screen.transactions

import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.Transaction

/**
 * UI State for Transactions screen.
 */
data class TransactionsState(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedPresetIndex: Int = 1,
    val dateRangeFrom: Long = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000),
    val dateRangeTo: Long = System.currentTimeMillis(),
) {
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

