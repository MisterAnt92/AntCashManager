package com.antcashmanager.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import co.touchlab.kermit.Logger
import com.antcashmanager.domain.security.LocalDataCipher
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Cifratura locale field-level basata su Android Keystore (AES/GCM).
 * La chiave viene mantenuta in cache in memoria per la durata della sessione.
 */
public class LocalDataCipherImpl(
    context: Context,
) : LocalDataCipher {
    public companion object {
        private const val TAG = "LocalDataCipher"
        private const val PREFS_NAME = "db_security_prefs"
        private const val KEY_DESIRED_ENCRYPTION = "desired_data_encryption"

        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "antcashmanager_local_data_key_v1"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH_BITS = 128
        private const val AES_KEY_SIZE_BITS = 256

        private const val ENCRYPTED_PREFIX = "enc:"
        private const val PAYLOAD_SEPARATOR = ":"
    }

    private val appContext = context.applicationContext
    private val logger = Logger.withTag(TAG)
    private val lock = Any()

    @Volatile
    private var cachedKey: SecretKey? = null

    override fun encryptString(value: String): String {
        if (value.isEmpty() || !isEncryptionEnabled() || value.startsWith(ENCRYPTED_PREFIX)) {
            return value
        }

        return runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            }
            val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            val iv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
            val payload = Base64.encodeToString(encrypted, Base64.NO_WRAP)
            "$ENCRYPTED_PREFIX$iv$PAYLOAD_SEPARATOR$payload"
        }.getOrElse { error ->
            logger.e(error) { "Encrypt fallback to plaintext" }
            value
        }
    }

    override fun decryptString(value: String): String {
        if (value.isEmpty() || !value.startsWith(ENCRYPTED_PREFIX)) {
            return value
        }

        return runCatching {
            val payload = value.removePrefix(ENCRYPTED_PREFIX)
            val separatorIndex = payload.indexOf(PAYLOAD_SEPARATOR)
            if (separatorIndex <= 0) return@runCatching value

            val ivBytes = Base64.decode(payload.substring(0, separatorIndex), Base64.NO_WRAP)
            val encryptedBytes =
                Base64.decode(payload.substring(separatorIndex + 1), Base64.NO_WRAP)

            val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                init(
                    Cipher.DECRYPT_MODE,
                    getOrCreateKey(),
                    GCMParameterSpec(GCM_TAG_LENGTH_BITS, ivBytes)
                )
            }
            String(cipher.doFinal(encryptedBytes), Charsets.UTF_8)
        }.getOrElse { error ->
            logger.e(error) { "Decrypt fallback to stored value" }
            value
        }
    }

    override fun clearCache() {
        synchronized(lock) {
            cachedKey = null
        }
    }

    private fun isEncryptionEnabled(): Boolean =
        appContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_DESIRED_ENCRYPTION, false)

    private fun getOrCreateKey(): SecretKey {
        cachedKey?.let { return it }

        synchronized(lock) {
            cachedKey?.let { return it }

            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
            val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
            if (existing != null) {
                cachedKey = existing
                return existing
            }

            val keyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(AES_KEY_SIZE_BITS)
                .build()
            keyGenerator.init(spec)
            val key = keyGenerator.generateKey()
            cachedKey = key
            return key
        }
    }
}

