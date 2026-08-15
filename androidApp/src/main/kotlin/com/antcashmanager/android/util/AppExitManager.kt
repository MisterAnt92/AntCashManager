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
 */
object AppExitManager {

    private val logger = Logger.withTag("AppExitManager")

    /**
     * Extension function for safe activity exit.
     * Automatically selects the appropriate exit method based on API level.
     */
    fun Activity.safeFinish() {
        val isSamsung = isSamsungDevice()
        val apiLevel = Build.VERSION.SDK_INT
        val manufacturer = Build.MANUFACTURER ?: "unknown"

        try {
            when {
                apiLevel >= 35 -> {
                    // API 35+ (Android 16+): Use finishAndRemoveTask() for robust task cleanup
                    finishAndRemoveTask()
                    logExit("finishAndRemoveTask", isSamsung, apiLevel, manufacturer)
                }
                else -> {
                    // API < 35: Use standard finish()
                    finish()
                    logExit("finish", isSamsung, apiLevel, manufacturer)
                }
            }
        } catch (e: Exception) {
            logger.w("finishAndRemoveTask() failed for $manufacturer (API $apiLevel): ${e.message}")
            try {
                // Fallback to finish()
                finish()
                logExit("finish_fallback", isSamsung, apiLevel, manufacturer)
            } catch (e2: Exception) {
                logger.e("finish() also failed for $manufacturer (API $apiLevel): ${e2.message}, using System.exit(0)")
                // Last resort
                System.exit(0)
            }
        }
    }

    /**
     * Detect if device is Samsung by checking Build.MANUFACTURER
     */
    private fun isSamsungDevice(): Boolean {
        return Build.MANUFACTURER?.equals("samsung", ignoreCase = true) == true
    }

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
