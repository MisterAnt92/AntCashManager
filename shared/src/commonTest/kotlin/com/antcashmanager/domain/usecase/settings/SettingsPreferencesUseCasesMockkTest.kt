package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Test sui 22 nuovi use case granulari per le preferenze settings.
 * Verifica che ciascun getter/setter deleghi correttamente al repository sottostante.
 */
class SettingsPreferencesUseCasesMockkTest {
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setup() {
        settingsRepository = mockk()
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Boolean Getters / Setters
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun getShowChartsUseCase_shouldDelegateToRepository() =
        runTest {
            every { settingsRepository.getShowCharts() } returns flowOf(true)
            val useCase = GetShowChartsUseCase(settingsRepository)

            val result = useCase().collect { /* consume flow */ }

            coVerify { settingsRepository.getShowCharts() }
        }

    @Test
    fun setShowChartsUseCase_shouldPassBooleanToRepository() =
        runTest {
            coEvery { settingsRepository.setShowCharts(any()) } returns Unit
            val useCase = SetShowChartsUseCase(settingsRepository)

            useCase(false)

            coVerify { settingsRepository.setShowCharts(false) }
        }

    @Test
    fun getHighContrastUseCase_shouldDelegateToRepository() =
        runTest {
            every { settingsRepository.getHighContrast() } returns flowOf(false)
            val useCase = GetHighContrastUseCase(settingsRepository)

            useCase().collect { }

            coVerify { settingsRepository.getHighContrast() }
        }

    @Test
    fun setHighContrastUseCase_shouldPassBooleanToRepository() =
        runTest {
            coEvery { settingsRepository.setHighContrast(any()) } returns Unit
            val useCase = SetHighContrastUseCase(settingsRepository)

            useCase(true)

            coVerify { settingsRepository.setHighContrast(true) }
        }

    @Test
    fun getLargeTextUseCase_shouldDelegateToRepository() =
        runTest {
            every { settingsRepository.getLargeText() } returns flowOf(true)
            val useCase = GetLargeTextUseCase(settingsRepository)

            useCase().collect { }

            coVerify { settingsRepository.getLargeText() }
        }

    @Test
    fun setLargeTextUseCase_shouldPassBooleanToRepository() =
        runTest {
            coEvery { settingsRepository.setLargeText(any()) } returns Unit
            val useCase = SetLargeTextUseCase(settingsRepository)

            useCase(false)

            coVerify { settingsRepository.setLargeText(false) }
        }

    @Test
    fun getReduceMotionUseCase_shouldDelegateToRepository() =
        runTest {
            every { settingsRepository.getReduceMotion() } returns flowOf(false)
            val useCase = GetReduceMotionUseCase(settingsRepository)

            useCase().collect { }

            coVerify { settingsRepository.getReduceMotion() }
        }

    @Test
    fun setReduceMotionUseCase_shouldPassBooleanToRepository() =
        runTest {
            coEvery { settingsRepository.setReduceMotion(any()) } returns Unit
            val useCase = SetReduceMotionUseCase(settingsRepository)

            useCase(true)

            coVerify { settingsRepository.setReduceMotion(true) }
        }

    @Test
    fun getShowTransactionNotesUseCase_shouldDelegateToRepository() =
        runTest {
            every { settingsRepository.getShowTransactionNotes() } returns flowOf(true)
            val useCase = GetShowTransactionNotesUseCase(settingsRepository)

            useCase().collect { }

            coVerify { settingsRepository.getShowTransactionNotes() }
        }

    @Test
    fun setShowTransactionNotesUseCase_shouldPassBooleanToRepository() =
        runTest {
            coEvery { settingsRepository.setShowTransactionNotes(any()) } returns Unit
            val useCase = SetShowTransactionNotesUseCase(settingsRepository)

            useCase(false)

            coVerify { settingsRepository.setShowTransactionNotes(false) }
        }

    // ──────────────────────────────────────────────────────────────────────────
    // String Getters / Setters
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun getCurrencySymbolUseCase_shouldDelegateToRepository() =
        runTest {
            every { settingsRepository.getCurrencySymbol() } returns flowOf("€")
            val useCase = GetCurrencySymbolUseCase(settingsRepository)

            useCase().collect { }

            coVerify { settingsRepository.getCurrencySymbol() }
        }

    @Test
    fun setCurrencySymbolUseCase_shouldPassStringToRepository() =
        runTest {
            coEvery { settingsRepository.setCurrencySymbol(any()) } returns Unit
            val useCase = SetCurrencySymbolUseCase(settingsRepository)

            useCase("$")

            coVerify { settingsRepository.setCurrencySymbol("$") }
        }

    @Test
    fun getDecimalSeparatorUseCase_shouldDelegateToRepository() =
        runTest {
            every { settingsRepository.getDecimalSeparator() } returns flowOf(",")
            val useCase = GetDecimalSeparatorUseCase(settingsRepository)

            useCase().collect { }

            coVerify { settingsRepository.getDecimalSeparator() }
        }

    @Test
    fun setDecimalSeparatorUseCase_shouldPassStringToRepository() =
        runTest {
            coEvery { settingsRepository.setDecimalSeparator(any()) } returns Unit
            val useCase = SetDecimalSeparatorUseCase(settingsRepository)

            useCase(".")

            coVerify { settingsRepository.setDecimalSeparator(".") }
        }

    @Test
    fun getThousandsSeparatorUseCase_shouldDelegateToRepository() =
        runTest {
            every { settingsRepository.getThousandsSeparator() } returns flowOf(".")
            val useCase = GetThousandsSeparatorUseCase(settingsRepository)

            useCase().collect { }

            coVerify { settingsRepository.getThousandsSeparator() }
        }

    @Test
    fun setThousandsSeparatorUseCase_shouldPassStringToRepository() =
        runTest {
            coEvery { settingsRepository.setThousandsSeparator(any()) } returns Unit
            val useCase = SetThousandsSeparatorUseCase(settingsRepository)

            useCase(",")

            coVerify { settingsRepository.setThousandsSeparator(",") }
        }

    // ──────────────────────────────────────────────────────────────────────────
    // Int Getter / Setter
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun getDecimalDigitsUseCase_shouldDelegateToRepository() =
        runTest {
            every { settingsRepository.getDecimalDigits() } returns flowOf(2)
            val useCase = GetDecimalDigitsUseCase(settingsRepository)

            useCase().collect { }

            coVerify { settingsRepository.getDecimalDigits() }
        }

    @Test
    fun setDecimalDigitsUseCase_shouldPassIntToRepository() =
        runTest {
            coEvery { settingsRepository.setDecimalDigits(any()) } returns Unit
            val useCase = SetDecimalDigitsUseCase(settingsRepository)

            useCase(3)

            coVerify { settingsRepository.setDecimalDigits(3) }
        }

    // ──────────────────────────────────────────────────────────────────────────
    // Enum Getter / Setter
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun getTransactionDisplayTypeUseCase_shouldDelegateToRepository() =
        runTest {
            every { settingsRepository.getTransactionDisplayType() } returns
                flowOf(
                    TransactionDisplayType.TREND,
                )
            val useCase = GetTransactionDisplayTypeUseCase(settingsRepository)

            useCase().collect { }

            coVerify { settingsRepository.getTransactionDisplayType() }
        }

    @Test
    fun setTransactionDisplayTypeUseCase_shouldPassEnumToRepository() =
        runTest {
            coEvery { settingsRepository.setTransactionDisplayType(any()) } returns Unit
            val useCase = SetTransactionDisplayTypeUseCase(settingsRepository)

            useCase(TransactionDisplayType.CATEGORY)

            coVerify { settingsRepository.setTransactionDisplayType(TransactionDisplayType.CATEGORY) }
        }

    // ──────────────────────────────────────────────────────────────────────────
    // Action UseCases (No Params)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun setTutorialCompletedUseCase_shouldPassBooleanToRepository() =
        runTest {
            coEvery { settingsRepository.setIsTutorialCompleted(any()) } returns Unit
            val useCase = SetTutorialCompletedUseCase(settingsRepository)

            useCase(true)

            coVerify { settingsRepository.setIsTutorialCompleted(true) }
        }

    @Test
    fun resetAllPreferencesUseCase_shouldDelegateToRepository() =
        runTest {
            coEvery { settingsRepository.resetAllPreferences() } returns Unit
            val useCase = ResetAllPreferencesUseCase(settingsRepository)

            useCase()

            coVerify { settingsRepository.resetAllPreferences() }
        }
}
