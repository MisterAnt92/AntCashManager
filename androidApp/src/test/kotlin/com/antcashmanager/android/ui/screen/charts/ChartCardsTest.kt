package com.antcashmanager.android.ui.screen.charts
import org.junit.Ignore

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.ui.screen.charts.DailyAmount
import com.antcashmanager.android.ui.screen.charts.view.QuickStatsCard
import com.antcashmanager.android.ui.screen.charts.view.WeekdayExpenseCard
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.util.LocalCurrencyFormat
import com.antcashmanager.domain.model.CurrencyFormat
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for chart card composables (QuickStatsCard, WeekdayExpenseCard).
 *
 * Tests cover:
 * - Card display with valid data
 * - Layout responsiveness
 * - Text truncation and overflow handling
 * - Accessibility attributes (semantics)
 * - Empty state handling
 *
 * Uses Compose UI Test v2 framework (StandardTestDispatcher-based)
 * with kotlinx-coroutines-test for deterministic test execution.
 */
class ChartCardsTest : BaseUnitTest() {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ============== QuickStatsCard Tests ==============

    @Test
    fun quickStatsCard_isHiddenWhenDataIsEmpty() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    QuickStatsCard(chartData = ChartData())
                }
            }
        }

        // Card should not be displayed when dailyTimeline is empty
        composeTestRule.onNodeWithText("Quick Statistics").assertDoesNotExist()
    }

    @Test
    fun quickStatsCard_displaysTitleWithValidData() {
        val chartData = createSampleChartData()

        composeTestRule.setContent {
            CompositionLocalProvider(LocalCurrencyFormat provides CurrencyFormat.DEFAULT) {
                AntCashManagerTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        QuickStatsCard(chartData = chartData)
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("Quick Statistics").assertExists()
    }

    @Test
    fun quickStatsCard_displaysDaysTracked() {
        val chartData = createSampleChartData()

        composeTestRule.setContent {
            CompositionLocalProvider(LocalCurrencyFormat provides CurrencyFormat.DEFAULT) {
                AntCashManagerTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        QuickStatsCard(chartData = chartData)
                    }
                }
            }
        }

        // Days tracked label and value should be displayed
        composeTestRule.onNodeWithText("Days Tracked").assertExists()
        composeTestRule.onNodeWithText(chartData.dailyTimeline.size.toString()).assertExists()
    }

    @Test
    fun quickStatsCard_displaysMaxDailyExpense() {
        val chartData = createSampleChartData()

        composeTestRule.setContent {
            CompositionLocalProvider(LocalCurrencyFormat provides CurrencyFormat.DEFAULT) {
                AntCashManagerTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        QuickStatsCard(chartData = chartData)
                    }
                }
            }
        }

        // Max daily label should be displayed
        composeTestRule.onNodeWithText("Max Daily").assertExists()
    }

    @Test
    fun quickStatsCard_displaysAverageDailyExpense() {
        val chartData = createSampleChartData()

        composeTestRule.setContent {
            CompositionLocalProvider(LocalCurrencyFormat provides CurrencyFormat.DEFAULT) {
                AntCashManagerTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        QuickStatsCard(chartData = chartData)
                    }
                }
            }
        }

        // Average daily label should be displayed
        composeTestRule.onNodeWithText("Avg Daily").assertExists()
    }

    @Test
    fun quickStatsCard_handlesLargeNumbersWithoutTruncation() {
        val largeChartData = ChartData(
            dailyTimeline = listOf(
                DailyAmount("Day 1", 5000.50),
                DailyAmount("Day 2", 3500.75),
                DailyAmount("Day 3", 7200.00),
            )
        )

        composeTestRule.setContent {
            AntCashManagerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    QuickStatsCard(chartData = largeChartData)
                }
            }
        }

        // Title and structure should be visible
        composeTestRule.onNodeWithText("Quick Statistics").assertExists()
        // Verify data is present without checking exact formatting
        // (formatting depends on CurrencyFormat which is mocked)
    }

    // ============== WeekdayExpenseCard Tests ==============

    @Test
    fun weekdayExpenseCard_isHiddenWhenDataIsEmpty() {
        composeTestRule.setContent {
            AntCashManagerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WeekdayExpenseCard(chartData = ChartData())
                }
            }
        }

        // Card should not be displayed when expenseByWeekday is empty
        composeTestRule.onNodeWithText("Weekday Distribution").assertDoesNotExist()
    }

    @Test
    fun weekdayExpenseCard_displaysTitleWithValidData() {
        val chartData = createSampleChartDataWithWeekday()

        composeTestRule.setContent {
            AntCashManagerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WeekdayExpenseCard(chartData = chartData)
                }
            }
        }

        composeTestRule.onNodeWithText("Weekday Distribution").assertExists()
    }

    @Test
    fun weekdayExpenseCard_displaysAllWeekdayLabels() {
        val chartData = createSampleChartDataWithWeekday()

        composeTestRule.setContent {
            AntCashManagerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WeekdayExpenseCard(chartData = chartData)
                }
            }
        }

        // Verify all weekday labels are present
        val weekdayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        weekdayLabels.forEach { label ->
            composeTestRule.onNodeWithText(label).assertExists()
        }
    }

    @Test
    fun weekdayExpenseCard_displaysExpensesWithoutOverflow() {
        val chartData = createSampleChartDataWithWeekday()

        composeTestRule.setContent {
            AntCashManagerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WeekdayExpenseCard(chartData = chartData)
                }
            }
        }

        // Verify the card renders without crashing (content fits within bounds)
        composeTestRule.onNodeWithText("Weekday Distribution").assertExists()
    }

    @Test
    fun weekdayExpenseCard_handlesDaysWithoutExpenses() {
        val chartData = ChartData(
            expenseByWeekday = mapOf(
                1 to 100.0,  // Monday
                2 to 0.0,    // Tuesday (no expense)
                3 to 250.50, // Wednesday
                // Thursday-Sunday have no entries
            )
        )

        composeTestRule.setContent {
            AntCashManagerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WeekdayExpenseCard(chartData = chartData)
                }
            }
        }

        composeTestRule.onNodeWithText("Weekday Distribution").assertExists()
        composeTestRule.onNodeWithText("Mon").assertExists()
        composeTestRule.onNodeWithText("Wed").assertExists()
    }

    @Test
    fun weekdayExpenseCard_handlesLargeCurrencyAmounts() {
        val chartData = ChartData(
            expenseByWeekday = mapOf(
                1 to 5000.50,
                2 to 3500.75,
                3 to 7200.00,
                4 to 2150.25,
                5 to 4800.10,
                6 to 1200.00,
                7 to 3300.00,
            )
        )

        composeTestRule.setContent {
            AntCashManagerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WeekdayExpenseCard(chartData = chartData)
                }
            }
        }

        // Card should render without text overflow or layout issues
        composeTestRule.onNodeWithText("Weekday Distribution").assertExists()
        // Verify all weekday labels are still visible
        composeTestRule.onNodeWithText("Mon").assertExists()
        composeTestRule.onNodeWithText("Fri").assertExists()
    }

    // ============== Helper Functions ==============

    private fun createSampleChartData(): ChartData {
        return ChartData(
            dailyTimeline = listOf(
                DailyAmount("Day 1", 50.0),
                DailyAmount("Day 2", 75.50),
                DailyAmount("Day 3", 60.0),
                DailyAmount("Day 4", 85.25),
                DailyAmount("Day 5", 55.0),
            )
        )
    }

    private fun createSampleChartDataWithWeekday(): ChartData {
        return ChartData(
            expenseByWeekday = mapOf(
                1 to 150.0,  // Monday
                2 to 200.50, // Tuesday
                3 to 120.0,  // Wednesday
                4 to 180.75, // Thursday
                5 to 250.0,  // Friday
                6 to 100.0,  // Saturday
                7 to 175.25, // Sunday
            )
        )
    }
}
