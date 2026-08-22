package com.antcashmanager.android.data.storage

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import co.touchlab.kermit.Logger
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Gestisce la cifratura e decifratura dei token Google Drive usando AndroidKeyStore.
 *
 * **Comportamento per versione Android**:
 * - **API 28+**: Usa AndroidKeyStore nativo (EncryptedSharedPreferences wrapper)
 * - **API 21-27**: Fallback a EncryptedSharedPreferences con MasterKey (genera chiave AES automatica)
 * - **API <21**: Non supportato (avrebbe bisogno di un'implementazione di fallback diversa)
 *
 * **Sicurezza**:
 * - AccessToken (1h): Cifratura AES-GCM, auto-refresh via Google Play Services
 * - RefreshToken (long-lived): Cifratura AES-GCM robusta, revocabile solo da Google
 * - Non persiste in plain text — sempre cifratura attiva
 *
 * **Utilizzo**:
 * ```kotlin
 * val storage = EncryptedGoogleDriveStorage(context)
 * storage.saveAccessToken("token_value")
 * val token = storage.getAccessToken()
 * storage.clearAllTokens()  // On logout
 * ```
 */
class EncryptedGoogleDriveStorage(
    private val context: Context,
) {

    companion object {
        private const val PREFS_NAME = "google_drive_tokens"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEYSTORE_ALIAS = "google_drive_key"
    }

    private val logger = Logger

    /**
     * Salva l'access token (cifratura AES-GCM).
     *
     * **Nota**: AccessToken ha vita breve (1 ora). È richifrabile se scade durante l'uso.
     */
    suspend fun saveAccessToken(token: String) {
        try {
            val prefs = getEncryptedSharedPreferences()
            prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
            logger.d(tag = "GoogleDriveStorage") { "Access token saved successfully" }
        } catch (e: Exception) {
            logger.e(tag = "GoogleDriveStorage") { "Error saving access token: ${e.message}" }
            throw e
        }
    }

    /**
     * Recupera l'access token (decifratura AES-GCM).
     */
    suspend fun getAccessToken(): String? {
        return try {
            val prefs = getEncryptedSharedPreferences()
            val token = prefs.getString(KEY_ACCESS_TOKEN, null)
            logger.d(tag = "GoogleDriveStorage") { "Access token retrieved (${if (token != null) "present" else "null"})" }
            token
        } catch (e: Exception) {
            logger.e(tag = "GoogleDriveStorage") { "Error retrieving access token: ${e.message}" }
            null
        }
    }

    /**
     * Salva il refresh token (cifratura AES-GCM robusta).
     *
     * **Nota**: Refresh token ha vita lunga (finché non revocato da Google).
     * Non deve mai essere perso — è il "master key" per ottenere nuovi access token.
     */
    suspend fun saveRefreshToken(token: String) {
        try {
            val prefs = getEncryptedSharedPreferences()
            prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
            logger.d(tag = "GoogleDriveStorage") { "Refresh token saved successfully" }
        } catch (e: Exception) {
            logger.e(tag = "GoogleDriveStorage") { "Error saving refresh token: ${e.message}" }
            throw e
        }
    }

    /**
     * Recupera il refresh token (decifratura AES-GCM).
     */
    suspend fun getRefreshToken(): String? {
        return try {
            val prefs = getEncryptedSharedPreferences()
            val token = prefs.getString(KEY_REFRESH_TOKEN, null)
            logger.d(tag = "GoogleDriveStorage") { "Refresh token retrieved (${if (token != null) "present" else "null"})" }
            token
        } catch (e: Exception) {
            logger.e(tag = "GoogleDriveStorage") { "Error retrieving refresh token: ${e.message}" }
            null
        }
    }

    /**
     * Cancella tutti i token (access + refresh).
     *
     * **Utilizzo**: Logout dell'utente — rivoca l'accesso a Google Drive.
     */
    suspend fun clearAllTokens() {
        try {
            val prefs = getEncryptedSharedPreferences()
            prefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .apply()
            logger.d(tag = "GoogleDriveStorage") { "All tokens cleared successfully" }
        } catch (e: Exception) {
            logger.e(tag = "GoogleDriveStorage") { "Error clearing tokens: ${e.message}" }
            throw e
        }
    }

    /**
     * Otiene le shared preferences cifrate.
     *
     * **Implementazione**:
     * - API 28+: Usa MasterKey di sistema + AES-GCM
     * - API 21-27: Genera MasterKey con EncryptedSharedPreferences (supportato da androidx.security)
     * - API <21: Eccezione — non supportato
     */
    private fun getEncryptedSharedPreferences(): android.content.SharedPreferences {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // API 28+: MasterKey nativo con AndroidKeyStore
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // API 21-27: Fallback a EncryptedSharedPreferences con MasterKey automatico
            logger.w(tag = "GoogleDriveStorage") { "Using EncryptedSharedPreferences fallback for API ${Build.VERSION.SDK_INT}" }

            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } else {
            throw UnsupportedOperationException("EncryptedGoogleDriveStorage requires API 21+")
        }
    }
}
