package com.antcashmanager.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import co.touchlab.kermit.Logger
import com.antcashmanager.data.local.DatabaseEncryptionManager
import com.antcashmanager.data.security.LocalDataCipherImpl
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.BackupDestination
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

public class SettingsRepositoryImpl(
    private val context: Context,
    private val dataStore: DataStore<Preferences> = context.dataStore,
    private val localDataCipher: LocalDataCipherImpl = LocalDataCipherImpl(context),
) : SettingsRepository {

    private val themeKey = stringPreferencesKey("theme")
    private val languageKey = stringPreferencesKey("language")
    private val showChartsKey = booleanPreferencesKey("show_charts")
    private val highContrastKey = booleanPreferencesKey("high_contrast")
    private val largeTextKey = booleanPreferencesKey("large_text")
    private val reduceMotionKey = booleanPreferencesKey("reduce_motion")
    private val showTransactionNotesKey = booleanPreferencesKey("show_transaction_notes")
    private val maskAmountsKey = booleanPreferencesKey("mask_amounts")
    private val currencySymbolKey = stringPreferencesKey("currency_symbol")
    private val decimalDigitsKey = intPreferencesKey("decimal_digits")
    private val decimalSeparatorKey = stringPreferencesKey("decimal_separator")
    private val thousandsSeparatorKey = stringPreferencesKey("thousands_separator")
    private val mealVoucherValueKey = doublePreferencesKey("meal_voucher_value")
    private val dateFormatKey = stringPreferencesKey("date_format")
    private val dateFilterExpandedKey = booleanPreferencesKey("date_filter_expanded")
    private val homeDateFilterPresetKey = intPreferencesKey("home_date_filter_preset")
    private val homeDateFilterFromKey = longPreferencesKey("home_date_filter_from")
    private val homeDateFilterToKey = longPreferencesKey("home_date_filter_to")
    private val transactionsDateFilterPresetKey =
        intPreferencesKey("transactions_date_filter_preset")
    private val transactionsDateFilterFromKey = longPreferencesKey("transactions_date_filter_from")
    private val transactionsDateFilterToKey = longPreferencesKey("transactions_date_filter_to")
    private val chartsDateFilterPresetKey = intPreferencesKey("charts_date_filter_preset")
    private val chartsDateFilterFromKey = longPreferencesKey("charts_date_filter_from")
    private val chartsDateFilterToKey = longPreferencesKey("charts_date_filter_to")
    private val chartsZoomEnabledKey = booleanPreferencesKey("charts_zoom_enabled")
    private val showPaymentTypeBreakdownKey = booleanPreferencesKey("show_payment_type_breakdown")
    private val showQuickInsightsCardKey = booleanPreferencesKey("show_quick_insights_card")
    private val transactionDisplayTypeKey = stringPreferencesKey("transaction_display_type")
    private val transactionsTransactionDisplayTypeKey =
        stringPreferencesKey("transactions_transaction_display_type")
    private val isTutorialCompletedKey = booleanPreferencesKey("is_tutorial_completed")
    private val categorySortOrderInitializedKey =
        booleanPreferencesKey("category_sort_order_initialized")
    private val dataEncryptionEnabledKey = booleanPreferencesKey("data_encryption_enabled")
    private val lastBackupTimestampKey = longPreferencesKey("last_backup_timestamp")
    private val lastRestoreTimestampKey = longPreferencesKey("last_restore_timestamp")
    private val autoBackupEnabledKey = booleanPreferencesKey("auto_backup_enabled")
    private val autoBackupFolderUriKey = stringPreferencesKey("auto_backup_folder_uri")
    private val suggestionsEnabledKey = booleanPreferencesKey("suggestions_enabled")
    private val suggestionsClearedAtKey = longPreferencesKey("suggestions_cleared_at")
    private val widgetBackgroundColorKey = longPreferencesKey("widget_background_color")
    private val widgetOpacityKey = intPreferencesKey("widget_opacity")
    // ── Card Customization (v3+) ──
    private val chartCardsOrderKey = stringPreferencesKey("chart_cards_order")
    private val homeTopCardsOrderKey = stringPreferencesKey("home_top_cards_order")

    // ── Google Drive Backup Configuration ──
    private val autoBackupDestinationKey = stringPreferencesKey("auto_backup_destination")
    private val googleDriveFolderIdKey = stringPreferencesKey("google_drive_folder_id")
    private val googleDriveFolderNameKey = stringPreferencesKey("google_drive_folder_name")
    private val googleDriveAuthTokenKey = stringPreferencesKey("google_drive_auth_token")
    private val googleDriveRefreshTokenKey = stringPreferencesKey("google_drive_refresh_token")
    private val googleDriveUserEmailKey = stringPreferencesKey("google_drive_user_email")

    // ── Default Payment Type ──
    private val defaultPaymentTypeKey = stringPreferencesKey("default_payment_type")

    private fun createSavedDateFilter(
        defaultPresetIndex: Int,
        defaultDurationMs: Long
    ): SavedDateFilter {
        val now = System.currentTimeMillis()
        return SavedDateFilter(
            presetIndex = defaultPresetIndex,
            from = now - defaultDurationMs,
            to = now,
        )
    }

    private fun defaultHomeDateFilter(): SavedDateFilter =
        createSavedDateFilter(defaultPresetIndex = 1, defaultDurationMs = WEEK_IN_MILLIS)

    private fun defaultTransactionsDateFilter(): SavedDateFilter =
        createSavedDateFilter(defaultPresetIndex = 1, defaultDurationMs = WEEK_IN_MILLIS)

    private fun defaultChartsDateFilter(): SavedDateFilter =
        createSavedDateFilter(defaultPresetIndex = 1, defaultDurationMs = MONTH_IN_MILLIS)

    private fun SavedDateFilter.normalized(): SavedDateFilter {
        val normalizedFrom = minOf(from, to)
        val normalizedTo = maxOf(from, to)
        return copy(from = normalizedFrom, to = normalizedTo)
    }

    override fun getTheme(): Flow<AppTheme> =
        dataStore.data.map { preferences ->
            val themeName = preferences[themeKey] ?: AppTheme.SYSTEM.name
            val theme = AppTheme.entries.find { it.name == themeName } ?: AppTheme.SYSTEM
            Logger.d(tag = "SettingsRepo") { "Loaded theme from DataStore: $theme" }
            theme
        }

    override suspend fun setTheme(theme: AppTheme) {
        Logger.d(tag = "SettingsRepo") { "Saving theme to DataStore: $theme" }
        dataStore.edit { preferences ->
            preferences[themeKey] = theme.name
        }
        Logger.d(tag = "SettingsRepo") { "Theme saved successfully" }
    }

    override fun getLanguage(): Flow<AppLanguage> =
        dataStore.data.map { preferences ->
            val langName = preferences[languageKey] ?: AppLanguage.SYSTEM.name
            val language = AppLanguage.entries.find { it.name == langName } ?: AppLanguage.SYSTEM
            Logger.d(tag = "SettingsRepo") { "Loaded language from DataStore: $language" }
            language
        }

    override suspend fun setLanguage(language: AppLanguage) {
        Logger.d(tag = "SettingsRepo") { "Saving language to DataStore: $language" }
        dataStore.edit { preferences ->
            preferences[languageKey] = language.name
        }
        Logger.d(tag = "SettingsRepo") { "Language saved successfully" }
    }

    override fun getShowCharts(): Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[showChartsKey] ?: true
        }

    override suspend fun setShowCharts(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[showChartsKey] = show
        }
    }

    override fun getHighContrast(): Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[highContrastKey] ?: false
        }

    override suspend fun setHighContrast(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[highContrastKey] = enabled
        }
    }

    override fun getLargeText(): Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[largeTextKey] ?: false
        }

    override suspend fun setLargeText(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[largeTextKey] = enabled
        }
    }

    override fun getReduceMotion(): Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[reduceMotionKey] ?: false
        }

    override suspend fun setReduceMotion(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[reduceMotionKey] = enabled
        }
    }

    override fun getShowTransactionNotes(): Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[showTransactionNotesKey] ?: true
        }

    override suspend fun setShowTransactionNotes(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[showTransactionNotesKey] = show
        }
    }

    override fun getMaskAmounts(): Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[maskAmountsKey] ?: false
        }

    override suspend fun setMaskAmounts(mask: Boolean) {
        dataStore.edit { preferences ->
            preferences[maskAmountsKey] = mask
        }
    }

    override fun getCurrencySymbol(): Flow<String> =
        dataStore.data.map { it[currencySymbolKey] ?: "\u20ac" }

    override suspend fun setCurrencySymbol(symbol: String) {
        dataStore.edit { it[currencySymbolKey] = symbol }
    }

    override fun getDecimalDigits(): Flow<Int> =
        dataStore.data.map { it[decimalDigitsKey] ?: 2 }

    override suspend fun setDecimalDigits(digits: Int) {
        dataStore.edit { it[decimalDigitsKey] = digits }
    }

    override fun getDecimalSeparator(): Flow<String> =
        dataStore.data.map { it[decimalSeparatorKey] ?: "," }

    override suspend fun setDecimalSeparator(separator: String) {
        dataStore.edit { it[decimalSeparatorKey] = separator }
    }

    override fun getThousandsSeparator(): Flow<String> =
        dataStore.data.map { it[thousandsSeparatorKey] ?: "." }

    override suspend fun setThousandsSeparator(separator: String) {
        dataStore.edit { it[thousandsSeparatorKey] = separator }
    }

    override fun getMealVoucherValue(): Flow<Double> =
        dataStore.data.map { it[mealVoucherValueKey] ?: DEFAULT_MEAL_VOUCHER_VALUE }

    override suspend fun setMealVoucherValue(value: Double) {
        dataStore.edit { it[mealVoucherValueKey] = value }
    }

    override fun getDateFormat(): Flow<String> =
        dataStore.data.map { it[dateFormatKey] ?: "dd/MM/yyyy" }

    override suspend fun setDateFormat(pattern: String) {
        dataStore.edit { it[dateFormatKey] = pattern }
    }

    override fun getDateFilterExpanded(): Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[dateFilterExpandedKey] ?: true
        }

    override suspend fun setDateFilterExpanded(expanded: Boolean) {
        dataStore.edit { preferences ->
            preferences[dateFilterExpandedKey] = expanded
        }
    }

    override fun getHomeDateFilterPreset(): Flow<Int> =
        dataStore.data.map { it[homeDateFilterPresetKey] ?: 1 }

    override suspend fun setHomeDateFilterPreset(index: Int) {
        dataStore.edit { it[homeDateFilterPresetKey] = index }
    }

    override fun getHomeDateFilterState(): Flow<SavedDateFilter> =
        dataStore.data.map { preferences ->
            val defaultFilter = defaultHomeDateFilter()
            SavedDateFilter(
                presetIndex = preferences[homeDateFilterPresetKey] ?: defaultFilter.presetIndex,
                from = preferences[homeDateFilterFromKey] ?: defaultFilter.from,
                to = preferences[homeDateFilterToKey] ?: defaultFilter.to,
            ).normalized()
        }

    override suspend fun setHomeDateFilterState(filter: SavedDateFilter) {
        val normalizedFilter = filter.normalized()
        dataStore.edit { preferences ->
            preferences[homeDateFilterPresetKey] = normalizedFilter.presetIndex
            preferences[homeDateFilterFromKey] = normalizedFilter.from
            preferences[homeDateFilterToKey] = normalizedFilter.to
        }
    }

    override fun getTransactionsDateFilterPreset(): Flow<Int> =
        dataStore.data.map { it[transactionsDateFilterPresetKey] ?: 1 }

    override suspend fun setTransactionsDateFilterPreset(index: Int) {
        dataStore.edit { it[transactionsDateFilterPresetKey] = index }
    }

    override fun getTransactionsDateFilterState(): Flow<SavedDateFilter> =
        dataStore.data.map { preferences ->
            val defaultFilter = defaultTransactionsDateFilter()
            SavedDateFilter(
                presetIndex =
                    preferences[transactionsDateFilterPresetKey] ?: defaultFilter.presetIndex,
                from = preferences[transactionsDateFilterFromKey] ?: defaultFilter.from,
                to = preferences[transactionsDateFilterToKey] ?: defaultFilter.to,
            ).normalized()
        }

    override suspend fun setTransactionsDateFilterState(filter: SavedDateFilter) {
        val normalizedFilter = filter.normalized()
        dataStore.edit { preferences ->
            preferences[transactionsDateFilterPresetKey] = normalizedFilter.presetIndex
            preferences[transactionsDateFilterFromKey] = normalizedFilter.from
            preferences[transactionsDateFilterToKey] = normalizedFilter.to
        }
    }

    override fun getChartsDateFilterPreset(): Flow<Int> =
        dataStore.data.map { it[chartsDateFilterPresetKey] ?: 1 }

    override suspend fun setChartsDateFilterPreset(index: Int) {
        dataStore.edit { it[chartsDateFilterPresetKey] = index }
    }

    override fun getChartsDateFilterState(): Flow<SavedDateFilter> =
        dataStore.data.map { preferences ->
            val defaultFilter = defaultChartsDateFilter()
            SavedDateFilter(
                presetIndex = preferences[chartsDateFilterPresetKey] ?: defaultFilter.presetIndex,
                from = preferences[chartsDateFilterFromKey] ?: defaultFilter.from,
                to = preferences[chartsDateFilterToKey] ?: defaultFilter.to,
            ).normalized()
        }

    override suspend fun setChartsDateFilterState(filter: SavedDateFilter) {
        val normalizedFilter = filter.normalized()
        dataStore.edit { preferences ->
            preferences[chartsDateFilterPresetKey] = normalizedFilter.presetIndex
            preferences[chartsDateFilterFromKey] = normalizedFilter.from
            preferences[chartsDateFilterToKey] = normalizedFilter.to
        }
    }

    override fun getChartsZoomEnabled(): Flow<Boolean> =
        dataStore.data.map { it[chartsZoomEnabledKey] ?: false }

    override suspend fun setChartsZoomEnabled(enabled: Boolean) {
        dataStore.edit { it[chartsZoomEnabledKey] = enabled }
    }

    override fun getShowPaymentTypeBreakdown(): Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[showPaymentTypeBreakdownKey] ?: false
        }

    override suspend fun setShowPaymentTypeBreakdown(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[showPaymentTypeBreakdownKey] = show
        }
    }

    override fun getShowQuickInsightsCard(): Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[showQuickInsightsCardKey] ?: true
        }

    override suspend fun setShowQuickInsightsCard(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[showQuickInsightsCardKey] = show
        }
    }

    override fun getDefaultPaymentType(): Flow<String> =
        dataStore.data.map { preferences ->
            preferences[defaultPaymentTypeKey] ?: "ELECTRONIC"
        }

    override suspend fun setDefaultPaymentType(paymentType: String) {
        dataStore.edit { preferences ->
            preferences[defaultPaymentTypeKey] = paymentType
        }
    }

    override fun getTransactionDisplayType(): Flow<TransactionDisplayType> =
        dataStore.data.map { preferences ->
            val typeName =
                preferences[transactionDisplayTypeKey] ?: TransactionDisplayType.TREND.name
            try {
                TransactionDisplayType.valueOf(typeName)
            } catch (_: IllegalArgumentException) {
                TransactionDisplayType.TREND
            }
        }

    override suspend fun setTransactionDisplayType(displayType: TransactionDisplayType) {
        dataStore.edit { preferences ->
            preferences[transactionDisplayTypeKey] = displayType.name
        }
    }

    override fun getTransactionsTransactionDisplayType(): Flow<TransactionDisplayType> =
        dataStore.data.map { preferences ->
            val typeName = preferences[transactionsTransactionDisplayTypeKey]
                ?: TransactionDisplayType.TREND.name
            try {
                TransactionDisplayType.valueOf(typeName)
            } catch (_: IllegalArgumentException) {
                TransactionDisplayType.TREND
            }
        }

    override suspend fun setTransactionsTransactionDisplayType(displayType: TransactionDisplayType) {
        dataStore.edit { preferences ->
            preferences[transactionsTransactionDisplayTypeKey] = displayType.name
        }
    }

    override fun getIsTutorialCompleted(): Flow<Boolean> =
        dataStore.data.map { it[isTutorialCompletedKey] ?: false }

    override suspend fun setIsTutorialCompleted(completed: Boolean) {
        dataStore.edit { it[isTutorialCompletedKey] = completed }
    }

    override fun getCategorySortOrderInitialized(): Flow<Boolean> =
        dataStore.data.map { it[categorySortOrderInitializedKey] ?: false }

    override suspend fun setCategorySortOrderInitialized(initialized: Boolean) {
        dataStore.edit { it[categorySortOrderInitializedKey] = initialized }
    }

    override fun getDataEncryptionEnabled(): Flow<Boolean> =
        dataStore.data.map { it[dataEncryptionEnabledKey] ?: false }

    override suspend fun setDataEncryptionEnabled(enabled: Boolean) {
        dataStore.edit { it[dataEncryptionEnabledKey] = enabled }
        DatabaseEncryptionManager.setEncryptionDesired(context, enabled)
        // Invalidate cipher cache so new encryption state is reflected immediately
        localDataCipher.invalidateEncryptionFlag()
    }

    override fun getLastBackupTimestamp(): Flow<Long?> =
        dataStore.data.map { it[lastBackupTimestampKey] ?: 0L }

    override suspend fun setLastBackupTimestamp(timestamp: Long) {
        dataStore.edit { it[lastBackupTimestampKey] = timestamp }
    }

    override fun getLastRestoreTimestamp(): Flow<Long?> =
        dataStore.data.map { it[lastRestoreTimestampKey] }

    override suspend fun setLastRestoreTimestamp(timestamp: Long) {
        dataStore.edit { it[lastRestoreTimestampKey] = timestamp }
    }

    override fun getAutoBackupEnabled(): Flow<Boolean> =
        dataStore.data.map { it[autoBackupEnabledKey] ?: false }

    override suspend fun setAutoBackupEnabled(enabled: Boolean) {
        dataStore.edit { it[autoBackupEnabledKey] = enabled }
    }

    override fun getAutoBackupFolderUri(): Flow<String?> =
        dataStore.data.map { it[autoBackupFolderUriKey] }

    override suspend fun setAutoBackupFolderUri(uri: String?) {
        dataStore.edit { preferences ->
            if (uri == null) {
                preferences.remove(autoBackupFolderUriKey)
            } else {
                preferences[autoBackupFolderUriKey] = uri
            }
        }
    }

    override fun getSuggestionsEnabled(): Flow<Boolean> =
        dataStore.data.map { it[suggestionsEnabledKey] ?: true }

    override suspend fun setSuggestionsEnabled(enabled: Boolean) {
        dataStore.edit { it[suggestionsEnabledKey] = enabled }
    }

    override fun getSuggestionsClearedAt(): Flow<Long?> =
        dataStore.data.map { it[suggestionsClearedAtKey] }

    override suspend fun setSuggestionsClearedAt(timestamp: Long) {
        dataStore.edit { it[suggestionsClearedAtKey] = timestamp }
    }

    override fun getWidgetBackgroundColor(): Flow<Long> =
        dataStore.data.map { it[widgetBackgroundColorKey] ?: DEFAULT_WIDGET_BACKGROUND_COLOR }

    override suspend fun setWidgetBackgroundColor(color: Long) {
        dataStore.edit { it[widgetBackgroundColorKey] = color }
    }

    override fun getWidgetOpacity(): Flow<Int> =
        dataStore.data.map { it[widgetOpacityKey] ?: DEFAULT_WIDGET_OPACITY }

    override suspend fun setWidgetOpacity(opacity: Int) {
        dataStore.edit { it[widgetOpacityKey] = opacity }
    }

    // ── Card Customization (v3+) ──
    override fun getChartCardsOrder(): Flow<String> =
        dataStore.data.map { it[chartCardsOrderKey] ?: "" }

    override suspend fun setChartCardsOrder(order: String) {
        dataStore.edit { it[chartCardsOrderKey] = order }
    }

    override fun getHomeTopCardsOrder(): Flow<String> =
        dataStore.data.map { it[homeTopCardsOrderKey] ?: "" }

    override suspend fun setHomeTopCardsOrder(order: String) {
        dataStore.edit { it[homeTopCardsOrderKey] = order }
    }

    // ── Google Drive Backup Configuration ──
    override fun getAutoBackupDestination(): Flow<BackupDestination> =
        dataStore.data.map { preferences ->
            val destinationName = preferences[autoBackupDestinationKey] ?: BackupDestination.LOCAL.name
            BackupDestination.valueOf(destinationName)
        }

    override suspend fun setAutoBackupDestination(destination: BackupDestination) {
        dataStore.edit { preferences ->
            preferences[autoBackupDestinationKey] = destination.name
        }
    }

    override fun getGoogleDriveFolderId(): Flow<String?> =
        dataStore.data.map { it[googleDriveFolderIdKey] }

    override suspend fun setGoogleDriveFolderId(folderId: String?) {
        dataStore.edit { preferences ->
            if (folderId == null) {
                preferences.remove(googleDriveFolderIdKey)
            } else {
                preferences[googleDriveFolderIdKey] = folderId
            }
        }
    }

    override fun getGoogleDriveFolderName(): Flow<String?> =
        dataStore.data.map { it[googleDriveFolderNameKey] }

    override suspend fun setGoogleDriveFolderName(folderName: String?) {
        dataStore.edit { preferences ->
            if (folderName == null) {
                preferences.remove(googleDriveFolderNameKey)
            } else {
                preferences[googleDriveFolderNameKey] = folderName
            }
        }
    }

    override fun getGoogleDriveAuthToken(): Flow<String?> =
        dataStore.data.map { it[googleDriveAuthTokenKey] }

    override suspend fun setGoogleDriveAuthToken(token: String?) {
        dataStore.edit { preferences ->
            if (token == null) {
                preferences.remove(googleDriveAuthTokenKey)
            } else {
                preferences[googleDriveAuthTokenKey] = token
            }
        }
    }

    override fun getGoogleDriveRefreshToken(): Flow<String?> =
        dataStore.data.map { it[googleDriveRefreshTokenKey] }

    override suspend fun setGoogleDriveRefreshToken(token: String?) {
        dataStore.edit { preferences ->
            if (token == null) {
                preferences.remove(googleDriveRefreshTokenKey)
            } else {
                preferences[googleDriveRefreshTokenKey] = token
            }
        }
    }

    override fun getGoogleDriveUserEmail(): Flow<String?> =
        dataStore.data.map { it[googleDriveUserEmailKey] }

    override suspend fun setGoogleDriveUserEmail(email: String?) {
        dataStore.edit { preferences ->
            if (email == null) {
                preferences.remove(googleDriveUserEmailKey)
            } else {
                preferences[googleDriveUserEmailKey] = email
            }
        }
    }

    override suspend fun resetAllPreferences() {
        val defaultHomeFilter = defaultHomeDateFilter()
        val defaultTransactionsFilter = defaultTransactionsDateFilter()
        val defaultChartsFilter = defaultChartsDateFilter()

        dataStore.edit { prefs ->
            prefs[themeKey] = AppTheme.SYSTEM.name
            prefs[languageKey] = AppLanguage.SYSTEM.name
            prefs[showChartsKey] = true
            prefs[highContrastKey] = false
            prefs[largeTextKey] = false
            prefs[reduceMotionKey] = false
            prefs[showTransactionNotesKey] = true
            prefs[maskAmountsKey] = false
            prefs[currencySymbolKey] = "\u20ac"
            prefs[decimalDigitsKey] = 2
            prefs[decimalSeparatorKey] = ","
            prefs[thousandsSeparatorKey] = ""
            prefs[mealVoucherValueKey] = DEFAULT_MEAL_VOUCHER_VALUE
            prefs[dateFormatKey] = "dd/MM/yyyy"
            prefs[dateFilterExpandedKey] = true
            prefs[homeDateFilterPresetKey] = defaultHomeFilter.presetIndex
            prefs[homeDateFilterFromKey] = defaultHomeFilter.from
            prefs[homeDateFilterToKey] = defaultHomeFilter.to
            prefs[transactionsDateFilterPresetKey] = defaultTransactionsFilter.presetIndex
            prefs[transactionsDateFilterFromKey] = defaultTransactionsFilter.from
            prefs[transactionsDateFilterToKey] = defaultTransactionsFilter.to
            prefs[chartsDateFilterPresetKey] = defaultChartsFilter.presetIndex
            prefs[chartsDateFilterFromKey] = defaultChartsFilter.from
            prefs[chartsDateFilterToKey] = defaultChartsFilter.to
            prefs[chartsZoomEnabledKey] = false
            prefs[showPaymentTypeBreakdownKey] = false
            prefs[showQuickInsightsCardKey] = true
            prefs[transactionDisplayTypeKey] = TransactionDisplayType.TREND.name
            prefs[transactionsTransactionDisplayTypeKey] = TransactionDisplayType.TREND.name
            prefs[isTutorialCompletedKey] = false
            prefs[dataEncryptionEnabledKey] = false
            prefs[suggestionsEnabledKey] = true
            prefs.remove(suggestionsClearedAtKey)
            prefs[widgetBackgroundColorKey] = DEFAULT_WIDGET_BACKGROUND_COLOR
            prefs[widgetOpacityKey] = DEFAULT_WIDGET_OPACITY
            prefs[chartCardsOrderKey] = ""
            prefs[homeTopCardsOrderKey] = ""
        }
        DatabaseEncryptionManager.setEncryptionDesired(context, false)
    }

    private companion object {
        const val WEEK_IN_MILLIS = 7L * 24 * 60 * 60 * 1000
        const val MONTH_IN_MILLIS = 30L * 24 * 60 * 60 * 1000
        const val DEFAULT_MEAL_VOUCHER_VALUE = 5.29
        const val DEFAULT_WIDGET_BACKGROUND_COLOR = -16777216L // Black (ARGB)
        const val DEFAULT_WIDGET_OPACITY = 100
    }
}
