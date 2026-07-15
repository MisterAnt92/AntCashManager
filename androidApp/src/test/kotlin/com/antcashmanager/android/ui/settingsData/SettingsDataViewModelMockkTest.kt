package com.antcashmanager.android.ui.settingsData

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.data.backup.BackupService
import com.antcashmanager.android.data.backup.RestoreResult
import com.antcashmanager.android.security.BackupPayloadCipher
import com.antcashmanager.android.ui.screen.settingsData.BackupResult
import com.antcashmanager.android.ui.screen.settingsData.DeleteResult
import com.antcashmanager.android.ui.screen.settingsData.RestoreOperationResult
import com.antcashmanager.android.ui.screen.settingsData.RestoreSuccessInfo
import com.antcashmanager.android.ui.screen.settingsData.SettingsDataViewModel
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.transaction.DeleteAllTransactionsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsDataViewModelMockkTest : BaseUnitTest() {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var deleteAllTransactionsUseCase: DeleteAllTransactionsUseCase
    private lateinit var backupService: BackupService

    @Before
    fun setup() {
        settingsRepository = mockk(relaxed = true)
        categoryRepository = mockk(relaxed = true)
        deleteAllTransactionsUseCase = mockk()
        backupService = mockk()

        every { settingsRepository.getDataEncryptionEnabled() } returns flowOf(false)
        coEvery { settingsRepository.setDataEncryptionEnabled(any()) } returns Unit
        coEvery { settingsRepository.resetAllPreferences() } returns Unit
        coEvery { categoryRepository.deleteAllCategories() } returns Unit

        mockkObject(BackupPayloadCipher)
    }

    @After
    fun tearDown() {
        unmockkObject(BackupPayloadCipher)
    }

    // ── Toggle encryption ──

    @Test
    fun setDataEncryptionEnabled_shouldDelegateToRepository_whenToggled() = runViewModelTest {
        val viewModel = buildViewModel()

        viewModel.setDataEncryptionEnabled(true)
        advanceUntilIdle()

        coVerify(exactly = 1) { settingsRepository.setDataEncryptionEnabled(true) }
    }

    // ── Dialog show/dismiss ──

    @Test
    fun showDeleteConfirmDialog_shouldSetDialogVisible_whenCalled() = runViewModelTest {
        val viewModel = buildViewModel()

        viewModel.showDeleteConfirmDialog()

        assertTrue(viewModel.state.value.showDeleteConfirmDialog)
    }

    @Test
    fun dismissDeleteConfirmDialog_shouldHideDialog_whenCalled() = runViewModelTest {
        val viewModel = buildViewModel()
        viewModel.showDeleteConfirmDialog()

        viewModel.dismissDeleteConfirmDialog()

        assertFalse(viewModel.state.value.showDeleteConfirmDialog)
    }

    @Test
    fun dismissDeleteSuccessDialog_shouldResetDeleteResultToIdle_whenCalled() = runViewModelTest {
        coEvery { deleteAllTransactionsUseCase() } returns Result.success(Unit)
        val viewModel = buildViewModel()
        viewModel.deleteAllData()
        advanceUntilIdle()

        viewModel.dismissDeleteSuccessDialog()

        val state = viewModel.state.value
        assertFalse(state.showDeleteSuccessDialog)
        assertEquals(DeleteResult.Idle, state.deleteResult)
    }

    @Test
    fun showResetPreferencesDialog_shouldSetDialogVisible_whenCalled() = runViewModelTest {
        val viewModel = buildViewModel()

        viewModel.showResetPreferencesDialog()

        assertTrue(viewModel.state.value.showResetPreferencesDialog)
    }

    @Test
    fun dismissResetPreferencesDialog_shouldHideDialog_whenCalled() = runViewModelTest {
        val viewModel = buildViewModel()
        viewModel.showResetPreferencesDialog()

        viewModel.dismissResetPreferencesDialog()

        assertFalse(viewModel.state.value.showResetPreferencesDialog)
    }

    @Test
    fun dismissBackupSuccessDialog_shouldResetBackupResultToIdle_whenCalled() = runViewModelTest {
        coEvery { backupService.createBackup() } returns Result.success("{}")
        val viewModel = buildViewModel()
        viewModel.createBackup()
        advanceUntilIdle()

        viewModel.dismissBackupSuccessDialog()

        val state = viewModel.state.value
        assertFalse(state.showBackupSuccessDialog)
        assertEquals(BackupResult.Idle, state.backupResult)
    }

    @Test
    fun dismissBackupErrorDialog_shouldClearErrorMessageAndHideDialog_whenCalled() = runViewModelTest {
        coEvery { backupService.createBackup() } returns Result.failure(IllegalStateException("backup failed"))
        val viewModel = buildViewModel()
        viewModel.createBackup()
        advanceUntilIdle()

        viewModel.dismissBackupErrorDialog()

        val state = viewModel.state.value
        assertFalse(state.showBackupErrorDialog)
        assertEquals(BackupResult.Idle, state.backupResult)
        assertEquals("", state.backupErrorMessage)
    }

    @Test
    fun dismissRestoreSuccessDialog_shouldClearSuccessInfoAndHideDialog_whenCalled() = runViewModelTest {
        every { BackupPayloadCipher.isEncryptedPayload(any()) } returns false
        coEvery { backupService.restoreBackup(any()) } returns Result.success(RestoreResult(3, 2))
        val viewModel = buildViewModel()
        viewModel.restoreBackup("{}")
        advanceUntilIdle()

        viewModel.dismissRestoreSuccessDialog()

        val state = viewModel.state.value
        assertFalse(state.showRestoreSuccessDialog)
        assertNull(state.restoreSuccessInfo)
    }

    @Test
    fun dismissRestoreErrorDialog_shouldClearErrorMessageAndHideDialog_whenCalled() = runViewModelTest {
        every { BackupPayloadCipher.isEncryptedPayload(any()) } returns false
        coEvery { backupService.restoreBackup(any()) } returns Result.failure(IllegalStateException("restore failed"))
        val viewModel = buildViewModel()
        viewModel.restoreBackup("{}")
        advanceUntilIdle()

        viewModel.dismissRestoreErrorDialog()

        val state = viewModel.state.value
        assertFalse(state.showRestoreErrorDialog)
        assertEquals("", state.restoreErrorMessage)
    }

    @Test
    fun onBackupFileSaved_shouldClearPendingBackupDataAndShowSuccessDialog_whenCalled() = runViewModelTest {
        coEvery { backupService.createBackup() } returns Result.success("{}")
        val viewModel = buildViewModel()
        viewModel.createBackup()
        advanceUntilIdle()

        viewModel.onBackupFileSaved()

        val state = viewModel.state.value
        assertNull(state.pendingBackupData)
        assertNull(state.pendingBackupFileName)
        assertTrue(state.showBackupSuccessDialog)
    }

    @Test
    fun clearPendingBackupRequest_shouldClearPendingBackupDataWithoutShowingDialog_whenCalled() = runViewModelTest {
        coEvery { backupService.createBackup() } returns Result.success("{}")
        val viewModel = buildViewModel()
        viewModel.createBackup()
        advanceUntilIdle()

        viewModel.clearPendingBackupRequest()

        val state = viewModel.state.value
        assertNull(state.pendingBackupData)
        assertNull(state.pendingBackupFileName)
        assertFalse(state.showBackupSuccessDialog)
    }

    @Test
    fun onBackupFileSaveError_shouldSetErrorStateAndShowErrorDialog_whenCalled() = runViewModelTest {
        val viewModel = buildViewModel()

        viewModel.onBackupFileSaveError("disk full")

        val state = viewModel.state.value
        assertEquals(BackupResult.Error("disk full"), state.backupResult)
        assertEquals("disk full", state.backupErrorMessage)
        assertTrue(state.showBackupErrorDialog)
    }

    @Test
    fun onRestoreFileReadError_shouldSetErrorStateAndShowErrorDialog_whenCalled() = runViewModelTest {
        val viewModel = buildViewModel()

        viewModel.onRestoreFileReadError("file not readable")

        val state = viewModel.state.value
        assertEquals(RestoreOperationResult.Error("file not readable"), state.restoreResult)
        assertEquals("file not readable", state.restoreErrorMessage)
        assertTrue(state.showRestoreErrorDialog)
    }

    // ── deleteAllData ──

    @Test
    fun deleteAllData_shouldShowSuccessDialog_whenDeleteAndCategoryCleanupSucceed() = runViewModelTest {
        coEvery { deleteAllTransactionsUseCase() } returns Result.success(Unit)
        val viewModel = buildViewModel()

        viewModel.deleteAllData()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(DeleteResult.Success, state.deleteResult)
        assertTrue(state.showDeleteSuccessDialog)
        assertFalse(state.showDeleteConfirmDialog)
        coVerify(exactly = 1) { categoryRepository.deleteAllCategories() }
    }

    @Test
    fun deleteAllData_shouldSetErrorResult_whenDeleteAllTransactionsUseCaseFails() = runViewModelTest {
        coEvery { deleteAllTransactionsUseCase() } returns Result.failure(IllegalStateException("delete failed"))
        val viewModel = buildViewModel()

        viewModel.deleteAllData()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(DeleteResult.Error("delete failed"), state.deleteResult)
        assertFalse(state.showDeleteConfirmDialog)
    }

    // ── createBackup ──

    @Test
    fun createBackup_shouldStorePendingUnencryptedPayload_whenEncryptionDisabled() = runViewModelTest {
        every { settingsRepository.getDataEncryptionEnabled() } returns flowOf(false)
        coEvery { backupService.createBackup() } returns Result.success("{\"json\":true}")
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.createBackup()
        advanceUntilIdle()

        val state = viewModel.state.value
        val fileName = state.pendingBackupFileName!!
        assertEquals("{\"json\":true}", state.pendingBackupData)
        assertTrue(fileName.startsWith("antcashmanager_backup_"))
        assertTrue(fileName.endsWith(".json"))
        assertEquals(BackupResult.Success, state.backupResult)
    }

    @Test
    fun createBackup_shouldEncryptPendingPayload_whenEncryptionEnabled() = runViewModelTest {
        every { settingsRepository.getDataEncryptionEnabled() } returns flowOf(true)
        coEvery { backupService.createBackup() } returns Result.success("{\"json\":true}")
        every { BackupPayloadCipher.encrypt("{\"json\":true}") } returns "ACM_ENC_V1:encrypted"
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.createBackup()
        advanceUntilIdle()

        assertEquals("ACM_ENC_V1:encrypted", viewModel.state.value.pendingBackupData)
    }

    @Test
    fun createBackup_shouldShowErrorDialog_whenBackupServiceFails() = runViewModelTest {
        coEvery { backupService.createBackup() } returns Result.failure(IllegalStateException("backup failed"))
        val viewModel = buildViewModel()

        viewModel.createBackup()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.showBackupErrorDialog)
        assertEquals(BackupResult.Error("backup failed"), state.backupResult)
    }

    @Test
    fun createBackup_shouldShowErrorDialog_whenEncryptionThrows() = runViewModelTest {
        every { settingsRepository.getDataEncryptionEnabled() } returns flowOf(true)
        coEvery { backupService.createBackup() } returns Result.success("{\"json\":true}")
        every { BackupPayloadCipher.encrypt(any()) } throws IllegalStateException("keystore error")
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.createBackup()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.showBackupErrorDialog)
        assertNull(state.pendingBackupData)
    }

    // ── restoreBackup ──

    @Test
    fun restoreBackup_shouldShowSuccessDialogWithCounts_whenUnencryptedPayloadRestoresSuccessfully() = runViewModelTest {
        every { BackupPayloadCipher.isEncryptedPayload(any()) } returns false
        coEvery { backupService.restoreBackup("{\"json\":true}") } returns Result.success(RestoreResult(3, 2))
        val viewModel = buildViewModel()

        viewModel.restoreBackup("{\"json\":true}")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(RestoreSuccessInfo(3, 2), state.restoreSuccessInfo)
        assertTrue(state.showRestoreSuccessDialog)
    }

    @Test
    fun restoreBackup_shouldDecryptPayloadBeforeDelegating_whenPayloadIsEncrypted() = runViewModelTest {
        every { BackupPayloadCipher.isEncryptedPayload("ACM_ENC_V1:encrypted") } returns true
        every { BackupPayloadCipher.decrypt("ACM_ENC_V1:encrypted") } returns "{\"json\":true}"
        coEvery { backupService.restoreBackup(any()) } returns Result.success(RestoreResult(1, 1))
        val viewModel = buildViewModel()

        viewModel.restoreBackup("ACM_ENC_V1:encrypted")
        advanceUntilIdle()

        coVerify(exactly = 1) { backupService.restoreBackup("{\"json\":true}") }
    }

    @Test
    fun restoreBackup_shouldShowErrorDialog_whenBackupServiceFails() = runViewModelTest {
        every { BackupPayloadCipher.isEncryptedPayload(any()) } returns false
        coEvery { backupService.restoreBackup(any()) } returns Result.failure(IllegalStateException("restore failed"))
        val viewModel = buildViewModel()

        viewModel.restoreBackup("{\"json\":true}")
        advanceUntilIdle()

        assertTrue(viewModel.state.value.showRestoreErrorDialog)
    }

    @Test
    fun restoreBackup_shouldShowErrorDialog_whenDecryptionThrows() = runViewModelTest {
        every { BackupPayloadCipher.isEncryptedPayload(any()) } returns true
        every { BackupPayloadCipher.decrypt(any()) } throws IllegalStateException("bad payload")
        val viewModel = buildViewModel()

        viewModel.restoreBackup("ACM_ENC_V1:corrupted")
        advanceUntilIdle()

        assertTrue(viewModel.state.value.showRestoreErrorDialog)
        coVerify(exactly = 0) { backupService.restoreBackup(any()) }
    }

    // ── resetAllPreferences ──

    @Test
    fun resetAllPreferences_shouldDelegateToRepositoryAndHideDialog_whenCalled() = runViewModelTest {
        val viewModel = buildViewModel()
        viewModel.showResetPreferencesDialog()

        viewModel.resetAllPreferences()
        advanceUntilIdle()

        coVerify(exactly = 1) { settingsRepository.resetAllPreferences() }
        assertFalse(viewModel.state.value.showResetPreferencesDialog)
    }

    private fun buildViewModel(): SettingsDataViewModel = SettingsDataViewModel(
        settingsRepository = settingsRepository,
        categoryRepository = categoryRepository,
        deleteAllTransactionsUseCase = deleteAllTransactionsUseCase,
        backupService = backupService,
    )
}
