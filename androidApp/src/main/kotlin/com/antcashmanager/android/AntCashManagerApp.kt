package com.antcashmanager.android

import android.app.Application
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.data.migrateSettingsFromLegacyDataStore
import com.antcashmanager.android.di.appModules
import com.antcashmanager.android.work.AutoBackupNotifier
import com.antcashmanager.domain.manager.ProfileManager
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.security.LocalDataCipher
import com.antcashmanager.domain.usecase.category.EnsureDefaultCategoriesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class AntCashManagerApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // In release: suppress Verbose/Debug logs to avoid leaking internal state
        // to logcat and to let R8 dead-code-eliminate the unused log lambdas.
        if (!BuildConfig.DEBUG) Logger.setMinSeverity(Severity.Warn)
        Logger.d(tag = "AntCashManagerApp") { "Application started" }

        // Migrate legacy "settings" DataStore (v1.7) to split stores (v1.8) before Koin creates
        // any DataStore instance. runBlocking is intentional: a one-shot disk read/write that
        // completes in <100ms and must finish before any SettingsRepository access.
        runBlocking(Dispatchers.IO) {
            migrateSettingsFromLegacyDataStore(this@AntCashManagerApp)
        }

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@AntCashManagerApp)
            modules(appModules)
        }

        // Apply consent synchronously so Crashlytics is enabled before any coroutine or
        // ViewModel starts. Without this, crashes in the ~300ms window between Koin init and
        // the first DataStore emission are silently lost (firebase_crashlytics_collection_enabled
        // is false in the Manifest for GDPR compliance). The async collect below handles future
        // consent changes (e.g. user toggles the setting).
        runBlocking(Dispatchers.IO) {
            val consent = get<SettingsRepository>().getAnalyticsConsent().first()
            get<AnalyticsManager>().applyConsent(consent == true)
        }

        appScope.launch {
            // Crea il notification channel per i fallimenti del backup automatico
            AutoBackupNotifier.ensureChannel(this@AntCashManagerApp)

            // Keep consent in sync with DataStore for the app's lifetime (GDPR toggle).
            get<SettingsRepository>().getAnalyticsConsent().collect { consent ->
                get<AnalyticsManager>().applyConsent(consent == true)
            }
        }

        appScope.launch {
            // Initialize ProfileManager before any ViewModel reads activeProfileId
            get<ProfileManager>().initialize()
            get<EnsureDefaultCategoriesUseCase>()()
            ensureCategorySortOrderInitialized()
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            get<LocalDataCipher>().clearCache()
        }
    }

    /**
     * Backfill one-shot: assegna `sortOrder` in base all'ordine alfabetico per tipo alle
     * categorie già esistenti su un device che aggiorna da una versione precedente dell'app,
     * prima dell'introduzione del riordino manuale. Senza questo passaggio tutte le categorie
     * erediterebbero `sortOrder = 0` dalla migration Room, rendendo l'ordine visualizzato
     * instabile/arbitrario invece di preservare quello alfabetico già visto dall'utente.
     */
    private suspend fun ensureCategorySortOrderInitialized() {
        val settingsRepository: SettingsRepository = get()
        if (settingsRepository.getCategorySortOrderInitialized().first()) return

        val categoryRepository: CategoryRepository = get()
        categoryRepository
            .getAllCategories()
            .first()
            .groupBy { it.type }
            .values
            .forEach { categoriesOfType ->
                categoriesOfType.sortedBy { it.name }.forEachIndexed { index, category ->
                    if (category.sortOrder != index) {
                        categoryRepository.updateCategory(category.copy(sortOrder = index))
                    }
                }
            }

        settingsRepository.setCategorySortOrderInitialized(true)
        Logger.d(tag = "AntCashManagerApp") { "Category sort order backfilled" }
    }

}
