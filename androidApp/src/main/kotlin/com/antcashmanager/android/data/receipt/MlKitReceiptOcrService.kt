package com.antcashmanager.android.data.receipt

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import co.touchlab.kermit.Logger
import com.antcashmanager.domain.exception.ReceiptScanException
import com.antcashmanager.domain.service.ReceiptOcrService
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Implementazione Android di [ReceiptOcrService] basata su Google ML Kit Text Recognition.
 *
 * Utilizza il riconoscimento testo on-device (nessuna rete necessaria).
 * Compatibile con testi latini (italiano, inglese, francese, tedesco, spagnolo).
 *
 * **Ottimizzazione memoria**:
 * - Implementa downsampling intelligente (inSampleSize) per evitare OOM
 * - Max bitmap: 2048×2048 pixel (riduce allocazione 8-10x su immagini 4000×3000)
 * - bitmap.recycle() in finally block per liberare memoria nativa
 * - Memory monitoring per diagnostica
 */
class MlKitReceiptOcrService : ReceiptOcrService {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    companion object {
        private const val MAX_BITMAP_WIDTH = 2048
        private const val MAX_BITMAP_HEIGHT = 2048
        private const val MEMORY_WARNING_THRESHOLD = 50_000_000 // 50MB
    }

    /**
     * Estrae il testo da un'immagine JPEG/PNG fornita come [ByteArray].
     *
     * Implementa downsampling intelligente per evitare OOM su device con heap limitato.
     * Max bitmap size: 2048×2048 pixels (preserva qualità OCR, riduce allocazione 8-10x).
     *
     * @param imageBytes Byte dell'immagine.
     * @return [Result] con il testo estratto completo, o [ReceiptScanException.OcrFailed].
     */
    override suspend fun extractText(imageBytes: ByteArray): Result<String> {
        return try {
            if (imageBytes.isEmpty()) {
                return Result.failure(ReceiptScanException.InvalidImage)
            }

            // ═══════════════════════════════════════════════════════════════════
            // STEP 1: Check available heap memory (defensive)
            // ═══════════════════════════════════════════════════════════════════
            val availableHeap =
                Runtime.getRuntime().maxMemory() -
                    (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())

            if (availableHeap < MEMORY_WARNING_THRESHOLD) {
                Logger.w(tag = ReceiptConstants.TAG) {
                    "Low memory warning: ${availableHeap / 1_000_000}MB available, OCR may fail"
                }
            }

            // ═══════════════════════════════════════════════════════════════════
            // STEP 2: Decode dimensions WITHOUT allocating bitmap
            // ═══════════════════════════════════════════════════════════════════
            val dimensionOptions =
                BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, dimensionOptions)

            val imageWidth = dimensionOptions.outWidth
            val imageHeight = dimensionOptions.outHeight

            if (imageWidth <= 0 || imageHeight <= 0) {
                return Result.failure(ReceiptScanException.InvalidImage)
            }

            // ═══════════════════════════════════════════════════════════════════
            // STEP 3: Calculate inSampleSize for intelligent downsampling
            // ═══════════════════════════════════════════════════════════════════
            val inSampleSize = calculateInSampleSize(imageWidth, imageHeight)

            Logger.d(tag = ReceiptConstants.TAG) {
                "Image dimensions: $imageWidth×$imageHeight, " +
                    "inSampleSize=$inSampleSize, " +
                    "final size: ${imageWidth / inSampleSize}×${imageHeight / inSampleSize}"
            }

            // ═══════════════════════════════════════════════════════════════════
            // STEP 4: Decode bitmap WITH downsampling
            // ═══════════════════════════════════════════════════════════════════
            val decodeOptions =
                BitmapFactory.Options().apply {
                    this.inSampleSize = inSampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888 // ML Kit richiede ARGB
                }

            val bitmap =
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, decodeOptions)
                    ?: return Result.failure(ReceiptScanException.InvalidImage)

            try {
                // ═════════════════════════════════════════════════════════════
                // STEP 5: Process bitmap with ML Kit
                // ═════════════════════════════════════════════════════════════
                val inputImage = InputImage.fromBitmap(bitmap, 0)

                return suspendCancellableCoroutine { continuation ->
                    recognizer
                        .process(inputImage)
                        .addOnSuccessListener { visionText ->
                            Logger.d(tag = ReceiptConstants.TAG) {
                                "OCR success: ${visionText.text.length} chars extracted"
                            }
                            continuation.resume(Result.success(visionText.text))
                        }.addOnFailureListener { exception ->
                            Logger.e(throwable = exception, tag = ReceiptConstants.TAG) { "OCR failed" }
                            // Unbundled ML Kit: model may still be downloading via Play Services
                            val failure =
                                if (exception is MlKitException &&
                                    exception.errorCode == MlKitException.UNAVAILABLE
                                ) {
                                    ReceiptScanException.ModelNotReady
                                } else {
                                    ReceiptScanException.OcrFailed(exception)
                                }
                            continuation.resume(Result.failure(failure))
                        }

                    continuation.invokeOnCancellation {
                        recognizer.close()
                    }
                }
            } finally {
                // ═════════════════════════════════════════════════════════════
                // STEP 6: Cleanup - CRITICAL: libera memoria nativa del bitmap
                // ═════════════════════════════════════════════════════════════
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                    Logger.d(tag = ReceiptConstants.TAG) { "Bitmap recycled" }
                }
            }
        } catch (e: Exception) {
            Logger.e(throwable = e, tag = ReceiptConstants.TAG) { "Unexpected error during OCR" }
            Result.failure(ReceiptScanException.OcrFailed(e))
        }
    }

    /**
     * Calcola il fattore di downsampling (inSampleSize) per mantenere bitmap entro limiti.
     *
     * Formula: 4000×3000 → inSampleSize=2 → 2000×1500 (~12MB vs ~107MB)
     *
     * @param imageWidth Larghezza immagine originale
     * @param imageHeight Altezza immagine originale
     * @param maxWidth Max larghezza consentita (default: 2048)
     * @param maxHeight Max altezza consentita (default: 2048)
     * @return Fattore di downsampling (1, 2, 4, 8, ...)
     */
    private fun calculateInSampleSize(
        imageWidth: Int,
        imageHeight: Int,
        maxWidth: Int = MAX_BITMAP_WIDTH,
        maxHeight: Int = MAX_BITMAP_HEIGHT,
    ): Int {
        var inSampleSize = 1

        // Downsampling progressivo finché non rientra nei limiti
        while ((imageWidth / inSampleSize) > maxWidth ||
            (imageHeight / inSampleSize) > maxHeight
        ) {
            inSampleSize *= 2
        }

        return inSampleSize
    }
}
