package com.antcashmanager.domain.usecase.receipt

import com.antcashmanager.domain.exception.ReceiptScanException
import com.antcashmanager.domain.model.ReceiptData
import com.antcashmanager.domain.service.ReceiptOcrService
import com.antcashmanager.domain.usecase.BaseUseCase
import com.antcashmanager.domain.util.ReceiptTextParser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * UseCase per la scansione di uno scontrino.
 *
 * Orchestrazione:
 * 1. Invia i byte dell'immagine al servizio OCR [ReceiptOcrService].
 * 2. Parsa il testo estratto tramite [ReceiptTextParser].
 * 3. Valida che l'importo totale sia stato trovato.
 *
 * @param ocrService Servizio OCR (implementazione platform-specific).
 * @param dispatcher Dispatcher per l'esecuzione asincrona (default: [Dispatchers.Default]).
 */
class ScanReceiptUseCase(
    private val ocrService: ReceiptOcrService,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : BaseUseCase<ByteArray, Result<ReceiptData>>(dispatcher) {

    /**
     * Esegue la scansione OCR e il parsing del testo.
     *
     * Validazioni:
     * - Immagine non vuota
     * - Testo OCR estratto non vuoto
     * - Importo totale > 0
     * - Il tipo transazione è sempre EXPENSE (garantito da [CreateTransactionFromReceiptUseCase])
     *
     * @param params Byte dell'immagine dello scontrino (JPEG/PNG).
     * @return [Result] con [ReceiptData] estratto e validato, o [ReceiptScanException].
     */
    override suspend fun execute(params: ByteArray): Result<ReceiptData> = runCatching {
        if (params.isEmpty()) throw ReceiptScanException.InvalidImage

        val text = ocrService.extractText(params).getOrElse { rootCause ->
            throw ReceiptScanException.OcrFailed(rootCause)
        }

        if (text.isBlank()) throw ReceiptScanException.NoTextExtracted

        // Il parser esegue internamente la validazione della consistenza (IVA vs totale)
        val receiptData = ReceiptTextParser.parse(text)

        if (receiptData.totalAmount <= 0.0) throw ReceiptScanException.AmountNotFound

        receiptData
    }
}

