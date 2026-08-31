package com.antcashmanager.android.ui.screen.transactions

// ══════════════════════════════════════════════════════════════════════════════
// STATE
// ══════════════════════════════════════════════════════════════════════════════

import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.analytics.EngagementTracker
import com.antcashmanager.android.ui.base.BaseViewModel
import com.antcashmanager.android.ui.screen.transactions.event.TransactionsEvent
import com.antcashmanager.android.util.withCorrectAmounts
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionSuggestions
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.category.GetCategoriesUseCase
import com.antcashmanager.domain.usecase.settings.GetTransactionsDateFilterStateUseCase
import com.antcashmanager.domain.usecase.settings.SetTransactionsDateFilterStateUseCase
import com.antcashmanager.domain.usecase.transaction.DeleteTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.FilterTransactionsUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionSuggestionsUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionsUseCase
import com.antcashmanager.domain.usecase.transaction.InsertTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.TransactionFilterParams
import com.antcashmanager.domain.usecase.transaction.UpdateTransactionUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


// ══════════════════════════════════════════════════════════════════════════════
// VIEWMODEL
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class TransactionsViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val insertTransactionUseCase: InsertTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val filterTransactionsUseCase: FilterTransactionsUseCase,
    private val getTransactionSuggestionsUseCase: GetTransactionSuggestionsUseCase,
    private val getTransactionsDateFilterStateUseCase: GetTransactionsDateFilterStateUseCase,
    private val setTransactionsDateFilterStateUseCase: SetTransactionsDateFilterStateUseCase,
    private val settingsRepository: SettingsRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val engagementTracker: EngagementTracker,
) : BaseViewModel<TransactionsEvent>(dispatcher) {

    constructor(
        transactionRepository: TransactionRepository,
        categoryRepository: CategoryRepository,
        settingsRepository: SettingsRepository,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        engagementTracker: EngagementTracker,
    ) : this(
        getTransactionsUseCase = GetTransactionsUseCase(
            transactionRepository = transactionRepository,
            dispatcher = dispatcher,
        ),
        insertTransactionUseCase = InsertTransactionUseCase(
            transactionRepository = transactionRepository,
            dispatcher = dispatcher,
        ),
        updateTransactionUseCase = UpdateTransactionUseCase(
            transactionRepository = transactionRepository,
            dispatcher = dispatcher,
        ),
        deleteTransactionUseCase = DeleteTransactionUseCase(
            transactionRepository = transactionRepository,
            dispatcher = dispatcher,
        ),
        getCategoriesUseCase = GetCategoriesUseCase(
            categoryRepository = categoryRepository,
            dispatcher = dispatcher,
        ),
        filterTransactionsUseCase = FilterTransactionsUseCase(
            dispatcher = dispatcher,
        ),
        getTransactionSuggestionsUseCase = GetTransactionSuggestionsUseCase(
            repository = transactionRepository,
            settingsRepository = settingsRepository,
            dispatcher = dispatcher,
        ),
        getTransactionsDateFilterStateUseCase = GetTransactionsDateFilterStateUseCase(
            settingsRepository = settingsRepository,
            dispatcher = dispatcher,
        ),
        setTransactionsDateFilterStateUseCase = SetTransactionsDateFilterStateUseCase(
            settingsRepository = settingsRepository,
            dispatcher = dispatcher,
        ),
        settingsRepository = settingsRepository,
        dispatcher = dispatcher,
        engagementTracker = engagementTracker,
    )


    // ── Internal filter state ──
    private val _filterState = MutableStateFlow(FilterState())

    // ── Transactions & Categories flows ──
    private val transactionsFlow = getTransactionsUseCase()
        .map { it.getOrElse { emptyList() }.withCorrectAmounts() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val categoriesFlow = getCategoriesUseCase()
        .map { it.getOrElse { emptyList() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val searchQueryFlow = _filterState
        .map { it.searchQuery }
        .distinctUntilChanged()

    // ── Filtered Transactions Flow (updates immediately on search and filter changes) ──
    // Per i preset relativi (non-custom), "from" e "to" vengono ricalcolati dinamicamente
    // ogni volta che il combine viene rieseguito (es. quando Room emette dopo un insert).
    // Questo garantisce che le transazioni appena inserite siano immediatamente visibili
    // senza bisogno di aggiornare manualmente il filtro.
    private val filteredTransactionsFlow = combine(
        transactionsFlow,
        searchQueryFlow,
        _filterState.map { it.selectedCategory }.distinctUntilChanged(),
        _filterState.map { it.selectedTransactionType }.distinctUntilChanged(),
        _filterState.map { it.selectedPaymentType }.distinctUntilChanged(),
        _filterState.map { it.dateRangeFrom }.distinctUntilChanged(),
        _filterState.map { it.dateRangeTo }.distinctUntilChanged(),
        _filterState.map { it.selectedPresetIndex }.distinctUntilChanged(),
    ) { args: Array<Any?> ->
        val transactions = args[0] as List<Transaction>
        val query = args[1] as String
        val category = args[2] as String?
        val type = args[3] as TransactionType?
        val payment = args[4] as PaymentType?
        val storedFrom = args[5] as Long
        val storedTo = args[6] as Long
        val presetIndex = args[7] as Int

        // Per i preset relativi, ricalcola i bound dinamicamente al momento dell'esecuzione.
        // Il combine si riesegue ogni volta che transactionsFlow emette (Room insert/update),
        // garantendo che il range sia sempre corretto per i preset relativi.
        val from = if (presetIndex != SavedDateFilter.CUSTOM_PRESET_INDEX) {
            TransactionsState.getDateFromForPreset(presetIndex)
        } else {
            storedFrom
        }
        val to = if (presetIndex != SavedDateFilter.CUSTOM_PRESET_INDEX) {
            // End of current day (23:59:59.999)
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calendar.set(java.util.Calendar.MINUTE, 59)
            calendar.set(java.util.Calendar.SECOND, 59)
            calendar.set(java.util.Calendar.MILLISECOND, 999)
            calendar.timeInMillis
        } else {
            storedTo
        }

        val filterParams = TransactionFilterParams(
            searchQuery = query,
            categoryName = category,
            transactionType = type,
            paymentType = payment,
            dateFrom = from,
            dateTo = to,
        )
        filterTransactionsUseCase(
            FilterTransactionsUseCase.Params(transactions, filterParams)
        ).getOrElse { emptyList() }
    }

    // ── Search Suggestions Flow (updates immediately on typing) ──
    private val searchSuggestionsFlow = _filterState
        .map { it.searchQuery }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList<String>())
            } else {
                combine(
                    transactionsFlow,
                    getTransactionSuggestionsUseCase().map { it.getOrDefault(TransactionSuggestions()) }
                ) { transactions, suggestions ->
                    val matchingFromHistory = transactions
                        .asSequence()
                        .map { it.title }
                        .filter {
                            it.contains(query, ignoreCase = true) && !it.equals(
                                query,
                                ignoreCase = true
                            )
                        }
                        .distinct()
                        .take(3)
                        .toList()

                    val matchingFromSuggestions = suggestions.titles
                        .filter {
                            it.contains(query, ignoreCase = true) &&
                                    !it.equals(query, ignoreCase = true) &&
                                    it !in matchingFromHistory
                        }
                        .take(3)

                    (matchingFromHistory + matchingFromSuggestions).distinct()
                }
            }
        }

    // ── Combined UI State ──
    val state: StateFlow<TransactionsState> = combine(
        transactionsFlow,
        categoriesFlow,
        filteredTransactionsFlow,
        searchSuggestionsFlow,
        _filterState,
        settingsRepository.getTransactionDisplayType(),
    ) { transactions, categories, filtered, suggestions, filterState, displayType ->
        val categoryCache = categories.associateBy { it.name }

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

        TransactionsState(
            transactions = transactions,
            filteredTransactions = enrichedFiltered,
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
            searchSuggestions = suggestions,
            transactionDisplayType = displayType,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionsState(isLoading = true),
    )

    init {
        observeSavedDateFilter()
    }

    // ── Event Handling ──
    override fun onEvent(event: TransactionsEvent) {
        logDebug("Event: $event")
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
            is TransactionsEvent.SetDateFilterExpanded -> setDateFilterExpanded(event.expanded)

            // Transaction CRUD events
            is TransactionsEvent.AddTransactionClicked -> { /* Navigation handled by Screen */
            }

            is TransactionsEvent.DeleteTransaction -> deleteTransaction(event.transaction)
            is TransactionsEvent.UpdateTransaction -> updateTransaction(event.transaction)
        }
    }

    // ── Private Methods - Date Range ──
    private fun observeSavedDateFilter() {
        viewModelScope.launch {
            getTransactionsDateFilterStateUseCase()
                .map { result: Result<SavedDateFilter> -> result.getOrNull() }
                .filterNotNull()
                .collect { savedFilter: SavedDateFilter ->
                    // Per i preset relativi (non-custom) ricalcola sempre i bound in modo
                    // dinamico: "to" deve essere "adesso" per includere le transazioni appena
                    // inserite, "from" deve retrocedere dal momento corrente.
                    // Per il range personalizzato si rispettano le date fisse salvate.
                    val (from, to) = if (savedFilter.isCustom) {
                        savedFilter.from to savedFilter.to
                    } else {
                        TransactionsState.getDateFromForPreset(savedFilter.presetIndex) to
                                System.currentTimeMillis()
                    }
                    _filterState.update {
                        it.copy(
                            selectedPresetIndex = savedFilter.presetIndex,
                            dateRangeFrom = from,
                            dateRangeTo = to,
                        )
                    }
                }
        }
    }

    private fun selectPreset(index: Int) {
        val dateRangeTo = System.currentTimeMillis()
        val dateRangeFrom = TransactionsState.getDateFromForPreset(index)
        _filterState.update {
            it.copy(
                selectedPresetIndex = index,
                dateRangeFrom = dateRangeFrom,
                dateRangeTo = dateRangeTo,
            )
        }
        persistDateFilter(
            SavedDateFilter(
                presetIndex = index,
                from = dateRangeFrom,
                to = dateRangeTo,
            ),
        )
    }

    private fun setDateRange(from: Long, to: Long) {
        val normalizedFrom = minOf(from, to)
        val normalizedTo = maxOf(from, to)
        _filterState.update {
            it.copy(
                selectedPresetIndex = SavedDateFilter.CUSTOM_PRESET_INDEX,
                dateRangeFrom = normalizedFrom,
                dateRangeTo = normalizedTo,
            )
        }
        persistDateFilter(
            SavedDateFilter(
                presetIndex = SavedDateFilter.CUSTOM_PRESET_INDEX,
                from = normalizedFrom,
                to = normalizedTo,
            ),
        )
    }

    private fun persistDateFilter(filter: SavedDateFilter) {
        viewModelScope.launch {
            val result = setTransactionsDateFilterStateUseCase(filter)
            result.onFailure { error ->
                if (error is kotlinx.coroutines.CancellationException) throw error
                logError(
                    message = "Failed to persist transactions date filter: ${error.message}",
                    throwable = error
                )
            }
        }
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

        // Track search effectiveness when query is entered
        if (query.isNotEmpty()) {
            val resultsCount = state.value.filteredTransactions.size
            engagementTracker.trackTransactionSearchEffectiveness(
                queryLength = query.length,
                resultsCount = resultsCount,
                resultClicked = false
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

    private fun setDateFilterExpanded(expanded: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDateFilterExpanded(expanded)
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
        logDebug("Adding transaction: $title")
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
                logError(
                    message = "Failed to insert transaction: ${error.message}",
                    throwable = error
                )
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        logDebug("Updating transaction: ${transaction.title}")
        viewModelScope.launch {
            val result = updateTransactionUseCase(transaction)
            result.onFailure { error ->
                if (error is kotlinx.coroutines.CancellationException) throw error
                logError(
                    message = "Failed to update transaction: ${error.message}",
                    throwable = error
                )
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        logDebug("Deleting transaction: ${transaction.title}")
        viewModelScope.launch {
            val result = deleteTransactionUseCase(transaction)
            result.onFailure { error ->
                if (error is kotlinx.coroutines.CancellationException) throw error
                logError(
                    message = "Failed to delete transaction: ${error.message}",
                    throwable = error
                )
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

}
