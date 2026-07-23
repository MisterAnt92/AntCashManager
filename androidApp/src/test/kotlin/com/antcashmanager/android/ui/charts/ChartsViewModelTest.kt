package com.antcashmanager.android.ui.charts

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.testutil.FakeSettingsRepository
import com.antcashmanager.android.testutil.FakeTransactionRepository
import com.antcashmanager.android.ui.screen.charts.ChartsViewModel
import com.antcashmanager.android.ui.screen.charts.RangePreset
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.SavedDateFilter
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

@OptIn(ExperimentalCoroutinesApi::class)
class ChartsViewModelTest : BaseUnitTest() {
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
        viewModel.setDateRange(now - 86400000, now + 86400000)
        advanceUntilIdle()
        assertEquals(2000.0, viewModel.chartData.value.totalIncome, 0.01)
        assertEquals(200.0, viewModel.chartData.value.totalExpense, 0.01)
        assertEquals(2, viewModel.chartData.value.expenseByCategory.size)
        collectJob.cancel()
    }

    @Test
    fun setPresetRangeUpdatesDateRange() = runViewModelTest {
        val initialRange = viewModel.dateRange.value
        viewModel.setPresetRange(RangePreset.YEAR)
        advanceUntilIdle()
        val newRange = viewModel.dateRange.value
        assertTrue(newRange.from < initialRange.from)
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
            )
            presetViewModel.setPresetRange(preset)
            advanceUntilIdle()

            val range = presetViewModel.dateRange.value
            assertEquals(preset.ordinal, presetViewModel.selectedPresetIndex.value)
            // Ogni preset più lungo deve produrre un "from" più indietro nel tempo del precedente.
            assertTrue(range.from <= previousFrom)
            previousFrom = range.from
        }
    }

    @Test
    fun expenseByCategoryGroupsCorrectly() = runViewModelTest {
        val now = System.currentTimeMillis()
        fakeRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Lunch",
                amount = 15.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = now
            ),
            Transaction(
                id = 2,
                title = "Dinner",
                amount = 25.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = now
            ),
            Transaction(
                id = 3,
                title = "Bus",
                amount = 5.0,
                category = "Transport",
                type = TransactionType.EXPENSE,
                timestamp = now
            ),
        )
        viewModel.setDateRange(now - 86400000, now + 86400000)
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.chartData.collect {}
        }
        advanceUntilIdle()
        val expenseByCategory = viewModel.chartData.value.expenseByCategory
        assertEquals(40.0, expenseByCategory["Food"] ?: 0.0, 0.01)
        assertEquals(5.0, expenseByCategory["Transport"] ?: 0.0, 0.01)
        collectJob.cancel()
    }

    @Test
    fun chartDataGroupsPaymentTypeBreakdownCorrectly() = runViewModelTest {
        val now = System.currentTimeMillis()
        fakeRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Salary",
                amount = 2000.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = now,
                paymentType = PaymentType.ELECTRONIC,
            ),
            Transaction(
                id = 2,
                title = "Groceries",
                amount = 60.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = now,
                paymentType = PaymentType.CASH,
            ),
            Transaction(
                id = 3,
                title = "Lunch",
                amount = 12.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = now,
                paymentType = PaymentType.MEAL_VOUCHERS,
            ),
            Transaction(
                id = 4,
                title = "Coffee",
                amount = 3.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = now,
                paymentType = PaymentType.CASH,
            ),
        )
        viewModel.setDateRange(now - 86400000, now + 86400000)
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.chartData.collect {}
        }
        advanceUntilIdle()

        val breakdown = viewModel.chartData.value.paymentTypeBreakdown
        assertEquals(2000.0, breakdown[PaymentType.ELECTRONIC] ?: 0.0, 0.01)
        assertEquals(63.0, breakdown[PaymentType.CASH] ?: 0.0, 0.01)
        assertEquals(12.0, breakdown[PaymentType.MEAL_VOUCHERS] ?: 0.0, 0.01)
        collectJob.cancel()
    }

    @Test
    fun customChartsDateRangeIsPersistedAndRestored() = runViewModelTest {
        val from = 1_712_000_000_000L
        val to = 1_712_800_000_000L

        viewModel.setDateRange(from, to)
        advanceUntilIdle()

        assertEquals(SavedDateFilter.CUSTOM_PRESET_INDEX, viewModel.selectedPresetIndex.value)
        assertEquals(from, fakeSettingsRepository.chartsDateFilterState.value.from)
        assertEquals(to, fakeSettingsRepository.chartsDateFilterState.value.to)

        val restoredViewModel = ChartsViewModel(
            transactionRepository = fakeRepo,
            settingsRepository = fakeSettingsRepository,
            dispatcher = testDispatcher,
        )
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            restoredViewModel.chartData.collect {}
        }
        advanceUntilIdle()

        assertEquals(
            SavedDateFilter.CUSTOM_PRESET_INDEX,
            restoredViewModel.selectedPresetIndex.value
        )
        assertEquals(from, restoredViewModel.dateRange.value.from)
        assertEquals(to, restoredViewModel.dateRange.value.to)
        collectJob.cancel()
    }
}
