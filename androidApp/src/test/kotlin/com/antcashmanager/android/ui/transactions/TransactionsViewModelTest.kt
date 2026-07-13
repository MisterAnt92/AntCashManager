package com.antcashmanager.android.ui.transactions

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.ui.screen.transactions.TransactionsEvent
import com.antcashmanager.android.ui.screen.transactions.TransactionsViewModel
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModelTest : BaseUnitTest() {

    private lateinit var fakeTransactionRepo: FakeTransactionRepository
    private lateinit var fakeCategoryRepo: FakeCategoryRepository
    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var viewModel: TransactionsViewModel

    @Before
    fun setup() {
        fakeTransactionRepo = FakeTransactionRepository()
        fakeCategoryRepo = FakeCategoryRepository()
        fakeSettingsRepository = FakeSettingsRepository()
        viewModel = TransactionsViewModel(
            transactionRepository = fakeTransactionRepo,
            categoryRepository = fakeCategoryRepo,
            settingsRepository = fakeSettingsRepository,
            dispatcher = testDispatcher,
        )
    }

    @Test
    fun init_shouldHaveEmptyTransactionsList_whenViewModelIsInitialized() = runViewModelTest {
        assertTrue(viewModel.state.value.isLoading) // Initial state check
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.transactions.isEmpty())
        collectJob.cancel()
    }

    @Test
    fun init_shouldHaveEmptyCategoriesList_whenViewModelIsInitialized() = runViewModelTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        assertTrue(viewModel.state.value.categories.isEmpty())
        collectJob.cancel()
    }

    @Test
    fun addTransaction_shouldAddNewTransaction_whenCalled() = runViewModelTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
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

        assertEquals(1, viewModel.state.value.transactions.size)
        assertEquals("Lunch", viewModel.state.value.transactions.first().title)
        assertEquals(-15.0, viewModel.state.value.transactions.first().amount, 0.01)
        assertEquals(TransactionType.EXPENSE, viewModel.state.value.transactions.first().type)
        collectJob.cancel()
    }

    @Test
    fun deleteTransaction_shouldRemoveTransaction_whenCalled() = runViewModelTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

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
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.transactions.size)

        viewModel.deleteTransaction(transaction)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.transactions.isEmpty())
        collectJob.cancel()
    }

    @Test
    fun updateTransaction_shouldUpdateExistingTransaction_whenCalled() = runViewModelTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

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
        advanceUntilIdle()

        val updated = transaction.copy(title = "Espresso", amount = 2.5)
        viewModel.updateTransaction(updated)
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.transactions.size)
        assertEquals("Espresso", viewModel.state.value.transactions.first().title)
        assertEquals(-2.5, viewModel.state.value.transactions.first().amount, 0.01)
        collectJob.cancel()
    }

    @Test
    fun categories_shouldReflectRepositoryData_whenRepositoryEmitsCategories() = runViewModelTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        fakeCategoryRepo.categories.value = listOf(
            Category(id = 1, name = "Food", icon = "restaurant", color = 0xFFE57373),
            Category(id = 2, name = "Transport", icon = "directions_bus", color = 0xFF81C784),
        )
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.categories.size)
        assertEquals("Food", viewModel.state.value.categories[0].name)
        assertEquals("Transport", viewModel.state.value.categories[1].name)
        collectJob.cancel()
    }

    @Test
    fun addTransaction_shouldAddIncomeWithPositiveAmount_whenTypeIsIncome() = runViewModelTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
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

        assertEquals(1, viewModel.state.value.transactions.size)
        assertEquals(TransactionType.INCOME, viewModel.state.value.transactions.first().type)
        assertEquals(3000.0, viewModel.state.value.transactions.first().amount, 0.01)
        collectJob.cancel()
    }

    @Test
    fun searchQuery_shouldFilterTransactionsByTitle_whenQueryIsUpdated() = runViewModelTest {
        // Cancel shared viewModel
        viewModel.viewModelScope.cancel()

        val now = System.currentTimeMillis()
        fakeTransactionRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Salary April",
                amount = 2500.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = now,
            ),
            Transaction(
                id = 2,
                title = "Groceries",
                amount = 85.50,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = now,
            ),
        )

        val testViewModel = TransactionsViewModel(
            transactionRepository = fakeTransactionRepo,
            categoryRepository = fakeCategoryRepo,
            settingsRepository = fakeSettingsRepository,
            dispatcher = testDispatcher,
        )

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            testViewModel.state.collect {}
        }
        advanceUntilIdle()

        // Ensure date range includes all data
        testViewModel.onEvent(
            TransactionsEvent.SetDateRange(
                0L,
                Long.MAX_VALUE,
            )
        )
        advanceUntilIdle()

        testViewModel.onEvent(
            TransactionsEvent.UpdateSearchQuery("salary")
        )
        advanceUntilIdle()

        assertEquals("salary", testViewModel.state.value.searchQuery)
        assertEquals(1, testViewModel.state.value.filteredTransactions.size)
        assertEquals("Salary April", testViewModel.state.value.filteredTransactions.first().title)

        collectJob.cancel()
    }

    @Test
    fun searchQuery_shouldMatchAmountWithCommaSeparator_whenSearchingForAmount() = runViewModelTest {
        val now = System.currentTimeMillis()
        fakeTransactionRepo.transactions.value = listOf(
            Transaction(
                id = 1,
                title = "Groceries",
                amount = 85.50,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = now,
            ),
            Transaction(
                id = 2,
                title = "Salary",
                amount = 2500.0,
                category = "Work",
                type = TransactionType.INCOME,
                timestamp = now,
            ),
        )

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(
            TransactionsEvent.SetDateRange(
                0L,
                Long.MAX_VALUE,
            )
        )
        advanceUntilIdle()

        viewModel.onEvent(
            TransactionsEvent.UpdateSearchQuery("85,5")
        )
        advanceUntilIdle()

        assertFalse(viewModel.state.value.filteredTransactions.isEmpty())
        assertEquals("Groceries", viewModel.state.value.filteredTransactions.first().title)

        collectJob.cancel()
    }

    @Test
    fun onEvent_shouldPersistCustomTransactionsFilter_whenSetDateRangeIsReceived() = runViewModelTest {
        val from = 1_710_000_000_000L
        val to = 1_710_900_000_000L

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(
            com.antcashmanager.android.ui.screen.transactions.TransactionsEvent.SetDateRange(
                from = from,
                to = to,
            )
        )
        advanceUntilIdle()

        assertEquals(SavedDateFilter.CUSTOM_PRESET_INDEX, viewModel.state.value.selectedPresetIndex)
        assertEquals(from, fakeSettingsRepository.transactionsDateFilterState.value.from)
        assertEquals(to, fakeSettingsRepository.transactionsDateFilterState.value.to)
        collectJob.cancel()
    }

    @Test
    fun init_shouldRestoreSavedTransactionsDateFilter_whenViewModelIsRecreated() = runViewModelTest {
        val from = 1_711_000_000_000L
        val to = 1_711_900_000_000L
        fakeSettingsRepository.transactionsDateFilterState.value = SavedDateFilter(
            presetIndex = SavedDateFilter.CUSTOM_PRESET_INDEX,
            from = from,
            to = to,
        )
        advanceUntilIdle()

        val restoredViewModel = TransactionsViewModel(
            transactionRepository = fakeTransactionRepo,
            categoryRepository = fakeCategoryRepo,
            settingsRepository = fakeSettingsRepository,
            dispatcher = testDispatcher,
        )

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            restoredViewModel.state.collect {}
        }
        advanceUntilIdle()

        assertEquals(SavedDateFilter.CUSTOM_PRESET_INDEX, restoredViewModel.state.value.selectedPresetIndex)
        assertEquals(from, restoredViewModel.state.value.dateRangeFrom)
        assertEquals(to, restoredViewModel.state.value.dateRangeTo)
        collectJob.cancel()
    }

    @Test
    fun init_shouldUseDynamicDateRangeTo_whenRestoringNonCustomPreset() = runViewModelTest {
        // Simula un filtro salvato con preset non-custom e un "to" stantio (passato di 1 ora)
        val staleStoredTo = System.currentTimeMillis() - (60L * 60 * 1000)
        val staleStoredFrom = staleStoredTo - (7L * 24 * 60 * 60 * 1000)
        fakeSettingsRepository.transactionsDateFilterState.value = SavedDateFilter(
            presetIndex = 1, // "Last 7 days" preset, non-custom
            from = staleStoredFrom,
            to = staleStoredTo,
        )

        val restoredViewModel = TransactionsViewModel(
            transactionRepository = fakeTransactionRepo,
            categoryRepository = fakeCategoryRepo,
            settingsRepository = fakeSettingsRepository,
            dispatcher = testDispatcher,
        )

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
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

    @Test
    fun addTransaction_shouldAppearInFilteredTransactions_whenTimestampIsAfterStoredFilterTo() =
        runViewModelTest {
            // Cancel shared viewModel
            viewModel.viewModelScope.cancel()

            // Simula un "to" storato nel passato (1 ora fa)
            val staleStoredTo = System.currentTimeMillis() - (60L * 60 * 1000)
            val staleStoredFrom = staleStoredTo - (7L * 24 * 60 * 60 * 1000)
            fakeSettingsRepository.transactionsDateFilterState.value = SavedDateFilter(
                presetIndex = 1, // "Last 7 days", non-custom
                from = staleStoredFrom,
                to = staleStoredTo,
            )

            // Crea un ViewModel che caricherà il filtro stantio dallo storage
            val testViewModel = TransactionsViewModel(
                transactionRepository = fakeTransactionRepo,
                categoryRepository = fakeCategoryRepo,
                settingsRepository = fakeSettingsRepository,
                dispatcher = testDispatcher,
            )

            val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
                testViewModel.state.collect {}
            }
            advanceUntilIdle()

            // Inserisce una transazione con timestamp = adesso (> staleStoredTo)
            // IMPORTANTE: Use a slightly future timestamp to ensure it's not filtered out
            // if System.currentTimeMillis() inside ViewModel is called slightly before this.
            val transactionTimestamp = System.currentTimeMillis() + 1000L 
            testViewModel.addTransaction(
                title = "Caffè",
                amount = 2.0,
                category = "Food",
                type = TransactionType.EXPENSE,
                timestamp = transactionTimestamp,
            )
            advanceUntilIdle()

            // La transazione deve comparire subito in filteredTransactions senza necessità di refresh
            assertEquals(
                "La transazione deve comparire subito in filteredTransactions",
                1,
                testViewModel.state.value.filteredTransactions.size,
            )
            assertEquals("Caffè", testViewModel.state.value.filteredTransactions.first().title)
            collectJob.cancel()
        }

    @Test
    fun onEvent_shouldUpdatePendingFilters_whenFilterEventsAreReceived() = runViewModelTest {
        // Cancel shared viewModel
        viewModel.viewModelScope.cancel()

        val testViewModel = TransactionsViewModel(
            transactionRepository = fakeTransactionRepo,
            categoryRepository = fakeCategoryRepo,
            settingsRepository = fakeSettingsRepository,
            dispatcher = testDispatcher,
        )

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            testViewModel.state.collect {}
        }
        advanceUntilIdle()

        testViewModel.onEvent(TransactionsEvent.UpdateCategoryFilter("Work"))
        testViewModel.onEvent(TransactionsEvent.UpdateTransactionTypeFilter(TransactionType.INCOME))
        testViewModel.onEvent(TransactionsEvent.UpdatePaymentTypeFilter(PaymentType.CASH))
        advanceUntilIdle()

        assertEquals("Work", testViewModel.state.value.pendingCategory)
        assertEquals(TransactionType.INCOME, testViewModel.state.value.pendingTransactionType)
        assertEquals(PaymentType.CASH, testViewModel.state.value.pendingPaymentType)
        // Check that active filters are still null
        assertNull(testViewModel.state.value.selectedCategory)
        assertNull(testViewModel.state.value.selectedTransactionType)
        assertNull(testViewModel.state.value.selectedPaymentType)

        collectJob.cancel()
    }

    @Test
    fun applyFilters_shouldMakePendingFiltersActive_whenCalled() = runViewModelTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(TransactionsEvent.UpdateCategoryFilter("Work"))
        viewModel.onEvent(TransactionsEvent.ApplyFilters)
        advanceUntilIdle()

        assertEquals("Work", viewModel.state.value.selectedCategory)
        collectJob.cancel()
    }

    @Test
    fun cancelFilterChanges_shouldResetPendingToActive_whenCalled() = runViewModelTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(TransactionsEvent.UpdateCategoryFilter("Work"))
        advanceUntilIdle() // Ensure state is updated
        assertEquals("Work", viewModel.state.value.pendingCategory)
        assertNull(viewModel.state.value.selectedCategory)

        viewModel.onEvent(TransactionsEvent.CancelFilterChanges)
        advanceUntilIdle()

        assertNull(viewModel.state.value.pendingCategory)
        collectJob.cancel()
    }

    @Test
    fun clearAllFilters_shouldResetAllFilters_whenCalled() = runViewModelTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(TransactionsEvent.UpdateCategoryFilter("Work"))
        viewModel.onEvent(TransactionsEvent.ApplyFilters)
        viewModel.onEvent(TransactionsEvent.UpdateSearchQuery("Salary"))
        advanceUntilIdle()

        assertEquals("Work", viewModel.state.value.selectedCategory)
        assertEquals("Salary", viewModel.state.value.searchQuery)

        viewModel.onEvent(TransactionsEvent.ClearAllFilters)
        advanceUntilIdle()

        assertNull(viewModel.state.value.selectedCategory)
        assertEquals("", viewModel.state.value.searchQuery)
        assertNull(viewModel.state.value.pendingCategory)
        assertEquals("", viewModel.state.value.pendingSearchQuery)

        collectJob.cancel()
    }

    @Test
    fun toggleEvents_shouldUpdateExpansionState_whenReceived() = runViewModelTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isSearchExpanded)
        viewModel.onEvent(TransactionsEvent.ToggleSearchExpanded)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isSearchExpanded)

        assertFalse(viewModel.state.value.isFiltersExpanded)
        viewModel.onEvent(TransactionsEvent.ToggleFiltersExpanded)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isFiltersExpanded)

        collectJob.cancel()
    }

    @Test
    fun selectPreset_shouldUpdateDateRange_whenCalled() = runViewModelTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        // Index 0 is "Today" usually (depends on implementation in TransactionsState, let's check)
        // For now just check it changes from the default (index 1)
        viewModel.onEvent(TransactionsEvent.SelectPreset(0))
        advanceUntilIdle()

        assertEquals(0, viewModel.state.value.selectedPresetIndex)
        assertEquals(0, fakeSettingsRepository.transactionsDateFilterState.value.presetIndex)

        collectJob.cancel()
    }

    @Test
    fun setDateRange_shouldNormalizeDates_whenFromIsGreaterThanTo() = runViewModelTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        val from = 2000L
        val to = 1000L
        viewModel.onEvent(TransactionsEvent.SetDateRange(from, to))
        advanceUntilIdle()

        assertEquals(1000L, viewModel.state.value.dateRangeFrom)
        assertEquals(2000L, viewModel.state.value.dateRangeTo)

        collectJob.cancel()
    }

    @Test
    fun filteredTransactions_shouldEnrichWithCategoryData_whenCategoryExistsInRepo() = runViewModelTest {
        val now = System.currentTimeMillis()
        val category = Category(id = 1, name = "Food", icon = "restaurant", color = 0xFFFF0000)
        fakeCategoryRepo.categories.value = listOf(category)

        val transaction = Transaction(
            id = 1,
            title = "Lunch",
            amount = 10.0,
            category = "Food",
            type = TransactionType.EXPENSE,
            timestamp = now,
            categoryIcon = "", // Missing data
            categoryColor = 0xFF90A4AE // Default color
        )
        fakeTransactionRepo.transactions.value = listOf(transaction)

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        val enriched = viewModel.state.value.filteredTransactions.first()
        assertEquals("restaurant", enriched.categoryIcon)
        assertEquals(0xFFFF0000, enriched.categoryColor)

        collectJob.cancel()
    }

    @Test
    fun searchSuggestions_shouldProvideSuggestions_whenQueryIsNotEmpty() = runViewModelTest {
        val now = System.currentTimeMillis()
        fakeTransactionRepo.transactions.value = listOf(
            Transaction(id = 1, title = "Groceries", amount = 10.0, category = "Food", type = TransactionType.EXPENSE, timestamp = now),
            Transaction(id = 2, title = "Green Tea", amount = 5.0, category = "Food", type = TransactionType.EXPENSE, timestamp = now)
        )

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(TransactionsEvent.UpdateSearchQuery("Gr"))
        advanceUntilIdle()

        val suggestions = viewModel.state.value.searchSuggestions
        assertTrue(suggestions.contains("Groceries"))
        assertTrue(suggestions.contains("Green Tea"))
        assertFalse(suggestions.contains("Gr")) // Should not contain exact match

        collectJob.cancel()
    }

    @Test
    fun searchSuggestions_shouldBeEmpty_whenQueryIsBlank() = runViewModelTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(TransactionsEvent.UpdateSearchQuery("   "))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.searchSuggestions.isEmpty())
        collectJob.cancel()
    }

    @Test
    fun searchSuggestions_shouldLimitHistoryTo3AndCombineWithSuggestions() = runViewModelTest {
        val now = System.currentTimeMillis()
        // History: 4 matching titles
        fakeTransactionRepo.transactions.value = listOf(
            Transaction(id = 1, title = "Apple", amount = 1.0, category = "Food", type = TransactionType.EXPENSE, timestamp = now),
            Transaction(id = 2, title = "Apricot", amount = 1.0, category = "Food", type = TransactionType.EXPENSE, timestamp = now),
            Transaction(id = 3, title = "Application", amount = 1.0, category = "Tech", type = TransactionType.EXPENSE, timestamp = now),
            Transaction(id = 4, title = "Apartment", amount = 1.0, category = "Rent", type = TransactionType.EXPENSE, timestamp = now),
        )
        // Suggestions: 2 matching titles (one duplicate with history)
        fakeTransactionRepo.distinctTitles.value = listOf("Apple", "Appendix")

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.onEvent(TransactionsEvent.UpdateSearchQuery("Ap"))
        advanceUntilIdle()

        val suggestions = viewModel.state.value.searchSuggestions
        // Should have 3 from history + 1 from suggestions (Appendix) = 4 total
        // "Apartment" is the 4th in history, so it should be skipped if history is limited to 3
        assertEquals(4, suggestions.size)
        assertTrue(suggestions.contains("Apple"))
        assertTrue(suggestions.contains("Apricot"))
        assertTrue(suggestions.contains("Application"))
        assertTrue(suggestions.contains("Appendix"))
        assertFalse(suggestions.contains("Apartment")) // Limited to 3 from history

        collectJob.cancel()
    }
}

// ── Fake Repositories ──

private class FakeTransactionRepository : TransactionRepository {
    val transactions = MutableStateFlow<List<Transaction>>(emptyList())
    @get:JvmName("mutableDistinctTitles")
    val distinctTitles = MutableStateFlow<List<String>>(emptyList())

    override fun getAllTransactions(): Flow<List<Transaction>> = transactions

    override suspend fun getTransactionById(id: Long): Transaction? =
        transactions.value.find { it.id == id }

    override suspend fun insertTransaction(transaction: Transaction): Long {
        transactions.value += transaction
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

    override fun getDistinctTitles() = distinctTitles
    override fun getDistinctPayees() = flowOf(emptyList<String>())
    override fun getDistinctNotes() = flowOf(emptyList<String>())
    override fun getDistinctLocations() = flowOf(emptyList<String>())
    override fun getDistinctTags() = flowOf(emptyList<String>())
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

    override suspend fun getCategoryByName(name: String): Category? =
        categories.value.find { it.name == name }
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

    @get:JvmName("mutableTransactionsDateFilterState")
    val transactionsDateFilterState = MutableStateFlow(
        SavedDateFilter(
            presetIndex = 1,
            from = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000),
            to = System.currentTimeMillis(),
        ),
    )

    @get:JvmName("mutableChartsDateFilterState")
    val chartsDateFilterState = MutableStateFlow(
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
    override fun getMealVoucherValue(): Flow<Double> {
        TODO("Not yet implemented")
    }

    override suspend fun setMealVoucherValue(value: Double) {
        TODO("Not yet implemented")
    }

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
    override fun getTransactionDisplayType() = flowOf(com.antcashmanager.domain.model.TransactionDisplayType.TREND)
    override suspend fun setTransactionDisplayType(displayType: com.antcashmanager.domain.model.TransactionDisplayType) = Unit
    override fun getTransactionsTransactionDisplayType() =
        flowOf(com.antcashmanager.domain.model.TransactionDisplayType.TREND)

    override suspend fun setTransactionsTransactionDisplayType(displayType: com.antcashmanager.domain.model.TransactionDisplayType) = Unit
    override fun getShowInitialAnimation() = flowOf(true)
    override suspend fun setShowInitialAnimation(show: Boolean) = Unit
    override fun getIsTutorialCompleted() = flowOf(true)
    override suspend fun setIsTutorialCompleted(completed: Boolean) = Unit
    override fun getDataEncryptionEnabled() = flowOf(false)
    override suspend fun setDataEncryptionEnabled(enabled: Boolean) = Unit
    override suspend fun resetAllPreferences() = Unit
}

