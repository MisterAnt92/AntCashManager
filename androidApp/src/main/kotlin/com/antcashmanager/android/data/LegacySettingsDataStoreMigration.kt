package com.antcashmanager.android.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import androidx.datastore.preferences.core.PreferenceDataStoreFactory

/**
 * One-shot migration from the legacy "settings" DataStore (v1.7, single store for all settings)
 * to the split stores introduced in v1.8: "device_settings" and "settings_profile_1".
 *
 * Must be called with runBlocking BEFORE startKoin so no other DataStore instance for these
 * files exists yet. A per-scope cancel ensures file handles are released immediately after.
 */
internal suspend fun migrateSettingsFromLegacyDataStore(context: Context) {
    val appContext = context.applicationContext
    val legacyFile = appContext.preferencesDataStoreFile("settings")
    if (!legacyFile.exists()) return

    val migrationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    try {
        val deviceStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(scope = migrationScope) {
            appContext.preferencesDataStoreFile("device_settings")
        }

        val migrationDoneKey = booleanPreferencesKey("legacy_v17_migration_done")
        if (deviceStore.data.first()[migrationDoneKey] == true) return

        val legacyStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(scope = migrationScope) {
            legacyFile
        }

        val legacyPrefs: Preferences = try {
            legacyStore.data.first()
        } catch (e: Exception) {
            Logger.w(tag = "LegacyMigration") { "Cannot read legacy settings: $e" }
            deviceStore.edit { it[migrationDoneKey] = true }
            return
        }

        if (legacyPrefs.asMap().isEmpty()) {
            deviceStore.edit { it[migrationDoneKey] = true }
            return
        }

        // ── Device-global keys → device_settings ──────────────────────────────────
        deviceStore.edit { p ->
            legacyPrefs[booleanPreferencesKey("is_tutorial_completed")]
                ?.let { p[booleanPreferencesKey("is_tutorial_completed")] = it }
            legacyPrefs[booleanPreferencesKey("data_encryption_enabled")]
                ?.let { p[booleanPreferencesKey("data_encryption_enabled")] = it }
            legacyPrefs[booleanPreferencesKey("auto_backup_enabled")]
                ?.let { p[booleanPreferencesKey("auto_backup_enabled")] = it }
            legacyPrefs[longPreferencesKey("last_backup_timestamp")]
                ?.let { p[longPreferencesKey("last_backup_timestamp")] = it }
            legacyPrefs[longPreferencesKey("last_restore_timestamp")]
                ?.let { p[longPreferencesKey("last_restore_timestamp")] = it }
            legacyPrefs[stringPreferencesKey("auto_backup_folder_uri")]
                ?.let { p[stringPreferencesKey("auto_backup_folder_uri")] = it }
            legacyPrefs[stringPreferencesKey("backup_frequency")]
                ?.let { p[stringPreferencesKey("backup_frequency")] = it }
            legacyPrefs[longPreferencesKey("widget_background_color")]
                ?.let { p[longPreferencesKey("widget_background_color")] = it }
            legacyPrefs[intPreferencesKey("widget_opacity")]
                ?.let { p[intPreferencesKey("widget_opacity")] = it }
            legacyPrefs[stringPreferencesKey("auto_backup_destination")]
                ?.let { p[stringPreferencesKey("auto_backup_destination")] = it }
            legacyPrefs[stringPreferencesKey("google_drive_folder_id")]
                ?.let { p[stringPreferencesKey("google_drive_folder_id")] = it }
            legacyPrefs[stringPreferencesKey("google_drive_folder_name")]
                ?.let { p[stringPreferencesKey("google_drive_folder_name")] = it }
            legacyPrefs[stringPreferencesKey("google_drive_auth_token")]
                ?.let { p[stringPreferencesKey("google_drive_auth_token")] = it }
            legacyPrefs[stringPreferencesKey("google_drive_refresh_token")]
                ?.let { p[stringPreferencesKey("google_drive_refresh_token")] = it }
            legacyPrefs[stringPreferencesKey("google_drive_user_email")]
                ?.let { p[stringPreferencesKey("google_drive_user_email")] = it }
            legacyPrefs[doublePreferencesKey("meal_voucher_value")]
                ?.let { p[doublePreferencesKey("meal_voucher_value")] = it }
            p[migrationDoneKey] = true
        }

        // ── Per-profile keys → settings_profile_1 ─────────────────────────────────
        val profileStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(scope = migrationScope) {
            appContext.preferencesDataStoreFile("settings_profile_1")
        }

        profileStore.edit { p ->
            legacyPrefs[stringPreferencesKey("theme")]
                ?.let { p[stringPreferencesKey("theme")] = it }
            legacyPrefs[stringPreferencesKey("language")]
                ?.let { p[stringPreferencesKey("language")] = it }
            legacyPrefs[booleanPreferencesKey("show_charts")]
                ?.let { p[booleanPreferencesKey("show_charts")] = it }
            legacyPrefs[booleanPreferencesKey("high_contrast")]
                ?.let { p[booleanPreferencesKey("high_contrast")] = it }
            legacyPrefs[booleanPreferencesKey("large_text")]
                ?.let { p[booleanPreferencesKey("large_text")] = it }
            legacyPrefs[booleanPreferencesKey("reduce_motion")]
                ?.let { p[booleanPreferencesKey("reduce_motion")] = it }
            legacyPrefs[booleanPreferencesKey("show_transaction_notes")]
                ?.let { p[booleanPreferencesKey("show_transaction_notes")] = it }
            legacyPrefs[booleanPreferencesKey("mask_amounts")]
                ?.let { p[booleanPreferencesKey("mask_amounts")] = it }
            legacyPrefs[stringPreferencesKey("currency_symbol")]
                ?.let { p[stringPreferencesKey("currency_symbol")] = it }
            legacyPrefs[intPreferencesKey("decimal_digits")]
                ?.let { p[intPreferencesKey("decimal_digits")] = it }
            legacyPrefs[stringPreferencesKey("decimal_separator")]
                ?.let { p[stringPreferencesKey("decimal_separator")] = it }
            legacyPrefs[stringPreferencesKey("thousands_separator")]
                ?.let { p[stringPreferencesKey("thousands_separator")] = it }
            legacyPrefs[stringPreferencesKey("date_format")]
                ?.let { p[stringPreferencesKey("date_format")] = it }
            legacyPrefs[booleanPreferencesKey("date_filter_expanded")]
                ?.let { p[booleanPreferencesKey("date_filter_expanded")] = it }
            legacyPrefs[intPreferencesKey("home_date_filter_preset")]
                ?.let { p[intPreferencesKey("home_date_filter_preset")] = it }
            legacyPrefs[longPreferencesKey("home_date_filter_from")]
                ?.let { p[longPreferencesKey("home_date_filter_from")] = it }
            legacyPrefs[longPreferencesKey("home_date_filter_to")]
                ?.let { p[longPreferencesKey("home_date_filter_to")] = it }
            legacyPrefs[intPreferencesKey("transactions_date_filter_preset")]
                ?.let { p[intPreferencesKey("transactions_date_filter_preset")] = it }
            legacyPrefs[longPreferencesKey("transactions_date_filter_from")]
                ?.let { p[longPreferencesKey("transactions_date_filter_from")] = it }
            legacyPrefs[longPreferencesKey("transactions_date_filter_to")]
                ?.let { p[longPreferencesKey("transactions_date_filter_to")] = it }
            legacyPrefs[intPreferencesKey("charts_date_filter_preset")]
                ?.let { p[intPreferencesKey("charts_date_filter_preset")] = it }
            legacyPrefs[longPreferencesKey("charts_date_filter_from")]
                ?.let { p[longPreferencesKey("charts_date_filter_from")] = it }
            legacyPrefs[longPreferencesKey("charts_date_filter_to")]
                ?.let { p[longPreferencesKey("charts_date_filter_to")] = it }
            legacyPrefs[booleanPreferencesKey("charts_zoom_enabled")]
                ?.let { p[booleanPreferencesKey("charts_zoom_enabled")] = it }
            legacyPrefs[booleanPreferencesKey("show_payment_type_breakdown")]
                ?.let { p[booleanPreferencesKey("show_payment_type_breakdown")] = it }
            legacyPrefs[booleanPreferencesKey("show_quick_insights_card")]
                ?.let { p[booleanPreferencesKey("show_quick_insights_card")] = it }
            legacyPrefs[stringPreferencesKey("transaction_display_type")]
                ?.let { p[stringPreferencesKey("transaction_display_type")] = it }
            legacyPrefs[stringPreferencesKey("transactions_transaction_display_type")]
                ?.let { p[stringPreferencesKey("transactions_transaction_display_type")] = it }
            legacyPrefs[booleanPreferencesKey("suggestions_enabled")]
                ?.let { p[booleanPreferencesKey("suggestions_enabled")] = it }
            legacyPrefs[longPreferencesKey("suggestions_cleared_at")]
                ?.let { p[longPreferencesKey("suggestions_cleared_at")] = it }
            legacyPrefs[stringPreferencesKey("chart_cards_order")]
                ?.let { p[stringPreferencesKey("chart_cards_order")] = it }
            legacyPrefs[stringPreferencesKey("home_top_cards_order")]
                ?.let { p[stringPreferencesKey("home_top_cards_order")] = it }
            legacyPrefs[stringPreferencesKey("default_payment_type")]
                ?.let { p[stringPreferencesKey("default_payment_type")] = it }
            legacyPrefs[booleanPreferencesKey("category_sort_order_initialized")]
                ?.let { p[booleanPreferencesKey("category_sort_order_initialized")] = it }
        }

        Logger.i(tag = "LegacyMigration") { "Legacy settings migration completed: ${legacyPrefs.asMap().size} keys migrated" }
    } finally {
        migrationScope.cancel()
    }
}
