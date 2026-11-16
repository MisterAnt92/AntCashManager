package com.antcashmanager.android.ui.settingsDisplay

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.analytics.tracker.EngagementTracker
import com.antcashmanager.android.testutil.FakeSettingsRepository
import com.antcashmanager.android.ui.screen.settings.displaySettings.DisplayEvent
import com.antcashmanager.android.ui.screen.settings.displaySettings.DisplayViewModel
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.service.WidgetUpdateNotifier
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DisplayViewModelTest : BaseUnitTest() {
    private lateinit var fakeSettingsRepo: FakeSettingsRepository
    private lateinit var fakeWidgetUpdateNotifier: FakeWidgetUpdateNotifier
    private lateinit var analyticsManager: AnalyticsManager
    private lateinit var engagementTracker: EngagementTracker
    private lateinit var viewModel: DisplayViewModel

    @Before
    fun setup() {
        fakeSettingsRepo = FakeSettingsRepository()
        fakeWidgetUpdateNotifier = FakeWidgetUpdateNotifier()
        analyticsManager = mockk(relaxed = true)
        engagementTracker = mockk(relaxed = true)
        viewModel = DisplayViewModel(fakeSettingsRepo, fakeWidgetUpdateNotifier, analyticsManager, engagementTracker)
    }

    // --- Default state ---

    @Test
    fun state_shouldExposeDefaults_whenViewModelIsCreated() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    launch { viewModel.currencySymbol.collect { } }
                    launch { viewModel.decimalDigits.collect { } }
                    launch { viewModel.decimalSeparator.collect { } }
                    launch { viewModel.thousandsSeparator.collect { } }
                }
            advanceUntilIdle()

            assertEquals("€", viewModel.currencySymbol.value)
            assertEquals(2, viewModel.decimalDigits.value)
            assertEquals(",", viewModel.decimalSeparator.value)
            assertEquals("", viewModel.thousandsSeparator.value)

            collectJob.cancel()
        }

    @Test
    fun state_shouldExposeWidgetDefaults_whenViewModelIsCreated() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    launch { viewModel.widgetBackgroundColor.collect { } }
                    launch { viewModel.widgetOpacity.collect { } }
                }
            advanceUntilIdle()

            assertEquals(0xFFFFFFFFL, viewModel.widgetBackgroundColor.value)
            assertEquals(100, viewModel.widgetOpacity.value)

            collectJob.cancel()
        }

    // --- Currency & formatting ---

    @Test
    fun onSetCurrencySymbolEvent_shouldUpdateCurrencySymbol_whenValidSymbolProvided() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) { launch { viewModel.currencySymbol.collect { } } }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetCurrencySymbolEvent("$"))
            advanceUntilIdle()

            assertEquals("$", viewModel.currencySymbol.value)
            collectJob.cancel()
        }

    @Test
    fun onSetCurrencySymbolEvent_shouldKeepCurrentValue_whenSymbolExceedsMaxLength() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) { launch { viewModel.currencySymbol.collect { } } }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetCurrencySymbolEvent("INVALID"))
            advanceUntilIdle()

            assertEquals("€", viewModel.currencySymbol.value)
            collectJob.cancel()
        }

    @Test
    fun onSetDecimalDigitsEvent_shouldUpdateDecimalDigits_whenValidCountProvided() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) { launch { viewModel.decimalDigits.collect { } } }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetDecimalDigitsEvent(0))
            advanceUntilIdle()

            assertEquals(0, viewModel.decimalDigits.value)
            collectJob.cancel()
        }

    @Test
    fun onSetDecimalDigitsEvent_shouldClampToMaximum_whenValueExceedsMax() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) { launch { viewModel.decimalDigits.collect { } } }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetDecimalDigitsEvent(9))
            advanceUntilIdle()

            assertEquals(4, viewModel.decimalDigits.value)
            collectJob.cancel()
        }

    @Test
    fun onSetDecimalDigitsEvent_shouldClampToZero_whenValueIsNegative() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) { launch { viewModel.decimalDigits.collect { } } }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetDecimalDigitsEvent(-5))
            advanceUntilIdle()

            assertEquals(0, viewModel.decimalDigits.value)
            collectJob.cancel()
        }

    @Test
    fun onSetThousandsSeparatorEvent_shouldKeepDefault_whenSeparatorExceedsMaxLength() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) { launch { viewModel.thousandsSeparator.collect { } } }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetThousandsSeparatorEvent("INVALID"))
            advanceUntilIdle()

            assertEquals("", viewModel.thousandsSeparator.value)
            collectJob.cancel()
        }

    @Test
    fun onSetDecimalSeparatorEvent_shouldUpdateDecimalSeparator_whenValidValueProvided() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    launch { viewModel.decimalSeparator.collect { } }
                    launch { viewModel.thousandsSeparator.collect { } }
                }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetDecimalSeparatorEvent("."))
            viewModel.onEvent(DisplayEvent.SetThousandsSeparatorEvent(","))
            advanceUntilIdle()

            assertEquals(".", viewModel.decimalSeparator.value)
            assertEquals(",", viewModel.thousandsSeparator.value)
            collectJob.cancel()
        }

    @Test
    fun onResetPreferencesEvent_shouldRestoreAllDefaults_whenResetCalled() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    launch { viewModel.decimalSeparator.collect { } }
                    launch { viewModel.thousandsSeparator.collect { } }
                }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetDecimalSeparatorEvent("."))
            viewModel.onEvent(DisplayEvent.SetThousandsSeparatorEvent(","))
            advanceUntilIdle()

            viewModel.resetAllPreferences()
            advanceUntilIdle()

            assertEquals("€", viewModel.currencySymbol.value)
            assertEquals(2, viewModel.decimalDigits.value)
            assertEquals(",", viewModel.decimalSeparator.value)
            assertEquals("", viewModel.thousandsSeparator.value)
            collectJob.cancel()
        }

    @Test
    fun onSetThousandsSeparatorEvent_shouldRejectValue_whenEqualToDecimalSeparator() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    launch { viewModel.decimalSeparator.collect { } }
                    launch { viewModel.thousandsSeparator.collect { } }
                }
            advanceUntilIdle()

            // Thousands cannot equal decimal default ","
            viewModel.onEvent(DisplayEvent.SetThousandsSeparatorEvent(","))
            advanceUntilIdle()

            assertEquals(",", viewModel.decimalSeparator.value)
            assertEquals("", viewModel.thousandsSeparator.value)
            collectJob.cancel()
        }

    @Test
    fun onSetDecimalSeparatorEvent_shouldClearThousands_whenEqualToThousandsSeparator() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    launch { viewModel.decimalSeparator.collect { } }
                    launch { viewModel.thousandsSeparator.collect { } }
                }
            advanceUntilIdle()

            // Set up state: thousands = ".", decimal = ","
            viewModel.onEvent(DisplayEvent.SetThousandsSeparatorEvent("."))
            advanceUntilIdle()
            assertEquals(".", viewModel.thousandsSeparator.value)

            // Setting decimal to same value as thousands → thousands must be cleared
            viewModel.onEvent(DisplayEvent.SetDecimalSeparatorEvent("."))
            advanceUntilIdle()

            assertEquals(".", viewModel.decimalSeparator.value)
            assertEquals("", viewModel.thousandsSeparator.value)
            collectJob.cancel()
        }

    // --- Meal voucher ---

    @Test
    fun onSetMealVoucherValueEvent_shouldUpdateValue_whenValueIsPositive() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) { launch { viewModel.mealVoucherValue.collect { } } }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetMealVoucherValueEvent(7.5))
            advanceUntilIdle()

            assertEquals(7.5, viewModel.mealVoucherValue.value, 0.0)
            collectJob.cancel()
        }

    @Test
    fun onSetMealVoucherValueEvent_shouldClampToZero_whenValueIsNegative() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) { launch { viewModel.mealVoucherValue.collect { } } }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetMealVoucherValueEvent(-1.0))
            advanceUntilIdle()

            assertEquals(0.0, viewModel.mealVoucherValue.value, 0.0)
            collectJob.cancel()
        }

    // --- Date format ---

    @Test
    fun onSetDateFormatEvent_shouldUpdateDateFormat_whenValidFormatProvided() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) { launch { viewModel.dateFormat.collect { } } }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetDateFormatEvent("yyyy-MM-dd"))
            advanceUntilIdle()

            assertEquals("yyyy-MM-dd", viewModel.dateFormat.value)
            collectJob.cancel()
        }

    // --- Display switches ---

    @Test
    fun onSetShowChartsSectionEvent_shouldUpdateShowChartsSection_whenEventReceived() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) { launch { viewModel.showChartsSection.collect { } } }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetShowChartsSectionEvent(false))
            advanceUntilIdle()

            assertFalse(viewModel.showChartsSection.value)
            collectJob.cancel()
        }

    @Test
    fun onSetChartsZoomEnabledEvent_shouldUpdateChartsZoomEnabled_whenEventReceived() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) { launch { viewModel.chartsZoomEnabled.collect { } } }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetChartsZoomEnabledEvent(true))
            advanceUntilIdle()

            assertTrue(viewModel.chartsZoomEnabled.value)
            collectJob.cancel()
        }

    @Test
    fun onSetShowTransactionNotesEvent_shouldUpdateShowTransactionNotes_whenEventReceived() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) { launch { viewModel.showTransactionNotes.collect { } } }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetShowTransactionNotesEvent(false))
            advanceUntilIdle()

            assertFalse(viewModel.showTransactionNotes.value)
            collectJob.cancel()
        }

    @Test
    fun onSetShowPaymentTypeBreakdownEvent_shouldUpdateShowPaymentTypeBreakdown_whenEventReceived() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    launch { viewModel.showPaymentTypeBreakdown.collect { } }
                }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetShowPaymentTypeBreakdownEvent(true))
            advanceUntilIdle()

            assertTrue(viewModel.showPaymentTypeBreakdown.value)
            collectJob.cancel()
        }

    @Test
    fun onSetShowQuickInsightsCardEvent_shouldUpdateShowQuickInsightsCard_whenEventReceived() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    launch { viewModel.showQuickInsightsCard.collect { } }
                }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetShowQuickInsightsCardEvent(true))
            advanceUntilIdle()

            assertTrue(viewModel.showQuickInsightsCard.value)
            collectJob.cancel()
        }

    @Test
    fun onSetMaskAmountsEvent_shouldUpdateMaskAmounts_whenEventReceived() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) { launch { viewModel.maskAmounts.collect { } } }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetMaskAmountsEvent(true))
            advanceUntilIdle()

            assertTrue(viewModel.maskAmounts.value)
            collectJob.cancel()
        }

    // --- Transaction display types ---

    @Test
    fun onSetTransactionDisplayTypeEvent_shouldUpdateHomeDisplayType_whenTypeChanged() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    launch { viewModel.transactionDisplayType.collect { } }
                }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetTransactionDisplayTypeEvent(TransactionDisplayType.CATEGORY))
            advanceUntilIdle()

            assertEquals(TransactionDisplayType.CATEGORY, viewModel.transactionDisplayType.value)
            collectJob.cancel()
        }

    @Test
    fun onSetTransactionsTransactionDisplayTypeEvent_shouldUpdateTransactionsDisplayType_whenTypeChanged() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    launch { viewModel.transactionsTransactionDisplayType.collect { } }
                }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetTransactionsTransactionDisplayTypeEvent(TransactionDisplayType.CATEGORY))
            advanceUntilIdle()

            assertEquals(TransactionDisplayType.CATEGORY, viewModel.transactionsTransactionDisplayType.value)
            collectJob.cancel()
        }

    // --- Widget appearance ---

    @Test
    fun onSetWidgetBackgroundColorEvent_shouldUpdateColorAndNotify_whenColorChanged() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    launch { viewModel.widgetBackgroundColor.collect { } }
                }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetWidgetBackgroundColorEvent(0xFF212121L))
            advanceUntilIdle()

            assertEquals(0xFF212121L, viewModel.widgetBackgroundColor.value)
            assertEquals(1, fakeWidgetUpdateNotifier.notifyCount)
            collectJob.cancel()
        }

    @Test
    fun onSetWidgetOpacityEvent_shouldUpdateOpacityAndNotify_whenValueInRange() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) { launch { viewModel.widgetOpacity.collect { } } }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetWidgetOpacityEvent(40))
            advanceUntilIdle()

            assertEquals(40, viewModel.widgetOpacity.value)
            assertEquals(1, fakeWidgetUpdateNotifier.notifyCount)
            collectJob.cancel()
        }

    @Test
    fun onSetWidgetOpacityEvent_shouldClampToHundred_whenValueExceedsMax() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) { launch { viewModel.widgetOpacity.collect { } } }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetWidgetOpacityEvent(150))
            advanceUntilIdle()

            assertEquals(100, viewModel.widgetOpacity.value)
            collectJob.cancel()
        }

    @Test
    fun onSetWidgetOpacityEvent_shouldClampToZero_whenValueIsNegative() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) { launch { viewModel.widgetOpacity.collect { } } }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetWidgetOpacityEvent(-20))
            advanceUntilIdle()

            assertEquals(0, viewModel.widgetOpacity.value)
            collectJob.cancel()
        }
}

private class FakeWidgetUpdateNotifier : WidgetUpdateNotifier {
    var notifyCount = 0
        private set

    override suspend fun notifyTransactionsChanged() {
        notifyCount++
    }
}
