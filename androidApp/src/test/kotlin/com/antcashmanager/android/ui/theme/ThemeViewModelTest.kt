package com.antcashmanager.android.ui.theme

import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeSettingsRepository
    private lateinit var viewModel: ThemeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeSettingsRepository()
        viewModel = ThemeViewModel(fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `default theme exposed`() = runTest(testDispatcher) {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.appTheme.collect {}
        }
        advanceUntilIdle()

        assertEquals(AppTheme.SYSTEM, viewModel.appTheme.value)

        collectJob.cancel()
    }

    @Test
    fun `setTheme updates repository`() = runTest(testDispatcher) {
        viewModel.setTheme(AppTheme.DARK)
        advanceUntilIdle()

        // Fake repo mirrors setTheme into its flow
        assertEquals(AppTheme.DARK, fakeRepo.themeFlow.value)
    }
}

private class FakeSettingsRepository : SettingsRepository {
    val themeFlow = MutableStateFlow(AppTheme.SYSTEM)
    private val highContrastFlow = MutableStateFlow(false)
    private val largeTextFlow = MutableStateFlow(false)
    private val reduceMotionFlow = MutableStateFlow(false)

    override fun getTheme(): Flow<AppTheme> = themeFlow
    override suspend fun setTheme(theme: AppTheme) { themeFlow.value = theme }

    override fun getLanguage() = throw UnsupportedOperationException()
    override suspend fun setLanguage(language: com.antcashmanager.domain.model.AppLanguage) = Unit
    override fun getShowCharts() = throw UnsupportedOperationException()
    override suspend fun setShowCharts(show: Boolean) = Unit
    override fun getHighContrast(): Flow<Boolean> = highContrastFlow
    override suspend fun setHighContrast(enabled: Boolean) { highContrastFlow.value = enabled }
    override fun getLargeText(): Flow<Boolean> = largeTextFlow
    override suspend fun setLargeText(enabled: Boolean) { largeTextFlow.value = enabled }
    override fun getReduceMotion(): Flow<Boolean> = reduceMotionFlow
    override suspend fun setReduceMotion(enabled: Boolean) { reduceMotionFlow.value = enabled }
    override fun getShowTransactionNotes() = throw UnsupportedOperationException()
    override suspend fun setShowTransactionNotes(show: Boolean) = Unit
    override fun getCurrencySymbol() = throw UnsupportedOperationException()
    override suspend fun setCurrencySymbol(symbol: String) = Unit
    override fun getDecimalDigits() = throw UnsupportedOperationException()
    override suspend fun setDecimalDigits(digits: Int) = Unit
    override fun getDecimalSeparator() = throw UnsupportedOperationException()
    override suspend fun setDecimalSeparator(separator: String) = Unit
    override fun getThousandsSeparator() = throw UnsupportedOperationException()
    override suspend fun setThousandsSeparator(separator: String) = Unit
    override fun getDateFormat() = throw UnsupportedOperationException()
    override suspend fun setDateFormat(pattern: String) = Unit
    override fun getDateFilterExpanded() = throw UnsupportedOperationException()
    override suspend fun setDateFilterExpanded(expanded: Boolean) = Unit
    override fun getShowPaymentTypeBreakdown() = throw UnsupportedOperationException()
    override suspend fun setShowPaymentTypeBreakdown(show: Boolean) = Unit
    override fun getTransactionDisplayType() = throw UnsupportedOperationException()
    override suspend fun setTransactionDisplayType(displayType: com.antcashmanager.domain.model.TransactionDisplayType) = Unit
    override fun getTransactionsTransactionDisplayType() = throw UnsupportedOperationException()
    override suspend fun setTransactionsTransactionDisplayType(displayType: com.antcashmanager.domain.model.TransactionDisplayType) = Unit
    override fun getTransactionsDateFilterPreset(): Flow<Int> = throw UnsupportedOperationException()
    override suspend fun setTransactionsDateFilterPreset(index: Int) {}

    override fun getChartsDateFilterPreset(): Flow<Int> = throw UnsupportedOperationException()
    override suspend fun setChartsDateFilterPreset(index: Int) {}

    override fun getHomeDateFilterPreset(): Flow<Int> = throw UnsupportedOperationException()
    override suspend fun setHomeDateFilterPreset(index: Int) {}

    override fun getChartsZoomEnabled() = throw UnsupportedOperationException()
    override suspend fun setChartsZoomEnabled(enabled: Boolean) = Unit

    override fun getIsTutorialCompleted() = throw UnsupportedOperationException()
    override suspend fun setIsTutorialCompleted(completed: Boolean) = Unit

    override suspend fun resetAllPreferences() = Unit
}

