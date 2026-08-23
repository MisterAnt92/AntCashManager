package com.antcashmanager.android.ui.home

import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.analytics.SegmentationTracker
import com.antcashmanager.android.testutil.FakeCategoryRepository
import com.antcashmanager.android.testutil.FakeSettingsRepository
import com.antcashmanager.android.testutil.FakeTransactionRepository
import com.antcashmanager.android.ui.screen.home.HomeViewModel
import com.antcashmanager.android.ui.screen.home.event.HomeEvent
import com.antcashmanager.domain.model.PaymentType
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest : BaseUnitTest() {
    private lateinit var fakeRepo: FakeTransactionRepository
    private lateinit var fakeCategoryRepo: FakeCategoryRepository
    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var segmentationTracker: SegmentationTracker
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        fakeRepo = FakeTransactionRepository()
        fakeCategoryRepo = FakeCategoryRepository()
        fakeSettingsRepository = FakeSettingsRepository()
        segmentationTracker = mockk(relaxed = true)
        viewModel = HomeViewModel(
            transactionRepository = fakeRepo,
            settingsRepository = fakeSettingsRepository,
            categoryRepository = fakeCategoryRepo,
            dispatcher = testDispatcher,
            searchDebounceMs = 0L,
            segmentationTracker = segmentationTracker,
        )
    }

    @After
    fun tearDown() {
        if (::viewModel.isInitialized) {
            viewModel.viewModelScope.cancel()
        }
    }

    @Test
    fun transactions_shouldBeEmpty_whenViewModelIsInitialized() = runViewModelTest {
        val collectJob = launch {
            viewModel.transactions.collect {}
        }
        advanceUntilIdle()

        assertTrue(viewModel.transactions.value.isEmpty())
        collectJob.cancel()
    }

    @Test
    fun transactions_shouldReflectRepositoryData_whenDateRangeIncludesAllTransactions() =
        runViewModelTest {
            // Use safe timestamp that won't have timing issues
            val now = 1_700_000_000_000L
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
                HomeEvent.SetDateRange(
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
    fun stateTransactions_shouldUpdate_whenRepositoryEmitsNewData() = runViewModelTest {
        fun awaitTransactionsSize(expected: Int) {
            repeat(10) {
                advanceUntilIdle()
                if (viewModel.state.value.transactions.size == expected) {
                    return
                }
            }
            fail("Expected transactions size=$expected but was ${viewModel.state.value.transactions.size}")
        }

        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()
        assertTrue(viewModel.state.value.transactions.isEmpty())

        val now = fakeSettingsRepository.homeDateFilterState.value.to - 1L
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
        awaitTransactionsSize(1)

        assertEquals(1, viewModel.state.value.transactions.size)
        assertEquals("Bonus", viewModel.state.value.transactions.first().title)
        collectJob.cancel()
    }

    @Test
    fun transactions_shouldContainIncomeAndExpenseWithNormalizedAmounts_whenRepositoryHasMixedTypes() =
        runViewModelTest {
            fakeSettingsRepository.homeDateFilterState.value = SavedDateFilter(
                presetIndex = SavedDateFilter.CUSTOM_PRESET_INDEX,
                from = 0L,
                to = Long.MAX_VALUE,
            )

            val now = 1_700_000_000_000L
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
                HomeEvent.SetDateRange(
                    0L,
                    Long.MAX_VALUE
                )
            )
            advanceUntilIdle()

            val incomes = viewModel.transactions.value.filter { it.type == TransactionType.INCOME }
            val expenses =
                viewModel.transactions.value.filter { it.type == TransactionType.EXPENSE }
            assertEquals(1, incomes.size)
            assertEquals(1, expenses.size)
            assertEquals(3000.0, incomes.first().amount, 0.01)
            assertEquals(-800.0, expenses.first().amount, 0.01)
            collectJob.cancel()
        }

    @Test
    fun onEvent_shouldSetSelectedTransaction_whenShowTransactionDetailsIsReceived() =
        runViewModelTest {
            // Use timestamp within default filter range
            val now = fakeSettingsRepository.homeDateFilterState.value.to - 1_000L
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
                HomeEvent.ShowTransactionDetails(
                    transaction
                )
            )
            advanceUntilIdle()

            assertEquals(transaction, viewModel.state.value.selectedTransaction)
            collectJob.cancel()
        }

    @Test
    fun onEvent_shouldClearSelectedTransaction_whenDismissTransactionDetailsIsReceived() =
        runViewModelTest {
            // Use timestamp within default filter range
            val now = fakeSettingsRepository.homeDateFilterState.value.to - 1_000L
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
                HomeEvent.ShowTransactionDetails(
                    transaction
                )
            )
            advanceUntilIdle()
            assertEquals(transaction, viewModel.state.value.selectedTransaction)

            viewModel.onEvent(HomeEvent.DismissTransactionDetails)
            advanceUntilIdle()
            assertEquals(null, viewModel.state.value.selectedTransaction)
            collectJob.cancel()
        }

    @Test
    fun balanceByPaymentType_shouldCalculatePerPaymentType_whenTransactionsContainMixedPaymentTypes() =
        runViewModelTest {
            // Cancel shared viewModel
            viewModel.viewModelScope.cancel()

            // Use timestamp within the default filter range to ensure transactions are included
            val now = fakeSettingsRepository.homeDateFilterState.value.to - 1_000L
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

            val testViewModel = HomeViewModel(
                transactionRepository = fakeRepo,
                settingsRepository = fakeSettingsRepository,
                categoryRepository = fakeCategoryRepo,
                dispatcher = testDispatcher,
                searchDebounceMs = 0L,
                segmentationTracker = segmentationTracker,
            )

            val collectJob = launch {
                testViewModel.state.collect {}
            }
            advanceUntilIdle()

            val balanceByPaymentType = testViewModel.state.value.balanceByPaymentType
            // ELECTRONIC: 2000 income = 2000
            assertEquals(2000.0, balanceByPaymentType[PaymentType.ELECTRONIC] ?: 0.0, 0.01)
            // CASH: 300 income - 150 expense = 150
            assertEquals(150.0, balanceByPaymentType[PaymentType.CASH] ?: 0.0, 0.01)
            // MEAL_VOUCHERS: 0 income - 50 expense = -50
            assertEquals(-50.0, balanceByPaymentType[PaymentType.MEAL_VOUCHERS] ?: 0.0, 0.01)
            collectJob.cancel()
        }

    @Test
    fun balanceByPaymentType_shouldExcludePaymentType_whenNetBalanceIsZero() = runViewModelTest {
        val now = fakeSettingsRepository.homeDateFilterState.value.to - 1_000L
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
        assertFalse(balanceByPaymentType.containsKey(PaymentType.ELECTRONIC))
        collectJob.cancel()
    }

    @Test
    fun balanceByPaymentType_shouldBeEmpty_whenThereAreNoTransactions() = runViewModelTest {
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
    fun balanceByPaymentType_shouldIncludeOnlyTransactionsWithinDefaultDateFilter_whenInitialized() =
        runViewModelTest {
            // Cancel shared viewModel
            viewModel.viewModelScope.cancel()

            val defaultFilter = fakeSettingsRepository.homeDateFilterState.value
            val inRangeTimestamp = defaultFilter.to - 1_000L
            val outOfRangeTimestamp = defaultFilter.from - 1_000L

            fakeRepo.transactions.value = listOf(
                Transaction(
                    id = 1,
                    title = "Recent Income",
                    amount = 1000.0,
                    category = "Work",
                    type = TransactionType.INCOME,
                    timestamp = inRangeTimestamp,
                    paymentType = PaymentType.ELECTRONIC,
                ),
                Transaction(
                    id = 2,
                    title = "Old Income",
                    amount = 500.0,
                    category = "Work",
                    type = TransactionType.INCOME,
                    timestamp = outOfRangeTimestamp,
                    paymentType = PaymentType.CASH,
                ),
            )

            val testViewModel = HomeViewModel(
                transactionRepository = fakeRepo,
                settingsRepository = fakeSettingsRepository,
                categoryRepository = fakeCategoryRepo,
                dispatcher = testDispatcher,
                searchDebounceMs = 0L,
            segmentationTracker = mockk(relaxed = true),
            )

            val collectJob = launch {
                testViewModel.state.collect {}
            }
            advanceUntilIdle()

            // Default filter is last 7 days, so only the recent transaction should be included
            val balanceByPaymentType = testViewModel.state.value.balanceByPaymentType
            assertEquals(1000.0, balanceByPaymentType[PaymentType.ELECTRONIC] ?: 0.0, 0.01)
            assertFalse(
                "Old Income should be excluded by date filter",
                balanceByPaymentType.containsKey(PaymentType.CASH)
            )
            collectJob.cancel()
        }

    @Test
    fun totalsAndBalance_shouldBeCalculatedCorrectly_whenTransactionsContainIncomeAndExpense() =
        runViewModelTest {
            // Cancel the shared viewModel
            viewModel.viewModelScope.cancel()

            val now = System.currentTimeMillis() - 1000L
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

            val testViewModel = HomeViewModel(
                transactionRepository = fakeRepo,
                settingsRepository = fakeSettingsRepository,
                categoryRepository = fakeCategoryRepo,
                dispatcher = testDispatcher,
                searchDebounceMs = 0L,
            segmentationTracker = mockk(relaxed = true),
            )

            val collectJob = launch {
                testViewModel.state.collect {}
            }
            advanceUntilIdle()

            // Ensure we are using CUSTOM filter with full range for this test
            testViewModel.onEvent(
                HomeEvent.SetDateRange(
                    0L,
                    Long.MAX_VALUE
                )
            )
            advanceUntilIdle()

            val uiState = testViewModel.state.value
            assertEquals(2500.0, uiState.totalIncome, 0.01)
            assertEquals(-1000.0, uiState.totalExpense, 0.01)
            assertEquals(1500.0, uiState.balance, 0.01)

            collectJob.cancel()
        }

    @Test
    fun totals_shouldUseNormalizedAmountSigns_whenStoredTransactionSignsAreInconsistent() =
        runViewModelTest {
            val now = 1_700_000_000_000L
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

            // Ensure we are using CUSTOM filter with full range for this test
            viewModel.onEvent(
                HomeEvent.SetDateRange(
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
    fun onEvent_shouldUpdateTotals_whenSetDateRangeIsReceived() = runViewModelTest {
        // Cancel shared viewModel
        viewModel.viewModelScope.cancel()

        val recentTimestamp = 1_720_000_000_000L
        val rangeFrom = 1_719_800_000_000L
        val rangeTo = 1_720_100_000_000L
        val oldTimestamp = 1_718_000_000_000L

        fakeRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Recent Income",
                amount = 1000.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = recentTimestamp,
            ),
            Transaction(
                id = 2,
                title = "Recent Expense",
                amount = 300.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = recentTimestamp,
            ),
            Transaction(
                id = 3,
                title = "Old Income",
                amount = 900.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = oldTimestamp,
            ),
        )

        val testViewModel = HomeViewModel(
            transactionRepository = fakeRepo,
            settingsRepository = fakeSettingsRepository,
            categoryRepository = fakeCategoryRepo,
            dispatcher = testDispatcher,
            searchDebounceMs = 0L,
            segmentationTracker = segmentationTracker,
        )

        val collectJob = launch {
            testViewModel.state.collect {}
        }
        advanceUntilIdle()

        testViewModel.onEvent(
            HomeEvent.SetDateRange(
                rangeFrom,
                rangeTo,
            )
        )
        advanceUntilIdle()

        val uiState = testViewModel.state.value
        assertEquals(1000.0, uiState.totalIncome, 0.01)
        assertEquals(-300.0, uiState.totalExpense, 0.01)
        assertEquals(700.0, uiState.balance, 0.01)

        collectJob.cancel()
    }

    @Test
    fun onEvent_shouldPersistCustomFilterState_whenSetDateRangeIsReceived() = runViewModelTest {
        val from = 1_700_000_000_000L
        val to = 1_700_100_000_000L

        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(
            HomeEvent.SetDateRange(from, to)
        )
        advanceUntilIdle()

        assertEquals(SavedDateFilter.CUSTOM_PRESET_INDEX, viewModel.state.value.selectedPresetIndex)
        assertEquals(from, fakeSettingsRepository.homeDateFilterState.value.from)
        assertEquals(to, fakeSettingsRepository.homeDateFilterState.value.to)
        collectJob.cancel()
    }

    @Test
    fun state_shouldRestoreSavedCustomHomeFilter_whenViewModelIsRecreated() = runViewModelTest {
        val from = 1_701_000_000_000L
        val to = 1_701_500_000_000L
        fakeSettingsRepository.homeDateFilterState.value = SavedDateFilter(
            presetIndex = SavedDateFilter.CUSTOM_PRESET_INDEX,
            from = from,
            to = to,
        )

        val restoredViewModel = HomeViewModel(
            transactionRepository = fakeRepo,
            settingsRepository = fakeSettingsRepository,
            categoryRepository = fakeCategoryRepo,
            dispatcher = testDispatcher,
            searchDebounceMs = 0L,
            segmentationTracker = mockk(relaxed = true),
        )

        val collectJob = launch {
            restoredViewModel.state.collect {}
        }
        advanceUntilIdle()

        assertEquals(
            SavedDateFilter.CUSTOM_PRESET_INDEX,
            restoredViewModel.state.value.selectedPresetIndex
        )
        assertEquals(from, restoredViewModel.state.value.dateRangeFrom)
        assertEquals(to, restoredViewModel.state.value.dateRangeTo)
        collectJob.cancel()
    }

    @Test
    fun init_shouldUseDynamicDateRangeTo_whenRestoringNonCustomPreset() = runViewModelTest {
        // Simula un filtro salvato con preset non-custom e un "to" stantio (passato di 1 ora)
        val staleStoredTo = System.currentTimeMillis() - (60L * 60 * 1000)
        val staleStoredFrom = staleStoredTo - (7L * 24 * 60 * 60 * 1000)
        fakeSettingsRepository.homeDateFilterState.value = SavedDateFilter(
            presetIndex = 1, // "Last 7 days" preset, non-custom
            from = staleStoredFrom,
            to = staleStoredTo,
        )

        val restoredViewModel = HomeViewModel(
            transactionRepository = fakeRepo,
            settingsRepository = fakeSettingsRepository,
            categoryRepository = fakeCategoryRepo,
            dispatcher = testDispatcher,
            searchDebounceMs = 0L,
            segmentationTracker = mockk(relaxed = true),
        )

        val collectJob = launch {
            restoredViewModel.state.collect {}
        }
        advanceUntilIdle()

        // dateRangeTo deve essere >= staleStoredTo (cioè ricalcolato dinamicamente come "adesso")
        assertTrue(
            "dateRangeTo deve essere > staleStoredTo per i preset non-custom",
            restoredViewModel.state.value.dateRangeTo > staleStoredTo,
        )
        // Il presetIndex deve essere mantenuto correttamente
        assertEquals(1, restoredViewModel.state.value.selectedPresetIndex)
        collectJob.cancel()
    }
}
