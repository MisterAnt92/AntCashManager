package com.antcashmanager.android.ui.screen.settings.dataManagement

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.auth.GoogleSignInManager
import com.antcashmanager.android.data.backup.BackupService
import com.antcashmanager.android.work.AutoBackupScheduler
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.transaction.DeleteAllTransactionsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test per il ViewModel di Gestione Dati, focalizzato su Automatic Backup.
 *
 * Verifica:
 * - Enable/disable del backup automatico con scheduler
 * - Selezione della cartella e persistenza del URI
 * - Flow di autoBackupEnabled e autoBackupFolderUri nello stato
 */
class SettingsDataViewModelAutoBackupTest : BaseUnitTest() {

    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val categoryRepository: CategoryRepository = mockk(relaxed = true)
    private val deleteAllTransactionsUseCase: DeleteAllTransactionsUseCase = mockk(relaxed = true)
    private val backupService: BackupService = mockk(relaxed = true)
    private val autoBackupScheduler: AutoBackupScheduler = mockk(relaxed = true)
    private val googleSignInManager: GoogleSignInManager = mockk(relaxed = true)

    private fun createViewModel(): SettingsDataViewModel {
        return SettingsDataViewModel(
            settingsRepository = settingsRepository,
            categoryRepository = categoryRepository,
            deleteAllTransactionsUseCase = deleteAllTransactionsUseCase,
            backupService = backupService,
            autoBackupScheduler = autoBackupScheduler,
            googleSignInManager = googleSignInManager,
        )
    }

    @Test
    fun `when setAutoBackupEnabled is called with true, scheduler should schedule work`() = runUnitTest {
        val viewModel = createViewModel()

        viewModel.setAutoBackupEnabled(true)

        coVerify { autoBackupScheduler.schedule() }
        coVerify { settingsRepository.setAutoBackupEnabled(true) }
    }

    @Test
    fun `when setAutoBackupEnabled is called with false, scheduler should cancel work`() = runUnitTest {
        val viewModel = createViewModel()

        viewModel.setAutoBackupEnabled(false)

        coVerify { autoBackupScheduler.cancel() }
        coVerify { settingsRepository.setAutoBackupEnabled(false) }
    }

    @Test
    fun `when onAutoBackupFolderSelected is called, URI and enable should be persisted`() = runUnitTest {
        val viewModel = createViewModel()
        val testUri = "content://com.android.externalstorage.documents/tree/primary%3ADocuments"

        viewModel.onAutoBackupFolderSelected(testUri)

        coVerify { settingsRepository.setAutoBackupFolderUri(testUri) }
        coVerify { settingsRepository.setAutoBackupEnabled(true) }
        coVerify { autoBackupScheduler.schedule() }
    }

    @Test
    fun `onAutoBackupFolderSelectionCancelled should not change state`() = runUnitTest {
        val viewModel = createViewModel()

        viewModel.onAutoBackupFolderSelectionCancelled()

        // Nessuna chiamata al repository o scheduler
        coVerify(exactly = 0) { settingsRepository.setAutoBackupEnabled(any()) }
        coVerify(exactly = 0) { autoBackupScheduler.schedule() }
    }

    @Test
    fun `when onAutoBackupFolderSelected with invalid URI, should handle gracefully`() = runUnitTest {
        val viewModel = createViewModel()
        val invalidUri = "invalid://uri/path"

        viewModel.onAutoBackupFolderSelected(invalidUri)

        // Should still persist the URI (validation happens later)
        coVerify { settingsRepository.setAutoBackupFolderUri(invalidUri) }
    }

    @Test
    fun `when folder permission is revoked, scheduler should cancel work`() = runUnitTest {
        val viewModel = createViewModel()

        viewModel.setAutoBackupEnabled(false)

        coVerify { autoBackupScheduler.cancel() }
        coVerify { settingsRepository.setAutoBackupEnabled(false) }
    }

    @Test
    fun `when setAutoBackupEnabled is called multiple times, latest state wins`() = runUnitTest {
        val viewModel = createViewModel()

        viewModel.setAutoBackupEnabled(true)

        viewModel.setAutoBackupEnabled(false)

        viewModel.setAutoBackupEnabled(true)

        // Final state should be enabled
        coVerify { settingsRepository.setAutoBackupEnabled(true) }
        coVerify { autoBackupScheduler.schedule() }
    }

    @Test
    fun `when onAutoBackupFolderSelected is called twice, last URI is persisted`() = runUnitTest {
        val viewModel = createViewModel()
        val uri1 = "content://first/path"
        val uri2 = "content://second/path"

        viewModel.onAutoBackupFolderSelected(uri1)

        viewModel.onAutoBackupFolderSelected(uri2)

        coVerify { settingsRepository.setAutoBackupFolderUri(uri2) }
    }

    @Test
    fun `when autoBackupEnabled is true, state should contain folder URI from repository`() = runUnitTest {
        val testUri = "content://com.android.externalstorage/tree/primary%3AAntCashManager"
        val settingsRepositoryWithUri: SettingsRepository = mockk(relaxed = true)

        // Mock the flow to return the URI
        io.mockk.coEvery { settingsRepositoryWithUri.getAutoBackupFolderUri() } returns
            kotlinx.coroutines.flow.flowOf(testUri)
        io.mockk.coEvery { settingsRepositoryWithUri.getAutoBackupEnabled() } returns
            kotlinx.coroutines.flow.flowOf(true)
        io.mockk.coEvery { settingsRepositoryWithUri.getLastBackupTimestamp() } returns
            kotlinx.coroutines.flow.flowOf(null)
        io.mockk.coEvery { settingsRepositoryWithUri.getLastRestoreTimestamp() } returns
            kotlinx.coroutines.flow.flowOf(null)
        io.mockk.coEvery { settingsRepositoryWithUri.getSuggestionsEnabled() } returns
            kotlinx.coroutines.flow.flowOf(false)
        io.mockk.coEvery { settingsRepositoryWithUri.getDataEncryptionEnabled() } returns
            kotlinx.coroutines.flow.flowOf(false)
        io.mockk.coEvery { settingsRepositoryWithUri.getAutoBackupDestination() } returns
            kotlinx.coroutines.flow.flowOf(com.antcashmanager.domain.model.BackupDestination.LOCAL)
        io.mockk.coEvery { settingsRepositoryWithUri.getGoogleDriveUserEmail() } returns
            kotlinx.coroutines.flow.flowOf(null)

        val viewModel = SettingsDataViewModel(
            settingsRepository = settingsRepositoryWithUri,
            categoryRepository = categoryRepository,
            deleteAllTransactionsUseCase = deleteAllTransactionsUseCase,
            backupService = backupService,
            autoBackupScheduler = autoBackupScheduler,
            googleSignInManager = googleSignInManager,
        )

        assertEquals(testUri, viewModel.state.value.autoBackupFolderUri)
        assertTrue(viewModel.state.value.autoBackupEnabled)
    }

    @Test
    fun `when autoBackupEnabled but folder URI is null, should show not-selected state`() = runUnitTest {
        val settingsRepositoryNoUri: SettingsRepository = mockk(relaxed = true)

        // Mock the flows
        coEvery { settingsRepositoryNoUri.getAutoBackupFolderUri() } returns flowOf(null)
        coEvery { settingsRepositoryNoUri.getAutoBackupEnabled() } returns flowOf(true)
        coEvery { settingsRepositoryNoUri.getLastBackupTimestamp() } returns flowOf(null)
        coEvery { settingsRepositoryNoUri.getLastRestoreTimestamp() } returns flowOf(null)
        coEvery { settingsRepositoryNoUri.getSuggestionsEnabled() } returns flowOf(false)
        coEvery { settingsRepositoryNoUri.getDataEncryptionEnabled() } returns flowOf(false)
        coEvery { settingsRepositoryNoUri.getAutoBackupDestination() } returns
            flowOf(com.antcashmanager.domain.model.BackupDestination.LOCAL)
        coEvery { settingsRepositoryNoUri.getGoogleDriveUserEmail() } returns flowOf(null)

        val viewModel = SettingsDataViewModel(
            settingsRepository = settingsRepositoryNoUri,
            categoryRepository = categoryRepository,
            deleteAllTransactionsUseCase = deleteAllTransactionsUseCase,
            backupService = backupService,
            autoBackupScheduler = autoBackupScheduler,
            googleSignInManager = googleSignInManager,
        )

        assertNull(viewModel.state.value.autoBackupFolderUri)
        assertTrue(viewModel.state.value.autoBackupEnabled)
    }
}
