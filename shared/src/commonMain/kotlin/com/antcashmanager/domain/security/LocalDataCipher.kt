package com.antcashmanager.domain.security

/**
 * Contratto per cifrare/decifrare i dati locali a livello campo.
 */
public interface LocalDataCipher {
    public fun encryptString(value: String): String
    public fun decryptString(value: String): String
    public fun clearCache(): Unit
}

