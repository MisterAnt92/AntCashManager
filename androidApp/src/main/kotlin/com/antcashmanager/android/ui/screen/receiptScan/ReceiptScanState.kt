package com.antcashmanager.android.ui.screen.receiptScan

import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.ReceiptData

/**
 * UI State per la schermata di scansione scontrino.
 *
 * @property step Step corrente del flusso di scansione.
 * @property receiptData Dati estratti dallo scontrino (disponibile dopo la scansione).
 * @property title Titolo transazione (editabile dall'utente).
 * @property payee Beneficiario (editabile).
 * @property location Luogo (editabile).
 * @property notes Note della transazione (editabile, pre-popolato con i dettagli scontrino).
 * @property vatNote Nota IVA formattata (solo lettura, calcolata automaticamente).
 * @property selectedCategory Categoria selezionata per la transazione.
 * @property selectedPaymentType Tipo di pagamento — rilevato automaticamente ma modificabile dall'utente.
 * @property categories Lista categorie di tipo EXPENSE disponibili.
 * @property isLoading Indica operazione in corso.
 * @property error Messaggio d'errore, null se nessun errore.
 * @property isTransactionSaved True quando la transazione è stata salvata con successo.
 * @property showCategoryDialog Mostra il dialog di selezione categoria.
 * @property showPaymentTypeDialog Mostra il dialog di selezione tipo pagamento.
 */
data class ReceiptScanState(
    val step: ReceiptScanStep = ReceiptScanStep.CAPTURE,
    val receiptData: ReceiptData? = null,
    val title: String = "",
    val payee: String = "",
    val location: String = "",
    val notes: String = "",
    val vatNote: String = "",
    val selectedCategory: Category? = null,
    val selectedPaymentType: PaymentType = PaymentType.ELECTRONIC,
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isTransactionSaved: Boolean = false,
    val showCategoryDialog: Boolean = false,
    val showPaymentTypeDialog: Boolean = false,
)

/**
 * Step del flusso di scansione scontrino.
 */
enum class ReceiptScanStep {
    /** Cattura immagine tramite fotocamera o galleria. */
    CAPTURE,

    /** Elaborazione OCR in corso. */
    PROCESSING,

    /** Revisione e correzione dei dati estratti prima del salvataggio. */
    REVIEW,
}

