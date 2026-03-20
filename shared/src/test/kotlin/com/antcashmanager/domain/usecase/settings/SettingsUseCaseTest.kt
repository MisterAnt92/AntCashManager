package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
    fun `invoke returns SYSTEM theme by default`() = runTest {
        val theme = useCase().first()
        assertEquals(AppTheme.SYSTEM, theme)
    }

    @Test
    fun `invoke returns DARK after setting dark theme`() = runTest {
        fakeRepo.setTheme(AppTheme.DARK)

        val theme = useCase().first()
        assertEquals(AppTheme.DARK, theme)
    }

    @Test
    fun `invoke returns LIGHT after setting light theme`() = runTest {
        fakeRepo.setTheme(AppTheme.LIGHT)

        val theme = useCase().first()
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
    fun `invoke sets theme to DARK`() = runTest {
        setThemeUseCase(AppTheme.DARK)

        val currentTheme = getThemeUseCase().first()
        assertEquals(AppTheme.DARK, currentTheme)
    }

    @Test
    fun `invoke sets theme to LIGHT`() = runTest {
        setThemeUseCase(AppTheme.LIGHT)

        val currentTheme = getThemeUseCase().first()
        assertEquals(AppTheme.LIGHT, currentTheme)
    }

    @Test
    fun `invoke changes theme from DARK to SYSTEM`() = runTest {
        setThemeUseCase(AppTheme.DARK)
        assertEquals(AppTheme.DARK, getThemeUseCase().first())

        setThemeUseCase(AppTheme.SYSTEM)
        assertEquals(AppTheme.SYSTEM, getThemeUseCase().first())
    }
}

/**
 * Fake repository per test dei settings.
 */
internal class FakeSettingsRepository : SettingsRepository {
    private val themeFlow = MutableStateFlow(AppTheme.SYSTEM)
    private val languageFlow = MutableStateFlow(AppLanguage.SYSTEM)

    override fun getTheme(): Flow<AppTheme> = themeFlow

    override suspend fun setTheme(theme: AppTheme) {
        themeFlow.value = theme
    }

    override fun getLanguage(): Flow<AppLanguage> = languageFlow

    override suspend fun setLanguage(language: AppLanguage) {
        languageFlow.value = language
    }
}

