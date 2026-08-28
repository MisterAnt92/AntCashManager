package com.antcashmanager.android.data.backup

import com.antcashmanager.android.security.BackupPayloadCipher
import com.antcashmanager.android.testutil.FakeCategoryRepository
import com.antcashmanager.android.testutil.FakeSettingsRepository
import com.antcashmanager.android.testutil.FakeTransactionRepository
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.BackupDestination
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.service.WidgetUpdateNotifier
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupServiceTest {

    private fun buildService(
        transactionRepository: FakeTransactionRepository = FakeTransactionRepository(),
        categoryRepository: FakeCategoryRepository = FakeCategoryRepository(),
        settingsRepository: FakeSettingsRepository = FakeSettingsRepository(),
        widgetUpdateNotifier: WidgetUpdateNotifier = FakeWidgetUpdateNotifier(),
    ) = BackupService(
        transactionRepository,
        categoryRepository,
        settingsRepository,
        widgetUpdateNotifier
    )

    @Test
    fun createBackup_shouldProduceCurrentVersionWithNonNullSettings_always() = runTest {
        val service = buildService()

        val jsonString = service.createBackup().getOrThrow()
        val backupData = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            .decodeFromString<BackupData>(jsonString)

        assertEquals(BackupConstants.CURRENT_VERSION, backupData.version)
        assertTrue(backupData.settings != null)
    }

    @Test
    fun createBackupThenRestoreBackup_shouldPreserveAllTransactionAndCategoryFields_whenRoundTripped() =
        runTest {
            val sourceTransaction = Transaction(
                id = 1,
                title = "Spesa",
                amount = 42.5,
                category = "Alimentari",
                type = TransactionType.EXPENSE,
                timestamp = 1_700_000_000_000L,
                notes = "note",
                payee = "Coop",
                location = "Milano",
                isRecurring = true,
                tags = "casa,cibo",
                recurrenceInterval = "MONTHLY",
                paymentType = PaymentType.MEAL_VOUCHERS,
                mealVoucherCount = 3,
                categoryIcon = "fastfood",
                categoryColor = 0xFF00FF00,
            )
            val sourceCategory = Category(
                id = 1,
                name = "Alimentari",
                icon = "fastfood",
                color = 0xFF00FF00,
                type = "EXPENSE",
                isDefault = false,
                sortOrder = 4,
                isHidden = true,
            )
            val sourceService = buildService(
                transactionRepository = FakeTransactionRepository(listOf(sourceTransaction)),
                categoryRepository = FakeCategoryRepository(listOf(sourceCategory)),
            )
            val jsonString = sourceService.createBackup().getOrThrow()

            val targetTransactionRepo = FakeTransactionRepository()
            val targetCategoryRepo = FakeCategoryRepository()
            val targetService = buildService(
                transactionRepository = targetTransactionRepo,
                categoryRepository = targetCategoryRepo,
            )

            val result = targetService.restoreBackup(jsonString).getOrThrow()

            assertEquals(1, result.transactionsRestored)
            assertEquals(1, result.categoriesRestored)

            val restoredTransaction = targetTransactionRepo.transactions.value.single()
            assertEquals(sourceTransaction.title, restoredTransaction.title)
            assertEquals(sourceTransaction.amount, restoredTransaction.amount, 0.0)
            assertEquals(sourceTransaction.paymentType, restoredTransaction.paymentType)
            assertEquals(sourceTransaction.mealVoucherCount, restoredTransaction.mealVoucherCount)
            assertEquals(sourceTransaction.categoryIcon, restoredTransaction.categoryIcon)
            assertEquals(sourceTransaction.categoryColor, restoredTransaction.categoryColor)

            val restoredCategory = targetCategoryRepo.categories.value.single()
            assertEquals(sourceCategory.name, restoredCategory.name)
            assertEquals(sourceCategory.icon, restoredCategory.icon)
            assertEquals(sourceCategory.color, restoredCategory.color)
            assertEquals(sourceCategory.sortOrder, restoredCategory.sortOrder)
            assertEquals(sourceCategory.isHidden, restoredCategory.isHidden)
        }

    @Test
    fun createBackupThenRestoreBackup_shouldPreserveSettings_whenRoundTripped() = runTest {
        val sourceSettings = FakeSettingsRepository().apply {
            theme.value = AppTheme.DARK
            language.value = AppLanguage.ITALIAN
            highContrast.value = true
            largeText.value = true
            reduceMotion.value = true
            currencySymbol.value = "$"
            decimalDigits.value = 0
            mealVoucherValue.value = 7.5
            showPaymentTypeBreakdown.value = true
            chartsZoomEnabled.value = true
            maskAmounts.value = true
            suggestionsEnabled.value = false
            suggestionsClearedAt.value = 1_700_000_000_000L
            widgetBackgroundColor.value = 0xFF212121L
            widgetOpacity.value = 42
        }
        val sourceService = buildService(settingsRepository = sourceSettings)
        val jsonString = sourceService.createBackup().getOrThrow()

        val targetSettings = FakeSettingsRepository()
        val targetWidgetNotifier = FakeWidgetUpdateNotifier()
        val targetService = buildService(
            settingsRepository = targetSettings,
            widgetUpdateNotifier = targetWidgetNotifier,
        )

        targetService.restoreBackup(jsonString).getOrThrow()

        assertEquals(AppTheme.DARK, targetSettings.theme.value)
        assertEquals(AppLanguage.ITALIAN, targetSettings.language.value)
        assertTrue(targetSettings.highContrast.value)
        assertTrue(targetSettings.largeText.value)
        assertTrue(targetSettings.reduceMotion.value)
        assertEquals("$", targetSettings.currencySymbol.value)
        assertEquals(0, targetSettings.decimalDigits.value)
        assertEquals(7.5, targetSettings.mealVoucherValue.value, 0.0)
        assertTrue(targetSettings.showPaymentTypeBreakdown.value)
        assertTrue(targetSettings.chartsZoomEnabled.value)
        assertTrue(targetSettings.maskAmounts.value)
        assertFalse(targetSettings.suggestionsEnabled.value)
        assertEquals(1_700_000_000_000L, targetSettings.suggestionsClearedAt.value)
        assertEquals(0xFF212121L, targetSettings.widgetBackgroundColor.value)
        assertEquals(42, targetSettings.widgetOpacity.value)
        assertEquals(1, targetWidgetNotifier.notifyCount)
    }

    @Test
    fun restoreBackup_shouldApplySuggestionsAndWidgetDefaults_whenBackupIsV2WithoutThoseFields() =
        runTest {
            // Backup v2 "vecchio": blocco settings presente ma senza i campi aggiunti
            // successivamente (suggerimenti, aspetto widget).
            val legacyV2Json = """
            {
              "version": 2,
              "timestamp": 0,
              "transactions": [],
              "categories": [],
              "settings": {
                "theme": "DARK",
                "currencySymbol": "£"
              }
            }
        """.trimIndent()

            val targetSettings = FakeSettingsRepository().apply {
                suggestionsEnabled.value = false
                widgetBackgroundColor.value = 0xFF000000L
                widgetOpacity.value = 10
            }
            val targetWidgetNotifier = FakeWidgetUpdateNotifier()
            val service = buildService(
                settingsRepository = targetSettings,
                widgetUpdateNotifier = targetWidgetNotifier,
            )

            service.restoreBackup(legacyV2Json).getOrThrow()

            assertEquals(AppTheme.DARK, targetSettings.theme.value)
            assertEquals("£", targetSettings.currencySymbol.value)
            // Campi assenti dal backup: applicati ai default di SettingsBackup (non lasciati invariati,
            // coerente col comportamento di tutti gli altri campi del blocco "settings" presente).
            assertTrue(targetSettings.suggestionsEnabled.value)
            assertEquals(0xFFFFFFFFL, targetSettings.widgetBackgroundColor.value)
            assertEquals(100, targetSettings.widgetOpacity.value)
            assertEquals(1, targetWidgetNotifier.notifyCount)
        }

    @Test
    fun restoreBackup_shouldApplyDefaultsAndLeaveSettingsUnchanged_whenBackupIsLegacyV1WithoutNewFieldsOrSettings() =
        runTest {
            // Backup v1: nessun campo paymentType/mealVoucherCount/categoryIcon/categoryColor,
            // nessun blocco "settings".
            val legacyJson = """
                {
                  "version": 1,
                  "timestamp": 0,
                  "transactions": [
                    {"id": 1, "title": "Vecchia", "amount": 10.0, "category": "Varie", "type": "EXPENSE", "timestamp": 0}
                  ],
                  "categories": [
                    {"id": 1, "name": "Varie", "color": 100}
                  ]
                }
            """.trimIndent()

            val transactionRepo = FakeTransactionRepository()
            val categoryRepo = FakeCategoryRepository()
            val settingsRepo = FakeSettingsRepository().apply {
                theme.value = AppTheme.DARK
                currencySymbol.value = "$"
            }
            val service = buildService(transactionRepo, categoryRepo, settingsRepo)

            val result = service.restoreBackup(legacyJson).getOrThrow()

            assertEquals(1, result.transactionsRestored)
            val restored = transactionRepo.transactions.value.single()
            assertEquals(PaymentType.ELECTRONIC, restored.paymentType)
            assertEquals(0, restored.mealVoucherCount)
            assertEquals("", restored.categoryIcon)

            // Le impostazioni correnti non devono essere state toccate: nessun blocco "settings" nel backup v1.
            assertEquals(AppTheme.DARK, settingsRepo.theme.value)
            assertEquals("$", settingsRepo.currencySymbol.value)
        }

    @Test
    fun restoreBackup_shouldLeaveSettingsUnchanged_whenSettingsBlockIsExplicitlyNull() = runTest {
        val explicitNullJson = """
            {"version": 2, "timestamp": 0, "transactions": [], "categories": [], "settings": null}
        """.trimIndent()

        val settingsRepo = FakeSettingsRepository().apply {
            theme.value = AppTheme.DARK
        }
        val service = buildService(settingsRepository = settingsRepo)

        service.restoreBackup(explicitNullJson).getOrThrow()

        assertEquals(AppTheme.DARK, settingsRepo.theme.value)
    }

    @Test
    fun restoreBackup_shouldImportKnownFieldsBestEffort_whenVersionIsNewerThanSupported() =
        runTest {
            // Simula un backup creato da una versione futura dell'app: la struttura dei campi
            // conosciuti (transactions/categories) è ancora compatibile, solo il numero di
            // versione è più alto. Non deve più bloccare il restore.
            val futureJson = """
            {
              "version": 99, "timestamp": 0,
              "transactions": [
                {"id": 1, "title": "Dal futuro", "amount": 15.0, "category": "Varie", "type": "EXPENSE", "timestamp": 0}
              ],
              "categories": [{"id": 1, "name": "Varie", "color": 100}]
            }
        """.trimIndent()
            val transactionRepo = FakeTransactionRepository()
            val categoryRepo = FakeCategoryRepository()
            val service = buildService(transactionRepo, categoryRepo)

            val result = service.restoreBackup(futureJson).getOrThrow()

            assertEquals(1, result.transactionsRestored)
            assertEquals(1, result.categoriesRestored)
            assertEquals("Dal futuro", transactionRepo.transactions.value.single().title)
        }

    @Test
    fun restoreBackup_shouldSkipOnlyTheUnreadableTransaction_whenStrictDecodeOfWholePayloadFails() =
        runTest {
            // La seconda transazione manca di campi obbligatori (amount/category/type/timestamp):
            // una decodifica rigorosa dell'intera lista fallirebbe, perdendo anche la prima
            // transazione, valida. Il fallback lenient deve invece importare solo quella leggibile.
            val malformedJson = """
            {
              "version": 2, "timestamp": 0,
              "transactions": [
                {"id": 1, "title": "Valida", "amount": 10.0, "category": "Varie", "type": "EXPENSE", "timestamp": 0},
                {"id": 2, "title": "Corrotta"}
              ],
              "categories": [{"id": 1, "name": "Varie", "color": 100}]
            }
        """.trimIndent()
            val transactionRepo = FakeTransactionRepository()
            val categoryRepo = FakeCategoryRepository()
            val service = buildService(transactionRepo, categoryRepo)

            val result = service.restoreBackup(malformedJson).getOrThrow()

            assertEquals(1, result.transactionsRestored)
            assertEquals(1, result.categoriesRestored)
            assertEquals("Valida", transactionRepo.transactions.value.single().title)
        }

    @Test
    fun restoreBackup_shouldFail_whenPayloadIsNotValidJsonAtAll() = runTest {
        val transactionRepo = FakeTransactionRepository()
        val categoryRepo = FakeCategoryRepository()
        val service = buildService(transactionRepo, categoryRepo)

        val result = service.restoreBackup("questo non è JSON")

        assertTrue(result.isFailure)
        assertTrue(transactionRepo.transactions.value.isEmpty())
        assertTrue(categoryRepo.categories.value.isEmpty())
    }

    @Test
    fun restoreBackup_shouldDeduplicateCategoriesCaseInsensitively_whenCategoryAlreadyExistsAsDefault() =
        runTest {
            val existingDefault = Category(
                id = 1,
                name = "Cibo",
                icon = "food",
                color = 0xFF112233,
                type = "EXPENSE",
                isDefault = true,
            )
            val categoryRepo = FakeCategoryRepository(listOf(existingDefault))
            val service = buildService(categoryRepository = categoryRepo)

            val backupJson = """
            {
              "version": 2, "timestamp": 0, "transactions": [],
              "categories": [{"id": 5, "name": "cibo ", "icon": "x", "color": 100, "type": "expense", "isDefault": false}]
            }
        """.trimIndent()

            val result = service.restoreBackup(backupJson).getOrThrow()

            assertEquals(1, result.categoriesRestored)
            assertEquals(1, categoryRepo.categories.value.size)
            assertEquals("Cibo", categoryRepo.categories.value.single().name)
        }

    private class SelectivelyFailingTransactionRepository(
        private val failingTitle: String,
    ) : FakeTransactionRepository() {
        override suspend fun insertTransaction(transaction: Transaction): Long {
            if (transaction.title == failingTitle) throw IllegalStateException("boom")
            return super.insertTransaction(transaction)
        }
    }

    @Test
    fun restoreBackup_shouldSkipFailingRecordAndContinue_whenSingleTransactionInsertFails() =
        runTest {
            val good = Transaction(
                id = 1,
                title = "Good",
                amount = 10.0,
                category = "Food",
                type = TransactionType.EXPENSE
            )
            val bad = Transaction(
                id = 2,
                title = "Bad",
                amount = 20.0,
                category = "Food",
                type = TransactionType.EXPENSE
            )
            val sourceService = buildService(
                transactionRepository = FakeTransactionRepository(listOf(good, bad)),
            )
            val jsonString = sourceService.createBackup().getOrThrow()

            val targetTransactionRepo =
                SelectivelyFailingTransactionRepository(failingTitle = "Bad")
            val targetService = buildService(transactionRepository = targetTransactionRepo)

            val result = targetService.restoreBackup(jsonString).getOrThrow()

            assertEquals(1, result.transactionsRestored)
            assertEquals("Good", targetTransactionRepo.transactions.value.single().title)
        }

    private class ThrowingOnThemeSettingsRepository : FakeSettingsRepository() {
        override suspend fun setTheme(theme: AppTheme) {
            throw IllegalStateException("settings write failed")
        }
    }

    private class FakeWidgetUpdateNotifier : WidgetUpdateNotifier {
        var notifyCount = 0
            private set

        override suspend fun notifyTransactionsChanged() {
            notifyCount++
        }
    }

    @Test
    fun restoreBackup_shouldRollbackToOriginalData_whenApplyingSettingsFailsAfterDataWasReplaced() =
        runTest {
            val existingTransaction = Transaction(
                id = 1,
                title = "Esistente",
                amount = 5.0,
                category = "Varie",
                type = TransactionType.EXPENSE
            )
            val existingCategory =
                Category(id = 1, name = "Varie", color = 0xFF000000, isDefault = true)

            val sourceService = buildService(
                transactionRepository = FakeTransactionRepository(
                    listOf(
                        Transaction(
                            id = 1,
                            title = "Nuova",
                            amount = 1.0,
                            category = "Varie",
                            type = TransactionType.EXPENSE
                        )
                    ),
                ),
            )
            val jsonString = sourceService.createBackup().getOrThrow()

            val targetTransactionRepo = FakeTransactionRepository(listOf(existingTransaction))
            val targetCategoryRepo = FakeCategoryRepository(listOf(existingCategory))
            val targetSettingsRepo = ThrowingOnThemeSettingsRepository()
            val targetService =
                buildService(targetTransactionRepo, targetCategoryRepo, targetSettingsRepo)

            val result = targetService.restoreBackup(jsonString)

            assertTrue(result.isFailure)
            assertEquals(listOf(existingTransaction), targetTransactionRepo.transactions.value)
            assertEquals(listOf(existingCategory), targetCategoryRepo.categories.value)
        }

    @Test
    fun createBackupThenRestoreBackup_shouldRegenerateSuggestionsFromRestoredTransactions_always() =
        runTest {
            val sourceTransactions = listOf(
                Transaction(
                    id = 1,
                    title = "Spesa",
                    amount = 10.0,
                    category = "Cibo",
                    type = TransactionType.EXPENSE,
                    payee = "Coop",
                    tags = "casa"
                ),
                Transaction(
                    id = 2,
                    title = "Benzina",
                    amount = 20.0,
                    category = "Trasporti",
                    type = TransactionType.EXPENSE,
                    location = "Stazione"
                ),
            )
            val sourceService =
                buildService(transactionRepository = FakeTransactionRepository(sourceTransactions))
            val expectedTitles =
                FakeTransactionRepository(sourceTransactions).getDistinctTitles().first()
            val jsonString = sourceService.createBackup().getOrThrow()

            val targetTransactionRepo = FakeTransactionRepository()
            val targetService = buildService(transactionRepository = targetTransactionRepo)
            targetService.restoreBackup(jsonString).getOrThrow()

            assertEquals(
                expectedTitles.toSet(),
                targetTransactionRepo.getDistinctTitles().first().toSet()
            )
            assertFalse(targetTransactionRepo.getDistinctTitles().first().isEmpty())
        }

    @Test
    fun createBackupThenRestoreBackup_shouldPreserveDataWhenEncrypted_roundTrip() =
        runTest {
            // Setup: create encrypted backup
            val sourceTransaction = Transaction(
                id = 1,
                title = "Spesa",
                amount = 42.5,
                category = "Alimentari",
                type = TransactionType.EXPENSE,
                timestamp = 1_700_000_000_000L,
            )
            val sourceService = buildService(
                transactionRepository = FakeTransactionRepository(listOf(sourceTransaction)),
            )
            val jsonString = sourceService.createBackup().getOrThrow()

            // Encrypt the backup
            val encryptedPayload = BackupPayloadCipher.encrypt(jsonString)
            assertTrue(BackupPayloadCipher.isEncryptedPayload(encryptedPayload))

            // Decrypt and restore
            val decryptedPayload = BackupPayloadCipher.decrypt(encryptedPayload)

            val targetTransactionRepo = FakeTransactionRepository()
            val targetService = buildService(transactionRepository = targetTransactionRepo)
            val result = targetService.restoreBackup(decryptedPayload).getOrThrow()

            assertEquals(1, result.transactionsRestored)
            assertEquals(sourceTransaction.title, targetTransactionRepo.transactions.value.single().title)
        }

    @Test
    fun restoreBackup_shouldFailGracefully_whenEncryptedPayloadIsCorrupted() = runTest {
        val sourceTransaction = Transaction(
            id = 1,
            title = "Test",
            amount = 10.0,
            category = "Test",
            type = TransactionType.EXPENSE,
        )
        val sourceService = buildService(
            transactionRepository = FakeTransactionRepository(listOf(sourceTransaction)),
        )
        val jsonString = sourceService.createBackup().getOrThrow()

        // Encrypt
        val encryptedPayload = BackupPayloadCipher.encrypt(jsonString)

        // Corrupt the payload
        val parts = encryptedPayload.removePrefix("ACM_ENC_V1:").split(":", limit = 2)
        val corruptedData = parts[1].dropLast(1) + "X"
        val corruptedPayload = "ACM_ENC_V1:${parts[0]}:$corruptedData"

        val targetService = buildService()
        val result = runCatching {
            BackupPayloadCipher.decrypt(corruptedPayload)
        }

        assertTrue(result.isFailure)
    }

    @Test
    fun createBackupThenRestoreBackup_shouldPreserveGoogleDriveConfig_whenRoundTripped() = runTest {
        // Setup: abilita Google Drive backup nel sourceSettings
        val sourceSettings = FakeSettingsRepository().apply {
            autoBackupEnabled.value = true
            autoBackupDestination.value = BackupDestination.GOOGLE_DRIVE
            autoBackupFolderUri.value = "content://com.android.externalstorage.documents/tree/primary%3AAntCashManager"
            googleDriveFolderId.value = "folder_id_12345"
            googleDriveUserEmail.value = "user@gmail.com"
        }

        val sourceService = buildService(settingsRepository = sourceSettings)
        val backupJson = sourceService.createBackup().getOrThrow()

        // Restore in targetSettings
        val targetSettings = FakeSettingsRepository()
        val targetService = buildService(settingsRepository = targetSettings)
        targetService.restoreBackup(backupJson).getOrThrow()

        // Assert tutti i 5 campi sono preservati
        assertTrue(targetSettings.autoBackupEnabled.value)
        assertEquals(BackupDestination.GOOGLE_DRIVE, targetSettings.autoBackupDestination.value)
        assertEquals("content://com.android.externalstorage.documents/tree/primary%3AAntCashManager",
            targetSettings.autoBackupFolderUri.value)
        assertEquals("folder_id_12345", targetSettings.googleDriveFolderId.value)
        assertEquals("user@gmail.com", targetSettings.googleDriveUserEmail.value)
    }

    @Test
    fun restoreBackup_shouldApplyDefaults_whenBackupIsV3WithoutGoogleDriveFields() = runTest {
        // Simula un backup v3 senza i campi v4
        val legacyV3Json = """
            {
              "version": 3,
              "timestamp": 1234567890000,
              "transactions": [],
              "categories": [],
              "settings": {
                "theme": "DARK",
                "language": "ITALIAN",
                "currencySymbol": "€"
              }
            }
        """.trimIndent()

        val targetSettings = FakeSettingsRepository()
        val service = buildService(settingsRepository = targetSettings)
        service.restoreBackup(legacyV3Json).getOrThrow()

        // Assert defaults applicati per campi v4
        assertFalse(targetSettings.autoBackupEnabled.value)
        assertEquals(BackupDestination.LOCAL, targetSettings.autoBackupDestination.value)
        assertNull(targetSettings.autoBackupFolderUri.value)
        assertNull(targetSettings.googleDriveFolderId.value)
        assertNull(targetSettings.googleDriveUserEmail.value)
    }
}
