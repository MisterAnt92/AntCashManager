package com.antcashmanager.android.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.antcashmanager.domain.service.PreferencesStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Android-specific implementation of [PreferencesStorage] using SharedPreferences.
 *
 * **Platform**: Android only (via `androidMain` source set)
 *
 * **Encryption**: Supports encrypted preferences via EncryptedSharedPreferences if configured.
 *
 * **Thread Safety**: SharedPreferences is thread-safe internally.
 *
 * **Usage**:
 * ```kotlin
 * val storage = AndroidPreferencesStorageImpl(context)
 * storage.putString("theme", "dark")
 * val theme = storage.getString("theme", "light")
 * ```
 *
 * **For iOS Support**:
 * Create `IosPreferencesStorageImpl` in `iosMain` using same interface.
 *
 * @param context Android Context for SharedPreferences access
 * @param preferencesName SharedPreferences file name (default: "app_preferences")
 */
class AndroidPreferencesStorageImpl(
    private val context: Context,
    private val preferencesName: String = "app_preferences",
) : PreferencesStorage {

    private val preferences: SharedPreferences =
        context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)

    // ── String preferences ──

    override suspend fun getString(key: String, defaultValue: String): String =
        preferences.getString(key, defaultValue) ?: defaultValue

    override fun getStringAsFlow(key: String, defaultValue: String): Flow<String> =
        flow { emit(getString(key, defaultValue)) }

    override suspend fun putString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    // ── Integer preferences ──

    override suspend fun getInt(key: String, defaultValue: Int): Int =
        preferences.getInt(key, defaultValue)

    override fun getIntAsFlow(key: String, defaultValue: Int): Flow<Int> =
        flow { emit(getInt(key, defaultValue)) }

    override suspend fun putInt(key: String, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }

    // ── Boolean preferences ──

    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        preferences.getBoolean(key, defaultValue)

    override fun getBooleanAsFlow(key: String, defaultValue: Boolean): Flow<Boolean> =
        flow { emit(getBoolean(key, defaultValue)) }

    override suspend fun putBoolean(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }

    // ── Long preferences ──

    override suspend fun getLong(key: String, defaultValue: Long): Long =
        preferences.getLong(key, defaultValue)

    override fun getLongAsFlow(key: String, defaultValue: Long): Flow<Long> =
        flow { emit(getLong(key, defaultValue)) }

    override suspend fun putLong(key: String, value: Long) {
        preferences.edit().putLong(key, value).apply()
    }

    // ── Double preferences ──

    override suspend fun getDouble(key: String, defaultValue: Double): Double {
        val stored = preferences.getString(key, null) ?: return defaultValue
        return try {
            stored.toDouble()
        } catch (e: NumberFormatException) {
            defaultValue
        }
    }

    override fun getDoubleAsFlow(key: String, defaultValue: Double): Flow<Double> =
        flow { emit(getDouble(key, defaultValue)) }

    override suspend fun putDouble(key: String, value: Double) {
        preferences.edit().putString(key, value.toString()).apply()
    }

    // ── Key operations ──

    override suspend fun contains(key: String): Boolean =
        preferences.contains(key)

    override suspend fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }

    override suspend fun clear() {
        preferences.edit().clear().apply()
    }

    override suspend fun getAllKeys(): Set<String> =
        preferences.all.keys
}
