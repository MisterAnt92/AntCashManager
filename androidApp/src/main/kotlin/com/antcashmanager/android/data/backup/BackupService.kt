package com.antcashmanager.android.data.backup

import co.touchlab.kermit.Logger
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.BackupDestination
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.service.NoOpWidgetUpdateNotifier
import com.antcashmanager.domain.service.WidgetUpdateNotifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import java.util.Locale

/**
 * Service responsible for backup and restore operations.
 * Handles serialization/deserialization of app data to JSON format.
 */
class BackupService(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
    private val widgetUpdateNotifier: WidgetUpdateNotifier = NoOpWidgetUpdateNotifier,
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Creates a backup of all app data and returns it as a JSON string.
     */
    suspend fun createBackup(): Result<String> = withContext(Dispatchers.IO) {
        try {
            Logger.d(tag = "BackupService") { "Creating backup..." }

            val transactions = transactionRepository.getAllTransactions().first()
            val categories = categoryRepository.getAllCategories().first()

            val backupData = BackupData(
                transactions = transactions.map { it.toBackup() },
                categories = categories.map { it.toBackup() },
                settings = buildSettingsBackup(),
            )

            val jsonString = json.encodeToString(backupData)
            Logger.d(tag = "BackupService") { "Backup created: ${transactions.size} transactions, ${categories.size} categories" }
            Result.success(jsonString)
        } catch (e: Exception) {
            Logger.e(tag = "BackupService") { "Error creating backup: ${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Restores app data from a JSON string backup.
     * This will REPLACE all existing data.
     *
     * Il parsing è a due livelli per massimizzare la retrocompatibilità: si tenta prima una
     * decodifica rigorosa dell'intero payload; se fallisce (es. un singolo campo con tipo
     * inatteso, versione futura con struttura leggermente diversa, file parzialmente
     * corrotto) si ripiega su [parseBackupDataLeniently], che decodifica transazioni e
     * categorie **una per una**, scartando solo le voci realmente illeggibili invece di
     * abortire l'intero restore. Una versione superiore a quella supportata non blocca più il
     * restore: si importa comunque tutto ciò che è strutturalmente compatibile.
     *
     * Prima di cancellare i dati esistenti ne viene catturato uno snapshot in memoria: se il
     * ripristino fallisce con un'eccezione non recuperabile, si tenta un rollback best-effort
     * riscrivendo lo snapshot. Il rollback stesso non è atomico (nessuna transazione DB
     * cross-repository è disponibile attraverso le interfacce di dominio attuali): copre il
     * caso comune di un'eccezione gestita a metà del processo, non un crash/kill del processo.
     */
    suspend fun restoreBackup(jsonString: String): Result<RestoreResult> =
        withContext(Dispatchers.IO) {
            val backupData = try {
                json.decodeFromString<BackupData>(jsonString)
            } catch (e: Exception) {
                Logger.w(tag = "BackupService") { "Strict backup parsing failed, falling back to lenient per-field parsing: ${e.message}" }
                try {
                    parseBackupDataLeniently(jsonString)
                } catch (fallbackError: Exception) {
                    Logger.e(tag = "BackupService") { "Lenient parsing also failed: ${fallbackError.message}" }
                    return@withContext Result.failure(e)
                }
            }

            if (backupData.version > BackupConstants.CURRENT_VERSION) {
                Logger.w(tag = "BackupService") {
                    "Backup version ${backupData.version} is newer than supported (${BackupConstants.CURRENT_VERSION}); " +
                            "importing best-effort using only the fields this app version understands."
                }
            }

            val existingTransactionsSnapshot = transactionRepository.getAllTransactions().first()
            val existingCategoriesSnapshot = categoryRepository.getAllCategories().first()

            try {
                Logger.d(tag = "BackupService") { "Restoring backup..." }

                // Clear existing data
                transactionRepository.deleteAllTransactions()
                categoryRepository.deleteAllCategories()

                // Restore categories first (transactions reference them).
                // deleteAllCategories() preserva le categorie isDefault: bisogna quindi
                // ripartire dalle categorie effettivamente sopravvissute, non da un set vuoto.
                val existingCategoryKeys = categoryRepository.getAllCategories()
                    .first()
                    .mapTo(mutableSetOf()) { category ->
                        toCategoryKey(category.name, category.type)
                    }
                var categoriesRestored = 0
                for (categoryBackup in backupData.categories) {
                    val categoryKey = toCategoryKey(categoryBackup.name, categoryBackup.type)
                    if (categoryKey in existingCategoryKeys) {
                        categoriesRestored++
                        continue
                    }

                    try {
                        categoryRepository.insertCategory(categoryBackup.toCategory())
                        existingCategoryKeys.add(categoryKey)
                        categoriesRestored++
                    } catch (e: Exception) {
                        Logger.w(tag = "BackupService") { "Failed to restore category: ${categoryBackup.name}" }
                    }
                }

                // Restore transactions
                var transactionsRestored = 0
                for (transactionBackup in backupData.transactions) {
                    try {
                        transactionRepository.insertTransaction(transactionBackup.toTransaction())
                        transactionsRestored++
                    } catch (e: Exception) {
                        Logger.w(tag = "BackupService") { "Failed to restore transaction: ${transactionBackup.title}" }
                    }
                }

                // Restore settings, se presenti (assenti in un backup v1 o se esplicitamente null)
                backupData.settings?.let {
                    applySettings(it)
                    // Assicura il refresh dei widget anche se il backup non contiene
                    // transazioni (in quel caso insertTransaction non lo farebbe scattare).
                    widgetUpdateNotifier.notifyTransactionsChanged()
                }

                Logger.d(tag = "BackupService") { "Restore completed: $transactionsRestored transactions, $categoriesRestored categories" }
                Result.success(
                    RestoreResult(
                        transactionsRestored = transactionsRestored,
                        categoriesRestored = categoriesRestored,
                    ),
                )
            } catch (e: Exception) {
                Logger.e(tag = "BackupService") { "Error restoring backup, attempting rollback: ${e.message}" }
                runCatching {
                    // deleteAllCategories() preserva le categorie isDefault: quelle sopravvivono
                    // già alla cancellazione, quindi vanno escluse dal reinserimento per non
                    // duplicarle.
                    categoryRepository.deleteAllCategories()
                    existingCategoriesSnapshot.filterNot { it.isDefault }
                        .forEach { categoryRepository.insertCategory(it) }
                    transactionRepository.deleteAllTransactions()
                    existingTransactionsSnapshot.forEach {
                        transactionRepository.insertTransaction(
                            it
                        )
                    }
                }.onFailure { rollbackError ->
                    Logger.e(tag = "BackupService") { "Rollback also failed — data may be inconsistent: ${rollbackError.message}" }
                }
                Result.failure(e)
            }
        }

    /**
     * Parsing tollerante usato come fallback quando la decodifica rigorosa di [BackupData]
     * fallisce. Legge la struttura campo per campo direttamente dal JSON grezzo e decodifica
     * ogni transazione/categoria singolarmente, scartando (con log) solo le voci che non si
     * possono interpretare — invece di perdere l'intero backup per un singolo record
     * malformato o per un formato di versione futura leggermente diverso.
     */
    private fun parseBackupDataLeniently(jsonString: String): BackupData {
        val root = json.parseToJsonElement(jsonString).jsonObject

        val version = root["version"]?.jsonPrimitive?.intOrNull ?: 1
        val timestamp = root["timestamp"]?.jsonPrimitive?.longOrNull ?: System.currentTimeMillis()

        val transactions = root["transactions"]?.jsonArray.orEmpty().mapNotNull { element ->
            runCatching { json.decodeFromJsonElement<TransactionBackup>(element) }
                .onFailure { Logger.w(tag = "BackupService") { "Skipping unreadable transaction entry: ${it.message}" } }
                .getOrNull()
        }

        val categories = root["categories"]?.jsonArray.orEmpty().mapNotNull { element ->
            runCatching { json.decodeFromJsonElement<CategoryBackup>(element) }
                .onFailure { Logger.w(tag = "BackupService") { "Skipping unreadable category entry: ${it.message}" } }
                .getOrNull()
        }

        val settings = root["settings"]
            ?.takeIf { it != JsonNull }
            ?.let { element ->
                runCatching { json.decodeFromJsonElement<SettingsBackup>(element) }
                    .onFailure { Logger.w(tag = "BackupService") { "Skipping unreadable settings block: ${it.message}" } }
                    .getOrNull()
            }

        return BackupData(
            version = version,
            timestamp = timestamp,
            transactions = transactions,
            categories = categories,
            settings = settings,
        )
    }

    private suspend fun buildSettingsBackup(): SettingsBackup = SettingsBackup(
        theme = settingsRepository.getTheme().first().name,
        language = settingsRepository.getLanguage().first().name,
        highContrast = settingsRepository.getHighContrast().first(),
        largeText = settingsRepository.getLargeText().first(),
        reduceMotion = settingsRepository.getReduceMotion().first(),
        showCharts = settingsRepository.getShowCharts().first(),
        showTransactionNotes = settingsRepository.getShowTransactionNotes().first(),
        maskAmounts = settingsRepository.getMaskAmounts().first(),
        showPaymentTypeBreakdown = settingsRepository.getShowPaymentTypeBreakdown().first(),
        showQuickInsightsCard = settingsRepository.getShowQuickInsightsCard().first(),
        showInitialAnimation = settingsRepository.getShowInitialAnimation().first(),
        transactionDisplayType = settingsRepository.getTransactionDisplayType().first().name,
        transactionsTransactionDisplayType = settingsRepository.getTransactionsTransactionDisplayType()
            .first().name,
        currencySymbol = settingsRepository.getCurrencySymbol().first(),
        decimalDigits = settingsRepository.getDecimalDigits().first(),
        decimalSeparator = settingsRepository.getDecimalSeparator().first(),
        thousandsSeparator = settingsRepository.getThousandsSeparator().first(),
        mealVoucherValue = settingsRepository.getMealVoucherValue().first(),
        dateFormat = settingsRepository.getDateFormat().first(),
        chartsZoomEnabled = settingsRepository.getChartsZoomEnabled().first(),
        suggestionsEnabled = settingsRepository.getSuggestionsEnabled().first(),
        suggestionsClearedAt = settingsRepository.getSuggestionsClearedAt().first(),
        widgetBackgroundColor = settingsRepository.getWidgetBackgroundColor().first(),
        widgetOpacity = settingsRepository.getWidgetOpacity().first(),
        // ── v3+ fields ──
        chartCardsOrder = settingsRepository.getChartCardsOrder().first(),
        homeTopCardsOrder = settingsRepository.getHomeTopCardsOrder().first(),
        dataEncryptionEnabled = settingsRepository.getDataEncryptionEnabled().first(),
        // ── v4+ fields (Backup Destination & Google Drive Config) ──
        autoBackupEnabled = settingsRepository.getAutoBackupEnabled().first(),
        autoBackupDestination = settingsRepository.getAutoBackupDestination().first().name,
        autoBackupFolderUri = settingsRepository.getAutoBackupFolderUri().first(),
        googleDriveFolderId = settingsRepository.getGoogleDriveFolderId().first(),
        googleDriveUserEmail = settingsRepository.getGoogleDriveUserEmail().first(),
    )

    private suspend fun applySettings(settings: SettingsBackup) {
        settingsRepository.setTheme(enumValueOfOrDefault(settings.theme, AppTheme.SYSTEM))
        settingsRepository.setLanguage(enumValueOfOrDefault(settings.language, AppLanguage.SYSTEM))
        settingsRepository.setHighContrast(settings.highContrast)
        settingsRepository.setLargeText(settings.largeText)
        settingsRepository.setReduceMotion(settings.reduceMotion)
        settingsRepository.setShowCharts(settings.showCharts)
        settingsRepository.setShowTransactionNotes(settings.showTransactionNotes)
        settingsRepository.setMaskAmounts(settings.maskAmounts)
        settingsRepository.setShowPaymentTypeBreakdown(settings.showPaymentTypeBreakdown)
        settingsRepository.setShowQuickInsightsCard(settings.showQuickInsightsCard)
        settingsRepository.setShowInitialAnimation(settings.showInitialAnimation)
        settingsRepository.setTransactionDisplayType(
            enumValueOfOrDefault(settings.transactionDisplayType, TransactionDisplayType.TREND),
        )
        settingsRepository.setTransactionsTransactionDisplayType(
            enumValueOfOrDefault(
                settings.transactionsTransactionDisplayType,
                TransactionDisplayType.TREND
            ),
        )
        settingsRepository.setCurrencySymbol(settings.currencySymbol)
        settingsRepository.setDecimalDigits(settings.decimalDigits)
        settingsRepository.setDecimalSeparator(settings.decimalSeparator)
        settingsRepository.setThousandsSeparator(settings.thousandsSeparator)
        settingsRepository.setMealVoucherValue(settings.mealVoucherValue)
        settingsRepository.setDateFormat(settings.dateFormat)
        settingsRepository.setChartsZoomEnabled(settings.chartsZoomEnabled)
        settingsRepository.setSuggestionsEnabled(settings.suggestionsEnabled)
        // Nessun "unset": se il backup non ha una data di cancellazione (null), quella
        // corrente sul device resta invariata invece di essere azzerata.
        settings.suggestionsClearedAt?.let { settingsRepository.setSuggestionsClearedAt(it) }
        settingsRepository.setWidgetBackgroundColor(settings.widgetBackgroundColor)
        settingsRepository.setWidgetOpacity(settings.widgetOpacity)

        // ── v3+ fields ──
        // Card customization (empty string means use default order)
        if (settings.chartCardsOrder.isNotEmpty()) {
            settingsRepository.setChartCardsOrder(settings.chartCardsOrder)
        }
        if (settings.homeTopCardsOrder.isNotEmpty()) {
            settingsRepository.setHomeTopCardsOrder(settings.homeTopCardsOrder)
        }
        // CRITICAL: Encryption status must be preserved
        settingsRepository.setDataEncryptionEnabled(settings.dataEncryptionEnabled)

        // ── v4+ fields (Backup Destination & Google Drive Config) ──
        settingsRepository.setAutoBackupEnabled(settings.autoBackupEnabled)
        settingsRepository.setAutoBackupDestination(
            enumValueOfOrDefault(settings.autoBackupDestination, BackupDestination.LOCAL)
        )
        settings.autoBackupFolderUri?.let { settingsRepository.setAutoBackupFolderUri(it) }
        settings.googleDriveFolderId?.let { settingsRepository.setGoogleDriveFolderId(it) }
        settings.googleDriveUserEmail?.let { settingsRepository.setGoogleDriveUserEmail(it) }
    }

    private inline fun <reified T : Enum<T>> enumValueOfOrDefault(name: String, default: T): T =
        try {
            enumValueOf<T>(name)
        } catch (e: IllegalArgumentException) {
            default
        }

    private fun Transaction.toBackup() = TransactionBackup(
        id = id,
        title = title,
        amount = amount,
        category = category,
        type = type.name,
        timestamp = timestamp,
        notes = notes,
        payee = payee,
        location = location,
        isRecurring = isRecurring,
        tags = tags,
        recurrenceInterval = recurrenceInterval,
        paymentType = paymentType.name,
        mealVoucherCount = mealVoucherCount,
        mealVoucherDifference = mealVoucherDifference,
        categoryIcon = categoryIcon,
        categoryColor = categoryColor,
    )

    private fun TransactionBackup.toTransaction() = Transaction(
        id = 0, // Let Room auto-generate new IDs
        title = title,
        amount = amount,
        category = category,
        type = try {
            TransactionType.valueOf(type)
        } catch (e: Exception) {
            TransactionType.EXPENSE
        },
        timestamp = timestamp,
        notes = notes,
        payee = payee,
        location = location,
        isRecurring = isRecurring,
        tags = tags,
        recurrenceInterval = recurrenceInterval,
        paymentType = try {
            PaymentType.valueOf(paymentType)
        } catch (e: Exception) {
            PaymentType.ELECTRONIC
        },
        mealVoucherCount = mealVoucherCount,
        mealVoucherDifference = mealVoucherDifference,
        categoryIcon = categoryIcon,
        categoryColor = categoryColor,
    )

    private fun Category.toBackup() = CategoryBackup(
        id = id,
        name = name,
        icon = icon,
        color = color,
        type = type,
        isDefault = isDefault,
        sortOrder = sortOrder,
        isHidden = isHidden,
    )

    private fun CategoryBackup.toCategory() = Category(
        id = 0, // Let Room auto-generate new IDs
        name = name,
        icon = icon,
        color = color,
        type = type,
        isDefault = isDefault,
        sortOrder = sortOrder,
        isHidden = isHidden,
    )

    private fun toCategoryKey(name: String, type: String): String =
        "${name.trim().lowercase(Locale.ROOT)}|${type.trim().uppercase(Locale.ROOT)}"
}

/**
 * Result of a restore operation.
 */
data class RestoreResult(
    val transactionsRestored: Int,
    val categoriesRestored: Int,
)
