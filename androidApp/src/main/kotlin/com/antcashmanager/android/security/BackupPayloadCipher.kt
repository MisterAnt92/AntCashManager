package com.antcashmanager.android.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec

/**
 * Cifra/decifra il payload di backup con AES-GCM usando una chiave custodita in Android Keystore.
 */
object BackupPayloadCipher {
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "antcashmanager_backup_payload_key_v1"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val ENCRYPTED_PREFIX = "ACM_ENC_V1:"
    private const val ENCRYPTED_PREFIX_V2 = "ACM_ENC_V2:"  // PBKDF2-derived key version

    // PBKDF2 parameters
    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val PBKDF2_ITERATIONS = 100_000
    private const val PBKDF2_KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16

    fun isEncryptedPayload(payload: String): Boolean =
        payload.startsWith(ENCRYPTED_PREFIX) || payload.startsWith(ENCRYPTED_PREFIX_V2)

    private fun isV2Payload(payload: String): Boolean = payload.startsWith(ENCRYPTED_PREFIX_V2)

    fun encrypt(plainText: String): String {
        val secretKey = getOrCreateSecretKey()
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, secretKey)
        }

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val dataBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        return "$ENCRYPTED_PREFIX$ivBase64:$dataBase64"
    }

    fun decrypt(payload: String): String {
        require(isEncryptedPayload(payload)) { "Payload is not encrypted with supported format" }

        return if (isV2Payload(payload)) {
            // V2: PBKDF2-based (but we can't decrypt without password from older method)
            throw IllegalArgumentException("This backup uses password-based encryption (V2). Use decryptWithPassword() instead.")
        } else {
            // V1: Legacy Keystore-based (device-bound)
            decryptV1Legacy(payload)
        }
    }

    /**
     * Encrypt backup with password-derived key (V2: portable across devices).
     * Format: "ACM_ENC_V2:salt:iv:data" (all base64-encoded)
     */
    fun encryptWithPassword(plainText: String, password: String): String {
        val salt = ByteArray(SALT_LENGTH_BYTES).apply {
            SecureRandom().nextBytes(this)
        }

        val secretKey = deriveKeyFromPassword(password, salt)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, secretKey)
        }

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        val saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val dataBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        return "$ENCRYPTED_PREFIX_V2$saltBase64:$ivBase64:$dataBase64"
    }

    /**
     * Decrypt backup with password-derived key (V2: portable across devices).
     */
    fun decryptWithPassword(payload: String, password: String): String {
        require(isV2Payload(payload)) { "Payload is not encrypted with V2 format" }

        val encoded = payload.removePrefix(ENCRYPTED_PREFIX_V2)
        val parts = encoded.split(':', limit = 3)
        require(parts.size == 3) { "Invalid encrypted payload V2 format" }

        val salt = Base64.decode(parts[0], Base64.NO_WRAP)
        val iv = Base64.decode(parts[1], Base64.NO_WRAP)
        val encryptedBytes = Base64.decode(parts[2], Base64.NO_WRAP)

        val secretKey = deriveKeyFromPassword(password, salt)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        }

        val plainBytes = cipher.doFinal(encryptedBytes)
        return String(plainBytes, Charsets.UTF_8)
    }

    private fun decryptV1Legacy(payload: String): String {
        val encoded = payload.removePrefix(ENCRYPTED_PREFIX)
        val parts = encoded.split(':', limit = 2)
        require(parts.size == 2) { "Invalid encrypted payload format" }

        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val encryptedBytes = Base64.decode(parts[1], Base64.NO_WRAP)

        val secretKey = getOrCreateSecretKey()
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        }

        return try {
            val plainBytes = cipher.doFinal(encryptedBytes)
            String(plainBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            // V1 uses device-bound Keystore key: if decrypt fails, the backup
            // was created on a different device (not a password error)
            throw IllegalArgumentException(
                "Failed to decrypt backup: This backup was created on another device. " +
                    "Device-bound backups cannot be restored across different phones. " +
                    "Use password-protected backups (v1.8+) for cross-device portability.",
                e
            )
        }
    }

    private fun deriveKeyFromPassword(password: String, salt: ByteArray): SecretKey {
        val keySpec = PBEKeySpec(
            password.toCharArray(),
            salt,
            PBKDF2_ITERATIONS,
            PBKDF2_KEY_LENGTH_BITS
        )
        val keyFactory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        return keyFactory.generateSecret(keySpec)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) return existingKey

        val keyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }
}

