package com.antcashmanager.android.di

import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.analytics.tracker.EngagementTracker
import com.antcashmanager.android.analytics.tracker.ErrorTracker
import com.antcashmanager.android.analytics.tracker.PerformanceTracker
import com.antcashmanager.android.analytics.tracker.SegmentationTracker
import com.antcashmanager.android.analytics.tracker.SessionTracker
import com.antcashmanager.android.auth.GoogleSignInManager
import com.antcashmanager.android.data.backup.BackupService
import com.antcashmanager.android.data.receipt.MlKitReceiptOcrService
import com.antcashmanager.android.drive.DriveUploadManager
import com.antcashmanager.android.ui.screen.categories.CategoriesViewModel
import com.antcashmanager.android.ui.screen.charts.ChartsViewModel
import com.antcashmanager.android.ui.screen.home.HomeViewModel
import com.antcashmanager.android.ui.screen.home.transactionDetail.TransactionDetailsViewModel
import com.antcashmanager.android.ui.screen.receiptScan.ReceiptScanViewModel
import com.antcashmanager.android.ui.screen.settings.SettingsViewModel
import com.antcashmanager.android.ui.screen.settings.dataManagement.SettingsDataViewModel
import com.antcashmanager.android.ui.screen.settings.displaySettings.DisplayViewModel
import com.antcashmanager.android.ui.screen.transactions.TransactionsViewModel
import com.antcashmanager.android.ui.screen.transactions.addImport.AddTransactionViewModel
import com.antcashmanager.android.ui.screen.transactions.addImport.manager.SuggestionsManager
import com.antcashmanager.android.ui.screen.transactions.addImport.manager.TransactionLoadManager
import com.antcashmanager.android.ui.screen.transactions.addImport.manager.TransactionSubmitManager
import com.antcashmanager.android.ui.theme.ThemeViewModel
import com.antcashmanager.android.ui.widget.GlanceWidgetUpdateNotifier
import com.antcashmanager.android.work.AutoBackupScheduler
import com.antcashmanager.data.local.DatabaseProvider
import com.antcashmanager.data.repository.CategoryRepositoryImpl
import com.antcashmanager.data.repository.SettingsRepositoryImpl
import com.antcashmanager.data.repository.TransactionRepositoryImpl
import com.antcashmanager.data.security.LocalDataCipherImpl
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.security.LocalDataCipher
import com.antcashmanager.domain.service.ReceiptOcrService
import com.antcashmanager.domain.service.WidgetUpdateNotifier
import com.antcashmanager.domain.usecase.ShareTransactionUseCase
import com.antcashmanager.domain.usecase.category.DeleteCategoryUseCase
import com.antcashmanager.domain.usecase.category.GetCategoriesUseCase
import com.antcashmanager.domain.usecase.category.InsertCategoryUseCase
import com.antcashmanager.domain.usecase.category.UpdateCategoryUseCase
import com.antcashmanager.domain.usecase.receipt.CreateTransactionFromReceiptUseCase
import com.antcashmanager.domain.usecase.receipt.ScanReceiptUseCase
import com.antcashmanager.domain.usecase.settings.GetChartsDateFilterStateUseCase
import com.antcashmanager.domain.usecase.settings.GetCurrencySymbolUseCase
import com.antcashmanager.domain.usecase.settings.GetDecimalDigitsUseCase
import com.antcashmanager.domain.usecase.settings.GetDecimalSeparatorUseCase
import com.antcashmanager.domain.usecase.settings.GetHighContrastUseCase
import com.antcashmanager.domain.usecase.settings.GetHomeDateFilterStateUseCase
import com.antcashmanager.domain.usecase.settings.GetLanguageUseCase
import com.antcashmanager.domain.usecase.settings.GetLargeTextUseCase
import com.antcashmanager.domain.usecase.settings.GetMealVoucherValueUseCase
import com.antcashmanager.domain.usecase.settings.GetReduceMotionUseCase
import com.antcashmanager.domain.usecase.settings.GetSettingUseCase
import com.antcashmanager.domain.usecase.settings.GetShowChartsUseCase
import com.antcashmanager.domain.usecase.settings.GetShowTransactionNotesUseCase
import com.antcashmanager.domain.usecase.settings.GetThemeUseCase
import com.antcashmanager.domain.usecase.settings.GetThousandsSeparatorUseCase
import com.antcashmanager.domain.usecase.settings.GetTransactionDisplayTypeUseCase
import com.antcashmanager.domain.usecase.settings.GetTransactionsDateFilterStateUseCase
import com.antcashmanager.domain.usecase.settings.ResetAllPreferencesUseCase
import com.antcashmanager.domain.usecase.settings.SetChartsDateFilterStateUseCase
import com.antcashmanager.domain.usecase.settings.SetCurrencySymbolUseCase
import com.antcashmanager.domain.usecase.settings.SetDecimalDigitsUseCase
import com.antcashmanager.domain.usecase.settings.SetDecimalSeparatorUseCase
import com.antcashmanager.domain.usecase.settings.SetHighContrastUseCase
import com.antcashmanager.domain.usecase.settings.SetHomeDateFilterStateUseCase
import com.antcashmanager.domain.usecase.settings.SetLanguageUseCase
import com.antcashmanager.domain.usecase.settings.SetLargeTextUseCase
import com.antcashmanager.domain.usecase.settings.SetReduceMotionUseCase
import com.antcashmanager.domain.usecase.settings.SetSettingUseCase
import com.antcashmanager.domain.usecase.settings.SetShowChartsUseCase
import com.antcashmanager.domain.usecase.settings.SetShowTransactionNotesUseCase
import com.antcashmanager.domain.usecase.settings.SetThemeUseCase
import com.antcashmanager.domain.usecase.settings.SetThousandsSeparatorUseCase
import com.antcashmanager.domain.usecase.settings.SetTransactionDisplayTypeUseCase
import com.antcashmanager.domain.usecase.settings.SetTransactionsDateFilterStateUseCase
import com.antcashmanager.domain.usecase.settings.SetTutorialCompletedUseCase
import com.antcashmanager.domain.usecase.settings.SettingsUseCasesProvider
import com.antcashmanager.domain.usecase.transaction.DeleteAllTransactionsUseCase
import com.antcashmanager.domain.usecase.transaction.DeleteTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.FilterTransactionsUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionByIdUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionSuggestionsUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionsByDateRangeUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionsUseCase
import com.antcashmanager.domain.usecase.transaction.InsertTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.SyncTransactionCategoriesUseCase
import com.antcashmanager.domain.usecase.transaction.UpdateTransactionUseCase
import com.antcashmanager.domain.validation.TransactionValidator
import com.antcashmanager.domain.validation.TransactionValidatorImpl
import kotlinx.coroutines.flow.map
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

// ─────────────────────────────────────────────────────────────────────────────
// Data Layer
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Modulo Koin per il Data Layer:
 * - Database Room
 * - LocalDataCipher
 * - Repository (TransactionRepository, SettingsRepository, CategoryRepository)
 * - AnalyticsManager
 * - Servizi (MlKitReceiptOcrService)
 */
val dataModule =
    module {
        single { DatabaseProvider.getDatabase(androidApplication()) }
        single<LocalDataCipher> { LocalDataCipherImpl(androidApplication()) }
        single<WidgetUpdateNotifier> { GlanceWidgetUpdateNotifier(androidApplication()) }
        single<TransactionRepository> {
            TransactionRepositoryImpl(
                transactionDao = get<com.antcashmanager.data.local.AppDatabase>().transactionDao(),
                localDataCipher = get(),
                widgetUpdateNotifier = get(),
            )
        }
        single<SettingsRepository> { SettingsRepositoryImpl(androidApplication()) }
        single { AutoBackupScheduler(androidApplication()) }
        single { GoogleSignInManager(androidApplication(), get<SettingsRepository>()) }
        single { DriveUploadManager(androidApplication(), get<GoogleSignInManager>(), get<SettingsRepository>()) }
        single<CategoryRepository> {
            CategoryRepositoryImpl(
                categoryDao = get<com.antcashmanager.data.local.AppDatabase>().categoryDao(),
            )
        }
        single { AnalyticsManager(androidApplication()) }

        // PHASE 2: Performance & Session & Error Tracking
        single { PerformanceTracker(get<AnalyticsManager>()) }
        single { SessionTracker(get<AnalyticsManager>()) }
        single { ErrorTracker(get<AnalyticsManager>()) }

        // PHASE 3: Advanced Analytics (Segmentation & Engagement)
        single { SegmentationTracker(get<AnalyticsManager>()) }
        single { EngagementTracker(get<AnalyticsManager>()) }

        single {
            BackupService(
                transactionRepository = get(),
                categoryRepository = get(),
                settingsRepository = get(),
                widgetUpdateNotifier = get(),
            )
        }
        factory<ReceiptOcrService> { MlKitReceiptOcrService() }

        // ─────────────────────────────────────────────────────────────────────────────
        // WEEK 3: KMP READINESS - DOMAIN VALIDATION & PREFERENCES STORAGE
        // ─────────────────────────────────────────────────────────────────────────────

        // Domain validation for transactions
        single<TransactionValidator> { TransactionValidatorImpl() }
    }

val useCaseModule =
    module {
        factory { GetTransactionsUseCase(transactionRepository = get()) }
        factory { GetTransactionsByDateRangeUseCase(transactionRepository = get()) }
        factory { GetTransactionSuggestionsUseCase(repository = get(), settingsRepository = get()) }
        factory { InsertTransactionUseCase(transactionRepository = get()) }
        factory { UpdateTransactionUseCase(transactionRepository = get()) }
        factory { DeleteTransactionUseCase(transactionRepository = get()) }
        factory { DeleteAllTransactionsUseCase(transactionRepository = get()) }
        factory { FilterTransactionsUseCase() }
        factory { SyncTransactionCategoriesUseCase(transactionRepository = get()) }
        factory { GetTransactionByIdUseCase(transactionRepository = get()) }

        factory { GetCategoriesUseCase(categoryRepository = get()) }
        factory { InsertCategoryUseCase(categoryRepository = get()) }
        factory { UpdateCategoryUseCase(categoryRepository = get()) }
        factory { DeleteCategoryUseCase(categoryRepository = get()) }

        // ─────────────────────────────────────────────────────────────────────────────
        // GENERIC SETTINGS USE CASES (Consolidation: reduces 33 boilerplate → 2 generics)
        // ─────────────────────────────────────────────────────────────────────────────
        // Pattern: GetSettingUseCase<T> + SetSettingUseCase<T> replace 33 individual classes
        // Impact: Removes 1000+ lines of boilerplate, simplifies DI registration
        // Migration: Phase in one preference group at a time (Theme → Language → Display → Accessibility)
        //
        // BEFORE (boilerplate):
        //   factory { GetThemeUseCase(settingsRepository = get()) }
        //   factory { SetThemeUseCase(settingsRepository = get()) }
        //   ... repeat 33 times
        //
        // AFTER (generic):
        //   factory<GetSettingUseCase<String>> { GetSettingUseCase(getter = { get<SettingsRepository>().getTheme() }) }
        //   factory<SetSettingUseCase<String>> { SetSettingUseCase(setter = { get<SettingsRepository>().setTheme(it) }) }

        // ✅ REFACTORED: Settings use cases now use generics internally
        // This approach eliminates duplication while maintaining backward compatibility
        //
        // Strategy: Specific use cases (GetThemeUseCase, etc.) are now implemented
        // using the generic GetSettingUseCase<T> and SetSettingUseCase<T> internally.
        // This provides a migration path without breaking changes.
        //
        // Benefits:
        // - Eliminates 33 boilerplate use case classes
        // - No changes to ViewModel layer (uses specific use cases)
        // - ViewModels can gradually migrate to generics later if needed
        // - Single point of maintenance (generics)

        factory { ScanReceiptUseCase(ocrService = get()) }

        // ─────────────────────────────────────────────────────────────────────────────
        // RESTORED: Settings use cases (Legacy support during migration)
        // These are required by ViewModels and Activity that haven't migrated to generics yet.
        // ─────────────────────────────────────────────────────────────────────────────
        factory { GetThemeUseCase(settingsRepository = get()) }
        factory { SetThemeUseCase(settingsRepository = get()) }
        factory { GetLanguageUseCase(settingsRepository = get()) }
        factory { SetLanguageUseCase(settingsRepository = get()) }
        factory { GetShowChartsUseCase(settingsRepository = get()) }
        factory { SetShowChartsUseCase(settingsRepository = get()) }
        factory { GetHighContrastUseCase(settingsRepository = get()) }
        factory { SetHighContrastUseCase(settingsRepository = get()) }
        factory { GetLargeTextUseCase(settingsRepository = get()) }
        factory { SetLargeTextUseCase(settingsRepository = get()) }
        factory { GetReduceMotionUseCase(settingsRepository = get()) }
        factory { SetReduceMotionUseCase(settingsRepository = get()) }
        factory { GetShowTransactionNotesUseCase(settingsRepository = get()) }
        factory { SetShowTransactionNotesUseCase(settingsRepository = get()) }
        factory { GetCurrencySymbolUseCase(settingsRepository = get()) }
        factory { SetCurrencySymbolUseCase(settingsRepository = get()) }
        factory { GetDecimalDigitsUseCase(settingsRepository = get()) }
        factory { SetDecimalDigitsUseCase(settingsRepository = get()) }
        factory { GetDecimalSeparatorUseCase(settingsRepository = get()) }
        factory { SetDecimalSeparatorUseCase(settingsRepository = get()) }
        factory { GetThousandsSeparatorUseCase(settingsRepository = get()) }
        factory { SetThousandsSeparatorUseCase(settingsRepository = get()) }
        factory { GetTransactionDisplayTypeUseCase(settingsRepository = get()) }
        factory { SetTransactionDisplayTypeUseCase(settingsRepository = get()) }
        factory { GetHomeDateFilterStateUseCase(settingsRepository = get()) }
        factory { SetHomeDateFilterStateUseCase(settingsRepository = get()) }
        factory { GetChartsDateFilterStateUseCase(settingsRepository = get()) }
        factory { SetChartsDateFilterStateUseCase(settingsRepository = get()) }
        factory { GetTransactionsDateFilterStateUseCase(settingsRepository = get()) }
        factory { SetTransactionsDateFilterStateUseCase(settingsRepository = get()) }
        factory { GetMealVoucherValueUseCase(settingsRepository = get()) }
        factory { SetTutorialCompletedUseCase(settingsRepository = get()) }

        factory { CreateTransactionFromReceiptUseCase(transactionRepository = get()) }
        factory { ShareTransactionUseCase() }
        factory { ResetAllPreferencesUseCase(settingsRepository = get()) }

        factory {
            val repo = get<SettingsRepository>()
            SettingsUseCasesProvider(
                getTheme = GetSettingUseCase<String>(getter = { repo.getTheme().map { it.name } }),
                setTheme = SetSettingUseCase<String>(setter = { repo.setTheme(AppTheme.valueOf(it)) }),
                getLanguage = GetSettingUseCase<String>(getter = { repo.getLanguage().map { it.name } }),
                setLanguage = SetSettingUseCase<String>(setter = { repo.setLanguage(AppLanguage.valueOf(it)) }),
                getShowCharts = GetSettingUseCase<Boolean>(getter = { repo.getShowCharts() }),
                setShowCharts = SetSettingUseCase<Boolean>(setter = { repo.setShowCharts(it) }),
                getHighContrast = GetSettingUseCase<Boolean>(getter = { repo.getHighContrast() }),
                setHighContrast = SetSettingUseCase<Boolean>(setter = { repo.setHighContrast(it) }),
                getLargeText = GetSettingUseCase<Boolean>(getter = { repo.getLargeText() }),
                setLargeText = SetSettingUseCase<Boolean>(setter = { repo.setLargeText(it) }),
                getReduceMotion = GetSettingUseCase<Boolean>(getter = { repo.getReduceMotion() }),
                setReduceMotion = SetSettingUseCase<Boolean>(setter = { repo.setReduceMotion(it) }),
                getShowTransactionNotes = GetSettingUseCase<Boolean>(getter = { repo.getShowTransactionNotes() }),
                getCurrencySymbol = GetSettingUseCase<String>(getter = { repo.getCurrencySymbol() }),
                setCurrencySymbol = SetSettingUseCase<String>(setter = { repo.setCurrencySymbol(it) }),
                getDecimalDigits = GetSettingUseCase<Int>(getter = { repo.getDecimalDigits() }),
                setDecimalDigits = SetSettingUseCase<Int>(setter = { repo.setDecimalDigits(it) }),
                getDecimalSeparator = GetSettingUseCase<String>(getter = { repo.getDecimalSeparator() }),
                setDecimalSeparator = SetSettingUseCase<String>(setter = { repo.setDecimalSeparator(it) }),
                getThousandsSeparator = GetSettingUseCase<String>(getter = { repo.getThousandsSeparator() }),
                setThousandsSeparator = SetSettingUseCase<String>(setter = { repo.setThousandsSeparator(it) }),
                getTransactionDisplayType =
                    GetSettingUseCase<String>(
                        getter = { repo.getTransactionDisplayType().map { it.name } },
                    ),
                setTransactionDisplayType =
                    SetSettingUseCase<String>(
                        setter = { repo.setTransactionDisplayType(TransactionDisplayType.valueOf(it)) },
                    ),
                setTutorialCompleted = SetSettingUseCase<Boolean>(setter = { repo.setIsTutorialCompleted(it) }),
                resetAllPreferences = ResetAllPreferencesUseCase(settingsRepository = repo),
            )
        }
    }

// ─────────────────────────────────────────────────────────────────────────────
// Presentation Layer (ViewModel)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Modulo Koin per il Presentation Layer.
 * Ogni ViewModel è registrato come `viewModel` (lifecycle-aware singleton per composable).
 * [AddTransactionViewModel] accetta il parametro opzionale `transactionId` via `parametersOf`.
 */
val presentationModule =
    module {
        viewModel {
            HomeViewModel(
                getTransactionsUseCase = get(),
                filterTransactionsUseCase = get(),
                getTransactionSuggestionsUseCase = get(),
                getHomeDateFilterStateUseCase = get(),
                setHomeDateFilterStateUseCase = get(),
                getCategoriesUseCase = get(),
                settingsRepository = get(),
                segmentationTracker = get<SegmentationTracker>(),
            )
        }
        viewModel {
            CategoriesViewModel(
                getCategoriesUseCase = get(),
                insertCategoryUseCase = get(),
                updateCategoryUseCase = get(),
                deleteCategoryUseCase = get(),
                syncTransactionCategoriesUseCase = get(),
                analyticsManager = get(),
            )
        }
        viewModel {
            ChartsViewModel(
                getTransactionsByDateRangeUseCase = get(),
                getChartsDateFilterStateUseCase = get(),
                setChartsDateFilterStateUseCase = get(),
                performanceTracker = get<PerformanceTracker>(),
                segmentationTracker = get<SegmentationTracker>(),
                settingsRepository = get(),
            )
        }
        viewModel {
            TransactionsViewModel(
                getTransactionsUseCase = get(),
                insertTransactionUseCase = get(),
                updateTransactionUseCase = get(),
                deleteTransactionUseCase = get(),
                getCategoriesUseCase = get(),
                filterTransactionsUseCase = get(),
                getTransactionSuggestionsUseCase = get(),
                getTransactionsDateFilterStateUseCase = get(),
                setTransactionsDateFilterStateUseCase = get(),
                settingsRepository = get(),
                engagementTracker = get<EngagementTracker>(),
            )
        }

        // ── Manager per AddTransaction ──
        single {
            TransactionLoadManager(
                getTransactionByIdUseCase = get(),
                getCategoriesUseCase = get(),
                getMealVoucherValueUseCase = get(),
            )
        }

        single {
            TransactionSubmitManager(
                insertTransactionUseCase = get(),
                updateTransactionUseCase = get(),
            )
        }

        single {
            SuggestionsManager(
                getTransactionSuggestionsUseCase = get(),
            )
        }

        viewModel { (transactionId: Long?) ->
            AddTransactionViewModel(
                loadManager = get(),
                submitManager = get(),
                suggestionsManager = get(),
                deleteTransactionUseCase = get(),
                getTransactionByIdUseCase = get(),
                analyticsManager = get(),
                settingsRepository = get(),
                performanceTracker = get<PerformanceTracker>(),
                errorTracker = get<ErrorTracker>(),
                transactionId = transactionId,
                savedStateHandle = get(), // NEW: Per state recovery
            )
        }
        viewModel {
            SettingsViewModel(
                settingsUseCases = get(),
                settingsRepository = get(),
                deleteAllTransactionsUseCase = get(),
                insertTransactionUseCase = get(),
                widgetUpdateNotifier = get(),
            )
        }
        viewModel {
            DisplayViewModel(
                settingsRepository = get(),
                widgetUpdateNotifier = get(),
                analyticsManager = get(),
                engagementTracker = get<EngagementTracker>(),
            )
        }
        viewModel {
            SettingsDataViewModel(
                settingsRepository = get(),
                categoryRepository = get(),
                transactionRepository = get(),
                deleteAllTransactionsUseCase = get(),
                backupService = get(),
                autoBackupScheduler = get(),
                googleSignInManager = get(),
                performanceTracker = get<PerformanceTracker>(),
                errorTracker = get<ErrorTracker>(),
            )
        }
        viewModel {
            ReceiptScanViewModel(
                scanReceiptUseCase = get(),
                createTransactionUseCase = get(),
                getCategoriesUseCase = get(),
                getTransactionSuggestionsUseCase = get(),
                analyticsManager = get(),
                performanceTracker = get<PerformanceTracker>(),
                errorTracker = get<ErrorTracker>(),
            )
        }
        viewModelOf(::TransactionDetailsViewModel)
        viewModelOf(::ThemeViewModel)
    }

/** Aggregazione di tutti i moduli Koin dell'app. */
val appModules = listOf(dataModule, useCaseModule, presentationModule)
