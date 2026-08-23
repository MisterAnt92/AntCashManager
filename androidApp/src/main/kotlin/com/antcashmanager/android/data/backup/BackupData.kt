package com.antcashmanager.android.data.backup

import kotlinx.serialization.Serializable

/**
 * Data class representing the backup structure for import/export.
 * Contains all app data that needs to be persisted.
 *
 * [settings] è nullable: un backup v1 non lo contiene affatto, e in fase di restore la sua
 * assenza (`null`) significa esplicitamente "non toccare le impostazioni correnti", a
 * differenza di un blocco presente con valori di default.
 */
@Serializable
data class BackupData(
    val version: Int = BackupConstants.CURRENT_VERSION,
    val timestamp: Long = System.currentTimeMillis(),
    val transactions: List<TransactionBackup> = emptyList(),
    val categories: List<CategoryBackup> = emptyList(),
    val settings: SettingsBackup? = null,
)

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
    val paymentType: String = "ELECTRONIC",
    val mealVoucherCount: Int = 0,
    val mealVoucherDifference: Double = 0.0,
    val categoryIcon: String = "",
    val categoryColor: Long = 0xFF90A4AE,
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
    val sortOrder: Int = 0,
    val isHidden: Boolean = false,
)

/**
 * Serializable representation delle impostazioni dell'app per backup purposes.
 * Copre tema/lingua/accessibilità/visualizzazione/ordinamento carte/encryption/backup config.
 * Esclude deliberatamente: stato dei filtri data per schermata, tutorial completato,
 * token di autenticazione Google (authToken, refreshToken - sensibili).
 */
@Serializable
data class SettingsBackup(
    val theme: String = "SYSTEM",
    val language: String = "SYSTEM",
    val highContrast: Boolean = false,
    val largeText: Boolean = false,
    val reduceMotion: Boolean = false,
    val showCharts: Boolean = true,
    val showTransactionNotes: Boolean = true,
    val maskAmounts: Boolean = false,
    val showPaymentTypeBreakdown: Boolean = false,
    val showQuickInsightsCard: Boolean = false,
    val showInitialAnimation: Boolean = false,
    val transactionDisplayType: String = "TREND",
    val transactionsTransactionDisplayType: String = "TREND",
    val currencySymbol: String = "€",
    val decimalDigits: Int = 2,
    val decimalSeparator: String = ",",
    val thousandsSeparator: String = "",
    val mealVoucherValue: Double = 5.29,
    val dateFormat: String = "dd/MM/yyyy",
    val chartsZoomEnabled: Boolean = false,
    val suggestionsEnabled: Boolean = true,
    val suggestionsClearedAt: Long? = null,
    val widgetBackgroundColor: Long = 0xFFFFFFFFL,
    val widgetOpacity: Int = 100,
    // ── Card Customization (v2+) ──
    val chartCardsOrder: String = "", // Comma-separated chart card type storage keys
    val homeTopCardsOrder: String = "", // Comma-separated home top card type storage keys
    // ── Security (v3+) ──
    val dataEncryptionEnabled: Boolean = false, // Whether backup data encryption is enabled
    // ── Backup Destination & Google Drive Config (v4+) ──
    val autoBackupEnabled: Boolean = false,
    val autoBackupDestination: String = "LOCAL", // Enum serializzato come String
    val autoBackupFolderUri: String? = null,     // SAF URI della cartella locale
    val googleDriveFolderId: String? = null,     // ID della cartella Google Drive
    val googleDriveFolderName: String? = null,   // Nome leggibile della cartella Google Drive
    val googleDriveUserEmail: String? = null,    // Email del account Google
    // ── Payment Type Config (v4+) ──
    val defaultPaymentType: String = "ELECTRONIC", // Tipo di pagamento predefinito
)
