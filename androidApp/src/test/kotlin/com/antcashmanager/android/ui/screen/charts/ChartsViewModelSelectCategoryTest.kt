package com.antcashmanager.android.ui.screen.charts

import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.analytics.tracker.PerformanceTracker
import com.antcashmanager.android.analytics.tracker.SegmentationTracker
import com.antcashmanager.android.testutil.FakeSettingsRepository
import com.antcashmanager.android.testutil.FakeTransactionRepository
import com.antcashmanager.android.ui.screen.charts.view.TrendDirection
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChartsViewModelSelectCategoryTest : BaseUnitTest() {
    private lateinit var fakeTransactionRepo: FakeTransactionRepository
    private lateinit var fakeSettingsRepo: FakeSettingsRepository
    private lateinit var performanceTracker: PerformanceTracker
    private lateinit var segmentationTracker: SegmentationTracker
    private lateinit var viewModel: ChartsViewModel

    @Before
    fun setup() {
        fakeTransactionRepo = FakeTransactionRepository()
        fakeSettingsRepo = FakeSettingsRepository()
        performanceTracker = mockk(relaxed = true)
        segmentationTracker = mockk(relaxed = true)
        viewModel =
            ChartsViewModel(
                fakeTransactionRepo,
                fakeSettingsRepo,
                dispatcher = testDispatcher,
                performanceTracker = performanceTracker,
                segmentationTracker = segmentationTracker,
            )
    }

    @After
    override fun tearDownDispatchers() {
        if (::viewModel.isInitialized) {
            runBlocking { viewModel.viewModelScope.coroutineContext[Job]?.cancelAndJoin() }
            testDispatcher.scheduler.advanceUntilIdle()
        }
        super.tearDownDispatchers()
    }

    private fun includeAllTransactions() {
        fakeSettingsRepo.chartsDateFilterState.value =
            SavedDateFilter(
                presetIndex = SavedDateFilter.CUSTOM_PRESET_INDEX,
                from = 0L,
                to = Long.MAX_VALUE,
            )
    }

    @Test
    fun onSelectChartCategoryEvent_shouldUpdateDetails_whenCategoryExists() =
        runViewModelTest {
            val collectJob =
                launchInBackground {
                    viewModel.selectedChartDetails.collect { }
                }

            viewModel.onEvent(
                ChartEvent.SelectChartCategoryEvent(
                    categoryName = "Food",
                    amount = 250.0,
                    colorHex = 0xFFE57373,
                    isExpense = true,
                ),
            )
            advanceUntilIdle()

            val details = viewModel.selectedChartDetails.value
            assertNotNull(details)
            assertEquals("Food", details?.categoryName)
            assertEquals(250.0, details?.amount ?: 0.0, 0.01)

            collectJob.cancel()
        }

    @Test
    fun onSelectChartCategoryEvent_shouldCalculatePercentage_whenTotalAmountIsKnown() =
        runViewModelTest {
            val timestamp = 1_704_106_800_000L // 2024-01-01 12:00 UTC (fixed)

            fakeTransactionRepo.transactions.value =
                listOf(
                    Transaction(
                        id = 1,
                        title = "Food",
                        amount = -100.0,
                        timestamp = timestamp,
                        category = "Food",
                        type = TransactionType.EXPENSE,
                        paymentType = PaymentType.CASH,
                    ),
                    Transaction(
                        id = 2,
                        title = "Transport",
                        amount = -150.0,
                        timestamp = timestamp,
                        category = "Transport",
                        type = TransactionType.EXPENSE,
                        paymentType = PaymentType.CASH,
                    ),
                )

            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    launch { viewModel.chartData.collect { } }
                    launch { viewModel.selectedChartDetails.collect { } }
                }
            advanceUntilIdle()

            viewModel.onEvent(ChartEvent.SetDateRangeEvent(timestamp - 86_400_000L, timestamp + 86_400_000L))
            advanceUntilIdle()

            // Food = 100 out of 250 total = 40%
            viewModel.onEvent(
                ChartEvent.SelectChartCategoryEvent(
                    categoryName = "Food",
                    amount = 100.0,
                    colorHex = 0xFFE57373,
                    isExpense = true,
                ),
            )
            advanceUntilIdle()

            val details = viewModel.selectedChartDetails.value
            assertEquals(40, details?.percentage)

            collectJob.cancel()
        }

    @Test
    fun onSelectChartCategoryEvent_shouldReturnZeroPercent_whenTotalIsZero() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    launch { viewModel.chartData.collect { } }
                    launch { viewModel.selectedChartDetails.collect { } }
                }
            advanceUntilIdle()

            viewModel.onEvent(
                ChartEvent.SelectChartCategoryEvent(
                    categoryName = "Empty",
                    amount = 100.0,
                    colorHex = 0xFFE57373,
                    isExpense = true,
                ),
            )
            advanceUntilIdle()

            val details = viewModel.selectedChartDetails.value
            assertEquals(0, details?.percentage)

            collectJob.cancel()
        }

    @Test
    fun onSelectChartCategoryEvent_shouldStoreColorHex_whenCategoryHasColor() =
        runViewModelTest {
            val collectJob =
                launchInBackground {
                    viewModel.selectedChartDetails.collect { }
                }

            val colorHex = 0xFFE57373L
            viewModel.onEvent(
                ChartEvent.SelectChartCategoryEvent(
                    categoryName = "Food",
                    amount = 250.0,
                    colorHex = colorHex,
                    isExpense = true,
                ),
            )
            advanceUntilIdle()

            val details = viewModel.selectedChartDetails.value
            assertEquals(colorHex, details?.colorHex)

            collectJob.cancel()
        }

    @Test
    fun onClearChartSelectionEvent_shouldClearDetails_whenSelectionExists() =
        runViewModelTest {
            val collectJob =
                launchInBackground {
                    viewModel.selectedChartDetails.collect { }
                }

            viewModel.onEvent(
                ChartEvent.SelectChartCategoryEvent(
                    categoryName = "Food",
                    amount = 250.0,
                    colorHex = 0xFFE57373,
                    isExpense = true,
                ),
            )
            advanceUntilIdle()
            assertNotNull(viewModel.selectedChartDetails.value)

            viewModel.onEvent(ChartEvent.ClearChartSelectionEvent)
            advanceUntilIdle()

            assertNull(viewModel.selectedChartDetails.value)

            collectJob.cancel()
        }

    @Test
    fun onSelectChartCategoryEvent_shouldDefaultTrendToNeutral_whenTrendNotComputed() =
        runViewModelTest {
            val collectJob =
                launchInBackground {
                    viewModel.selectedChartDetails.collect { }
                }

            viewModel.onEvent(
                ChartEvent.SelectChartCategoryEvent(
                    categoryName = "Food",
                    amount = 250.0,
                    colorHex = 0xFFE57373,
                    isExpense = true,
                ),
            )
            advanceUntilIdle()

            val details = viewModel.selectedChartDetails.value
            assertEquals(TrendDirection.NEUTRAL, details?.trend)

            collectJob.cancel()
        }
}
