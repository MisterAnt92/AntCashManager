package com.antcashmanager.android.ui.screen.transactions

// ══════════════════════════════════════════════════════════════════════════════
// STATE
// ══════════════════════════════════════════════════════════════════════════════

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.category.GetCategoriesUseCase
import com.antcashmanager.domain.usecase.transaction.DeleteTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionsUseCase
import com.antcashmanager.domain.usecase.transaction.InsertTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.UpdateTransactionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ══════════════════════════════════════════════════════════════════════════════
// EVENTS
// ══════════════════════════════════════════════════════════════════════════════

/**
 * UI Events for Transactions screen.
 */
sealed interface TransactionsEvent {
    data class SelectPreset(val index: Int) : TransactionsEvent
    data class SetDateRange(val from: Long, val to: Long) : TransactionsEvent
    data object AddTransactionClicked : TransactionsEvent
    data class DeleteTransaction(val transaction: Transaction) : TransactionsEvent
    data class UpdateTransaction(val transaction: Transaction) : TransactionsEvent
}

// ══════════════════════════════════════════════════════════════════════════════
// VIEWMODEL
// ══════════════════════════════════════════════════════════════════════════════

class TransactionsViewModel(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {

    // ── UseCases ──
    private val getTransactionsUseCase = GetTransactionsUseCase(transactionRepository)
    private val insertTransactionUseCase = InsertTransactionUseCase(transactionRepository)
    private val updateTransactionUseCase = UpdateTransactionUseCase(transactionRepository)
    private val deleteTransactionUseCase = DeleteTransactionUseCase(transactionRepository)
    private val getCategoriesUseCase = GetCategoriesUseCase(categoryRepository)

    // ── Internal filter state ──
    private val _filterState = MutableStateFlow(FilterState())

    // ── Combined UI State ──
    val state: StateFlow<TransactionsState> = combine(
        getTransactionsUseCase(),
        getCategoriesUseCase(),
        _filterState,
    ) { transactions, categories, filterState ->
        val filtered = transactions.filter {
            it.timestamp in filterState.dateRangeFrom..filterState.dateRangeTo
        }
        TransactionsState(
            transactions = transactions,
            filteredTransactions = filtered,
            categories = categories,
            isLoading = false,
            selectedPresetIndex = filterState.selectedPresetIndex,
            dateRangeFrom = filterState.dateRangeFrom,
            dateRangeTo = filterState.dateRangeTo,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionsState(isLoading = true),
    )

    // ...existing code...

    // ── Event Handling ──
    fun onEvent(event: TransactionsEvent) {
        Logger.d("TransactionsViewModel") { "Event: $event" }
        when (event) {
            is TransactionsEvent.SelectPreset -> selectPreset(event.index)
            is TransactionsEvent.SetDateRange -> setDateRange(event.from, event.to)
            is TransactionsEvent.AddTransactionClicked -> { /* Navigation handled by Screen */
            }

            is TransactionsEvent.DeleteTransaction -> deleteTransaction(event.transaction)
            is TransactionsEvent.UpdateTransaction -> updateTransaction(event.transaction)
        }
    }

    // ── Private Methods ──
    private fun selectPreset(index: Int) {
        _filterState.update {
            it.copy(
                selectedPresetIndex = index,
                dateRangeFrom = TransactionsState.getDateFromForPreset(index),
                dateRangeTo = System.currentTimeMillis(),
            )
        }
    }

    private fun setDateRange(from: Long, to: Long) {
        _filterState.update { it.copy(dateRangeFrom = from, dateRangeTo = to) }
    }

    fun addTransaction(
        title: String,
        amount: Double,
        category: String,
        type: TransactionType,
        timestamp: Long,
        notes: String = "",
        payee: String = "",
        location: String = "",
        tags: String = "",
        isRecurring: Boolean = false,
        recurrenceInterval: String = "",
    ) {
        Logger.d("TransactionsViewModel") { "Adding transaction: $title" }
        viewModelScope.launch {
            insertTransactionUseCase(
                Transaction(
                    title = title,
                    amount = amount,
                    category = category,
                    type = type,
                    timestamp = timestamp,
                    notes = notes,
                    payee = payee,
                    location = location,
                    tags = tags,
                    isRecurring = isRecurring,
                    recurrenceInterval = recurrenceInterval,
                ),
            )
        }
    }

    // Make update/delete public so tests can call them
    fun updateTransaction(transaction: Transaction) {
        Logger.d("TransactionsViewModel") { "Updating transaction: ${transaction.title}" }
        viewModelScope.launch {
            updateTransactionUseCase(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        Logger.d("TransactionsViewModel") { "Deleting transaction: ${transaction.title}" }
        viewModelScope.launch {
            deleteTransactionUseCase(transaction)
        }
    }
}

/**
 * Internal filter state to track user filter selections.
 */
private data class FilterState(
    val selectedPresetIndex: Int = 1,
    val dateRangeFrom: Long = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000),
    val dateRangeTo: Long = System.currentTimeMillis(),
)

