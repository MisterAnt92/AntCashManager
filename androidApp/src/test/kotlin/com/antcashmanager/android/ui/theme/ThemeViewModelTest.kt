package com.antcashmanager.android.ui.theme

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.testutil.FakeSettingsRepository
import com.antcashmanager.domain.model.AppTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest : BaseUnitTest() {
    private lateinit var fakeRepo: FakeSettingsRepository
    private lateinit var viewModel: ThemeViewModel

    @Before
    fun setup() {
        fakeRepo = FakeSettingsRepository()
        viewModel = ThemeViewModel(fakeRepo)
    }

    @Test
    fun appTheme_shouldReturnSystemDefault_whenRepositoryHasNoValue() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    viewModel.appTheme.collect {}
                }
            advanceUntilIdle()

            assertEquals(AppTheme.SYSTEM, viewModel.appTheme.value)
            collectJob.cancel()
        }

    @Test
    fun onSetThemeEvent_shouldUpdateRepository_whenThemeEventReceived() =
        runViewModelTest {
            viewModel.onEvent(ThemeEvent.SetThemeEvent(AppTheme.DARK))
            advanceUntilIdle()

            assertEquals(AppTheme.DARK, fakeRepo.theme.value)
        }

    @Test
    fun onSetThemeEvent_shouldUpdateExposedAppTheme_whenThemeChanges() =
        runViewModelTest {
            val collectJob =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    viewModel.appTheme.collect {}
                }
            advanceUntilIdle()

            viewModel.onEvent(ThemeEvent.SetThemeEvent(AppTheme.LIGHT))
            advanceUntilIdle()

            assertEquals(AppTheme.LIGHT, viewModel.appTheme.value)
            collectJob.cancel()
        }
}
