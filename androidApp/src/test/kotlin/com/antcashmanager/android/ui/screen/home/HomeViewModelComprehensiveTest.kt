package com.antcashmanager.android.ui.home

import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.testutil.FakeCategoryRepository
import com.antcashmanager.android.testutil.FakeSettingsRepository
import com.antcashmanager.android.testutil.FakeTransactionRepository
import com.antcashmanager.android.ui.screen.home.HomeViewModel
import com.antcashmanager.domain.model.SavedDateFilter
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

private const val T0 = 1_700_000_000_000L

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelComprehensiveTest : BaseUnitTest() {
    private lateinit var fakeRepo: FakeTransactionRepository
    private lateinit var fakeCategoryRepo: FakeCategoryRepository
    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var viewModel: HomeViewModel

    private fun includeAllTransactions() {
        fakeSettingsRepository.homeDateFilterState.value =
            SavedDateFilter(
                presetIndex = SavedDateFilter.CUSTOM_PRESET_INDEX,
                from = 0L,
                to = Long.MAX_VALUE,
            )
    }

    @Before
    fun setup() {
        fakeRepo = FakeTransactionRepository()
        fakeCategoryRepo = FakeCategoryRepository()
        fakeSettingsRepository = FakeSettingsRepository()
        viewModel =
            HomeViewModel(
                transactionRepository = fakeRepo,
                settingsRepository = fakeSettingsRepository,
                categoryRepository = fakeCategoryRepo,
                dispatcher = testDispatcher,
                searchDebounceMs = 0L,
                segmentationTracker = mockk(relaxed = true),
                analyticsManager = mockk(relaxed = true),
            )
    }

    @After
    override fun tearDownDispatchers() {
        if (::viewModel.isInitialized) {
            viewModel.viewModelScope.cancel()
            testDispatcher.scheduler.advanceUntilIdle()
        }
        super.tearDownDispatchers()
    }

    @Test
    fun transactions_shouldBeEmpty_whenRepositoryIsEmpty() =
        runViewModelTest {
            val collectJob = launch { viewModel.transactions.collect {} }
            advanceUntilIdle()

            assertTrue(viewModel.transactions.value.isEmpty())
            collectJob.cancel()
        }

    @Test
    fun transactions_shouldShowAllTransactions_whenLoaded() =
        runViewModelTest {
            fakeRepo.transactions.value =
                listOf(
                    Transaction(id = 1, title = "Lunch", amount = -50.0, category = "Food", type = TransactionType.EXPENSE, timestamp = T0),
                    Transaction(id = 2, title = "Salary", amount = 5000.0, category = "Income", type = TransactionType.INCOME, timestamp = T0 - 1000),
                    Transaction(id = 3, title = "Rent", amount = -1000.0, category = "Housing", type = TransactionType.EXPENSE, timestamp = T0 - 2000),
                )
            includeAllTransactions()

            val collectJob = launch { viewModel.transactions.collect {} }
            advanceUntilIdle()

            assertEquals(3, viewModel.transactions.value.size)
            collectJob.cancel()
        }

    @Test
    fun transactions_shouldSeparateByType_whenMixedIncomeAndExpenseLoaded() =
        runViewModelTest {
            fakeRepo.transactions.value =
                listOf(
                    Transaction(1, "Income", 5000.0, "Salary", TransactionType.INCOME, T0),
                    Transaction(2, "Expense1", -1000.0, "Housing", TransactionType.EXPENSE, T0 - 1000),
                    Transaction(3, "Expense2", -500.0, "Food", TransactionType.EXPENSE, T0 - 2000),
                )
            includeAllTransactions()

            val collectJob = launch { viewModel.transactions.collect {} }
            advanceUntilIdle()

            val txs = viewModel.transactions.value
            assertEquals(2, txs.filter { it.type == TransactionType.EXPENSE }.size)
            assertEquals(1, txs.filter { it.type == TransactionType.INCOME }.size)
            collectJob.cancel()
        }
}
