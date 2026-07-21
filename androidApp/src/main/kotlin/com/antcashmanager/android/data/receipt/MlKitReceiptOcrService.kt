package com.antcashmanager.android.data.receipt

import android.graphics.BitmapFactory
import co.touchlab.kermit.Logger
import com.antcashmanager.domain.exception.ReceiptScanException
import com.antcashmanager.domain.service.ReceiptOcrService
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
 */
class MlKitReceiptOcrService : ReceiptOcrService {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Estrae il testo da un'immagine JPEG/PNG fornita come [ByteArray].
     *
     * @param imageBytes Byte dell'immagine.
     * @return [Result] con il testo estratto completo, o [ReceiptScanException.OcrFailed].
     */
    override suspend fun extractText(imageBytes: ByteArray): Result<String> {
        return try {
            if (imageBytes.isEmpty()) {
                return Result.failure(ReceiptScanException.InvalidImage)
            }

            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ?: return Result.failure(ReceiptScanException.InvalidImage)

            val inputImage = InputImage.fromBitmap(bitmap, 0)

            suspendCancellableCoroutine { continuation ->
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        Logger.d(tag = ReceiptConstants.TAG) {
                            "OCR success: ${visionText.text.length} chars extracted"
                        }
                        continuation.resume(Result.success(visionText.text))
                    }
                    .addOnFailureListener { exception ->
                        Logger.e(throwable = exception, tag = ReceiptConstants.TAG) { "OCR failed" }
                        continuation.resume(
                            Result.failure(ReceiptScanException.OcrFailed(exception))
                        )
                    }

                continuation.invokeOnCancellation {
                    recognizer.close()
                }
            }
        } catch (e: Exception) {
            Logger.e(throwable = e, tag = ReceiptConstants.TAG) { "Unexpected error during OCR" }
            Result.failure(ReceiptScanException.OcrFailed(e))
        }
    }
}

