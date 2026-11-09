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
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import com.antcashmanager.data.local.DatabaseEncryptionManager
import com.antcashmanager.data.security.LocalDataCipherImpl
import com.antcashmanager.domain.manager.ProfileManager
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.BackupDestination
import com.antcashmanager.domain.model.BackupFrequency
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

public class SettingsRepositoryImpl(
    private val context: Context,
    private val profileManager: ProfileManager,
    private val localDataCipher: LocalDataCipherImpl = LocalDataCipherImpl(context),
) : SettingsRepository {

    // ── DataStore infrastructure ──────────────────────────────────────────────

    /** Device-global store: backup schedule, Drive tokens, encryption, analytics, widget. */
    private val deviceStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("device_settings")
        }
    }

    private val profileStoreCache =
        java.util.concurrent.ConcurrentHashMap<Long, DataStore<Preferences>>()

    private fun profileStore(id: Long): DataStore<Preferences> =
        profileStoreCache.getOrPut(id) {
            PreferenceDataStoreFactory.create {
                context.preferencesDataStoreFile("settings_profile_$id")
            }
        }

    /** Flow of the active profile's DataStore — switches automatically on profile change. */
    private val activeProfileStore: Flow<DataStore<Preferences>> =
        profileManager.activeProfileId.map { profileStore(it) }

    private val pid get() = profileManager.getActiveProfileIdSync()

    // ── Per-profile preference keys ───────────────────────────────────────────

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
    private val suggestionsEnabledKey = booleanPreferencesKey("suggestions_enabled")
    private val suggestionsClearedAtKey = longPreferencesKey("suggestions_cleared_at")
    private val chartCardsOrderKey = stringPreferencesKey("chart_cards_order")
    private val homeTopCardsOrderKey = stringPreferencesKey("home_top_cards_order")
    private val defaultPaymentTypeKey = stringPreferencesKey("default_payment_type")
    private val categorySortOrderInitializedKey =
        booleanPreferencesKey("category_sort_order_initialized")

    // ── Device-global preference keys ─────────────────────────────────────────

    private val analyticsConsentKey = booleanPreferencesKey("analytics_consent")
    private val dataEncryptionEnabledKey = booleanPreferencesKey("data_encryption_enabled")
    private val lastBackupTimestampKey = longPreferencesKey("last_backup_timestamp")
    private val lastRestoreTimestampKey = longPreferencesKey("last_restore_timestamp")
    private val autoBackupEnabledKey = booleanPreferencesKey("auto_backup_enabled")
    private val autoBackupFolderUriKey = stringPreferencesKey("auto_backup_folder_uri")
    private val backupFrequencyKey = stringPreferencesKey("backup_frequency")
    private val widgetBackgroundColorKey = longPreferencesKey("widget_background_color")
    private val widgetOpacityKey = intPreferencesKey("widget_opacity")
    private val autoBackupDestinationKey = stringPreferencesKey("auto_backup_destination")
    private val googleDriveFolderIdKey = stringPreferencesKey("google_drive_folder_id")
    private val googleDriveFolderNameKey = stringPreferencesKey("google_drive_folder_name")
    private val googleDriveAuthTokenKey = stringPreferencesKey("google_drive_auth_token")
    private val googleDriveRefreshTokenKey = stringPreferencesKey("google_drive_refresh_token")
    private val googleDriveUserEmailKey = stringPreferencesKey("google_drive_user_email")

    // ── Date filter helpers ───────────────────────────────────────────────────

    private fun createSavedDateFilter(defaultPresetIndex: Int, defaultDurationMs: Long): SavedDateFilter {
        val now = System.currentTimeMillis()
        return SavedDateFilter(presetIndex = defaultPresetIndex, from = now - defaultDurationMs, to = now)
    }

    private fun defaultHomeDateFilter(): SavedDateFilter =
        createSavedDateFilter(defaultPresetIndex = 1, defaultDurationMs = WEEK_IN_MILLIS)

    private fun defaultTransactionsDateFilter(): SavedDateFilter =
        createSavedDateFilter(defaultPresetIndex = 1, defaultDurationMs = WEEK_IN_MILLIS)

    private fun defaultChartsDateFilter(): SavedDateFilter =
        createSavedDateFilter(defaultPresetIndex = 1, defaultDurationMs = MONTH_IN_MILLIS)

    private fun SavedDateFilter.normalized(): SavedDateFilter =
        copy(from = minOf(from, to), to = maxOf(from, to))

    // ── Per-profile settings ──────────────────────────────────────────────────

    override fun getTheme(): Flow<AppTheme> =
        activeProfileStore.flatMapLatest { store ->
            store.data.map { prefs ->
                val name = prefs[themeKey] ?: AppTheme.SYSTEM.name
                Logger.d(tag = "SettingsRepo") { "Loaded theme: $name" }
                AppTheme.entries.find { it.name == name } ?: AppTheme.SYSTEM
            }
        }

    override suspend fun setTheme(theme: AppTheme) {
        profileStore(pid).edit { it[themeKey] = theme.name }
    }

    override fun getLanguage(): Flow<AppLanguage> =
        activeProfileStore.flatMapLatest { store ->
            store.data.map { prefs ->
                val name = prefs[languageKey] ?: AppLanguage.SYSTEM.name
                AppLanguage.entries.find { it.name == name } ?: AppLanguage.SYSTEM
            }
        }

    override suspend fun setLanguage(language: AppLanguage) {
        profileStore(pid).edit { it[languageKey] = language.name }
    }

    override fun getShowCharts(): Flow<Boolean> =
        activeProfileStore.flatMapLatest { it.data.map { p -> p[showChartsKey] ?: true } }

    override suspend fun setShowCharts(show: Boolean) {
        profileStore(pid).edit { it[showChartsKey] = show }
    }

    override fun getHighContrast(): Flow<Boolean> =
        activeProfileStore.flatMapLatest { it.data.map { p -> p[highContrastKey] ?: false } }

    override suspend fun setHighContrast(enabled: Boolean) {
        profileStore(pid).edit { it[highContrastKey] = enabled }
    }

    override fun getLargeText(): Flow<Boolean> =
        activeProfileStore.flatMapLatest { it.data.map { p -> p[largeTextKey] ?: false } }

    override suspend fun setLargeText(enabled: Boolean) {
        profileStore(pid).edit { it[largeTextKey] = enabled }
    }

    override fun getReduceMotion(): Flow<Boolean> =
        activeProfileStore.flatMapLatest { it.data.map { p -> p[reduceMotionKey] ?: false } }

    override suspend fun setReduceMotion(enabled: Boolean) {
        profileStore(pid).edit { it[reduceMotionKey] = enabled }
    }

    override fun getShowTransactionNotes(): Flow<Boolean> =
        activeProfileStore.flatMapLatest { it.data.map { p -> p[showTransactionNotesKey] ?: true } }

    override suspend fun setShowTransactionNotes(show: Boolean) {
        profileStore(pid).edit { it[showTransactionNotesKey] = show }
    }

    override fun getMaskAmounts(): Flow<Boolean> =
        activeProfileStore.flatMapLatest { it.data.map { p -> p[maskAmountsKey] ?: false } }

    override suspend fun setMaskAmounts(mask: Boolean) {
        profileStore(pid).edit { it[maskAmountsKey] = mask }
    }

    override fun getCurrencySymbol(): Flow<String> =
        activeProfileStore.flatMapLatest { it.data.map { p -> p[currencySymbolKey] ?: "€" } }

    override suspend fun setCurrencySymbol(symbol: String) {
        profileStore(pid).edit { it[currencySymbolKey] = symbol }
    }

    override fun getDecimalDigits(): Flow<Int> =
        activeProfileStore.flatMapLatest { it.data.map { p -> p[decimalDigitsKey] ?: 2 } }

    override suspend fun setDecimalDigits(digits: Int) {
        profileStore(pid).edit { it[decimalDigitsKey] = digits }
    }

    override fun getDecimalSeparator(): Flow<String> =
        activeProfileStore.flatMapLatest { it.data.map { p -> p[decimalSeparatorKey] ?: "," } }

    override suspend fun setDecimalSeparator(separator: String) {
        profileStore(pid).edit { it[decimalSeparatorKey] = separator }
    }

    override fun getThousandsSeparator(): Flow<String> =
        activeProfileStore.flatMapLatest { it.data.map { p -> p[thousandsSeparatorKey] ?: "" } }

    override suspend fun setThousandsSeparator(separator: String) {
        profileStore(pid).edit { it[thousandsSeparatorKey] = separator }
    }

    override fun getMealVoucherValue(): Flow<Double> =
        activeProfileStore.flatMapLatest {
            it.data.map { p -> p[mealVoucherValueKey] ?: DEFAULT_MEAL_VOUCHER_VALUE }
        }

    override suspend fun setMealVoucherValue(value: Double) {
        profileStore(pid).edit { it[mealVoucherValueKey] = value }
    }

    override fun getDateFormat(): Flow<String> =
        activeProfileStore.flatMapLatest { it.data.map { p -> p[dateFormatKey] ?: "dd/MM/yyyy" } }

    override suspend fun setDateFormat(pattern: String) {
        profileStore(pid).edit { it[dateFormatKey] = pattern }
    }

    override fun getDateFilterExpanded(): Flow<Boolean> =
        activeProfileStore.flatMapLatest { it.data.map { p -> p[dateFilterExpandedKey] ?: true } }

    override suspend fun setDateFilterExpanded(expanded: Boolean) {
        profileStore(pid).edit { it[dateFilterExpandedKey] = expanded }
    }

    override fun getHomeDateFilterPreset(): Flow<Int> =
        activeProfileStore.flatMapLatest { it.data.map { p -> p[homeDateFilterPresetKey] ?: 1 } }

    override suspend fun setHomeDateFilterPreset(index: Int) {
        profileStore(pid).edit { it[homeDateFilterPresetKey] = index }
    }

    override fun getHomeDateFilterState(): Flow<SavedDateFilter> =
        activeProfileStore.flatMapLatest { store ->
            store.data.map { prefs ->
                val default = defaultHomeDateFilter()
                SavedDateFilter(
                    presetIndex = prefs[homeDateFilterPresetKey] ?: default.presetIndex,
                    from = prefs[homeDateFilterFromKey] ?: default.from,
                    to = prefs[homeDateFilterToKey] ?: default.to,
                ).normalized()
            }
        }

    override suspend fun setHomeDateFilterState(filter: SavedDateFilter) {
        val n = filter.normalized()
        profileStore(pid).edit {
            it[homeDateFilterPresetKey] = n.presetIndex
            it[homeDateFilterFromKey] = n.from
            it[homeDateFilterToKey] = n.to
        }
    }

    override fun getTransactionsDateFilterPreset(): Flow<Int> =
        activeProfileStore.flatMapLatest {
            it.data.map { p -> p[transactionsDateFilterPresetKey] ?: 1 }
        }

    override suspend fun setTransactionsDateFilterPreset(index: Int) {
        profileStore(pid).edit { it[transactionsDateFilterPresetKey] = index }
    }

    override fun getTransactionsDateFilterState(): Flow<SavedDateFilter> =
        activeProfileStore.flatMapLatest { store ->
            store.data.map { prefs ->
                val default = defaultTransactionsDateFilter()
                SavedDateFilter(
                    presetIndex = prefs[transactionsDateFilterPresetKey] ?: default.presetIndex,
                    from = prefs[transactionsDateFilterFromKey] ?: default.from,
                    to = prefs[transactionsDateFilterToKey] ?: default.to,
                ).normalized()
            }
        }

    override suspend fun setTransactionsDateFilterState(filter: SavedDateFilter) {
        val n = filter.normalized()
        profileStore(pid).edit {
            it[transactionsDateFilterPresetKey] = n.presetIndex
            it[transactionsDateFilterFromKey] = n.from
            it[transactionsDateFilterToKey] = n.to
        }
    }

    override fun getChartsDateFilterPreset(): Flow<Int> =
        activeProfileStore.flatMapLatest {
            it.data.map { p -> p[chartsDateFilterPresetKey] ?: 1 }
        }

    override suspend fun setChartsDateFilterPreset(index: Int) {
        profileStore(pid).edit { it[chartsDateFilterPresetKey] = index }
    }

    override fun getChartsDateFilterState(): Flow<SavedDateFilter> =
        activeProfileStore.flatMapLatest { store ->
            store.data.map { prefs ->
                val default = defaultChartsDateFilter()
                SavedDateFilter(
                    presetIndex = prefs[chartsDateFilterPresetKey] ?: default.presetIndex,
                    from = prefs[chartsDateFilterFromKey] ?: default.from,
                    to = prefs[chartsDateFilterToKey] ?: default.to,
                ).normalized()
            }
        }

    override suspend fun setChartsDateFilterState(filter: SavedDateFilter) {
        val n = filter.normalized()
        profileStore(pid).edit {
            it[chartsDateFilterPresetKey] = n.presetIndex
            it[chartsDateFilterFromKey] = n.from
            it[chartsDateFilterToKey] = n.to
        }
    }

    override fun getChartsZoomEnabled(): Flow<Boolean> =
        activeProfileStore.flatMapLatest { it.data.map { p -> p[chartsZoomEnabledKey] ?: false } }

    override suspend fun setChartsZoomEnabled(enabled: Boolean) {
        profileStore(pid).edit { it[chartsZoomEnabledKey] = enabled }
    }

    override fun getShowPaymentTypeBreakdown(): Flow<Boolean> =
        activeProfileStore.flatMapLatest {
            it.data.map { p -> p[showPaymentTypeBreakdownKey] ?: false }
        }

    override suspend fun setShowPaymentTypeBreakdown(show: Boolean) {
        profileStore(pid).edit { it[showPaymentTypeBreakdownKey] = show }
    }

    override fun getShowQuickInsightsCard(): Flow<Boolean> =
        activeProfileStore.flatMapLatest {
            it.data.map { p -> p[showQuickInsightsCardKey] ?: true }
        }

    override suspend fun setShowQuickInsightsCard(show: Boolean) {
        profileStore(pid).edit { it[showQuickInsightsCardKey] = show }
    }

    override fun getDefaultPaymentType(): Flow<String> =
        activeProfileStore.flatMapLatest {
            it.data.map { p -> p[defaultPaymentTypeKey] ?: "ELECTRONIC" }
        }

    override suspend fun setDefaultPaymentType(paymentType: String) {
        profileStore(pid).edit { it[defaultPaymentTypeKey] = paymentType }
    }

    override fun getTransactionDisplayType(): Flow<TransactionDisplayType> =
        activeProfileStore.flatMapLatest { store ->
            store.data.map { prefs ->
                val name = prefs[transactionDisplayTypeKey] ?: TransactionDisplayType.TREND.name
                runCatching { TransactionDisplayType.valueOf(name) }.getOrDefault(TransactionDisplayType.TREND)
            }
        }

    override suspend fun setTransactionDisplayType(displayType: TransactionDisplayType) {
        profileStore(pid).edit { it[transactionDisplayTypeKey] = displayType.name }
    }

    override fun getTransactionsTransactionDisplayType(): Flow<TransactionDisplayType> =
        activeProfileStore.flatMapLatest { store ->
            store.data.map { prefs ->
                val name = prefs[transactionsTransactionDisplayTypeKey]
                    ?: TransactionDisplayType.TREND.name
                runCatching { TransactionDisplayType.valueOf(name) }
                    .getOrDefault(TransactionDisplayType.TREND)
            }
        }

    override suspend fun setTransactionsTransactionDisplayType(displayType: TransactionDisplayType) {
        profileStore(pid).edit { it[transactionsTransactionDisplayTypeKey] = displayType.name }
    }

    override fun getIsTutorialCompleted(): Flow<Boolean> =
        activeProfileStore.flatMapLatest {
            it.data.map { p -> p[isTutorialCompletedKey] ?: false }
        }

    override suspend fun setIsTutorialCompleted(completed: Boolean) {
        profileStore(pid).edit { it[isTutorialCompletedKey] = completed }
    }

    override fun getCategorySortOrderInitialized(): Flow<Boolean> =
        activeProfileStore.flatMapLatest {
            it.data.map { p -> p[categorySortOrderInitializedKey] ?: false }
        }

    override suspend fun setCategorySortOrderInitialized(initialized: Boolean) {
        profileStore(pid).edit { it[categorySortOrderInitializedKey] = initialized }
    }

    override fun getSuggestionsEnabled(): Flow<Boolean> =
        activeProfileStore.flatMapLatest {
            it.data.map { p -> p[suggestionsEnabledKey] ?: true }
        }

    override suspend fun setSuggestionsEnabled(enabled: Boolean) {
        profileStore(pid).edit { it[suggestionsEnabledKey] = enabled }
    }

    override fun getSuggestionsClearedAt(): Flow<Long?> =
        activeProfileStore.flatMapLatest { it.data.map { p -> p[suggestionsClearedAtKey] } }

    override suspend fun setSuggestionsClearedAt(timestamp: Long) {
        profileStore(pid).edit { it[suggestionsClearedAtKey] = timestamp }
    }

    override fun getChartCardsOrder(): Flow<String> =
        activeProfileStore.flatMapLatest { it.data.map { p -> p[chartCardsOrderKey] ?: "" } }

    override suspend fun setChartCardsOrder(order: String) {
        profileStore(pid).edit { it[chartCardsOrderKey] = order }
    }

    override fun getHomeTopCardsOrder(): Flow<String> =
        activeProfileStore.flatMapLatest { it.data.map { p -> p[homeTopCardsOrderKey] ?: "" } }

    override suspend fun setHomeTopCardsOrder(order: String) {
        profileStore(pid).edit { it[homeTopCardsOrderKey] = order }
    }

    // ── Device-global settings ────────────────────────────────────────────────

    override fun getAnalyticsConsent(): Flow<Boolean?> =
        deviceStore.data.map { it[analyticsConsentKey] }

    override suspend fun setAnalyticsConsent(granted: Boolean) {
        deviceStore.edit { it[analyticsConsentKey] = granted }
    }

    override fun getDataEncryptionEnabled(): Flow<Boolean> =
        deviceStore.data.map { it[dataEncryptionEnabledKey] ?: false }

    override suspend fun setDataEncryptionEnabled(enabled: Boolean) {
        deviceStore.edit { it[dataEncryptionEnabledKey] = enabled }
        DatabaseEncryptionManager.setEncryptionDesired(context, enabled)
        localDataCipher.invalidateEncryptionFlag()
    }

    override fun getLastBackupTimestamp(): Flow<Long?> =
        deviceStore.data.map { it[lastBackupTimestampKey] ?: 0L }

    override suspend fun setLastBackupTimestamp(timestamp: Long) {
        deviceStore.edit { it[lastBackupTimestampKey] = timestamp }
    }

    override fun getLastRestoreTimestamp(): Flow<Long?> =
        deviceStore.data.map { it[lastRestoreTimestampKey] }

    override suspend fun setLastRestoreTimestamp(timestamp: Long) {
        deviceStore.edit { it[lastRestoreTimestampKey] = timestamp }
    }

    override fun getAutoBackupEnabled(): Flow<Boolean> =
        deviceStore.data.map { it[autoBackupEnabledKey] ?: false }

    override suspend fun setAutoBackupEnabled(enabled: Boolean) {
        deviceStore.edit { it[autoBackupEnabledKey] = enabled }
    }

    override fun getAutoBackupFolderUri(): Flow<String?> =
        deviceStore.data.map { it[autoBackupFolderUriKey] }

    override suspend fun setAutoBackupFolderUri(uri: String?) {
        deviceStore.edit { prefs ->
            if (uri == null) prefs.remove(autoBackupFolderUriKey) else prefs[autoBackupFolderUriKey] = uri
        }
    }

    override fun getBackupFrequency(): Flow<BackupFrequency> =
        deviceStore.data.map { prefs ->
            prefs[backupFrequencyKey]
                ?.let { runCatching { BackupFrequency.valueOf(it) }.getOrNull() }
                ?: BackupFrequency.WEEKLY
        }

    override suspend fun setBackupFrequency(frequency: BackupFrequency) {
        deviceStore.edit { it[backupFrequencyKey] = frequency.name }
    }

    override fun getWidgetBackgroundColor(): Flow<Long> =
        deviceStore.data.map { it[widgetBackgroundColorKey] ?: DEFAULT_WIDGET_BACKGROUND_COLOR }

    override suspend fun setWidgetBackgroundColor(color: Long) {
        deviceStore.edit { it[widgetBackgroundColorKey] = color }
    }

    override fun getWidgetOpacity(): Flow<Int> =
        deviceStore.data.map { it[widgetOpacityKey] ?: DEFAULT_WIDGET_OPACITY }

    override suspend fun setWidgetOpacity(opacity: Int) {
        deviceStore.edit { it[widgetOpacityKey] = opacity }
    }

    override fun getAutoBackupDestination(): Flow<BackupDestination> =
        deviceStore.data.map { prefs ->
            val name = prefs[autoBackupDestinationKey] ?: BackupDestination.LOCAL.name
            BackupDestination.valueOf(name)
        }

    override suspend fun setAutoBackupDestination(destination: BackupDestination) {
        deviceStore.edit { it[autoBackupDestinationKey] = destination.name }
    }

    override fun getGoogleDriveFolderId(): Flow<String?> =
        deviceStore.data.map { it[googleDriveFolderIdKey] }

    override suspend fun setGoogleDriveFolderId(folderId: String?) {
        deviceStore.edit { prefs ->
            if (folderId == null) prefs.remove(googleDriveFolderIdKey)
            else prefs[googleDriveFolderIdKey] = folderId
        }
    }

    override fun getGoogleDriveFolderName(): Flow<String?> =
        deviceStore.data.map { it[googleDriveFolderNameKey] }

    override suspend fun setGoogleDriveFolderName(folderName: String?) {
        deviceStore.edit { prefs ->
            if (folderName == null) prefs.remove(googleDriveFolderNameKey)
            else prefs[googleDriveFolderNameKey] = folderName
        }
    }

    override fun getGoogleDriveAuthToken(): Flow<String?> =
        deviceStore.data.map { it[googleDriveAuthTokenKey] }

    override suspend fun setGoogleDriveAuthToken(token: String?) {
        deviceStore.edit { prefs ->
            if (token == null) prefs.remove(googleDriveAuthTokenKey)
            else prefs[googleDriveAuthTokenKey] = token
        }
    }

    override fun getGoogleDriveRefreshToken(): Flow<String?> =
        deviceStore.data.map { it[googleDriveRefreshTokenKey] }

    override suspend fun setGoogleDriveRefreshToken(token: String?) {
        deviceStore.edit { prefs ->
            if (token == null) prefs.remove(googleDriveRefreshTokenKey)
            else prefs[googleDriveRefreshTokenKey] = token
        }
    }

    override fun getGoogleDriveUserEmail(): Flow<String?> =
        deviceStore.data.map { it[googleDriveUserEmailKey] }

    override suspend fun setGoogleDriveUserEmail(email: String?) {
        deviceStore.edit { prefs ->
            if (email == null) prefs.remove(googleDriveUserEmailKey)
            else prefs[googleDriveUserEmailKey] = email
        }
    }

    // ── Reset ─────────────────────────────────────────────────────────────────

    override suspend fun resetAllPreferences() {
        val defaultHomeFilter = defaultHomeDateFilter()
        val defaultTransactionsFilter = defaultTransactionsDateFilter()
        val defaultChartsFilter = defaultChartsDateFilter()

        // Reset only the active profile's settings; device-global keys are not touched
        profileStore(pid).edit { prefs ->
            prefs[themeKey] = AppTheme.SYSTEM.name
            prefs[languageKey] = AppLanguage.SYSTEM.name
            prefs[showChartsKey] = true
            prefs[highContrastKey] = false
            prefs[largeTextKey] = false
            prefs[reduceMotionKey] = false
            prefs[showTransactionNotesKey] = true
            prefs[maskAmountsKey] = false
            prefs[currencySymbolKey] = "€"
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
            prefs[suggestionsEnabledKey] = true
            prefs.remove(suggestionsClearedAtKey)
            prefs[chartCardsOrderKey] = ""
            prefs[homeTopCardsOrderKey] = ""
            prefs[defaultPaymentTypeKey] = "ELECTRONIC"
        }
        DatabaseEncryptionManager.setEncryptionDesired(context, false)
    }

    // ── Backup helpers (used by BackupService in androidApp) ──────────────────

    /** Reads all per-profile settings as a snapshot for backup. */
    public suspend fun readProfileSettingsSnapshot(profileId: Long): ProfileSettingsSnapshot {
        val prefs = profileStore(profileId).data.first()
        return ProfileSettingsSnapshot(
            theme = prefs[themeKey] ?: AppTheme.SYSTEM.name,
            language = prefs[languageKey] ?: AppLanguage.SYSTEM.name,
            showCharts = prefs[showChartsKey] ?: true,
            highContrast = prefs[highContrastKey] ?: false,
            largeText = prefs[largeTextKey] ?: false,
            reduceMotion = prefs[reduceMotionKey] ?: false,
            showTransactionNotes = prefs[showTransactionNotesKey] ?: true,
            maskAmounts = prefs[maskAmountsKey] ?: false,
            showPaymentTypeBreakdown = prefs[showPaymentTypeBreakdownKey] ?: false,
            showQuickInsightsCard = prefs[showQuickInsightsCardKey] ?: true,
            transactionDisplayType = prefs[transactionDisplayTypeKey] ?: TransactionDisplayType.TREND.name,
            transactionsTransactionDisplayType = prefs[transactionsTransactionDisplayTypeKey] ?: TransactionDisplayType.TREND.name,
            currencySymbol = prefs[currencySymbolKey] ?: "€",
            decimalDigits = prefs[decimalDigitsKey] ?: 2,
            decimalSeparator = prefs[decimalSeparatorKey] ?: ",",
            thousandsSeparator = prefs[thousandsSeparatorKey] ?: "",
            mealVoucherValue = prefs[mealVoucherValueKey] ?: DEFAULT_MEAL_VOUCHER_VALUE,
            dateFormat = prefs[dateFormatKey] ?: "dd/MM/yyyy",
            chartsZoomEnabled = prefs[chartsZoomEnabledKey] ?: false,
            suggestionsEnabled = prefs[suggestionsEnabledKey] ?: true,
            suggestionsClearedAt = prefs[suggestionsClearedAtKey],
            chartCardsOrder = prefs[chartCardsOrderKey] ?: "",
            homeTopCardsOrder = prefs[homeTopCardsOrderKey] ?: "",
            defaultPaymentType = prefs[defaultPaymentTypeKey] ?: "ELECTRONIC",
        )
    }

    /** Applies a snapshot to a specific profile's DataStore. Device-global keys are skipped. */
    public suspend fun applyProfileSettingsSnapshot(profileId: Long, snapshot: ProfileSettingsSnapshot) {
        profileStore(profileId).edit { prefs ->
            prefs[themeKey] = snapshot.theme
            prefs[languageKey] = snapshot.language
            prefs[showChartsKey] = snapshot.showCharts
            prefs[highContrastKey] = snapshot.highContrast
            prefs[largeTextKey] = snapshot.largeText
            prefs[reduceMotionKey] = snapshot.reduceMotion
            prefs[showTransactionNotesKey] = snapshot.showTransactionNotes
            prefs[maskAmountsKey] = snapshot.maskAmounts
            prefs[showPaymentTypeBreakdownKey] = snapshot.showPaymentTypeBreakdown
            prefs[showQuickInsightsCardKey] = snapshot.showQuickInsightsCard
            prefs[transactionDisplayTypeKey] = snapshot.transactionDisplayType
            prefs[transactionsTransactionDisplayTypeKey] = snapshot.transactionsTransactionDisplayType
            prefs[currencySymbolKey] = snapshot.currencySymbol
            prefs[decimalDigitsKey] = snapshot.decimalDigits
            prefs[decimalSeparatorKey] = snapshot.decimalSeparator
            prefs[thousandsSeparatorKey] = snapshot.thousandsSeparator
            prefs[mealVoucherValueKey] = snapshot.mealVoucherValue
            prefs[dateFormatKey] = snapshot.dateFormat
            prefs[chartsZoomEnabledKey] = snapshot.chartsZoomEnabled
            prefs[suggestionsEnabledKey] = snapshot.suggestionsEnabled
            snapshot.suggestionsClearedAt?.let { prefs[suggestionsClearedAtKey] = it }
            prefs[chartCardsOrderKey] = snapshot.chartCardsOrder
            prefs[homeTopCardsOrderKey] = snapshot.homeTopCardsOrder
            prefs[defaultPaymentTypeKey] = snapshot.defaultPaymentType
        }
    }

    private companion object {
        const val WEEK_IN_MILLIS = 7L * 24 * 60 * 60 * 1000
        const val MONTH_IN_MILLIS = 30L * 24 * 60 * 60 * 1000
        const val DEFAULT_MEAL_VOUCHER_VALUE = 5.29
        const val DEFAULT_WIDGET_BACKGROUND_COLOR = -16777216L
        const val DEFAULT_WIDGET_OPACITY = 100
    }
}

/** Snapshot of a profile's per-profile settings for backup/restore purposes. */
public data class ProfileSettingsSnapshot(
    val theme: String = "SYSTEM",
    val language: String = "SYSTEM",
    val showCharts: Boolean = true,
    val highContrast: Boolean = false,
    val largeText: Boolean = false,
    val reduceMotion: Boolean = false,
    val showTransactionNotes: Boolean = true,
    val maskAmounts: Boolean = false,
    val showPaymentTypeBreakdown: Boolean = false,
    val showQuickInsightsCard: Boolean = true,
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
    val chartCardsOrder: String = "",
    val homeTopCardsOrder: String = "",
    val defaultPaymentType: String = "ELECTRONIC",
)
