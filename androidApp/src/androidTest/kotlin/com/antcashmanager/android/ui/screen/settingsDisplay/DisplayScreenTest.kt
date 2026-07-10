package com.antcashmanager.android.ui.screen.settingsDisplay

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.domain.model.TransactionDisplayType
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DisplayScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun firstCardVisible_and_dialogOpens_onClick() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val cardTitle = ctx.getString(R.string.settings_currency_symbol)
        val dialogTitle = ctx.getString(R.string.dialog_choose_currency)

        composeTestRule.setContent {
            AntCashManagerTheme {
                DisplayContent(
                    currencySymbol = "\u20ac",
                    onCurrencySymbolSelected = {},
                    decimalDigits = 2,
                    onDecimalDigitsSelected = {},
                    decimalSeparator = ",",
                    onDecimalSeparatorSelected = {},
                    thousandsSeparator = "",
                    onThousandsSeparatorSelected = {},
                    mealVoucherValue = 5.29,
                    onMealVoucherValueSelected = {},
                    showTransactionNotes = true,
                    onShowTransactionNotesChanged = {},
                    showChartsSection = true,
                    onShowChartsSectionChanged = {},
                    chartsZoomEnabled = true,
                    onChartsZoomEnabledChanged = {},
                    dateFormat = "dd/MM/yyyy",
                    onDateFormatSelected = {},
                    showPaymentTypeBreakdown = true,
                    onShowPaymentTypeBreakdownChanged = {},
                    transactionDisplayType = TransactionDisplayType.TREND,
                    onTransactionDisplayTypeSelected = {},
                    transactionsTransactionDisplayType = TransactionDisplayType.TREND,
                    onTransactionsTransactionDisplayTypeSelected = {},
                    onResetAllPreferences = {},
                    onNavigateBack = {},
                    showQuickInsightsCard = false,
                    onShowQuickInsightsCardChanged = {},
                )
            }
        }

        // Verify first card title exists and is visible
        composeTestRule.onNodeWithText(cardTitle).assertIsDisplayed()

        // Click it and verify dialog opens
        composeTestRule.onNodeWithText(cardTitle).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(dialogTitle).assertIsDisplayed()
    }
}

