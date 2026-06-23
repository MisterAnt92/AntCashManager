package com.antcashmanager.android.ui.charts

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.ui.screen.charts.ChartsViewModel
import com.antcashmanager.android.ui.screen.charts.RangePreset
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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
    fun `initial chart data is empty`() = runViewModelTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.chartData.collect {}
        }
        advanceUntilIdle()
        assertEquals(0.0, viewModel.chartData.value.totalIncome, 0.01)
        assertEquals(0.0, viewModel.chartData.value.totalExpense, 0.01)
        collectJob.cancel()
    }

    @Test
    fun `chart data computes totals correctly`() = runViewModelTest {
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
    fun `setPresetRange updates date range`() = runViewModelTest {
        val initialRange = viewModel.dateRange.value
        viewModel.setPresetRange(RangePreset.YEAR)
        advanceUntilIdle()
        val newRange = viewModel.dateRange.value
        assertTrue(newRange.from < initialRange.from)
    }

    @Test
    fun `expense by category groups correctly`() = runViewModelTest {
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
    fun `custom charts date range is persisted and restored`() = runViewModelTest {
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

        assertEquals(SavedDateFilter.CUSTOM_PRESET_INDEX, restoredViewModel.selectedPresetIndex.value)
        assertEquals(from, restoredViewModel.dateRange.value.from)
        assertEquals(to, restoredViewModel.dateRange.value.to)
        collectJob.cancel()
    }
}

private class FakeTransactionRepository : TransactionRepository {
    val transactions = MutableStateFlow<List<Transaction>>(emptyList())
    override fun getAllTransactions(): Flow<List<Transaction>> = transactions
    override suspend fun getTransactionById(id: Long): Transaction? =
        transactions.value.find { it.id == id }

    override suspend fun insertTransaction(transaction: Transaction): Long {
        transactions.value = transactions.value + transaction
        return transaction.id
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactions.value =
            transactions.value.map { if (it.id == transaction.id) transaction else it }
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactions.value = transactions.value.filter { it.id != transaction.id }
    }

    override suspend fun deleteAllTransactions() {
        transactions.value = emptyList()
    }

    override fun getTransactionsByDateRange(from: Long, to: Long): Flow<List<Transaction>> =
        transactions.map { list -> list.filter { it.timestamp in from..to } }

    override fun getRecurringTransactions(): Flow<List<Transaction>> =
        transactions.map { list -> list.filter { it.isRecurring } }

    override suspend fun updateCategoryData(categoryName: String, icon: String, color: Long) {
        // No-op for test
    }

    override fun getDistinctTitles() = flowOf(emptyList<String>())
    override fun getDistinctPayees() = flowOf(emptyList<String>())
    override fun getDistinctNotes() = flowOf(emptyList<String>())
    override fun getDistinctLocations() = flowOf(emptyList<String>())
    override fun getDistinctTags() = flowOf(emptyList<String>())
}

private class FakeSettingsRepository : SettingsRepository {
    private val homeDateFilterState = MutableStateFlow(
        SavedDateFilter(
            presetIndex = 1,
            from = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000),
            to = System.currentTimeMillis(),
        ),
    )
    private val transactionsDateFilterState = MutableStateFlow(
        SavedDateFilter(
            presetIndex = 1,
            from = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000),
            to = System.currentTimeMillis(),
        ),
    )
    @get:JvmName("mutableChartsDateFilterState")
    val chartsDateFilterState = MutableStateFlow(
        SavedDateFilter(
            presetIndex = 1,
            from = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000),
            to = System.currentTimeMillis(),
        ),
    )

    override fun getTheme() = flowOf(com.antcashmanager.domain.model.AppTheme.SYSTEM)
    override suspend fun setTheme(theme: com.antcashmanager.domain.model.AppTheme) = Unit
    override fun getLanguage() = flowOf(com.antcashmanager.domain.model.AppLanguage.SYSTEM)
    override suspend fun setLanguage(language: com.antcashmanager.domain.model.AppLanguage) = Unit
    override fun getShowCharts() = flowOf(true)
    override suspend fun setShowCharts(show: Boolean) = Unit
    override fun getHighContrast() = flowOf(false)
    override suspend fun setHighContrast(enabled: Boolean) = Unit
    override fun getLargeText() = flowOf(false)
    override suspend fun setLargeText(enabled: Boolean) = Unit
    override fun getReduceMotion() = flowOf(false)
    override suspend fun setReduceMotion(enabled: Boolean) = Unit
    override fun getShowTransactionNotes() = flowOf(true)
    override suspend fun setShowTransactionNotes(show: Boolean) = Unit
    override fun getCurrencySymbol() = flowOf("€")
    override suspend fun setCurrencySymbol(symbol: String) = Unit
    override fun getDecimalDigits() = flowOf(2)
    override suspend fun setDecimalDigits(digits: Int) = Unit
    override fun getDecimalSeparator() = flowOf(",")
    override suspend fun setDecimalSeparator(separator: String) = Unit
    override fun getThousandsSeparator() = flowOf("")
    override suspend fun setThousandsSeparator(separator: String) = Unit
    override fun getDateFormat() = flowOf("dd/MM/yyyy")
    override suspend fun setDateFormat(pattern: String) = Unit
    override fun getDateFilterExpanded() = flowOf(true)
    override suspend fun setDateFilterExpanded(expanded: Boolean) = Unit
    override fun getHomeDateFilterPreset() = homeDateFilterState.map { it.presetIndex }
    override suspend fun setHomeDateFilterPreset(index: Int) = Unit
    override fun getHomeDateFilterState() = homeDateFilterState
    override suspend fun setHomeDateFilterState(filter: SavedDateFilter) {
        homeDateFilterState.value = filter
    }

    override fun getTransactionsDateFilterPreset() = transactionsDateFilterState.map { it.presetIndex }
    override suspend fun setTransactionsDateFilterPreset(index: Int) = Unit
    override fun getTransactionsDateFilterState() = transactionsDateFilterState
    override suspend fun setTransactionsDateFilterState(filter: SavedDateFilter) {
        transactionsDateFilterState.value = filter
    }

    override fun getChartsDateFilterPreset() = chartsDateFilterState.map { it.presetIndex }
    override suspend fun setChartsDateFilterPreset(index: Int) = Unit
    override fun getChartsDateFilterState() = chartsDateFilterState
    override suspend fun setChartsDateFilterState(filter: SavedDateFilter) {
        chartsDateFilterState.value = filter
    }

    override fun getChartsZoomEnabled() = flowOf(true)
    override suspend fun setChartsZoomEnabled(enabled: Boolean) = Unit
    override fun getShowPaymentTypeBreakdown() = flowOf(false)
    override suspend fun setShowPaymentTypeBreakdown(show: Boolean) = Unit
    override fun getTransactionDisplayType() = flowOf(com.antcashmanager.domain.model.TransactionDisplayType.TREND)
    override suspend fun setTransactionDisplayType(displayType: com.antcashmanager.domain.model.TransactionDisplayType) = Unit
    override fun getTransactionsTransactionDisplayType() = flowOf(com.antcashmanager.domain.model.TransactionDisplayType.TREND)
    override suspend fun setTransactionsTransactionDisplayType(displayType: com.antcashmanager.domain.model.TransactionDisplayType) = Unit
    override fun getIsTutorialCompleted() = flowOf(true)
    override suspend fun setIsTutorialCompleted(completed: Boolean) = Unit
    override fun getDataEncryptionEnabled() = flowOf(false)
    override suspend fun setDataEncryptionEnabled(enabled: Boolean) = Unit
    override suspend fun resetAllPreferences() = Unit
}

