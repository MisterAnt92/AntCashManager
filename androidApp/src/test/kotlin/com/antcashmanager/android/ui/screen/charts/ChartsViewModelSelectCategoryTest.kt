package com.antcashmanager.android.ui.screen.charts

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.testutil.FakeSettingsRepository
import com.antcashmanager.android.testutil.FakeTransactionRepository
import com.antcashmanager.android.ui.screen.charts.view.TrendDirection
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.Calendar

/**
 * Unit tests for chart category selection and details display.
 *
 * Tests cover:
 * - Selecting a chart category
 * - Clearing chart selection
 * - Percentage calculation
 * - Amount handling for income/expense
 * - Transaction count tracking
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChartsViewModelSelectCategoryTest : BaseUnitTest() {

    private lateinit var fakeTransactionRepo: FakeTransactionRepository
    private lateinit var fakeSettingsRepo: FakeSettingsRepository
    private lateinit var viewModel: ChartsViewModel

    @Before
    fun setup() {
        fakeTransactionRepo = FakeTransactionRepository()
        fakeSettingsRepo = FakeSettingsRepository()
        // Pass testDispatcher to ensure ViewModel uses the same dispatcher as the test
        viewModel = ChartsViewModel(fakeTransactionRepo, fakeSettingsRepo, dispatcher = testDispatcher)
    }

    @Test
    fun selectChartCategory_updatesSelectedChartDetails() = runViewModelTest {
        val collectJob = launchInBackground {
            viewModel.selectedChartDetails.collect { }
        }

        viewModel.selectChartCategory(
            categoryName = "Food",
            amount = 250.0,
            colorHex = 0xFFE57373,
            isExpense = true,
        )
        advanceUntilIdle()

        val details = viewModel.selectedChartDetails.value
        assertNotNull(details)
        assertEquals("Food", details?.categoryName)
        assertEquals(250.0, details?.amount ?: 0.0, 0.01)

        collectJob.cancel()
    }

    @Test
    fun selectChartCategory_calculatesPercentageCorrectly() = runViewModelTest {
        val cal = Calendar.getInstance()
        cal.set(2024, 0, 1, 12, 0, 0)
        val timestamp = cal.timeInMillis

        fakeTransactionRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Food",
                amount = -100.0,
                timestamp = timestamp,
                category = "Food",
                type = TransactionType.EXPENSE,
                paymentType = PaymentType.CASH
            ),
            Transaction(
                id = 2,
                title = "Transport",
                amount = -150.0,
                timestamp = timestamp,
                category = "Transport",
                type = TransactionType.EXPENSE,
                paymentType = PaymentType.CASH
            ),
        )

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            launch { viewModel.chartData.collect { } }
            launch { viewModel.selectedChartDetails.collect { } }
        }
        advanceUntilIdle()

        // Set date range to load transactions
        viewModel.setDateRange(timestamp - 86_400_000L, timestamp + 86_400_000L)
        advanceUntilIdle()

        // Select Food category (100 out of 250 total = 40%)
        viewModel.selectChartCategory(
            categoryName = "Food",
            amount = 100.0,
            colorHex = 0xFFE57373,
            isExpense = true,
        )
        advanceUntilIdle()

        val details = viewModel.selectedChartDetails.value
        assertEquals(40, details?.percentage)

        collectJob.cancel()
    }

    @Test
    fun selectChartCategory_coercesPercentageBounds() = runViewModelTest {
        val collectJob = launchInBackground {
            viewModel.selectedChartDetails.collect { }
        }

        viewModel.selectChartCategory(
            categoryName = "Test",
            amount = 1000.0,
            colorHex = 0xFFE57373,
            isExpense = true,
        )
        advanceUntilIdle()

        val details = viewModel.selectedChartDetails.value
        // Percentage should be clamped to max 100
        assertEquals(true, details?.percentage!! <= 100)

        collectJob.cancel()
    }

    @Test
    fun selectChartCategory_handleZeroTotalAmount() = runViewModelTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            launch { viewModel.chartData.collect { } }
            launch { viewModel.selectedChartDetails.collect { } }
        }
        advanceUntilIdle()

        viewModel.selectChartCategory(
            categoryName = "Empty",
            amount = 100.0,
            colorHex = 0xFFE57373,
            isExpense = true,
        )
        advanceUntilIdle()

        val details = viewModel.selectedChartDetails.value
        // When total is 0, percentage should be 0
        assertEquals(0, details?.percentage)

        collectJob.cancel()
    }

    @Test
    fun selectChartCategory_storesColorHex() = runViewModelTest {
        val collectJob = launchInBackground {
            viewModel.selectedChartDetails.collect { }
        }

        val colorHex = 0xFFE57373L
        viewModel.selectChartCategory(
            categoryName = "Food",
            amount = 250.0,
            colorHex = colorHex,
            isExpense = true,
        )
        advanceUntilIdle()

        val details = viewModel.selectedChartDetails.value
        assertEquals(colorHex, details?.colorHex)

        collectJob.cancel()
    }

    @Test
    fun clearChartSelection_setsDetailsToNull() = runViewModelTest {
        val collectJob = launchInBackground {
            viewModel.selectedChartDetails.collect { }
        }

        // First select a category
        viewModel.selectChartCategory(
            categoryName = "Food",
            amount = 250.0,
            colorHex = 0xFFE57373,
            isExpense = true,
        )
        advanceUntilIdle()
        assertNotNull(viewModel.selectedChartDetails.value)

        // Then clear
        viewModel.clearChartSelection()
        advanceUntilIdle()

        assertNull(viewModel.selectedChartDetails.value)

        collectJob.cancel()
    }

    @Test
    fun selectChartCategory_handlesIncomeCategory() = runViewModelTest {
        val collectJob = launchInBackground {
            viewModel.selectedChartDetails.collect { }
        }

        viewModel.selectChartCategory(
            categoryName = "Salary",
            amount = 5000.0,
            colorHex = 0xFF81C784,
            isExpense = false,
        )
        advanceUntilIdle()

        val details = viewModel.selectedChartDetails.value
        assertEquals("Salary", details?.categoryName)
        assertEquals(5000.0, details?.amount ?: 0.0, 0.01)

        collectJob.cancel()
    }

    @Test
    fun selectChartCategory_defaultsTrendToNeutral() = runViewModelTest {
        val collectJob = launchInBackground {
            viewModel.selectedChartDetails.collect { }
        }

        viewModel.selectChartCategory(
            categoryName = "Food",
            amount = 250.0,
            colorHex = 0xFFE57373,
            isExpense = true,
        )
        advanceUntilIdle()

        val details = viewModel.selectedChartDetails.value
        assertEquals(TrendDirection.NEUTRAL, details?.trend)

        collectJob.cancel()
    }
}
