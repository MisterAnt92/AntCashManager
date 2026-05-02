package com.antcashmanager.domain.exception

/**
 * Eccezioni di dominio per la funzionalità di scansione scontrini.
 */
sealed class ReceiptScanException(message: String) : Exception(message) {

    /** L'immagine fornita è vuota o non valida. */
    object InvalidImage : ReceiptScanException("Image is empty or invalid")

    /** Il motore OCR non ha estratto testo leggibile dall'immagine. */
    object NoTextExtracted : ReceiptScanException("No text could be extracted from the image")

    /** L'importo totale non è stato trovato nel testo dello scontrino. */
    object AmountNotFound : ReceiptScanException("Total amount not found in receipt text")

    /** Errore generico durante il processo OCR. */
    data class OcrFailed(val rootCause: Throwable) :
        ReceiptScanException("OCR processing failed: ${rootCause.message}")

    /** Nessuna categoria di spesa disponibile per creare la transazione. */
    object NoCategoryAvailable :
        ReceiptScanException("No expense category available to create transaction")
}

