package com.antcashmanager.data.local

import android.content.Context

/**
 * Gestisce lo stato desiderato della cifratura locale field-level.
 * Non usa più SQLCipher o librerie native.
 */
public object DatabaseEncryptionManager {
    private const val PREFS_NAME = "db_security_prefs"
    private const val KEY_DESIRED_ENCRYPTION = "desired_data_encryption"

    public fun isEncryptionDesired(context: Context): Boolean = prefs(context).getBoolean(KEY_DESIRED_ENCRYPTION, false)

    public fun setEncryptionDesired(
        context: Context,
        enabled: Boolean,
    ) {
        prefs(context).edit().putBoolean(KEY_DESIRED_ENCRYPTION, enabled).apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
