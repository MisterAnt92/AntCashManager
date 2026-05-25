package com.antcashmanager.android.ui.settings

import com.antcashmanager.android.domain.usecase.feedback.SendFeedbackEmailUseCase
import com.antcashmanager.android.ui.screen.settings.SettingsViewModel
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.settings.GetLanguageUseCase
import com.antcashmanager.domain.usecase.settings.GetThemeUseCase
import com.antcashmanager.domain.usecase.settings.SetLanguageUseCase
import com.antcashmanager.domain.usecase.settings.SetThemeUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelMockkTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var getThemeUseCase: GetThemeUseCase
    private lateinit var setThemeUseCase: SetThemeUseCase
    private lateinit var getLanguageUseCase: GetLanguageUseCase
    private lateinit var setLanguageUseCase: SetLanguageUseCase
    private lateinit var sendFeedbackEmailUseCase: SendFeedbackEmailUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        settingsRepository = mockk(relaxed = true)
        transactionRepository = mockk(relaxed = true)
        getThemeUseCase = mockk()
        setThemeUseCase = mockk()
        getLanguageUseCase = mockk()
        setLanguageUseCase = mockk()
        sendFeedbackEmailUseCase = mockk(relaxed = true)

        every { getThemeUseCase() } returns flowOf(Result.success(AppTheme.DARK))
        every { getLanguageUseCase() } returns flowOf(Result.success(AppLanguage.ITALIAN))
        every { settingsRepository.getShowCharts() } returns flowOf(true)
        every { settingsRepository.getHighContrast() } returns flowOf(false)
        every { settingsRepository.getLargeText() } returns flowOf(false)
        every { settingsRepository.getReduceMotion() } returns flowOf(false)
        every { settingsRepository.getCurrencySymbol() } returns flowOf("€")
        every { settingsRepository.getDecimalDigits() } returns flowOf(2)
        every { settingsRepository.getDecimalSeparator() } returns flowOf(",")
        every { settingsRepository.getThousandsSeparator() } returns flowOf(".")
        every { settingsRepository.getShowTransactionNotes() } returns flowOf(true)
        every { settingsRepository.getTransactionDisplayType() } returns flowOf(
            TransactionDisplayType.TREND,
        )

        coEvery { setThemeUseCase(any()) } returns Result.success(Unit)
        coEvery { setLanguageUseCase(any()) } returns Result.success(Unit)
        coEvery { settingsRepository.setShowCharts(any()) } returns Unit
        coEvery { settingsRepository.resetAllPreferences() } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun setTheme_shouldDelegateToUseCase_whenThemeChanges() = runTest(testDispatcher) {
        val viewModel = buildViewModel()

        viewModel.setTheme(AppTheme.LIGHT)
        advanceUntilIdle()

        coVerify(exactly = 1) { setThemeUseCase(AppTheme.LIGHT) }
    }

    @Test
    fun setLanguage_shouldDelegateToUseCase_whenLanguageChanges() = runTest(testDispatcher) {
        val viewModel = buildViewModel()

        viewModel.setLanguage(AppLanguage.SPANISH)
        advanceUntilIdle()

        coVerify(exactly = 1) { setLanguageUseCase(AppLanguage.SPANISH) }
    }

    @Test
    fun setShowCharts_shouldDelegateToRepository_whenToggleChanges() = runTest(testDispatcher) {
        val viewModel = buildViewModel()

        viewModel.setShowCharts(false)
        advanceUntilIdle()

        coVerify(exactly = 1) { settingsRepository.setShowCharts(false) }
    }

    @Test
    fun resetAllPreferences_shouldDelegateToRepository_whenRequested() = runTest(testDispatcher) {
        val viewModel = buildViewModel()

        viewModel.resetAllPreferences()
        advanceUntilIdle()

        coVerify(exactly = 1) { settingsRepository.resetAllPreferences() }
    }

    @Test
    fun state_shouldFallbackToDefaults_whenThemeAndLanguageUseCasesFail() = runTest(testDispatcher) {
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

    private fun buildViewModel(): SettingsViewModel = SettingsViewModel(
        settingsRepository = settingsRepository,
        transactionRepository = transactionRepository,
        getThemeUseCase = getThemeUseCase,
        setThemeUseCase = setThemeUseCase,
        getLanguageUseCase = getLanguageUseCase,
        setLanguageUseCase = setLanguageUseCase,
        sendFeedbackEmailUseCase = sendFeedbackEmailUseCase,
    )
}

