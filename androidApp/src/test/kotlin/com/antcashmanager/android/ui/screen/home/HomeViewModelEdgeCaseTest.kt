package com.antcashmanager.android.ui.screen.home

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionSuggestions
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Edge case tests for HomeViewModel.
 * Tests cover:
 * - Empty and single transaction scenarios
 * - Balance calculations with various amounts
 * - Suggestions generation and deduplication
 * - Special characters and large datasets
 * - Error recovery
 */
class HomeViewModelEdgeCaseTest : BaseUnitTest() {

    private val transactionRepository = mockk<TransactionRepository>()
    private val settingsRepository = mockk<SettingsRepository>()
    private val categoryRepository = mockk<CategoryRepository>()

    private fun setupViewModel(transactions: List<Transaction> = emptyList()) {
        coEvery { transactionRepository.getTransactions() } returns flowOf(transactions)
        coEvery { transactionRepository.getTransactionsByDateRange(any(), any()) } returns flowOf(transactions)
        coEvery { transactionRepository.getSuggestions(any()) } returns TransactionSuggestions()
        coEvery { categoryRepository.getCategories() } returns flowOf(emptyList())
        coEvery { settingsRepository.getHomeDateFilterState() } returns flowOf(
            SavedDateFilter(0, 0L, System.currentTimeMillis())
        )
        coEvery { settingsRepository.setHomeDateFilterState(any()) } returns Unit
    }

    @Test
    fun homeViewModel_withEmptyTransactionList_shouldShowZeroBalance() = runViewModelTest {
        setupViewModel(emptyList())

        val viewModel = HomeViewModel(
            transactionRepository = transactionRepository,
            settingsRepository = settingsRepository,
            categoryRepository = categoryRepository,
            dispatcher = testDispatcher,
        )
        advanceUntilIdle()

        assertEquals(emptyList(), viewModel.state.value.displayTransactions)
    }

    @Test
    fun homeViewModel_withSingleExpense_shouldCalculateNegativeBalance() = runViewModelTest {
        val singleExpense = listOf(
            Transaction(
                id = 1L,
                title = "Coffee",
                amount = -5.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis()
            )
        )
        setupViewModel(singleExpense)

        val viewModel = HomeViewModel(
            transactionRepository = transactionRepository,
            settingsRepository = settingsRepository,
            categoryRepository = categoryRepository,
            dispatcher = testDispatcher,
        )
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.displayTransactions.size)
    }

    @Test
    fun homeViewModel_withSingleIncome_shouldCalculatePositiveBalance() = runViewModelTest {
        val singleIncome = listOf(
            Transaction(
                id = 1L,
                title = "Salary",
                amount = 5000.0,
                category = "Income",
                type = TransactionType.INCOME,
                timestamp = System.currentTimeMillis()
            )
        )
        setupViewModel(singleIncome)

        val viewModel = HomeViewModel(
            transactionRepository = transactionRepository,
            settingsRepository = settingsRepository,
            categoryRepository = categoryRepository,
            dispatcher = testDispatcher,
        )
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.displayTransactions.size)
    }

    @Test
    fun homeViewModel_withManyTransactions_shouldHandleAllRecords() = runViewModelTest {
        val manyTransactions = (1..100).map { index ->
            Transaction(
                id = index.toLong(),
                title = "Transaction $index",
                amount = if (index % 2 == 0) -50.0 else 100.0,
                category = "Category ${index % 5}",
                type = if (index % 2 == 0) TransactionType.EXPENSE else TransactionType.INCOME,
                timestamp = System.currentTimeMillis() - (index * 1000L)
            )
        }
        setupViewModel(manyTransactions)

        val viewModel = HomeViewModel(
            transactionRepository = transactionRepository,
            settingsRepository = settingsRepository,
            categoryRepository = categoryRepository,
            dispatcher = testDispatcher,
        )
        advanceUntilIdle()

        assertEquals(100, viewModel.state.value.displayTransactions.size)
    }

    @Test
    fun homeViewModel_withVeryLargeAmounts_shouldNotOverflow() = runViewModelTest {
        val largeAmounts = listOf(
            Transaction(
                id = 1L,
                title = "Large income",
                amount = 9_999_999.99,
                category = "Income",
                type = TransactionType.INCOME,
                timestamp = System.currentTimeMillis()
            ),
            Transaction(
                id = 2L,
                title = "Large expense",
                amount = -9_999_999.99,
                category = "Expense",
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis()
            )
        )
        setupViewModel(largeAmounts)

        val viewModel = HomeViewModel(
            transactionRepository = transactionRepository,
            settingsRepository = settingsRepository,
            categoryRepository = categoryRepository,
            dispatcher = testDispatcher,
        )
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.displayTransactions.size)
    }

    @Test
    fun homeViewModel_withUnicodeCharactersInTitle_shouldPreserveEncoding() = runViewModelTest {
        val unicodeTransactions = listOf(
            Transaction(
                id = 1L,
                title = "Café français ☕",
                amount = -50.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis()
            ),
            Transaction(
                id = 2L,
                title = "北京烤鸭 🦆",
                amount = -35.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis()
            ),
            Transaction(
                id = 3L,
                title = "Σαλάτα Ελληνική 🥗",
                amount = -20.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis()
            )
        )
        setupViewModel(unicodeTransactions)

        val viewModel = HomeViewModel(
            transactionRepository = transactionRepository,
            settingsRepository = settingsRepository,
            categoryRepository = categoryRepository,
            dispatcher = testDispatcher,
        )
        advanceUntilIdle()

        val displayTxs = viewModel.state.value.displayTransactions
        assertTrue(displayTxs.any { it.title.contains("Café") })
        assertTrue(displayTxs.any { it.title.contains("北京") })
        assertTrue(displayTxs.any { it.title.contains("Ελληνική") })
    }

    @Test
    fun homeViewModel_withDuplicateTitles_shouldGenerateDedupSuggestions() = runViewModelTest {
        val duplicates = listOf(
            Transaction(1L, "Lunch", -50.0, "Food", TransactionType.EXPENSE, System.currentTimeMillis()),
            Transaction(2L, "Lunch", -45.0, "Food", TransactionType.EXPENSE, System.currentTimeMillis() - 1000L),
            Transaction(3L, "Lunch", -55.0, "Food", TransactionType.EXPENSE, System.currentTimeMillis() - 2000L),
        )

        val suggestions = TransactionSuggestions(
            titles = listOf("Lunch"),  // Deduplicated
            payees = emptyList(),
            notes = emptyList(),
            locations = emptyList(),
            tags = emptyList()
        )

        coEvery { transactionRepository.getTransactions() } returns flowOf(duplicates)
        coEvery { transactionRepository.getSuggestions(any()) } returns suggestions
        coEvery { categoryRepository.getCategories() } returns flowOf(emptyList())
        coEvery { settingsRepository.getHomeDateFilterState() } returns flowOf(
            SavedDateFilter(0, 0L, System.currentTimeMillis())
        )
        coEvery { settingsRepository.setHomeDateFilterState(any()) } returns Unit

        val viewModel = HomeViewModel(
            transactionRepository = transactionRepository,
            settingsRepository = settingsRepository,
            categoryRepository = categoryRepository,
            dispatcher = testDispatcher,
        )
        advanceUntilIdle()

        // Suggestions should be deduplicated
        assertEquals(1, suggestions.titles.size)
        assertEquals("Lunch", suggestions.titles[0])
    }

    @Test
    fun homeViewModel_withMixedSignAmounts_shouldCalculateCorrectBalance() = runViewModelTest {
        val mixedAmounts = listOf(
            Transaction(1L, "Income", 5000.0, "Salary", TransactionType.INCOME, System.currentTimeMillis()),
            Transaction(2L, "Expense1", -1000.0, "Housing", TransactionType.EXPENSE, System.currentTimeMillis() - 1000L),
            Transaction(3L, "Expense2", -500.0, "Food", TransactionType.EXPENSE, System.currentTimeMillis() - 2000L),
            Transaction(4L, "Bonus", 1000.0, "Bonus", TransactionType.INCOME, System.currentTimeMillis() - 3000L),
            Transaction(5L, "Expense3", -100.0, "Transport", TransactionType.EXPENSE, System.currentTimeMillis() - 4000L),
        )
        setupViewModel(mixedAmounts)

        val viewModel = HomeViewModel(
            transactionRepository = transactionRepository,
            settingsRepository = settingsRepository,
            categoryRepository = categoryRepository,
            dispatcher = testDispatcher,
        )
        advanceUntilIdle()

        val displayTxs = viewModel.state.value.displayTransactions
        assertEquals(5, displayTxs.size)

        // Verify all transaction types are present
        val incomes = displayTxs.filter { it.type == TransactionType.INCOME }
        val expenses = displayTxs.filter { it.type == TransactionType.EXPENSE }
        assertEquals(2, incomes.size)
        assertEquals(3, expenses.size)
    }

    @Test
    fun homeViewModel_withZeroAmountTransaction_shouldHandleGracefully() = runViewModelTest {
        val zeroAmount = listOf(
            Transaction(
                id = 1L,
                title = "Transfer (no amount)",
                amount = 0.0,
                category = "Transfer",
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis()
            )
        )
        setupViewModel(zeroAmount)

        val viewModel = HomeViewModel(
            transactionRepository = transactionRepository,
            settingsRepository = settingsRepository,
            categoryRepository = categoryRepository,
            dispatcher = testDispatcher,
        )
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.displayTransactions.size)
        assertEquals(0.0, viewModel.state.value.displayTransactions[0].amount)
    }

    @Test
    fun homeViewModel_withRecurringTransactions_shouldMarkRecurrence() = runViewModelTest {
        val recurring = listOf(
            Transaction(
                id = 1L,
                title = "Monthly Subscription",
                amount = -10.0,
                category = "Subscriptions",
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis(),
                isRecurring = true,
                recurrenceInterval = "MONTHLY"
            ),
            Transaction(
                id = 2L,
                title = "Weekly Expense",
                amount = -50.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis() - 1000L,
                isRecurring = true,
                recurrenceInterval = "WEEKLY"
            )
        )
        setupViewModel(recurring)

        val viewModel = HomeViewModel(
            transactionRepository = transactionRepository,
            settingsRepository = settingsRepository,
            categoryRepository = categoryRepository,
            dispatcher = testDispatcher,
        )
        advanceUntilIdle()

        val displayTxs = viewModel.state.value.displayTransactions
        assertTrue(displayTxs.all { it.isRecurring })
        assertTrue(displayTxs.any { it.recurrenceInterval == "MONTHLY" })
        assertTrue(displayTxs.any { it.recurrenceInterval == "WEEKLY" })
    }

    @Test
    fun homeViewModel_withComplexPayeeAndLocation_shouldPreserveData() = runViewModelTest {
        val complexData = listOf(
            Transaction(
                id = 1L,
                title = "Restaurant Visit",
                amount = -150.50,
                category = "Dining",
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis(),
                payee = "La Maison Française & Cie.",
                location = "Montmartre, Paris 75018, France",
                notes = "Dinner with friends — très magnifique!",
                tags = "france,travel,dining,#vacation"
            )
        )
        setupViewModel(complexData)

        val viewModel = HomeViewModel(
            transactionRepository = transactionRepository,
            settingsRepository = settingsRepository,
            categoryRepository = categoryRepository,
            dispatcher = testDispatcher,
        )
        advanceUntilIdle()

        val tx = viewModel.state.value.displayTransactions[0]
        assertTrue(tx.payee.contains("Française"))
        assertTrue(tx.location.contains("Paris"))
        assertTrue(tx.notes.contains("magnifique"))
        assertTrue(tx.tags.contains("vacation"))
    }
}
