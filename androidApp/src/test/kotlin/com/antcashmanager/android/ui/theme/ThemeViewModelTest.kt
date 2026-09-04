package com.antcashmanager.android.ui.theme

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.testutil.FakeSettingsRepository
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.android.ui.theme.ThemeEvent
import com.antcashmanager.android.ui.theme.ThemeViewModel
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
    fun defaultThemeExposed() = runViewModelTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.appTheme.collect {}
        }
        advanceUntilIdle()

        assertEquals(AppTheme.SYSTEM, viewModel.appTheme.value)

        collectJob.cancel()
    }

    @Test
    fun setThemeUpdatesRepository() = runViewModelTest {
        viewModel.onEvent(ThemeEvent.SetTheme(AppTheme.DARK))
        advanceUntilIdle()

        // Fake repo mirrors setTheme into its flow
        assertEquals(AppTheme.DARK, fakeRepo.theme.value)
    }
}
