package com.antcashmanager.android.ui.settings

import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
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
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeSettingsRepo: FakeSettingsRepository
    private lateinit var fakeTransactionRepo: FakeTransactionRepository
    private lateinit var fakeCategoryRepo: FakeCategoryRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeSettingsRepo = FakeSettingsRepository()
        fakeTransactionRepo = FakeTransactionRepository()
        fakeCategoryRepo = FakeCategoryRepository()
        viewModel = SettingsViewModel(fakeSettingsRepo, fakeTransactionRepo, fakeCategoryRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial theme is SYSTEM`() = runTest(testDispatcher) {
        // Avvia un collector per attivare WhileSubscribed
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.theme.collect {}
        }
        advanceUntilIdle()
        assertEquals(AppTheme.SYSTEM, viewModel.theme.value)
        collectJob.cancel()
    }

    @Test
    fun `setTheme updates theme to DARK`() = runTest(testDispatcher) {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.theme.collect {}
        }
        advanceUntilIdle()

        viewModel.setTheme(AppTheme.DARK)
        advanceUntilIdle()

        assertEquals(AppTheme.DARK, viewModel.theme.value)
        collectJob.cancel()
    }

    @Test
    fun `setTheme updates theme to LIGHT`() = runTest(testDispatcher) {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.theme.collect {}
        }
        advanceUntilIdle()

        viewModel.setTheme(AppTheme.LIGHT)
        advanceUntilIdle()

        assertEquals(AppTheme.LIGHT, viewModel.theme.value)
        collectJob.cancel()
    }

    @Test
    fun `setTheme can switch between themes`() = runTest(testDispatcher) {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.theme.collect {}
        }
        advanceUntilIdle()

        viewModel.setTheme(AppTheme.DARK)
        advanceUntilIdle()
        assertEquals(AppTheme.DARK, viewModel.theme.value)

        viewModel.setTheme(AppTheme.LIGHT)
        advanceUntilIdle()
        assertEquals(AppTheme.LIGHT, viewModel.theme.value)

        viewModel.setTheme(AppTheme.SYSTEM)
        advanceUntilIdle()
        assertEquals(AppTheme.SYSTEM, viewModel.theme.value)

        collectJob.cancel()
    }

    @Test
    fun `deleteAllData clears transactions and categories`() = runTest(testDispatcher) {
        // Add some test data
        fakeTransactionRepo.transactions.value = listOf(
            Transaction(id = 1, title = "Test", amount = 100.0, category = "Food", type = com.antcashmanager.domain.model.TransactionType.EXPENSE),
        )
        fakeCategoryRepo.categories.value = listOf(
            Category(id = 1, name = "Food", icon = "category", color = 0xFFE57373),
        )

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.deleteResult.collect {}
        }

        viewModel.deleteAllData()
        advanceUntilIdle()

        assertTrue(fakeTransactionRepo.transactions.value.isEmpty())
        assertTrue(fakeCategoryRepo.categories.value.isEmpty())
        collectJob.cancel()
    }
}

// ── Fake Repositories ──

private class FakeSettingsRepository : SettingsRepository {
    private val themeFlow = MutableStateFlow(AppTheme.SYSTEM)
    private val languageFlow = MutableStateFlow(AppLanguage.SYSTEM)
    private val showChartsFlow = MutableStateFlow(true)
    private val highContrastFlow = MutableStateFlow(false)
    private val largeTextFlow = MutableStateFlow(false)
    private val reduceMotionFlow = MutableStateFlow(false)

    override fun getTheme(): Flow<AppTheme> = themeFlow

    override suspend fun setTheme(theme: AppTheme) {
        themeFlow.value = theme
    }

    override fun getLanguage(): Flow<AppLanguage> = languageFlow

    override suspend fun setLanguage(language: AppLanguage) {
        languageFlow.value = language
    }

    override fun getShowCharts(): Flow<Boolean> = showChartsFlow
    override suspend fun setShowCharts(show: Boolean) { showChartsFlow.value = show }
    override fun getHighContrast(): Flow<Boolean> = highContrastFlow
    override suspend fun setHighContrast(enabled: Boolean) { highContrastFlow.value = enabled }
    override fun getLargeText(): Flow<Boolean> = largeTextFlow
    override suspend fun setLargeText(enabled: Boolean) { largeTextFlow.value = enabled }
    override fun getReduceMotion(): Flow<Boolean> = reduceMotionFlow
    override suspend fun setReduceMotion(enabled: Boolean) { reduceMotionFlow.value = enabled }
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
        transactions.value = transactions.value.map { if (it.id == transaction.id) transaction else it }
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactions.value = transactions.value.filter { it.id != transaction.id }
    }

    override suspend fun deleteAllTransactions() {
        transactions.value = emptyList()
    }

    override fun getTransactionsByDateRange(from: Long, to: Long): Flow<List<Transaction>> =
        transactions.map { list -> list.filter { it.timestamp in from..to } }
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
        categories.value = categories.value.map { if (it.id == category.id) category else it }
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
}
