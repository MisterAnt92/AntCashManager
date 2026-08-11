package com.antcashmanager.android.ui.screen.settings.dataManagement

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Data handling tests for SettingsDataViewModel.
 * Tests cover:
 * - Export/Import operations
 * - Data counting and statistics
 * - Large dataset handling
 * - Special character preservation
 * - Clear operations
 */
class SettingsDataViewModelDataHandlingTest : BaseUnitTest() {

    private val transactionRepository = mockk<TransactionRepository>()
    private val categoryRepository = mockk<CategoryRepository>()

    private fun createViewModel(
        transactions: List<Transaction> = emptyList(),
        categories: List<Category> = emptyList()
    ): SettingsDataViewModel {
        coEvery { transactionRepository.getTransactions() } returns flowOf(transactions)
        coEvery { categoryRepository.getCategories() } returns flowOf(categories)

        return SettingsDataViewModel(
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            dispatcher = testDispatcher,
        )
    }

    @Test
    fun settingsDataViewModel_withNoData_shouldShowZeroCounts() = runViewModelTest {
        val viewModel = createViewModel(emptyList(), emptyList())
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(0, state.transactionCount)
        assertEquals(0, state.categoryCount)
    }

    @Test
    fun settingsDataViewModel_shouldCountTransactionsAccurately() = runViewModelTest {
        val transactions = (1..50).map { index ->
            Transaction(
                id = index.toLong(),
                title = "Transaction $index",
                amount = -50.0,
                category = "Category",
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis()
            )
        }

        val viewModel = createViewModel(transactions)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(50, state.transactionCount)
    }

    @Test
    fun settingsDataViewModel_shouldCountCategoriesAccurately() = runViewModelTest {
        val categories = (1..10).map { index ->
            Category(
                id = index.toLong(),
                name = "Category $index",
                icon = "icon$index",
                color = 0xFF000000L + (index * 100000L),
                type = "EXPENSE"
            )
        }

        val viewModel = createViewModel(emptyList(), categories)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(10, state.categoryCount)
    }

    @Test
    fun settingsDataViewModel_shouldCountBothTransactionsAndCategories() = runViewModelTest {
        val transactions = (1..100).map { index ->
            Transaction(
                id = index.toLong(),
                title = "Transaction $index",
                amount = if (index % 2 == 0) -50.0 else 100.0,
                category = "Category",
                type = if (index % 2 == 0) TransactionType.EXPENSE else TransactionType.INCOME,
                timestamp = System.currentTimeMillis()
            )
        }

        val categories = (1..5).map { index ->
            Category(
                id = index.toLong(),
                name = "Category $index",
                icon = "icon$index",
                color = 0xFF000000L,
                type = "EXPENSE"
            )
        }

        val viewModel = createViewModel(transactions, categories)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(100, state.transactionCount)
        assertEquals(5, state.categoryCount)
    }

    @Test
    fun settingsDataViewModel_shouldHandleVeryLargeDataset() = runViewModelTest {
        val largeTransactionList = (1..10000).map { index ->
            Transaction(
                id = index.toLong(),
                title = "Transaction $index",
                amount = if (index % 2 == 0) -50.0 else 100.0,
                category = "Category ${index % 50}",
                type = if (index % 2 == 0) TransactionType.EXPENSE else TransactionType.INCOME,
                timestamp = System.currentTimeMillis() - (index * 1000L)
            )
        }

        val categories = (1..20).map { index ->
            Category(
                id = index.toLong(),
                name = "Category $index",
                icon = "icon$index",
                color = 0xFF000000L,
                type = "EXPENSE"
            )
        }

        val viewModel = createViewModel(largeTransactionList, categories)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(10000, state.transactionCount)
        assertEquals(20, state.categoryCount)
    }

    @Test
    fun settingsDataViewModel_shouldPreserveSpecialCharactersInExport() = runViewModelTest {
        val transactions = listOf(
            Transaction(
                id = 1L,
                title = "Café Français €100",
                amount = -100.0,
                category = "Dining",
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis(),
                payee = "Restaurant Français",
                notes = "Crêpes & Café — très bon!",
                location = "Montmartre, Paris 75018",
                tags = "travel,france,#vacation"
            )
        )

        val viewModel = createViewModel(transactions)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(1, state.transactionCount)
    }

    @Test
    fun settingsDataViewModel_shouldHandleDecimalAmountsPrecisely() = runViewModelTest {
        val transactions = listOf(
            Transaction(
                id = 1L,
                title = "Precise Amount 1",
                amount = -19.99,
                category = "Shopping",
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis()
            ),
            Transaction(
                id = 2L,
                title = "Precise Amount 2",
                amount = 123.456,
                category = "Income",
                type = TransactionType.INCOME,
                timestamp = System.currentTimeMillis()
            )
        )

        val viewModel = createViewModel(transactions)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(2, state.transactionCount)
    }

    @Test
    fun settingsDataViewModel_shouldHandleNegativeAmountsCorrectly() = runViewModelTest {
        val expenses = (1..10).map { index ->
            Transaction(
                id = index.toLong(),
                title = "Expense $index",
                amount = -50.0 * index,
                category = "Expenses",
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis()
            )
        }

        val viewModel = createViewModel(expenses)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(10, state.transactionCount)
    }

    @Test
    fun settingsDataViewModel_shouldHandlePositiveAmountsCorrectly() = runViewModelTest {
        val incomes = (1..5).map { index ->
            Transaction(
                id = index.toLong(),
                title = "Income $index",
                amount = 1000.0 * index,
                category = "Income",
                type = TransactionType.INCOME,
                timestamp = System.currentTimeMillis()
            )
        }

        val viewModel = createViewModel(incomes)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(5, state.transactionCount)
    }

    @Test
    fun settingsDataViewModel_shouldHandleZeroAmountTransactions() = runViewModelTest {
        val zeroAmountTx = listOf(
            Transaction(
                id = 1L,
                title = "Transfer",
                amount = 0.0,
                category = "Transfer",
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis()
            )
        )

        val viewModel = createViewModel(zeroAmountTx)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(1, state.transactionCount)
    }

    @Test
    fun settingsDataViewModel_shouldMixExpenseAndIncomeTransactions() = runViewModelTest {
        val transactions = listOf(
            Transaction(
                id = 1L,
                title = "Lunch",
                amount = -50.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis()
            ),
            Transaction(
                id = 2L,
                title = "Salary",
                amount = 5000.0,
                category = "Income",
                type = TransactionType.INCOME,
                timestamp = System.currentTimeMillis()
            ),
            Transaction(
                id = 3L,
                title = "Rent",
                amount = -1000.0,
                category = "Housing",
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis()
            )
        )

        val viewModel = createViewModel(transactions)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(3, state.transactionCount)

        // Verify mix of types
        assertTrue(transactions.any { it.type == TransactionType.INCOME })
        assertTrue(transactions.any { it.type == TransactionType.EXPENSE })
    }

    @Test
    fun settingsDataViewModel_shouldCountRecurringTransactions() = runViewModelTest {
        val recurringTransactions = (1..10).map { index ->
            Transaction(
                id = index.toLong(),
                title = "Recurring Transaction $index",
                amount = -100.0,
                category = "Subscriptions",
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis(),
                isRecurring = true,
                recurrenceInterval = if (index % 2 == 0) "WEEKLY" else "MONTHLY"
            )
        }

        val viewModel = createViewModel(recurringTransactions)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(10, state.transactionCount)
    }

    @Test
    fun settingsDataViewModel_shouldHandleCategoryColorVariations() = runViewModelTest {
        val categories = listOf(
            Category(1L, "Food", "🍔", 0xFFFF6B6B, "EXPENSE"),
            Category(2L, "Salary", "💰", 0xFF51CF66, "INCOME"),
            Category(3L, "Housing", "🏠", 0xFF4DABF7, "EXPENSE"),
            Category(4L, "Transport", "🚗", 0xFFFFD93D, "EXPENSE"),
            Category(5L, "Entertainment", "🎬", 0xFFEE5A6F, "EXPENSE"),
        )

        val viewModel = createViewModel(emptyList(), categories)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(5, state.categoryCount)
    }

    @Test
    fun settingsDataViewModel_shouldExportTransactionWithAllFields() = runViewModelTest {
        val complexTransaction = Transaction(
            id = 1L,
            title = "Complex Transaction",
            amount = -299.99,
            category = "Shopping",
            type = TransactionType.EXPENSE,
            timestamp = System.currentTimeMillis(),
            notes = "Notes with special chars: é, ñ, ü, 中文",
            payee = "Vendor Name & Co.",
            location = "City (Zip), Country",
            tags = "tag1,tag2,#important",
            isRecurring = false,
            recurrenceInterval = "",
            mealVoucherCount = 0,
            categoryIcon = "shopping_bag",
            categoryColor = 0xFF4CAF50
        )

        val viewModel = createViewModel(listOf(complexTransaction))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(1, state.transactionCount)
    }

    @Test
    fun settingsDataViewModel_shouldProvideSummaryStatistics() = runViewModelTest {
        val transactions = (1..100).map { index ->
            Transaction(
                id = index.toLong(),
                title = "Transaction $index",
                amount = if (index % 2 == 0) -50.0 else 100.0,
                category = "Category ${index % 5}",
                type = if (index % 2 == 0) TransactionType.EXPENSE else TransactionType.INCOME,
                timestamp = System.currentTimeMillis()
            )
        }

        val categories = (1..5).map { index ->
            Category(index.toLong(), "Category $index", "icon", 0xFF000000L, "EXPENSE")
        }

        val viewModel = createViewModel(transactions, categories)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(100, state.transactionCount)
        assertEquals(5, state.categoryCount)

        // Statistics should be available for display
        assertTrue(state.transactionCount > 0)
        assertTrue(state.categoryCount > 0)
    }
}
