package com.antcashmanager.android.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.antcashmanager.android.util.calculateBalance
import com.antcashmanager.android.util.calculateTotalExpense
import com.antcashmanager.android.util.calculateTotalIncome
import com.antcashmanager.android.util.withCorrectAmounts
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.transaction.GetTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

// ══════════════════════════════════════════════════════════════════════════════
// EVENTS
// ══════════════════════════════════════════════════════════════════════════════

/**
 * UI Events for Home screen.
 */
sealed interface HomeEvent {
    data class SelectPreset(val index: Int) : HomeEvent
    data class SetDateRange(val from: Long, val to: Long) : HomeEvent
    data class ShowTransactionDetails(val transaction: com.antcashmanager.domain.model.Transaction) :
        HomeEvent

    data object DismissTransactionDetails : HomeEvent
}

// ══════════════════════════════════════════════════════════════════════════════
// VIEWMODEL
// ══════════════════════════════════════════════════════════════════════════════

class HomeViewModel(
    transactionRepository: TransactionRepository,
    categoryRepository: com.antcashmanager.domain.repository.CategoryRepository,
) : ViewModel() {

    // ── UseCases ──
    private val getTransactionsUseCase = GetTransactionsUseCase(transactionRepository)

    // ── Categories cache for enriching transactions ──
    private val categoriesCache: StateFlow<Map<String, com.antcashmanager.domain.model.Category>> =
        categoryRepository.getAllCategories()
            .map { categories -> categories.associateBy { it.name } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyMap()
            )

    // ── Internal filter state ──
    private val _filterState = MutableStateFlow(FilterState())

    // ── Internal transaction selection state ──
    private val _selectedTransactionState =
        MutableStateFlow<com.antcashmanager.domain.model.Transaction?>(null)

    // ── Combined UI State ──
    val state: StateFlow<HomeState> = combine(
        getTransactionsUseCase().map { it.getOrElse { emptyList() } },
        _filterState,
        _selectedTransactionState,
        categoriesCache,
    ) { transactions, filterState, selectedTransaction, categoryCache ->
        val filtered = transactions.filter {
            it.timestamp in filterState.dateRangeFrom..filterState.dateRangeTo
        }

        // Enrich transactions with category icon and color from cache
        val enrichedFiltered = filtered.map { transaction ->
            val category = categoryCache[transaction.category]
            if (category != null && (transaction.categoryIcon.isEmpty() || transaction.categoryColor == 0xFF90A4AE)) {
                // Update transaction with category data
                transaction.copy(
                    categoryIcon = category.icon,
                    categoryColor = category.color
                )
            } else {
                transaction
            }
        }

        // Transform amounts based on transaction type - EXPENSE should be negative
        val transformedTransactions = enrichedFiltered.withCorrectAmounts()

        val totalIncome = transformedTransactions.calculateTotalIncome()
        val totalExpense = transformedTransactions.calculateTotalExpense() // This will be negative
        val balance = transformedTransactions.calculateBalance() // Sum of all amounts

        // Calculate balance by payment type (optimized with Map, excluding zeros)
        val balanceByPaymentType = transformedTransactions
            .groupBy { it.paymentType }
            .mapValues { (_, txs) -> txs.sumOf { it.amount } }
            .filterValues { it != 0.0 }

        HomeState(
            transactions = transactions.withCorrectAmounts(),
            filteredTransactions = transformedTransactions,
            recentTransactions = transformedTransactions.take(5),
            totalIncome = totalIncome,
            totalExpense = totalExpense, // Will be negative for display
            balance = balance,
            balanceByPaymentType = balanceByPaymentType,
            isLoading = false,
            selectedPresetIndex = filterState.selectedPresetIndex,
            dateRangeFrom = filterState.dateRangeFrom,
            dateRangeTo = filterState.dateRangeTo,
            selectedTransaction = selectedTransaction,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeState(isLoading = true),
    )

    // Convenience StateFlows used by UI/tests: expose transactions and recentTransactions directly
    val transactions = state.map { it.filteredTransactions }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentTransactions = state.map { it.recentTransactions }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        Logger.d("HomeViewModel") { "HomeViewModel initialized" }
    }

    // ── Event Handling ──
    fun onEvent(event: HomeEvent) {
        Logger.d("HomeViewModel") { "Event: $event" }
        when (event) {
            is HomeEvent.SelectPreset -> selectPreset(event.index)
            is HomeEvent.SetDateRange -> setDateRange(event.from, event.to)
            is HomeEvent.ShowTransactionDetails -> _selectedTransactionState.value =
                event.transaction

            HomeEvent.DismissTransactionDetails -> _selectedTransactionState.value = null
        }
    }

    private fun selectPreset(index: Int) {
        _filterState.update {
            it.copy(
                selectedPresetIndex = index,
                dateRangeFrom = HomeState.getDateFromForPreset(index),
                dateRangeTo = System.currentTimeMillis(),
            )
        }
    }

    private fun setDateRange(from: Long, to: Long) {
        _filterState.update { it.copy(dateRangeFrom = from, dateRangeTo = to) }
    }
}

/**
 * Internal filter state.
 */
private data class FilterState(
    val selectedPresetIndex: Int = 1,
    val dateRangeFrom: Long = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000),
    val dateRangeTo: Long = System.currentTimeMillis(),
)

