package com.antcashmanager.android.ui.home

import com.antcashmanager.android.ui.screen.home.HomeViewModel
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
// import kotlinx.coroutines.test.StandardTestDispatcher
// import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
    private val testDispatcher = Dispatchers.Default
    private lateinit var fakeRepo: FakeTransactionRepository
    private lateinit var fakeCategoryRepo: FakeCategoryRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeTransactionRepository()
        fakeCategoryRepo = FakeCategoryRepository()
        viewModel = HomeViewModel(
            transactionRepository = fakeRepo,
            categoryRepository = fakeCategoryRepo,
            dispatcher = testDispatcher,
            searchDebounceMs = 0L,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial transactions list is empty`() = runTest(testDispatcher) {
        val collectJob = launch {
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

        val collectJob = launch {
            viewModel.transactions.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(
            com.antcashmanager.android.ui.screen.home.HomeEvent.SetDateRange(
                0L,
                Long.MAX_VALUE
            )
        )
        advanceUntilIdle()

        assertEquals(2, viewModel.transactions.value.size)
        assertEquals("Salary", viewModel.transactions.value[0].title)
        assertEquals("Groceries", viewModel.transactions.value[1].title)
        collectJob.cancel()
    }

    @Test
    fun `transactions update when repository changes`() = runTest(testDispatcher) {
        val collectJob = launch {
            viewModel.transactions.collect {}
        }
        advanceUntilIdle()
        assertTrue(viewModel.transactions.value.isEmpty())

        viewModel.onEvent(
            com.antcashmanager.android.ui.screen.home.HomeEvent.SetDateRange(
                0L,
                Long.MAX_VALUE
            )
        )
        advanceUntilIdle()

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

        val collectJob = launch {
            viewModel.transactions.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(
            com.antcashmanager.android.ui.screen.home.HomeEvent.SetDateRange(
                0L,
                Long.MAX_VALUE
            )
        )
        advanceUntilIdle()

        val incomes = viewModel.transactions.value.filter { it.type == TransactionType.INCOME }
        val expenses = viewModel.transactions.value.filter { it.type == TransactionType.EXPENSE }
        assertEquals(1, incomes.size)
        assertEquals(1, expenses.size)
        assertEquals(3000.0, incomes.first().amount, 0.01)
        assertEquals(-800.0, expenses.first().amount, 0.01)
        collectJob.cancel()
    }

    @Test
    fun `show transaction details event sets selected transaction`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        val transaction = Transaction(
            id = 1,
            title = "Test Transaction",
            amount = 100.0,
            category = "Test",
            type = TransactionType.INCOME,
            timestamp = now,
        )
        fakeRepo.transactions.value = listOf(transaction)

        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(
            com.antcashmanager.android.ui.screen.home.HomeEvent.ShowTransactionDetails(
                transaction
            )
        )
        advanceUntilIdle()

        assertEquals(transaction, viewModel.state.value.selectedTransaction)
        collectJob.cancel()
    }

    @Test
    fun `dismiss transaction details clears selected transaction`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        val transaction = Transaction(
            id = 1,
            title = "Test Transaction",
            amount = 100.0,
            category = "Test",
            type = TransactionType.INCOME,
            timestamp = now,
        )
        fakeRepo.transactions.value = listOf(transaction)

        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(
            com.antcashmanager.android.ui.screen.home.HomeEvent.ShowTransactionDetails(
                transaction
            )
        )
        advanceUntilIdle()
        assertEquals(transaction, viewModel.state.value.selectedTransaction)

        viewModel.onEvent(com.antcashmanager.android.ui.screen.home.HomeEvent.DismissTransactionDetails)
        advanceUntilIdle()
        assertEquals(null, viewModel.state.value.selectedTransaction)
        collectJob.cancel()
    }

    @Test
    fun `balanceByPaymentType calculates correctly with mixed payment types`() =
        runTest(testDispatcher) {
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
                    title = "Cash Bonus",
                    amount = 300.0,
                    category = "Work",
                    type = TransactionType.INCOME,
                    timestamp = now,
                    paymentType = PaymentType.CASH,
                ),
                Transaction(
                    id = 3,
                    title = "Groceries",
                    amount = 150.0,  // Will become -150 after transformation
                    category = "Food",
                    type = TransactionType.EXPENSE,
                    timestamp = now,
                    paymentType = PaymentType.CASH,
                ),
                Transaction(
                    id = 4,
                    title = "Meal Voucher",
                    amount = 50.0,  // Will become -50 after transformation
                    category = "Food",
                    type = TransactionType.EXPENSE,
                    timestamp = now,
                    paymentType = PaymentType.MEAL_VOUCHERS,
                ),
            )

            val collectJob = launch {
                viewModel.state.collect {}
            }
            advanceUntilIdle()

            viewModel.onEvent(
                com.antcashmanager.android.ui.screen.home.HomeEvent.SetDateRange(
                    0L,
                    Long.MAX_VALUE
                )
            )
            advanceUntilIdle()

            val balanceByPaymentType = viewModel.state.value.balanceByPaymentType
            // ELECTRONIC: 2000 income = 2000
            assertEquals(2000.0, balanceByPaymentType[PaymentType.ELECTRONIC] ?: 0.0, 0.01)
            // CASH: 300 income - 150 expense = 150
            assertEquals(150.0, balanceByPaymentType[PaymentType.CASH] ?: 0.0, 0.01)
            // MEAL_VOUCHERS: 0 income - 50 expense = -50
            assertEquals(-50.0, balanceByPaymentType[PaymentType.MEAL_VOUCHERS] ?: 0.0, 0.01)
            collectJob.cancel()
        }

    @Test
    fun `balanceByPaymentType excludes zero values`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        fakeRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Income",
                amount = 100.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = now,
                paymentType = PaymentType.ELECTRONIC,
            ),
            Transaction(
                id = 2,
                title = "Expense",
                amount = 100.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = now,
                paymentType = PaymentType.ELECTRONIC,
            ),
        )

        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        val balanceByPaymentType = viewModel.state.value.balanceByPaymentType
        // ELECTRONIC should be 0 (100 - 100), so it should be excluded from map
        assertEquals(false, balanceByPaymentType.containsKey(PaymentType.ELECTRONIC))
        collectJob.cancel()
    }

    @Test
    fun `balanceByPaymentType is empty when no transactions`() = runTest(testDispatcher) {
        fakeRepo.transactions.value = emptyList()

        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        val balanceByPaymentType = viewModel.state.value.balanceByPaymentType
        assertTrue(balanceByPaymentType.isEmpty())
        collectJob.cancel()
    }

    @Test
    fun `date filter affects balanceByPaymentType calculation`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        val yesterday = now - (24 * 60 * 60 * 1000)
        val lastWeek = now - (7 * 24 * 60 * 60 * 1000)

        fakeRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Recent Income",
                amount = 1000.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = yesterday,
                paymentType = PaymentType.ELECTRONIC,
            ),
            Transaction(
                id = 2,
                title = "Old Income",
                amount = 500.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = lastWeek - (10 * 24 * 60 * 60 * 1000),
                paymentType = PaymentType.CASH,
            ),
        )

        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        // Default filter is last 7 days, so only the recent transaction should be included
        val balanceByPaymentType = viewModel.state.value.balanceByPaymentType
        assertEquals(1000.0, balanceByPaymentType[PaymentType.ELECTRONIC] ?: 0.0, 0.01)
        assertEquals(false, balanceByPaymentType.containsKey(PaymentType.CASH))
        collectJob.cancel()
    }

    @Test
    fun `totals and balance are calculated correctly for home cards`() = runTest(testDispatcher) {
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
                title = "Freelance",
                amount = 500.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = now,
            ),
            Transaction(
                id = 3,
                title = "Rent",
                amount = 800.0,
                category = "Home",
                type = TransactionType.EXPENSE,
                timestamp = now,
            ),
            Transaction(
                id = 4,
                title = "Groceries",
                amount = 200.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = now,
            ),
        )

        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(
            com.antcashmanager.android.ui.screen.home.HomeEvent.SetDateRange(
                0L,
                Long.MAX_VALUE
            )
        )
        advanceUntilIdle()

        val uiState = viewModel.state.value
        assertEquals(2500.0, uiState.totalIncome, 0.01)
        assertEquals(-1000.0, uiState.totalExpense, 0.01)
        assertEquals(1500.0, uiState.balance, 0.01)

        collectJob.cancel()
    }

    @Test
    fun `amount signs are normalized before totals`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        fakeRepo.transactions.value = listOf(
            // Wrong sign for INCOME in storage -> should be normalized to positive
            Transaction(
                id = 1,
                title = "Refund",
                amount = -120.0,
                category = "Other",
                type = TransactionType.INCOME,
                timestamp = now,
            ),
            // Wrong sign for EXPENSE in storage -> should be normalized to negative
            Transaction(
                id = 2,
                title = "Coffee",
                amount = 20.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = now,
            ),
        )

        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(
            com.antcashmanager.android.ui.screen.home.HomeEvent.SetDateRange(
                0L,
                Long.MAX_VALUE
            )
        )
        advanceUntilIdle()

        val uiState = viewModel.state.value
        assertEquals(120.0, uiState.totalIncome, 0.01)
        assertEquals(-20.0, uiState.totalExpense, 0.01)
        assertEquals(100.0, uiState.balance, 0.01)

        collectJob.cancel()
    }

    @Test
    fun `set date range updates totals based on selected period`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        val old = now - (20L * 24 * 60 * 60 * 1000)
        val from = now - (2L * 24 * 60 * 60 * 1000)

        fakeRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Recent Income",
                amount = 1000.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = now,
            ),
            Transaction(
                id = 2,
                title = "Recent Expense",
                amount = 300.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = now,
            ),
            Transaction(
                id = 3,
                title = "Old Income",
                amount = 900.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = old,
            ),
        )

        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(
            com.antcashmanager.android.ui.screen.home.HomeEvent.SetDateRange(
                from,
                now
            )
        )
        advanceUntilIdle()

        val uiState = viewModel.state.value
        assertEquals(1000.0, uiState.totalIncome, 0.01)
        assertEquals(-300.0, uiState.totalExpense, 0.01)
        assertEquals(700.0, uiState.balance, 0.01)

        collectJob.cancel()
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

        override suspend fun updateCategoryData(categoryName: String, icon: String, color: Long) {
            // No-op for test
        }

        override fun getDistinctTitles() = flowOf(emptyList<String>())
        override fun getDistinctPayees() = flowOf(emptyList<String>())
        override fun getDistinctNotes() = flowOf(emptyList<String>())
        override fun getDistinctLocations() = flowOf(emptyList<String>())
        override fun getDistinctTags() = flowOf(emptyList<String>())
    }

    private class FakeCategoryRepository : com.antcashmanager.domain.repository.CategoryRepository {
        val categories =
            MutableStateFlow<List<com.antcashmanager.domain.model.Category>>(emptyList())

        override fun getAllCategories(): Flow<List<com.antcashmanager.domain.model.Category>> =
            categories

        override suspend fun getCategoryById(id: Long): com.antcashmanager.domain.model.Category? =
            categories.value.find { it.id == id }

        override suspend fun getCategoryByName(name: String): com.antcashmanager.domain.model.Category? =
            categories.value.find { it.name == name }

        override suspend fun insertCategory(category: com.antcashmanager.domain.model.Category): Long {
            categories.value += category
            return category.id
        }

        override suspend fun updateCategory(category: com.antcashmanager.domain.model.Category) {
            categories.value = categories.value.map { if (it.id == category.id) category else it }
        }

        override suspend fun deleteCategory(category: com.antcashmanager.domain.model.Category) {
            categories.value = categories.value.filter { it.id != category.id }
        }

        override suspend fun deleteAllCategories() {
            categories.value = emptyList()
        }

        override fun getCategoriesByType(type: String): Flow<List<com.antcashmanager.domain.model.Category>> =
            categories.map { list -> list.filter { it.type == type } }

        override suspend fun getDefaultCategoryCount(): Int =
            categories.value.count { it.isDefault }
    }
}
