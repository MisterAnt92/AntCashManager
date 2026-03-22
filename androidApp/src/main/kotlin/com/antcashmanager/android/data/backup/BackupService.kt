package com.antcashmanager.android.data.backup

import co.touchlab.kermit.Logger
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Service responsible for backup and restore operations.
 * Handles serialization/deserialization of app data to JSON format.
 */
class BackupService(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
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
     */
    suspend fun restoreBackup(jsonString: String): Result<RestoreResult> = withContext(Dispatchers.IO) {
        try {
            Logger.d("BackupService") { "Restoring backup..." }

            val backupData = json.decodeFromString<BackupData>(jsonString)

            // Validate version
            if (backupData.version > BackupData.CURRENT_VERSION) {
                return@withContext Result.failure(
                    IllegalStateException("Backup version ${backupData.version} is not supported. Please update the app."),
                )
            }

            // Clear existing data
            transactionRepository.deleteAllTransactions()
            categoryRepository.deleteAllCategories()

            // Restore categories first (transactions reference them)
            var categoriesRestored = 0
            for (categoryBackup in backupData.categories) {
                try {
                    categoryRepository.insertCategory(categoryBackup.toCategory())
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

            Logger.d("BackupService") { "Restore completed: $transactionsRestored transactions, $categoriesRestored categories" }
            Result.success(
                RestoreResult(
                    transactionsRestored = transactionsRestored,
                    categoriesRestored = categoriesRestored,
                ),
            )
        } catch (e: Exception) {
            Logger.e("BackupService") { "Error restoring backup: ${e.message}" }
            Result.failure(e)
        }
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
}

/**
 * Result of a restore operation.
 */
data class RestoreResult(
    val transactionsRestored: Int,
    val categoriesRestored: Int,
)

