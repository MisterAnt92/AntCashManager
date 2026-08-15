package com.antcashmanager.android.security

import android.util.Base64
import com.antcashmanager.android.BaseUnitTest
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupPayloadCipherTest : BaseUnitTest() {

    @Test
    fun isEncryptedPayload_shouldReturnTrue_whenPayloadStartsWithPrefix() {
        val encrypted = "ACM_ENC_V1:abc123:def456"
        assertTrue(BackupPayloadCipher.isEncryptedPayload(encrypted))
    }

    @Test
    fun isEncryptedPayload_shouldReturnFalse_whenPayloadDoesNotStartWithPrefix() {
        val notEncrypted = """{"version": 1}"""
        assertFalse(BackupPayloadCipher.isEncryptedPayload(notEncrypted))
    }

    @Test
    fun encryptThenDecrypt_shouldPreservePlainText_roundTrip() {
        val plainText = """{"data":"test","amount":123.45}"""

        val encrypted = BackupPayloadCipher.encrypt(plainText)
        assertTrue(encrypted.startsWith("ACM_ENC_V1:"))

        val decrypted = BackupPayloadCipher.decrypt(encrypted)
        assertEquals(plainText, decrypted)
    }

    @Test
    fun encryptThenDecrypt_shouldHandleUnicodeCharacters_roundTrip() {
        val plainTextWithUnicode = """{"note":"Café ☕ 食べ物"}"""

        val encrypted = BackupPayloadCipher.encrypt(plainTextWithUnicode)
        val decrypted = BackupPayloadCipher.decrypt(encrypted)

        assertEquals(plainTextWithUnicode, decrypted)
    }

    @Test
    fun encryptThenDecrypt_shouldHandleEmptyString_roundTrip() {
        val plainText = ""

        val encrypted = BackupPayloadCipher.encrypt(plainText)
        val decrypted = BackupPayloadCipher.decrypt(encrypted)

        assertEquals(plainText, decrypted)
    }

    @Test
    fun encryptThenDecrypt_shouldHandleLargePayload_roundTrip() {
        // Simula un backup con molte transazioni
        val largePayload = buildString {
            append("""{"transactions":[""")
            repeat(1000) { i ->
                if (i > 0) append(",")
                append("""{"id":$i,"title":"Transazione $i","amount":$i.5}""")
            }
            append("]}")
        }

        val encrypted = BackupPayloadCipher.encrypt(largePayload)
        val decrypted = BackupPayloadCipher.decrypt(encrypted)

        assertEquals(largePayload, decrypted)
    }

    @Test
    fun decrypt_shouldFailWithInvalidPayloadFormat_whenPrefixMissing() {
        val invalidPayload = "invalid:abc123:def456"

        val result = runCatching { BackupPayloadCipher.decrypt(invalidPayload) }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("not encrypted") == true)
    }

    @Test
    fun decrypt_shouldFailWithInvalidPayloadFormat_whenPartsIncomplete() {
        val malformedPayload = "ACM_ENC_V1:onlyOnepart"

        val result = runCatching { BackupPayloadCipher.decrypt(malformedPayload) }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Invalid") == true)
    }

    @Test
    fun decrypt_shouldFailWithBadPadding_whenDataTampered() {
        // Criptare un payload
        val plainText = """{"data":"secret"}"""
        val encrypted = BackupPayloadCipher.encrypt(plainText)

        // Tamperare i dati
        val parts = encrypted.removePrefix("ACM_ENC_V1:").split(":", limit = 2)
        val tamperedData = parts[1].dropLast(1) + "X" // Modifica l'ultimo byte
        val tamperedPayload = "ACM_ENC_V1:${parts[0]}:$tamperedData"

        val result = runCatching { BackupPayloadCipher.decrypt(tamperedPayload) }

        assertTrue(result.isFailure)
    }

    @Test
    fun encryptDifferentPlainTexts_shouldProduceDifferentCiphertexts() {
        val text1 = "backup data 1"
        val text2 = "backup data 2"

        val encrypted1 = BackupPayloadCipher.encrypt(text1)
        val encrypted2 = BackupPayloadCipher.encrypt(text2)

        // Anche criptando la stessa cosa due volte, l'IV è randomico
        assertFalse(encrypted1 == encrypted2)
    }

    @Test
    fun encryptSameTextTwice_shouldProduceDifferentCiphertexts() {
        val text = "same plaintext"

        val encrypted1 = BackupPayloadCipher.encrypt(text)
        val encrypted2 = BackupPayloadCipher.encrypt(text)

        // L'IV è randomico, quindi cifrature diverse per lo stesso testo
        assertFalse(encrypted1 == encrypted2)

        // Ma ambedue decriptano al testo originale
        assertEquals(text, BackupPayloadCipher.decrypt(encrypted1))
        assertEquals(text, BackupPayloadCipher.decrypt(encrypted2))
    }

    @Test
    fun decrypt_shouldReturnCorrectStringType_notByteArrayString() {
        val plainText = "test"
        val encrypted = BackupPayloadCipher.encrypt(plainText)

        val decrypted = BackupPayloadCipher.decrypt(encrypted)

        // Verifica che non sia il risultato errato "[B@..."
        assertFalse(decrypted.startsWith("[B@"))
        assertEquals(plainText, decrypted)
        assertTrue(decrypted is String)
    }
}
