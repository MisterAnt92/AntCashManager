package com.antcashmanager.domain.repository

import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.BackupDestination
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.TransactionDisplayType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

public interface SettingsRepository {
    public fun getTheme(): Flow<AppTheme>
    public suspend fun setTheme(theme: AppTheme): Unit
    public fun getLanguage(): Flow<AppLanguage>
    public suspend fun setLanguage(language: AppLanguage): Unit
    public fun getShowCharts(): Flow<Boolean>
    public suspend fun setShowCharts(show: Boolean): Unit
    public fun getHighContrast(): Flow<Boolean>
    public suspend fun setHighContrast(enabled: Boolean): Unit
    public fun getLargeText(): Flow<Boolean>
    public suspend fun setLargeText(enabled: Boolean): Unit
    public fun getReduceMotion(): Flow<Boolean>
    public suspend fun setReduceMotion(enabled: Boolean): Unit
    public fun getShowTransactionNotes(): Flow<Boolean>
    public suspend fun setShowTransactionNotes(show: Boolean): Unit
    public fun getMaskAmounts(): Flow<Boolean>
    public suspend fun setMaskAmounts(mask: Boolean): Unit

    // ── Currency / number format ──
    public fun getCurrencySymbol(): Flow<String>
    public suspend fun setCurrencySymbol(symbol: String): Unit
    public fun getDecimalDigits(): Flow<Int>
    public suspend fun setDecimalDigits(digits: Int): Unit
    public fun getDecimalSeparator(): Flow<String>
    public suspend fun setDecimalSeparator(separator: String): Unit
    public fun getThousandsSeparator(): Flow<String>
    public suspend fun setThousandsSeparator(separator: String): Unit
    public fun getMealVoucherValue(): Flow<Double>
    public suspend fun setMealVoucherValue(value: Double): Unit

    // ── Date format ──
    public fun getDateFormat(): Flow<String>
    public suspend fun setDateFormat(pattern: String): Unit

    // ── Date Filter UI State ──
    public fun getDateFilterExpanded(): Flow<Boolean>
    public suspend fun setDateFilterExpanded(expanded: Boolean): Unit

    public fun getHomeDateFilterPreset(): Flow<Int>
    public suspend fun setHomeDateFilterPreset(index: Int): Unit
    public fun getHomeDateFilterState(): Flow<SavedDateFilter>
    public suspend fun setHomeDateFilterState(filter: SavedDateFilter): Unit

    public fun getTransactionsDateFilterPreset(): Flow<Int>
    public suspend fun setTransactionsDateFilterPreset(index: Int): Unit
    public fun getTransactionsDateFilterState(): Flow<SavedDateFilter>
    public suspend fun setTransactionsDateFilterState(filter: SavedDateFilter): Unit

    public fun getChartsDateFilterPreset(): Flow<Int>
    public suspend fun setChartsDateFilterPreset(index: Int): Unit
    public fun getChartsDateFilterState(): Flow<SavedDateFilter>
    public suspend fun setChartsDateFilterState(filter: SavedDateFilter): Unit

    public fun getChartsZoomEnabled(): Flow<Boolean>
    public suspend fun setChartsZoomEnabled(enabled: Boolean): Unit

    // ── Payment Type Breakdown ──
    public fun getShowPaymentTypeBreakdown(): Flow<Boolean>
    public suspend fun setShowPaymentTypeBreakdown(show: Boolean): Unit
    public fun getShowQuickInsightsCard(): Flow<Boolean> = flowOf(false)
    public suspend fun setShowQuickInsightsCard(show: Boolean): Unit {}

    // ── Default Payment Type ──
    public fun getDefaultPaymentType(): Flow<String>
    public suspend fun setDefaultPaymentType(paymentType: String): Unit

    // ── Transaction Display Type ──
    public fun getTransactionDisplayType(): Flow<TransactionDisplayType>
    public suspend fun setTransactionDisplayType(displayType: TransactionDisplayType): Unit

    public fun getTransactionsTransactionDisplayType(): Flow<TransactionDisplayType>
    public suspend fun setTransactionsTransactionDisplayType(displayType: TransactionDisplayType): Unit

    public fun getShowInitialAnimation(): Flow<Boolean>
    public suspend fun setShowInitialAnimation(show: Boolean): Unit

    // ── Onboarding / Tutorial ──
    public fun getIsTutorialCompleted(): Flow<Boolean>
    public suspend fun setIsTutorialCompleted(completed: Boolean): Unit

    /**
     * Flag one-shot: indica se il backfill di [com.antcashmanager.domain.model.Category.sortOrder]
     * per le categorie già esistenti (migrazione DB) è già stato eseguito. Stato locale del
     * device, non incluso in [resetAllPreferences] né nel backup.
     */
    public fun getCategorySortOrderInitialized(): Flow<Boolean>
    public suspend fun setCategorySortOrderInitialized(initialized: Boolean): Unit

    // ── Security ──
    public fun getDataEncryptionEnabled(): Flow<Boolean>
    public suspend fun setDataEncryptionEnabled(enabled: Boolean): Unit

    /** Resets every preference to its factory default. */
    public suspend fun resetAllPreferences(): Unit

    // ── Backup/Restore history (stato locale del device, non incluso in resetAllPreferences) ──
    public fun getLastBackupTimestamp(): Flow<Long?>
    public suspend fun setLastBackupTimestamp(timestamp: Long): Unit
    public fun getLastRestoreTimestamp(): Flow<Long?>
    public suspend fun setLastRestoreTimestamp(timestamp: Long): Unit

    // ── Automatic Backup (stato locale del device, non incluso in resetAllPreferences) ──
    public fun getAutoBackupEnabled(): Flow<Boolean>
    public suspend fun setAutoBackupEnabled(enabled: Boolean): Unit
    public fun getAutoBackupFolderUri(): Flow<String?>
    public suspend fun setAutoBackupFolderUri(uri: String?): Unit

    // ── Google Drive Backup Configuration ──
    public fun getAutoBackupDestination(): Flow<BackupDestination>
    public suspend fun setAutoBackupDestination(destination: BackupDestination): Unit

    public fun getGoogleDriveFolderId(): Flow<String?>
    public suspend fun setGoogleDriveFolderId(folderId: String?): Unit

    public fun getGoogleDriveFolderName(): Flow<String?>
    public suspend fun setGoogleDriveFolderName(folderName: String?): Unit

    public fun getGoogleDriveAuthToken(): Flow<String?>
    public suspend fun setGoogleDriveAuthToken(token: String?): Unit

    public fun getGoogleDriveRefreshToken(): Flow<String?>
    public suspend fun setGoogleDriveRefreshToken(token: String?): Unit

    public fun getGoogleDriveUserEmail(): Flow<String?>
    public suspend fun setGoogleDriveUserEmail(email: String?): Unit

    /**
     * ── Suggerimenti (autocomplete titoli/beneficiari/note/luoghi/tag) ──
     *
     * [getSuggestionsClearedAt] è il timestamp dell'ultima cancellazione richiesta
     * dall'utente: i suggerimenti vengono calcolati solo dalle transazioni successive a
     * questo istante, senza dover toccare le transazioni stesse. `null` significa "mai
     * cancellati".
     */
    public fun getSuggestionsEnabled(): Flow<Boolean>
    public suspend fun setSuggestionsEnabled(enabled: Boolean): Unit
    public fun getSuggestionsClearedAt(): Flow<Long?>
    public suspend fun setSuggestionsClearedAt(timestamp: Long): Unit

    // ── Aspetto widget (Home screen) ──
    public fun getWidgetBackgroundColor(): Flow<Long>
    public suspend fun setWidgetBackgroundColor(color: Long): Unit
    public fun getWidgetOpacity(): Flow<Int>
    public suspend fun setWidgetOpacity(opacity: Int): Unit

    // ── Card Customization (persisted per backup) ──
    /**
     * Comma-separated storage keys for chart cards order (e.g., "DISTRIBUTION,PERIOD,QUICK_STATS").
     * Empty string means use default order.
     */
    public fun getChartCardsOrder(): Flow<String>
    public suspend fun setChartCardsOrder(order: String): Unit

    /**
     * Comma-separated storage keys for home top cards order (e.g., "BALANCE,INCOME_EXPENSE").
     * Empty string means use default order.
     */
    public fun getHomeTopCardsOrder(): Flow<String>
    public suspend fun setHomeTopCardsOrder(order: String): Unit
}
