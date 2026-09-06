package com.antcashmanager.android.analytics.tracker

import android.os.Bundle
import com.antcashmanager.android.analytics.AnalyticsManager

/**
 * Tracker per errori e fallimenti dell'applicazione.
 *
 * Traccia:
 * - Form validation errors (missing fields, invalid formats)
 * - Database errors (query failures, schema mismatches)
 * - Sync errors (backup, restore, Google Drive)
 * - Payment errors (invalid payment method, processing failures)
 * - OCR errors (image processing, text extraction failures)
 *
 * Integrazione:
 * - ViewModel: wrap try/catch blocks
 * - Repository: catch database exceptions
 * - BackupWorker: catch sync failures
 * - PaymentProcessor: catch payment errors
 * - ReceiptScanner: catch OCR errors
 *
 * Privacy: Solo error codes e types, nessun stack trace o user data
 */
class ErrorTracker(
    private val analyticsManager: AnalyticsManager,
) {
    /**
     * Traccia un errore di validazione del form di transazione.
     *
     * Usare in AddTransactionViewModel.submitTransaction() nei casi di fallimento validazione
     *
     * @param fieldName Nome del campo che ha fallito (es: "title", "amount", "category")
     * @param errorCode Codice di errore (es: "missing_field", "invalid_format", "out_of_range")
     */
    fun trackTransactionValidationError(
        fieldName: String,
        errorCode: String,
    ) {
        analyticsManager.logEvent(
            "transaction_validation_error",
            Bundle().apply {
                putString("field_name", fieldName)
                putString("error_code", errorCode)
            },
        )
    }

    /**
     * Traccia un errore di estrazione OCR nelle ricevute.
     *
     * Usare in ReceiptScanViewModel quando l'OCR fallisce
     *
     * @param errorCode Codice di errore (es: "invalid_image", "text_not_found", "network_timeout")
     * @param retryCount Numero di retry effettuati
     */
    fun trackReceiptOcrError(
        errorCode: String,
        retryCount: Int = 0,
    ) {
        analyticsManager.logEvent(
            "receipt_ocr_error",
            Bundle().apply {
                putString("error_code", errorCode)
                putInt("retry_count", retryCount)
            },
        )
    }

    /**
     * Traccia un errore di database.
     *
     * Usare in Repository layer quando le query falliscono
     *
     * @param operation Nome dell'operazione (es: "insert", "update", "delete", "query")
     * @param errorCode Codice di errore (es: "constraint_violation", "schema_mismatch", "access_denied")
     */
    fun trackDatabaseError(
        operation: String,
        errorCode: String,
    ) {
        analyticsManager.logEvent(
            "database_error",
            Bundle().apply {
                putString("operation", operation)
                putString("error_code", errorCode)
            },
        )
    }

    /**
     * Traccia un errore di sincronizzazione (backup/restore).
     *
     * Usare in BackupWorker o SettingsDataViewModel durante operazioni di sync
     *
     * @param operation Tipo di operazione (es: "backup_local", "backup_google_drive", "restore_local")
     * @param errorCode Codice di errore (es: "permission_denied", "network_error", "token_expired")
     */
    fun trackSyncError(
        operation: String,
        errorCode: String,
    ) {
        analyticsManager.logEvent(
            "sync_error",
            Bundle().apply {
                putString("operation", operation)
                putString("error_code", errorCode)
            },
        )
    }

    /**
     * Traccia un errore nel processing del pagamento.
     *
     * Usare in PaymentProcessor o transaction submission quando il pagamento fallisce
     *
     * @param paymentType Tipo di pagamento (es: "ELECTRONIC", "CASH", "MEAL_VOUCHERS")
     * @param errorCode Codice di errore (es: "invalid_amount", "method_not_available", "processing_failed")
     */
    fun trackPaymentMethodError(
        paymentType: String,
        errorCode: String,
    ) {
        analyticsManager.logEvent(
            "payment_method_error",
            Bundle().apply {
                putString("payment_type", paymentType)
                putString("error_code", errorCode)
            },
        )
    }
}
