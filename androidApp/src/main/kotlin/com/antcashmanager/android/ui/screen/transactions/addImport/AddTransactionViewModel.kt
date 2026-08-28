package com.antcashmanager.android.ui.screen.transactions.addImport

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.analytics.ErrorTracker
import com.antcashmanager.android.analytics.PerformanceTracker
import com.antcashmanager.android.ui.base.BaseViewModel
import com.antcashmanager.android.ui.screen.transactions.addImport.event.AddTransactionEvent
import com.antcashmanager.android.ui.screen.transactions.addImport.manager.SuggestionsManager
import com.antcashmanager.android.ui.screen.transactions.addImport.manager.TransactionLoadManager
import com.antcashmanager.android.ui.screen.transactions.addImport.manager.TransactionSubmitManager
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.transaction.DeleteTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionByIdUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ══════════════════════════════════════════════════════════════════════════════
// VIEWMODEL
// ══════════════════════════════════════════════════════════════════════════════

class AddTransactionViewModel(
    private val loadManager: TransactionLoadManager,
    private val submitManager: TransactionSubmitManager,
    private val suggestionsManager: SuggestionsManager,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val analyticsManager: AnalyticsManager,
    private val settingsRepository: SettingsRepository,
    private val performanceTracker: PerformanceTracker,
    private val errorTracker: ErrorTracker,
    private val transactionId: Long? = null,
    private val savedStateHandle: SavedStateHandle,  // NEW: Preserva form state su rotation
) : BaseViewModel<AddTransactionEvent>() {

    // ── State ──
    // Nota: SavedStateHandle è disponibile per future implementazioni di state recovery
    // Per adesso usiamo MutableStateFlow standard; future: sincroniazzare con savedStateHandle per preservare
    // lo stato del form durante reconfigurazioni (rotation, process death)
    private val _state = MutableStateFlow(AddTransactionState())
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()

    // Track first form field interaction for analytics
    private var firstFormFieldTracked = false

    init {
        loadCategories()
        loadTransactionSuggestions()
        loadMealVoucherValue()
        loadDefaultPaymentType()
        if (transactionId != null) {
            loadTransactionForEdit(transactionId)
        }
    }

    private fun loadMealVoucherValue() {
        viewModelScope.launch {
            loadManager.loadMealVoucherValue()
                .onSuccess { mealVoucherValue ->
                    _state.update { it.copy(mealVoucherValue = mealVoucherValue) }
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    logError("Error loading meal voucher value: ${error.message}", error)
                }
        }
    }

    private fun loadDefaultPaymentType() {
        viewModelScope.launch {
            try {
                settingsRepository.getDefaultPaymentType().collect { defaultPaymentTypeStr ->
                    val paymentType = try {
                        PaymentType.valueOf(defaultPaymentTypeStr ?: "ELECTRONIC")
                    } catch (e: IllegalArgumentException) {
                        PaymentType.ELECTRONIC
                    }
                    _state.update { it.copy(selectedPaymentType = paymentType) }
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                logError("Error loading default payment type: ${error.message}", error)
                // Fallback a ELECTRONIC (già il default in AddTransactionState)
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            loadManager.loadCategories()
                .onSuccess { categories ->
                    _state.update { it.copy(categories = categories) }
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    logError("Error loading categories: ${error.message}", error)
                    _state.update { it.copy(error = AddTransactionConstant.ERROR_LOAD_CATEGORIES) }
                }
        }
    }

    private fun loadTransactionSuggestions() {
        logDebug("Loading transaction suggestions")
        viewModelScope.launch {
            suggestionsManager.getSuggestions().collect { result ->
                result.onSuccess { suggestions ->
                    logDebug(
                        "Suggestions loaded - titles: ${suggestions.titles.size}, " +
                                "payees: ${suggestions.payees.size}, " +
                                "notes: ${suggestions.notes.size}, " +
                                "locations: ${suggestions.locations.size}, " +
                                "tags: ${suggestions.tags.size}"
                    )
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
    }

    private fun loadTransactionForEdit(id: Long) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                logDebug("Loading transaction with id: $id")

                loadManager.prepareEditState(id, _state.value)
                    .onSuccess { newState ->
                        _state.value = newState.copy(currentStep = AddTransactionStep.DETAILS)
                        logDebug("Transaction loaded: ${newState.title}, category: ${newState.selectedCategory?.name}")
                    }
                    .onFailure { error ->
                        if (error is CancellationException) throw error
                        logError("Error loading transaction: ${error.message}")
                        val errorMessage = when (error) {
                            is IllegalArgumentException -> error.message
                                ?: AddTransactionConstant.ERROR_LOAD_TRANSACTION

                            else -> AddTransactionConstant.ERROR_LOAD_TRANSACTION
                        }
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = errorMessage,
                            )
                        }
                    }
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                logError("Error loading transaction: ${ex.message}")
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
    override fun onEvent(event: AddTransactionEvent) {
        logDebug("Event: $event")
        when (event) {
            is AddTransactionEvent.SelectCategory -> selectCategory(event.category)
            is AddTransactionEvent.SelectType -> selectType(event.type)
            is AddTransactionEvent.SelectPaymentType -> selectPaymentType(event.paymentType)
            is AddTransactionEvent.UpdateTitle -> {
                // Track first form field interaction
                if (!firstFormFieldTracked && event.title.isNotEmpty()) {
                    analyticsManager.logEvent("form_field_first_interaction", Bundle().apply {
                        putString("field", "title")
                    })
                    firstFormFieldTracked = true
                }
                _state.update { it.copy(title = event.title) }
            }
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

            is AddTransactionEvent.UpdateMealVoucherDifference -> {
                _state.update { it.copy(mealVoucherDifference = event.difference) }
            }

            is AddTransactionEvent.UpdateTimestamp -> _state.update { it.copy(timestamp = event.timestamp) }
            is AddTransactionEvent.SetRecurring -> _state.update { it.copy(isRecurring = event.isRecurring) }
            is AddTransactionEvent.UpdateRecurrenceInterval -> {
                // Track recurring interval selection
                analyticsManager.logEvent("recurring_interval_selected", Bundle().apply {
                    putString("interval", event.interval)
                })
                _state.update {
                    it.copy(
                        recurrenceInterval = event.interval
                    )
                }
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
        logDebug("Category selected: ${category.name}, type: ${category.type}")

        val transactionType = if (category.type.uppercase() == "INCOME")
            TransactionType.INCOME
        else
            TransactionType.EXPENSE

        val currentStep = _state.value.currentStep
        val isModifying = _state.value.isModifying

        // Auto-select payment type per categoria "Buoni pasto"
        val selectedPaymentType = if (category.name == "Buoni pasto") {
            logDebug("Auto-selecting MEAL_VOUCHERS payment type for Buoni pasto category")
            PaymentType.MEAL_VOUCHERS
        } else {
            _state.value.selectedPaymentType // Mantieni il precedente per altre categorie
        }

        _state.update {
            it.copy(
                selectedCategory = category,
                selectedType = transactionType,
                selectedPaymentType = selectedPaymentType,
                showCategoryDialog = false,
            )
        }

        // In creazione, al passo CATEGORY_SELECTION → avanza direttamente a DETAILS
        if (!isModifying && currentStep == AddTransactionStep.CATEGORY_SELECTION) {
            logDebug("Auto-advancing to DETAILS after category selection")
            _state.update { it.copy(currentStep = AddTransactionStep.DETAILS) }
        }
    }

    private fun selectType(type: TransactionType) {
        logDebug("Type selected: $type")
        _state.update {
            it.copy(selectedType = type, showTypeDialog = false)
        }
    }

    private fun selectPaymentType(paymentType: PaymentType) {
        logDebug("Payment type selected: $paymentType")
        // Track payment type selection
        analyticsManager.logEvent("payment_type_selected", Bundle().apply {
            putString("payment_type", paymentType.name)
        })
        _state.update { currentState ->
            // Se cambi DA MEAL_VOUCHERS a un altro tipo: resetta voucher, differenza e amount
            val resetMealVouchers = currentState.selectedPaymentType == PaymentType.MEAL_VOUCHERS &&
                    paymentType != PaymentType.MEAL_VOUCHERS

            currentState.copy(
                selectedPaymentType = paymentType,
                showPaymentTypeDialog = false,
                mealVoucherCount = if (resetMealVouchers) "0" else currentState.mealVoucherCount,
                mealVoucherDifference = if (resetMealVouchers) "0" else currentState.mealVoucherDifference,
                amount = if (resetMealVouchers) "" else currentState.amount
            )
        }
    }

    private fun nextStep() {
        val nextStep = when (_state.value.currentStep) {
            AddTransactionStep.CATEGORY_SELECTION -> AddTransactionStep.DETAILS
            AddTransactionStep.DETAILS -> return // Salvataggio diretto dal form
        }
        logDebug("Moving to next step: $nextStep")
        _state.update { it.copy(currentStep = nextStep) }
    }

    private fun previousStep() {
        val prev = when (_state.value.currentStep) {
            AddTransactionStep.CATEGORY_SELECTION -> return
            AddTransactionStep.DETAILS ->
                if (_state.value.isModifying) return
                else AddTransactionStep.CATEGORY_SELECTION
        }
        logDebug("Moving to previous step: $prev")
        _state.update { it.copy(currentStep = prev) }
    }

    private fun submitTransaction() {
        val currentState = _state.value
        val submitStartTime = System.currentTimeMillis()

        // Valida lo stato usando il manager
        val validationError = submitManager.validateTransactionState(currentState)
        if (validationError != null) {
            _state.update { it.copy(error = validationError) }
            val errorType = when (validationError) {
                AddTransactionConstant.ERROR_REQUIRED_CATEGORY_TYPE -> "missing_category_or_type"
                AddTransactionConstant.ERROR_REQUIRED_TITLE_AMOUNT -> "missing_title_or_amount"
                AddTransactionConstant.ERROR_INVALID_AMOUNT -> "invalid_amount"
                "Categoria non più disponibile" -> "category_not_found"
                "Numero buoni pasto non valido" -> "invalid_meal_voucher_count"
                else -> "unknown"
            }
            analyticsManager.logEvent("transaction_form_validation_failed", Bundle().apply {
                putString("error_type", errorType)
            })
            // Track validation error with ErrorTracker
            val fieldName = when (errorType) {
                "missing_category_or_type" -> "category"
                "missing_title_or_amount" -> "title"
                "invalid_amount" -> "amount"
                "category_not_found" -> "category"
                "invalid_meal_voucher_count" -> "meal_voucher_count"
                else -> "unknown"
            }
            errorTracker.trackTransactionValidationError(fieldName, errorType)
            return
        }

        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                // Costruisce il Transaction usando il manager
                val transaction = submitManager.buildTransaction(currentState, transactionId)

                // Salva il Transaction usando il manager
                submitManager.saveTransaction(transaction, currentState.isModifying)
                    .onSuccess {
                        logDebug("Transaction ${if (currentState.isModifying) "updated" else "inserted"} successfully")
                        val duration = System.currentTimeMillis() - submitStartTime
                        performanceTracker.trackTransactionFormSubmitLatency(duration, hasReceipt = false)
                        _state.update { it.copy(isTransactionSaved = true, isLoading = false) }
                    }
                    .onFailure { error ->
                        if (error is CancellationException) throw error
                        logError("Error saving transaction", error)
                        _state.update {
                            it.copy(
                                error = AddTransactionConstant.ERROR_SAVE,
                                isLoading = false
                            )
                        }
                    }
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                logError("Error submitting transaction: ${ex.message}")
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
                logDebug("Deleting transaction with id: $transactionId")

                val transaction = getTransactionByIdUseCase(transactionId!!).getOrThrow()
                if (transaction != null) {
                    val result = deleteTransactionUseCase(transaction)
                    result.onSuccess {
                        logDebug("Transaction deleted successfully")
                        _state.update {
                            it.copy(
                                isTransactionSaved = true,
                                isLoading = false,
                                showDeleteConfirmDialog = false
                            )
                        }
                    }.onFailure { error ->
                        if (error is CancellationException) throw error
                        logError("Error deleting transaction", error)
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
                logError("Error deleting transaction: ${ex.message}")
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
        logDebug("Resetting state")
        _state.value = AddTransactionState()
    }
}
