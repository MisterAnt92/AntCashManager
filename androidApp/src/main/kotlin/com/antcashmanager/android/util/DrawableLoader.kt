package com.antcashmanager.android.util

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import co.touchlab.kermit.Logger

/**
 * Utility per caricare drawable con downsampling intelligente.
 *
 * Risolve il problema di bitmap troppo grandi su device ad alta densità (xxhdpi 540+).
 *
 * **Problema**: Immagini drawable 1226×2559 su xxhdpi vengono upscalate 3x a ~3700×7700px (114 MB)
 * **Soluzione**: Caricare con inSampleSize per mantenere ~1226×2559 in memoria (~12 MB)
 *
 * **Usage**:
 * ```kotlin
 * val imageBitmap = DrawableLoader.loadDrawableAsBitmap(context, R.drawable.my_image)
 * Image(bitmap = imageBitmap, contentDescription = "")
 * ```
 */
object DrawableLoader {
    private const val TAG = "DrawableLoader"
    private const val MAX_BITMAP_WIDTH = 1226
    private const val MAX_BITMAP_HEIGHT = 2559

    /**
     * Carica un drawable come bitmap con downsampling intelligente.
     *
     * @param context Context Android
     * @param drawableRes Resource ID del drawable
     * @return ImageBitmap scaricato e downsampled
     */
    fun loadDrawableAsBitmap(
        context: Context,
        drawableRes: Int,
    ): ImageBitmap? {
        return try {
            // Decodi dimensions prima (no allocation)
            val dimensionOptions =
                BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }

            val inputStream = context.resources.openRawResource(drawableRes)
            BitmapFactory.decodeStream(inputStream, null, dimensionOptions)
            inputStream.close()

            val originalWidth = dimensionOptions.outWidth
            val originalHeight = dimensionOptions.outHeight

            if (originalWidth <= 0 || originalHeight <= 0) {
                Logger.w(tag = TAG) { "Invalid drawable dimensions: $originalWidth×$originalHeight" }
                return null
            }

            // Calcola inSampleSize
            val inSampleSize = calculateInSampleSize(originalWidth, originalHeight)

            Logger.d(tag = TAG) {
                "Loading drawable: $originalWidth×$originalHeight " +
                    "→ ${originalWidth / inSampleSize}×${originalHeight / inSampleSize} " +
                    "(inSampleSize=$inSampleSize)"
            }

            // Decodifica con downsampling
            val decodeOptions =
                BitmapFactory.Options().apply {
                    this.inSampleSize = inSampleSize
                }

            val inputStream2 = context.resources.openRawResource(drawableRes)
            val bitmap = BitmapFactory.decodeStream(inputStream2, null, decodeOptions)
            inputStream2.close()

            if (bitmap == null) {
                Logger.w(tag = TAG) { "Failed to decode drawable" }
                return null
            }

            // Converte a ImageBitmap per Compose
            return bitmap.asImageBitmap()
        } catch (e: Exception) {
            Logger.e(throwable = e, tag = TAG) { "Error loading drawable" }
            null
        }
    }

    /**
     * Calcola inSampleSize per downsampling intelligente.
     */
    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        maxWidth: Int = MAX_BITMAP_WIDTH,
        maxHeight: Int = MAX_BITMAP_HEIGHT,
    ): Int {
        var inSampleSize = 1

        while ((width / inSampleSize) > maxWidth ||
            (height / inSampleSize) > maxHeight
        ) {
            inSampleSize *= 2
        }

        return inSampleSize
    }
}
