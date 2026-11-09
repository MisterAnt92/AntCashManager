@file:Suppress("LocalVariableName")

package com.antcashmanager.android.ui.screen.charts

import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.analytics.tracker.PerformanceTracker
import com.antcashmanager.android.analytics.tracker.SegmentationTracker
import com.antcashmanager.android.testutil.FakeSettingsRepository
import com.antcashmanager.android.testutil.FakeTransactionRepository
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.TransactionType
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive edge case tests for ChartsViewModel.
 * Additional coverage for:
 * - Empty transaction lists
 * - Date range filtering
 * - Preset range selection
 * - Category aggregation
 * - Large datasets
 * - Chart details selection
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChartsViewModelComprehensiveTest : BaseUnitTest() {
    private lateinit var fakeRepo: FakeTransactionRepository
    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var performanceTracker: PerformanceTracker
    private lateinit var segmentationTracker: SegmentationTracker
    private lateinit var viewModel: ChartsViewModel

    private fun includeAllTransactions() {
        fakeSettingsRepository.chartsDateFilterState.value =
            SavedDateFilter(
                presetIndex = SavedDateFilter.CUSTOM_PRESET_INDEX,
                from = 0L,
                to = Long.MAX_VALUE,
            )
    }

    @Before
    fun setup() {
        fakeRepo = FakeTransactionRepository()
        fakeSettingsRepository = FakeSettingsRepository()
        performanceTracker = mockk(relaxed = true)
        segmentationTracker = mockk(relaxed = true)
        viewModel =
            ChartsViewModel(
                transactionRepository = fakeRepo,
                settingsRepository = fakeSettingsRepository,
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

    @Test
    fun chartData_shouldBeEmpty_whenRepositoryIsEmpty() =
        runViewModelTest {
            val collectJob =
                launch {
                    viewModel.chartData.collect {}
                }
            advanceUntilIdle()

            val chartData = viewModel.chartData.value
            assertTrue(chartData.incomeByCategory.isEmpty())
            assertTrue(chartData.expenseByCategory.isEmpty())
            assertEquals(0.0, chartData.totalIncome, 0.0)
            assertEquals(0.0, chartData.totalExpense, 0.0)
            collectJob.cancel()
        }

    @Test
    fun chartData_shouldDisplayAllTransactions_whenLoaded() =
        runViewModelTest {
            val t0 = 1_700_000_000_000L
            fakeRepo.transactions.value =
                listOf(
                    Transaction(
                        id = 1,
                        title = "Salary",
                        amount = 5000.0,
                        category = "Salary",
                        type = TransactionType.INCOME,
                        timestamp = t0,
                    ),
                    Transaction(
                        id = 2,
                        title = "Groceries",
                        amount = -100.0,
                        category = "Food",
                        type = TransactionType.EXPENSE,
                        timestamp = t0 - 1000,
                    ),
                    Transaction(
                        id = 3,
                        title = "Rent",
                        amount = -1500.0,
                        category = "Housing",
                        type = TransactionType.EXPENSE,
                        timestamp = t0 - 2000,
                    ),
                )

            includeAllTransactions()
            val collectJob =
                launch {
                    viewModel.chartData.collect {}
                }
            advanceUntilIdle()

            val chartData = viewModel.chartData.value
            assertEquals(5000.0, chartData.totalIncome, 0.01)
            assertEquals(1600.0, chartData.totalExpense, 0.01)
            collectJob.cancel()
        }

    @Test
    fun chartData_shouldAggregateByCategoryCorrectly_whenMultipleTransactionsSameCategory() =
        runViewModelTest {
            val t0 = 1_700_000_000_000L
            fakeRepo.transactions.value =
                listOf(
                    Transaction(
                        1,
                        "Breakfast",
                        -50.0,
                        "Food",
                        TransactionType.EXPENSE,
                        t0,
                    ),
                    Transaction(
                        2,
                        "Lunch",
                        -30.0,
                        "Food",
                        TransactionType.EXPENSE,
                        t0 - 1000,
                    ),
                    Transaction(
                        3,
                        "Dinner",
                        -40.0,
                        "Food",
                        TransactionType.EXPENSE,
                        t0 - 2000,
                    ),
                )

            includeAllTransactions()
            val collectJob =
                launch {
                    viewModel.chartData.collect {}
                }
            advanceUntilIdle()

            val chartData = viewModel.chartData.value
            val foodTotal = chartData.expenseByCategory["Food"]
            assertEquals(120.0, foodTotal ?: 0.0, 0.01)
            collectJob.cancel()
        }

    @Test
    fun chartData_shouldSeparateIncomeAndExpense_whenMixedTypesExist() =
        runViewModelTest {
            val t0 = 1_700_000_000_000L
            fakeRepo.transactions.value =
                listOf(
                    Transaction(1, "Salary", 3000.0, "Salary", TransactionType.INCOME, t0),
                    Transaction(2, "Bonus", 1000.0, "Bonus", TransactionType.INCOME, t0 - 1000),
                    Transaction(3, "Groceries", -200.0, "Food", TransactionType.EXPENSE, t0 - 2000),
                    Transaction(4, "Gas", -100.0, "Transport", TransactionType.EXPENSE, t0 - 3000),
                )

            includeAllTransactions()
            val collectJob =
                launch {
                    viewModel.chartData.collect {}
                }
            advanceUntilIdle()

            val chartData = viewModel.chartData.value
            assertEquals(4000.0, chartData.totalIncome, 0.01)
            assertEquals(300.0, chartData.totalExpense, 0.01)
            assertTrue(chartData.incomeByCategory.size >= 2)
            assertTrue(chartData.expenseByCategory.size >= 2)
            collectJob.cancel()
        }

    @Test
    fun chartData_shouldAggregateFiveCategoriesEach_whenLargeDatasetLoaded() =
        runViewModelTest {
            val transactions =
                (1..100).map { index ->
                    Transaction(
                        id = index.toLong(),
                        title = "Transaction $index",
                        amount = if (index % 2 == 0) -50.0 else 100.0,
                        category = "Category ${index % 5}",
                        type = if (index % 2 == 0) TransactionType.EXPENSE else TransactionType.INCOME,
                        timestamp = 1_700_000_000_000L - (index * 1000L),
                    )
                }
            fakeRepo.transactions.value = transactions
            includeAllTransactions()

            val collectJob = launch { viewModel.chartData.collect {} }
            advanceUntilIdle()

            val chartData = viewModel.chartData.value
            assertEquals(5, chartData.expenseByCategory.size)
            assertEquals(5, chartData.incomeByCategory.size)
            assertEquals(2500.0, chartData.totalExpense, 0.01)
            assertEquals(5000.0, chartData.totalIncome, 0.01)
            collectJob.cancel()
        }

    @Test
    fun setDateRange_shouldUpdateState_whenValidRangeProvided() =
        runViewModelTest {
            val now = System.currentTimeMillis()
            val from = now - 86400000 // 1 day ago
            val to = now

            viewModel.onEvent(ChartEvent.SetDateRangeEvent(from, to))
            advanceUntilIdle()

            val dateRange = viewModel.dateRange.value
            assertEquals(from, dateRange.from)
            assertEquals(to, dateRange.to)
        }

    @Test
    fun setDateRange_shouldNormalizeFromAndTo_whenFromIsGreater() =
        runViewModelTest {
            val now = System.currentTimeMillis()
            val from = now
            val to = now - 86400000 // Intentionally reversed

            viewModel.onEvent(ChartEvent.SetDateRangeEvent(from, to)) // Reversed order
            advanceUntilIdle()

            val dateRange = viewModel.dateRange.value
            assertEquals(to, dateRange.from) // Should be normalized
            assertEquals(from, dateRange.to)
        }

    @Test
    fun setPresetRange_shouldSetWeekPreset_whenWeekPresetSelected() =
        runViewModelTest {
            val collectJob =
                launch {
                    viewModel.selectedPresetIndex.collect {}
                }

            viewModel.onEvent(ChartEvent.SetPresetRangeEvent(RangePreset.WEEK))
            advanceUntilIdle()

            assertEquals(RangePreset.WEEK.ordinal, viewModel.selectedPresetIndex.value)
            collectJob.cancel()
        }

    @Test
    fun setPresetRange_shouldSetMonthPreset_whenMonthPresetSelected() =
        runViewModelTest {
            val collectJob =
                launch {
                    viewModel.selectedPresetIndex.collect {}
                }

            viewModel.onEvent(ChartEvent.SetPresetRangeEvent(RangePreset.MONTH))
            advanceUntilIdle()

            assertEquals(RangePreset.MONTH.ordinal, viewModel.selectedPresetIndex.value)
            collectJob.cancel()
        }

    @Test
    fun setPresetRange_shouldSetYearPreset_whenYearPresetSelected() =
        runViewModelTest {
            val collectJob =
                launch {
                    viewModel.selectedPresetIndex.collect {}
                }

            viewModel.onEvent(ChartEvent.SetPresetRangeEvent(RangePreset.YEAR))
            advanceUntilIdle()

            assertEquals(RangePreset.YEAR.ordinal, viewModel.selectedPresetIndex.value)
            collectJob.cancel()
        }

    @Test
    fun onSelectChartCategoryEvent_shouldSetDetails_whenCategorySelected() =
        runViewModelTest {
            val collectJob =
                launch {
                    viewModel.selectedChartDetails.collect {}
                }

            viewModel.onEvent(
                ChartEvent.SelectChartCategoryEvent(
                    categoryName = "Food",
                    amount = -500.0,
                    colorHex = 0xFF5555,
                    isExpense = true,
                ),
            )
            advanceUntilIdle()

            val details = viewModel.selectedChartDetails.value
            assertNotNull(details)
            assertEquals("Food", details?.categoryName)
            assertEquals(-500.0, details?.amount)
            collectJob.cancel()
        }

    @Test
    fun selectChartCategory_shouldCalculateFiftyPercent_whenCategoryIsHalfOfTotal() =
        runViewModelTest {
            fakeRepo.transactions.value =
                listOf(
                    Transaction(1, "Food", -500.0, "Food", TransactionType.EXPENSE, 1_700_000_000_000L),
                    Transaction(2, "Transport", -500.0, "Transport", TransactionType.EXPENSE, 1_700_000_000_000L - 1000),
                )

            includeAllTransactions()
            val collectJob =
                launch {
                    viewModel.chartData.collect {}
                    viewModel.selectedChartDetails.collect {}
                }
            advanceUntilIdle()

            viewModel.onEvent(
                ChartEvent.SelectChartCategoryEvent(
                    categoryName = "Food",
                    amount = -500.0,
                    colorHex = 0xFF5555,
                    isExpense = true,
                ),
            )
            advanceUntilIdle()

            val details = viewModel.selectedChartDetails.value
            assertNotNull(details)
            assertEquals(50, details?.percentage)
            collectJob.cancel()
        }

    @Test
    fun clearChartSelection_shouldSetDetailsToNull() =
        runViewModelTest {
            val collectJob =
                launch {
                    viewModel.selectedChartDetails.collect {}
                }

            viewModel.onEvent(
                ChartEvent.SelectChartCategoryEvent(
                    categoryName = "Food",
                    amount = -500.0,
                    colorHex = 0xFF5555,
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
    fun chartData_shouldHaveCorrectCategoryCount_whenMixedCategoriesExist() =
        runViewModelTest {
            val t0 = 1_700_000_000_000L
            fakeRepo.transactions.value =
                listOf(
                    Transaction(1, "Salary", 5000.0, "Salary", TransactionType.INCOME, t0),
                    Transaction(2, "Freelance", 1000.0, "Freelance", TransactionType.INCOME, t0 - 1000),
                    Transaction(3, "Food", -300.0, "Food", TransactionType.EXPENSE, t0 - 2000),
                    Transaction(4, "Transport", -200.0, "Transport", TransactionType.EXPENSE, t0 - 3000),
                    Transaction(5, "Entertainment", -150.0, "Entertainment", TransactionType.EXPENSE, t0 - 4000),
                )

            includeAllTransactions()
            val collectJob =
                launch {
                    viewModel.chartData.collect {}
                }
            advanceUntilIdle()

            val chartData = viewModel.chartData.value
            assertEquals(2, chartData.incomeByCategory.size)
            assertEquals(3, chartData.expenseByCategory.size)
            collectJob.cancel()
        }

    @Test
    fun chartData_shouldTotalToZero_whenAllAmountsAreZero() =
        runViewModelTest {
            fakeRepo.transactions.value =
                listOf(
                    Transaction(1, "Transfer", 0.0, "Transfer", TransactionType.EXPENSE, 1_700_000_000_000L),
                )

            val collectJob =
                launch {
                    viewModel.chartData.collect {}
                }
            advanceUntilIdle()

            val chartData = viewModel.chartData.value
            // Zero amount transactions should still appear but not affect totals significantly
            assertEquals(0.0, chartData.totalExpense, 0.0)
            collectJob.cancel()
        }

    @Test
    fun monthlyData_shouldShowTotalsForMonth_whenTransactionsLoaded() =
        runViewModelTest {
            // T0 = Nov 14 2023; T0_PREV_MONTH = Oct 15 2023 (different month for aggregation)
            val t0 = 1_700_000_000_000L
            val tPrevMonth = t0 - (30 * 24 * 60 * 60 * 1000L)

            fakeRepo.transactions.value =
                listOf(
                    Transaction(1, "Income", 1000.0, "Salary", TransactionType.INCOME, t0),
                    Transaction(2, "Expense", -500.0, "Food", TransactionType.EXPENSE, t0),
                    Transaction(3, "Old Income", 500.0, "Salary", TransactionType.INCOME, tPrevMonth),
                )

            includeAllTransactions()
            val collectJob = launch { viewModel.chartData.collect {} }
            advanceUntilIdle()

            val chartData = viewModel.chartData.value
            // 3 transactions across 2 calendar months → at least 2 monthly entries
            assertTrue(chartData.monthlyData.size >= 2)
            // overall totals across all months
            assertEquals(1500.0, chartData.totalIncome, 0.01)
            assertEquals(500.0, chartData.totalExpense, 0.01)
            collectJob.cancel()
        }
}
