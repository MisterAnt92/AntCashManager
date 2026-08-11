package com.antcashmanager.android

import androidx.compose.ui.test.junit4.v2.createComposeRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Base class per i test di Compose UI che necessitano di:
 * - Roboelectric per simulare Android framework
 * - Dispatcher setup per Coroutines (da BaseUnitTest)
 * - Compose UI test rule
 *
 * Tutti i test di Compose dovrebbero estendere questa classe.
 *
 * Esempio:
 * ```kotlin
 * @RunWith(RobolectricTestRunner::class)
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
@RunWith(RobolectricTestRunner::class)
abstract class BaseComposeUnitTest : BaseUnitTest() {

    @get:Rule
    val composeTestRule = createComposeRule()
}
