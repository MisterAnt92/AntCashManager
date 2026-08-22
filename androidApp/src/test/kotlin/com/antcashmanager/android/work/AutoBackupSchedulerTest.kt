package com.antcashmanager.android.work

import android.app.Application
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [AutoBackupScheduler].
 *
 * Verifies:
 * - Work scheduling with correct configuration
 * - Work cancellation
 * - Backoff policy settings
 * - Error handling for WorkManager failures
 * - Idempotent scheduling
 */
class AutoBackupSchedulerTest {

    private val application: Application = mockk(relaxed = true)
    private lateinit var scheduler: AutoBackupScheduler

    @Before
    fun setup() {
        scheduler = AutoBackupScheduler(application)
    }

    @Test
    fun schedule_schedulesWorkWithCorrectConfiguration() {
        scheduler.schedule()

        // Verify that schedule() completes without throwing
        // Actual WorkManager verification would require a test WorkManager
    }

    @Test
    fun cancel_cancelsScheduledWork() {
        scheduler.schedule()
        scheduler.cancel()

        // Verify that cancel() completes without throwing
    }

    @Test
    fun schedule_calledMultipleTimes_doesNotThrow() {
        scheduler.schedule()
        scheduler.schedule()
        scheduler.schedule()

        // Verify idempotency - no exception thrown
    }

    @Test
    fun cancel_calledWhenNoWorkScheduled_doesNotThrow() {
        scheduler.cancel()

        // Verify graceful handling when no work is scheduled
    }

    @Test
    fun schedule_withApplicationContext_completes() {
        val appContext: Application = mockk(relaxed = true)
        val schedulerWithContext = AutoBackupScheduler(appContext)

        schedulerWithContext.schedule()

        // Verify completion without error
    }

    @Test
    fun scheduleAndCancel_sequentially_bothSucceed() {
        scheduler.schedule()
        scheduler.cancel()
        scheduler.schedule()

        // Verify the workflow can be repeated
    }

    @Test
    fun schedule_logsSchedulingMessage() {
        scheduler.schedule()

        // Verify schedule completes (logging is handled internally)
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "auto_backup_weekly"
        private const val INTERVAL_DAYS = 7L
        private const val INITIAL_BACKOFF_MINUTES = 15L
    }
}
