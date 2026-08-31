package com.antcashmanager.android.ui.home

import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.testutil.FakeCategoryRepository
import com.antcashmanager.android.testutil.FakeSettingsRepository
import com.antcashmanager.android.testutil.FakeTransactionRepository
import com.antcashmanager.android.ui.screen.home.HomeViewModel
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive edge case tests for HomeViewModel.
 * Additional coverage for:
 * - Empty transaction lists
 * - Large datasets
 * - Unicode characters
 * - Mixed transaction types
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelComprehensiveTest : BaseUnitTest() {
    private lateinit var fakeRepo: FakeTransactionRepository
    private lateinit var fakeCategoryRepo: FakeCategoryRepository
    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        fakeRepo = FakeTransactionRepository()
        fakeCategoryRepo = FakeCategoryRepository()
        fakeSettingsRepository = FakeSettingsRepository()
        viewModel = HomeViewModel(
            transactionRepository = fakeRepo,
            settingsRepository = fakeSettingsRepository,
            categoryRepository = fakeCategoryRepo,
            dispatcher = testDispatcher,
            searchDebounceMs = 0L,
            segmentationTracker = mockk(relaxed = true),
        )
    }

    @After
    fun tearDown() {
        if (::viewModel.isInitialized) {
            viewModel.viewModelScope.cancel()
        }
    }

    @Test
    fun transactions_shouldBeEmpty_whenRepositoryIsEmpty() = runViewModelTest {
        val collectJob = launch {
            viewModel.transactions.collect {}
        }
        advanceUntilIdle()

        assertTrue(viewModel.transactions.value.isEmpty())
        collectJob.cancel()
    }

    @Test
    fun transactions_shouldShowAllTransactions_whenLoaded() = runViewModelTest {
        val now = System.currentTimeMillis()
        fakeRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Lunch",
                amount = -50.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = now,
            ),
            Transaction(
                id = 2,
                title = "Salary",
                amount = 5000.0,
                category = "Income",
                type = TransactionType.INCOME,
                timestamp = now - 1000,
            ),
            Transaction(
                id = 3,
                title = "Rent",
                amount = -1000.0,
                category = "Housing",
                type = TransactionType.EXPENSE,
                timestamp = now - 2000,
            ),
        )

        val collectJob = launch {
            viewModel.transactions.collect {}
        }
        advanceUntilIdle()

        assertEquals(3, viewModel.transactions.value.size)
        collectJob.cancel()
    }

    @Test
    fun transactions_shouldHandleLargeDataset() = runViewModelTest {
        val now = System.currentTimeMillis()
        val transactions = (1..100).map { index ->
            Transaction(
                id = index.toLong(),
                title = "Transaction $index",
                amount = if (index % 2 == 0) -50.0 else 100.0,
                category = "Category ${index % 5}",
                type = if (index % 2 == 0) TransactionType.EXPENSE else TransactionType.INCOME,
                timestamp = now - (index * 1000L),
            )
        }
        fakeRepo.transactions.value = transactions

        val collectJob = launch {
            viewModel.transactions.collect {}
        }
        advanceUntilIdle()

        assertEquals(100, viewModel.transactions.value.size)
        collectJob.cancel()
    }

    @Test
    fun transactions_shouldPreserveUnicodeCharacters() = runViewModelTest {
        val now = System.currentTimeMillis()
        fakeRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Café Français",
                amount = -50.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = now,
            ),
            Transaction(
                id = 2,
                title = "北京烤鸭",
                amount = -30.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = now - 1000,
            ),
        )

        val collectJob = launch {
            viewModel.transactions.collect {}
        }
        advanceUntilIdle()

        val txs = viewModel.transactions.value
        assertTrue(txs.any { it.title.contains("Café") })
        assertTrue(txs.any { it.title.contains("北京") })
        collectJob.cancel()
    }

    @Test
    fun transactions_shouldHandleMixedIncomeExpense() = runViewModelTest {
        val now = System.currentTimeMillis()
        fakeRepo.transactions.value = listOf(
            Transaction(1, "Income", 5000.0, "Salary", TransactionType.INCOME, now),
            Transaction(2, "Expense1", -1000.0, "Housing", TransactionType.EXPENSE, now - 1000),
            Transaction(3, "Expense2", -500.0, "Food", TransactionType.EXPENSE, now - 2000),
        )

        val collectJob = launch {
            viewModel.transactions.collect {}
        }
        advanceUntilIdle()

        val txs = viewModel.transactions.value
        val expenses = txs.filter { it.type == TransactionType.EXPENSE }
        val incomes = txs.filter { it.type == TransactionType.INCOME }

        assertEquals(2, expenses.size)
        assertEquals(1, incomes.size)
        collectJob.cancel()
    }

    @Test
    fun transactions_shouldHandleZeroAmountTransaction() = runViewModelTest {
        val now = System.currentTimeMillis()
        fakeRepo.transactions.value = listOf(
            Transaction(
                1,
                "Transfer",
                0.0,
                "Transfer",
                TransactionType.EXPENSE,
                now,
            )
        )

        val collectJob = launch {
            viewModel.transactions.collect {}
        }
        advanceUntilIdle()

        val txs = viewModel.transactions.value
        assertEquals(1, txs.size)
        assertEquals(0.0, txs[0].amount)
        collectJob.cancel()
    }

    @Test
    fun transactions_shouldHandleRecurringTransactions() = runViewModelTest {
        val now = System.currentTimeMillis()
        fakeRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Monthly Subscription",
                amount = -10.0,
                category = "Subscriptions",
                type = TransactionType.EXPENSE,
                timestamp = now,
                isRecurring = true,
                recurrenceInterval = "MONTHLY",
            )
        )

        val collectJob = launch {
            viewModel.transactions.collect {}
        }
        advanceUntilIdle()

        val txs = viewModel.transactions.value
        assertTrue(txs[0].isRecurring)
        assertEquals("MONTHLY", txs[0].recurrenceInterval)
        collectJob.cancel()
    }

    @Test
    fun transactions_shouldHandleVeryLargeAmounts() = runViewModelTest {
        val now = System.currentTimeMillis()
        fakeRepo.transactions.value = listOf(
            Transaction(
                1,
                "Large Income",
                9_999_999.99,
                "Income",
                TransactionType.INCOME,
                now,
            )
        )

        val collectJob = launch {
            viewModel.transactions.collect {}
        }
        advanceUntilIdle()

        val txs = viewModel.transactions.value
        assertEquals(1, txs.size)
        assertEquals(9_999_999.99, txs[0].amount)
        collectJob.cancel()
    }

    @Test
    fun transactions_shouldHandleComplexPayeeAndLocation() = runViewModelTest {
        val now = System.currentTimeMillis()
        fakeRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Restaurant",
                amount = -150.0,
                category = "Dining",
                type = TransactionType.EXPENSE,
                timestamp = now,
                payee = "La Maison Française & Cie.",
                location = "Paris, France 75018",
            )
        )

        val collectJob = launch {
            viewModel.transactions.collect {}
        }
        advanceUntilIdle()

        val txs = viewModel.transactions.value
        assertTrue(txs[0].payee.contains("Française"))
        assertTrue(txs[0].location.contains("France"))
        collectJob.cancel()
    }
}
