package com.antcashmanager.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
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
        context.dataStore.data.map { it[thousandsSeparatorKey] ?: "." }

    override suspend fun setThousandsSeparator(separator: String) {
        context.dataStore.edit { it[thousandsSeparatorKey] = separator }
    }

    override suspend fun resetAllPreferences() {
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
            prefs[thousandsSeparatorKey] = "."
        }
    }
}
