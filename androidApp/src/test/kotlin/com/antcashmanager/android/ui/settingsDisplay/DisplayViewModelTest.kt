package com.antcashmanager.android.ui.settingsDisplay

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.ui.screen.settingsDisplay.DisplayViewModel
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.repository.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DisplayViewModelTest : BaseUnitTest() {

    private lateinit var fakeSettingsRepo: FakeSettingsRepository
    private lateinit var viewModel: DisplayViewModel

    @Before
    fun setup() {
        fakeSettingsRepo = FakeSettingsRepository()
        viewModel = DisplayViewModel(fakeSettingsRepo)
    }

    @Test
    fun `defaults are exposed correctly`() = runViewModelTest {
        val collectJob = launch(
            UnconfinedTestDispatcher(testScheduler)
        ) {
            // subscribe to flows to ensure stateIn is active
            launch { viewModel.currencySymbol.collect { } }
            launch { viewModel.decimalDigits.collect { } }
            launch { viewModel.decimalSeparator.collect { } }
            launch { viewModel.thousandsSeparator.collect { } }
        }

        advanceUntilIdle()

        assertEquals("\u20ac", viewModel.currencySymbol.value)
        assertEquals(2, viewModel.decimalDigits.value)
        assertEquals(",", viewModel.decimalSeparator.value)
        assertEquals("", viewModel.thousandsSeparator.value)

        collectJob.cancel()
    }

    @Test
    fun `setCurrencySymbol updates value`() = runViewModelTest {
        val collectJob = launch(
            UnconfinedTestDispatcher(testScheduler)
        ) { launch { viewModel.currencySymbol.collect { } } }
        advanceUntilIdle()

        viewModel.setCurrencySymbol("$")
        advanceUntilIdle()

        assertEquals("$", viewModel.currencySymbol.value)
        collectJob.cancel()
    }

    @Test
    fun `setDecimalDigits updates value`() = runViewModelTest {
        val collectJob = launch(
            UnconfinedTestDispatcher(testScheduler)
        ) { launch { viewModel.decimalDigits.collect { } } }
        advanceUntilIdle()

        viewModel.setDecimalDigits(0)
        advanceUntilIdle()

        assertEquals(0, viewModel.decimalDigits.value)
        collectJob.cancel()
    }

    @Test
    fun `setSeparators update values and reset restores defaults`() = runViewModelTest {
        val collectJob = launch(
            UnconfinedTestDispatcher(testScheduler)
        ) {
            launch { viewModel.decimalSeparator.collect { } }
            launch { viewModel.thousandsSeparator.collect { } }
        }
        advanceUntilIdle()

        viewModel.setDecimalSeparator(".")
        viewModel.setThousandsSeparator(",")
        advanceUntilIdle()

        assertEquals(".", viewModel.decimalSeparator.value)
        assertEquals(",", viewModel.thousandsSeparator.value)

        viewModel.resetAllPreferences()
        advanceUntilIdle()

        // reset should restore to the repository defaults used in Fake
        assertEquals("\u20ac", viewModel.currencySymbol.value)
        assertEquals(2, viewModel.decimalDigits.value)
        assertEquals(",", viewModel.decimalSeparator.value)
        assertEquals("", viewModel.thousandsSeparator.value)

        collectJob.cancel()
    }

    @Test
    fun `currency inputs are sanitized`() = runViewModelTest {
        val collectJob = launch(
            UnconfinedTestDispatcher(testScheduler)
        ) {
            launch { viewModel.currencySymbol.collect { } }
            launch { viewModel.decimalDigits.collect { } }
            launch { viewModel.decimalSeparator.collect { } }
            launch { viewModel.thousandsSeparator.collect { } }
        }
        advanceUntilIdle()

        viewModel.setCurrencySymbol("INVALID")
        viewModel.setDecimalDigits(9)
        viewModel.setThousandsSeparator("INVALID")
        advanceUntilIdle()

        assertEquals("\u20ac", viewModel.currencySymbol.value)
        assertEquals(4, viewModel.decimalDigits.value)
        assertEquals("", viewModel.thousandsSeparator.value)

        viewModel.setDecimalDigits(-5)
        advanceUntilIdle()
        assertEquals(0, viewModel.decimalDigits.value)

        collectJob.cancel()
    }

    @Test
    fun `decimal and thousands separators cannot be equal`() = runViewModelTest {
        val collectJob = launch(
            UnconfinedTestDispatcher(testScheduler)
        ) {
            launch { viewModel.decimalSeparator.collect { } }
            launch { viewModel.thousandsSeparator.collect { } }
        }
        advanceUntilIdle()

        viewModel.setThousandsSeparator(".")
        advanceUntilIdle()
        assertEquals(".", viewModel.thousandsSeparator.value)

        viewModel.setDecimalSeparator(".")
        advanceUntilIdle()

        assertEquals(".", viewModel.decimalSeparator.value)
        assertEquals("", viewModel.thousandsSeparator.value)

        collectJob.cancel()
    }

}

// ── Fake repository for testing DisplayViewModel ──
private class FakeSettingsRepository : SettingsRepository {
    private val currencySymbolFlow = MutableStateFlow("\u20ac")
    private val decimalDigitsFlow = MutableStateFlow(2)
    private val decimalSeparatorFlow = MutableStateFlow(",")
    private val thousandsSeparatorFlow = MutableStateFlow("")
    private val showChartsFlow = MutableStateFlow(true)
    private val showTransactionNotesFlow = MutableStateFlow(true)
    private val dateFormatFlow = MutableStateFlow("dd/MM/yyyy")
    private val showPaymentTypeBreakdownFlow = MutableStateFlow(true)
    private val transactionDisplayTypeFlow = MutableStateFlow(TransactionDisplayType.TREND)
    private val dateFilterExpandedFlow = MutableStateFlow(false)
    private val chartsZoomEnabledFlow = MutableStateFlow(false)
    private val isTutorialCompletedFlow = MutableStateFlow(false)

    override fun getTheme() = throw UnsupportedOperationException()
    override suspend fun setTheme(theme: com.antcashmanager.domain.model.AppTheme) = Unit
    override fun getLanguage() = throw UnsupportedOperationException()
    override suspend fun setLanguage(language: com.antcashmanager.domain.model.AppLanguage) = Unit
    override fun getShowCharts() = showChartsFlow
    override suspend fun setShowCharts(show: Boolean) {
        showChartsFlow.value = show
    }

    override fun getHighContrast() = throw UnsupportedOperationException()
    override suspend fun setHighContrast(enabled: Boolean) = Unit
    override fun getLargeText() = throw UnsupportedOperationException()
    override suspend fun setLargeText(enabled: Boolean) = Unit
    override fun getReduceMotion() = throw UnsupportedOperationException()
    override suspend fun setReduceMotion(enabled: Boolean) = Unit
    override fun getShowTransactionNotes() = showTransactionNotesFlow
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

    override fun getDateFormat(): Flow<String> = dateFormatFlow
    override suspend fun setDateFormat(pattern: String) {
        dateFormatFlow.value = pattern
    }

    override fun getDateFilterExpanded(): Flow<Boolean> = dateFilterExpandedFlow
    override suspend fun setDateFilterExpanded(expanded: Boolean) {
        dateFilterExpandedFlow.value = expanded
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

    override fun getTransactionsDateFilterPreset(): Flow<Int> =
        throw UnsupportedOperationException()

    override suspend fun setTransactionsDateFilterPreset(index: Int) {}
    override fun getTransactionsDateFilterState(): Flow<SavedDateFilter> =
        throw UnsupportedOperationException()

    override suspend fun setTransactionsDateFilterState(filter: SavedDateFilter) {}

    override fun getChartsDateFilterPreset(): Flow<Int> = throw UnsupportedOperationException()
    override suspend fun setChartsDateFilterPreset(index: Int) {}
    override fun getChartsDateFilterState(): Flow<SavedDateFilter> = throw UnsupportedOperationException()
    override suspend fun setChartsDateFilterState(filter: SavedDateFilter) {}

    override fun getHomeDateFilterPreset(): Flow<Int> = throw UnsupportedOperationException()
    override suspend fun setHomeDateFilterPreset(index: Int) {}
    override fun getHomeDateFilterState(): Flow<SavedDateFilter> = throw UnsupportedOperationException()
    override suspend fun setHomeDateFilterState(filter: SavedDateFilter) {}

    override fun getChartsZoomEnabled(): Flow<Boolean> = chartsZoomEnabledFlow
    override suspend fun setChartsZoomEnabled(enabled: Boolean) {
        chartsZoomEnabledFlow.value = enabled
    }

    override fun getIsTutorialCompleted(): Flow<Boolean> = isTutorialCompletedFlow
    override suspend fun setIsTutorialCompleted(completed: Boolean) {
        isTutorialCompletedFlow.value = completed
    }

    private val dataEncryptionEnabledFlow = MutableStateFlow(false)

    override fun getDataEncryptionEnabled(): Flow<Boolean> = dataEncryptionEnabledFlow

    override suspend fun setDataEncryptionEnabled(enabled: Boolean) {
        dataEncryptionEnabledFlow.value = enabled
    }

    override suspend fun resetAllPreferences() {
        currencySymbolFlow.value = "\u20ac"
        decimalDigitsFlow.value = 2
        decimalSeparatorFlow.value = ","
        thousandsSeparatorFlow.value = ""
        showChartsFlow.value = true
        showTransactionNotesFlow.value = true
        dateFormatFlow.value = "dd/MM/yyyy"
        showPaymentTypeBreakdownFlow.value = true
        transactionDisplayTypeFlow.value = TransactionDisplayType.TREND
        dateFilterExpandedFlow.value = false
        chartsZoomEnabledFlow.value = false
        isTutorialCompletedFlow.value = false
        dataEncryptionEnabledFlow.value = false
    }
}
