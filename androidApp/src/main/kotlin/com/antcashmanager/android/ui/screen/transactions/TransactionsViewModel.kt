package com.antcashmanager.android.ui.screen.transactions

// ══════════════════════════════════════════════════════════════════════════════
// STATE
// ══════════════════════════════════════════════════════════════════════════════

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.antcashmanager.android.util.withCorrectAmounts
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.category.GetCategoriesUseCase
import com.antcashmanager.domain.usecase.transaction.DeleteTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.FilterTransactionsUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionsUseCase
import com.antcashmanager.domain.usecase.transaction.InsertTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.TransactionFilterParams
import com.antcashmanager.domain.usecase.transaction.UpdateTransactionUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
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
    // Date range events
    data class SelectPreset(val index: Int) : TransactionsEvent
    data class SetDateRange(val from: Long, val to: Long) : TransactionsEvent

    // Search & Filter events
    data class UpdateSearchQuery(val query: String) : TransactionsEvent
    data class UpdateCategoryFilter(val category: String?) : TransactionsEvent
    data class UpdateTransactionTypeFilter(val type: TransactionType?) : TransactionsEvent
    data class UpdatePaymentTypeFilter(val paymentType: PaymentType?) : TransactionsEvent
    data object ToggleSearchExpanded : TransactionsEvent
    data object ToggleFiltersExpanded : TransactionsEvent
    data object ApplyFilters : TransactionsEvent
    data object CancelFilterChanges : TransactionsEvent
    data object ClearAllFilters : TransactionsEvent

    // Transaction CRUD events
    data object AddTransactionClicked : TransactionsEvent
    data class DeleteTransaction(val transaction: Transaction) : TransactionsEvent
    data class UpdateTransaction(val transaction: Transaction) : TransactionsEvent
}

// ══════════════════════════════════════════════════════════════════════════════
// VIEWMODEL
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(FlowPreview::class)
class TransactionsViewModel(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    // ── UseCases ──
    private val getTransactionsUseCase = GetTransactionsUseCase(transactionRepository, dispatcher)
    private val insertTransactionUseCase = InsertTransactionUseCase(transactionRepository, dispatcher)
    private val updateTransactionUseCase = UpdateTransactionUseCase(transactionRepository, dispatcher)
    private val deleteTransactionUseCase = DeleteTransactionUseCase(transactionRepository, dispatcher)
    private val getCategoriesUseCase = GetCategoriesUseCase(categoryRepository, dispatcher)
    private val filterTransactionsUseCase = FilterTransactionsUseCase()

    // ── Internal filter state ──
    private val _filterState = MutableStateFlow(FilterState())

    // ── Search query with debounce for performance ──
    private val debouncedSearchQuery = _filterState
        .map { it.searchQuery }
        .distinctUntilChanged()
        .debounce(300L)

    // ── Combined UI State ──
    val state: StateFlow<TransactionsState> = combine(
        getTransactionsUseCase().map { it.getOrElse { emptyList() } },
        getCategoriesUseCase().map { it.getOrElse { emptyList() } },
        _filterState,
        debouncedSearchQuery,
    ) { transactions, categories, filterState, debouncedQuery ->

        // Apply amount correction for EXPENSE transactions
        val transformedTransactions = transactions.withCorrectAmounts()

        // Build filter params
        val filterParams = TransactionFilterParams(
            searchQuery = debouncedQuery,
            categoryName = filterState.selectedCategory,
            transactionType = filterState.selectedTransactionType,
            paymentType = filterState.selectedPaymentType,
            dateFrom = filterState.dateRangeFrom,
            dateTo = filterState.dateRangeTo,
        )

        // Filter transactions using UseCase (Result-wrapped)
        val filtered = run {
            val result = filterTransactionsUseCase(
                FilterTransactionsUseCase.Params(
                    transactions = transformedTransactions,
                    filterParams = filterParams,
                )
            )
            result.getOrElse { emptyList() }
        }

        TransactionsState(
            transactions = transformedTransactions,
            filteredTransactions = filtered,
            categories = categories,
            isLoading = false,
            selectedPresetIndex = filterState.selectedPresetIndex,
            dateRangeFrom = filterState.dateRangeFrom,
            dateRangeTo = filterState.dateRangeTo,
            searchQuery = filterState.searchQuery,
            selectedCategory = filterState.selectedCategory,
            selectedTransactionType = filterState.selectedTransactionType,
            selectedPaymentType = filterState.selectedPaymentType,
            pendingSearchQuery = filterState.pendingSearchQuery,
            pendingCategory = filterState.pendingCategory,
            pendingTransactionType = filterState.pendingTransactionType,
            pendingPaymentType = filterState.pendingPaymentType,
            isSearchExpanded = filterState.isSearchExpanded,
            isFiltersExpanded = filterState.isFiltersExpanded,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionsState(isLoading = true),
    )

    // ── Event Handling ──
    fun onEvent(event: TransactionsEvent) {
        Logger.d("TransactionsViewModel") { "Event: $event" }
        when (event) {
            // Date range events
            is TransactionsEvent.SelectPreset -> selectPreset(event.index)
            is TransactionsEvent.SetDateRange -> setDateRange(event.from, event.to)

            // Search & Filter events
            is TransactionsEvent.UpdateSearchQuery -> updateSearchQuery(event.query)
            is TransactionsEvent.UpdateCategoryFilter -> updateCategoryFilter(event.category)
            is TransactionsEvent.UpdateTransactionTypeFilter -> updateTransactionTypeFilter(event.type)
            is TransactionsEvent.UpdatePaymentTypeFilter -> updatePaymentTypeFilter(event.paymentType)
            is TransactionsEvent.ToggleSearchExpanded -> toggleSearchExpanded()
            is TransactionsEvent.ToggleFiltersExpanded -> toggleFiltersExpanded()
            is TransactionsEvent.ApplyFilters -> applyFilters()
            is TransactionsEvent.CancelFilterChanges -> cancelFilterChanges()
            is TransactionsEvent.ClearAllFilters -> clearAllFilters()

            // Transaction CRUD events
            is TransactionsEvent.AddTransactionClicked -> { /* Navigation handled by Screen */
            }

            is TransactionsEvent.DeleteTransaction -> deleteTransaction(event.transaction)
            is TransactionsEvent.UpdateTransaction -> updateTransaction(event.transaction)
        }
    }

    // ── Private Methods - Date Range ──
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

    // ── Private Methods - Search & Filters ──
    private fun updateSearchQuery(query: String) {
        _filterState.update {
            it.copy(
                pendingSearchQuery = query,
                // Search is applied immediately from the search bar
                searchQuery = query,
            )
        }
    }

    private fun updateCategoryFilter(category: String?) {
        _filterState.update { it.copy(pendingCategory = category) }
    }

    private fun updateTransactionTypeFilter(type: TransactionType?) {
        _filterState.update { it.copy(pendingTransactionType = type) }
    }

    private fun updatePaymentTypeFilter(paymentType: PaymentType?) {
        _filterState.update { it.copy(pendingPaymentType = paymentType) }
    }

    private fun toggleSearchExpanded() {
        _filterState.update { it.copy(isSearchExpanded = !it.isSearchExpanded) }
    }

    private fun toggleFiltersExpanded() {
        _filterState.update { it.copy(isFiltersExpanded = !it.isFiltersExpanded) }
    }

    private fun applyFilters() {
        _filterState.update {
            it.copy(
                searchQuery = it.pendingSearchQuery,
                selectedCategory = it.pendingCategory,
                selectedTransactionType = it.pendingTransactionType,
                selectedPaymentType = it.pendingPaymentType,
            )
        }
    }

    private fun cancelFilterChanges() {
        _filterState.update {
            it.copy(
                pendingSearchQuery = it.searchQuery,
                pendingCategory = it.selectedCategory,
                pendingTransactionType = it.selectedTransactionType,
                pendingPaymentType = it.selectedPaymentType,
            )
        }
    }

    private fun clearAllFilters() {
        _filterState.update {
            it.copy(
                searchQuery = "",
                selectedCategory = null,
                selectedTransactionType = null,
                selectedPaymentType = null,
                pendingSearchQuery = "",
                pendingCategory = null,
                pendingTransactionType = null,
                pendingPaymentType = null,
            )
        }
    }

    // ── Public Methods - Transaction CRUD ──
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
            val result = insertTransactionUseCase(
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
            result.onFailure { error ->
                if (error is kotlinx.coroutines.CancellationException) throw error
                Logger.e("TransactionsViewModel", error) { "Failed to insert transaction: ${error.message}" }
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        Logger.d("TransactionsViewModel") { "Updating transaction: ${transaction.title}" }
        viewModelScope.launch {
            val result = updateTransactionUseCase(transaction)
            result.onFailure { error ->
                if (error is kotlinx.coroutines.CancellationException) throw error
                Logger.e("TransactionsViewModel", error) { "Failed to update transaction: ${error.message}" }
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        Logger.d("TransactionsViewModel") { "Deleting transaction: ${transaction.title}" }
        viewModelScope.launch {
            val result = deleteTransactionUseCase(transaction)
            result.onFailure { error ->
                if (error is kotlinx.coroutines.CancellationException) throw error
                Logger.e("TransactionsViewModel", error) { "Failed to delete transaction: ${error.message}" }
            }
        }
    }
}

/**
 * Internal filter state to track user filter selections.
 */
private data class FilterState(
    // Date range
    val selectedPresetIndex: Int = 1,
    val dateRangeFrom: Long = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000),
    val dateRangeTo: Long = System.currentTimeMillis(),
    // Search & Filters - Active (confirmed)
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val selectedTransactionType: TransactionType? = null,
    val selectedPaymentType: PaymentType? = null,
    // Pending (temporary) filters - awaiting confirmation
    val pendingSearchQuery: String = "",
    val pendingCategory: String? = null,
    val pendingTransactionType: TransactionType? = null,
    val pendingPaymentType: PaymentType? = null,
    // UI state
    val isSearchExpanded: Boolean = false,
    val isFiltersExpanded: Boolean = false,
) {
    /**
     * Check if pending filters differ from confirmed filters
     */
    val hasFilterChanges: Boolean
        get() = pendingSearchQuery != searchQuery ||
                pendingCategory != selectedCategory ||
                pendingTransactionType != selectedTransactionType ||
                pendingPaymentType != selectedPaymentType

    /**
     * Get number of active (confirmed) filters
     */
    val activeFilterCount: Int
        get() {
            var count = 0
            if (searchQuery.isNotBlank()) count++
            if (selectedCategory != null) count++
            if (selectedTransactionType != null) count++
            if (selectedPaymentType != null) count++
            return count
        }

    /**
     * Get number of pending filters
     */
    val pendingFilterCount: Int
        get() {
            var count = 0
            if (pendingSearchQuery.isNotBlank()) count++
            if (pendingCategory != null) count++
            if (pendingTransactionType != null) count++
            if (pendingPaymentType != null) count++
            return count
        }
}
