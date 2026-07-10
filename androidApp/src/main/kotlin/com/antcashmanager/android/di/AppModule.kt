package com.antcashmanager.android.di

import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.data.backup.BackupService
import com.antcashmanager.android.data.receipt.MlKitReceiptOcrService
import com.antcashmanager.android.domain.usecase.feedback.SendFeedbackEmailUseCase
import com.antcashmanager.android.ui.screen.categories.CategoriesViewModel
import com.antcashmanager.android.ui.screen.charts.ChartsViewModel
import com.antcashmanager.android.ui.screen.home.HomeViewModel
import com.antcashmanager.android.ui.screen.homeTransactionDetail.TransactionDetailsViewModel
import com.antcashmanager.android.ui.screen.receiptScan.ReceiptScanViewModel
import com.antcashmanager.android.ui.screen.settings.SettingsViewModel
import com.antcashmanager.android.ui.screen.settingsData.SettingsDataViewModel
import com.antcashmanager.android.ui.screen.settingsDisplay.DisplayViewModel
import com.antcashmanager.android.ui.screen.transactionAdd.AddTransactionViewModel
import com.antcashmanager.android.ui.screen.transactions.TransactionsViewModel
import com.antcashmanager.android.ui.theme.ThemeViewModel
import com.antcashmanager.data.local.DatabaseProvider
import com.antcashmanager.data.repository.CategoryRepositoryImpl
import com.antcashmanager.data.repository.SettingsRepositoryImpl
import com.antcashmanager.data.repository.TransactionRepositoryImpl
import com.antcashmanager.data.security.LocalDataCipherImpl
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.security.LocalDataCipher
import com.antcashmanager.domain.service.ReceiptOcrService
import com.antcashmanager.domain.usecase.ShareTransactionUseCase
import com.antcashmanager.domain.usecase.category.DeleteCategoryUseCase
import com.antcashmanager.domain.usecase.category.GetCategoriesUseCase
import com.antcashmanager.domain.usecase.category.InsertCategoryUseCase
import com.antcashmanager.domain.usecase.category.UpdateCategoryUseCase
import com.antcashmanager.domain.usecase.receipt.CreateTransactionFromReceiptUseCase
import com.antcashmanager.domain.usecase.receipt.ScanReceiptUseCase
import com.antcashmanager.domain.usecase.settings.GetChartsDateFilterStateUseCase
import com.antcashmanager.domain.usecase.settings.GetHomeDateFilterStateUseCase
import com.antcashmanager.domain.usecase.settings.GetLanguageUseCase
import com.antcashmanager.domain.usecase.settings.GetThemeUseCase
import com.antcashmanager.domain.usecase.settings.GetTransactionsDateFilterStateUseCase
import com.antcashmanager.domain.usecase.settings.SetChartsDateFilterStateUseCase
import com.antcashmanager.domain.usecase.settings.SetHomeDateFilterStateUseCase
import com.antcashmanager.domain.usecase.settings.SetLanguageUseCase
import com.antcashmanager.domain.usecase.settings.SetThemeUseCase
import com.antcashmanager.domain.usecase.settings.SetTransactionsDateFilterStateUseCase
import com.antcashmanager.domain.usecase.transaction.DeleteAllTransactionsUseCase
import com.antcashmanager.domain.usecase.transaction.DeleteTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.FilterTransactionsUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionSuggestionsUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionsByDateRangeUseCase
import com.antcashmanager.domain.usecase.transaction.GetTransactionsUseCase
import com.antcashmanager.domain.usecase.transaction.InsertTransactionUseCase
import com.antcashmanager.domain.usecase.transaction.UpdateTransactionUseCase
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
val dataModule = module {
    single { DatabaseProvider.getDatabase(androidApplication()) }
    single<LocalDataCipher> { LocalDataCipherImpl(androidApplication()) }
    single<TransactionRepository> {
        TransactionRepositoryImpl(
            transactionDao = get<com.antcashmanager.data.local.AppDatabase>().transactionDao(),
            localDataCipher = get(),
        )
    }
    single<SettingsRepository> { SettingsRepositoryImpl(androidApplication()) }
    single<CategoryRepository> {
        CategoryRepositoryImpl(
            categoryDao = get<com.antcashmanager.data.local.AppDatabase>().categoryDao(),
        )
    }
    single { AnalyticsManager(androidApplication()) }
    single {
        BackupService(
            transactionRepository = get(),
            categoryRepository = get(),
        )
    }
    factory<ReceiptOcrService> { MlKitReceiptOcrService() }
}

val useCaseModule = module {
    factory { GetTransactionsUseCase(transactionRepository = get()) }
    factory { GetTransactionsByDateRangeUseCase(transactionRepository = get()) }
    factory { GetTransactionSuggestionsUseCase(repository = get()) }
    factory { InsertTransactionUseCase(transactionRepository = get()) }
    factory { UpdateTransactionUseCase(transactionRepository = get()) }
    factory { DeleteTransactionUseCase(transactionRepository = get()) }
    factory { DeleteAllTransactionsUseCase(transactionRepository = get()) }
    factory { FilterTransactionsUseCase() }

    factory { GetCategoriesUseCase(categoryRepository = get()) }
    factory { InsertCategoryUseCase(categoryRepository = get()) }
    factory { UpdateCategoryUseCase(categoryRepository = get()) }
    factory { DeleteCategoryUseCase(categoryRepository = get()) }

    factory { GetThemeUseCase(settingsRepository = get()) }
    factory { SetThemeUseCase(settingsRepository = get()) }
    factory { ScanReceiptUseCase(ocrService = get()) }
    factory { GetLanguageUseCase(settingsRepository = get()) }
    factory { SetLanguageUseCase(settingsRepository = get()) }
    factory { GetHomeDateFilterStateUseCase(settingsRepository = get()) }
    factory { SetHomeDateFilterStateUseCase(settingsRepository = get()) }
    factory { GetChartsDateFilterStateUseCase(settingsRepository = get()) }
    factory { SetChartsDateFilterStateUseCase(settingsRepository = get()) }
    factory { GetTransactionsDateFilterStateUseCase(settingsRepository = get()) }
    factory { SetTransactionsDateFilterStateUseCase(settingsRepository = get()) }

    factory { CreateTransactionFromReceiptUseCase(transactionRepository = get()) }
    factory { ShareTransactionUseCase() }
    factory { SendFeedbackEmailUseCase() }
}

// ─────────────────────────────────────────────────────────────────────────────
// Presentation Layer (ViewModel)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Modulo Koin per il Presentation Layer.
 * Ogni ViewModel è registrato come `viewModel` (lifecycle-aware singleton per composable).
 * [AddTransactionViewModel] accetta il parametro opzionale `transactionId` via `parametersOf`.
 */
val presentationModule = module {
    viewModel {
        HomeViewModel(
            getTransactionsUseCase = get(),
            filterTransactionsUseCase = get(),
            getTransactionSuggestionsUseCase = get(),
            getHomeDateFilterStateUseCase = get(),
            setHomeDateFilterStateUseCase = get(),
            getCategoriesUseCase = get(),
        )
    }
    viewModel {
        CategoriesViewModel(
            getCategoriesUseCase = get(),
            insertCategoryUseCase = get(),
            updateCategoryUseCase = get(),
            deleteCategoryUseCase = get(),
        )
    }
    viewModel {
        ChartsViewModel(
            getTransactionsByDateRangeUseCase = get(),
            getChartsDateFilterStateUseCase = get(),
            setChartsDateFilterStateUseCase = get(),
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
        )
    }
    viewModel { (transactionId: Long?) ->
        AddTransactionViewModel(
            transactionId = transactionId,
            transactionRepository = get(),
            getCategoriesUseCase = get(),
            insertTransactionUseCase = get(),
            updateTransactionUseCase = get(),
            deleteTransactionUseCase = get(),
            getTransactionSuggestionsUseCase = get(),
        )
    }
    viewModel {
        SettingsViewModel(
            settingsRepository = get(),
            transactionRepository = get(),
            getThemeUseCase = get(),
            setThemeUseCase = get(),
            getLanguageUseCase = get(),
            setLanguageUseCase = get(),
            sendFeedbackEmailUseCase = get(),
        )
    }
    viewModelOf(::DisplayViewModel)
    viewModelOf(::SettingsDataViewModel)
    viewModel {
        ReceiptScanViewModel(
            scanReceiptUseCase = get(),
            createTransactionUseCase = get(),
            getCategoriesUseCase = get(),
            getTransactionSuggestionsUseCase = get(),
        )
    }
    viewModelOf(::TransactionDetailsViewModel)
    viewModelOf(::ThemeViewModel)
}

/** Aggregazione di tutti i moduli Koin dell'app. */
val appModules = listOf(dataModule, useCaseModule, presentationModule)
