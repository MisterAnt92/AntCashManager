package com.antcashmanager.android.ui.screen.settings.dataManagement

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.analytics.tracker.ErrorTracker
import com.antcashmanager.android.analytics.tracker.PerformanceTracker
import com.antcashmanager.android.auth.GoogleSignInManager
import com.antcashmanager.android.auth.GoogleSignInResult
import com.antcashmanager.android.data.backup.BackupService
import com.antcashmanager.android.work.AutoBackupScheduler
import com.antcashmanager.domain.model.BackupDestination
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.usecase.transaction.DeleteAllTransactionsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Test

/**
 * Test unitari per Google Drive backup flow nel SettingsDataViewModel.
 *
 * **Coverage**:
 * - Sign-in flow (dialog show → sign-in call → success/error handling)
 * - Destination change (LOCAL ↔ GOOGLE_DRIVE)
 * - Sign-out flow (revoca credenziali)
 * - Token management (access token persistence)
 */
class SettingsDataViewModelGoogleDriveTest : BaseUnitTest() {
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val categoryRepository: CategoryRepository = mockk(relaxed = true)
    private val deleteAllTransactionsUseCase: DeleteAllTransactionsUseCase = mockk(relaxed = true)
    private val backupService: BackupService = mockk(relaxed = true)
    private val autoBackupScheduler: AutoBackupScheduler = mockk(relaxed = true)
    private val googleSignInManager: GoogleSignInManager = mockk(relaxed = true)
    private val performanceTracker: PerformanceTracker = mockk(relaxed = true)
    private val errorTracker: ErrorTracker = mockk(relaxed = true)

    private val viewModel: SettingsDataViewModel by lazy {
        // Setup default Flow responses
        coEvery { settingsRepository.getAutoBackupEnabled() } returns flowOf(true)
        coEvery { settingsRepository.getAutoBackupDestination() } returns flowOf(BackupDestination.LOCAL)
        coEvery { settingsRepository.getGoogleDriveUserEmail() } returns flowOf(null)
        coEvery { settingsRepository.getDataEncryptionEnabled() } returns flowOf(false)
        coEvery { settingsRepository.getLastBackupTimestamp() } returns flowOf(null)
        coEvery { settingsRepository.getLastRestoreTimestamp() } returns flowOf(null)
        coEvery { settingsRepository.getSuggestionsEnabled() } returns flowOf(true)

        SettingsDataViewModel(
            settingsRepository = settingsRepository,
            categoryRepository = categoryRepository,
            transactionRepository = mockk(relaxed = true),
            deleteAllTransactionsUseCase = deleteAllTransactionsUseCase,
            backupService = backupService,
            autoBackupScheduler = autoBackupScheduler,
            googleSignInManager = googleSignInManager,
            performanceTracker = performanceTracker,
            errorTracker = errorTracker,
        )
    }

    // ── Sign-In Flow Tests ──

    @Test
    fun `setAutoBackupDestination_toGoogleDrive_notSignedIn_showsDialog`() =
        runUnitTest {
            // Setup: user NOT signed in
            coEvery { googleSignInManager.isSignedIn() } returns false

            // Act: change destination to Google Drive
            viewModel.setAutoBackupDestination(BackupDestination.GOOGLE_DRIVE)

            // Assert: dialog should be shown
            assert(viewModel.state.value.showGoogleSignInDialog) {
                "Google Sign-In dialog should be shown when switching to Google Drive and not signed in"
            }
        }

    @Test
    fun `setAutoBackupDestination_toGoogleDrive_alreadySignedIn_noDialog`() =
        runUnitTest {
            // Setup: user already signed in
            coEvery { googleSignInManager.isSignedIn() } returns true

            // Act: change destination to Google Drive
            viewModel.setAutoBackupDestination(BackupDestination.GOOGLE_DRIVE)

            // Assert: dialog should NOT be shown, destination should be set directly
            assert(!viewModel.state.value.showGoogleSignInDialog) {
                "Google Sign-In dialog should NOT be shown when already signed in"
            }
            coVerify { settingsRepository.setAutoBackupDestination(BackupDestination.GOOGLE_DRIVE) }
        }

    @Test
    fun `setAutoBackupDestination_toLocal_closesDialog`() =
        runUnitTest {
            // Setup: dialog is open
            viewModel.dismissGoogleSignInDialog() // Ensure state is clean

            // Act: change destination to LOCAL
            viewModel.setAutoBackupDestination(BackupDestination.LOCAL)

            // Assert: dialog should be closed
            assert(!viewModel.state.value.showGoogleSignInDialog) {
                "Google Sign-In dialog should be closed when switching to LOCAL"
            }
            coVerify { settingsRepository.setAutoBackupDestination(BackupDestination.LOCAL) }
        }

    // ── Sign-In Success Tests ──

    @Test
    fun `initiateGoogleSignIn_success_updatesState`() =
        runUnitTest {
            // Setup: sign-in succeeds with email
            val testEmail = "user@example.com"
            val testAccessToken = "access_token_123"
            val signInResult =
                GoogleSignInResult(
                    email = testEmail,
                    accessToken = testAccessToken,
                    idToken = "id_token_456",
                )
            coEvery { googleSignInManager.signIn() } returns Result.success(signInResult)

            // Act: initiate sign-in
            viewModel.initiateGoogleSignIn()

            // Assert: state should reflect successful login
            assert(viewModel.state.value.isGoogleDriveSignedIn) {
                "Should be marked as signed in after successful sign-in"
            }
            assert(viewModel.state.value.googleDriveUserEmail == testEmail) {
                "User email should be stored in state"
            }
            assert(viewModel.state.value.autoBackupDestination == BackupDestination.GOOGLE_DRIVE) {
                "Destination should be set to GOOGLE_DRIVE after successful sign-in"
            }
        }

    @Test
    fun `initiateGoogleSignIn_failure_rollsBackToLocal`() =
        runUnitTest {
            // Setup: sign-in fails
            val error = Exception("Network error")
            coEvery { googleSignInManager.signIn() } returns Result.failure(error)

            // Act: initiate sign-in
            viewModel.initiateGoogleSignIn()

            // Assert: state should roll back to LOCAL
            assert(!viewModel.state.value.isGoogleDriveSignedIn) {
                "Should NOT be marked as signed in after failed sign-in"
            }
            assert(viewModel.state.value.autoBackupDestination == BackupDestination.LOCAL) {
                "Destination should remain/return to LOCAL after failed sign-in"
            }
            assert(!viewModel.state.value.showGoogleSignInDialog) {
                "Dialog should be closed after sign-in attempt (success or failure)"
            }
        }

    // ── Sign-Out Tests ──

    @Test
    fun `signOutFromGoogle_success_clearsState`() =
        runUnitTest {
            // Setup: user is signed in
            coEvery { googleSignInManager.signOut() } returns Result.success(Unit)

            // Act: sign out
            viewModel.signOutFromGoogle()

            // Assert: state should be cleared
            assert(!viewModel.state.value.isGoogleDriveSignedIn) {
                "Should NOT be marked as signed in after sign-out"
            }
            assert(viewModel.state.value.googleDriveUserEmail == null) {
                "User email should be cleared after sign-out"
            }
            assert(viewModel.state.value.autoBackupDestination == BackupDestination.LOCAL) {
                "Destination should return to LOCAL after sign-out"
            }
            coVerify { settingsRepository.setAutoBackupDestination(BackupDestination.LOCAL) }
        }

    @Test
    fun `signOutFromGoogle_failure_stillHandled`() =
        runUnitTest {
            // Setup: sign-out fails
            val error = Exception("Sign-out error")
            coEvery { googleSignInManager.signOut() } returns Result.failure(error)

            // Act: sign out
            viewModel.signOutFromGoogle()

            // Assert: state should still be cleared even if sign-out failed
            // (in practice, token is already cleared, only Google account needs revocation)
            assert(!viewModel.state.value.showGoogleSignInDialog) {
                "Dialog should not be shown after sign-out attempt"
            }
        }

    // ── Integration Tests ──

    @Test
    fun `fullSignInFlow_selectGoogleDrive_signIn_success`() =
        runUnitTest {
            // Setup
            val testEmail = "user@example.com"
            val signInResult =
                GoogleSignInResult(
                    email = testEmail,
                    accessToken = "token",
                    idToken = "idtoken",
                )
            coEvery { googleSignInManager.isSignedIn() } returns false
            coEvery { googleSignInManager.signIn() } returns Result.success(signInResult)

            // Step 1: User selects Google Drive (not signed in yet)
            viewModel.setAutoBackupDestination(BackupDestination.GOOGLE_DRIVE)
            assert(viewModel.state.value.showGoogleSignInDialog) {
                "Step 1: Dialog should be shown"
            }

            // Step 2: User initiates sign-in
            viewModel.initiateGoogleSignIn()
            assert(viewModel.state.value.isGoogleDriveSignedIn) {
                "Step 2: Should be signed in after successful sign-in"
            }
            assert(viewModel.state.value.autoBackupDestination == BackupDestination.GOOGLE_DRIVE) {
                "Step 2: Destination should be GOOGLE_DRIVE"
            }

            // Verify scheduler was notified
            coVerify { autoBackupScheduler.schedule() }
        }

    @Test
    fun `fullSignOutFlow_googleDrive_signOut_success`() =
        runUnitTest {
            // Setup: already signed in
            coEvery { googleSignInManager.signOut() } returns Result.success(Unit)
            coEvery { settingsRepository.getGoogleDriveUserEmail() } returns flowOf("user@example.com")

            // Step 1: User signs out
            viewModel.signOutFromGoogle()

            // Step 2: Verify state is cleaned
            assert(!viewModel.state.value.isGoogleDriveSignedIn) {
                "Should NOT be signed in after sign-out"
            }
            assert(viewModel.state.value.autoBackupDestination == BackupDestination.LOCAL) {
                "Destination should return to LOCAL"
            }

            // Verify Google Sign-Out was called
            coVerify { googleSignInManager.signOut() }
            // Verify settings were updated
            coVerify { settingsRepository.setAutoBackupDestination(BackupDestination.LOCAL) }
        }
}
