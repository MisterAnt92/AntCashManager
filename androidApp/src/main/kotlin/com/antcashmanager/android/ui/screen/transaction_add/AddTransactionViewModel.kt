package com.antcashmanager.android.ui.screen.transaction_add

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
import com.antcashmanager.domain.usecase.transaction.InsertTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.UpdateTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.DeleteTransactionUseCase
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
    private val categoryRepository: CategoryRepository,
    private val transactionId: Long? = null,
) : ViewModel() {

    companion object {
        private const val TAG = "AddTransactionViewModel"
    }

    // ── UseCases ──
    private val insertTransactionUseCase = InsertTransactionUseCase(transactionRepository)
    private val updateTransactionUseCase = UpdateTransactionUseCase(transactionRepository)
    private val getCategoriesUseCase = GetCategoriesUseCase(categoryRepository)
    private val deleteTransactionUseCase = DeleteTransactionUseCase(transactionRepository)

    // ── State ──
    private val _state = MutableStateFlow(AddTransactionState())
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()

    init {
        loadCategories()
        if (transactionId != null) {
            loadTransactionForEdit(transactionId)
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase().collect { categories ->
                _state.update { it.copy(categories = categories) }
            }
        }
    }

    private fun loadTransactionForEdit(id: Long) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                Logger.d(TAG) { "Loading transaction with id: $id" }

                val transaction = transactionRepository.getTransactionById(id)
                val categoryList = getCategoriesUseCase().first()

                if (transaction != null) {
                    val selectedCat = categoryList.find { it.name == transaction.category }
                    Logger.d(TAG) { "Transaction loaded: ${transaction.title}, category: $selectedCat" }

                    _state.update {
                        it.copy(
                            isModifying = true,
                            transactionId = id,
                            selectedCategory = selectedCat,
                            selectedType = transaction.type,
                            title = transaction.title,
                            amount = transaction.amount.toString(),
                            notes = transaction.notes,
                            payee = transaction.payee,
                            location = transaction.location,
                            tags = transaction.tags,
                            timestamp = transaction.timestamp,
                            isRecurring = transaction.isRecurring,
                            recurrenceInterval = transaction.recurrenceInterval,
                            selectedPaymentType = transaction.paymentType,
                            currentStep = AddTransactionStep.DETAILS,
                            isLoading = false,
                            categories = categoryList,
                        )
                    }
                } else {
                    Logger.w(TAG) { "Transaction with id $id not found" }
                    _state.update {
                        it.copy(isLoading = false, error = "Transazione non trovata")
                    }
                }
            } catch (ex: Exception) {
                Logger.e(TAG) { "Error loading transaction: ${ex.message}" }
                _state.update {
                    it.copy(isLoading = false, error = "Errore nel caricamento della transazione")
                }
            }
        }
    }

    /**
     * Gestisce gli eventi della UI.
     */
    fun onEvent(event: AddTransactionEvent) {
        Logger.d(TAG) { "Event: $event" }
        when (event) {
            is AddTransactionEvent.SelectCategory -> selectCategory(event.category)
            is AddTransactionEvent.SelectType -> selectType(event.type)
            is AddTransactionEvent.SelectPaymentType -> selectPaymentType(event.paymentType)
            is AddTransactionEvent.UpdateTitle -> _state.update { it.copy(title = event.title) }
            is AddTransactionEvent.UpdateAmount -> _state.update { it.copy(amount = event.amount) }
            is AddTransactionEvent.UpdateNotes -> _state.update { it.copy(notes = event.notes) }
            is AddTransactionEvent.UpdatePayee -> _state.update { it.copy(payee = event.payee) }
            is AddTransactionEvent.UpdateLocation -> _state.update { it.copy(location = event.location) }
            is AddTransactionEvent.UpdateTags -> _state.update { it.copy(tags = event.tags) }
            is AddTransactionEvent.UpdateTimestamp -> _state.update { it.copy(timestamp = event.timestamp) }
            is AddTransactionEvent.SetRecurring -> _state.update { it.copy(isRecurring = event.isRecurring) }
            is AddTransactionEvent.UpdateRecurrenceInterval -> _state.update { it.copy(recurrenceInterval = event.interval) }
            is AddTransactionEvent.NextStep -> nextStep()
            is AddTransactionEvent.PreviousStep -> previousStep()
            is AddTransactionEvent.EditCategory -> _state.update { it.copy(showCategoryDialog = true) }
            is AddTransactionEvent.EditType -> _state.update { it.copy(showTypeDialog = true) }
            is AddTransactionEvent.EditDate -> _state.update { it.copy(showDatePicker = true) }
            is AddTransactionEvent.EditPaymentType -> _state.update { it.copy(showPaymentTypeDialog = true) }
            is AddTransactionEvent.ShowCategoryDialog -> _state.update { it.copy(showCategoryDialog = true) }
            is AddTransactionEvent.ShowTypeDialog -> _state.update { it.copy(showTypeDialog = true) }
            is AddTransactionEvent.ShowPaymentTypeDialog -> _state.update { it.copy(showPaymentTypeDialog = true) }
            is AddTransactionEvent.DismissCategoryDialog -> _state.update { it.copy(showCategoryDialog = false) }
            is AddTransactionEvent.DismissTypeDialog -> _state.update { it.copy(showTypeDialog = false) }
            is AddTransactionEvent.DismissPaymentTypeDialog -> _state.update { it.copy(showPaymentTypeDialog = false) }
            is AddTransactionEvent.DismissDatePicker -> _state.update { it.copy(showDatePicker = false) }
            is AddTransactionEvent.Submit -> submitTransaction()
            is AddTransactionEvent.Cancel -> _state.value = AddTransactionState()
            is AddTransactionEvent.ShowDeleteConfirmDialog -> _state.update { it.copy(showDeleteConfirmDialog = true) }
            is AddTransactionEvent.DismissDeleteConfirmDialog -> _state.update { it.copy(showDeleteConfirmDialog = false) }
            is AddTransactionEvent.ConfirmDelete -> deleteTransaction()
        }
    }

    // ── Event Handlers ──

    private fun selectCategory(category: Category) {
        Logger.d(TAG) { "Category selected: ${category.name}, type: ${category.type}" }

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
            Logger.d(TAG) { "Auto-advancing to DETAILS after category selection" }
            _state.update { it.copy(currentStep = AddTransactionStep.DETAILS) }
        }
    }

    private fun selectType(type: TransactionType) {
        Logger.d(TAG) { "Type selected: $type" }
        _state.update {
            it.copy(selectedType = type, showTypeDialog = false)
        }
    }

    private fun selectPaymentType(paymentType: PaymentType) {
        Logger.d(TAG) { "Payment type selected: $paymentType" }
        _state.update {
            it.copy(selectedPaymentType = paymentType, showPaymentTypeDialog = false)
        }
    }

    private fun nextStep() {
        val nextStep = when (_state.value.currentStep) {
            AddTransactionStep.CATEGORY_SELECTION -> AddTransactionStep.DETAILS
            AddTransactionStep.DETAILS -> return // Salvataggio diretto dal form
        }
        Logger.d(TAG) { "Moving to next step: $nextStep" }
        _state.update { it.copy(currentStep = nextStep) }
    }

    private fun previousStep() {
        val prev = when (_state.value.currentStep) {
            AddTransactionStep.CATEGORY_SELECTION -> return
            AddTransactionStep.DETAILS ->
                if (_state.value.isModifying) return
                else AddTransactionStep.CATEGORY_SELECTION
        }
        Logger.d(TAG) { "Moving to previous step: $prev" }
        _state.update { it.copy(currentStep = prev) }
    }

    private fun submitTransaction() {
        val currentState = _state.value

        if (currentState.selectedCategory == null || currentState.selectedType == null) {
            _state.update { it.copy(error = "Categoria e tipo sono obbligatori") }
            return
        }
        if (currentState.title.isBlank() || currentState.amount.isBlank()) {
            _state.update { it.copy(error = "Titolo e importo sono obbligatori") }
            return
        }
        val amount = currentState.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _state.update { it.copy(error = "Importo non valido") }
            return
        }

        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                val transaction = Transaction(
                    id = if (currentState.isModifying) transactionId ?: 0 else 0,
                    title = currentState.title,
                    amount = amount,
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
                )

                if (currentState.isModifying) {
                    updateTransactionUseCase(transaction)
                    Logger.d(TAG) { "Transaction updated successfully" }
                } else {
                    insertTransactionUseCase(transaction)
                    Logger.d(TAG) { "Transaction inserted successfully" }
                }

                _state.update { it.copy(isTransactionSaved = true, isLoading = false) }
            } catch (ex: Exception) {
                Logger.e(TAG) { "Error submitting transaction: ${ex.message}" }
                _state.update { it.copy(error = "Errore durante il salvataggio", isLoading = false) }
            }
        }
    }

    private fun deleteTransaction() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                Logger.d(TAG) { "Deleting transaction with id: $transactionId" }

                val transaction = transactionRepository.getTransactionById(transactionId!!)
                if (transaction != null) {
                    deleteTransactionUseCase(transaction)
                    Logger.d(TAG) { "Transaction deleted successfully" }
                    _state.update {
                        it.copy(
                            isTransactionSaved = true,
                            isLoading = false,
                            showDeleteConfirmDialog = false
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            error = "Transazione non trovata",
                            isLoading = false,
                            showDeleteConfirmDialog = false
                        )
                    }
                }
            } catch (ex: Exception) {
                Logger.e(TAG) { "Error deleting transaction: ${ex.message}" }
                _state.update {
                    it.copy(
                        error = "Errore durante l'eliminazione",
                        isLoading = false,
                        showDeleteConfirmDialog = false
                    )
                }
            }
        }
    }

    fun reset() {
        Logger.d(TAG) { "Resetting state" }
        _state.value = AddTransactionState()
    }
}
