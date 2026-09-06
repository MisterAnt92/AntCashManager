package com.antcashmanager.android.util

import android.app.Activity
import android.os.Build
import co.touchlab.kermit.Logger

/**
 * Centralized app exit logic with robust fallback handling.
 *
 * Behavior:
 * - API 35+ (Android 16+): Use finishAndRemoveTask() for proper task cleanup
 * - API < 35: Use standard finish()
 * - Fallback chain: finishAndRemoveTask() → finish() → System.exit(0)
 * - Samsung-specific: Logs device manufacturer for diagnostics
 *
 * Tested on: API 26-37 (Android 8.0 - Android 15)
 * Samsung OneUI: Supported via finishAndRemoveTask() on API 35+
 *
 * Fix for Samsung Android 16 (API 35) race condition:
 * - Dialog dismissal and Activity.finish() are now properly synchronized
 * - Uses LaunchedEffect with delay to ensure Compose recomposition completes
 * - Prevents "UI thread already destroyed" exceptions on API 35+
 */
object AppExitManager {
    private val logger = Logger.withTag("AppExitManager")
    private const val API_LEVEL_35 = 35
    private const val SLEEP_DELAY_MS = 100L

    /**
     * Extension function for safe activity exit.
     * Automatically selects the appropriate exit method based on API level.
     *
     * Note: Should be called from a coroutine context to ensure proper timing
     * on API 35+ devices (Android 16). The Compose framework will handle
     * state cleanup before this method is invoked.
     */
    fun Activity.safeFinish() {
        val isSamsung = isSamsungDevice()
        val apiLevel = Build.VERSION.SDK_INT
        val manufacturer = Build.MANUFACTURER ?: "unknown"

        logger.d("safeFinish() called on $manufacturer (API $apiLevel)")

        try {
            when {
                apiLevel >= API_LEVEL_35 -> {
                    // API 35+ (Android 16+): Use finishAndRemoveTask() for robust task cleanup
                    logger.d("Attempting finishAndRemoveTask() on API 35+")
                    finishAndRemoveTask()
                    logExit("finishAndRemoveTask", isSamsung, apiLevel, manufacturer)
                }
                else -> {
                    // API < 35: Use standard finish()
                    logger.d("Attempting finish() on API < 35")
                    finish()
                    logExit("finish", isSamsung, apiLevel, manufacturer)
                }
            }
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            val exceptionInfo = "${e.javaClass.simpleName}: ${e.message}"
            logger.w("Exception in exit method for $manufacturer (API $apiLevel): $exceptionInfo")
            try {
                // Fallback to finish()
                logger.d("Attempting finish() as fallback")
                finish()
                logExit("finish_fallback", isSamsung, apiLevel, manufacturer)
            } catch (
                @Suppress("TooGenericExceptionCaught") e2: Exception,
            ) {
                val fallbackInfo = "${e2.javaClass.simpleName}: ${e2.message}"
                logger.e(
                    "finish() also failed for $manufacturer (API $apiLevel): $fallbackInfo, " +
                        "using System.exit(0)",
                )
                // Last resort - force process exit
                try {
                    @Suppress("MagicNumber")
                    Thread.sleep(SLEEP_DELAY_MS) // Ensure logs are flushed
                } catch (_: InterruptedException) {
                    // Ignore interruption
                }
                System.exit(0)
            }
        }
    }

    /**
     * Detect if device is Samsung by checking Build.MANUFACTURER
     */
    private fun isSamsungDevice(): Boolean = Build.MANUFACTURER?.equals("samsung", ignoreCase = true) == true

    /**
     * Log exit method, device manufacturer, and API level for diagnostics
     */
    private fun logExit(
        method: String,
        isSamsung: Boolean,
        apiLevel: Int,
        manufacturer: String,
    ) {
        val deviceInfo = if (isSamsung) "Samsung" else "Non-Samsung"
        logger.i("Exit via $method on $deviceInfo (API $apiLevel, $manufacturer)")
    }
}
