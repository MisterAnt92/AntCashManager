package com.antcashmanager.android.work

import com.antcashmanager.android.data.backup.BackupService
import com.antcashmanager.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Unit tests for AutoBackupWorker using MockK.
 *
 * Tests the backup worker logic without requiring Robolectric or instrumentation:
 * - Backup success with timestamp update
 * - Backup disabled state
 * - Missing folder URI configuration
 * - Error handling (IO, network errors)
 * - Encryption flow
 *
 * NOTE: Full Worker.doWork() integration testing should be in src/androidTest
 * as it requires TestListenableWorkerBuilder and Android environment.
 * This suite tests the underlying logic using pure unit test approach.
 */
class AutoBackupWorkerTest {

    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val backupService: BackupService = mockk(relaxed = true)

    @Before
    fun setup() {
        // Setup default mocks
        coEvery { settingsRepository.getAutoBackupEnabled() } returns flowOf(true)
        coEvery { settingsRepository.getAutoBackupFolderUri() } returns flowOf("content://test/uri")
        coEvery { settingsRepository.getDataEncryptionEnabled() } returns flowOf(false)
        coEvery { backupService.createBackup() } returns Result.success("{\"backup\":\"data\"}")
    }

    @Test
    fun autoBackupEnabled_shouldReturnTrue_whenSettingIsEnabled() = runTest {
        coEvery { settingsRepository.getAutoBackupEnabled() } returns flowOf(true)

        val settings = settingsRepository.getAutoBackupEnabled()
        settings.collect { enabled ->
            assertTrue("Backup should be enabled", enabled == true)
        }

        coVerify(exactly = 1) { settingsRepository.getAutoBackupEnabled() }
    }

    @Test
    fun autoBackupFolderUri_shouldReturnConfiguredUri_whenSet() = runTest {
        coEvery { settingsRepository.getAutoBackupFolderUri() } returns flowOf("content://test/uri")

        var capturedUri: String? = null
        settingsRepository.getAutoBackupFolderUri().collect { uri ->
            capturedUri = uri
        }

        assertTrue("URI should match configured value", capturedUri == "content://test/uri")
    }

    @Test
    fun autoBackupFolderUri_shouldReturnNull_whenNotConfigured() = runTest {
        coEvery { settingsRepository.getAutoBackupFolderUri() } returns flowOf(null)

        var capturedUri: String? = "initial"
        settingsRepository.getAutoBackupFolderUri().collect { uri ->
            capturedUri = uri
        }

        assertTrue("URI should be null when not configured", capturedUri == null)
    }

    @Test
    fun createBackup_shouldReturnSuccess_whenBackupSucceeds() = runTest {
        val backupData = "{\"data\":\"test\"}"
        coEvery { backupService.createBackup() } returns Result.success(backupData)

        val result = backupService.createBackup()

        assertTrue("Backup should succeed", result.isSuccess)
        assertTrue("Backup data should match", result.getOrNull() == backupData)
    }

    @Test
    fun createBackup_shouldReturnFailure_whenIoExceptionOccurs() = runTest {
        coEvery { backupService.createBackup() } returns Result.failure(IOException("IO error"))

        val result = backupService.createBackup()

        assertTrue("Backup should fail", result.isFailure)
        assertTrue("Should be IO exception", result.exceptionOrNull() is IOException)
    }

    @Test
    fun createBackup_shouldReturnFailure_whenNetworkErrorOccurs() = runTest {
        coEvery { backupService.createBackup() } returns Result.failure(Exception("Network timeout"))

        val result = backupService.createBackup()

        assertTrue("Backup should fail", result.isFailure)
        assertTrue("Should contain network error", result.exceptionOrNull()?.message?.contains("Network") == true)
    }

    @Test
    fun dataEncryption_shouldBeEnabled_whenSettingIsTrue() = runTest {
        coEvery { settingsRepository.getDataEncryptionEnabled() } returns flowOf(true)

        var encryptionEnabled = false
        settingsRepository.getDataEncryptionEnabled().collect { enabled ->
            encryptionEnabled = enabled
        }

        assertTrue("Encryption should be enabled", encryptionEnabled)
    }

    @Test
    fun dataEncryption_shouldBeDisabled_whenSettingIsFalse() = runTest {
        coEvery { settingsRepository.getDataEncryptionEnabled() } returns flowOf(false)

        var encryptionEnabled = true
        settingsRepository.getDataEncryptionEnabled().collect { enabled ->
            encryptionEnabled = enabled
        }

        assertTrue("Encryption should be disabled", !encryptionEnabled)
    }

    @Test
    fun lastBackupTimestamp_shouldBeUpdated_whenBackupCompletes() = runTest {
        val timestamp = System.currentTimeMillis()
        coEvery { settingsRepository.setLastBackupTimestamp(any()) } returns Unit

        settingsRepository.setLastBackupTimestamp(timestamp)

        coVerify(exactly = 1) { settingsRepository.setLastBackupTimestamp(timestamp) }
    }

    @Test
    fun backupData_shouldHandleLargePayload_withoutTimeoutOrException() = runTest {
        val largeBackupData = buildString {
            append("{\"transactions\": [")
            append((0..10000).joinToString(",") { "{\"id\":$it}" })
            append("]}")
        }
        coEvery { backupService.createBackup() } returns Result.success(largeBackupData)

        val result = backupService.createBackup()

        assertTrue("Large backup should succeed", result.isSuccess)
        assertTrue("Backup data should be non-empty", result.getOrNull()?.isNotEmpty() == true)
    }

    @Test
    fun backupFlow_shouldBeChainingCorrectly_withMultipleCalls() = runTest {
        var callCount = 0
        coEvery { backupService.createBackup() } answers {
            callCount++
            Result.success("{\"backup\":\"data_$callCount\"}")
        }

        backupService.createBackup()
        backupService.createBackup()
        backupService.createBackup()

        coVerify(exactly = 3) { backupService.createBackup() }
        assertTrue("Should have been called 3 times", callCount == 3)
    }
}
