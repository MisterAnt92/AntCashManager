@file:Suppress("LocalVariableName")

package com.antcashmanager.android.ui.components.button

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.antcashmanager.android.BaseComposeUnitTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests per [ReorderButtons] component.
 *
 * Testa:
 * - Stato enabled/disabled dei pulsanti
 * - Callback execution su click
 * - Tint dinamico (non testabile direttamente, ma verifichiamo enabled state che lo controlla)
 */
@RunWith(RobolectricTestRunner::class)
class ReorderButtonsTest : BaseComposeUnitTest() {
    private val testUpDescription = "Test Move Up"
    private val testDownDescription = "Test Move Down"

    @Test
    fun whenCanMoveUp_upButtonIsEnabled() {
        composeTestRule.setContent {
            ReorderButtons(
                onMoveUp = {},
                onMoveDown = {},
                canMoveUp = true,
                canMoveDown = false,
                upDescription = testUpDescription,
                downDescription = testDownDescription,
            )
        }

        composeTestRule
            .onNodeWithContentDescription(testUpDescription)
            .assertIsEnabled()
    }

    @Test
    fun whenCannotMoveUp_upButtonIsDisabled() {
        composeTestRule.setContent {
            ReorderButtons(
                onMoveUp = {},
                onMoveDown = {},
                canMoveUp = false,
                canMoveDown = true,
                upDescription = testUpDescription,
                downDescription = testDownDescription,
            )
        }

        composeTestRule
            .onNodeWithContentDescription(testUpDescription)
            .assertIsNotEnabled()
    }

    @Test
    fun whenCanMoveDown_downButtonIsEnabled() {
        composeTestRule.setContent {
            ReorderButtons(
                onMoveUp = {},
                onMoveDown = {},
                canMoveUp = false,
                canMoveDown = true,
                upDescription = testUpDescription,
                downDescription = testDownDescription,
            )
        }

        composeTestRule
            .onNodeWithContentDescription(testDownDescription)
            .assertIsEnabled()
    }

    @Test
    fun whenCannotMoveDown_downButtonIsDisabled() {
        composeTestRule.setContent {
            ReorderButtons(
                onMoveUp = {},
                onMoveDown = {},
                canMoveUp = true,
                canMoveDown = false,
                upDescription = testUpDescription,
                downDescription = testDownDescription,
            )
        }

        composeTestRule
            .onNodeWithContentDescription(testDownDescription)
            .assertIsNotEnabled()
    }

    @Test
    fun onMoveUp_callbackIsCalled() {
        var upClicked = false
        composeTestRule.setContent {
            ReorderButtons(
                onMoveUp = { upClicked = true },
                onMoveDown = {},
                canMoveUp = true,
                canMoveDown = true,
                upDescription = testUpDescription,
                downDescription = testDownDescription,
            )
        }

        composeTestRule
            .onNodeWithContentDescription(testUpDescription)
            .performClick()

        assert(upClicked)
    }

    @Test
    fun onMoveDown_callbackIsCalled() {
        var downClicked = false
        composeTestRule.setContent {
            ReorderButtons(
                onMoveUp = {},
                onMoveDown = { downClicked = true },
                canMoveUp = true,
                canMoveDown = true,
                upDescription = testUpDescription,
                downDescription = testDownDescription,
            )
        }

        composeTestRule
            .onNodeWithContentDescription(testDownDescription)
            .performClick()

        assert(downClicked)
    }

    @Test
    fun whenBothDisabled_neitherButtonRespondsToClick() {
        var upClicked = false
        var downClicked = false
        composeTestRule.setContent {
            ReorderButtons(
                onMoveUp = { upClicked = true },
                onMoveDown = { downClicked = true },
                canMoveUp = false,
                canMoveDown = false,
                upDescription = testUpDescription,
                downDescription = testDownDescription,
            )
        }

        // Tentare di cliccare pulsanti disabilitati (non dovrebbe funzionare)
        composeTestRule
            .onNodeWithContentDescription(testUpDescription)
            .performClick()
        composeTestRule
            .onNodeWithContentDescription(testDownDescription)
            .performClick()

        assert(!upClicked)
        assert(!downClicked)
    }

    @Test
    fun whenBothEnabled_bothButtonsRespond() {
        var upClicked = false
        var downClicked = false
        composeTestRule.setContent {
            ReorderButtons(
                onMoveUp = { upClicked = true },
                onMoveDown = { downClicked = true },
                canMoveUp = true,
                canMoveDown = true,
                upDescription = testUpDescription,
                downDescription = testDownDescription,
            )
        }

        composeTestRule
            .onNodeWithContentDescription(testUpDescription)
            .performClick()
        composeTestRule
            .onNodeWithContentDescription(testDownDescription)
            .performClick()

        assert(upClicked)
        assert(downClicked)
    }

    @Test
    fun multipleClicks_callCallbackMultipleTimes() {
        var upClickCount = 0
        composeTestRule.setContent {
            ReorderButtons(
                onMoveUp = { upClickCount++ },
                onMoveDown = {},
                canMoveUp = true,
                canMoveDown = false,
                upDescription = testUpDescription,
                downDescription = testDownDescription,
            )
        }

        repeat(3) {
            composeTestRule
                .onNodeWithContentDescription(testUpDescription)
                .performClick()
        }

        assert(upClickCount == 3)
    }
}
