package com.antcashmanager.android.ui.screen.home

import com.antcashmanager.android.ui.base.ErrorState
import com.antcashmanager.android.ui.mapper.TransactionUi
import com.antcashmanager.android.ui.screen.home.model.HomeTopCardType
import com.antcashmanager.domain.model.MealVoucherSummary
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.model.TransactionType
import kotlin.math.abs

/**
 * UI State for Home screen. Derived values are computed once per emission so the screen
 * only reads them.
 */
data class HomeState(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val recentTransactions: List<TransactionUi> = emptyList(),
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
    // Settings (populated by HomeViewModel from SettingsRepository)
    val topCardsOrder: List<HomeTopCardType> = HomeTopCardType.defaultOrder,
    val editingTopCardsOrder: List<HomeTopCardType>? = null,
    val dateFilterExpanded: Boolean = false,
    val showPaymentTypeBreakdown: Boolean = false,
    val showQuickInsightsCard: Boolean = true,
    val reduceMotion: Boolean = false,
    val transactionDisplayType: TransactionDisplayType = TransactionDisplayType.TREND,
    val isTutorialCompleted: Boolean = false,
    val mealVoucherSummary: MealVoucherSummary? = null,
    // Error handling (FASE 5: Error Feedback UX)
    val errorState: ErrorState = ErrorState(),
) {
    /** Top cards actually rendered: Quick Insights is dropped when its setting is off, Meal Vouchers when no data. */
    val visibleTopCards: List<HomeTopCardType> =
        topCardsOrder
            .filterNot { it == HomeTopCardType.QUICK_INSIGHTS && !showQuickInsightsCard }
            .filterNot { it == HomeTopCardType.MEAL_VOUCHERS && mealVoucherSummary == null }

    val isTopCardsOrderDialogVisible: Boolean get() = editingTopCardsOrder != null

    val netBalance: Double = balance

    val transactionCount: Int = filteredTransactions.size

    /** Average absolute amount per transaction in the current filter. */
    val averageAmount: Double =
        if (transactionCount > 0) (abs(totalIncome) + abs(totalExpense)) / transactionCount else 0.0

    val dailyAverageExpense: Double =
        run {
            val days = (dateRangeTo - dateRangeFrom).toDouble() / HomeConstant.ONE_DAY_MS
            if (days > 0) abs(totalExpense) / days else 0.0
        }

    val biggestExpense: Transaction? =
        filteredTransactions.filter { it.type == TransactionType.EXPENSE }.maxByOrNull { abs(it.amount) }

    /** Payment-type balances in enum order, zero entries already removed. */
    val balanceByPaymentTypeOrdered: List<Pair<PaymentType, Double>> =
        PaymentType.entries.mapNotNull { type -> balanceByPaymentType[type]?.let { type to it } }

    companion object {
        val PRESETS = HomeConstant.PRESETS

        fun getDateFromForPreset(index: Int): Long =
            com.antcashmanager.android.ui.screen.common.DateRangePreset.dateFromForPreset(index)
    }
}
