package com.antcashmanager.domain.usecase.receipt

import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.ReceiptData
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.base.UseCase
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
public data class CreateTransactionFromReceiptParams(
    public val receiptData: ReceiptData,
    public val title: String,
    public val categoryName: String,
    public val categoryIcon: String = "",
    public val categoryColor: Long = 0xFF90A4AE,
    public val notes: String = "",
    public val paymentType: PaymentType? = null,
    public val timestamp: Long = System.currentTimeMillis(),
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
public class CreateTransactionFromReceiptUseCase(
    private val transactionRepository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<CreateTransactionFromReceiptParams, Long>(dispatcher) {

    internal companion object {
        private const val DEFAULT_RECEIPT_TITLE = "Scontrino"
        private const val VAT_LABEL = "IVA"
    }

    /**
     * Crea e persiste la transazione di uscita.
     *
     * Garantisce:
     * - Il tipo è sempre [TransactionType.EXPENSE] (scontrini = uscite)
     * - Il tipo di pagamento viene dall'utente (override) o dal rilevamento automatico OCR
     * - L'IVA è codificata nel campo notes in formato compatto (no colonne extra su DB)
     *
     * @param params [CreateTransactionFromReceiptParams] con tutti i dati necessari.
     * @return L'id della transazione inserita. Lancia un'eccezione in caso di errore.
     */
    override suspend fun execute(params: CreateTransactionFromReceiptParams): Long {
        val receipt = params.receiptData

        // Il tipo di pagamento viene dall'utente (se ha fatto override) o dall'OCR
        val resolvedPaymentType = params.paymentType ?: receipt.paymentType
        val resolvedTitle = params.title.ifBlank {
            receipt.payee.ifBlank { DEFAULT_RECEIPT_TITLE }
        }
        val resolvedNotes = buildReceiptNotes(
            existingNotes = params.notes,
            vatRate = receipt.vatRate,
            vatAmount = receipt.vatAmount,
        )

        val transaction = Transaction(
            title = resolvedTitle,
            amount = receipt.totalAmount,
            category = params.categoryName,
            type = TransactionType.EXPENSE,       // scontrini = SEMPRE uscite
            paymentType = resolvedPaymentType,    // cash / buoni pasto / elettronico
            timestamp = params.timestamp,
            notes = resolvedNotes,
            payee = receipt.payee,
            location = receipt.location,
            categoryIcon = params.categoryIcon,
            categoryColor = params.categoryColor,
        )

        return transactionRepository.insertTransaction(transaction)
    }

    private fun buildReceiptNotes(
        existingNotes: String,
        vatRate: Double,
        vatAmount: Double,
    ): String {
        val vatNote = buildString {
            if (vatRate > 0.0 || vatAmount > 0.0) {
                append(VAT_LABEL)
                if (vatRate > 0.0) {
                    append(" ")
                    append(vatRate.toInt())
                    append("%")
                }
                if (vatAmount > 0.0) {
                    append(": ")
                    append(vatAmount)
                }
            }
        }

        return listOf(vatNote, existingNotes)
            .filter { it.isNotBlank() }
            .joinToString(separator = "\n\n")
    }
}

