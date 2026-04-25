package com.antcashmanager.android.ui.transactions

import com.antcashmanager.android.ui.screen.transactions.TransactionsViewModel
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeTransactionRepo: FakeTransactionRepository
    private lateinit var fakeCategoryRepo: FakeCategoryRepository
    private lateinit var viewModel: TransactionsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeTransactionRepo = FakeTransactionRepository()
        fakeCategoryRepo = FakeCategoryRepository()
        viewModel = TransactionsViewModel(fakeTransactionRepo, fakeCategoryRepo, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial transactions list is empty`() = runTest(testDispatcher) {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        assertTrue(viewModel.state.value.transactions.isEmpty())
        collectJob.cancel()
    }

    @Test
    fun `initial categories list is empty`() = runTest(testDispatcher) {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        assertTrue(viewModel.state.value.categories.isEmpty())
        collectJob.cancel()
    }

    @Test
    fun `addTransaction adds a new transaction`() = runTest(testDispatcher) {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        val now = System.currentTimeMillis()
        viewModel.addTransaction(
            title = "Lunch",
            amount = 15.0,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = now,
        )
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.transactions.size)
        assertEquals("Lunch", viewModel.state.value.transactions.first().title)
        assertEquals(-15.0, viewModel.state.value.transactions.first().amount, 0.01)
        assertEquals(TransactionType.EXPENSE, viewModel.state.value.transactions.first().type)
        collectJob.cancel()
    }

    @Test
    fun `deleteTransaction removes transaction`() = runTest(testDispatcher) {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        val now = System.currentTimeMillis()
        val transaction = Transaction(
            id = 1,
            title = "Coffee",
            amount = 3.0,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = now,
        )
        fakeTransactionRepo.transactions.value = listOf(transaction)
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.transactions.size)

        viewModel.deleteTransaction(transaction)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.transactions.isEmpty())
        collectJob.cancel()
    }

    @Test
    fun `updateTransaction updates existing transaction`() = runTest(testDispatcher) {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        val now = System.currentTimeMillis()
        val transaction = Transaction(
            id = 1,
            title = "Coffee",
            amount = 3.0,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = now,
        )
        fakeTransactionRepo.transactions.value = listOf(transaction)
        advanceUntilIdle()

        val updated = transaction.copy(title = "Espresso", amount = 2.5)
        viewModel.updateTransaction(updated)
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.transactions.size)
        assertEquals("Espresso", viewModel.state.value.transactions.first().title)
        assertEquals(-2.5, viewModel.state.value.transactions.first().amount, 0.01)
        collectJob.cancel()
    }

    @Test
    fun `categories reflect repository data`() = runTest(testDispatcher) {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        fakeCategoryRepo.categories.value = listOf(
            Category(id = 1, name = "Food", icon = "restaurant", color = 0xFFE57373),
            Category(id = 2, name = "Transport", icon = "directions_bus", color = 0xFF81C784),
        )
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.categories.size)
        assertEquals("Food", viewModel.state.value.categories[0].name)
        assertEquals("Transport", viewModel.state.value.categories[1].name)
        collectJob.cancel()
    }

    @Test
    fun `addTransaction with income type`() = runTest(testDispatcher) {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        val now = System.currentTimeMillis()
        viewModel.addTransaction(
            title = "Salary",
            amount = 3000.0,
            category = "Work",
            type = TransactionType.INCOME,
            timestamp = now,
        )
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.transactions.size)
        assertEquals(TransactionType.INCOME, viewModel.state.value.transactions.first().type)
        assertEquals(3000.0, viewModel.state.value.transactions.first().amount, 0.01)
        collectJob.cancel()
    }

    @Test
    fun `search query filters transactions by title immediately`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        fakeTransactionRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Salary April",
                amount = 2500.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = now,
            ),
            Transaction(
                id = 2,
                title = "Groceries",
                amount = 85.50,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = now,
            ),
        )

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        // Ensure date range includes all data
        viewModel.onEvent(
            com.antcashmanager.android.ui.screen.transactions.TransactionsEvent.SetDateRange(
                0L,
                Long.MAX_VALUE,
            )
        )
        advanceUntilIdle()

        viewModel.onEvent(
            com.antcashmanager.android.ui.screen.transactions.TransactionsEvent.UpdateSearchQuery("salary")
        )
        advanceUntilIdle()

        assertEquals("salary", viewModel.state.value.searchQuery)
        assertEquals(1, viewModel.state.value.filteredTransactions.size)
        assertEquals("Salary April", viewModel.state.value.filteredTransactions.first().title)

        collectJob.cancel()
    }

    @Test
    fun `search query matches amount with comma separator`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        fakeTransactionRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Groceries",
                amount = 85.50,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = now,
            ),
            Transaction(
                id = 2,
                title = "Salary",
                amount = 2500.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = now,
            ),
        )

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(
            com.antcashmanager.android.ui.screen.transactions.TransactionsEvent.SetDateRange(
                0L,
                Long.MAX_VALUE,
            )
        )
        advanceUntilIdle()

        viewModel.onEvent(
            com.antcashmanager.android.ui.screen.transactions.TransactionsEvent.UpdateSearchQuery("85,5")
        )
        advanceUntilIdle()

        assertFalse(viewModel.state.value.filteredTransactions.isEmpty())
        assertEquals("Groceries", viewModel.state.value.filteredTransactions.first().title)

        collectJob.cancel()
    }
}

// ── Fake Repositories ──

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
        transactions.value = transactions.value.map {
            if (it.id == transaction.id) transaction else it
        }
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

private class FakeCategoryRepository : CategoryRepository {
    val categories = MutableStateFlow<List<Category>>(emptyList())

    override fun getAllCategories(): Flow<List<Category>> = categories

    override suspend fun getCategoryById(id: Long): Category? =
        categories.value.find { it.id == id }

    override suspend fun insertCategory(category: Category): Long {
        categories.value = categories.value + category
        return category.id
    }

    override suspend fun updateCategory(category: Category) {
        categories.value = categories.value.map {
            if (it.id == category.id) category else it
        }
    }

    override suspend fun deleteCategory(category: Category) {
        categories.value = categories.value.filter { it.id != category.id }
    }

    override suspend fun deleteAllCategories() {
        categories.value = emptyList()
    }

    override fun getCategoriesByType(type: String): Flow<List<Category>> =
        categories.map { list -> list.filter { it.type == type } }

    override suspend fun getDefaultCategoryCount(): Int =
        categories.value.count { it.isDefault }

    override suspend fun getCategoryByName(name: String): Category? =
        categories.value.find { it.name == name }
}
