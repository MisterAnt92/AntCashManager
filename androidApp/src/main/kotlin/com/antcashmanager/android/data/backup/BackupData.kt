package com.antcashmanager.android.data.backup

import kotlinx.serialization.Serializable

/**
 * Data class representing the backup structure for import/export.
 * Contains all app data that needs to be persisted.
 */
@Serializable
data class BackupData(
    val version: Int = CURRENT_VERSION,
    val timestamp: Long = System.currentTimeMillis(),
    val transactions: List<TransactionBackup> = emptyList(),
    val categories: List<CategoryBackup> = emptyList(),
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

/**
 * Serializable representation of a Transaction for backup purposes.
 */
@Serializable
data class TransactionBackup(
    val id: Long,
    val title: String,
    val amount: Double,
    val category: String,
    val type: String,
    val timestamp: Long,
    val notes: String = "",
    val payee: String = "",
    val location: String = "",
    val isRecurring: Boolean = false,
    val tags: String = "",
    val recurrenceInterval: String = "",
)

/**
 * Serializable representation of a Category for backup purposes.
 */
@Serializable
data class CategoryBackup(
    val id: Long,
    val name: String,
    val icon: String = "category",
    val color: Long,
    val type: String = "EXPENSE",
    val isDefault: Boolean = false,
)

