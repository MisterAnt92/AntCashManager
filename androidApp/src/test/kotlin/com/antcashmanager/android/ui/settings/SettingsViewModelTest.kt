package com.antcashmanager.android.ui.settings

import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeSettingsRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeSettingsRepository()
        viewModel = SettingsViewModel(fakeRepo)
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
}

private class FakeSettingsRepository : SettingsRepository {
    private val themeFlow = MutableStateFlow(AppTheme.SYSTEM)

    override fun getTheme(): Flow<AppTheme> = themeFlow

    override suspend fun setTheme(theme: AppTheme) {
        themeFlow.value = theme
    }
}
