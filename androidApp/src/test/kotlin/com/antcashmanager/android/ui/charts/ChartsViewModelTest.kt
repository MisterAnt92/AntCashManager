package com.antcashmanager.android.ui.charts
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
@OptIn(ExperimentalCoroutinesApi::class)
class ChartsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeTransactionRepository
    private lateinit var viewModel: ChartsViewModel
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeTransactionRepository()
        viewModel = ChartsViewModel(fakeRepo)
    }
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    @Test
    fun `initial chart data is empty`() = runTest(testDispatcher) {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.chartData.collect {}
        }
        advanceUntilIdle()
        assertEquals(0.0, viewModel.chartData.value.totalIncome, 0.01)
        assertEquals(0.0, viewModel.chartData.value.totalExpense, 0.01)
        collectJob.cancel()
    }
    @Test
    fun `chart data computes totals correctly`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        fakeRepo.transactions.value = listOf(
            Transaction(id = 1, title = "Salary", amount = 2000.0, category = "Work", type = TransactionType.INCOME, timestamp = now),
            Transaction(id = 2, title = "Food", amount = 150.0, category = "Food", type = TransactionType.EXPENSE, timestamp = now),
            Transaction(id = 3, title = "Transport", amount = 50.0, category = "Transport", type = TransactionType.EXPENSE, timestamp = now),
        )
        // Set date range to cover now
        viewModel.setDateRange(now - 86400000, now + 86400000)
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.chartData.collect {}
        }
        advanceUntilIdle()
        assertEquals(2000.0, viewModel.chartData.value.totalIncome, 0.01)
        assertEquals(200.0, viewModel.chartData.value.totalExpense, 0.01)
        assertEquals(2, viewModel.chartData.value.expenseByCategory.size)
        collectJob.cancel()
    }
    @Test
    fun `setPresetRange updates date range`() = runTest(testDispatcher) {
        val initialRange = viewModel.dateRange.value
        viewModel.setPresetRange(RangePreset.YEAR)
        advanceUntilIdle()
        val newRange = viewModel.dateRange.value
        assertTrue(newRange.from < initialRange.from)
        collectJob(testScheduler)
    }
    private fun collectJob(testScheduler: Any) {
        // Helper, no-op
    }
    @Test
    fun `expense by category groups correctly`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        fakeRepo.transactions.value = listOf(
            Transaction(id = 1, title = "Lunch", amount = 15.0, category = "Food", type = TransactionType.EXPENSE, timestamp = now),
            Transaction(id = 2, title = "Dinner", amount = 25.0, category = "Food", type = TransactionType.EXPENSE, timestamp = now),
            Transaction(id = 3, title = "Bus", amount = 5.0, category = "Transport", type = TransactionType.EXPENSE, timestamp = now),
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
        transactions.value = transactions.value.map { if (it.id == transaction.id) transaction else it }
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
}
