package com.antcashmanager.android.ui.screen.transactions.addImport.manager

import com.antcashmanager.android.ui.screen.settings.displaySettings.DisplayConstant
import com.antcashmanager.android.ui.screen.transactions.addImport.AddTransactionState
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.usecase.category.GetCategoriesUseCase
import com.antcashmanager.domain.usecase.settings.GetMealVoucherValueUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionByIdUseCase
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
    suspend fun loadCategories(): Result<List<Category>> =
        runCatching {
            getCategoriesUseCase()
                .first()
                .getOrThrow()
                .filterNot { it.isHidden }
                .sortedBy { it.sortOrder }
        }

    /**
     * Carica il valore unitario dei buoni pasto dalle settings.
     *
     * Se non trovato, ritorna il valore di default definito in DisplayConstant.
     *
     * @return Result contenente il valore unitario dei buoni pasto
     */
    suspend fun loadMealVoucherValue(): Result<Double> =
        runCatching {
            getMealVoucherValueUseCase().first().getOrDefault(DisplayConstant.DEFAULT_MEAL_VOUCHER_VALUE)
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
    suspend fun loadTransactionForEdit(transactionId: Long): Result<Long> =
        runCatching {
            val transaction =
                getTransactionByIdUseCase(transactionId).getOrThrow()
                    ?: throw IllegalArgumentException("Transaction with id $transactionId not found")

            val categoryList = getCategoriesUseCase().first().getOrThrow()
            val selectedCat =
                categoryList.find { it.name == transaction.category }
                    ?: throw IllegalArgumentException(
                        "Category '${transaction.category}' not found for transaction ${transaction.id}",
                    )

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
    suspend fun prepareEditState(
        transactionId: Long,
        currentState: AddTransactionState,
    ): Result<AddTransactionState> =
        prepareEditState(transactionId) { currentState }

    /**
     * Variante che legge lo stato corrente solo al momento del merge finale,
     * evitando di sovrascrivere dati dinamici caricati in parallelo.
     */
    suspend fun prepareEditState(
        transactionId: Long,
        currentStateProvider: () -> AddTransactionState,
    ): Result<AddTransactionState> =
        runCatching {
            val transaction =
                getTransactionByIdUseCase(transactionId).getOrThrow()
                    ?: throw IllegalArgumentException("Transaction with id $transactionId not found")

            val categoryList = getCategoriesUseCase().first().getOrThrow()
            val selectedCat =
                categoryList.find { it.name == transaction.category }
                    ?: throw IllegalArgumentException(
                        "Category '${transaction.category}' not found for transaction ${transaction.id}",
                    )

            // La categoria già assegnata alla transazione deve restare selezionabile
            // nel picker anche se nel frattempo è stata nascosta
            val visibleCategories = categoryList.filterNot { it.isHidden }
            val categoriesForPicker =
                if (selectedCat.isHidden) {
                    visibleCategories + selectedCat
                } else {
                    visibleCategories
                }

            val currentState = currentStateProvider()

            // Il valore unitario del buono non è persistito: lo si ricava dai dati salvati
            // per preservare il totale storico anche se l'impostazione è cambiata.
            val derivedVoucherValue =
                if (transaction.paymentType == PaymentType.MEAL_VOUCHERS && transaction.mealVoucherCount > 0) {
                    val raw = (abs(transaction.amount) - transaction.mealVoucherDifference) / transaction.mealVoucherCount
                    (Math.round(raw * 10_000.0) / 10_000.0).takeIf { it.isFinite() && it > 0 }
                } else {
                    null
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
                mealVoucherDifference = transaction.mealVoucherDifference.toString(),
                mealVoucherValue = derivedVoucherValue ?: currentState.mealVoucherValue,
                isLoading = false,
                categories = categoriesForPicker,
            )
        }
}
