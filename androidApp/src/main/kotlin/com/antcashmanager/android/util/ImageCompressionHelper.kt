package com.antcashmanager.android.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import co.touchlab.kermit.Logger
import java.io.ByteArrayOutputStream

/**
 * Utility per comprimere e ridimensionare immagini prima del processing OCR.
 *
 * **Strategia**:
 * 1. Decode immagine con downsampling (max 1920×1440)
 * 2. Encode come JPEG con quality 80 (OCR non richiede qualità fotografica)
 * 3. Riduce size: ~48MB raw → ~200-300KB compressed
 *
 * **Utilizzo**:
 * ```kotlin
 * val compressedBytes = ImageCompressionHelper.compressImage(originalBytes)
 * // Passa compressedBytes a OCR service
 * ```
 *
 * **Compatibilità**:
 * - Android 8.0+ (API 26+)
 * - Tutti i device, specialmente Moto Z2 Play (540 dpi)
 * - Reusable per backup, import, e altre features
 */
object ImageCompressionHelper {

    private const val MAX_IMAGE_WIDTH = 1920
    private const val MAX_IMAGE_HEIGHT = 1440
    private const val JPEG_QUALITY = 80  // 0-100, dove 100 = massima qualità
    private const val TAG = "ImageCompressionHelper"

    /**
     * Comprimi immagine da ByteArray a ByteArray compresso.
     *
     * Ridimensiona a max 1920×1440 e comprime con qualità JPEG 80.
     *
     * @param imageBytes Byte array dell'immagine originale (JPEG/PNG)
     * @return ByteArray compresso (JPEG quality 80, max 1920×1440)
     * @throws IllegalArgumentException se imageBytes è vuoto
     * @throws Exception se fallisce decode/encode
     */
    fun compressImage(imageBytes: ByteArray): ByteArray {
        if (imageBytes.isEmpty()) {
            throw IllegalArgumentException("Image bytes cannot be empty")
        }

        return try {
            // ═════════════════════════════════════════════════════════════════════
            // STEP 1: Decode dimensions first (no allocation)
            // ═════════════════════════════════════════════════════════════════════
            val dimensionOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, dimensionOptions)

            val originalWidth = dimensionOptions.outWidth
            val originalHeight = dimensionOptions.outHeight

            if (originalWidth <= 0 || originalHeight <= 0) {
                throw IllegalArgumentException("Invalid image dimensions: ${originalWidth}×${originalHeight}")
            }

            // ═════════════════════════════════════════════════════════════════════
            // STEP 2: Calculate downsampling factor
            // ═════════════════════════════════════════════════════════════════════
            val inSampleSize = calculateInSampleSize(
                originalWidth,
                originalHeight,
                MAX_IMAGE_WIDTH,
                MAX_IMAGE_HEIGHT
            )

            Logger.d(tag = TAG) {
                "Compressing image: ${originalWidth}×${originalHeight} " +
                "→ ${originalWidth / inSampleSize}×${originalHeight / inSampleSize} " +
                "(inSampleSize=$inSampleSize)"
            }

            // ═════════════════════════════════════════════════════════════════════
            // STEP 3: Decode bitmap WITH downsampling
            // ═════════════════════════════════════════════════════════════════════
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }

            val bitmap = BitmapFactory.decodeByteArray(
                imageBytes, 0, imageBytes.size, decodeOptions
            ) ?: throw IllegalArgumentException("Failed to decode image")

            try {
                // ═════════════════════════════════════════════════════════════════
                // STEP 4: Re-encode as JPEG with quality 80
                // ═════════════════════════════════════════════════════════════════
                val outputStream = ByteArrayOutputStream()
                val success = bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    JPEG_QUALITY,
                    outputStream
                )

                if (!success) {
                    throw IllegalStateException("JPEG compression failed")
                }

                val compressedBytes = outputStream.toByteArray()
                val compressionRatio = (100 * compressedBytes.size) / imageBytes.size

                Logger.d(tag = TAG) {
                    "Compression result: ${imageBytes.size} bytes " +
                    "→ ${compressedBytes.size} bytes " +
                    "($compressionRatio% original size)"
                }

                return compressedBytes
            } finally {
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }
        } catch (e: Exception) {
            Logger.e(throwable = e, tag = TAG) {
                "Image compression failed: ${e.message}"
            }
            throw e
        }
    }

    /**
     * Calcola il fattore di downsampling (inSampleSize) per mantenere bitmap entro limiti.
     *
     * @param imageWidth Larghezza immagine originale
     * @param imageHeight Altezza immagine originale
     * @param maxWidth Max larghezza consentita
     * @param maxHeight Max altezza consentita
     * @return Fattore di downsampling (1, 2, 4, 8, ...)
     */
    private fun calculateInSampleSize(
        imageWidth: Int,
        imageHeight: Int,
        maxWidth: Int,
        maxHeight: Int
    ): Int {
        var inSampleSize = 1

        while ((imageWidth / inSampleSize) > maxWidth ||
            (imageHeight / inSampleSize) > maxHeight) {
            inSampleSize *= 2
        }

        return inSampleSize
    }
}
