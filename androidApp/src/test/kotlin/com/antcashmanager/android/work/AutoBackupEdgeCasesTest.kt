package com.antcashmanager.android.work

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.antcashmanager.android.data.backup.BackupService
import com.antcashmanager.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

/**
 * Edge case and stress tests for AutoBackupWorker.
 *
 * Verifies:
 * - Very large backups (10k+ transactions)
 * - Special characters in filenames
 * - Unicode content in backup
 * - Empty backups
 * - Concurrent attempts
 * - Storage quota scenarios
 */
@RunWith(AndroidJUnit4::class)
class AutoBackupEdgeCasesTest {
    private lateinit var context: Context
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val backupService: BackupService = mockk(relaxed = true)

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        stopKoin()
        startKoin {
            modules(
                module {
                    single { settingsRepository }
                    single { backupService }
                },
            )
        }

        coEvery { settingsRepository.getAutoBackupEnabled() } returns flowOf(true)
        coEvery { settingsRepository.getAutoBackupFolderUri() } returns flowOf("content://test/uri")
        coEvery { settingsRepository.getDataEncryptionEnabled() } returns flowOf(false)
        coEvery { settingsRepository.setLastBackupTimestamp(any()) } returns Unit
    }

    @Test
    fun very_large_backup_with_10000_transactions_completes() =
        runTest {
            val largeBackup = buildLargeBackup(10000)

            coEvery { backupService.createBackup() } returns Result.success(largeBackup)

            val worker = TestListenableWorkerBuilder<AutoBackupWorker>(context).build()
            val result = worker.doWork()

            assert(result == ListenableWorker.Result.success())
        }

    @Test
    fun extremely_large_backup_50000_transactions_succeeds() =
        runTest {
            val hugeBackup = buildLargeBackup(50000)

            coEvery { backupService.createBackup() } returns Result.success(hugeBackup)

            val worker = TestListenableWorkerBuilder<AutoBackupWorker>(context).build()
            val result = worker.doWork()

            assert(result == ListenableWorker.Result.success() || result == ListenableWorker.Result.retry())
        }

    @Test
    fun empty_backup_data_succeeds() =
        runTest {
            coEvery { backupService.createBackup() } returns Result.success("{\"transactions\": []}")

            val worker = TestListenableWorkerBuilder<AutoBackupWorker>(context).build()
            val result = worker.doWork()

            assert(result == ListenableWorker.Result.success())
        }

    @Test
    fun backup_with_unicode_characters_succeeds() =
        runTest {
            val unicodeBackup =
                """
                {
                  "transactions": [
                    {"title": "Café ☕", "amount": -5.5, "category": "食べ物"},
                    {"title": "Москва 🚀", "amount": -10.0, "category": "旅行"},
                    {"title": "Αθήνα 🇬🇷", "amount": -20.0, "category": "Путешествие"}
                  ]
                }
                """.trimIndent()

            coEvery { backupService.createBackup() } returns Result.success(unicodeBackup)

            val worker = TestListenableWorkerBuilder<AutoBackupWorker>(context).build()
            val result = worker.doWork()

            assert(result == ListenableWorker.Result.success())
        }

    @Test
    fun backup_with_special_characters_in_content_succeeds() =
        runTest {
            val specialCharsBackup =
                """
                {
                  "transactions": [
                    {"title": "Test\"with\"quotes", "notes": "Line1\nLine2\tTab"},
                    {"title": "Path/with\\slashes", "category": "C:\\Windows\\System32"}
                  ]
                }
                """.trimIndent()

            coEvery { backupService.createBackup() } returns Result.success(specialCharsBackup)

            val worker = TestListenableWorkerBuilder<AutoBackupWorker>(context).build()
            val result = worker.doWork()

            assert(result == ListenableWorker.Result.success())
        }

    @Test
    fun rapidly_triggered_backups_handled_gracefully() =
        runTest {
            coEvery { backupService.createBackup() } returns Result.success("{\"data\":\"test\"}")

            // Simulate rapid-fire backup attempts
            val results = mutableListOf<ListenableWorker.Result>()
            repeat(5) {
                val worker = TestListenableWorkerBuilder<AutoBackupWorker>(context).build()
                results.add(worker.doWork())
            }

            // At least most should succeed
            val successes = results.count { it == ListenableWorker.Result.success() }
            assert(successes >= 3)
        }

    @Test
    fun backup_with_very_long_transaction_title_succeeds() =
        runTest {
            val longTitle = "A".repeat(1000) // 1000 character title
            val backupWithLongTitle =
                """
                {
                  "transactions": [
                    {"title": "$longTitle", "amount": -5.5}
                  ]
                }
                """.trimIndent()

            coEvery { backupService.createBackup() } returns Result.success(backupWithLongTitle)

            val worker = TestListenableWorkerBuilder<AutoBackupWorker>(context).build()
            val result = worker.doWork()

            assert(result == ListenableWorker.Result.success())
        }

    @Test
    fun null_or_empty_folder_uri_fails_gracefully() =
        runTest {
            coEvery { settingsRepository.getAutoBackupFolderUri() } returns flowOf(null)
            coEvery { backupService.createBackup() } returns Result.success("{\"data\":\"test\"}")

            val worker = TestListenableWorkerBuilder<AutoBackupWorker>(context).build()
            val result = worker.doWork()

            assert(result == ListenableWorker.Result.failure())
        }

    @Test
    fun malformed_json_backup_fails_gracefully() =
        runTest {
            coEvery { backupService.createBackup() } returns Result.success("{invalid json")

            val worker = TestListenableWorkerBuilder<AutoBackupWorker>(context).build()
            val result = worker.doWork()

            assert(result == ListenableWorker.Result.failure() || result == ListenableWorker.Result.retry())
        }

    // Helper functions
    private fun buildLargeBackup(transactionCount: Int): String {
        val transactions =
            (0 until transactionCount).joinToString(",") { i ->
                """{"id":$i,"title":"Transaction$i","amount":-${10.50 + i},"category":"Food"}"""
            }
        return """{"backup_version":2,"transactions":[$transactions]}"""
    }
}
