package com.antcashmanager.android.ui.screen.home

import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.ui.base.BaseViewModel
import com.antcashmanager.android.util.calculateBalance
import com.antcashmanager.android.util.calculateTotalExpense
import com.antcashmanager.android.util.calculateTotalIncome
import com.antcashmanager.android.util.withCorrectAmounts
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.TransactionSuggestions
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.category.GetCategoriesUseCase
import com.antcashmanager.domain.usecase.settings.GetHomeDateFilterStateUseCase
import com.antcashmanager.domain.usecase.settings.SetHomeDateFilterStateUseCase
import com.antcashmanager.domain.usecase.transaction.FilterTransactionsUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionSuggestionsUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionsUseCase
import com.antcashmanager.domain.usecase.transaction.TransactionFilterParams
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    // Search events
    data class UpdateSearchQuery(val query: String) : HomeEvent
    data object ToggleSearchExpanded : HomeEvent
}

// ══════════════════════════════════════════════════════════════════════════════
// VIEWMODEL
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val filterTransactionsUseCase: FilterTransactionsUseCase,
    private val getTransactionSuggestionsUseCase: GetTransactionSuggestionsUseCase,
    private val getHomeDateFilterStateUseCase: GetHomeDateFilterStateUseCase,
    private val setHomeDateFilterStateUseCase: SetHomeDateFilterStateUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    searchDebounceMs: Long = 300L,
) : BaseViewModel() {

    constructor(
        transactionRepository: TransactionRepository,
        settingsRepository: SettingsRepository,
        categoryRepository: CategoryRepository,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        searchDebounceMs: Long = 300L,
    ) : this(
        getTransactionsUseCase = GetTransactionsUseCase(
            transactionRepository = transactionRepository,
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
        getHomeDateFilterStateUseCase = GetHomeDateFilterStateUseCase(
            settingsRepository = settingsRepository,
            dispatcher = dispatcher,
        ),
        setHomeDateFilterStateUseCase = SetHomeDateFilterStateUseCase(
            settingsRepository = settingsRepository,
            dispatcher = dispatcher,
        ),
        getCategoriesUseCase = GetCategoriesUseCase(
            categoryRepository = categoryRepository,
            dispatcher = dispatcher,
        ),
        searchDebounceMs = searchDebounceMs,
    )

    // ── Categories cache for enriching transactions ──
    private val categoriesCache: StateFlow<Map<String, com.antcashmanager.domain.model.Category>> =
        getCategoriesUseCase()
            .map { result -> result.getOrElse { emptyList() }.associateBy { it.name } }
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

    // ── Transactions Flow ──
    private val transactionsFlow = getTransactionsUseCase()
        .map { it.getOrElse { emptyList() }.withCorrectAmounts() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ── Search query with debounce for performance ──
    private val debouncedSearchQuery = _filterState
        .map { it.searchQuery }
        .distinctUntilChanged()
        .debounce(searchDebounceMs)

    // ── Filtered Transactions Flow ──
    // Per i preset relativi (non-custom), "from" e "to" vengono ricalcolati dinamicamente
    // ogni volta che il combine viene rieseguito (es. quando Room emette dopo un insert).
    // Questo garantisce che le transazioni appena inserite siano immediatamente visibili
    // senza bisogno di aggiornare manualmente il filtro.
    private val filteredTransactionsFlow = combine(
        transactionsFlow,
        debouncedSearchQuery,
        _filterState.map { it.dateRangeFrom }.distinctUntilChanged(),
        _filterState.map { it.dateRangeTo }.distinctUntilChanged(),
        _filterState.map { it.selectedPresetIndex }.distinctUntilChanged(),
    ) { transactions, query, storedFrom, storedTo, presetIndex ->
        // Per i preset relativi, ricalcola i bound dinamicamente al momento dell'esecuzione.
        val from = if (presetIndex != SavedDateFilter.CUSTOM_PRESET_INDEX) {
            HomeState.getDateFromForPreset(presetIndex)
        } else {
            storedFrom
        }
        val to = if (presetIndex != SavedDateFilter.CUSTOM_PRESET_INDEX) {
            // End of current day (start of next day at 00:00:00)
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
            dateFrom = from,
            dateTo = to,
        )
        filterTransactionsUseCase(
            FilterTransactionsUseCase.Params(transactions, filterParams)
        ).getOrElse { emptyList() }
    }

    // ── Search Suggestions Flow ──
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
                            it.contains(query, ignoreCase = true) &&
                                    !it.equals(query, ignoreCase = true)
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
    val state: StateFlow<HomeState> = combine(
        transactionsFlow,
        filteredTransactionsFlow,
        searchSuggestionsFlow,
        _filterState,
        _selectedTransactionState,
        categoriesCache,
    ) { args: Array<Any?> ->
        val transactions = args[0] as List<com.antcashmanager.domain.model.Transaction>
        val filtered = args[1] as List<com.antcashmanager.domain.model.Transaction>
        val suggestions = args[2] as List<String>
        val filterState = args[3] as FilterState
        val selectedTransaction = args[4] as com.antcashmanager.domain.model.Transaction?
        val categoryCache = args[5] as Map<String, com.antcashmanager.domain.model.Category>

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

        val totalIncome = enrichedFiltered.calculateTotalIncome()
        val totalExpense = enrichedFiltered.calculateTotalExpense() // This will be negative
        val balance = enrichedFiltered.calculateBalance() // Sum of all amounts

        // Calculate balance by payment type (optimized with Map, excluding zeros)
        val balanceByPaymentType = enrichedFiltered
            .groupBy { it.paymentType }
            .mapValues { (_, txs) -> txs.sumOf { it.amount } }
            .filterValues { it != 0.0 }

        HomeState(
            transactions = transactions,
            filteredTransactions = enrichedFiltered,
            recentTransactions = enrichedFiltered,
            totalIncome = totalIncome,
            totalExpense = totalExpense, // Will be negative for display
            balance = balance,
            balanceByPaymentType = balanceByPaymentType,
            isLoading = false,
            selectedPresetIndex = filterState.selectedPresetIndex,
            dateRangeFrom = filterState.dateRangeFrom,
            dateRangeTo = filterState.dateRangeTo,
            selectedTransaction = selectedTransaction,
            searchQuery = filterState.searchQuery,
            isSearchExpanded = filterState.isSearchExpanded,
            searchSuggestions = suggestions,
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
        observeSavedDateFilter()
        logDebug("HomeViewModel initialized")
    }

    // ── Event Handling ──
    fun onEvent(event: HomeEvent) {
        logDebug("Event: $event")
        when (event) {
            is HomeEvent.SelectPreset -> selectPreset(event.index)
            is HomeEvent.SetDateRange -> setDateRange(event.from, event.to)
            is HomeEvent.ShowTransactionDetails -> _selectedTransactionState.value =
                event.transaction

            HomeEvent.DismissTransactionDetails -> _selectedTransactionState.value = null
            is HomeEvent.UpdateSearchQuery -> updateSearchQuery(event.query)
            HomeEvent.ToggleSearchExpanded -> toggleSearchExpanded()
        }
    }

    private fun updateSearchQuery(query: String) {
        _filterState.update { it.copy(searchQuery = query) }
    }

    private fun toggleSearchExpanded() {
        _filterState.update { it.copy(isSearchExpanded = !it.isSearchExpanded) }
    }

    private fun observeSavedDateFilter() {
        viewModelScope.launch {
            getHomeDateFilterStateUseCase()
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
                        HomeState.getDateFromForPreset(savedFilter.presetIndex) to
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
        val dateRangeFrom = HomeState.getDateFromForPreset(index)
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
            val result = setHomeDateFilterStateUseCase(filter)
            result.onFailure { error ->
                if (error is kotlinx.coroutines.CancellationException) throw error
                logError("Failed to persist home date filter: ${error.message}", throwable = error)
            }
        }
    }
}

/**
 * Internal filter state.
 */
private data class FilterState(
    val selectedPresetIndex: Int = 1,
    val dateRangeFrom: Long = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000),
    val dateRangeTo: Long = System.currentTimeMillis(),
    val searchQuery: String = "",
    val isSearchExpanded: Boolean = false,
)
