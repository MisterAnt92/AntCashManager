package com.antcashmanager.android

import androidx.compose.ui.test.junit4.v2.createComposeRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule

/**
 * Base class per i test di Compose UI che necessitano di:
 * - Dispatcher setup per Coroutines (da BaseUnitTest)
 * - Compose UI test rule (v2 - latest)
 *
 * IMPORTANTE: NON usare @RunWith(RobolectricTestRunner::class) nei test di unit test!
 * Roboelectric è riservato SOLO per instrumentation test (androidTest).
 *
 * Regola del progetto: MockK + Fake repositories + Compose.uiTest v2 (no Roboelectric in unit tests)
 *
 * Tutti i test di Compose dovrebbero estendere questa classe.
 *
 * Esempio corretto:
 * ```kotlin
 * class MyComponentTest : BaseComposeUnitTest() {
 *     @Test
 *     fun myComponentRendersCorrectly() {
 *         composeTestRule.setContent {
 *             MyComponent()
 *         }
 *         composeTestRule.onNodeWithText("Hello").assertIsDisplayed()
 *     }
 * }
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseComposeUnitTest : BaseUnitTest() {

    @get:Rule
    val composeTestRule = createComposeRule()
}
