package com.antcashmanager.android

import android.os.Build
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule

/**
 * Base class per i test di Compose UI che necessitano di:
 * - Dispatcher setup per Coroutines (da BaseUnitTest)
 * - Compose UI test rule (v2 - latest)
 * - Mock di android.os.Build.FINGERPRINT per evitare NullPointerException in Compose test framework
 *
 * NOTA IMPORTANTE: I test Compose in src/test hanno limitazioni intrinseche:
 * - createComposeRule() non può lanciare un'Activity reale (disponibile solo in AndroidJUnit4/src/androidTest)
 * - La integrazione con Robolectric è fragile e non supportata ufficialmente
 * - Test con dipendenze Android complesse (Resources, Context, Koin) falliscono
 *
 * MIGRAZIONE: Questi test vanno spostati a src/androidTest/kotlin/ dove avranno:
 * - Robolectric TestRunner
 * - Accesso completo al framework Android
 * - Gestione corretta di Activity e lifecycle
 *
 * Per adesso, i test estendono questa classe ma sono marked con @Ignore e devono essere migrati.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Ignore("Compose tests in src/test must be migrated to src/androidTest - see BaseComposeUnitTest KDoc")
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
