package com.antcashmanager.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.antcashmanager.data.local.DatabaseEncryptionManager
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.TransactionDisplayType
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryImplTest {

    private lateinit var tempDir: File
    private lateinit var testScope: CoroutineScope
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var context: Context
    private lateinit var repository: SettingsRepositoryImpl

    @Before
    fun setup() {
        tempDir = Files.createTempDirectory("settings_repo_test").toFile()
        testScope = CoroutineScope(UnconfinedTestDispatcher() + Job())
        dataStore = PreferenceDataStoreFactory.create(scope = testScope) {
            File(tempDir, "test.preferences_pb")
        }
        context = mockk()
        mockkObject(DatabaseEncryptionManager)
        every { DatabaseEncryptionManager.setEncryptionDesired(any(), any()) } just Runs
        repository = SettingsRepositoryImpl(context = context, dataStore = dataStore)
    }

    @After
    fun tearDown() {
        unmockkObject(DatabaseEncryptionManager)
        testScope.cancel()
        tempDir.deleteRecursively()
    }

    @Test
    fun getTheme_shouldReturnSystem_whenNoThemeIsStored() = runTest {
        assertEquals(AppTheme.SYSTEM, repository.getTheme().first())
    }

    @Test
    fun getTheme_shouldFallbackToSystem_whenStoredThemeIsInvalid() = runTest {
        dataStore.edit { it[stringPreferencesKey("theme")] = "BOGUS" }

        assertEquals(AppTheme.SYSTEM, repository.getTheme().first())
    }

    @Test
    fun getLanguage_shouldFallbackToSystem_whenStoredLanguageIsInvalid() = runTest {
        dataStore.edit { it[stringPreferencesKey("language")] = "BOGUS" }

        assertEquals(AppLanguage.SYSTEM, repository.getLanguage().first())
    }

    @Test
    fun getTransactionDisplayType_shouldFallbackToTrend_whenStoredValueIsInvalid() = runTest {
        dataStore.edit { it[stringPreferencesKey("transaction_display_type")] = "BOGUS" }

        assertEquals(TransactionDisplayType.TREND, repository.getTransactionDisplayType().first())
    }

    @Test
    fun setTheme_shouldPersistAndBeReadableViaGetTheme_whenCalled() = runTest {
        repository.setTheme(AppTheme.DARK)

        assertEquals(AppTheme.DARK, repository.getTheme().first())
    }

    @Test
    fun getHomeDateFilterState_shouldReturnNormalizedDefault_whenNothingStored() = runTest {
        val filter = repository.getHomeDateFilterState().first()

        assertEquals(1, filter.presetIndex)
        assertTrue(filter.from <= filter.to)
    }

    @Test
    fun setHomeDateFilterState_shouldNormalizeFromAndTo_whenFromIsAfterTo() = runTest {
        repository.setHomeDateFilterState(SavedDateFilter(presetIndex = 2, from = 2000L, to = 1000L))

        val filter = repository.getHomeDateFilterState().first()
        assertEquals(1000L, filter.from)
        assertEquals(2000L, filter.to)
    }

    @Test
    fun setTransactionsDateFilterState_shouldNormalizeFromAndTo_whenFromIsAfterTo() = runTest {
        repository.setTransactionsDateFilterState(
            SavedDateFilter(presetIndex = 2, from = 5000L, to = 3000L),
        )

        val filter = repository.getTransactionsDateFilterState().first()
        assertEquals(3000L, filter.from)
        assertEquals(5000L, filter.to)
    }

    @Test
    fun setChartsDateFilterState_shouldNormalizeFromAndTo_whenFromIsAfterTo() = runTest {
        repository.setChartsDateFilterState(
            SavedDateFilter(presetIndex = 2, from = 9000L, to = 4000L),
        )

        val filter = repository.getChartsDateFilterState().first()
        assertEquals(4000L, filter.from)
        assertEquals(9000L, filter.to)
    }

    @Test
    fun getChartsDateFilterState_shouldReturnDefaultWithMonthDuration_whenNothingStored() = runTest {
        val homeFilter = repository.getHomeDateFilterState().first()
        val chartsFilter = repository.getChartsDateFilterState().first()

        val homeDuration = homeFilter.to - homeFilter.from
        val chartsDuration = chartsFilter.to - chartsFilter.from
        assertTrue("Charts default duration should be longer than Home's", chartsDuration > homeDuration)
    }

    @Test
    fun setDataEncryptionEnabled_shouldPersistFlagAndNotifyEncryptionManager_whenEnabled() = runTest {
        repository.setDataEncryptionEnabled(true)

        assertEquals(true, repository.getDataEncryptionEnabled().first())
        verify(exactly = 1) { DatabaseEncryptionManager.setEncryptionDesired(context, true) }
    }

    @Test
    fun resetAllPreferences_shouldRestoreAllDefaultsAndDisableEncryption_whenCalled() = runTest {
        repository.setTheme(AppTheme.DARK)
        repository.setLanguage(AppLanguage.ITALIAN)
        repository.setCurrencySymbol("$")
        repository.setDataEncryptionEnabled(true)
        repository.setTransactionDisplayType(TransactionDisplayType.CATEGORY)

        repository.resetAllPreferences()

        assertEquals(AppTheme.SYSTEM, repository.getTheme().first())
        assertEquals(AppLanguage.SYSTEM, repository.getLanguage().first())
        assertEquals("€", repository.getCurrencySymbol().first())
        assertEquals(false, repository.getDataEncryptionEnabled().first())
        assertEquals(TransactionDisplayType.TREND, repository.getTransactionDisplayType().first())
        verify(exactly = 1) { DatabaseEncryptionManager.setEncryptionDesired(context, false) }
    }
}
