package com.antcashmanager.android.ui.transactions

import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.CategoryRepository
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
        viewModel = TransactionsViewModel(fakeTransactionRepo, fakeCategoryRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial transactions list is empty`() = runTest(testDispatcher) {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.transactions.collect {}
        }
        advanceUntilIdle()

        assertTrue(viewModel.transactions.value.isEmpty())
        collectJob.cancel()
    }

    @Test
    fun `initial categories list is empty`() = runTest(testDispatcher) {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.categories.collect {}
        }
        advanceUntilIdle()

        assertTrue(viewModel.categories.value.isEmpty())
        collectJob.cancel()
    }

    @Test
    fun `addTransaction adds a new transaction`() = runTest(testDispatcher) {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.transactions.collect {}
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

        assertEquals(1, viewModel.transactions.value.size)
        assertEquals("Lunch", viewModel.transactions.value.first().title)
        assertEquals(15.0, viewModel.transactions.value.first().amount, 0.01)
        assertEquals(TransactionType.EXPENSE, viewModel.transactions.value.first().type)
        collectJob.cancel()
    }

    @Test
    fun `deleteTransaction removes transaction`() = runTest(testDispatcher) {
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

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.transactions.collect {}
        }
        advanceUntilIdle()
        assertEquals(1, viewModel.transactions.value.size)

        viewModel.deleteTransaction(transaction)
        advanceUntilIdle()

        assertTrue(viewModel.transactions.value.isEmpty())
        collectJob.cancel()
    }

    @Test
    fun `updateTransaction updates existing transaction`() = runTest(testDispatcher) {
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

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.transactions.collect {}
        }
        advanceUntilIdle()

        val updated = transaction.copy(title = "Espresso", amount = 2.5)
        viewModel.updateTransaction(updated)
        advanceUntilIdle()

        assertEquals(1, viewModel.transactions.value.size)
        assertEquals("Espresso", viewModel.transactions.value.first().title)
        assertEquals(2.5, viewModel.transactions.value.first().amount, 0.01)
        collectJob.cancel()
    }

    @Test
    fun `categories reflect repository data`() = runTest(testDispatcher) {
        fakeCategoryRepo.categories.value = listOf(
            Category(id = 1, name = "Food", icon = "restaurant", color = 0xFFE57373),
            Category(id = 2, name = "Transport", icon = "directions_bus", color = 0xFF81C784),
        )

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.categories.collect {}
        }
        advanceUntilIdle()

        assertEquals(2, viewModel.categories.value.size)
        assertEquals("Food", viewModel.categories.value[0].name)
        assertEquals("Transport", viewModel.categories.value[1].name)
        collectJob.cancel()
    }

    @Test
    fun `addTransaction with income type`() = runTest(testDispatcher) {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.transactions.collect {}
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

        assertEquals(1, viewModel.transactions.value.size)
        assertEquals(TransactionType.INCOME, viewModel.transactions.value.first().type)
        assertEquals(3000.0, viewModel.transactions.value.first().amount, 0.01)
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
}

