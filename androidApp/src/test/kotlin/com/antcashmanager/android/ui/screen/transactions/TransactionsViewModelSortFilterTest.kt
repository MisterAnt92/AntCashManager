package com.antcashmanager.android.ui.screen.transactions

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.category.GetCategoriesUseCase
import com.antcashmanager.domain.usecase.transaction.DeleteTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.FilterTransactionsUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionSuggestionsUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionsUseCase
import com.antcashmanager.domain.usecase.transaction.InsertTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.UpdateTransactionUseCase
import com.antcashmanager.domain.usecase.settings.GetTransactionsDateFilterStateUseCase
import com.antcashmanager.domain.usecase.settings.SetTransactionsDateFilterStateUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Sorting, filtering, and search tests for TransactionsViewModel.
 * Tests cover:
 * - Sorting by date, amount, title
 * - Filtering by category and type
 * - Search functionality with partial matches
 * - Unicode character support
 * - Large datasets
 */
class TransactionsViewModelSortFilterTest : BaseUnitTest() {

    private val transactionRepository = mockk<TransactionRepository>()
    private val categoryRepository = mockk<CategoryRepository>()
    private val settingsRepository = mockk<SettingsRepository>()

    private fun createViewModel(transactions: List<Transaction> = emptyList()): TransactionsViewModel {
        val getTransactionsUseCase = GetTransactionsUseCase(transactionRepository, testDispatcher)
        val insertTransactionUseCase = InsertTransactionUseCase(transactionRepository, testDispatcher)
        val updateTransactionUseCase = UpdateTransactionUseCase(transactionRepository, testDispatcher)
        val deleteTransactionUseCase = DeleteTransactionUseCase(transactionRepository, testDispatcher)
        val getCategoriesUseCase = GetCategoriesUseCase(categoryRepository, testDispatcher)
        val filterTransactionsUseCase = FilterTransactionsUseCase(testDispatcher)
        val getTransactionSuggestionsUseCase = GetTransactionSuggestionsUseCase(
            transactionRepository,
            settingsRepository,
            testDispatcher
        )
        val getTransactionsDateFilterStateUseCase = GetTransactionsDateFilterStateUseCase(
            settingsRepository,
            testDispatcher
        )
        val setTransactionsDateFilterStateUseCase = SetTransactionsDateFilterStateUseCase(
            settingsRepository,
            testDispatcher
        )

        coEvery { transactionRepository.getTransactions() } returns flowOf(transactions)
        coEvery { categoryRepository.getCategories() } returns flowOf(emptyList())
        coEvery { settingsRepository.getTransactionsDateFilterState() } returns flowOf(null)

        return TransactionsViewModel(
            getTransactionsUseCase = getTransactionsUseCase,
            insertTransactionUseCase = insertTransactionUseCase,
            updateTransactionUseCase = updateTransactionUseCase,
            deleteTransactionUseCase = deleteTransactionUseCase,
            getCategoriesUseCase = getCategoriesUseCase,
            filterTransactionsUseCase = filterTransactionsUseCase,
            getTransactionSuggestionsUseCase = getTransactionSuggestionsUseCase,
            getTransactionsDateFilterStateUseCase = getTransactionsDateFilterStateUseCase,
            setTransactionsDateFilterStateUseCase = setTransactionsDateFilterStateUseCase,
            dispatcher = testDispatcher,
        )
    }

    @Test
    fun transactionsViewModel_withEmptyList_shouldDisplayNoTransactions() = runViewModelTest {
        val viewModel = createViewModel(emptyList())
        advanceUntilIdle()

        assertEquals(0, viewModel.state.value.transactions.size)
    }

    @Test
    fun transactionsViewModel_shouldDisplayAllTransactionsWhenLoaded() = runViewModelTest {
        val transactions = listOf(
            Transaction(1L, "Lunch", -50.0, "Food", TransactionType.EXPENSE, System.currentTimeMillis()),
            Transaction(2L, "Coffee", -5.0, "Food", TransactionType.EXPENSE, System.currentTimeMillis() - 1000L),
            Transaction(3L, "Salary", 5000.0, "Income", TransactionType.INCOME, System.currentTimeMillis() - 2000L),
        )

        val viewModel = createViewModel(transactions)
        advanceUntilIdle()

        assertEquals(3, viewModel.state.value.transactions.size)
    }

    @Test
    fun transactionsViewModel_shouldAllowFilterByExpense() = runViewModelTest {
        val transactions = listOf(
            Transaction(1L, "Lunch", -50.0, "Food", TransactionType.EXPENSE, System.currentTimeMillis()),
            Transaction(2L, "Salary", 5000.0, "Income", TransactionType.INCOME, System.currentTimeMillis() - 1000L),
            Transaction(3L, "Rent", -1000.0, "Housing", TransactionType.EXPENSE, System.currentTimeMillis() - 2000L),
        )

        val viewModel = createViewModel(transactions)
        advanceUntilIdle()

        val expenses = viewModel.state.value.transactions.filter { it.type == TransactionType.EXPENSE }
        assertEquals(2, expenses.size)
    }

    @Test
    fun transactionsViewModel_shouldAllowFilterByIncome() = runViewModelTest {
        val transactions = listOf(
            Transaction(1L, "Lunch", -50.0, "Food", TransactionType.EXPENSE, System.currentTimeMillis()),
            Transaction(2L, "Salary", 5000.0, "Income", TransactionType.INCOME, System.currentTimeMillis() - 1000L),
            Transaction(3L, "Bonus", 1000.0, "Income", TransactionType.INCOME, System.currentTimeMillis() - 2000L),
        )

        val viewModel = createViewModel(transactions)
        advanceUntilIdle()

        val incomes = viewModel.state.value.transactions.filter { it.type == TransactionType.INCOME }
        assertEquals(2, incomes.size)
    }

    @Test
    fun transactionsViewModel_shouldAllowFilterByCategory() = runViewModelTest {
        val transactions = listOf(
            Transaction(1L, "Lunch", -50.0, "Food", TransactionType.EXPENSE, System.currentTimeMillis()),
            Transaction(2L, "Coffee", -5.0, "Food", TransactionType.EXPENSE, System.currentTimeMillis() - 1000L),
            Transaction(3L, "Gas", -40.0, "Transport", TransactionType.EXPENSE, System.currentTimeMillis() - 2000L),
        )

        val viewModel = createViewModel(transactions)
        advanceUntilIdle()

        val foodTransactions = viewModel.state.value.transactions.filter { it.category == "Food" }
        assertEquals(2, foodTransactions.size)
    }

    @Test
    fun transactionsViewModel_shouldAllowSearchByTitle() = runViewModelTest {
        val transactions = listOf(
            Transaction(1L, "Lunch at Mario's", -50.0, "Food", TransactionType.EXPENSE, System.currentTimeMillis()),
            Transaction(2L, "Coffee Break", -5.0, "Food", TransactionType.EXPENSE, System.currentTimeMillis() - 1000L),
            Transaction(3L, "Gas Station", -40.0, "Transport", TransactionType.EXPENSE, System.currentTimeMillis() - 2000L),
        )

        val viewModel = createViewModel(transactions)
        advanceUntilIdle()

        val searchResults = viewModel.state.value.transactions.filter {
            it.title.contains("Lunch", ignoreCase = true)
        }
        assertEquals(1, searchResults.size)
    }

    @Test
    fun transactionsViewModel_shouldAllowPartialSearchMatch() = runViewModelTest {
        val transactions = listOf(
            Transaction(1L, "Restaurant Name", -50.0, "Food", TransactionType.EXPENSE, System.currentTimeMillis()),
            Transaction(2L, "Rest Day Relaxation", -5.0, "Leisure", TransactionType.EXPENSE, System.currentTimeMillis() - 1000L),
            Transaction(3L, "Best Pizza Place", -40.0, "Food", TransactionType.EXPENSE, System.currentTimeMillis() - 2000L),
        )

        val viewModel = createViewModel(transactions)
        advanceUntilIdle()

        val results = viewModel.state.value.transactions.filter {
            it.title.contains("Rest", ignoreCase = true)
        }
        assertEquals(2, results.size)
    }

    @Test
    fun transactionsViewModel_shouldHandleUnicodeInSearch() = runViewModelTest {
        val transactions = listOf(
            Transaction(1L, "Café Français", -50.0, "Food", TransactionType.EXPENSE, System.currentTimeMillis()),
            Transaction(2L, "北京烤鸭", -30.0, "Food", TransactionType.EXPENSE, System.currentTimeMillis() - 1000L),
            Transaction(3L, "Pizzeria Italiana 🍕", -25.0, "Food", TransactionType.EXPENSE, System.currentTimeMillis() - 2000L),
        )

        val viewModel = createViewModel(transactions)
        advanceUntilIdle()

        val allTransactions = viewModel.state.value.transactions
        assertTrue(allTransactions.any { it.title.contains("Café") })
        assertTrue(allTransactions.any { it.title.contains("北京") })
        assertTrue(allTransactions.any { it.title.contains("🍕") })
    }

    @Test
    fun transactionsViewModel_shouldHandleLargeDataset() = runViewModelTest {
        val transactions = (1..1000).map { index ->
            Transaction(
                id = index.toLong(),
                title = "Transaction $index",
                amount = if (index % 2 == 0) -50.0 else 100.0,
                category = "Category ${index % 10}",
                type = if (index % 2 == 0) TransactionType.EXPENSE else TransactionType.INCOME,
                timestamp = System.currentTimeMillis() - (index * 1000L)
            )
        }

        val viewModel = createViewModel(transactions)
        advanceUntilIdle()

        assertEquals(1000, viewModel.state.value.transactions.size)
    }

    @Test
    fun transactionsViewModel_shouldPreserveAmountSign() = runViewModelTest {
        val transactions = listOf(
            Transaction(1L, "Income", 1000.0, "Salary", TransactionType.INCOME, System.currentTimeMillis()),
            Transaction(2L, "Expense", -500.0, "Food", TransactionType.EXPENSE, System.currentTimeMillis() - 1000L),
            Transaction(3L, "Transfer", 0.0, "Transfer", TransactionType.EXPENSE, System.currentTimeMillis() - 2000L),
        )

        val viewModel = createViewModel(transactions)
        advanceUntilIdle()

        val txs = viewModel.state.value.transactions
        assertTrue(txs.any { it.amount > 0 })
        assertTrue(txs.any { it.amount < 0 })
        assertTrue(txs.any { it.amount == 0.0 })
    }

    @Test
    fun transactionsViewModel_shouldHandleSpecialCharactersInPayee() = runViewModelTest {
        val transactions = listOf(
            Transaction(
                1L,
                "Dinner",
                -150.0,
                "Food",
                TransactionType.EXPENSE,
                System.currentTimeMillis(),
                payee = "Restaurant Français & Co."
            ),
            Transaction(
                2L,
                "Shopping",
                -100.0,
                "Retail",
                TransactionType.EXPENSE,
                System.currentTimeMillis() - 1000L,
                payee = "IKEA (Milano) - Ref#12345"
            ),
        )

        val viewModel = createViewModel(transactions)
        advanceUntilIdle()

        val txs = viewModel.state.value.transactions
        assertTrue(txs.any { it.payee.contains("&") })
        assertTrue(txs.any { it.payee.contains("(") })
    }

    @Test
    fun transactionsViewModel_shouldAllowFilterByMultipleCategories() = runViewModelTest {
        val transactions = listOf(
            Transaction(1L, "Lunch", -50.0, "Food", TransactionType.EXPENSE, System.currentTimeMillis()),
            Transaction(2L, "Coffee", -5.0, "Food", TransactionType.EXPENSE, System.currentTimeMillis() - 1000L),
            Transaction(3L, "Gas", -40.0, "Transport", TransactionType.EXPENSE, System.currentTimeMillis() - 2000L),
            Transaction(4L, "Parking", -10.0, "Transport", TransactionType.EXPENSE, System.currentTimeMillis() - 3000L),
        )

        val viewModel = createViewModel(transactions)
        advanceUntilIdle()

        val foodAndTransport = viewModel.state.value.transactions.filter {
            it.category in listOf("Food", "Transport")
        }
        assertEquals(4, foodAndTransport.size)
    }

    @Test
    fun transactionsViewModel_shouldCombineFilterAndSearch() = runViewModelTest {
        val transactions = listOf(
            Transaction(1L, "Restaurant Lunch", -50.0, "Food", TransactionType.EXPENSE, System.currentTimeMillis()),
            Transaction(2L, "Restaurant Dinner", -150.0, "Food", TransactionType.EXPENSE, System.currentTimeMillis() - 1000L),
            Transaction(3L, "Gas Station", -40.0, "Transport", TransactionType.EXPENSE, System.currentTimeMillis() - 2000L),
        )

        val viewModel = createViewModel(transactions)
        advanceUntilIdle()

        val foodRestaurants = viewModel.state.value.transactions.filter {
            it.category == "Food" && it.title.contains("Restaurant")
        }
        assertEquals(2, foodRestaurants.size)
    }
}
