package com.antcashmanager.android.ui.charts

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.analytics.tracker.PerformanceTracker
import com.antcashmanager.android.analytics.tracker.SegmentationTracker
import com.antcashmanager.android.testutil.FakeSettingsRepository
import com.antcashmanager.android.testutil.FakeTransactionRepository
import com.antcashmanager.android.ui.screen.charts.ChartEvent
import com.antcashmanager.android.ui.screen.charts.ChartsViewModel
import com.antcashmanager.android.ui.screen.charts.RangePreset
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChartsViewModelTest : BaseUnitTest() {
    private lateinit var fakeRepo: FakeTransactionRepository
    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var performanceTracker: PerformanceTracker
    private lateinit var segmentationTracker: SegmentationTracker
    private lateinit var viewModel: ChartsViewModel

    @Before
    fun setup() {
        fakeRepo = FakeTransactionRepository()
        fakeSettingsRepository = FakeSettingsRepository()
        performanceTracker = mockk(relaxed = true)
        segmentationTracker = mockk(relaxed = true)
        viewModel = ChartsViewModel(
            transactionRepository = fakeRepo,
            settingsRepository = fakeSettingsRepository,
            dispatcher = testDispatcher,
            performanceTracker = performanceTracker,
            segmentationTracker = segmentationTracker,
        )
    }

    @Test
    fun initialChartDataIsEmpty() = runViewModelTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.chartData.collect {}
        }
        advanceUntilIdle()
        assertEquals(0.0, viewModel.chartData.value.totalIncome, 0.01)
        assertEquals(0.0, viewModel.chartData.value.totalExpense, 0.01)
        collectJob.cancel()
    }

    @Test
    fun chartDataComputesTotalsCorrectly() = runViewModelTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.chartData.collect {}
        }
        val now = System.currentTimeMillis()
        fakeRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Salary",
                amount = 2000.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = now
            ),
            Transaction(
                id = 2,
                title = "Food",
                amount = 150.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = now
            ),
            Transaction(
                id = 3,
                title = "Transport",
                amount = 50.0,
                category = "Transport",
                type = TransactionType.EXPENSE,
                timestamp = now
            ),
        )
        // Set date range to cover now
        viewModel.onEvent(ChartEvent.SetDateRange(now - 86400000, now + 86400000))
        advanceUntilIdle()
        assertEquals(2000.0, viewModel.chartData.value.totalIncome, 0.01)
        assertEquals(200.0, viewModel.chartData.value.totalExpense, 0.01)
        assertEquals(2, viewModel.chartData.value.expenseByCategory.size)
        collectJob.cancel()
    }

    @Test
    fun setPresetRangeUpdatesDateRange_whenMultiYearPresetsAreSelected() = runViewModelTest {
        val multiYearPresets = listOf(
            RangePreset.TWO_YEARS,
            RangePreset.THREE_YEARS,
            RangePreset.FIVE_YEARS,
            RangePreset.SIX_YEARS,
        )
        var previousFrom = viewModel.dateRange.value.from

        // Ogni preset viene verificato su una ViewModel dedicata: setPresetRange persiste in modo
        // fire-and-forget, quindi selezioni multiple ravvicinate sulla stessa istanza potrebbero
        // risolversi fuori ordine e non sono rappresentative del calcolo del range in sé.
        multiYearPresets.forEach { preset ->
            val presetViewModel = ChartsViewModel(
                transactionRepository = fakeRepo,
                settingsRepository = FakeSettingsRepository(),
                dispatcher = testDispatcher,
                performanceTracker = mockk(relaxed = true),
                segmentationTracker = mockk(relaxed = true),
            )
            presetViewModel.onEvent(ChartEvent.SetPresetRange(preset))
            advanceUntilIdle()

            val range = presetViewModel.dateRange.value
            assertEquals(preset.ordinal, presetViewModel.selectedPresetIndex.value)
            // Ogni preset più lungo deve produrre un "from" più indietro nel tempo del precedente.
            assertTrue(range.from <= previousFrom)
            previousFrom = range.from
        }
    }
}
