package com.antcashmanager.domain.security

/**
 * Contratto per cifrare/decifrare i dati locali a livello campo.
 */
interface LocalDataCipher {
    fun encryptString(value: String): String
    fun decryptString(value: String): String
    fun clearCache()
}

