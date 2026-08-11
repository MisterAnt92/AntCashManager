package com.antcashmanager.domain.model

import com.antcashmanager.domain.model.AppTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for AppTheme enum.
 *
 * Tests cover:
 * - Theme values (LIGHT, DARK, SYSTEM)
 * - Theme conversion to string
 * - Theme parsing from string
 */
class AppThemeEnumTest {

    @Test
    fun appTheme_shouldHaveLightTheme() {
        val theme = AppTheme.LIGHT
        assertNotNull(theme)
        assertEquals(AppTheme.LIGHT, theme)
    }

    @Test
    fun appTheme_shouldHaveDarkTheme() {
        val theme = AppTheme.DARK
        assertNotNull(theme)
        assertEquals(AppTheme.DARK, theme)
    }

    @Test
    fun appTheme_shouldHaveSystemTheme() {
        val theme = AppTheme.SYSTEM
        assertNotNull(theme)
        assertEquals(AppTheme.SYSTEM, theme)
    }

    @Test
    fun appTheme_shouldHaveThreeValues() {
        val themes = AppTheme.values()
        assertEquals(3, themes.size)
    }

    @Test
    fun appTheme_shouldContainAllThemes() {
        val themes = AppTheme.values().toList()
        assert(themes.contains(AppTheme.LIGHT))
        assert(themes.contains(AppTheme.DARK))
        assert(themes.contains(AppTheme.SYSTEM))
    }

    @Test
    fun appTheme_lightThemeName() {
        assertEquals("LIGHT", AppTheme.LIGHT.name)
    }

    @Test
    fun appTheme_darkThemeName() {
        assertEquals("DARK", AppTheme.DARK.name)
    }

    @Test
    fun appTheme_systemThemeName() {
        assertEquals("SYSTEM", AppTheme.SYSTEM.name)
    }

    @Test
    fun appTheme_valueOfShouldWork() {
        assertEquals(AppTheme.LIGHT, AppTheme.valueOf("LIGHT"))
        assertEquals(AppTheme.DARK, AppTheme.valueOf("DARK"))
        assertEquals(AppTheme.SYSTEM, AppTheme.valueOf("SYSTEM"))
    }
}
