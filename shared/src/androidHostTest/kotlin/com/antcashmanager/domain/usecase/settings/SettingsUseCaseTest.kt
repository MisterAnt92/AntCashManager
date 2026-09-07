package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.testutil.FakeSettingsRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetThemeUseCaseTest {
    private lateinit var fakeRepo: FakeSettingsRepository
    private lateinit var useCase: GetThemeUseCase

    @Before
    fun setup() {
        fakeRepo = FakeSettingsRepository()
        useCase = GetThemeUseCase(fakeRepo)
    }

    @Test
    fun invokeReturnsSYSTEMThemeByDefault() =
        runTest {
            val theme = useCase().first().getOrThrow()
            assertEquals(AppTheme.SYSTEM, theme)
        }

    @Test
    fun invokeReturnsDARKAfterSettingDarkTheme() =
        runTest {
            fakeRepo.setTheme(AppTheme.DARK)

            val theme = useCase().first().getOrThrow()
            assertEquals(AppTheme.DARK, theme)
        }

    @Test
    fun invokeReturnsLIGHTAfterSettingLightTheme() =
        runTest {
            fakeRepo.setTheme(AppTheme.LIGHT)

            val theme = useCase().first().getOrThrow()
            assertEquals(AppTheme.LIGHT, theme)
        }
}

class SetThemeUseCaseTest {
    private lateinit var fakeRepo: FakeSettingsRepository
    private lateinit var setThemeUseCase: SetThemeUseCase
    private lateinit var getThemeUseCase: GetThemeUseCase

    @Before
    fun setup() {
        fakeRepo = FakeSettingsRepository()
        setThemeUseCase = SetThemeUseCase(fakeRepo)
        getThemeUseCase = GetThemeUseCase(fakeRepo)
    }

    @Test
    fun invokeSetsThemeToDARK() =
        runTest {
            setThemeUseCase(AppTheme.DARK)

            val currentTheme = getThemeUseCase().first().getOrThrow()
            assertEquals(AppTheme.DARK, currentTheme)
        }

    @Test
    fun invokeSetsThemeToLIGHT() =
        runTest {
            setThemeUseCase(AppTheme.LIGHT)

            val currentTheme = getThemeUseCase().first().getOrThrow()
            assertEquals(AppTheme.LIGHT, currentTheme)
        }

    @Test
    fun invokeChangesThemeFromDARKToSYSTEM() =
        runTest {
            setThemeUseCase(AppTheme.DARK)
            assertEquals(AppTheme.DARK, getThemeUseCase().first().getOrThrow())

            setThemeUseCase(AppTheme.SYSTEM)
            assertEquals(AppTheme.SYSTEM, getThemeUseCase().first().getOrThrow())
        }
}

class GetLanguageUseCaseTest {
    private lateinit var fakeRepo: FakeSettingsRepository
    private lateinit var useCase: GetLanguageUseCase

    @Before
    fun setup() {
        fakeRepo = FakeSettingsRepository()
        useCase = GetLanguageUseCase(fakeRepo)
    }

    @Test
    fun invoke_shouldReturnSystemLanguage_whenNoLanguageIsSet() =
        runTest {
            val language = useCase().first().getOrThrow()
            assertEquals(AppLanguage.SYSTEM, language)
        }

    @Test
    fun invoke_shouldReturnItalian_whenLanguageIsSetToItalian() =
        runTest {
            fakeRepo.setLanguage(AppLanguage.ITALIAN)

            val language = useCase().first().getOrThrow()
            assertEquals(AppLanguage.ITALIAN, language)
        }

    @Test
    fun invoke_shouldReturnFailure_whenRepositoryThrows() =
        runTest {
            val throwingRepo = mockk<SettingsRepository>()
            every { throwingRepo.getLanguage() } returns flow { throw IllegalStateException("read failure") }

            val result = GetLanguageUseCase(throwingRepo)().first()

            assertTrue(result.isFailure)
        }
}

class SetLanguageUseCaseTest {
    private lateinit var fakeRepo: FakeSettingsRepository
    private lateinit var setLanguageUseCase: SetLanguageUseCase
    private lateinit var getLanguageUseCase: GetLanguageUseCase

    @Before
    fun setup() {
        fakeRepo = FakeSettingsRepository()
        setLanguageUseCase = SetLanguageUseCase(fakeRepo)
        getLanguageUseCase = GetLanguageUseCase(fakeRepo)
    }

    @Test
    fun invoke_shouldPersistFrenchLanguage_whenCalledWithFrench() =
        runTest {
            setLanguageUseCase(AppLanguage.FRENCH)

            val currentLanguage = getLanguageUseCase().first().getOrThrow()
            assertEquals(AppLanguage.FRENCH, currentLanguage)
        }

    @Test
    fun invoke_shouldChangeLanguageFromFrenchToSystem_whenCalledAgain() =
        runTest {
            setLanguageUseCase(AppLanguage.FRENCH)
            assertEquals(AppLanguage.FRENCH, getLanguageUseCase().first().getOrThrow())

            setLanguageUseCase(AppLanguage.SYSTEM)
            assertEquals(AppLanguage.SYSTEM, getLanguageUseCase().first().getOrThrow())
        }

    @Test
    fun invoke_shouldReturnFailure_whenRepositorySetLanguageThrows() =
        runTest {
            val throwingRepo = mockk<SettingsRepository>()
            coEvery { throwingRepo.setLanguage(any()) } throws IllegalStateException("write failure")

            val result = SetLanguageUseCase(throwingRepo)(AppLanguage.GERMAN)

            assertTrue(result.isFailure)
        }
}
