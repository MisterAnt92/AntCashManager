package com.antcashmanager.android.data.storage

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test suite for [AndroidPreferencesStorageImpl].
 *
 * Tests all preference types and operations.
 */
class AndroidPreferencesStorageImplTest {

    private val mockPreferences = mockk<SharedPreferences>()
    private val mockEditor = mockk<SharedPreferences.Editor>()
    private val mockContext = mockk<Context>()
    private lateinit var storage: AndroidPreferencesStorageImpl

    @Before
    fun setup() {
        every { mockContext.getSharedPreferences("app_preferences", Context.MODE_PRIVATE) } returns mockPreferences
        every { mockPreferences.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.putInt(any(), any()) } returns mockEditor
        every { mockEditor.putBoolean(any(), any()) } returns mockEditor
        every { mockEditor.putLong(any(), any()) } returns mockEditor
        every { mockEditor.remove(any()) } returns mockEditor
        every { mockEditor.clear() } returns mockEditor
        every { mockEditor.apply() } returns Unit

        storage = AndroidPreferencesStorageImpl(mockContext)
    }

    // ── String tests ──

    @Test
    fun getString_shouldReturnStoredValue_whenKeyExists() = runTest {
        // Arrange
        every { mockPreferences.getString("theme", "light") } returns "dark"

        // Act
        val result = storage.getString("theme", "light")

        // Assert
        assertEquals("dark", result)
    }

    @Test
    fun getString_shouldReturnDefaultValue_whenKeyNotExists() = runTest {
        // Arrange
        every { mockPreferences.getString("theme", "light") } returns null

        // Act
        val result = storage.getString("theme", "light")

        // Assert
        assertEquals("light", result)
    }

    @Test
    fun putString_shouldStoreValue_whenCalled() = runTest {
        // Act
        storage.putString("theme", "dark")

        // Assert
        verify { mockEditor.putString("theme", "dark") }
        verify { mockEditor.apply() }
    }

    // ── Integer tests ──

    @Test
    fun getInt_shouldReturnStoredValue_whenKeyExists() = runTest {
        // Arrange
        every { mockPreferences.getInt("decimal_digits", 2) } returns 3

        // Act
        val result = storage.getInt("decimal_digits", 2)

        // Assert
        assertEquals(3, result)
    }

    @Test
    fun getInt_shouldReturnDefaultValue_whenKeyNotExists() = runTest {
        // Arrange
        every { mockPreferences.getInt("decimal_digits", 2) } returns 2

        // Act
        val result = storage.getInt("decimal_digits", 2)

        // Assert
        assertEquals(2, result)
    }

    @Test
    fun putInt_shouldStoreValue_whenCalled() = runTest {
        // Act
        storage.putInt("decimal_digits", 3)

        // Assert
        verify { mockEditor.putInt("decimal_digits", 3) }
        verify { mockEditor.apply() }
    }

    // ── Boolean tests ──

    @Test
    fun getBoolean_shouldReturnStoredValue_whenKeyExists() = runTest {
        // Arrange
        every { mockPreferences.getBoolean("high_contrast", false) } returns true

        // Act
        val result = storage.getBoolean("high_contrast", false)

        // Assert
        assertTrue(result)
    }

    @Test
    fun getBoolean_shouldReturnDefaultValue_whenKeyNotExists() = runTest {
        // Arrange
        every { mockPreferences.getBoolean("high_contrast", false) } returns false

        // Act
        val result = storage.getBoolean("high_contrast", false)

        // Assert
        assertFalse(result)
    }

    @Test
    fun putBoolean_shouldStoreValue_whenCalled() = runTest {
        // Act
        storage.putBoolean("high_contrast", true)

        // Assert
        verify { mockEditor.putBoolean("high_contrast", true) }
        verify { mockEditor.apply() }
    }

    // ── Long tests ──

    @Test
    fun getLong_shouldReturnStoredValue_whenKeyExists() = runTest {
        // Arrange
        every { mockPreferences.getLong("timestamp", 0L) } returns 1000000L

        // Act
        val result = storage.getLong("timestamp", 0L)

        // Assert
        assertEquals(1000000L, result)
    }

    @Test
    fun putLong_shouldStoreValue_whenCalled() = runTest {
        // Act
        storage.putLong("timestamp", 1000000L)

        // Assert
        verify { mockEditor.putLong("timestamp", 1000000L) }
        verify { mockEditor.apply() }
    }

    // ── Double tests ──

    @Test
    fun getDouble_shouldReturnStoredValue_whenKeyExists() = runTest {
        // Arrange
        every { mockPreferences.getString("meal_voucher_value", null) } returns "10.50"

        // Act
        val result = storage.getDouble("meal_voucher_value", 0.0)

        // Assert
        assertEquals(10.50, result)
    }

    @Test
    fun getDouble_shouldReturnDefaultValue_whenKeyNotExists() = runTest {
        // Arrange
        every { mockPreferences.getString("meal_voucher_value", null) } returns null

        // Act
        val result = storage.getDouble("meal_voucher_value", 0.0)

        // Assert
        assertEquals(0.0, result)
    }

    @Test
    fun putDouble_shouldStoreValue_whenCalled() = runTest {
        // Act
        storage.putDouble("meal_voucher_value", 10.50)

        // Assert
        verify { mockEditor.putString("meal_voucher_value", "10.5") }
        verify { mockEditor.apply() }
    }

    // ── Key operations ──

    @Test
    fun contains_shouldReturnTrue_whenKeyExists() = runTest {
        // Arrange
        every { mockPreferences.contains("theme") } returns true

        // Act
        val result = storage.contains("theme")

        // Assert
        assertTrue(result)
    }

    @Test
    fun contains_shouldReturnFalse_whenKeyNotExists() = runTest {
        // Arrange
        every { mockPreferences.contains("theme") } returns false

        // Act
        val result = storage.contains("theme")

        // Assert
        assertFalse(result)
    }

    @Test
    fun remove_shouldRemoveKey_whenCalled() = runTest {
        // Act
        storage.remove("theme")

        // Assert
        verify { mockEditor.remove("theme") }
        verify { mockEditor.apply() }
    }

    @Test
    fun clear_shouldClearAllPreferences_whenCalled() = runTest {
        // Act
        storage.clear()

        // Assert
        verify { mockEditor.clear() }
        verify { mockEditor.apply() }
    }

    @Test
    fun getAllKeys_shouldReturnAllKeys_whenCalled() = runTest {
        // Arrange
        val mockMap = mapOf("theme" to "dark", "language" to "en")
        every { mockPreferences.all } returns mockMap

        // Act
        val result = storage.getAllKeys()

        // Assert
        assertEquals(setOf("theme", "language"), result)
    }
}
