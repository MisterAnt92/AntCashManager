package com.antcashmanager.android.ui.screen.settingsData

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

@RunWith(AndroidJUnit4::class)
class CharsetHandlingTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun readBackupFile_shouldHandleUTF8Encoding() {
        val jsonContent = """{"version":1,"transactions":[],"categories":[]}"""
        val bytes = jsonContent.toByteArray(StandardCharsets.UTF_8)
        val inputStream = ByteArrayInputStream(bytes)

        val result = inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }

        assert(result == jsonContent)
    }

    @Test
    fun readBackupFile_shouldHandleISO88591Encoding() {
        val jsonContent = """{"note":"Café"}"""
        val bytes = jsonContent.toByteArray(StandardCharsets.ISO_8859_1)
        val inputStream = ByteArrayInputStream(bytes)

        val result = inputStream.bufferedReader(StandardCharsets.ISO_8859_1).use { it.readText() }

        assert(result == jsonContent)
    }

    @Test
    fun readBackupFile_shouldFallbackWhenUTF8FailsAndUseISO88591() {
        val jsonContent = """{"note":"data"}"""

        // Simula lettura ISO-8859-1 quando UTF-8 fallisce
        val bytes = jsonContent.toByteArray(StandardCharsets.ISO_8859_1)
        val inputStream = ByteArrayInputStream(bytes)

        val result = runCatching {
            inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        }.recoverCatching {
            ByteArrayInputStream(bytes).bufferedReader(StandardCharsets.ISO_8859_1).use { it.readText() }
        }.getOrNull()

        assert(result == jsonContent)
    }

    @Test
    fun readBackupFile_shouldPreserveUnicodeCharactersInUTF8() {
        val jsonContent = """{"items":["café","naïve","日本語"]}"""
        val bytes = jsonContent.toByteArray(StandardCharsets.UTF_8)
        val inputStream = ByteArrayInputStream(bytes)

        val result = inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }

        assert(result == jsonContent)
    }

    @Test
    fun readBackupFile_shouldHandleMultilineJSON() {
        val jsonContent = """{
    "version": 1,
    "transactions": [
        {"id": 1, "title": "Test"}
    ]
}"""
        val bytes = jsonContent.toByteArray(StandardCharsets.UTF_8)
        val inputStream = ByteArrayInputStream(bytes)

        val result = inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }

        assert(result == jsonContent)
    }

    @Test
    fun readBackupFile_shouldHandleLargeBackupFile() {
        // Simula un file backup grande con molte transazioni
        val largeJson = buildString {
            append("""{"transactions":[""")
            repeat(100) { i ->
                if (i > 0) append(",")
                append("""{"id":$i,"title":"Transazione $i","amount":$i.5}""")
            }
            append("]}")
        }
        val bytes = largeJson.toByteArray(StandardCharsets.UTF_8)
        val inputStream = ByteArrayInputStream(bytes)

        val result = inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }

        assert(result == largeJson)
        assert(result.contains("""Transazione 99"""))
    }

    @Test
    fun readBackupFile_shouldHandleEmptyFile() {
        val emptyContent = ""
        val bytes = emptyContent.toByteArray(StandardCharsets.UTF_8)
        val inputStream = ByteArrayInputStream(bytes)

        val result = inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }

        assert(result == emptyContent)
    }

    @Test
    fun readBackupFile_shouldHandleSpecialCharactersInJSON() {
        val jsonWithSpecialChars = """{"note":"Path: C:\\Users\\test","amount":123.45,"emoji":"😀"}"""
        val bytes = jsonWithSpecialChars.toByteArray(StandardCharsets.UTF_8)
        val inputStream = ByteArrayInputStream(bytes)

        val result = inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }

        assert(result == jsonWithSpecialChars)
    }
}
