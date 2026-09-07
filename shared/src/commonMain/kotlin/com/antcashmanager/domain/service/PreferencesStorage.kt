package com.antcashmanager.domain.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * KMP-compatible preferences storage service.
 *
 * Abstracts platform-specific preference storage (SharedPreferences on Android,
 * UserDefaults on iOS) behind a common interface.
 *
 * **Implementations**:
 * - `AndroidPreferencesStorageImpl` - Uses SharedPreferences (Android)
 * - `IosPreferencesStorageImpl` - Uses UserDefaults (iOS) - Future
 * - `InMemoryPreferencesStorage` - For testing
 *
 * **Benefits**:
 * - Single KMP-compatible interface
 * - Enables iOS support without changing domain code
 * - Testable without platform dependencies
 * - Supports both String and Complex types (via serialization)
 *
 * **Usage**:
 * ```kotlin
 * // Get preference with default
 * val theme = preferencesStorage.getString("theme", "light")
 *
 * // Observe changes
 * preferencesStorage.getStringAsFlow("theme", "light")
 *     .collect { theme -> updateUI(theme) }
 *
 * // Set preference
 * preferencesStorage.putString("theme", "dark")
 *
 * // Clear all
 * preferencesStorage.clear()
 * ```
 */
public interface PreferencesStorage {
    // ── String preferences ──

    /**
     * Get a string preference by key.
     *
     * @param key Preference key
     * @param defaultValue Value returned if key not found
     * @return Stored value or defaultValue
     */
    public suspend fun getString(
        key: String,
        defaultValue: String = "",
    ): String

    /**
     * Get a string preference as a Flow for reactive updates.
     *
     * Emits current value immediately, then emits on changes.
     *
     * @param key Preference key
     * @param defaultValue Value returned if key not found
     * @return Flow that emits stored value and changes
     */
    public fun getStringAsFlow(
        key: String,
        defaultValue: String = "",
    ): Flow<String>

    /**
     * Store a string preference.
     *
     * @param key Preference key
     * @param value Value to store
     */
    public suspend fun putString(
        key: String,
        value: String,
    ): Unit

    // ── Integer preferences ──

    public suspend fun getInt(
        key: String,
        defaultValue: Int = 0,
    ): Int

    public fun getIntAsFlow(
        key: String,
        defaultValue: Int = 0,
    ): Flow<Int>

    public suspend fun putInt(
        key: String,
        value: Int,
    ): Unit

    // ── Boolean preferences ──

    public suspend fun getBoolean(
        key: String,
        defaultValue: Boolean = false,
    ): Boolean

    public fun getBooleanAsFlow(
        key: String,
        defaultValue: Boolean = false,
    ): Flow<Boolean>

    public suspend fun putBoolean(
        key: String,
        value: Boolean,
    ): Unit

    // ── Long preferences ──

    public suspend fun getLong(
        key: String,
        defaultValue: Long = 0L,
    ): Long

    public fun getLongAsFlow(
        key: String,
        defaultValue: Long = 0L,
    ): Flow<Long>

    public suspend fun putLong(
        key: String,
        value: Long,
    ): Unit

    // ── Double preferences ──

    public suspend fun getDouble(
        key: String,
        defaultValue: Double = 0.0,
    ): Double

    public fun getDoubleAsFlow(
        key: String,
        defaultValue: Double = 0.0,
    ): Flow<Double>

    public suspend fun putDouble(
        key: String,
        value: Double,
    ): Unit

    // ── Key operations ──

    /**
     * Check if a preference key exists.
     */
    public suspend fun contains(key: String): Boolean

    /**
     * Remove a preference by key.
     */
    public suspend fun remove(key: String): Unit

    /**
     * Clear all preferences.
     */
    public suspend fun clear(): Unit

    /**
     * Get all stored keys.
     */
    public suspend fun getAllKeys(): Set<String>
}

/**
 * No-op implementation for testing or platforms without preferences.
 *
 * Stores everything in memory, lost on app termination.
 */
public class InMemoryPreferencesStorage : PreferencesStorage {
    private val data = mutableMapOf<String, Any?>()

    override suspend fun getString(
        key: String,
        defaultValue: String,
    ): String = (data[key] as? String) ?: defaultValue

    override fun getStringAsFlow(
        key: String,
        defaultValue: String,
    ): Flow<String> = flow { emit(getString(key, defaultValue)) }

    override suspend fun putString(
        key: String,
        value: String,
    ) {
        data[key] = value
    }

    override suspend fun getInt(
        key: String,
        defaultValue: Int,
    ): Int = (data[key] as? Int) ?: defaultValue

    override fun getIntAsFlow(
        key: String,
        defaultValue: Int,
    ): Flow<Int> = flow { emit(getInt(key, defaultValue)) }

    override suspend fun putInt(
        key: String,
        value: Int,
    ) {
        data[key] = value
    }

    override suspend fun getBoolean(
        key: String,
        defaultValue: Boolean,
    ): Boolean = (data[key] as? Boolean) ?: defaultValue

    override fun getBooleanAsFlow(
        key: String,
        defaultValue: Boolean,
    ): Flow<Boolean> = flow { emit(getBoolean(key, defaultValue)) }

    override suspend fun putBoolean(
        key: String,
        value: Boolean,
    ) {
        data[key] = value
    }

    override suspend fun getLong(
        key: String,
        defaultValue: Long,
    ): Long = (data[key] as? Long) ?: defaultValue

    override fun getLongAsFlow(
        key: String,
        defaultValue: Long,
    ): Flow<Long> = flow { emit(getLong(key, defaultValue)) }

    override suspend fun putLong(
        key: String,
        value: Long,
    ) {
        data[key] = value
    }

    override suspend fun getDouble(
        key: String,
        defaultValue: Double,
    ): Double = (data[key] as? Double) ?: defaultValue

    override fun getDoubleAsFlow(
        key: String,
        defaultValue: Double,
    ): Flow<Double> = flow { emit(getDouble(key, defaultValue)) }

    override suspend fun putDouble(
        key: String,
        value: Double,
    ) {
        data[key] = value
    }

    override suspend fun contains(key: String): Boolean = data.containsKey(key)

    override suspend fun remove(key: String) {
        data.remove(key)
    }

    override suspend fun clear() {
        data.clear()
    }

    override suspend fun getAllKeys(): Set<String> = data.keys.toSet()
}

// ── Generic JSON serialization extensions ──

@PublishedApi
internal val jsonInstance: Json = Json { ignoreUnknownKeys = true }

/**
 * Get a preference deserialized from JSON.
 *
 * @param key Preference key
 * @param defaultValue Value returned if key not found or parsing fails
 * @return Stored value or defaultValue
 */
public suspend inline fun <reified T> PreferencesStorage.getJson(
    key: String,
    defaultValue: T? = null,
): T? {
    val jsonString = getString(key, "")
    if (jsonString.isEmpty()) return defaultValue
    return try {
        jsonInstance.decodeFromString<T>(jsonString)
    } catch (e: Exception) {
        defaultValue
    }
}

/**
 * Observe a preference deserialized from JSON.
 */
public inline fun <reified T> PreferencesStorage.getJsonAsFlow(
    key: String,
    defaultValue: T? = null,
): Flow<T?> =
    getStringAsFlow(key, "").map { jsonString ->
        if (jsonString.isEmpty()) {
            defaultValue
        } else {
            try {
                jsonInstance.decodeFromString<T>(jsonString)
            } catch (e: Exception) {
                defaultValue
            }
        }
    }

/**
 * Store a preference serialized as JSON.
 */
public suspend inline fun <reified T> PreferencesStorage.putJson(
    key: String,
    value: T,
) {
    val jsonString = jsonInstance.encodeToString(value)
    putString(key, jsonString)
}
