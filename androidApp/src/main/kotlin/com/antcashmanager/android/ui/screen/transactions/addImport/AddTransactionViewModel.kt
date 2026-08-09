package com.antcashmanager.android.ui.screen.transactions.addImport

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.ui.base.BaseViewModel
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.ui.screen.transactions.addImport.event.AddTransactionEvent
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.usecase.category.GetCategoriesUseCase
import com.antcashmanager.domain.usecase.settings.GetMealVoucherValueUseCase
import com.antcashmanager.domain.usecase.transaction.DeleteTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionByIdUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionSuggestionsUseCase
import com.antcashmanager.domain.usecase.transaction.InsertTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.UpdateTransactionUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import java.util.Locale

// ══════════════════════════════════════════════════════════════════════════════
// VIEWMODEL
// ══════════════════════════════════════════════════════════════════════════════

class AddTransactionViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getMealVoucherValueUseCase: GetMealVoucherValueUseCase,
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val insertTransactionUseCase: InsertTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val getTransactionSuggestionsUseCase: GetTransactionSuggestionsUseCase,
    private val analyticsManager: AnalyticsManager,
    private val transactionId: Long? = null,
) : BaseViewModel<AddTransactionEvent>() {

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
                val mealVoucherValue = getMealVoucherValueUseCase().first().getOrDefault(0.0)
                _state.update { it.copy(mealVoucherValue = mealVoucherValue) }
            } catch (ex: Exception) {
                logError("Error loading meal voucher value: ${ex.message}")
                // Keep default value
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase().collect { result ->
                result.onSuccess { categories ->
                    // Le categorie nascoste non vanno offerte per nuove transazioni.
                    // Mantieni ordine consistente tramite sortOrder dal database.
                    val sorted = categories.filterNot { category -> category.isHidden }
                        .sortedBy { it.sortOrder }
                    _state.update { it.copy(categories = sorted) }
                }.onFailure { error ->
                    if (error is CancellationException) throw error
                    logError("Error loading categories: ${error.message}", error)
                    _state.update { it.copy(error = AddTransactionConstant.ERROR_LOAD_CATEGORIES) }
                }
            }
        }
    }

    private fun loadTransactionSuggestions() {
        logDebug("Loading transaction suggestions")
        viewModelScope.launch {
            getTransactionSuggestionsUseCase().collect { result ->
                result.onSuccess { suggestions ->
                    logDebug("Suggestions loaded - titles: ${suggestions.titles.size}, " +
                                "payees: ${suggestions.payees.size}, " +
                                "notes: ${suggestions.notes.size}, " +
                                "locations: ${suggestions.locations.size}, " +
                                "tags: ${suggestions.tags.size}")
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

                val transaction = getTransactionByIdUseCase(id).getOrThrow()
                val categoryResult = getCategoriesUseCase().first()
                val categoryList = try {
                    categoryResult.getOrThrow()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logError("Error loading categories for edit: ${e.message}")
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
                    logDebug("Transaction loaded: ${transaction.title}, category: $selectedCat")

                    // FASE 2: Verifica se la categoria è stata cancellata
                    if (selectedCat == null) {
                        logWarn("Category '${transaction.category}' not found for transaction ${transaction.id}")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = "Categoria non trovata: ${transaction.category}",
                            )
                        }
                        return@launch
                    }

                    // La categoria già assegnata alla transazione deve restare selezionabile
                    // nel picker anche se nel frattempo è stata nascosta, altrimenti l'utente
                    // non la vedrebbe più tra le opzioni durante la modifica.
                    val visibleCategories = categoryList.filterNot { it.isHidden }
                    val categoriesForPicker = if (selectedCat.isHidden) {
                        visibleCategories + selectedCat
                    } else {
                        visibleCategories
                    }

                    _state.update {
                        it.copy(
                            isModifying = true,
                            transactionId = id,
                            selectedCategory = selectedCat,
                            selectedType = transaction.type,
                            title = transaction.title,
                            amount = abs(transaction.amount).toString(),
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
                            categories = categoriesForPicker,
                        )
                    }
                } else {
                    logWarn("Transaction with id $id not found")
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
                _state.update { currentState ->
                    // Calcola l'importo automaticamente basato sul numero di voucher
                    val calculatedAmount = event.count.toIntOrNull()?.let { count ->
                        // Importo = numero voucher * valore unitario
                        val total = count * currentState.mealVoucherValue
                        String.format(java.util.Locale.US, "%.2f", total)
                    } ?: ""

                    currentState.copy(
                        mealVoucherCount = event.count,
                        amount = calculatedAmount,
                    )
                }
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
        _state.update { currentState ->
            // Se cambi DA MEAL_VOUCHERS a un altro tipo: resetta voucher e amount calcolato
            val resetMealVouchers = currentState.selectedPaymentType == PaymentType.MEAL_VOUCHERS &&
                                    paymentType != PaymentType.MEAL_VOUCHERS

            currentState.copy(
                selectedPaymentType = paymentType,
                showPaymentTypeDialog = false,
                mealVoucherCount = if (resetMealVouchers) "0" else currentState.mealVoucherCount,
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

        if (currentState.selectedCategory == null || currentState.selectedType == null) {
            _state.update { it.copy(error = AddTransactionConstant.ERROR_REQUIRED_CATEGORY_TYPE) }
            analyticsManager.logEvent("transaction_form_validation_failed", Bundle().apply {
                putString("error_type", "missing_category_or_type")
            })
            return
        }

        // FASE 1: Validare che la categoria esista ancora nella lista disponibile
        if (!currentState.categories.any { it.id == currentState.selectedCategory?.id }) {
            _state.update { it.copy(error = "Categoria non più disponibile") }
            analyticsManager.logEvent("transaction_form_validation_failed", Bundle().apply {
                putString("error_type", "category_not_found")
            })
            return
        }

        // FIX: Titolo sempre obbligatorio
        if (currentState.title.isBlank()) {
            _state.update { it.copy(error = AddTransactionConstant.ERROR_REQUIRED_TITLE_AMOUNT) }
            analyticsManager.logEvent("transaction_form_validation_failed", Bundle().apply {
                putString("error_type", "missing_title")
            })
            return
        }

        // FIX: Validazione differenziata per MEAL_VOUCHERS
        if (currentState.selectedPaymentType == PaymentType.MEAL_VOUCHERS) {
            // Per MEAL_VOUCHERS: validare solo mealVoucherCount (amount viene calcolato automaticamente)
            val voucherCount = currentState.mealVoucherCount.toIntOrNull()
            if (voucherCount == null || voucherCount <= 0) {
                _state.update { it.copy(error = "Numero buoni pasto non valido") }
                analyticsManager.logEvent("transaction_form_validation_failed", Bundle().apply {
                    putString("error_type", "invalid_meal_voucher_count")
                })
                return
            }
        } else {
            // Per altre transazioni: validare amount totale
            if (currentState.amount.isBlank()) {
                _state.update { it.copy(error = AddTransactionConstant.ERROR_REQUIRED_TITLE_AMOUNT) }
                analyticsManager.logEvent("transaction_form_validation_failed", Bundle().apply {
                    putString("error_type", "missing_amount")
                })
                return
            }
            val amount = currentState.amount.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                _state.update { it.copy(error = AddTransactionConstant.ERROR_INVALID_AMOUNT) }
                analyticsManager.logEvent("transaction_form_validation_failed", Bundle().apply {
                    putString("error_type", "invalid_amount")
                })
                return
            }
        }

        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                // Calcola l'importo finale: positivo per INCOME, negativo per EXPENSE
                val finalAmount = currentState.totalAmount.let { amount ->
                    if (currentState.selectedType == TransactionType.EXPENSE) {
                        -amount // Nega per le spese
                    } else {
                        amount // Mantieni positivo per le entrate
                    }
                }

                val transaction = Transaction(
                    id = if (currentState.isModifying) transactionId ?: 0 else 0,
                    title = currentState.title,
                    amount = finalAmount,
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
                        logDebug("Transaction updated successfully")
                        _state.update { it.copy(isTransactionSaved = true, isLoading = false) }
                    }.onFailure { error ->
                        if (error is CancellationException) throw error
                        logError("Error updating transaction", error)
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
                        logDebug("Transaction inserted successfully")
                        _state.update { it.copy(isTransactionSaved = true, isLoading = false) }
                    }.onFailure { error ->
                        if (error is CancellationException) throw error
                        logError("Error inserting transaction", error)
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
