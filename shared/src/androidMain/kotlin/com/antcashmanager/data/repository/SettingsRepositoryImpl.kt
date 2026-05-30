package com.antcashmanager.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.antcashmanager.data.local.DatabaseEncryptionManager
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepositoryImpl(
    private val context: Context,
) : SettingsRepository {

    private val themeKey = stringPreferencesKey("theme")
    private val languageKey = stringPreferencesKey("language")
    private val showChartsKey = booleanPreferencesKey("show_charts")
    private val highContrastKey = booleanPreferencesKey("high_contrast")
    private val largeTextKey = booleanPreferencesKey("large_text")
    private val reduceMotionKey = booleanPreferencesKey("reduce_motion")
    private val showTransactionNotesKey = booleanPreferencesKey("show_transaction_notes")
    private val currencySymbolKey = stringPreferencesKey("currency_symbol")
    private val decimalDigitsKey = intPreferencesKey("decimal_digits")
    private val decimalSeparatorKey = stringPreferencesKey("decimal_separator")
    private val thousandsSeparatorKey = stringPreferencesKey("thousands_separator")
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
    private val dataEncryptionEnabledKey = booleanPreferencesKey("data_encryption_enabled")

    private fun createSavedDateFilter(defaultPresetIndex: Int, defaultDurationMs: Long): SavedDateFilter {
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
        context.dataStore.data.map { preferences ->
            val themeName = preferences[themeKey] ?: AppTheme.SYSTEM.name
            AppTheme.entries.find { it.name == themeName } ?: AppTheme.SYSTEM
        }

    override suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[themeKey] = theme.name
        }
    }

    override fun getLanguage(): Flow<AppLanguage> =
        context.dataStore.data.map { preferences ->
            val langName = preferences[languageKey] ?: AppLanguage.SYSTEM.name
            AppLanguage.entries.find { it.name == langName } ?: AppLanguage.SYSTEM
        }

    override suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { preferences ->
            preferences[languageKey] = language.name
        }
    }

    override fun getShowCharts(): Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[showChartsKey] ?: true
        }

    override suspend fun setShowCharts(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[showChartsKey] = show
        }
    }

    override fun getHighContrast(): Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[highContrastKey] ?: false
        }

    override suspend fun setHighContrast(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[highContrastKey] = enabled
        }
    }

    override fun getLargeText(): Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[largeTextKey] ?: false
        }

    override suspend fun setLargeText(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[largeTextKey] = enabled
        }
    }

    override fun getReduceMotion(): Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[reduceMotionKey] ?: false
        }

    override suspend fun setReduceMotion(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[reduceMotionKey] = enabled
        }
    }

    override fun getShowTransactionNotes(): Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[showTransactionNotesKey] ?: true
        }

    override suspend fun setShowTransactionNotes(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[showTransactionNotesKey] = show
        }
    }

    override fun getCurrencySymbol(): Flow<String> =
        context.dataStore.data.map { it[currencySymbolKey] ?: "\u20ac" }

    override suspend fun setCurrencySymbol(symbol: String) {
        context.dataStore.edit { it[currencySymbolKey] = symbol }
    }

    override fun getDecimalDigits(): Flow<Int> =
        context.dataStore.data.map { it[decimalDigitsKey] ?: 2 }

    override suspend fun setDecimalDigits(digits: Int) {
        context.dataStore.edit { it[decimalDigitsKey] = digits }
    }

    override fun getDecimalSeparator(): Flow<String> =
        context.dataStore.data.map { it[decimalSeparatorKey] ?: "," }

    override suspend fun setDecimalSeparator(separator: String) {
        context.dataStore.edit { it[decimalSeparatorKey] = separator }
    }

    override fun getThousandsSeparator(): Flow<String> =
        context.dataStore.data.map { it[thousandsSeparatorKey] ?: "" }

    override suspend fun setThousandsSeparator(separator: String) {
        context.dataStore.edit { it[thousandsSeparatorKey] = separator }
    }

    override fun getDateFormat(): Flow<String> =
        context.dataStore.data.map { it[dateFormatKey] ?: "dd/MM/yyyy" }

    override suspend fun setDateFormat(pattern: String) {
        context.dataStore.edit { it[dateFormatKey] = pattern }
    }

    override fun getDateFilterExpanded(): Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[dateFilterExpandedKey] ?: true
        }

    override suspend fun setDateFilterExpanded(expanded: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[dateFilterExpandedKey] = expanded
        }
    }

    override fun getHomeDateFilterPreset(): Flow<Int> =
        context.dataStore.data.map { it[homeDateFilterPresetKey] ?: 1 }

    override suspend fun setHomeDateFilterPreset(index: Int) {
        context.dataStore.edit { it[homeDateFilterPresetKey] = index }
    }

    override fun getHomeDateFilterState(): Flow<SavedDateFilter> =
        context.dataStore.data.map { preferences ->
            val defaultFilter = defaultHomeDateFilter()
            SavedDateFilter(
                presetIndex = preferences[homeDateFilterPresetKey] ?: defaultFilter.presetIndex,
                from = preferences[homeDateFilterFromKey] ?: defaultFilter.from,
                to = preferences[homeDateFilterToKey] ?: defaultFilter.to,
            ).normalized()
        }

    override suspend fun setHomeDateFilterState(filter: SavedDateFilter) {
        val normalizedFilter = filter.normalized()
        context.dataStore.edit { preferences ->
            preferences[homeDateFilterPresetKey] = normalizedFilter.presetIndex
            preferences[homeDateFilterFromKey] = normalizedFilter.from
            preferences[homeDateFilterToKey] = normalizedFilter.to
        }
    }

    override fun getTransactionsDateFilterPreset(): Flow<Int> =
        context.dataStore.data.map { it[transactionsDateFilterPresetKey] ?: 1 }

    override suspend fun setTransactionsDateFilterPreset(index: Int) {
        context.dataStore.edit { it[transactionsDateFilterPresetKey] = index }
    }

    override fun getTransactionsDateFilterState(): Flow<SavedDateFilter> =
        context.dataStore.data.map { preferences ->
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
        context.dataStore.edit { preferences ->
            preferences[transactionsDateFilterPresetKey] = normalizedFilter.presetIndex
            preferences[transactionsDateFilterFromKey] = normalizedFilter.from
            preferences[transactionsDateFilterToKey] = normalizedFilter.to
        }
    }

    override fun getChartsDateFilterPreset(): Flow<Int> =
        context.dataStore.data.map { it[chartsDateFilterPresetKey] ?: 1 }

    override suspend fun setChartsDateFilterPreset(index: Int) {
        context.dataStore.edit { it[chartsDateFilterPresetKey] = index }
    }

    override fun getChartsDateFilterState(): Flow<SavedDateFilter> =
        context.dataStore.data.map { preferences ->
            val defaultFilter = defaultChartsDateFilter()
            SavedDateFilter(
                presetIndex = preferences[chartsDateFilterPresetKey] ?: defaultFilter.presetIndex,
                from = preferences[chartsDateFilterFromKey] ?: defaultFilter.from,
                to = preferences[chartsDateFilterToKey] ?: defaultFilter.to,
            ).normalized()
        }

    override suspend fun setChartsDateFilterState(filter: SavedDateFilter) {
        val normalizedFilter = filter.normalized()
        context.dataStore.edit { preferences ->
            preferences[chartsDateFilterPresetKey] = normalizedFilter.presetIndex
            preferences[chartsDateFilterFromKey] = normalizedFilter.from
            preferences[chartsDateFilterToKey] = normalizedFilter.to
        }
    }

    override fun getChartsZoomEnabled(): Flow<Boolean> =
        context.dataStore.data.map { it[chartsZoomEnabledKey] ?: true }

    override suspend fun setChartsZoomEnabled(enabled: Boolean) {
        context.dataStore.edit { it[chartsZoomEnabledKey] = enabled }
    }

    override fun getShowPaymentTypeBreakdown(): Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[showPaymentTypeBreakdownKey] ?: false
        }

    override suspend fun setShowPaymentTypeBreakdown(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[showPaymentTypeBreakdownKey] = show
        }
    }

    override fun getShowQuickInsightsCard(): Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[showQuickInsightsCardKey] ?: false
        }

    override suspend fun setShowQuickInsightsCard(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[showQuickInsightsCardKey] = show
        }
    }

    override fun getTransactionDisplayType(): Flow<TransactionDisplayType> =
        context.dataStore.data.map { preferences ->
            val typeName =
                preferences[transactionDisplayTypeKey] ?: TransactionDisplayType.TREND.name
            try {
                TransactionDisplayType.valueOf(typeName)
            } catch (_: IllegalArgumentException) {
                TransactionDisplayType.TREND
            }
        }

    override suspend fun setTransactionDisplayType(displayType: TransactionDisplayType) {
        context.dataStore.edit { preferences ->
            preferences[transactionDisplayTypeKey] = displayType.name
        }
    }

    override fun getTransactionsTransactionDisplayType(): Flow<TransactionDisplayType> =
        context.dataStore.data.map { preferences ->
            val typeName = preferences[transactionsTransactionDisplayTypeKey]
                ?: TransactionDisplayType.TREND.name
            try {
                TransactionDisplayType.valueOf(typeName)
            } catch (_: IllegalArgumentException) {
                TransactionDisplayType.TREND
            }
        }

    override suspend fun setTransactionsTransactionDisplayType(displayType: TransactionDisplayType) {
        context.dataStore.edit { preferences ->
            preferences[transactionsTransactionDisplayTypeKey] = displayType.name
        }
    }

    override fun getIsTutorialCompleted(): Flow<Boolean> =
        context.dataStore.data.map { it[isTutorialCompletedKey] ?: false }

    override suspend fun setIsTutorialCompleted(completed: Boolean) {
        context.dataStore.edit { it[isTutorialCompletedKey] = completed }
    }

    override fun getDataEncryptionEnabled(): Flow<Boolean> =
        context.dataStore.data.map { it[dataEncryptionEnabledKey] ?: false }

    override suspend fun setDataEncryptionEnabled(enabled: Boolean) {
        context.dataStore.edit { it[dataEncryptionEnabledKey] = enabled }
        DatabaseEncryptionManager.setEncryptionDesired(context, enabled)
    }

    override suspend fun resetAllPreferences() {
        val defaultHomeFilter = defaultHomeDateFilter()
        val defaultTransactionsFilter = defaultTransactionsDateFilter()
        val defaultChartsFilter = defaultChartsDateFilter()

        context.dataStore.edit { prefs ->
            prefs[themeKey] = AppTheme.SYSTEM.name
            prefs[languageKey] = AppLanguage.SYSTEM.name
            prefs[showChartsKey] = true
            prefs[highContrastKey] = false
            prefs[largeTextKey] = false
            prefs[reduceMotionKey] = false
            prefs[showTransactionNotesKey] = true
            prefs[currencySymbolKey] = "\u20ac"
            prefs[decimalDigitsKey] = 2
            prefs[decimalSeparatorKey] = ","
            prefs[thousandsSeparatorKey] = ""
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
            prefs[chartsZoomEnabledKey] = true
            prefs[showPaymentTypeBreakdownKey] = false
            prefs[showQuickInsightsCardKey] = false
            prefs[transactionDisplayTypeKey] = TransactionDisplayType.TREND.name
            prefs[transactionsTransactionDisplayTypeKey] = TransactionDisplayType.TREND.name
            prefs[isTutorialCompletedKey] = false
            prefs[dataEncryptionEnabledKey] = false
        }
        DatabaseEncryptionManager.setEncryptionDesired(context, false)
    }

    private companion object {
        const val WEEK_IN_MILLIS = 7L * 24 * 60 * 60 * 1000
        const val MONTH_IN_MILLIS = 30L * 24 * 60 * 60 * 1000
    }
}
