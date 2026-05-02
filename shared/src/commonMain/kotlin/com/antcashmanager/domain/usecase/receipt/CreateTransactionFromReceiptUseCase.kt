package com.antcashmanager.domain.usecase.receipt

import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.ReceiptData
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.BaseUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Parametri per la creazione di una transazione da scontrino.
 *
 * @property receiptData Dati estratti dallo scontrino.
 * @property title Titolo della transazione (es. nome negozio o titolo personalizzato).
 * @property categoryName Nome della categoria selezionata dall'utente.
 * @property categoryIcon Icona della categoria.
 * @property categoryColor Colore della categoria (ARGB Long).
 * @property paymentType Tipo di pagamento. Se null, usa il valore rilevato in [receiptData].
 * @property timestamp Data/ora della transazione.
 */
data class CreateTransactionFromReceiptParams(
    val receiptData: ReceiptData,
    val title: String,
    val categoryName: String,
    val categoryIcon: String = "",
    val categoryColor: Long = 0xFF90A4AE,
    val notes: String = "",
    val paymentType: PaymentType? = null,
    val timestamp: Long = System.currentTimeMillis(),
)

/**
 * UseCase per la creazione di una [Transaction] di tipo USCITA a partire dai dati
 * estratti da uno scontrino.
 *
 * L'IVA viene serializzata nel campo `notes` in formato compatto per minimizzare
 * lo spazio su database senza aggiungere colonne.
 * Formato: "IVA {rate}%: {amount}" (prefisso, se IVA presente).
 *
 * @param transactionRepository Repository per il salvataggio della transazione.
 * @param dispatcher Dispatcher per l'esecuzione asincrona.
 */
class CreateTransactionFromReceiptUseCase(
    private val transactionRepository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : BaseUseCase<CreateTransactionFromReceiptParams, Result<Long>>(dispatcher) {

    /**
     * Crea e persiste la transazione di uscita.
     *
     * Garantisce:
     * - Il tipo è sempre [TransactionType.EXPENSE] (scontrini = uscite)
     * - Il tipo di pagamento viene dall'utente (override) o dal rilevamento automatico OCR
     * - L'IVA è codificata nel campo notes in formato compatto (no colonne extra su DB)
     *
     * @param params [CreateTransactionFromReceiptParams] con tutti i dati necessari.
     * @return [Result] con l'id della transazione inserita, o eccezione di dominio.
     */
    override suspend fun execute(params: CreateTransactionFromReceiptParams): Result<Long> = runCatching {
        val receipt = params.receiptData

        // Il tipo di pagamento viene dall'utente (se ha fatto override) o dall'OCR
        val resolvedPaymentType = params.paymentType ?: receipt.paymentType

        val transaction = Transaction(
            title = params.title.ifBlank { receipt.payee.ifBlank { "Spesa" } },
            amount = receipt.totalAmount,
            category = params.categoryName,
            type = TransactionType.EXPENSE,       // scontrini = SEMPRE uscite
            paymentType = resolvedPaymentType,    // cash / buoni pasto / elettronico
            timestamp = params.timestamp,
            notes = params.notes,
            payee = receipt.payee,
            location = receipt.location,
            categoryIcon = params.categoryIcon,
            categoryColor = params.categoryColor,
        )

        transactionRepository.insertTransaction(transaction)
    }
}

