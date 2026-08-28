package com.antcashmanager.android.work

import android.app.Application
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import co.touchlab.kermit.Logger
import java.util.concurrent.TimeUnit

/**
 * Gestisce lo scheduling del backup automatico settimanale via WorkManager.
 *
 * Il lavoro persiste attraverso i riavvii tramite il DB interno di WorkManager.
 * Non è necessario ri-schedulare ad ogni Application.onCreate().
 *
 * Uso:
 * - [schedule()]: Attiva il backup settimanale (o aggiorna se già in corso)
 * - [cancel()]: Annulla il backup settimanale
 *
 * Questa classe è iniettata in Koin come singleton per restare mockabile nei test ViewModel.
 */
class AutoBackupScheduler(
    private val application: Application,
) {

    companion object {
        private const val UNIQUE_WORK_NAME = "auto_backup_weekly"
        private const val BACKUP_INTERVAL_DAYS = 7L
        private const val INITIAL_BACKOFF_MINUTES = 15L
    }

    /**
     * Schedula il backup automatico settimanale.
     * Se già schedulato, aggiorna la configurazione (ExistingPeriodicWorkPolicy.UPDATE).
     */
    fun schedule() {
        try {
            Logger.d(tag = "AutoBackupScheduler") { "Scheduling auto backup (7-day interval)" }

            val backupWorkRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(
                BACKUP_INTERVAL_DAYS,
                TimeUnit.DAYS,
            )
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    INITIAL_BACKOFF_MINUTES,
                    TimeUnit.MINUTES,
                )
                .build()

            WorkManager.getInstance(application).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                backupWorkRequest,
            )

            Logger.d(tag = "AutoBackupScheduler") { "Auto backup scheduled successfully" }
        } catch (e: Exception) {
            Logger.e(tag = "AutoBackupScheduler") { "Error scheduling auto backup: ${e.message}" }
        }
    }

    /**
     * Cancella il backup automatico schedulato.
     */
    fun cancel() {
        try {
            Logger.d(tag = "AutoBackupScheduler") { "Cancelling auto backup" }
            WorkManager.getInstance(application).cancelUniqueWork(UNIQUE_WORK_NAME)
            Logger.d(tag = "AutoBackupScheduler") { "Auto backup cancelled successfully" }
        } catch (e: Exception) {
            Logger.e(tag = "AutoBackupScheduler") { "Error cancelling auto backup: ${e.message}" }
        }
    }
}
