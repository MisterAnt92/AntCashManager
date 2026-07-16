package com.antcashmanager.android.ui.settings

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.ui.screen.settings.SettingsViewModel
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest : BaseUnitTest() {
    private lateinit var fakeSettingsRepo: FakeSettingsRepository
    private lateinit var fakeTransactionRepo: FakeTransactionRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        fakeSettingsRepo = FakeSettingsRepository()
        fakeTransactionRepo = FakeTransactionRepository()
        viewModel = SettingsViewModel(
            settingsRepository = fakeSettingsRepo,
            transactionRepository = fakeTransactionRepo,
            useCaseDispatcher = testDispatcher,
        )
    }

    @Test
    fun `initial theme is SYSTEM`() = runViewModelTest {
        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()
        assertEquals(AppTheme.SYSTEM, viewModel.state.value.theme)
        collectJob.cancel()
    }

    @Test
    fun `setTheme updates theme to DARK`() = runViewModelTest {
        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.setTheme(AppTheme.DARK)
        advanceUntilIdle()

        assertEquals(AppTheme.DARK, viewModel.state.value.theme)
        collectJob.cancel()
    }

    @Test
    fun `setTheme updates theme to LIGHT`() = runViewModelTest {
        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.setTheme(AppTheme.LIGHT)
        advanceUntilIdle()

        assertEquals(AppTheme.LIGHT, viewModel.state.value.theme)
        collectJob.cancel()
    }

    @Test
    fun `setTheme can switch between themes`() = runViewModelTest {
        fun awaitTheme(expected: AppTheme) {
            repeat(5) {
                advanceUntilIdle()
                if (viewModel.state.value.theme == expected) {
                    return
                }
            }
            fail("Expected theme=$expected but was ${viewModel.state.value.theme}")
        }

        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.setTheme(AppTheme.DARK)
        awaitTheme(AppTheme.DARK)

        viewModel.setTheme(AppTheme.LIGHT)
        awaitTheme(AppTheme.LIGHT)

        viewModel.setTheme(AppTheme.SYSTEM)
        awaitTheme(AppTheme.SYSTEM)

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
    override suspend fun setShowCharts(show: Boolean) {
        showChartsFlow.value = show
    }

    override fun getHighContrast(): Flow<Boolean> = highContrastFlow
    override suspend fun setHighContrast(enabled: Boolean) {
        highContrastFlow.value = enabled
    }

    override fun getLargeText(): Flow<Boolean> = largeTextFlow
    override suspend fun setLargeText(enabled: Boolean) {
        largeTextFlow.value = enabled
    }

    override fun getReduceMotion(): Flow<Boolean> = reduceMotionFlow
    override suspend fun setReduceMotion(enabled: Boolean) {
        reduceMotionFlow.value = enabled
    }

    // Additional preferences required by SettingsRepository
    private val showTransactionNotesFlow = MutableStateFlow(true)
    private val currencySymbolFlow = MutableStateFlow("\u20ac")
    private val decimalDigitsFlow = MutableStateFlow(2)
    private val decimalSeparatorFlow = MutableStateFlow(",")
    private val thousandsSeparatorFlow = MutableStateFlow(".")

    override fun getShowTransactionNotes(): Flow<Boolean> = showTransactionNotesFlow
    override suspend fun setShowTransactionNotes(show: Boolean) {
        showTransactionNotesFlow.value = show
    }

    override fun getCurrencySymbol(): Flow<String> = currencySymbolFlow
    override suspend fun setCurrencySymbol(symbol: String) {
        currencySymbolFlow.value = symbol
    }

    override fun getDecimalDigits(): Flow<Int> = decimalDigitsFlow
    override suspend fun setDecimalDigits(digits: Int) {
        decimalDigitsFlow.value = digits
    }

    override fun getDecimalSeparator(): Flow<String> = decimalSeparatorFlow
    override suspend fun setDecimalSeparator(separator: String) {
        decimalSeparatorFlow.value = separator
    }

    override fun getThousandsSeparator(): Flow<String> = thousandsSeparatorFlow
    override suspend fun setThousandsSeparator(separator: String) {
        thousandsSeparatorFlow.value = separator
    }

    override fun getMealVoucherValue(): Flow<Double> {
        TODO("Not yet implemented")
    }

    override suspend fun setMealVoucherValue(value: Double) {
        TODO("Not yet implemented")
    }

    private val dateFormatFlow = MutableStateFlow("dd/MM/yyyy")
    private val showPaymentTypeBreakdownFlow = MutableStateFlow(true)
    private val transactionDisplayTypeFlow = MutableStateFlow(TransactionDisplayType.TREND)

    override fun getDateFormat(): Flow<String> = dateFormatFlow

    override suspend fun setDateFormat(pattern: String) {
        dateFormatFlow.value = pattern
    }

    override fun getShowPaymentTypeBreakdown(): Flow<Boolean> = showPaymentTypeBreakdownFlow

    override suspend fun setShowPaymentTypeBreakdown(show: Boolean) {
        showPaymentTypeBreakdownFlow.value = show
    }

    override fun getTransactionDisplayType(): Flow<TransactionDisplayType> =
        transactionDisplayTypeFlow

    override suspend fun setTransactionDisplayType(displayType: TransactionDisplayType) {
        transactionDisplayTypeFlow.value = displayType
    }

    override fun getTransactionsTransactionDisplayType(): Flow<TransactionDisplayType> =
        transactionDisplayTypeFlow

    override suspend fun setTransactionsTransactionDisplayType(displayType: TransactionDisplayType) {
        transactionDisplayTypeFlow.value = displayType
    }

    override fun getHomeDateFilterPreset(): Flow<Int> = flowOf(1)
    override suspend fun setHomeDateFilterPreset(index: Int) {}
    override fun getHomeDateFilterState(): Flow<SavedDateFilter> = flowOf(
        SavedDateFilter(1, System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000), System.currentTimeMillis())
    )

    override suspend fun setHomeDateFilterState(filter: SavedDateFilter) {}
    override fun getTransactionsDateFilterPreset(): Flow<Int> = flowOf(1)
    override suspend fun setTransactionsDateFilterPreset(index: Int) {}
    override fun getTransactionsDateFilterState(): Flow<SavedDateFilter> = flowOf(
        SavedDateFilter(1, System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000), System.currentTimeMillis())
    )

    override suspend fun setTransactionsDateFilterState(filter: SavedDateFilter) {}
    override fun getChartsDateFilterPreset(): Flow<Int> = flowOf(1)
    override suspend fun setChartsDateFilterPreset(index: Int) {}
    override fun getChartsDateFilterState(): Flow<SavedDateFilter> = flowOf(
        SavedDateFilter(1, System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000), System.currentTimeMillis())
    )

    override suspend fun setChartsDateFilterState(filter: SavedDateFilter) {}
    override fun getChartsZoomEnabled(): Flow<Boolean> = flowOf(true)
    override suspend fun setChartsZoomEnabled(enabled: Boolean) {}

    private val showInitialAnimationFlow = MutableStateFlow(true)
    override fun getShowInitialAnimation(): Flow<Boolean> = showInitialAnimationFlow
    override suspend fun setShowInitialAnimation(show: Boolean) {
        showInitialAnimationFlow.value = show
    }

    override fun getIsTutorialCompleted(): Flow<Boolean> = flowOf(true)
    override suspend fun setIsTutorialCompleted(completed: Boolean) {}

    private val dataEncryptionEnabledFlow = MutableStateFlow(false)

    override fun getDataEncryptionEnabled(): Flow<Boolean> = dataEncryptionEnabledFlow

    override suspend fun setDataEncryptionEnabled(enabled: Boolean) {
        dataEncryptionEnabledFlow.value = enabled
    }

    private val dateFilterExpandedFlow = MutableStateFlow(false)

    override fun getDateFilterExpanded(): Flow<Boolean> = dateFilterExpandedFlow
    override suspend fun setDateFilterExpanded(expanded: Boolean) {
        dateFilterExpandedFlow.value = expanded
    }

    override suspend fun resetAllPreferences() {
        // reset to defaults
        themeFlow.value = AppTheme.SYSTEM
        languageFlow.value = AppLanguage.SYSTEM
        showChartsFlow.value = true
        highContrastFlow.value = false
        largeTextFlow.value = false
        reduceMotionFlow.value = false
        showTransactionNotesFlow.value = true
        currencySymbolFlow.value = "\u20ac"
        decimalDigitsFlow.value = 2
        decimalSeparatorFlow.value = ","
        thousandsSeparatorFlow.value = "."
        dateFormatFlow.value = "dd/MM/yyyy"
        dateFilterExpandedFlow.value = false
        showPaymentTypeBreakdownFlow.value = true
        transactionDisplayTypeFlow.value = TransactionDisplayType.TREND
        dataEncryptionEnabledFlow.value = false
    }
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
        transactions.value =
            transactions.value.map { if (it.id == transaction.id) transaction else it }
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

    override suspend fun renameCategory(oldCategoryName: String, newCategoryName: String, icon: String, color: Long) {
        // No-op for test
    }

    override fun getDistinctTitles() = flowOf(emptyList<String>())
    override fun getDistinctPayees() = flowOf(emptyList<String>())
    override fun getDistinctNotes() = flowOf(emptyList<String>())
    override fun getDistinctLocations() = flowOf(emptyList<String>())
    override fun getDistinctTags() = flowOf(emptyList<String>())
}
