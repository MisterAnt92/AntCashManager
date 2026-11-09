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

    @Test
    fun defaultsAreExposedCorrectly() =
        runViewModelTest {
            val collectJob =
                launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) {
                    // subscribe to flows to ensure stateIn is active
                    launch { viewModel.currencySymbol.collect { } }
                    launch { viewModel.decimalDigits.collect { } }
                    launch { viewModel.decimalSeparator.collect { } }
                    launch { viewModel.thousandsSeparator.collect { } }
                }

            advanceUntilIdle()

            assertEquals("\u20ac", viewModel.currencySymbol.value)
            assertEquals(2, viewModel.decimalDigits.value)
            assertEquals(",", viewModel.decimalSeparator.value)
            assertEquals("", viewModel.thousandsSeparator.value)

            collectJob.cancel()
        }

    @Test
    fun setCurrencySymbolUpdatesValue() =
        runViewModelTest {
            val collectJob =
                launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) { launch { viewModel.currencySymbol.collect { } } }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetCurrencySymbolEvent("$"))
            advanceUntilIdle()

            assertEquals("$", viewModel.currencySymbol.value)
            collectJob.cancel()
        }

    @Test
    fun setDecimalDigitsUpdatesValue() =
        runViewModelTest {
            val collectJob =
                launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) { launch { viewModel.decimalDigits.collect { } } }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetDecimalDigitsEvent(0))
            advanceUntilIdle()

            assertEquals(0, viewModel.decimalDigits.value)
            collectJob.cancel()
        }

    @Test
    fun setSeparatorsUpdateValuesAndResetRestoresDefaults() =
        runViewModelTest {
            val collectJob =
                launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) {
                    launch { viewModel.decimalSeparator.collect { } }
                    launch { viewModel.thousandsSeparator.collect { } }
                }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetDecimalSeparatorEvent("."))
            viewModel.onEvent(DisplayEvent.SetThousandsSeparatorEvent(","))
            advanceUntilIdle()

            assertEquals(".", viewModel.decimalSeparator.value)
            assertEquals(",", viewModel.thousandsSeparator.value)

            viewModel.resetAllPreferences()
            advanceUntilIdle()

            // reset should restore to the repository defaults used in Fake
            assertEquals("\u20ac", viewModel.currencySymbol.value)
            assertEquals(2, viewModel.decimalDigits.value)
            assertEquals(",", viewModel.decimalSeparator.value)
            assertEquals("", viewModel.thousandsSeparator.value)

            collectJob.cancel()
        }

    @Test
    fun currencyInputsAreSanitized() =
        runViewModelTest {
            val collectJob =
                launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) {
                    launch { viewModel.currencySymbol.collect { } }
                    launch { viewModel.decimalDigits.collect { } }
                    launch { viewModel.decimalSeparator.collect { } }
                    launch { viewModel.thousandsSeparator.collect { } }
                }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetCurrencySymbolEvent("INVALID"))
            viewModel.onEvent(DisplayEvent.SetDecimalDigitsEvent(9))
            viewModel.onEvent(DisplayEvent.SetThousandsSeparatorEvent("INVALID"))
            advanceUntilIdle()

            assertEquals("\u20ac", viewModel.currencySymbol.value)
            assertEquals(4, viewModel.decimalDigits.value)
            assertEquals("", viewModel.thousandsSeparator.value)

            viewModel.onEvent(DisplayEvent.SetDecimalDigitsEvent(-5))
            advanceUntilIdle()
            assertEquals(0, viewModel.decimalDigits.value)

            collectJob.cancel()
        }

    @Test
    fun decimalAndThousandsSeparatorsCannotBeEqual() =
        runViewModelTest {
            val collectJob =
                launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) {
                    launch { viewModel.decimalSeparator.collect { } }
                    launch { viewModel.thousandsSeparator.collect { } }
                }
            advanceUntilIdle()

            // 1. Setting thousands same as current decimal -> should result in default (empty)
            viewModel.onEvent(DisplayEvent.SetThousandsSeparatorEvent(","))
            advanceUntilIdle()
            assertEquals(",", viewModel.decimalSeparator.value)
            assertEquals("", viewModel.thousandsSeparator.value)

            // 2. Setting thousands different -> should work
            viewModel.onEvent(DisplayEvent.SetThousandsSeparatorEvent("."))
            advanceUntilIdle()
            assertEquals(".", viewModel.thousandsSeparator.value)

            // 3. Setting decimal same as current thousands -> should reset thousands to default (empty)
            viewModel.onEvent(DisplayEvent.SetDecimalSeparatorEvent("."))
            advanceUntilIdle()

            assertEquals(".", viewModel.decimalSeparator.value)
            assertEquals("", viewModel.thousandsSeparator.value)

            collectJob.cancel()
        }

    @Test
    fun setMealVoucherValueUpdatesValueAndHandlesNegative() =
        runViewModelTest {
            val collectJob =
                launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) { launch { viewModel.mealVoucherValue.collect { } } }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetMealVoucherValueEvent(7.5))
            advanceUntilIdle()
            assertEquals(7.5, viewModel.mealVoucherValue.value, 0.0)

            viewModel.onEvent(DisplayEvent.SetMealVoucherValueEvent(-1.0))
            advanceUntilIdle()
            assertEquals(0.0, viewModel.mealVoucherValue.value, 0.0)

            collectJob.cancel()
        }

    @Test
    fun setDateFormatUpdatesValue() =
        runViewModelTest {
            val collectJob =
                launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) { launch { viewModel.dateFormat.collect { } } }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetDateFormatEvent("yyyy-MM-dd"))
            advanceUntilIdle()
            assertEquals("yyyy-MM-dd", viewModel.dateFormat.value)

            collectJob.cancel()
        }

    @Test
    fun displaySwitchesUpdateValues() =
        runViewModelTest {
            val collectJob =
                launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) {
                    launch { viewModel.showChartsSection.collect { } }
                    launch { viewModel.chartsZoomEnabled.collect { } }
                    launch { viewModel.showTransactionNotes.collect { } }
                    launch { viewModel.showPaymentTypeBreakdown.collect { } }
                    launch { viewModel.showQuickInsightsCard.collect { } }
                    launch { viewModel.maskAmounts.collect { } }
                }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetShowChartsSectionEvent(false))
            viewModel.onEvent(DisplayEvent.SetChartsZoomEnabledEvent(true))
            viewModel.onEvent(DisplayEvent.SetShowTransactionNotesEvent(false))
            viewModel.onEvent(DisplayEvent.SetShowPaymentTypeBreakdownEvent(true))
            viewModel.onEvent(DisplayEvent.SetShowQuickInsightsCardEvent(true))
            viewModel.onEvent(DisplayEvent.SetMaskAmountsEvent(true))
            advanceUntilIdle()

            assertFalse(viewModel.showChartsSection.value)
            assertTrue(viewModel.chartsZoomEnabled.value)
            assertFalse(viewModel.showTransactionNotes.value)
            assertTrue(viewModel.showPaymentTypeBreakdown.value)
            assertTrue(viewModel.showQuickInsightsCard.value)
            assertTrue(viewModel.maskAmounts.value)

            collectJob.cancel()
        }

    @Test
    fun transactionDisplayTypesUpdateValues() =
        runViewModelTest {
            val collectJob =
                launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) {
                    launch { viewModel.transactionDisplayType.collect { } }
                    launch { viewModel.transactionsTransactionDisplayType.collect { } }
                }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetTransactionDisplayTypeEvent(TransactionDisplayType.CATEGORY))
            viewModel.onEvent(DisplayEvent.SetTransactionsTransactionDisplayTypeEvent(TransactionDisplayType.CATEGORY))
            advanceUntilIdle()

            assertEquals(TransactionDisplayType.CATEGORY, viewModel.transactionDisplayType.value)
            assertEquals(
                TransactionDisplayType.CATEGORY,
                viewModel.transactionsTransactionDisplayType.value,
            )

            collectJob.cancel()
        }

    @Test
    fun widgetAppearanceDefaultsAreExposedCorrectly() =
        runViewModelTest {
            val collectJob =
                launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) {
                    launch { viewModel.widgetBackgroundColor.collect { } }
                    launch { viewModel.widgetOpacity.collect { } }
                }
            advanceUntilIdle()

            assertEquals(0xFFFFFFFFL, viewModel.widgetBackgroundColor.value)
            assertEquals(100, viewModel.widgetOpacity.value)

            collectJob.cancel()
        }

    @Test
    fun setWidgetBackgroundColorUpdatesValueAndNotifiesWidgets() =
        runViewModelTest {
            val collectJob =
                launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) { launch { viewModel.widgetBackgroundColor.collect { } } }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetWidgetBackgroundColorEvent(0xFF212121L))
            advanceUntilIdle()

            assertEquals(0xFF212121L, viewModel.widgetBackgroundColor.value)
            assertEquals(1, fakeWidgetUpdateNotifier.notifyCount)

            collectJob.cancel()
        }

    @Test
    fun setWidgetOpacityUpdatesValueAndNotifiesWidgets() =
        runViewModelTest {
            val collectJob =
                launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) { launch { viewModel.widgetOpacity.collect { } } }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetWidgetOpacityEvent(40))
            advanceUntilIdle()

            assertEquals(40, viewModel.widgetOpacity.value)
            assertEquals(1, fakeWidgetUpdateNotifier.notifyCount)

            collectJob.cancel()
        }

    @Test
    fun setWidgetOpacityCoercesOutOfRangeValues() =
        runViewModelTest {
            val collectJob =
                launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) { launch { viewModel.widgetOpacity.collect { } } }
            advanceUntilIdle()

            viewModel.onEvent(DisplayEvent.SetWidgetOpacityEvent(150))
            advanceUntilIdle()
            assertEquals(100, viewModel.widgetOpacity.value)

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
