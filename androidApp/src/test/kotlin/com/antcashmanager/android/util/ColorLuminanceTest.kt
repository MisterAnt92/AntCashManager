package com.antcashmanager.android.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorLuminanceTest {
    @Test
    fun isPerceptuallyLight_shouldBeTrue_whenWhite() {
        assertTrue(0xFFFFFFFFL.isPerceptuallyLight())
    }

    @Test
    fun isPerceptuallyLight_shouldBeFalse_whenBlackOrDarkGrey() {
        assertFalse(0xFF000000L.isPerceptuallyLight())
        assertFalse(0xFF212121L.isPerceptuallyLight())
    }

    @Test
    fun isPerceptuallyLight_shouldHonourThreshold_whenMidGrey() {
        val midGrey = 0xFF808080L
        assertTrue(midGrey.isPerceptuallyLight(threshold = 0.5f))
        assertFalse(midGrey.isPerceptuallyLight(threshold = 0.6f))
    }
}
