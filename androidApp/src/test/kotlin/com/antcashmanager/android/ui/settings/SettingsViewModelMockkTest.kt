package com.antcashmanager.android.ui.settings

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.data.feedback.FeedbackEmailHelper
import com.antcashmanager.android.ui.screen.settings.SettingsViewModel
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.usecase.settings.GetCurrencySymbolUseCase
import com.antcashmanager.domain.usecase.settings.GetDecimalDigitsUseCase
import com.antcashmanager.domain.usecase.settings.GetDecimalSeparatorUseCase
import com.antcashmanager.domain.usecase.settings.GetHighContrastUseCase
import com.antcashmanager.domain.usecase.settings.GetLanguageUseCase
import com.antcashmanager.domain.usecase.settings.GetLargeTextUseCase
import com.antcashmanager.domain.usecase.settings.GetReduceMotionUseCase
import com.antcashmanager.domain.usecase.settings.GetShowChartsUseCase
import com.antcashmanager.domain.usecase.settings.GetShowTransactionNotesUseCase
import com.antcashmanager.domain.usecase.settings.GetThemeUseCase
import com.antcashmanager.domain.usecase.settings.GetThousandsSeparatorUseCase
import com.antcashmanager.domain.usecase.settings.GetTransactionDisplayTypeUseCase
import com.antcashmanager.domain.usecase.settings.ResetAllPreferencesUseCase
import com.antcashmanager.domain.usecase.settings.SetCurrencySymbolUseCase
import com.antcashmanager.domain.usecase.settings.SetDecimalDigitsUseCase
import com.antcashmanager.domain.usecase.settings.SetDecimalSeparatorUseCase
import com.antcashmanager.domain.usecase.settings.SetHighContrastUseCase
import com.antcashmanager.domain.usecase.settings.SetLanguageUseCase
import com.antcashmanager.domain.usecase.settings.SetLargeTextUseCase
import com.antcashmanager.domain.usecase.settings.SetReduceMotionUseCase
import com.antcashmanager.domain.usecase.settings.SetShowChartsUseCase
import com.antcashmanager.domain.usecase.settings.SetThemeUseCase
import com.antcashmanager.domain.usecase.settings.SetThousandsSeparatorUseCase
import com.antcashmanager.domain.usecase.settings.SetTutorialCompletedUseCase
import com.antcashmanager.domain.usecase.transaction.DeleteAllTransactionsUseCase
import com.antcashmanager.domain.usecase.transaction.InsertTransactionUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelMockkTest : BaseUnitTest() {

    private lateinit var getThemeUseCase: GetThemeUseCase
    private lateinit var setThemeUseCase: SetThemeUseCase
    private lateinit var getLanguageUseCase: GetLanguageUseCase
    private lateinit var setLanguageUseCase: SetLanguageUseCase
    private lateinit var getShowChartsUseCase: GetShowChartsUseCase
    private lateinit var setShowChartsUseCase: SetShowChartsUseCase
    private lateinit var getHighContrastUseCase: GetHighContrastUseCase
    private lateinit var setHighContrastUseCase: SetHighContrastUseCase
    private lateinit var getLargeTextUseCase: GetLargeTextUseCase
    private lateinit var setLargeTextUseCase: SetLargeTextUseCase
    private lateinit var getReduceMotionUseCase: GetReduceMotionUseCase
    private lateinit var setReduceMotionUseCase: SetReduceMotionUseCase
    private lateinit var getCurrencySymbolUseCase: GetCurrencySymbolUseCase
    private lateinit var setCurrencySymbolUseCase: SetCurrencySymbolUseCase
    private lateinit var getDecimalDigitsUseCase: GetDecimalDigitsUseCase
    private lateinit var setDecimalDigitsUseCase: SetDecimalDigitsUseCase
    private lateinit var getDecimalSeparatorUseCase: GetDecimalSeparatorUseCase
    private lateinit var setDecimalSeparatorUseCase: SetDecimalSeparatorUseCase
    private lateinit var getThousandsSeparatorUseCase: GetThousandsSeparatorUseCase
    private lateinit var setThousandsSeparatorUseCase: SetThousandsSeparatorUseCase
    private lateinit var getShowTransactionNotesUseCase: GetShowTransactionNotesUseCase
    private lateinit var getTransactionDisplayTypeUseCase: GetTransactionDisplayTypeUseCase
    private lateinit var setTutorialCompletedUseCase: SetTutorialCompletedUseCase
    private lateinit var resetAllPreferencesUseCase: ResetAllPreferencesUseCase
    private lateinit var deleteAllTransactionsUseCase: DeleteAllTransactionsUseCase
    private lateinit var insertTransactionUseCase: InsertTransactionUseCase

    @Before
    fun setup() {
        // Boolean getters
        getShowChartsUseCase = mockk()
        getHighContrastUseCase = mockk()
        getLargeTextUseCase = mockk()
        getReduceMotionUseCase = mockk()
        getShowTransactionNotesUseCase = mockk()

        // Boolean setters
        setShowChartsUseCase = mockk()
        setHighContrastUseCase = mockk()
        setLargeTextUseCase = mockk()
        setReduceMotionUseCase = mockk()

        // String getters
        getCurrencySymbolUseCase = mockk()
        getDecimalSeparatorUseCase = mockk()
        getThousandsSeparatorUseCase = mockk()

        // String setters
        setCurrencySymbolUseCase = mockk()
        setDecimalSeparatorUseCase = mockk()
        setThousandsSeparatorUseCase = mockk()

        // Int getter/setter
        getDecimalDigitsUseCase = mockk()
        setDecimalDigitsUseCase = mockk()

        // Enum getter
        getTransactionDisplayTypeUseCase = mockk()

        // Theme & Language (already tested in previous versions)
        getThemeUseCase = mockk()
        setThemeUseCase = mockk()
        getLanguageUseCase = mockk()
        setLanguageUseCase = mockk()

        // Other setters/actions
        setTutorialCompletedUseCase = mockk()
        resetAllPreferencesUseCase = mockk()
        deleteAllTransactionsUseCase = mockk()
        insertTransactionUseCase = mockk()

        // Default behavior for all getters: return success
        every { getThemeUseCase() } returns flowOf(Result.success(AppTheme.DARK))
        every { getLanguageUseCase() } returns flowOf(Result.success(AppLanguage.ITALIAN))
        every { getShowChartsUseCase() } returns flowOf(Result.success(true))
        every { getHighContrastUseCase() } returns flowOf(Result.success(false))
        every { getLargeTextUseCase() } returns flowOf(Result.success(false))
        every { getReduceMotionUseCase() } returns flowOf(Result.success(false))
        every { getCurrencySymbolUseCase() } returns flowOf(Result.success("€"))
        every { getDecimalDigitsUseCase() } returns flowOf(Result.success(2))
        every { getDecimalSeparatorUseCase() } returns flowOf(Result.success(","))
        every { getThousandsSeparatorUseCase() } returns flowOf(Result.success("."))
        every { getShowTransactionNotesUseCase() } returns flowOf(Result.success(true))
        every { getTransactionDisplayTypeUseCase() } returns flowOf(
            Result.success(
                TransactionDisplayType.TREND
            )
        )

        // Default behavior for all setters/actions: return success
        coEvery { setThemeUseCase(any()) } returns Result.success(Unit)
        coEvery { setLanguageUseCase(any()) } returns Result.success(Unit)
        coEvery { setShowChartsUseCase(any()) } returns Result.success(Unit)
        coEvery { setHighContrastUseCase(any()) } returns Result.success(Unit)
        coEvery { setLargeTextUseCase(any()) } returns Result.success(Unit)
        coEvery { setReduceMotionUseCase(any()) } returns Result.success(Unit)
        coEvery { setCurrencySymbolUseCase(any()) } returns Result.success(Unit)
        coEvery { setDecimalDigitsUseCase(any()) } returns Result.success(Unit)
        coEvery { setDecimalSeparatorUseCase(any()) } returns Result.success(Unit)
        coEvery { setThousandsSeparatorUseCase(any()) } returns Result.success(Unit)
        coEvery { setTutorialCompletedUseCase(any()) } returns Result.success(Unit)
        coEvery { resetAllPreferencesUseCase() } returns Result.success(Unit)
        coEvery { deleteAllTransactionsUseCase() } returns Result.success(Unit)
        coEvery { insertTransactionUseCase(any()) } returns Result.success(0L)
    }

    @After
    fun tearDown() {
        unmockkObject(FeedbackEmailHelper)
    }

    @Test
    fun setTheme_shouldDelegateToUseCase_whenThemeChanges() = runViewModelTest {
        val viewModel = buildViewModel()

        viewModel.setTheme(AppTheme.LIGHT)
        advanceUntilIdle()

        coVerify(exactly = 1) { setThemeUseCase(AppTheme.LIGHT) }
    }

    @Test
    fun setLanguage_shouldDelegateToUseCase_whenLanguageChanges() = runViewModelTest {
        val viewModel = buildViewModel()

        viewModel.setLanguage(AppLanguage.SPANISH)
        advanceUntilIdle()

        coVerify(exactly = 1) { setLanguageUseCase(AppLanguage.SPANISH) }
    }

    @Test
    fun setShowCharts_shouldDelegateToUseCase_whenToggleChanges() = runViewModelTest {
        val viewModel = buildViewModel()

        viewModel.setShowCharts(false)
        advanceUntilIdle()

        coVerify(exactly = 1) { setShowChartsUseCase(false) }
    }

    @Test
    fun resetAllPreferences_shouldDelegateToUseCase_whenRequested() = runViewModelTest {
        val viewModel = buildViewModel()

        viewModel.resetAllPreferences()
        advanceUntilIdle()

        coVerify(exactly = 1) { resetAllPreferencesUseCase() }
    }

    @Test
    fun state_shouldFallbackToDefaults_whenThemeAndLanguageUseCasesFail() = runViewModelTest {
        every { getThemeUseCase() } returns flowOf(Result.failure(IllegalStateException("theme-failed")))
        every { getLanguageUseCase() } returns flowOf(
            Result.failure(IllegalStateException("language-failed")),
        )
        val viewModel = buildViewModel()
        val collectJob = launch { viewModel.state.collect {} }

        advanceUntilIdle()

        assertEquals(AppTheme.SYSTEM, viewModel.state.value.theme)
        assertEquals(AppLanguage.SYSTEM, viewModel.state.value.language)
        collectJob.cancel()
    }

    @Test
    fun state_shouldReflectThemeUseCase_whenFlowEmits() = runViewModelTest {
        every { getThemeUseCase() } returns flowOf(Result.success(AppTheme.LIGHT))
        val viewModel = buildViewModel()
        val collectJob = launch { viewModel.state.collect {} }

        advanceUntilIdle()

        assertEquals(AppTheme.LIGHT, viewModel.state.value.theme)
        collectJob.cancel()
    }

    private fun buildViewModel(): SettingsViewModel = SettingsViewModel(
        getThemeUseCase = getThemeUseCase,
        setThemeUseCase = setThemeUseCase,
        getLanguageUseCase = getLanguageUseCase,
        setLanguageUseCase = setLanguageUseCase,
        getShowChartsUseCase = getShowChartsUseCase,
        setShowChartsUseCase = setShowChartsUseCase,
        getHighContrastUseCase = getHighContrastUseCase,
        setHighContrastUseCase = setHighContrastUseCase,
        getLargeTextUseCase = getLargeTextUseCase,
        setLargeTextUseCase = setLargeTextUseCase,
        getReduceMotionUseCase = getReduceMotionUseCase,
        setReduceMotionUseCase = setReduceMotionUseCase,
        getCurrencySymbolUseCase = getCurrencySymbolUseCase,
        setCurrencySymbolUseCase = setCurrencySymbolUseCase,
        getDecimalDigitsUseCase = getDecimalDigitsUseCase,
        setDecimalDigitsUseCase = setDecimalDigitsUseCase,
        getDecimalSeparatorUseCase = getDecimalSeparatorUseCase,
        setDecimalSeparatorUseCase = setDecimalSeparatorUseCase,
        getThousandsSeparatorUseCase = getThousandsSeparatorUseCase,
        setThousandsSeparatorUseCase = setThousandsSeparatorUseCase,
        getShowTransactionNotesUseCase = getShowTransactionNotesUseCase,
        getTransactionDisplayTypeUseCase = getTransactionDisplayTypeUseCase,
        setTutorialCompletedUseCase = setTutorialCompletedUseCase,
        resetAllPreferencesUseCase = resetAllPreferencesUseCase,
        deleteAllTransactionsUseCase = deleteAllTransactionsUseCase,
        insertTransactionUseCase = insertTransactionUseCase,
    )
}
