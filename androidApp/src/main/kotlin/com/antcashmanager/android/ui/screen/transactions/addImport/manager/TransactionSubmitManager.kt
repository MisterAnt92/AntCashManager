package com.antcashmanager.android.ui.screen.transactions.addImport.manager

import com.antcashmanager.android.ui.screen.transactions.addImport.AddTransactionConstant
import com.antcashmanager.android.ui.screen.transactions.addImport.AddTransactionState
import com.antcashmanager.android.ui.screen.transactions.addImport.validator.TransactionValidator
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.usecase.transaction.InsertTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.UpdateTransactionUseCase

/**
 * Manager responsabile della validazione e del salvataggio delle transazioni.
 *
 * Responsabilità:
 * - Validare lo stato della transazione prima del salvataggio
 * - Convertire lo stato in un oggetto Transaction
 * - Gestire insert vs update a seconda della modalità
 * - Gestire errori specifici del salvataggio
 *
 * Return type: Result<Unit> per gestione errori uniforme
 */
class TransactionSubmitManager(
    private val insertTransactionUseCase: InsertTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val validator: TransactionValidator = TransactionValidator,
) {

    /**
     * Valida lo stato della transazione e ritorna un messaggio di errore se invalido.
     *
     * Validazioni:
     * - Categoria e tipo di transazione obbligatori
     * - Titolo obbligatorio
     * - Validazione differenziata per MEAL_VOUCHERS vs altre transazioni
     * - Categoria deve essere disponibile (non eliminata)
     *
     * @param state Lo stato della transazione da validare
     * @return null se valido, altrimenti un messaggio di errore
     */
    fun validateTransactionState(state: AddTransactionState): String? {
        // Categoria e tipo obbligatori
        if (state.selectedCategory == null || state.selectedType == null) {
            return AddTransactionConstant.ERROR_REQUIRED_CATEGORY_TYPE
        }

        // Titolo obbligatorio
        if (state.title.isBlank()) {
            return AddTransactionConstant.ERROR_REQUIRED_TITLE_AMOUNT
        }

        // Categoria deve essere disponibile nella lista
        if (!state.categories.any { it.id == state.selectedCategory.id }) {
            return "Categoria non più disponibile"
        }

        // Validazione differenziata per MEAL_VOUCHERS
        when (state.selectedPaymentType) {
            PaymentType.MEAL_VOUCHERS -> {
                val voucherCount = state.mealVoucherCount.toIntOrNull()
                if (voucherCount == null || voucherCount <= 0) {
                    return "Numero buoni pasto non valido"
                }
            }

            else -> {
                // Per altre transazioni: validare l'importo
                if (state.amount.isBlank()) {
                    return AddTransactionConstant.ERROR_REQUIRED_TITLE_AMOUNT
                }
                val amount = state.amount.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    return AddTransactionConstant.ERROR_INVALID_AMOUNT
                }
            }
        }

        return null
    }

    /**
     * Converte lo stato della transazione in un oggetto Transaction.
     *
     * Logica:
     * - L'importo è calcolato in base al tipo di transazione
     * - Per EXPENSE: negativo
     * - Per INCOME: positivo
     * - Per MEAL_VOUCHERS: calcolato dal numero di voucher
     *
     * @param state Lo stato della transazione
     * @param transactionId L'ID della transazione (0 per nuove, >0 per modifica)
     * @return Transaction pronta per il salvataggio
     * @throws IllegalStateException Se selectedCategory o selectedType sono null
     *                               (dovrebbero essere validati in validateTransactionState())
     */
    fun buildTransaction(state: AddTransactionState, transactionId: Long?): Transaction {
        // Validate che categoria e tipo siano non-null
        // (dovrebbero già essere validati in validateTransactionState, ma controlliamo qui per safety)
        val selectedCategory = state.selectedCategory
            ?: throw IllegalStateException("selectedCategory must not be null")
        val selectedType = state.selectedType
            ?: throw IllegalStateException("selectedType must not be null")

        // Calcola l'importo finale con il segno corretto
        val finalAmount = state.totalAmount.let { amount ->
            if (selectedType == TransactionType.EXPENSE) {
                -amount // Negativo per le spese
            } else {
                amount // Positivo per le entrate
            }
        }

        return Transaction(
            id = if (state.isModifying) transactionId ?: 0 else 0,
            title = state.title,
            amount = finalAmount,
            category = selectedCategory.name,
            type = selectedType,
            timestamp = state.timestamp,
            notes = state.notes,
            payee = state.payee,
            location = state.location,
            tags = state.tags,
            isRecurring = state.isRecurring,
            recurrenceInterval = state.recurrenceInterval,
            paymentType = state.selectedPaymentType,
            mealVoucherCount = state.mealVoucherCount.toIntOrNull() ?: 0,
            categoryIcon = selectedCategory.icon,
            categoryColor = selectedCategory.color,
        )
    }

    /**
     * Salva la transazione nel database.
     *
     * Esegue insert o update a seconda della modalità (new vs edit).
     *
     * @param transaction La transazione da salvare
     * @param isModifying true per update, false per insert
     * @return Result<Unit> con successo o errore
     */
    suspend fun saveTransaction(transaction: Transaction, isModifying: Boolean): Result<Unit> =
        runCatching {
            if (isModifying) {
                updateTransactionUseCase(transaction).getOrThrow()
            } else {
                insertTransactionUseCase(transaction).getOrThrow()
            }
        }

    /**
     * Esegue il flusso completo di submit della transazione:
     * 1. Valida lo stato
     * 2. Costruisce il Transaction object
     * 3. Salva nel DB
     *
     * @param state Lo stato della transazione
     * @param transactionId L'ID della transazione (per modifica)
     * @return Result<Unit> con successo o messaggio di errore
     */
    suspend fun submitTransaction(state: AddTransactionState, transactionId: Long?): Result<Unit> =
        runCatching {
            // 1. Valida
            val validationError = validateTransactionState(state)
            if (validationError != null) {
                throw IllegalArgumentException(validationError)
            }

            // 2. Costruisci
            val transaction = buildTransaction(state, transactionId)

            // 3. Salva
            saveTransaction(transaction, state.isModifying).getOrThrow()
        }
}
