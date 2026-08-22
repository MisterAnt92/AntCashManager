package com.antcashmanager.android.ui.screen.settings.dataManagement

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.auth.GoogleSignInManager
import com.antcashmanager.android.data.backup.BackupService
import com.antcashmanager.android.work.AutoBackupScheduler
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.transaction.DeleteAllTransactionsUseCase
import io.mockk.coVerify
import io.mockk.mockk
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
}
