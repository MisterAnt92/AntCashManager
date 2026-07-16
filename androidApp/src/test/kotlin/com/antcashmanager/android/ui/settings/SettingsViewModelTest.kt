package com.antcashmanager.android.ui.settings

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.testutil.FakeSettingsRepository
import com.antcashmanager.android.testutil.FakeTransactionRepository
import com.antcashmanager.android.ui.screen.settings.SettingsViewModel
import com.antcashmanager.domain.model.AppTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest : BaseUnitTest() {
    private lateinit var fakeSettingsRepo: FakeSettingsRepository
    private lateinit var fakeTransactionRepo: FakeTransactionRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        fakeSettingsRepo = FakeSettingsRepository()
        fakeTransactionRepo = FakeTransactionRepository()
        viewModel = SettingsViewModel(
            settingsRepository = fakeSettingsRepo,
            transactionRepository = fakeTransactionRepo,
            useCaseDispatcher = testDispatcher,
        )
    }

    @Test
    fun initialThemeIsSYSTEM() = runViewModelTest {
        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()
        assertEquals(AppTheme.SYSTEM, viewModel.state.value.theme)
        collectJob.cancel()
    }

    @Test
    fun setThemeUpdatesThemeToDARK() = runViewModelTest {
        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.setTheme(AppTheme.DARK)
        advanceUntilIdle()

        assertEquals(AppTheme.DARK, viewModel.state.value.theme)
        collectJob.cancel()
    }

    @Test
    fun setThemeUpdatesThemeToLIGHT() = runViewModelTest {
        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.setTheme(AppTheme.LIGHT)
        advanceUntilIdle()

        assertEquals(AppTheme.LIGHT, viewModel.state.value.theme)
        collectJob.cancel()
    }

    @Test
    fun setThemeCanSwitchBetweenThemes() = runViewModelTest {
        fun awaitTheme(expected: AppTheme) {
            repeat(5) {
                advanceUntilIdle()
                if (viewModel.state.value.theme == expected) {
                    return
                }
            }
            fail("Expected theme=$expected but was ${viewModel.state.value.theme}")
        }

        val collectJob = launch {
            viewModel.state.collect {}
        }
        advanceUntilIdle()

        viewModel.setTheme(AppTheme.DARK)
        awaitTheme(AppTheme.DARK)

        viewModel.setTheme(AppTheme.LIGHT)
        awaitTheme(AppTheme.LIGHT)

        viewModel.setTheme(AppTheme.SYSTEM)
        awaitTheme(AppTheme.SYSTEM)

        collectJob.cancel()
    }
}
