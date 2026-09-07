package com.antcashmanager.android.ui.settings

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.data.feedback.FeedbackEmailHelper
import com.antcashmanager.android.ui.screen.settings.SettingEvent
import com.antcashmanager.android.ui.screen.settings.SettingsViewModel
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.service.WidgetUpdateNotifier
import com.antcashmanager.domain.usecase.settings.GetSettingUseCase
import com.antcashmanager.domain.usecase.settings.ResetAllPreferencesUseCase
import com.antcashmanager.domain.usecase.settings.SetSettingUseCase
import com.antcashmanager.domain.usecase.settings.SettingsUseCasesProvider
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
    private lateinit var getThemeUseCase: GetSettingUseCase<String>
    private lateinit var setThemeUseCase: SetSettingUseCase<String>
    private lateinit var getLanguageUseCase: GetSettingUseCase<String>
    private lateinit var setLanguageUseCase: SetSettingUseCase<String>
    private lateinit var getShowChartsUseCase: GetSettingUseCase<Boolean>
    private lateinit var setShowChartsUseCase: SetSettingUseCase<Boolean>
    private lateinit var getHighContrastUseCase: GetSettingUseCase<Boolean>
    private lateinit var setHighContrastUseCase: SetSettingUseCase<Boolean>
    private lateinit var getLargeTextUseCase: GetSettingUseCase<Boolean>
    private lateinit var setLargeTextUseCase: SetSettingUseCase<Boolean>
    private lateinit var getReduceMotionUseCase: GetSettingUseCase<Boolean>
    private lateinit var setReduceMotionUseCase: SetSettingUseCase<Boolean>
    private lateinit var getCurrencySymbolUseCase: GetSettingUseCase<String>
    private lateinit var setCurrencySymbolUseCase: SetSettingUseCase<String>
    private lateinit var getDecimalDigitsUseCase: GetSettingUseCase<Int>
    private lateinit var setDecimalDigitsUseCase: SetSettingUseCase<Int>
    private lateinit var getDecimalSeparatorUseCase: GetSettingUseCase<String>
    private lateinit var setDecimalSeparatorUseCase: SetSettingUseCase<String>
    private lateinit var getThousandsSeparatorUseCase: GetSettingUseCase<String>
    private lateinit var setThousandsSeparatorUseCase: SetSettingUseCase<String>
    private lateinit var getShowTransactionNotesUseCase: GetSettingUseCase<Boolean>
    private lateinit var getTransactionDisplayTypeUseCase: GetSettingUseCase<String>
    private lateinit var setTutorialCompletedUseCase: SetSettingUseCase<Boolean>
    private lateinit var setTransactionDisplayTypeUseCase: SetSettingUseCase<String>
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

        // Enum getter/setter
        getTransactionDisplayTypeUseCase = mockk()
        setTransactionDisplayTypeUseCase = mockk()

        // Theme & Language
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
        every { getThemeUseCase() } returns flowOf(Result.success("dark"))
        every { getLanguageUseCase() } returns flowOf(Result.success("it"))
        every { getShowChartsUseCase() } returns flowOf(Result.success(true))
        every { getHighContrastUseCase() } returns flowOf(Result.success(false))
        every { getLargeTextUseCase() } returns flowOf(Result.success(false))
        every { getReduceMotionUseCase() } returns flowOf(Result.success(false))
        every { getCurrencySymbolUseCase() } returns flowOf(Result.success("€"))
        every { getDecimalDigitsUseCase() } returns flowOf(Result.success(2))
        every { getDecimalSeparatorUseCase() } returns flowOf(Result.success(","))
        every { getThousandsSeparatorUseCase() } returns flowOf(Result.success("."))
        every { getShowTransactionNotesUseCase() } returns flowOf(Result.success(true))
        every { getTransactionDisplayTypeUseCase() } returns flowOf(Result.success("trend"))

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
        coEvery { setTransactionDisplayTypeUseCase(any()) } returns Result.success(Unit)
        coEvery { resetAllPreferencesUseCase() } returns Result.success(Unit)
        coEvery { deleteAllTransactionsUseCase() } returns Result.success(Unit)
        coEvery { insertTransactionUseCase(any()) } returns Result.success(0L)
    }

    @After
    fun tearDown() {
        unmockkObject(FeedbackEmailHelper)
    }

    @Test
    fun setTheme_shouldDelegateToUseCase_whenThemeChanges() =
        runViewModelTest {
            val viewModel = buildViewModel()

            viewModel.onEvent(SettingEvent.SetTheme(AppTheme.LIGHT))
            advanceUntilIdle()

            coVerify(exactly = 1) { setThemeUseCase(any()) }
        }

    @Test
    fun setLanguage_shouldDelegateToUseCase_whenLanguageChanges() =
        runViewModelTest {
            val viewModel = buildViewModel()

            viewModel.onEvent(SettingEvent.SetLanguage(AppLanguage.SPANISH))
            advanceUntilIdle()

            coVerify(exactly = 1) { setLanguageUseCase(any()) }
        }

    @Test
    fun setShowCharts_shouldDelegateToUseCase_whenToggleChanges() =
        runViewModelTest {
            val viewModel = buildViewModel()

            viewModel.onEvent(SettingEvent.SetShowCharts(false))
            advanceUntilIdle()

            coVerify(exactly = 1) { setShowChartsUseCase(any()) }
        }

    @Test
    fun resetAllPreferences_shouldDelegateToUseCase_whenRequested() =
        runViewModelTest {
            val viewModel = buildViewModel()

            viewModel.onEvent(SettingEvent.ResetAllPreferences)
            advanceUntilIdle()

            coVerify(exactly = 1) { resetAllPreferencesUseCase() }
        }

    @Test
    fun state_shouldFallbackToDefaults_whenThemeAndLanguageUseCasesFail() =
        runViewModelTest {
            every { getThemeUseCase() } returns flowOf(Result.failure(IllegalStateException("theme-failed")))
            every { getLanguageUseCase() } returns
                flowOf(
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
    fun state_shouldReflectThemeUseCase_whenFlowEmits() =
        runViewModelTest {
            every { getThemeUseCase() } returns flowOf(Result.success("light"))
            val viewModel = buildViewModel()
            val collectJob = launch { viewModel.state.collect {} }

            advanceUntilIdle()

            assertEquals(AppTheme.LIGHT, viewModel.state.value.theme)
            collectJob.cancel()
        }

    private fun buildViewModel(): SettingsViewModel {
        val settingsUseCasesProvider: SettingsUseCasesProvider = mockk()
        every { settingsUseCasesProvider.getTheme } returns getThemeUseCase
        every { settingsUseCasesProvider.setTheme } returns setThemeUseCase
        every { settingsUseCasesProvider.getLanguage } returns getLanguageUseCase
        every { settingsUseCasesProvider.setLanguage } returns setLanguageUseCase
        every { settingsUseCasesProvider.getShowCharts } returns getShowChartsUseCase
        every { settingsUseCasesProvider.setShowCharts } returns setShowChartsUseCase
        every { settingsUseCasesProvider.getHighContrast } returns getHighContrastUseCase
        every { settingsUseCasesProvider.setHighContrast } returns setHighContrastUseCase
        every { settingsUseCasesProvider.getLargeText } returns getLargeTextUseCase
        every { settingsUseCasesProvider.setLargeText } returns setLargeTextUseCase
        every { settingsUseCasesProvider.getReduceMotion } returns getReduceMotionUseCase
        every { settingsUseCasesProvider.setReduceMotion } returns setReduceMotionUseCase
        every { settingsUseCasesProvider.getCurrencySymbol } returns getCurrencySymbolUseCase
        every { settingsUseCasesProvider.setCurrencySymbol } returns setCurrencySymbolUseCase
        every { settingsUseCasesProvider.getDecimalDigits } returns getDecimalDigitsUseCase
        every { settingsUseCasesProvider.setDecimalDigits } returns setDecimalDigitsUseCase
        every { settingsUseCasesProvider.getDecimalSeparator } returns getDecimalSeparatorUseCase
        every { settingsUseCasesProvider.setDecimalSeparator } returns setDecimalSeparatorUseCase
        every { settingsUseCasesProvider.getThousandsSeparator } returns getThousandsSeparatorUseCase
        every { settingsUseCasesProvider.setThousandsSeparator } returns setThousandsSeparatorUseCase
        every { settingsUseCasesProvider.getShowTransactionNotes } returns getShowTransactionNotesUseCase
        every { settingsUseCasesProvider.getTransactionDisplayType } returns getTransactionDisplayTypeUseCase
        every { settingsUseCasesProvider.setTransactionDisplayType } returns setTransactionDisplayTypeUseCase
        every { settingsUseCasesProvider.setTutorialCompleted } returns setTutorialCompletedUseCase
        every { settingsUseCasesProvider.resetAllPreferences } returns resetAllPreferencesUseCase

        val widgetUpdateNotifier: WidgetUpdateNotifier = mockk(relaxed = true)

        return SettingsViewModel(
            settingsUseCases = settingsUseCasesProvider,
            deleteAllTransactionsUseCase = deleteAllTransactionsUseCase,
            insertTransactionUseCase = insertTransactionUseCase,
            widgetUpdateNotifier = widgetUpdateNotifier,
        )
    }
}
