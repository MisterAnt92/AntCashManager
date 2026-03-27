package com.antcashmanager.android.ui.home

import com.antcashmanager.android.ui.screen.home.home.HomeViewModel
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
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeTransactionRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeTransactionRepository()
        viewModel = HomeViewModel(fakeRepo)
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
    fun `transactions reflect repository data`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        fakeRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Salary",
                amount = 2000.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = now,
            ),
            Transaction(
                id = 2,
                title = "Groceries",
                amount = 50.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = now,
            ),
        )

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.transactions.collect {}
        }
        advanceUntilIdle()

        assertEquals(2, viewModel.transactions.value.size)
        assertEquals("Salary", viewModel.transactions.value[0].title)
        assertEquals("Groceries", viewModel.transactions.value[1].title)
        collectJob.cancel()
    }

    @Test
    fun `transactions update when repository changes`() = runTest(testDispatcher) {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.transactions.collect {}
        }
        advanceUntilIdle()
        assertTrue(viewModel.transactions.value.isEmpty())

        val now = System.currentTimeMillis()
        fakeRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Bonus",
                amount = 500.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = now,
            ),
        )
        advanceUntilIdle()

        assertEquals(1, viewModel.transactions.value.size)
        assertEquals("Bonus", viewModel.transactions.value.first().title)
        collectJob.cancel()
    }

    @Test
    fun `transactions contain correct types`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        fakeRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Salary",
                amount = 3000.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = now,
            ),
            Transaction(
                id = 2,
                title = "Rent",
                amount = 800.0,
                category = "Housing",
                type = TransactionType.EXPENSE,
                timestamp = now,
            ),
        )

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.transactions.collect {}
        }
        advanceUntilIdle()

        val incomes = viewModel.transactions.value.filter { it.type == TransactionType.INCOME }
        val expenses = viewModel.transactions.value.filter { it.type == TransactionType.EXPENSE }
        assertEquals(1, incomes.size)
        assertEquals(1, expenses.size)
        assertEquals(3000.0, incomes.first().amount, 0.01)
        assertEquals(800.0, expenses.first().amount, 0.01)
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

