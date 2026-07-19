package com.antcashmanager.android.data.backup

import co.touchlab.kermit.Logger
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Locale

/**
 * Service responsible for backup and restore operations.
 * Handles serialization/deserialization of app data to JSON format.
 */
class BackupService(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
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
            Logger.d("BackupService") { "Creating backup..." }

            val transactions = transactionRepository.getAllTransactions().first()
            val categories = categoryRepository.getAllCategories().first()

            val backupData = BackupData(
                transactions = transactions.map { it.toBackup() },
                categories = categories.map { it.toBackup() },
                settings = buildSettingsBackup(),
            )

            val jsonString = json.encodeToString(backupData)
            Logger.d("BackupService") { "Backup created: ${transactions.size} transactions, ${categories.size} categories" }
            Result.success(jsonString)
        } catch (e: Exception) {
            Logger.e("BackupService") { "Error creating backup: ${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Restores app data from a JSON string backup.
     * This will REPLACE all existing data.
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
                Logger.e("BackupService") { "Error parsing backup: ${e.message}" }
                return@withContext Result.failure(e)
            }

            // Validate version
            if (backupData.version > BackupConstants.CURRENT_VERSION) {
                return@withContext Result.failure(
                    IllegalStateException("Backup version ${backupData.version} is not supported. Please update the app."),
                )
            }

            val existingTransactionsSnapshot = transactionRepository.getAllTransactions().first()
            val existingCategoriesSnapshot = categoryRepository.getAllCategories().first()

            try {
                Logger.d("BackupService") { "Restoring backup..." }

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
                        Logger.w("BackupService") { "Failed to restore category: ${categoryBackup.name}" }
                    }
                }

                // Restore transactions
                var transactionsRestored = 0
                for (transactionBackup in backupData.transactions) {
                    try {
                        transactionRepository.insertTransaction(transactionBackup.toTransaction())
                        transactionsRestored++
                    } catch (e: Exception) {
                        Logger.w("BackupService") { "Failed to restore transaction: ${transactionBackup.title}" }
                    }
                }

                // Restore settings, se presenti (assenti in un backup v1 o se esplicitamente null)
                backupData.settings?.let { applySettings(it) }

                Logger.d("BackupService") { "Restore completed: $transactionsRestored transactions, $categoriesRestored categories" }
                Result.success(
                    RestoreResult(
                        transactionsRestored = transactionsRestored,
                        categoriesRestored = categoriesRestored,
                    ),
                )
            } catch (e: Exception) {
                Logger.e("BackupService") { "Error restoring backup, attempting rollback: ${e.message}" }
                runCatching {
                    // deleteAllCategories() preserva le categorie isDefault: quelle sopravvivono
                    // già alla cancellazione, quindi vanno escluse dal reinserimento per non
                    // duplicarle.
                    categoryRepository.deleteAllCategories()
                    existingCategoriesSnapshot.filterNot { it.isDefault }
                        .forEach { categoryRepository.insertCategory(it) }
                    transactionRepository.deleteAllTransactions()
                    existingTransactionsSnapshot.forEach { transactionRepository.insertTransaction(it) }
                }.onFailure { rollbackError ->
                    Logger.e("BackupService") { "Rollback also failed — data may be inconsistent: ${rollbackError.message}" }
                }
                Result.failure(e)
            }
        }

    private suspend fun buildSettingsBackup(): SettingsBackup = SettingsBackup(
        theme = settingsRepository.getTheme().first().name,
        language = settingsRepository.getLanguage().first().name,
        highContrast = settingsRepository.getHighContrast().first(),
        largeText = settingsRepository.getLargeText().first(),
        reduceMotion = settingsRepository.getReduceMotion().first(),
        showCharts = settingsRepository.getShowCharts().first(),
        showTransactionNotes = settingsRepository.getShowTransactionNotes().first(),
        showPaymentTypeBreakdown = settingsRepository.getShowPaymentTypeBreakdown().first(),
        showQuickInsightsCard = settingsRepository.getShowQuickInsightsCard().first(),
        showInitialAnimation = settingsRepository.getShowInitialAnimation().first(),
        transactionDisplayType = settingsRepository.getTransactionDisplayType().first().name,
        transactionsTransactionDisplayType = settingsRepository.getTransactionsTransactionDisplayType().first().name,
        currencySymbol = settingsRepository.getCurrencySymbol().first(),
        decimalDigits = settingsRepository.getDecimalDigits().first(),
        decimalSeparator = settingsRepository.getDecimalSeparator().first(),
        thousandsSeparator = settingsRepository.getThousandsSeparator().first(),
        mealVoucherValue = settingsRepository.getMealVoucherValue().first(),
        dateFormat = settingsRepository.getDateFormat().first(),
        chartsZoomEnabled = settingsRepository.getChartsZoomEnabled().first(),
    )

    private suspend fun applySettings(settings: SettingsBackup) {
        settingsRepository.setTheme(enumValueOfOrDefault(settings.theme, AppTheme.SYSTEM))
        settingsRepository.setLanguage(enumValueOfOrDefault(settings.language, AppLanguage.SYSTEM))
        settingsRepository.setHighContrast(settings.highContrast)
        settingsRepository.setLargeText(settings.largeText)
        settingsRepository.setReduceMotion(settings.reduceMotion)
        settingsRepository.setShowCharts(settings.showCharts)
        settingsRepository.setShowTransactionNotes(settings.showTransactionNotes)
        settingsRepository.setShowPaymentTypeBreakdown(settings.showPaymentTypeBreakdown)
        settingsRepository.setShowQuickInsightsCard(settings.showQuickInsightsCard)
        settingsRepository.setShowInitialAnimation(settings.showInitialAnimation)
        settingsRepository.setTransactionDisplayType(
            enumValueOfOrDefault(settings.transactionDisplayType, TransactionDisplayType.TREND),
        )
        settingsRepository.setTransactionsTransactionDisplayType(
            enumValueOfOrDefault(settings.transactionsTransactionDisplayType, TransactionDisplayType.TREND),
        )
        settingsRepository.setCurrencySymbol(settings.currencySymbol)
        settingsRepository.setDecimalDigits(settings.decimalDigits)
        settingsRepository.setDecimalSeparator(settings.decimalSeparator)
        settingsRepository.setThousandsSeparator(settings.thousandsSeparator)
        settingsRepository.setMealVoucherValue(settings.mealVoucherValue)
        settingsRepository.setDateFormat(settings.dateFormat)
        settingsRepository.setChartsZoomEnabled(settings.chartsZoomEnabled)
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
    )

    private fun CategoryBackup.toCategory() = Category(
        id = 0, // Let Room auto-generate new IDs
        name = name,
        icon = icon,
        color = color,
        type = type,
        isDefault = isDefault,
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
