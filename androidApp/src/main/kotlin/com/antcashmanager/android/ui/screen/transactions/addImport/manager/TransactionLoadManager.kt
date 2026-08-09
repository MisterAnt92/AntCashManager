package com.antcashmanager.android.ui.screen.transactions.addImport.manager

import com.antcashmanager.android.ui.screen.transactions.addImport.AddTransactionConstant
import com.antcashmanager.android.ui.screen.transactions.addImport.AddTransactionState
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.usecase.category.GetCategoriesUseCase
import com.antcashmanager.domain.usecase.settings.GetMealVoucherValueUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionByIdUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlin.math.abs

/**
 * Manager responsabile del caricamento dei dati iniziali e delle transazioni per la modifica.
 *
 * Responsabilità:
 * - Caricare le categorie dal DB
 * - Caricare il valore dei buoni pasto dalle settings
 * - Caricare una transazione esistente per la modifica
 * - Gestire errori specifici del caricamento
 *
 * Return type: Result<AddTransactionState> per gestione errori uniforme
 */
class TransactionLoadManager(
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getMealVoucherValueUseCase: GetMealVoucherValueUseCase,
) {

    /**
     * Carica le categorie disponibili dal database.
     *
     * Filtra le categorie nascoste (isHidden = true) e le ordina
     * secondo l'ordine specificato nel database.
     *
     * @return Result contenente la lista di categorie ordinate, o un errore
     */
    suspend fun loadCategories(): Result<List<Category>> = runCatching {
        getCategoriesUseCase().first().getOrThrow()
            .filterNot { it.isHidden }
            .sortedBy { it.sortOrder }
    }

    /**
     * Carica il valore unitario dei buoni pasto dalle settings.
     *
     * Se non trovato, ritorna 0.0 come valore di default.
     *
     * @return Result contenente il valore unitario dei buoni pasto
     */
    suspend fun loadMealVoucherValue(): Result<Double> = runCatching {
        getMealVoucherValueUseCase().first().getOrDefault(0.0)
    }

    /**
     * Carica una transazione esistente dal DB per la modifica.
     *
     * Carica la transazione, le categorie, e prepara lo stato iniziale
     * per il form di modifica.
     *
     * Logica speciale:
     * - Se la categoria della transazione è stata nascosta nel frattempo,
     *   la mantiene comunque visibile nel picker per permettere la modifica
     * - Converte l'importo assoluto (il DB mantiene sign per type)
     *
     * @param transactionId ID della transazione da caricare
     * @return Result contenente l'id della transazione caricata per validazione
     * @throws IllegalArgumentException Se la categoria non è trovata
     */
    suspend fun loadTransactionForEdit(transactionId: Long): Result<Long> = runCatching {
        val transaction = getTransactionByIdUseCase(transactionId).getOrThrow()
            ?: throw IllegalArgumentException("Transaction with id $transactionId not found")

        val categoryList = getCategoriesUseCase().first().getOrThrow()
        val selectedCat = categoryList.find { it.name == transaction.category }
            ?: throw IllegalArgumentException("Category '${transaction.category}' not found for transaction ${transaction.id}")

        // Valida che la categoria esista
        transactionId
    }

    /**
     * Prepara lo stato iniziale per la modifica di una transazione.
     *
     * @param transactionId ID della transazione da caricare
     * @param currentState Lo stato attuale del ViewModel
     * @return Result contenente il nuovo stato con i dati della transazione
     */
    suspend fun prepareEditState(transactionId: Long, currentState: AddTransactionState): Result<AddTransactionState> = runCatching {
        val transaction = getTransactionByIdUseCase(transactionId).getOrThrow()
            ?: throw IllegalArgumentException("Transaction with id $transactionId not found")

        val categoryList = getCategoriesUseCase().first().getOrThrow()
        val selectedCat = categoryList.find { it.name == transaction.category }
            ?: throw IllegalArgumentException("Category '${transaction.category}' not found for transaction ${transaction.id}")

        // La categoria già assegnata alla transazione deve restare selezionabile
        // nel picker anche se nel frattempo è stata nascosta
        val visibleCategories = categoryList.filterNot { it.isHidden }
        val categoriesForPicker = if (selectedCat.isHidden) {
            visibleCategories + selectedCat
        } else {
            visibleCategories
        }

        currentState.copy(
            isModifying = true,
            transactionId = transactionId,
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
            isLoading = false,
            categories = categoriesForPicker,
        )
    }
}
