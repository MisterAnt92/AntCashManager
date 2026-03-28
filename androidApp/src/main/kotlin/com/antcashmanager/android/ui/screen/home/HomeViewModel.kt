package com.antcashmanager.android.ui.screen.home.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.antcashmanager.domain.model.TransactionType
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
}

// ══════════════════════════════════════════════════════════════════════════════
// VIEWMODEL
// ══════════════════════════════════════════════════════════════════════════════

class HomeViewModel(
    transactionRepository: TransactionRepository,
) : ViewModel() {

    // ── UseCases ──
    private val getTransactionsUseCase = GetTransactionsUseCase(transactionRepository)

    // ── Internal filter state ──
    private val _filterState = MutableStateFlow(FilterState())

    // ── Combined UI State ──
    val state: StateFlow<HomeState> = combine(
        getTransactionsUseCase(),
        _filterState,
    ) { transactions, filterState ->
        val filtered = transactions.filter {
            it.timestamp in filterState.dateRangeFrom..filterState.dateRangeTo
        }
        val totalIncome = filtered.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense =
            filtered.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        HomeState(
            transactions = transactions,
            filteredTransactions = filtered,
            recentTransactions = filtered.take(5),
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = totalIncome - totalExpense,
            isLoading = false,
            selectedPresetIndex = filterState.selectedPresetIndex,
            dateRangeFrom = filterState.dateRangeFrom,
            dateRangeTo = filterState.dateRangeTo,
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

