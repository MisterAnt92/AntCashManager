package com.antcashmanager.android.ui.screen.charts

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.testutil.FakeSettingsRepository
import com.antcashmanager.android.testutil.FakeTransactionRepository
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class ChartsViewModelTest : BaseUnitTest() {

    private lateinit var fakeTransactionRepo: FakeTransactionRepository
    private lateinit var fakeSettingsRepo: FakeSettingsRepository
    private lateinit var viewModel: ChartsViewModel

    @Before
    fun setup() {
        fakeTransactionRepo = FakeTransactionRepository()
        fakeSettingsRepo = FakeSettingsRepository()
        viewModel = ChartsViewModel(fakeTransactionRepo, fakeSettingsRepo)
    }

    @Test
    fun initialChartDataIsEmpty() = runViewModelTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            launch { viewModel.chartData.collect { } }
        }
        advanceUntilIdle()

        val chartData = viewModel.chartData.value
        assertEquals(0, chartData.dailyTimeline.size)
        assertEquals(0, chartData.expenseByWeekday.size)

        collectJob.cancel()
    }

    /**
     * Test that dailyTimeline is correctly populated with daily aggregated expenses.
     */
    @Test
    fun dailyTimelineAggregatesExpensesByDate() = runViewModelTest {
        val cal = Calendar.getInstance()
        cal.set(2024, 0, 1, 12, 0, 0)
        val jan1 = cal.timeInMillis

        cal.set(2024, 0, 2, 12, 0, 0)
        val jan2 = cal.timeInMillis

        cal.set(2024, 0, 3, 12, 0, 0)
        val jan3 = cal.timeInMillis

        fakeTransactionRepo.transactions.value = listOf(
            Transaction(id = 1, title = "Lunch", amount = -100.0, timestamp = jan1, category = "Food", type = TransactionType.EXPENSE, paymentType = PaymentType.CASH),
            Transaction(id = 2, title = "Coffee", amount = -50.0, timestamp = jan1, category = "Food", type = TransactionType.EXPENSE, paymentType = PaymentType.CASH),
            Transaction(id = 3, title = "Dinner", amount = -75.0, timestamp = jan2, category = "Food", type = TransactionType.EXPENSE, paymentType = PaymentType.CASH),
            Transaction(id = 4, title = "Groceries", amount = -200.0, timestamp = jan3, category = "Shopping", type = TransactionType.EXPENSE, paymentType = PaymentType.ELECTRONIC),
            Transaction(id = 5, title = "Snack", amount = -50.0, timestamp = jan3, category = "Food", type = TransactionType.EXPENSE, paymentType = PaymentType.CASH),
        )

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            launch { viewModel.chartData.collect { } }
        }

        viewModel.setDateRange(jan1 - 86_400_000L, jan3 + 86_400_000L)
        advanceUntilIdle()

        val chartData = viewModel.chartData.value
        assertEquals(3, chartData.dailyTimeline.size)
        assertEquals(150.0, chartData.dailyTimeline[0].expense, 0.01)
        assertEquals(75.0, chartData.dailyTimeline[1].expense, 0.01)
        assertEquals(250.0, chartData.dailyTimeline[2].expense, 0.01)

        collectJob.cancel()
    }

    /**
     * Test that expenseByWeekday normalizes Calendar.DAY_OF_WEEK to EU convention.
     */
    @Test
    fun expenseByWeekdayNormalizesCorrectly() = runViewModelTest {
        val cal = Calendar.getInstance()
        cal.set(2024, 0, 1, 12, 0, 0)  // Monday
        val monday = cal.timeInMillis

        fakeTransactionRepo.transactions.value = listOf(
            Transaction(id = 1, title = "Monday 1", amount = -100.0, timestamp = monday, category = "Food", type = TransactionType.EXPENSE, paymentType = PaymentType.CASH),
            Transaction(id = 2, title = "Monday 2", amount = -50.0, timestamp = monday, category = "Food", type = TransactionType.EXPENSE, paymentType = PaymentType.CASH),
        )

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            launch { viewModel.chartData.collect { } }
        }

        cal.set(2024, 0, 1, 0, 0, 0)
        val startDate = cal.timeInMillis
        cal.set(2024, 0, 10, 23, 59, 59)
        val endDate = cal.timeInMillis

        viewModel.setDateRange(startDate, endDate)
        advanceUntilIdle()

        val chartData = viewModel.chartData.value
        assertTrue(chartData.expenseByWeekday.isNotEmpty())

        // Monday is day 1 in EU convention
        val mondayAverage = chartData.expenseByWeekday[1]
        assertEquals(75.0, mondayAverage ?: 0.0, 0.01)

        collectJob.cancel()
    }

    /**
     * Test that expenseByWeekday ignores income transactions.
     */
    @Test
    fun expenseByWeekdayIgnoresIncomeTransactions() = runViewModelTest {
        val cal = Calendar.getInstance()
        cal.set(2024, 0, 1, 12, 0, 0)
        val monday = cal.timeInMillis

        fakeTransactionRepo.transactions.value = listOf(
            Transaction(id = 1, title = "Expense", amount = -100.0, timestamp = monday, category = "Food", type = TransactionType.EXPENSE, paymentType = PaymentType.CASH),
            Transaction(id = 2, title = "Salary", amount = 5000.0, timestamp = monday, category = "Salary", type = TransactionType.INCOME, paymentType = PaymentType.ELECTRONIC),
        )

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            launch { viewModel.chartData.collect { } }
        }

        cal.set(2024, 0, 1, 0, 0, 0)
        val startDate = cal.timeInMillis
        cal.set(2024, 0, 10, 23, 59, 59)
        val endDate = cal.timeInMillis

        viewModel.setDateRange(startDate, endDate)
        advanceUntilIdle()

        val chartData = viewModel.chartData.value
        val mondayExpense = chartData.expenseByWeekday[1]
        assertEquals(100.0, mondayExpense ?: 0.0, 0.01)

        collectJob.cancel()
    }

    /**
     * Test that small datasets are handled correctly.
     */
    @Test
    fun chartDataHandlesSmallDatasetsCorrectly() = runViewModelTest {
        val cal = Calendar.getInstance()
        cal.set(2024, 0, 1, 12, 0, 0)
        val date1 = cal.timeInMillis

        fakeTransactionRepo.transactions.value = listOf(
            Transaction(id = 1, title = "Single", amount = -42.50, timestamp = date1, category = "Food", type = TransactionType.EXPENSE, paymentType = PaymentType.CASH),
        )

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            launch { viewModel.chartData.collect { } }
        }

        viewModel.setDateRange(date1 - 86_400_000L, date1 + 86_400_000L)
        advanceUntilIdle()

        val chartData = viewModel.chartData.value
        assertEquals(1, chartData.dailyTimeline.size)
        assertEquals(42.50, chartData.dailyTimeline[0].expense, 0.01)
        assertTrue(chartData.expenseByWeekday.isNotEmpty())

        collectJob.cancel()
    }
}
