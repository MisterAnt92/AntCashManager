package com.antcashmanager.android.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.antcashmanager.domain.model.Category
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
import kotlin.math.abs

// ══════════════════════════════════════════════════════════════════════════════
// EXTENDED STATE WITH SEARCH
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Extended TransactionsState with search functionality.
 */
data class TransactionsStateExtended(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedPresetIndex: Int = 1,
    val dateRangeFrom: Long = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000),
    val dateRangeTo: Long = System.currentTimeMillis(),
    val searchQuery: String = "",
    val searchError: String? = null,
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

// ══════════════════════════════════════════════════════════════════════════════
// EXTENDED EVENTS WITH SEARCH
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Extended TransactionsEvent with search functionality.
 */
sealed interface TransactionsEventExtended {
    data class SelectPreset(val index: Int) : TransactionsEventExtended
    data class SetDateRange(val from: Long, val to: Long) : TransactionsEventExtended
    data class SearchTransactions(val query: String) : TransactionsEventExtended
    data object AddTransactionClicked : TransactionsEventExtended
    data class DeleteTransaction(val transaction: Transaction) : TransactionsEventExtended
    data class UpdateTransaction(val transaction: Transaction) : TransactionsEventExtended
}

// ══════════════════════════════════════════════════════════════════════════════
// EXTENDED VIEWMODEL WITH SEARCH
// ══════════════════════════════════════════════════════════════════════════════

class TransactionsViewModelWithSearch(
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
    private val _filterState = MutableStateFlow(FilterStateExtended())

    // ── Combined UI State with filtering logic ──
    val state: StateFlow<TransactionsStateExtended> = combine(
        getTransactionsUseCase(),
        getCategoriesUseCase(),
        _filterState,
    ) { transactions, categories, filterState ->
        // Apply date range filter
        val dateFiltered = transactions.filter {
            it.timestamp in filterState.dateRangeFrom..filterState.dateRangeTo
        }

        // Apply search filter (by title or amount) if query is not empty
        val filtered = if (filterState.searchQuery.isBlank()) {
            dateFiltered
        } else {
            val query = filterState.searchQuery.trim().lowercase()
            dateFiltered.filter { transaction ->
                // Search by title
                val titleMatches = transaction.title.lowercase().contains(query)
                
                // Search by amount (handle various formats: "123", "123.45", etc.)
                val amountMatches = try {
                    val queryAmount = query.toDoubleOrNull()
                    if (queryAmount != null) {
                        abs(transaction.amount - queryAmount) < 0.01 // Allow small floating point difference
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    false
                }

                titleMatches || amountMatches
            }
        }

        TransactionsStateExtended(
            transactions = transactions,
            filteredTransactions = filtered,
            categories = categories,
            isLoading = false,
            selectedPresetIndex = filterState.selectedPresetIndex,
            dateRangeFrom = filterState.dateRangeFrom,
            dateRangeTo = filterState.dateRangeTo,
            searchQuery = filterState.searchQuery,
            searchError = validateSearchQuery(filterState.searchQuery),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionsStateExtended(isLoading = true),
    )

    // ── Event Handling ──
    fun onEvent(event: TransactionsEventExtended) {
        Logger.d("TransactionsViewModel") { "Event: $event" }
        when (event) {
            is TransactionsEventExtended.SelectPreset -> selectPreset(event.index)
            is TransactionsEventExtended.SetDateRange -> setDateRange(event.from, event.to)
            is TransactionsEventExtended.SearchTransactions -> updateSearch(event.query)
            is TransactionsEventExtended.AddTransactionClicked -> { /* Navigation handled by Screen */ }
            is TransactionsEventExtended.DeleteTransaction -> deleteTransaction(event.transaction)
            is TransactionsEventExtended.UpdateTransaction -> updateTransaction(event.transaction)
        }
    }

    // ── Private Methods ──
    private fun selectPreset(index: Int) {
        _filterState.update {
            it.copy(
                selectedPresetIndex = index,
                dateRangeFrom = TransactionsStateExtended.getDateFromForPreset(index),
                dateRangeTo = System.currentTimeMillis(),
            )
        }
    }

    private fun setDateRange(from: Long, to: Long) {
        _filterState.update { it.copy(dateRangeFrom = from, dateRangeTo = to) }
    }

    private fun updateSearch(query: String) {
        Logger.d("TransactionsViewModel") { "Search query: '$query'" }
        _filterState.update { it.copy(searchQuery = query) }
    }

    private fun validateSearchQuery(query: String): String? {
        return when {
            query.isBlank() -> null
            query.length > 100 -> "La ricerca è troppo lunga (max 100 caratteri)"
            else -> null
        }
    }

    fun addTransaction(
        title: String,
        amount: Double,
        category: String,
        type: TransactionType,
        timestamp: Long,
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
                ),
            )
        }
    }

    private fun updateTransaction(transaction: Transaction) {
        Logger.d("TransactionsViewModel") { "Updating transaction: ${transaction.title}" }
        viewModelScope.launch {
            updateTransactionUseCase(transaction)
        }
    }

    private fun deleteTransaction(transaction: Transaction) {
        Logger.d("TransactionsViewModel") { "Deleting transaction: ${transaction.title}" }
        viewModelScope.launch {
            deleteTransactionUseCase(transaction)
        }
    }
}

/**
 * Internal filter state to track user filter selections with search.
 */
private data class FilterStateExtended(
    val selectedPresetIndex: Int = 1,
    val dateRangeFrom: Long = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000),
    val dateRangeTo: Long = System.currentTimeMillis(),
    val searchQuery: String = "",
)

