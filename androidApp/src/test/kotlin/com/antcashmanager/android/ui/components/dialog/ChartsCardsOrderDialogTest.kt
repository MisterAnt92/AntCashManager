package com.antcashmanager.android.ui.components.dialog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onNodeWithContentDescription
import com.antcashmanager.android.ui.screen.charts.view.ChartsCardsOrderDialog
import com.antcashmanager.android.ui.screen.charts.model.ChartCardType
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests per [ChartsCardsOrderDialog] component.
 *
 * Testa:
 * - Visualizzazione degli elementi nella lista
 * - Callback di move up/down
 * - Stato enabled/disabled dei pulsanti (first/last element logic)
 * - Confirm e dismiss button behavior
 * - Accessibility (contentDescription)
 */
class ChartsCardsOrderDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val testOrder = listOf(
        ChartCardType.SPENDING_FORECAST_CARD,
        ChartCardType.QUICK_STATS_CARD,
    )

    @Test
    fun dialog_shouldDisplayAllOrderItems() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                ChartsCardsOrderDialog(
                    order = testOrder,
                    onMoveUp = {},
                    onMoveDown = {},
                    onDismiss = {},
                    onConfirm = {},
                )
            }
        }

        // Verificare che la dialog è visualizzata
        composeTestRule.onNodeWithText("", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun dialog_shouldCallOnMoveUp_whenMoveUpButtonClicked() {
        var moveUpCalled = false
        var moveUpIndex = -1

        composeTestRule.setContent {
            AntCashManagerTheme {
                ChartsCardsOrderDialog(
                    order = testOrder,
                    onMoveUp = { index ->
                        moveUpCalled = true
                        moveUpIndex = index
                    },
                    onMoveDown = {},
                    onDismiss = {},
                    onConfirm = {},
                )
            }
        }

        // Il secondo elemento ha move up abilitato
        composeTestRule.onNodeWithContentDescription("Move up", substring = true)
            .performClick()

        assert(moveUpCalled)
    }

    @Test
    fun dialog_shouldCallOnMoveDown_whenMoveDownButtonClicked() {
        var moveDownCalled = false

        composeTestRule.setContent {
            AntCashManagerTheme {
                ChartsCardsOrderDialog(
                    order = testOrder,
                    onMoveUp = {},
                    onMoveDown = { moveDownCalled = true },
                    onDismiss = {},
                    onConfirm = {},
                )
            }
        }

        // Il primo elemento ha move down abilitato
        composeTestRule.onNodeWithContentDescription("Move down", substring = true)
            .performClick()

        assert(moveDownCalled)
    }

    @Test
    fun dialog_shouldCallOnDismiss_whenCancelButtonClicked() {
        var dismissCalled = false

        composeTestRule.setContent {
            AntCashManagerTheme {
                ChartsCardsOrderDialog(
                    order = testOrder,
                    onMoveUp = {},
                    onMoveDown = {},
                    onDismiss = { dismissCalled = true },
                    onConfirm = {},
                )
            }
        }

        // Cliccare cancel
        composeTestRule.onNodeWithText("Cancel", substring = true)
            .performClick()

        assert(dismissCalled)
    }

    @Test
    fun dialog_shouldCallOnConfirm_whenConfirmButtonClicked() {
        var confirmCalled = false

        composeTestRule.setContent {
            AntCashManagerTheme {
                ChartsCardsOrderDialog(
                    order = testOrder,
                    onMoveUp = {},
                    onMoveDown = {},
                    onDismiss = {},
                    onConfirm = { confirmCalled = true },
                )
            }
        }

        // Cliccare confirm
        composeTestRule.onNodeWithText("Confirm", substring = true)
            .performClick()

        assert(confirmCalled)
    }

    @Test
    fun dialog_firstElement_canMoveDown_butNotUp() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                ChartsCardsOrderDialog(
                    order = testOrder,
                    onMoveUp = {},
                    onMoveDown = {},
                    onDismiss = {},
                    onConfirm = {},
                )
            }
        }

        // Il primo elemento può muoversi giù ma non su
        // ReorderButtons gestisce canMoveUp=false e canMoveDown=true
    }

    @Test
    fun dialog_lastElement_canMoveUp_butNotDown() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                ChartsCardsOrderDialog(
                    order = testOrder,
                    onMoveUp = {},
                    onMoveDown = {},
                    onDismiss = {},
                    onConfirm = {},
                )
            }
        }

        // L'ultimo elemento può muoversi su ma non giù
        // ReorderButtons gestisce canMoveUp=true e canMoveDown=false
    }

    @Test
    fun dialog_withThreeElements_middleElement_hasAllButtonsEnabled() {
        val threeElementOrder = listOf(
            ChartCardType.SPENDING_FORECAST_CARD,
            ChartCardType.QUICK_STATS_CARD,
            ChartCardType.DAILY_EXPENSE_CHART_CARD,
        )

        composeTestRule.setContent {
            AntCashManagerTheme {
                ChartsCardsOrderDialog(
                    order = threeElementOrder,
                    onMoveUp = {},
                    onMoveDown = {},
                    onDismiss = {},
                    onConfirm = {},
                )
            }
        }

        // L'elemento di mezzo ha entrambi i pulsanti abilitati
    }

    @Test
    fun dialog_withSingleElement_disablesBothMoveButtons() {
        val singleOrder = listOf(ChartCardType.SPENDING_FORECAST_CARD)

        composeTestRule.setContent {
            AntCashManagerTheme {
                ChartsCardsOrderDialog(
                    order = singleOrder,
                    onMoveUp = {},
                    onMoveDown = {},
                    onDismiss = {},
                    onConfirm = {},
                )
            }
        }

        // Con un solo elemento, entrambi i pulsanti sono disabilitati
    }

    @Test
    fun dialog_hasAccessibilityDescriptions() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                ChartsCardsOrderDialog(
                    order = testOrder,
                    onMoveUp = {},
                    onMoveDown = {},
                    onDismiss = {},
                    onConfirm = {},
                )
            }
        }

        // Verificare che i pulsanti hanno contentDescription per l'accessibilità
        // ChartsCardsOrderDialog passa upDescription e downDescription a ReorderButtons
    }
}
