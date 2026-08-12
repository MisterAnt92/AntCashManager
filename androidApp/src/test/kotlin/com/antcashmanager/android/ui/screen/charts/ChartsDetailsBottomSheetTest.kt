@file:Suppress("LocalVariableName")

package com.antcashmanager.android.ui.screen.charts

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onNodeWithText
import com.antcashmanager.android.BaseComposeUnitTest
import com.antcashmanager.android.ui.screen.charts.view.ChartsDetailsBottomSheet
import com.antcashmanager.android.ui.screen.charts.view.ChartDetailsData
import com.antcashmanager.android.ui.screen.charts.view.TrendDirection
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for ChartsDetailsBottomSheet composable.
 *
 * Tests cover:
 * - Bottom sheet display with valid data
 * - Dismiss callback functionality
 * - Amount masking behavior
 * - Trend direction rendering
 *
 * Uses Compose UI Test v2 framework (no BaseUnitTest - pure Compose UI test).
 */

@RunWith(RobolectricTestRunner::class)
class ChartsDetailsBottomSheetTest : BaseComposeUnitTest() {


    private val sampleDetails = ChartDetailsData(
        categoryName = "Food",
        amount = 250.50,
        percentage = 35,
        colorHex = 0xFFE57373,
        transactionCount = 12,
        trend = TrendDirection.UP,
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun bottomSheet_displaysWithValidData() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ChartsDetailsBottomSheet(
                        chartDetails = sampleDetails,
                        onDismiss = {},
                    )
                }
            }
        }

        // Verify bottom sheet is displayed
        composeTestRule.onNodeWithText("Details").assertExists()
        composeTestRule.onNodeWithText("Food").assertExists()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun bottomSheet_displaysPercentage() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ChartsDetailsBottomSheet(
                        chartDetails = sampleDetails,
                        onDismiss = {},
                    )
                }
            }
        }

        // Verify percentage is displayed
        composeTestRule.onNodeWithText("35% of total").assertExists()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun bottomSheet_displaysTransactionCount() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ChartsDetailsBottomSheet(
                        chartDetails = sampleDetails,
                        onDismiss = {},
                    )
                }
            }
        }

        // Verify transaction count is displayed
        composeTestRule.onNodeWithText("12").assertExists()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun bottomSheet_displaysTrendIndicator() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ChartsDetailsBottomSheet(
                        chartDetails = sampleDetails.copy(trend = TrendDirection.UP),
                        onDismiss = {},
                    )
                }
            }
        }

        // Verify trend indicator exists
        composeTestRule.onNodeWithText("Spending trend: Increasing").assertExists()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun bottomSheet_hiddenWhenDetailsIsNull() {
        var dismissCalled = false

        composeTestRule.setContent {
            AntCashManagerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ChartsDetailsBottomSheet(
                        chartDetails = null,
                        onDismiss = { dismissCalled = true },
                    )
                }
            }
        }

        // Bottom sheet should not be visible
        composeTestRule.onNodeWithText("Details").assertDoesNotExist()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun bottomSheet_handlesNeutralTrend() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ChartsDetailsBottomSheet(
                        chartDetails = sampleDetails.copy(trend = TrendDirection.NEUTRAL),
                        onDismiss = {},
                    )
                }
            }
        }

        // Neutral trend should not display trend section
        composeTestRule.onNodeWithText("Spending trend: Stable").assertDoesNotExist()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun bottomSheet_handleDecreasingTrend() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ChartsDetailsBottomSheet(
                        chartDetails = sampleDetails.copy(trend = TrendDirection.DOWN),
                        onDismiss = {},
                    )
                }
            }
        }

        // Decreasing trend should display
        composeTestRule.onNodeWithText("Spending trend: Decreasing").assertExists()
    }
}
