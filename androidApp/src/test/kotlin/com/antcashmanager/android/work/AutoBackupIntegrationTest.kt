package com.antcashmanager.android.work

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.analytics.tracker.ErrorTracker
import com.antcashmanager.android.auth.GoogleSignInManager
import com.antcashmanager.android.data.backup.BackupService
import com.antcashmanager.android.drive.DriveUploadManager
import com.antcashmanager.android.security.BackupPayloadCipher
import com.antcashmanager.domain.model.BackupDestination
import com.antcashmanager.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.annotation.Config

/**
 * Integration tests for AutoBackup end-to-end flows.
 *
 * Verifies:
 * - Complete backup workflow (enabled, folder configured, encryption)
 * - Backup immediately after restore
 * - Multiple backup cycles
 * - Error recovery and retry logic
 */
@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class AutoBackupIntegrationTest {
    private lateinit var context: Context
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val backupService: BackupService = mockk(relaxed = true)
    private val driveUploadManager: DriveUploadManager = mockk(relaxed = true)

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        stopKoin()
        startKoin {
            modules(
                module {
                    single { settingsRepository }
                    single { backupService }
                    single { mockk<GoogleSignInManager>(relaxed = true) }
                    single { driveUploadManager }
                    single { mockk<AnalyticsManager>(relaxed = true) }
                    single { mockk<ErrorTracker>(relaxed = true) }
                },
            )
        }

        // Setup default successful backup flow
        every { settingsRepository.getAutoBackupDestination() } returns flowOf(BackupDestination.GOOGLE_DRIVE)
        coEvery { settingsRepository.getAutoBackupEnabled() } returns flowOf(true)
        coEvery { settingsRepository.getAutoBackupFolderUri() } returns flowOf("content://backup/uri")
        coEvery { settingsRepository.getDataEncryptionEnabled() } returns flowOf(false)
        coEvery { settingsRepository.setLastBackupTimestamp(any()) } returns Unit
        coEvery { backupService.createBackup() } returns Result.success("{\"backup\":\"data\"}")
        coEvery { driveUploadManager.ensureFolderExists() } returns Result.success("backup-folder")
        coEvery { driveUploadManager.uploadBackupFile(any(), any(), any(), any()) } returns Result.success("backup-file")
        mockkObject(BackupPayloadCipher)
        every { BackupPayloadCipher.encrypt(any()) } returns "encrypted_backup"
    }

    @After
    fun tearDown() {
        stopKoin()
        unmockkObject(BackupPayloadCipher)
    }

    @Test
    fun completeBackupWorkflow_enabled_encrypted_succeeds() =
        runTest {
            coEvery { settingsRepository.getDataEncryptionEnabled() } returns flowOf(true)

            val worker = TestListenableWorkerBuilder<AutoBackupWorker>(context).build()
            val result = worker.doWork()

            assert(result == ListenableWorker.Result.success())
        }

    @Test
    fun backupImmediatelyAfterRestore_usesCurrentData() =
        runTest {
            // Simulate: restore just happened, then backup runs
            val restoredBackupData = "{\"restored\":true}"
            val newBackupData = "{\"current\":\"data\"}"

            coEvery { backupService.createBackup() }
                .returnsMany(
                    Result.success(restoredBackupData),
                    Result.success(newBackupData),
                )

            val worker1 = TestListenableWorkerBuilder<AutoBackupWorker>(context).build()
            val result1 = worker1.doWork()

            val worker2 = TestListenableWorkerBuilder<AutoBackupWorker>(context).build()
            val result2 = worker2.doWork()

            assert(result1 == ListenableWorker.Result.success())
            assert(result2 == ListenableWorker.Result.success())
        }

    @Test
    fun multipleBupCycles_eachUpdatesTimestamp() =
        runTest {
            val timestamps = mutableListOf<Long>()

            coEvery { settingsRepository.setLastBackupTimestamp(capture(timestamps)) } returns Unit

            // Run backup 3 times
            repeat(3) {
                val worker = TestListenableWorkerBuilder<AutoBackupWorker>(context).build()
                val result = worker.doWork()
                assert(result == ListenableWorker.Result.success())
            }

            // Each should have recorded a timestamp
            assert(timestamps.size == 3)
            // Timestamps should be in ascending order
            assert(timestamps[0] <= timestamps[1])
            assert(timestamps[1] <= timestamps[2])
        }

    @Test
    fun errorRecoveryFlow_ioError_thenSuccess() =
        runTest {
            coEvery { backupService.createBackup() }
                .returnsMany(
                    Result.failure(java.io.IOException("IO error")),
                    Result.success("{\"retry\":\"success\"}"),
                )

            val worker1 = TestListenableWorkerBuilder<AutoBackupWorker>(context).build()
            val result1 = worker1.doWork()

            // First attempt should retry
            assert(result1 == ListenableWorker.Result.retry())

            val worker2 = TestListenableWorkerBuilder<AutoBackupWorker>(context).build()
            val result2 = worker2.doWork()

            // Second attempt should succeed
            assert(result2 == ListenableWorker.Result.success())
        }

    @Test
    fun backupWithUriPermissionRevoked_gracefullyFails() =
        runTest {
            every { settingsRepository.getAutoBackupDestination() } returns flowOf(BackupDestination.LOCAL)
            coEvery { settingsRepository.getAutoBackupFolderUri() } returns flowOf("content://revoked/uri")
            coEvery { backupService.createBackup() } returns Result.success("{\"data\":\"test\"}")

            val worker = TestListenableWorkerBuilder<AutoBackupWorker>(context).build()
            val result = worker.doWork()

            // Should fail or retry when URI is invalid
            assert(result == ListenableWorker.Result.failure() || result == ListenableWorker.Result.retry())
        }

    @Test
    fun disabledThenEnabledBackup_respectsLatestState() =
        runTest {
            // First: disabled
            coEvery { settingsRepository.getAutoBackupEnabled() } returns flowOf(false)

            var worker = TestListenableWorkerBuilder<AutoBackupWorker>(context).build()
            var result = worker.doWork()

            // Should succeed without backing up
            assert(result == ListenableWorker.Result.success())

            // Second: enabled
            coEvery { settingsRepository.getAutoBackupEnabled() } returns flowOf(true)
            coEvery { backupService.createBackup() } returns Result.success("{\"data\":\"test\"}")

            worker = TestListenableWorkerBuilder<AutoBackupWorker>(context).build()
            result = worker.doWork()

            // Should perform backup
            assert(result == ListenableWorker.Result.success())
        }
}
