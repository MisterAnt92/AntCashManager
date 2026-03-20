package com.antcashmanager.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
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
}
