package com.antcashmanager.android.ui.screen.add_transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.category.GetCategoriesUseCase
import com.antcashmanager.domain.usecase.transaction.InsertTransactionUseCase
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
 * Events per la schermata di aggiunta transazione.
 */
sealed interface AddTransactionEvent {
    data class SelectCategory(val category: Category) : AddTransactionEvent
    data class SelectType(val type: TransactionType) : AddTransactionEvent
    data class UpdateTitle(val title: String) : AddTransactionEvent
    data class UpdateAmount(val amount: String) : AddTransactionEvent
    data class UpdateNotes(val notes: String) : AddTransactionEvent
    data class UpdatePayee(val payee: String) : AddTransactionEvent
    data class UpdateLocation(val location: String) : AddTransactionEvent
    data class UpdateTags(val tags: String) : AddTransactionEvent
    data class UpdateTimestamp(val timestamp: Long) : AddTransactionEvent
    data class SetRecurring(val isRecurring: Boolean) : AddTransactionEvent
    data class UpdateRecurrenceInterval(val interval: String) : AddTransactionEvent
    data object NextStep : AddTransactionEvent
    data object PreviousStep : AddTransactionEvent
    data object Submit : AddTransactionEvent
    data object Cancel : AddTransactionEvent
}

// ══════════════════════════════════════════════════════════════════════════════
// VIEWMODEL
// ══════════════════════════════════════════════════════════════════════════════

class AddTransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "AddTransactionViewModel"
        private const val SHARING_TIMEOUT = 5_000L
    }

    // ── UseCases ──
    private val insertTransactionUseCase = InsertTransactionUseCase(transactionRepository)
    private val getCategoriesUseCase = GetCategoriesUseCase(categoryRepository)

    // ── Internal state ──
    private val _internalState = MutableStateFlow(AddTransactionState())

    // ── Public state ──
    val state: StateFlow<AddTransactionState> = combine(
        _internalState,
        getCategoriesUseCase(),
    ) { internalState, categories ->
        internalState.copy(categories = categories)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
        initialValue = AddTransactionState(isLoading = true),
    )

    /**
     * Gestisce gli eventi della UI.
     */
    fun onEvent(event: AddTransactionEvent) {
        Logger.d(TAG) { "Event: $event" }
        when (event) {
            is AddTransactionEvent.SelectCategory -> selectCategory(event.category)
            is AddTransactionEvent.SelectType -> selectType(event.type)
            is AddTransactionEvent.UpdateTitle -> updateTitle(event.title)
            is AddTransactionEvent.UpdateAmount -> updateAmount(event.amount)
            is AddTransactionEvent.UpdateNotes -> updateNotes(event.notes)
            is AddTransactionEvent.UpdatePayee -> updatePayee(event.payee)
            is AddTransactionEvent.UpdateLocation -> updateLocation(event.location)
            is AddTransactionEvent.UpdateTags -> updateTags(event.tags)
            is AddTransactionEvent.UpdateTimestamp -> updateTimestamp(event.timestamp)
            is AddTransactionEvent.SetRecurring -> setRecurring(event.isRecurring)
            is AddTransactionEvent.UpdateRecurrenceInterval -> updateRecurrenceInterval(event.interval)
            is AddTransactionEvent.NextStep -> nextStep()
            is AddTransactionEvent.PreviousStep -> previousStep()
            is AddTransactionEvent.Submit -> submitTransaction()
            is AddTransactionEvent.Cancel -> cancel()
        }
    }

    // ── Event Handlers ──

    private fun selectCategory(category: Category) {
        Logger.d(TAG) { "Category selected: ${category.name}" }
        _internalState.update { it.copy(selectedCategory = category) }
    }

    private fun selectType(type: TransactionType) {
        Logger.d(TAG) { "Type selected: $type" }
        _internalState.update { it.copy(selectedType = type) }
    }

    private fun updateTitle(title: String) {
        _internalState.update { it.copy(title = title) }
    }

    private fun updateAmount(amount: String) {
        _internalState.update { it.copy(amount = amount) }
    }

    private fun updateNotes(notes: String) {
        _internalState.update { it.copy(notes = notes) }
    }

    private fun updatePayee(payee: String) {
        _internalState.update { it.copy(payee = payee) }
    }

    private fun updateLocation(location: String) {
        _internalState.update { it.copy(location = location) }
    }

    private fun updateTags(tags: String) {
        _internalState.update { it.copy(tags = tags) }
    }

    private fun updateTimestamp(timestamp: Long) {
        _internalState.update { it.copy(timestamp = timestamp) }
    }

    private fun setRecurring(isRecurring: Boolean) {
        _internalState.update { it.copy(isRecurring = isRecurring) }
    }

    private fun updateRecurrenceInterval(interval: String) {
        _internalState.update { it.copy(recurrenceInterval = interval) }
    }

    private fun nextStep() {
        val currentState = _internalState.value
        
        // Validazione del step corrente prima di procedere
        if (!isCurrentStepValid(currentState)) {
            Logger.w(TAG) { "Cannot proceed: current step is not valid" }
            return
        }

        val nextStep = when (currentState.currentStep) {
            AddTransactionStep.CATEGORY_SELECTION -> AddTransactionStep.TYPE_SELECTION
            AddTransactionStep.TYPE_SELECTION -> AddTransactionStep.DETAILS
            AddTransactionStep.DETAILS -> AddTransactionStep.CONFIRMATION
            AddTransactionStep.CONFIRMATION -> return
        }

        Logger.d(TAG) { "Moving to next step: $nextStep" }
        _internalState.update { it.copy(currentStep = nextStep) }
    }

    private fun previousStep() {
        val currentState = _internalState.value
        val previousStep = when (currentState.currentStep) {
            AddTransactionStep.CATEGORY_SELECTION -> return
            AddTransactionStep.TYPE_SELECTION -> AddTransactionStep.CATEGORY_SELECTION
            AddTransactionStep.DETAILS -> AddTransactionStep.TYPE_SELECTION
            AddTransactionStep.CONFIRMATION -> AddTransactionStep.DETAILS
        }

        Logger.d(TAG) { "Moving to previous step: $previousStep" }
        _internalState.update { it.copy(currentStep = previousStep) }
    }

    private fun submitTransaction() {
        val currentState = _internalState.value

        // Validazione finale
        if (currentState.selectedCategory == null || currentState.selectedType == null) {
            Logger.e(TAG) { "Cannot submit: category or type not selected" }
            _internalState.update { it.copy(error = "Categoria e tipo sono obbligatori") }
            return
        }

        if (currentState.title.isBlank() || currentState.amount.isBlank()) {
            Logger.e(TAG) { "Cannot submit: title or amount is blank" }
            _internalState.update { it.copy(error = "Titolo e importo sono obbligatori") }
            return
        }

        val amount = currentState.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Logger.e(TAG) { "Cannot submit: invalid amount" }
            _internalState.update { it.copy(error = "Importo non valido") }
            return
        }

        Logger.d(TAG) { "Submitting transaction: ${currentState.title}" }
        viewModelScope.launch {
            try {
                _internalState.update { it.copy(isLoading = true) }
                
                val transaction = Transaction(
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
                )

                insertTransactionUseCase(transaction)
                Logger.d(TAG) { "Transaction submitted successfully" }
                _internalState.value = AddTransactionState()
            } catch (ex: Exception) {
                Logger.e(TAG) { "Error submitting transaction: ${ex.message}" }
                _internalState.update { it.copy(error = "Errore durante il salvataggio") }
            } finally {
                _internalState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun cancel() {
        Logger.d(TAG) { "Cancelling transaction entry" }
        _internalState.value = AddTransactionState()
    }

    /**
     * Valida il step corrente prima di procedere al successivo.
     */
    private fun isCurrentStepValid(state: AddTransactionState): Boolean {
        return when (state.currentStep) {
            AddTransactionStep.CATEGORY_SELECTION -> state.selectedCategory != null
            AddTransactionStep.TYPE_SELECTION -> state.selectedType != null
            AddTransactionStep.DETAILS -> state.title.isNotBlank() && state.amount.isNotBlank()
            AddTransactionStep.CONFIRMATION -> true
        }
    }

    /**
     * Resetta lo stato a quello iniziale.
     */
    fun reset() {
        Logger.d(TAG) { "Resetting state" }
        _internalState.value = AddTransactionState()
    }
}

