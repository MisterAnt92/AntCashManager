package com.antcashmanager.domain.exception

/**
 * Eccezioni di dominio per la funzionalità di scansione scontrini.
 */
public sealed class ReceiptScanException(message: String) : Exception(message) {

    /** L'immagine fornita è vuota o non valida. */
    public object InvalidImage : ReceiptScanException("Image is empty or invalid")

    /** Il motore OCR non ha estratto testo leggibile dall'immagine. */
    public object NoTextExtracted : ReceiptScanException("No text could be extracted from the image")

    /** L'importo totale non è stato trovato nel testo dello scontrino. */
    public object AmountNotFound : ReceiptScanException("Total amount not found in receipt text")

    /** Errore generico durante il processo OCR. */
    public data class OcrFailed(val rootCause: Throwable) :
        ReceiptScanException("OCR processing failed: ${rootCause.message}")

    /** Nessuna categoria di spesa disponibile per creare la transazione. */
    public object NoCategoryAvailable :
        ReceiptScanException("No expense category available to create transaction")
}

