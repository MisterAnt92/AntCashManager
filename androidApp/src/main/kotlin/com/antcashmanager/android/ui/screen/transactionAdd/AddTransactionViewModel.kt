package com.antcashmanager.android.ui.screen.transactionAdd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.category.GetCategoriesUseCase
import com.antcashmanager.domain.usecase.transaction.DeleteTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionSuggestionsUseCase
import com.antcashmanager.domain.usecase.transaction.InsertTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.UpdateTransactionUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ══════════════════════════════════════════════════════════════════════════════
// EVENTS
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Events per la schermata di aggiunta/modifica transazione.
 * Flusso semplificato: Categoria → Dettagli (con salvataggio diretto).
 */
sealed interface AddTransactionEvent {
    // ── Selezione ──
    data class SelectCategory(val category: Category) : AddTransactionEvent
    data class SelectType(val type: TransactionType) : AddTransactionEvent
    data class SelectPaymentType(val paymentType: PaymentType) : AddTransactionEvent

    // ── Modifica campi ──
    data class UpdateTitle(val title: String) : AddTransactionEvent
    data class UpdateAmount(val amount: String) : AddTransactionEvent
    data class UpdateNotes(val notes: String) : AddTransactionEvent
    data class UpdatePayee(val payee: String) : AddTransactionEvent
    data class UpdateLocation(val location: String) : AddTransactionEvent
    data class UpdateTags(val tags: String) : AddTransactionEvent
    data class UpdateTimestamp(val timestamp: Long) : AddTransactionEvent
    data class SetRecurring(val isRecurring: Boolean) : AddTransactionEvent
    data class UpdateRecurrenceInterval(val interval: String) : AddTransactionEvent
    data class UpdateMealVoucherCount(val count: String) : AddTransactionEvent

    // ── Navigazione ──
    data object NextStep : AddTransactionEvent
    data object PreviousStep : AddTransactionEvent
    data object Submit : AddTransactionEvent
    data object Cancel : AddTransactionEvent

    // ── Dialog inline per modifica rapida ──
    data object EditCategory : AddTransactionEvent
    data object EditType : AddTransactionEvent
    data object EditDate : AddTransactionEvent
    data object EditPaymentType : AddTransactionEvent
    data object ShowCategoryDialog : AddTransactionEvent
    data object ShowTypeDialog : AddTransactionEvent
    data object ShowPaymentTypeDialog : AddTransactionEvent
    data object DismissCategoryDialog : AddTransactionEvent
    data object DismissTypeDialog : AddTransactionEvent
    data object DismissPaymentTypeDialog : AddTransactionEvent
    data object DismissDatePicker : AddTransactionEvent

    // ── Dialog eliminazione ──
    data object ShowDeleteConfirmDialog : AddTransactionEvent
    data object DismissDeleteConfirmDialog : AddTransactionEvent
    data object ConfirmDelete : AddTransactionEvent
}

// ══════════════════════════════════════════════════════════════════════════════
// VIEWMODEL
// ══════════════════════════════════════════════════════════════════════════════

class AddTransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val settingsRepository: com.antcashmanager.domain.repository.SettingsRepository,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val insertTransactionUseCase: InsertTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val getTransactionSuggestionsUseCase: GetTransactionSuggestionsUseCase,
    private val transactionId: Long? = null,
) : ViewModel() {

    constructor(
        transactionRepository: TransactionRepository,
        categoryRepository: CategoryRepository,
        settingsRepository: com.antcashmanager.domain.repository.SettingsRepository,
        transactionId: Long? = null,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
    ) : this(
        transactionRepository = transactionRepository,
        settingsRepository = settingsRepository,
        getCategoriesUseCase = GetCategoriesUseCase(categoryRepository, dispatcher),
        insertTransactionUseCase = InsertTransactionUseCase(transactionRepository, dispatcher),
        updateTransactionUseCase = UpdateTransactionUseCase(transactionRepository, dispatcher),
        deleteTransactionUseCase = DeleteTransactionUseCase(transactionRepository, dispatcher),
        getTransactionSuggestionsUseCase = GetTransactionSuggestionsUseCase(
            transactionRepository,
            settingsRepository,
            dispatcher,
        ),
        transactionId = transactionId,
    )


    // ── State ──
    private val _state = MutableStateFlow(AddTransactionState())
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()

    init {
        loadCategories()
        loadTransactionSuggestions()
        loadMealVoucherValue()
        if (transactionId != null) {
            loadTransactionForEdit(transactionId)
        }
    }

    private fun loadMealVoucherValue() {
        viewModelScope.launch {
            try {
                val mealVoucherValue = settingsRepository.getMealVoucherValue().first()
                _state.update { it.copy(mealVoucherValue = mealVoucherValue) }
            } catch (ex: Exception) {
                Logger.e(AddTransactionConstant.TAG) {
                    "Error loading meal voucher value: ${ex.message}"
                }
                // Keep default value
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase().collect { result ->
                result.onSuccess { categories ->
                    _state.update { it.copy(categories = categories) }
                }.onFailure { error ->
                    if (error is CancellationException) throw error
                    Logger.e(AddTransactionConstant.TAG, error) {
                        "Error loading categories: ${error.message}"
                    }
                    _state.update { it.copy(error = AddTransactionConstant.ERROR_LOAD_CATEGORIES) }
                }
            }
        }
    }

    private fun loadTransactionSuggestions() {
        Logger.d(AddTransactionConstant.TAG) { "Loading transaction suggestions" }
        viewModelScope.launch {
            getTransactionSuggestionsUseCase().collect { suggestions ->
                Logger.d(AddTransactionConstant.TAG) {
                    "Suggestions loaded - titles: ${suggestions.titles.size}, " +
                            "payees: ${suggestions.payees.size}, " +
                            "notes: ${suggestions.notes.size}, " +
                            "locations: ${suggestions.locations.size}, " +
                            "tags: ${suggestions.tags.size}"
                }
                _state.update {
                    it.copy(
                        titleSuggestions = suggestions.titles,
                        payeeSuggestions = suggestions.payees,
                        notesSuggestions = suggestions.notes,
                        locationSuggestions = suggestions.locations,
                        tagsSuggestions = suggestions.tags,
                    )
                }
            }
        }
    }

    private fun loadTransactionForEdit(id: Long) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                Logger.d(AddTransactionConstant.TAG) { "Loading transaction with id: $id" }

                val transaction = transactionRepository.getTransactionById(id)
                val categoryResult = getCategoriesUseCase().first()
                val categoryList = try {
                    categoryResult.getOrThrow()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Logger.e(AddTransactionConstant.TAG) {
                        "Error loading categories for edit: ${e.message}"
                    }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = AddTransactionConstant.ERROR_LOAD_CATEGORIES,
                        )
                    }
                    emptyList()
                }

                if (transaction != null) {
                    val selectedCat = categoryList.find { it.name == transaction.category }
                    Logger.d(AddTransactionConstant.TAG) {
                        "Transaction loaded: ${transaction.title}, category: $selectedCat"
                    }

                    _state.update {
                        it.copy(
                            isModifying = true,
                            transactionId = id,
                            selectedCategory = selectedCat,
                            selectedType = transaction.type,
                            title = transaction.title,
                            amount = kotlin.math.abs(transaction.amount).toString(),
                            notes = transaction.notes,
                            payee = transaction.payee,
                            location = transaction.location,
                            tags = transaction.tags,
                            timestamp = transaction.timestamp,
                            isRecurring = transaction.isRecurring,
                            recurrenceInterval = transaction.recurrenceInterval,
                            selectedPaymentType = transaction.paymentType,
                            mealVoucherCount = transaction.mealVoucherCount.toString(),
                            currentStep = AddTransactionStep.DETAILS,
                            isLoading = false,
                            categories = categoryList,
                        )
                    }
                } else {
                    Logger.w(AddTransactionConstant.TAG) { "Transaction with id $id not found" }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = AddTransactionConstant.ERROR_TRANSACTION_NOT_FOUND,
                        )
                    }
                }
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                Logger.e(AddTransactionConstant.TAG) { "Error loading transaction: ${ex.message}" }
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = AddTransactionConstant.ERROR_LOAD_TRANSACTION,
                    )
                }
            }
        }
    }

    /**
     * Gestisce gli eventi della UI.
     */
    fun onEvent(event: AddTransactionEvent) {
        Logger.d(AddTransactionConstant.TAG) { "Event: $event" }
        when (event) {
            is AddTransactionEvent.SelectCategory -> selectCategory(event.category)
            is AddTransactionEvent.SelectType -> selectType(event.type)
            is AddTransactionEvent.SelectPaymentType -> selectPaymentType(event.paymentType)
            is AddTransactionEvent.UpdateTitle -> _state.update { it.copy(title = event.title) }
            is AddTransactionEvent.UpdateAmount -> {
                // Permetti input libero durante la digitazione, validazione al salvataggio
                _state.update { it.copy(amount = event.amount) }
            }

            is AddTransactionEvent.UpdateNotes -> _state.update { it.copy(notes = event.notes) }
            is AddTransactionEvent.UpdatePayee -> _state.update { it.copy(payee = event.payee) }
            is AddTransactionEvent.UpdateLocation -> _state.update { it.copy(location = event.location) }
            is AddTransactionEvent.UpdateTags -> _state.update { it.copy(tags = event.tags) }
            is AddTransactionEvent.UpdateMealVoucherCount -> {
                _state.update { it.copy(mealVoucherCount = event.count) }
            }
            is AddTransactionEvent.UpdateTimestamp -> _state.update { it.copy(timestamp = event.timestamp) }
            is AddTransactionEvent.SetRecurring -> _state.update { it.copy(isRecurring = event.isRecurring) }
            is AddTransactionEvent.UpdateRecurrenceInterval -> _state.update {
                it.copy(
                    recurrenceInterval = event.interval
                )
            }

            is AddTransactionEvent.NextStep -> nextStep()
            is AddTransactionEvent.PreviousStep -> previousStep()
            is AddTransactionEvent.EditCategory -> _state.update { it.copy(showCategoryDialog = true) }
            is AddTransactionEvent.EditType -> _state.update { it.copy(showTypeDialog = true) }
            is AddTransactionEvent.EditDate -> _state.update { it.copy(showDatePicker = true) }
            is AddTransactionEvent.EditPaymentType -> _state.update { it.copy(showPaymentTypeDialog = true) }
            is AddTransactionEvent.ShowCategoryDialog -> _state.update { it.copy(showCategoryDialog = true) }
            is AddTransactionEvent.ShowTypeDialog -> _state.update { it.copy(showTypeDialog = true) }
            is AddTransactionEvent.ShowPaymentTypeDialog -> _state.update {
                it.copy(
                    showPaymentTypeDialog = true
                )
            }

            is AddTransactionEvent.DismissCategoryDialog -> _state.update {
                it.copy(
                    showCategoryDialog = false
                )
            }

            is AddTransactionEvent.DismissTypeDialog -> _state.update { it.copy(showTypeDialog = false) }
            is AddTransactionEvent.DismissPaymentTypeDialog -> _state.update {
                it.copy(
                    showPaymentTypeDialog = false
                )
            }

            is AddTransactionEvent.DismissDatePicker -> _state.update { it.copy(showDatePicker = false) }
            is AddTransactionEvent.Submit -> submitTransaction()
            is AddTransactionEvent.Cancel -> _state.value = AddTransactionState()
            is AddTransactionEvent.ShowDeleteConfirmDialog -> _state.update {
                it.copy(
                    showDeleteConfirmDialog = true
                )
            }

            is AddTransactionEvent.DismissDeleteConfirmDialog -> _state.update {
                it.copy(
                    showDeleteConfirmDialog = false
                )
            }

            is AddTransactionEvent.ConfirmDelete -> deleteTransaction()
        }
    }

    // ── Event Handlers ──

    private fun selectCategory(category: Category) {
        Logger.d(AddTransactionConstant.TAG) {
            "Category selected: ${category.name}, type: ${category.type}"
        }

        val transactionType = if (category.type.uppercase() == "INCOME")
            TransactionType.INCOME
        else
            TransactionType.EXPENSE

        val currentStep = _state.value.currentStep
        val isModifying = _state.value.isModifying

        _state.update {
            it.copy(
                selectedCategory = category,
                selectedType = transactionType,
                showCategoryDialog = false,
            )
        }

        // In creazione, al passo CATEGORY_SELECTION → avanza direttamente a DETAILS
        if (!isModifying && currentStep == AddTransactionStep.CATEGORY_SELECTION) {
            Logger.d(AddTransactionConstant.TAG) { "Auto-advancing to DETAILS after category selection" }
            _state.update { it.copy(currentStep = AddTransactionStep.DETAILS) }
        }
    }

    private fun selectType(type: TransactionType) {
        Logger.d(AddTransactionConstant.TAG) { "Type selected: $type" }
        _state.update {
            it.copy(selectedType = type, showTypeDialog = false)
        }
    }

    private fun selectPaymentType(paymentType: PaymentType) {
        Logger.d(AddTransactionConstant.TAG) { "Payment type selected: $paymentType" }
        _state.update {
            it.copy(selectedPaymentType = paymentType, showPaymentTypeDialog = false)
        }
    }

    private fun nextStep() {
        val nextStep = when (_state.value.currentStep) {
            AddTransactionStep.CATEGORY_SELECTION -> AddTransactionStep.DETAILS
            AddTransactionStep.DETAILS -> return // Salvataggio diretto dal form
        }
        Logger.d(AddTransactionConstant.TAG) { "Moving to next step: $nextStep" }
        _state.update { it.copy(currentStep = nextStep) }
    }

    private fun previousStep() {
        val prev = when (_state.value.currentStep) {
            AddTransactionStep.CATEGORY_SELECTION -> return
            AddTransactionStep.DETAILS ->
                if (_state.value.isModifying) return
                else AddTransactionStep.CATEGORY_SELECTION
        }
        Logger.d(AddTransactionConstant.TAG) { "Moving to previous step: $prev" }
        _state.update { it.copy(currentStep = prev) }
    }

    private fun submitTransaction() {
        val currentState = _state.value

        if (currentState.selectedCategory == null || currentState.selectedType == null) {
            _state.update { it.copy(error = AddTransactionConstant.ERROR_REQUIRED_CATEGORY_TYPE) }
            return
        }
        if (currentState.title.isBlank() || currentState.amount.isBlank()) {
            _state.update { it.copy(error = AddTransactionConstant.ERROR_REQUIRED_TITLE_AMOUNT) }
            return
        }
        val amount = currentState.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _state.update { it.copy(error = AddTransactionConstant.ERROR_INVALID_AMOUNT) }
            return
        }

        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                val transaction = Transaction(
                    id = if (currentState.isModifying) transactionId ?: 0 else 0,
                    title = currentState.title,
                    amount = currentState.totalAmount,
                    category = currentState.selectedCategory.name,
                    type = currentState.selectedType,
                    timestamp = currentState.timestamp,
                    notes = currentState.notes,
                    payee = currentState.payee,
                    location = currentState.location,
                    tags = currentState.tags,
                    isRecurring = currentState.isRecurring,
                    recurrenceInterval = currentState.recurrenceInterval,
                    paymentType = currentState.selectedPaymentType,
                    mealVoucherCount = currentState.mealVoucherCount.toIntOrNull() ?: 0,
                    categoryIcon = currentState.selectedCategory.icon,
                    categoryColor = currentState.selectedCategory.color,
                )

                if (currentState.isModifying) {
                    val result = updateTransactionUseCase(transaction)
                    result.onSuccess {
                        Logger.d(AddTransactionConstant.TAG) { "Transaction updated successfully" }
                        _state.update { it.copy(isTransactionSaved = true, isLoading = false) }
                    }.onFailure { error ->
                        if (error is CancellationException) throw error
                        Logger.e(AddTransactionConstant.TAG, error) { "Error updating transaction" }
                        _state.update {
                            it.copy(
                                error = AddTransactionConstant.ERROR_SAVE,
                                isLoading = false
                            )
                        }
                    }
                } else {
                    val result = insertTransactionUseCase(transaction)
                    result.onSuccess {
                        Logger.d(AddTransactionConstant.TAG) { "Transaction inserted successfully" }
                        _state.update { it.copy(isTransactionSaved = true, isLoading = false) }
                    }.onFailure { error ->
                        if (error is CancellationException) throw error
                        Logger.e(AddTransactionConstant.TAG, error) { "Error inserting transaction" }
                        _state.update {
                            it.copy(
                                error = AddTransactionConstant.ERROR_SAVE,
                                isLoading = false
                            )
                        }
                    }
                }
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                Logger.e(AddTransactionConstant.TAG) {
                    "Error submitting transaction: ${ex.message}"
                }
                _state.update {
                    it.copy(
                        error = AddTransactionConstant.ERROR_SAVE,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun deleteTransaction() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                Logger.d(AddTransactionConstant.TAG) { "Deleting transaction with id: $transactionId" }

                val transaction = transactionRepository.getTransactionById(transactionId!!)
                if (transaction != null) {
                    val result = deleteTransactionUseCase(transaction)
                    result.onSuccess {
                        Logger.d(AddTransactionConstant.TAG) { "Transaction deleted successfully" }
                        _state.update {
                            it.copy(
                                isTransactionSaved = true,
                                isLoading = false,
                                showDeleteConfirmDialog = false
                            )
                        }
                    }.onFailure { error ->
                        if (error is CancellationException) throw error
                        Logger.e(AddTransactionConstant.TAG, error) { "Error deleting transaction" }
                        _state.update {
                            it.copy(
                                error = AddTransactionConstant.ERROR_DELETE,
                                isLoading = false,
                                showDeleteConfirmDialog = false
                            )
                        }
                    }
                } else {
                    _state.update {
                        it.copy(
                            error = AddTransactionConstant.ERROR_TRANSACTION_NOT_FOUND,
                            isLoading = false,
                            showDeleteConfirmDialog = false
                        )
                    }
                }
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                Logger.e(AddTransactionConstant.TAG) {
                    "Error deleting transaction: ${ex.message}"
                }
                _state.update {
                    it.copy(
                        error = AddTransactionConstant.ERROR_DELETE,
                        isLoading = false,
                        showDeleteConfirmDialog = false
                    )
                }
            }
        }
    }

    fun reset() {
        Logger.d(AddTransactionConstant.TAG) { "Resetting state" }
        _state.value = AddTransactionState()
    }
}
