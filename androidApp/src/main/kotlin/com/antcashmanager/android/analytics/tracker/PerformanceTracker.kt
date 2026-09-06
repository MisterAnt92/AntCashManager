package com.antcashmanager.android.analytics.tracker

import android.os.Bundle
import com.antcashmanager.android.analytics.AnalyticsManager

/**
 * Tracker per metriche di performance dell'applicazione.
 *
 * Traccia:
 * - Screen load times (NavGraph)
 * - Operation latencies (form submission, chart rendering, OCR, backup)
 * - Memory/CPU metrics (quando disponibili)
 *
 * Integrazione:
 * - NavGraph: wrap LaunchedEffect per screen load time
 * - ViewModel: wrap operazioni asincrone per latency
 * - BackupWorker: track backup/restore duration
 *
 * Privacy: Solo metriche anonime, nessun dato sensibile
 */
class PerformanceTracker(private val analyticsManager: AnalyticsManager) {

    /**
     * Traccia il tempo di caricamento di una schermata.
     *
     * @param screenName Nome della schermata (es: "home_screen", "transactions_screen")
     * @param durationMs Durata di caricamento in millisecondi
     */
    fun trackScreenLoadTime(screenName: String, durationMs: Long) {
        analyticsManager.logEvent("screen_load_time", Bundle().apply {
            putString("screen", screenName)
            putLong("duration_ms", durationMs)
        })
    }

    /**
     * Traccia la latenza di invio del form di transazione.
     *
     * @param durationMs Durata di elaborazione in millisecondi
     * @param hasReceipt Se la transazione include una ricevuta scansionata
     */
    fun trackTransactionFormSubmitLatency(durationMs: Long, hasReceipt: Boolean = false) {
        analyticsManager.logEvent("transaction_form_submit_latency", Bundle().apply {
            putLong("duration_ms", durationMs)
            putBoolean("has_receipt", hasReceipt)
        })
    }

    /**
     * Traccia il tempo di rendering dei grafici.
     *
     * @param durationMs Durata di rendering in millisecondi
     * @param dataPoints Numero di punti dati renderizzati
     */
    fun trackChartRenderingTime(durationMs: Long, dataPoints: Int = 0) {
        analyticsManager.logEvent("chart_rendering_time", Bundle().apply {
            putLong("duration_ms", durationMs)
            putInt("data_points", dataPoints)
        })
    }

    /**
     * Traccia il tempo di processing OCR per la scansione ricevute.
     *
     * @param durationMs Durata di processing in millisecondi
     * @param pages Numero di pagine processate
     * @param success Se il processing è riuscito
     */
    fun trackReceiptOcrProcessingTime(durationMs: Long, pages: Int = 1, success: Boolean = true) {
        analyticsManager.logEvent("receipt_ocr_processing_time", Bundle().apply {
            putLong("duration_ms", durationMs)
            putInt("pages", pages)
            putBoolean("success", success)
        })
    }

    /**
     * Traccia la durata di un'operazione di backup/restore.
     *
     * @param durationMs Durata dell'operazione in millisecondi
     * @param fileSizeMb Dimensione del file in MB
     * @param operation Tipo di operazione ("backup" o "restore")
     */
    fun trackBackupRestoreDuration(durationMs: Long, fileSizeMb: Double = 0.0, operation: String = "backup") {
        analyticsManager.logEvent("backup_restore_duration", Bundle().apply {
            putLong("duration_ms", durationMs)
            putDouble("file_size_mb", fileSizeMb)
            putString("operation", operation)
        })
    }
}