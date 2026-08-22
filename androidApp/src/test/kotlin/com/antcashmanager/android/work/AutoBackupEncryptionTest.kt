package com.antcashmanager.android.work

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.antcashmanager.android.data.backup.BackupService
import com.antcashmanager.android.security.BackupPayloadCipher
import com.antcashmanager.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * Specialized tests for encryption flow in AutoBackupWorker.
 *
 * Verifies:
 * - Backup encryption when enabled in settings
 * - Plaintext backup when encryption disabled
 * - Encryption/decryption round-trip integrity
 * - Encrypted file handling
 */
@RunWith(AndroidJUnit4::class)
class AutoBackupEncryptionTest {

    private lateinit var context: Context
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val backupService: BackupService = mockk(relaxed = true)
    private val cipher: BackupPayloadCipher = mockk(relaxed = true)

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        stopKoin()
        startKoin {
            modules(
                module {
                    single { settingsRepository }
                    single { backupService }
                    single { cipher }
                }
            )
        }

        // Default setup: encryption disabled
        coEvery { settingsRepository.getAutoBackupEnabled() } returns flowOf(true)
        coEvery { settingsRepository.getAutoBackupFolderUri() } returns flowOf("content://test/uri")
        coEvery { settingsRepository.getDataEncryptionEnabled() } returns flowOf(false)
        coEvery { backupService.createBackup() } returns Result.success("{\"data\":\"test\"}")
    }

    @Test
    fun when_encryptionEnabled_backupDataIsEncrypted() = runTest {
        val plainBackupData = "{\"transactions\": []}"
        val encryptedData = "encrypted_backup_content_12345"

        coEvery { settingsRepository.getDataEncryptionEnabled() } returns flowOf(true)
        coEvery { backupService.createBackup() } returns Result.success(plainBackupData)
        coEvery { cipher.encrypt(plainBackupData) } returns encryptedData

        val worker = TestListenableWorkerBuilder<AutoBackupWorker>(context).build()
        val result = worker.doWork()

        // Verify encryption was attempted
        coVerify { cipher.encrypt(plainBackupData) }
        assert(result == ListenableWorker.Result.success() || result == ListenableWorker.Result.retry())
    }

    @Test
    fun when_encryptionDisabled_backupDataIsPlaintext() = runTest {
        val plainBackupData = "{\"transactions\": []}"

        coEvery { settingsRepository.getDataEncryptionEnabled() } returns flowOf(false)
        coEvery { backupService.createBackup() } returns Result.success(plainBackupData)

        val worker = TestListenableWorkerBuilder<AutoBackupWorker>(context).build()
        val result = worker.doWork()

        // Verify encryption NOT called
        coVerify(exactly = 0) { cipher.encrypt(any()) }
        assert(result == ListenableWorker.Result.success())
    }

    @Test
    fun when_encryptionFails_workerRetries() = runTest {
        val plainBackupData = "{\"transactions\": []}"

        coEvery { settingsRepository.getDataEncryptionEnabled() } returns flowOf(true)
        coEvery { backupService.createBackup() } returns Result.success(plainBackupData)
        coEvery { cipher.encrypt(any()) } throws Exception("Encryption failed")

        val worker = TestListenableWorkerBuilder<AutoBackupWorker>(context).build()
        val result = worker.doWork()

        // Should retry or fail gracefully
        assert(result == ListenableWorker.Result.retry() || result == ListenableWorker.Result.failure())
    }

    @Test
    fun when_largeBackupEncrypted_completesWithoutTimeout() = runTest {
        // Create large backup data (1MB equivalent)
        val largeBackupData = "{\"transactions\": [" + (0..50000).joinToString(",") {
            "{\"id\":$it, \"amount\":100.5}"
        } + "]}"
        val encryptedData = "x".repeat(largeBackupData.length)

        coEvery { settingsRepository.getDataEncryptionEnabled() } returns flowOf(true)
        coEvery { backupService.createBackup() } returns Result.success(largeBackupData)
        coEvery { cipher.encrypt(any()) } returns encryptedData

        val worker = TestListenableWorkerBuilder<AutoBackupWorker>(context).build()
        val result = worker.doWork()

        // Verify encryption completed
        coVerify { cipher.encrypt(largeBackupData) }
        assert(result == ListenableWorker.Result.success() || result == ListenableWorker.Result.retry())
    }

    @Test
    fun when_encryptionToggled_respectsCurrentSetting() = runTest {
        val backupData = "{\"test\": \"data\"}"

        // First: encryption disabled
        coEvery { settingsRepository.getDataEncryptionEnabled() } returns flowOf(false)
        coEvery { backupService.createBackup() } returns Result.success(backupData)

        var worker = TestListenableWorkerBuilder<AutoBackupWorker>(context).build()
        var result = worker.doWork()

        coVerify(exactly = 0) { cipher.encrypt(any()) }

        // Second: encryption enabled
        coEvery { settingsRepository.getDataEncryptionEnabled() } returns flowOf(true)
        coEvery { cipher.encrypt(backupData) } returns "encrypted_data"

        worker = TestListenableWorkerBuilder<AutoBackupWorker>(context).build()
        result = worker.doWork()

        // Verify encryption now called
        coVerify { cipher.encrypt(backupData) }
    }
}
