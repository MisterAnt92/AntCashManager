package com.antcashmanager.data.security

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class LocalDataCipherImplTest {

    private lateinit var cipher: LocalDataCipherImpl
    private lateinit var mockContext: Context

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)

        // Setup SharedPreferences mock to enable encryption by default
        val mockPrefs = mockk<SharedPreferences>()
        every { mockPrefs.getBoolean("desired_data_encryption", false) } returns true

        // Make context.applicationContext return mockContext itself
        every { mockContext.applicationContext } returns mockContext

        every {
            mockContext.getSharedPreferences(
                "db_security_prefs",
                Context.MODE_PRIVATE
            )
        } returns mockPrefs

        cipher = LocalDataCipherImpl(mockContext)
    }

    @Ignore("TODO: Fix MockK setup for SharedPreferences - encryption config needed")
    @Test
    fun encryptString_shouldEncryptNonEmptyValue_whenStringProvided() {
        val plaintext = "Sensitive Data"

        val encrypted = cipher.encryptString(plaintext)

        assertNotEquals(plaintext, encrypted)
        assertTrue(encrypted.length > plaintext.length)
    }

    @Test
    fun encryptString_shouldReturnEmptyString_whenInputIsEmpty() {
        val encrypted = cipher.encryptString("")

        assertEquals("", encrypted)
    }

    @Test
    fun encryptString_shouldNotDoubleEncrypt_whenAlreadyEncrypted() {
        val plaintext = "Data"
        val firstEncryption = cipher.encryptString(plaintext)

        val secondEncryption = cipher.encryptString(firstEncryption)

        assertEquals(firstEncryption, secondEncryption)
    }

    @Test
    fun decryptString_shouldRecoverOriginalValue_whenValidEncryptedStringProvided() {
        val plaintext = "Secret Message"
        val encrypted = cipher.encryptString(plaintext)

        val decrypted = cipher.decryptString(encrypted)

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun decryptString_shouldReturnOriginal_whenInputIsNotEncrypted() {
        val plaintext = "Not Encrypted"

        val result = cipher.decryptString(plaintext)

        assertEquals(plaintext, result)
    }

    @Test
    fun decryptString_shouldReturnEmptyString_whenInputIsEmpty() {
        val result = cipher.decryptString("")

        assertEquals("", result)
    }

    @Test
    fun roundTripEncryptDecrypt_shouldPreserveOriginalValue_whenMultipleOperations() {
        val values = listOf(
            "Simple text",
            "123456",
            "Special chars: !@#$%^&*()",
            "Unicode: 你好世界",
            "Very long text that spans multiple lines and contains lots of information",
        )

        for (value in values) {
            val encrypted = cipher.encryptString(value)
            val decrypted = cipher.decryptString(encrypted)
            assertEquals("Round trip failed for: $value", value, decrypted)
        }
    }

    @Test
    fun encryptString_shouldProduceConsistentResults_whenSameValueEncryptedMultipleTimes() {
        val plaintext = "Consistent Value"

        val encrypted1 = cipher.encryptString(plaintext)
        val encrypted2 = cipher.encryptString(plaintext)

        assertEquals(encrypted1, encrypted2)
    }

    @Test
    fun decryptString_shouldHandleSpecialCharacters_whenStringContainsSymbols() {
        val plaintext = "Email: user@example.com, Phone: +1-234-567-8900"

        val encrypted = cipher.encryptString(plaintext)
        val decrypted = cipher.decryptString(encrypted)

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun encryptString_shouldHandleLongStrings_whenTextIsVeryLarge() {
        val plaintext = "A".repeat(10000)

        val encrypted = cipher.encryptString(plaintext)
        val decrypted = cipher.decryptString(encrypted)

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun clearCache_shouldSucceed_whenCalled() {
        cipher.encryptString("Data1")
        cipher.encryptString("Data2")

        cipher.clearCache()

        // Verify cipher still works after cache clear
        val plaintext = "Data3"
        val encrypted = cipher.encryptString(plaintext)
        val decrypted = cipher.decryptString(encrypted)
        assertEquals(plaintext, decrypted)
    }

    @Test
    fun encryptString_shouldProduceUniqueResults_whenDifferentValuesEncrypted() {
        val encrypted1 = cipher.encryptString("Value1")
        val encrypted2 = cipher.encryptString("Value2")

        assertNotEquals(encrypted1, encrypted2)
    }

    @Test
    fun decryptString_shouldPreserveLeadingTrailingSpaces_whenStringContainsWhitespace() {
        val plaintext = "  Leading and trailing spaces  "

        val encrypted = cipher.encryptString(plaintext)
        val decrypted = cipher.decryptString(encrypted)

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun encryptString_shouldHandleNewlines_whenStringContainsLineBreaks() {
        val plaintext = "Line1\nLine2\nLine3"

        val encrypted = cipher.encryptString(plaintext)
        val decrypted = cipher.decryptString(encrypted)

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun encryptString_shouldHandleTabs_whenStringContainsTabCharacters() {
        val plaintext = "Column1\tColumn2\tColumn3"

        val encrypted = cipher.encryptString(plaintext)
        val decrypted = cipher.decryptString(encrypted)

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun decryptString_shouldHandleInvalidBase64_gracefully_whenStringIsCorrupted() {
        // This test verifies fallback behavior when encryption is corrupted
        val corruptedEncrypted = "enc:!@#$%^&*()"

        // Should not throw, should return as-is or handle gracefully
        val result = cipher.decryptString(corruptedEncrypted)

        assertTrue(result.isNotEmpty())
    }
}

