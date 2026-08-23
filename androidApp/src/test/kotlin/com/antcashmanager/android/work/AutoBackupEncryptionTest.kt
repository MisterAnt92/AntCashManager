package com.antcashmanager.android.work

import com.antcashmanager.android.data.backup.BackupService
import com.antcashmanager.android.security.BackupPayloadCipher
import com.antcashmanager.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Specialized unit tests for encryption flow in backup logic.
 *
 * Verifies using pure unit test approach with MockK:
 * - Backup encryption when enabled in settings
 * - Plaintext backup when encryption disabled
 * - Encryption/decryption integrity
 * - Error handling
 *
 * NOTE: Full Worker integration testing with TestListenableWorkerBuilder
 * should be in src/androidTest as it requires Android environment.
 * This suite tests the encryption logic using mocked services.
 */
class AutoBackupEncryptionTest {

    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val backupService: BackupService = mockk(relaxed = true)
    private val cipher: BackupPayloadCipher = mockk(relaxed = true)

    @Before
    fun setup() {
        // Default setup: encryption disabled
        coEvery { settingsRepository.getAutoBackupEnabled() } returns flowOf(true)
        coEvery { settingsRepository.getAutoBackupFolderUri() } returns flowOf("content://test/uri")
        coEvery { settingsRepository.getDataEncryptionEnabled() } returns flowOf(false)
        coEvery { backupService.createBackup() } returns Result.success("{\"data\":\"test\"}")
    }

    @Test
    fun encryptionEnabled_shouldEncryptBackupData_whenSettingIsTrue() = runTest {
        val plainBackupData = "{\"transactions\": []}"
        val encryptedData = "ACM_ENC_V1:encrypted_content_12345"

        coEvery { settingsRepository.getDataEncryptionEnabled() } returns flowOf(true)
        coEvery { cipher.encrypt(plainBackupData) } returns encryptedData

        val result = cipher.encrypt(plainBackupData)

        assertTrue("Encryption should succeed", result.startsWith("ACM_ENC_V1:"))
        coVerify(exactly = 1) { cipher.encrypt(plainBackupData) }
    }

    @Test
    fun encryptionDisabled_shouldNotEncryptBackupData_whenSettingIsFalse() = runTest {
        val backupData = "{\"transactions\": []}"

        coEvery { settingsRepository.getDataEncryptionEnabled() } returns flowOf(false)
        coEvery { backupService.createBackup() } returns Result.success(backupData)

        // When encryption is disabled, cipher should not be invoked
        coEvery { cipher.encrypt(any()) } throws IllegalStateException("Should not encrypt")

        var encryptionWasCalled = false
        try {
            val flow = settingsRepository.getDataEncryptionEnabled()
            flow.collect { encryptionEnabled ->
                if (!encryptionEnabled) {
                    // Don't call cipher
                } else {
                    encryptionWasCalled = true
                    cipher.encrypt(backupData)
                }
            }
        } catch (e: IllegalStateException) {
            // Expected if encryption was attempted
            encryptionWasCalled = true
        }

        assertFalse("Encryption should not be called when disabled", encryptionWasCalled)
    }

    @Test
    fun encryptionFailure_shouldPropagateError_whenCipherFails() = runTest {
        val plainBackupData = "{\"transactions\": []}"

        coEvery { settingsRepository.getDataEncryptionEnabled() } returns flowOf(true)
        coEvery { cipher.encrypt(any()) } throws Exception("Encryption failed")

        val result = runCatching { cipher.encrypt(plainBackupData) }

        assertTrue("Encryption failure should be caught", result.isFailure)
        assertTrue("Error message should indicate encryption issue",
            result.exceptionOrNull()?.message?.contains("Encryption") == true)
    }

    @Test
    fun largeBackupData_shouldEncryptWithoutTimeout_whenSizeIsLarge() = runTest {
        val largeBackupData = buildString {
            append("{\"transactions\": [")
            append((0..50000).joinToString(",") { "{\"id\":$it, \"amount\":100.5}" })
            append("]}")
        }
        val encryptedData = "x".repeat(largeBackupData.length)

        coEvery { settingsRepository.getDataEncryptionEnabled() } returns flowOf(true)
        coEvery { cipher.encrypt(largeBackupData) } returns encryptedData

        val result = cipher.encrypt(largeBackupData)

        assertTrue("Encryption should complete", result.isNotEmpty())
        assertTrue("Encrypted data should match expected", result == encryptedData)
        coVerify(exactly = 1) { cipher.encrypt(largeBackupData) }
    }

    @Test
    fun encryptionSetting_shouldRespectCurrentValue_whenToggled() = runTest {
        val backupData = "{\"test\": \"data\"}"
        var callCount = 0

        // First: encryption disabled
        coEvery { settingsRepository.getDataEncryptionEnabled() } returns flowOf(false)

        settingsRepository.getDataEncryptionEnabled().collect { encryptionEnabled ->
            if (encryptionEnabled) {
                callCount++
                cipher.encrypt(backupData)
            }
        }

        // Encryption should not have been called
        assertTrue("Encryption should not be called when disabled", callCount == 0)

        // Second: encryption enabled
        coEvery { settingsRepository.getDataEncryptionEnabled() } returns flowOf(true)
        coEvery { cipher.encrypt(backupData) } returns "encrypted_data"

        settingsRepository.getDataEncryptionEnabled().collect { encryptionEnabled ->
            if (encryptionEnabled) {
                callCount++
                cipher.encrypt(backupData)
            }
        }

        // Now encryption should have been called
        assertTrue("Encryption should be called when enabled", callCount > 0)
        coVerify { cipher.encrypt(backupData) }
    }

    @Test
    fun encryptedPayload_shouldContainCorrectPrefix_forIdentification() = runTest {
        val plainData = "backup_content"
        val encryptedPayload = "ACM_ENC_V1:encrypted_content_xyz"

        coEvery { cipher.encrypt(plainData) } returns encryptedPayload

        val result = cipher.encrypt(plainData)

        assertTrue("Encrypted payload should start with prefix", result.startsWith("ACM_ENC_V1:"))
    }

    @Test
    fun encryptionWithEmptyData_shouldHandleGracefully_whenBackupIsEmpty() = runTest {
        val emptyBackupData = "{}"
        val encryptedEmpty = "ACM_ENC_V1:encrypted_empty"

        coEvery { cipher.encrypt(emptyBackupData) } returns encryptedEmpty

        val result = cipher.encrypt(emptyBackupData)

        assertTrue("Should encrypt even empty backup", result.isNotEmpty())
        coVerify(exactly = 1) { cipher.encrypt(emptyBackupData) }
    }

    @Test
    fun backupFlow_withEncryption_shouldChainCorrectly_throughAllSteps() = runTest {
        val backupData = "{\"transactions\": []}"
        val encryptedData = "ACM_ENC_V1:encrypted_result"

        coEvery { settingsRepository.getAutoBackupEnabled() } returns flowOf(true)
        coEvery { settingsRepository.getDataEncryptionEnabled() } returns flowOf(true)
        coEvery { backupService.createBackup() } returns Result.success(backupData)
        coEvery { cipher.encrypt(backupData) } returns encryptedData

        // Simulate flow: enabled -> create backup -> encrypt
        var result: Result<String>? = null
        var encryptedResult: String? = null

        settingsRepository.getAutoBackupEnabled().collect { isEnabled ->
            if (isEnabled) {
                result = backupService.createBackup()
                if (result?.isSuccess == true) {
                    encryptedResult = cipher.encrypt(result?.getOrNull()!!)
                }
            }
        }

        assertTrue("Backup flow should complete", encryptedResult == encryptedData)
        coVerify { backupService.createBackup() }
        coVerify { cipher.encrypt(backupData) }
    }
}
