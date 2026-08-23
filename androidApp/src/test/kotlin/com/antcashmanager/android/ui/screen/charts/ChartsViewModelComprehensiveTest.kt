@file:Suppress("LocalVariableName")

package com.antcashmanager.android.ui.screen.charts

import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.testutil.FakeCategoryRepository
import com.antcashmanager.android.testutil.FakeSettingsRepository
import com.antcashmanager.android.testutil.FakeTransactionRepository
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

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
    private lateinit var viewModel: ChartsViewModel

    @Before
    fun setup() {
        fakeRepo = FakeTransactionRepository()
        fakeSettingsRepository = FakeSettingsRepository()
        viewModel = ChartsViewModel(
            transactionRepository = fakeRepo,
            settingsRepository = fakeSettingsRepository,
            dispatcher = testDispatcher,
        )
    }

    @After
    fun tearDown() {
        if (::viewModel.isInitialized) {
            viewModel.viewModelScope.cancel()
        }
    }

    @Test
    fun chartData_shouldBeEmpty_whenRepositoryIsEmpty() = runViewModelTest {
        val collectJob = launch {
            viewModel.chartData.collect {}
        }
        advanceUntilIdle()

        val chartData = viewModel.chartData.value
        assertTrue(chartData.incomeByCategory.isEmpty())
        assertTrue(chartData.expenseByCategory.isEmpty())
        assertEquals(0.0, chartData.totalIncome)
        assertEquals(0.0, chartData.totalExpense)
        collectJob.cancel()
    }

    @Test
    fun chartData_shouldDisplayAllTransactions_whenLoaded() = runViewModelTest {
        val now = System.currentTimeMillis()
        fakeRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Salary",
                amount = 5000.0,
                category = "Salary",
                type = TransactionType.INCOME,
                timestamp = now,
            ),
            Transaction(
                id = 2,
                title = "Groceries",
                amount = -100.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = now - 1000,
            ),
            Transaction(
                id = 3,
                title = "Rent",
                amount = -1500.0,
                category = "Housing",
                type = TransactionType.EXPENSE,
                timestamp = now - 2000,
            ),
        )

        val collectJob = launch {
            viewModel.chartData.collect {}
        }
        advanceUntilIdle()

        val chartData = viewModel.chartData.value
        assertEquals(5000.0, chartData.totalIncome)
        assertEquals(1600.0, chartData.totalExpense)
        collectJob.cancel()
    }

    @Test
    fun chartData_shouldAggregateByCategoryCorrectly() = runViewModelTest {
        val now = System.currentTimeMillis()
        fakeRepo.transactions.value = listOf(
            Transaction(
                1,
                "Breakfast",
                -50.0,
                "Food",
                TransactionType.EXPENSE,
                now,
            ),
            Transaction(
                2,
                "Lunch",
                -30.0,
                "Food",
                TransactionType.EXPENSE,
                now - 1000,
            ),
            Transaction(
                3,
                "Dinner",
                -40.0,
                "Food",
                TransactionType.EXPENSE,
                now - 2000,
            ),
        )

        val collectJob = launch {
            viewModel.chartData.collect {}
        }
        advanceUntilIdle()

        val chartData = viewModel.chartData.value
        val foodTotal = chartData.expenseByCategory["Food"]
        assertEquals(120.0, foodTotal)
        collectJob.cancel()
    }

    @Test
    fun chartData_shouldSeparateIncomeAndExpense() = runViewModelTest {
        val now = System.currentTimeMillis()
        fakeRepo.transactions.value = listOf(
            Transaction(1, "Salary", 3000.0, "Salary", TransactionType.INCOME, now),
            Transaction(2, "Bonus", 1000.0, "Bonus", TransactionType.INCOME, now - 1000),
            Transaction(3, "Groceries", -200.0, "Food", TransactionType.EXPENSE, now - 2000),
            Transaction(4, "Gas", -100.0, "Transport", TransactionType.EXPENSE, now - 3000),
        )

        val collectJob = launch {
            viewModel.chartData.collect {}
        }
        advanceUntilIdle()

        val chartData = viewModel.chartData.value
        assertEquals(4000.0, chartData.totalIncome)
        assertEquals(300.0, chartData.totalExpense)
        assertTrue(chartData.incomeByCategory.size >= 2)
        assertTrue(chartData.expenseByCategory.size >= 2)
        collectJob.cancel()
    }

    @Test
    fun chartData_shouldHandleLargeDataset() = runViewModelTest {
        val now = System.currentTimeMillis()
        val transactions = (1..100).map { index ->
            Transaction(
                id = index.toLong(),
                title = "Transaction $index",
                amount = if (index % 2 == 0) -50.0 else 100.0,
                category = "Category ${index % 5}",
                type = if (index % 2 == 0) TransactionType.EXPENSE else TransactionType.INCOME,
                timestamp = now - (index * 1000L),
            )
        }
        fakeRepo.transactions.value = transactions

        val collectJob = launch {
            viewModel.chartData.collect {}
        }
        advanceUntilIdle()

        val chartData = viewModel.chartData.value
        assertTrue(chartData.monthlyData.isNotEmpty() || chartData.yearlyData.isNotEmpty())
        collectJob.cancel()
    }

    @Test
    fun dateRange_shouldDefaultToLastMonth() = runViewModelTest {
        val collectJob = launch {
            viewModel.dateRange.collect {}
        }
        advanceUntilIdle()

        val dateRange = viewModel.dateRange.value
        assertNotNull(dateRange)
        assertTrue(dateRange.from > 0)
        assertTrue(dateRange.to > dateRange.from)
        collectJob.cancel()
    }

    @Test
    fun setDateRange_shouldUpdateState() = runViewModelTest {
        val now = System.currentTimeMillis()
        val from = now - 86400000 // 1 day ago
        val to = now

        viewModel.setDateRange(from, to)
        advanceUntilIdle()

        val dateRange = viewModel.dateRange.value
        assertEquals(from, dateRange.from)
        assertEquals(to, dateRange.to)
    }

    @Test
    fun setDateRange_shouldNormalizeFromAndTo_whenFromIsGreater() = runViewModelTest {
        val now = System.currentTimeMillis()
        val from = now
        val to = now - 86400000 // Intentionally reversed

        viewModel.setDateRange(from, to) // Reversed order
        advanceUntilIdle()

        val dateRange = viewModel.dateRange.value
        assertEquals(to, dateRange.from) // Should be normalized
        assertEquals(from, dateRange.to)
    }

    @Test
    fun setPresetRange_shouldSetWeekPreset() = runViewModelTest {
        val collectJob = launch {
            viewModel.selectedPresetIndex.collect {}
        }

        viewModel.setPresetRange(RangePreset.WEEK)
        advanceUntilIdle()

        assertEquals(RangePreset.WEEK.ordinal, viewModel.selectedPresetIndex.value)
        collectJob.cancel()
    }

    @Test
    fun setPresetRange_shouldSetMonthPreset() = runViewModelTest {
        val collectJob = launch {
            viewModel.selectedPresetIndex.collect {}
        }

        viewModel.setPresetRange(RangePreset.MONTH)
        advanceUntilIdle()

        assertEquals(RangePreset.MONTH.ordinal, viewModel.selectedPresetIndex.value)
        collectJob.cancel()
    }

    @Test
    fun setPresetRange_shouldSetYearPreset() = runViewModelTest {
        val collectJob = launch {
            viewModel.selectedPresetIndex.collect {}
        }

        viewModel.setPresetRange(RangePreset.YEAR)
        advanceUntilIdle()

        assertEquals(RangePreset.YEAR.ordinal, viewModel.selectedPresetIndex.value)
        collectJob.cancel()
    }

    @Test
    fun selectChartCategory_shouldSetDetails() = runViewModelTest {
        val collectJob = launch {
            viewModel.selectedChartDetails.collect {}
        }

        viewModel.selectChartCategory(
            categoryName = "Food",
            amount = -500.0,
            colorHex = 0xFF5555,
            isExpense = true,
        )
        advanceUntilIdle()

        val details = viewModel.selectedChartDetails.value
        assertNotNull(details)
        assertEquals("Food", details?.categoryName)
        assertEquals(-500.0, details?.amount)
        collectJob.cancel()
    }

    @Test
    fun selectChartCategory_shouldCalculatePercentageCorrectly() = runViewModelTest {
        val now = System.currentTimeMillis()
        fakeRepo.transactions.value = listOf(
            Transaction(1, "Food 1", -500.0, "Food", TransactionType.EXPENSE, now),
            Transaction(2, "Food 2", -500.0, "Food", TransactionType.EXPENSE, now - 1000),
        )

        val collectJob = launch {
            viewModel.chartData.collect {}
            viewModel.selectedChartDetails.collect {}
        }
        advanceUntilIdle()

        viewModel.selectChartCategory(
            categoryName = "Food",
            amount = -500.0,
            colorHex = 0xFF5555,
            isExpense = true,
        )
        advanceUntilIdle()

        val details = viewModel.selectedChartDetails.value
        assertNotNull(details)
        assertTrue(details?.percentage in 0..100)
        collectJob.cancel()
    }

    @Test
    fun clearChartSelection_shouldSetDetailsToNull() = runViewModelTest {
        val collectJob = launch {
            viewModel.selectedChartDetails.collect {}
        }

        viewModel.selectChartCategory(
            categoryName = "Food",
            amount = -500.0,
            colorHex = 0xFF5555,
        )
        advanceUntilIdle()

        assertNotNull(viewModel.selectedChartDetails.value)

        viewModel.clearChartSelection()
        advanceUntilIdle()

        assertNull(viewModel.selectedChartDetails.value)
        collectJob.cancel()
    }

    @Test
    fun chartData_shouldHandleMultipleCategories_inIncomeAndExpense() = runViewModelTest {
        val now = System.currentTimeMillis()
        fakeRepo.transactions.value = listOf(
            Transaction(1, "Salary", 5000.0, "Salary", TransactionType.INCOME, now),
            Transaction(2, "Freelance", 1000.0, "Freelance", TransactionType.INCOME, now - 1000),
            Transaction(3, "Food", -300.0, "Food", TransactionType.EXPENSE, now - 2000),
            Transaction(4, "Transport", -200.0, "Transport", TransactionType.EXPENSE, now - 3000),
            Transaction(5, "Entertainment", -150.0, "Entertainment", TransactionType.EXPENSE, now - 4000),
        )

        val collectJob = launch {
            viewModel.chartData.collect {}
        }
        advanceUntilIdle()

        val chartData = viewModel.chartData.value
        assertEquals(2, chartData.incomeByCategory.size)
        assertEquals(3, chartData.expenseByCategory.size)
        collectJob.cancel()
    }

    @Test
    fun chartData_shouldHandleZeroAmountTransactions() = runViewModelTest {
        val now = System.currentTimeMillis()
        fakeRepo.transactions.value = listOf(
            Transaction(1, "Transfer", 0.0, "Transfer", TransactionType.EXPENSE, now)
        )

        val collectJob = launch {
            viewModel.chartData.collect {}
        }
        advanceUntilIdle()

        val chartData = viewModel.chartData.value
        // Zero amount transactions should still appear but not affect totals significantly
        assertEquals(0.0, chartData.totalExpense)
        collectJob.cancel()
    }

    @Test
    fun monthlyData_shouldBeAggregatedCorrectly() = runViewModelTest {
        val now = System.currentTimeMillis()
        val oneMonthAgo = now - (30 * 24 * 60 * 60 * 1000L)

        fakeRepo.transactions.value = listOf(
            Transaction(1, "Income", 1000.0, "Salary", TransactionType.INCOME, now),
            Transaction(2, "Expense", -500.0, "Food", TransactionType.EXPENSE, now),
            Transaction(3, "Old Income", 500.0, "Salary", TransactionType.INCOME, oneMonthAgo),
        )

        val collectJob = launch {
            viewModel.chartData.collect {}
        }
        advanceUntilIdle()

        val chartData = viewModel.chartData.value
        assertTrue(chartData.monthlyData.size >= 1)
        collectJob.cancel()
    }
}
