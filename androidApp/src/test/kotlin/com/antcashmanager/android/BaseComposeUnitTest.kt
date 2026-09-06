package com.antcashmanager.android

import android.os.Build
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule

/**
 * Base class per i test di Compose UI che necessitano di:
 * - Dispatcher setup per Coroutines (da BaseUnitTest)
 * - Compose UI test rule (v2 - latest)
 * - Mock di android.os.Build.FINGERPRINT per evitare NullPointerException in Compose test framework
 *
 * Tutti i test di Compose dovrebbero estendere questa classe.
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseComposeUnitTest : BaseUnitTest() {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    open fun setUpBuildMocks() {
        // Mock Build.FINGERPRINT per Compose UI test framework (avoid NPE)
        mockkStatic(Build::class)
        every { Build.FINGERPRINT } returns "fake-fingerprint"
    }

    @After
    open fun tearDownBuildMocks() {
        unmockkStatic(Build::class)
    }
}
