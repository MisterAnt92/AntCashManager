package com.antcashmanager.android.ui.home

import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.ui.screen.home.HomeEvent
import com.antcashmanager.android.ui.screen.home.HomeViewModel
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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
    fun transactions_shouldReflectRepositoryData_whenDateRangeIncludesAllTransactions() = runViewModelTest {
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
        val expenses = viewModel.transactions.value.filter { it.type == TransactionType.EXPENSE }
        assertEquals(1, incomes.size)
        assertEquals(1, expenses.size)
        assertEquals(3000.0, incomes.first().amount, 0.01)
        assertEquals(-800.0, expenses.first().amount, 0.01)
        collectJob.cancel()
    }

    @Test
    fun onEvent_shouldSetSelectedTransaction_whenShowTransactionDetailsIsReceived() = runViewModelTest {
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
    fun onEvent_shouldClearSelectedTransaction_whenDismissTransactionDetailsIsReceived() = runViewModelTest {
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

            val collectJob = launch {
                viewModel.state.collect {}
            }
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

        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        // Default filter is last 7 days, so only the recent transaction should be included
        val balanceByPaymentType = viewModel.state.value.balanceByPaymentType
        assertEquals(1000.0, balanceByPaymentType[PaymentType.ELECTRONIC] ?: 0.0, 0.01)
        assertFalse(balanceByPaymentType.containsKey(PaymentType.CASH))
        collectJob.cancel()
    }

    @Test
    fun totalsAndBalance_shouldBeCalculatedCorrectly_whenTransactionsContainIncomeAndExpense() =
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
            HomeEvent.SetDateRange(
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

        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(
            HomeEvent.SetDateRange(
                rangeFrom,
                rangeTo,
            )
        )
        advanceUntilIdle()

        val uiState = viewModel.state.value
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
        )

        val collectJob = launch {
            restoredViewModel.state.collect {}
        }
        advanceUntilIdle()

        assertEquals(SavedDateFilter.CUSTOM_PRESET_INDEX, restoredViewModel.state.value.selectedPresetIndex)
        assertEquals(from, restoredViewModel.state.value.dateRangeFrom)
        assertEquals(to, restoredViewModel.state.value.dateRangeTo)
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

    private class FakeSettingsRepository : SettingsRepository {
        @get:JvmName("mutableHomeDateFilterState")
        val homeDateFilterState = MutableStateFlow(
            SavedDateFilter(
                presetIndex = 1,
                from = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000),
                to = System.currentTimeMillis(),
            ),
        )

        private val transactionsDateFilterState = MutableStateFlow(
            SavedDateFilter(
                presetIndex = 1,
                from = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000),
                to = System.currentTimeMillis(),
            ),
        )

        private val chartsDateFilterState = MutableStateFlow(
            SavedDateFilter(
                presetIndex = 1,
                from = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000),
                to = System.currentTimeMillis(),
            ),
        )

        override fun getTheme() = flowOf(com.antcashmanager.domain.model.AppTheme.SYSTEM)
        override suspend fun setTheme(theme: com.antcashmanager.domain.model.AppTheme) = Unit
        override fun getLanguage() = flowOf(com.antcashmanager.domain.model.AppLanguage.SYSTEM)
        override suspend fun setLanguage(language: com.antcashmanager.domain.model.AppLanguage) = Unit
        override fun getShowCharts() = flowOf(true)
        override suspend fun setShowCharts(show: Boolean) = Unit
        override fun getHighContrast() = flowOf(false)
        override suspend fun setHighContrast(enabled: Boolean) = Unit
        override fun getLargeText() = flowOf(false)
        override suspend fun setLargeText(enabled: Boolean) = Unit
        override fun getReduceMotion() = flowOf(false)
        override suspend fun setReduceMotion(enabled: Boolean) = Unit
        override fun getShowTransactionNotes() = flowOf(true)
        override suspend fun setShowTransactionNotes(show: Boolean) = Unit
        override fun getCurrencySymbol() = flowOf("€")
        override suspend fun setCurrencySymbol(symbol: String) = Unit
        override fun getDecimalDigits() = flowOf(2)
        override suspend fun setDecimalDigits(digits: Int) = Unit
        override fun getDecimalSeparator() = flowOf(",")
        override suspend fun setDecimalSeparator(separator: String) = Unit
        override fun getThousandsSeparator() = flowOf("")
        override suspend fun setThousandsSeparator(separator: String) = Unit
        override fun getDateFormat() = flowOf("dd/MM/yyyy")
        override suspend fun setDateFormat(pattern: String) = Unit
        override fun getDateFilterExpanded() = flowOf(true)
        override suspend fun setDateFilterExpanded(expanded: Boolean) = Unit
        override fun getHomeDateFilterPreset() = homeDateFilterState.map { it.presetIndex }
        override suspend fun setHomeDateFilterPreset(index: Int) = Unit
        override fun getHomeDateFilterState() = homeDateFilterState
        override suspend fun setHomeDateFilterState(filter: SavedDateFilter) {
            homeDateFilterState.value = filter
        }

        override fun getTransactionsDateFilterPreset() = transactionsDateFilterState.map { it.presetIndex }
        override suspend fun setTransactionsDateFilterPreset(index: Int) = Unit
        override fun getTransactionsDateFilterState() = transactionsDateFilterState
        override suspend fun setTransactionsDateFilterState(filter: SavedDateFilter) {
            transactionsDateFilterState.value = filter
        }

        override fun getChartsDateFilterPreset() = chartsDateFilterState.map { it.presetIndex }
        override suspend fun setChartsDateFilterPreset(index: Int) = Unit
        override fun getChartsDateFilterState() = chartsDateFilterState
        override suspend fun setChartsDateFilterState(filter: SavedDateFilter) {
            chartsDateFilterState.value = filter
        }

        override fun getChartsZoomEnabled() = flowOf(true)
        override suspend fun setChartsZoomEnabled(enabled: Boolean) = Unit
        override fun getShowPaymentTypeBreakdown() = flowOf(false)
        override suspend fun setShowPaymentTypeBreakdown(show: Boolean) = Unit
        override fun getTransactionDisplayType() =
            flowOf(com.antcashmanager.domain.model.TransactionDisplayType.TREND)

        override suspend fun setTransactionDisplayType(
            displayType: com.antcashmanager.domain.model.TransactionDisplayType,
        ) = Unit

        override fun getTransactionsTransactionDisplayType() =
            flowOf(com.antcashmanager.domain.model.TransactionDisplayType.TREND)

        override suspend fun setTransactionsTransactionDisplayType(
            displayType: com.antcashmanager.domain.model.TransactionDisplayType,
        ) = Unit

        override fun getShowInitialAnimation(): Flow<Boolean> = flowOf(true)
        override suspend fun setShowInitialAnimation(show: Boolean) = Unit

        override fun getIsTutorialCompleted() = flowOf(true)
        override suspend fun setIsTutorialCompleted(completed: Boolean) = Unit
        override fun getDataEncryptionEnabled() = flowOf(false)
        override suspend fun setDataEncryptionEnabled(enabled: Boolean) = Unit
        override suspend fun resetAllPreferences() = Unit
    }
}
