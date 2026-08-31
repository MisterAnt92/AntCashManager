package com.antcashmanager.android.di

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
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Modulo Koin per il Presentation Layer – ViewModels e Manager di supporto.
 *
 * Ogni ViewModel è registrato come `viewModel` (lifecycle-aware).
 * I Manager ([TransactionLoadManager], [TransactionSubmitManager], [SuggestionsManager])
 * sono registrati come `single` poiché condivisi da [AddTransactionViewModel].
 *
 * Pattern di registrazione:
 * - Standard: `viewModel { ClassName(...) }` — costruttore esplicito con dipendenze iniettate
 * - Shorthand: `viewModelOf(::ClassName)` — per ViewModel senza parametri aggiuntivi
 * - Parametrizzato: `viewModel { (param: Type?) -> ... }` — [AddTransactionViewModel] usa
 *   `parametersOf(transactionId)` per l'iniezione del parametro opzionale
 */
val viewModelModule = module {

    viewModel {
        HomeViewModel(
            getTransactionsUseCase = get(),
            filterTransactionsUseCase = get(),
            getTransactionSuggestionsUseCase = get(),
            getHomeDateFilterStateUseCase = get(),
            setHomeDateFilterStateUseCase = get(),
            getCategoriesUseCase = get(),
            segmentationTracker = get(),
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
            performanceTracker = get(),
            segmentationTracker = get(),
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
            engagementTracker = get(),
        )
    }

    // ── Manager per AddTransaction ────────────────────────────────────────────
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
            performanceTracker = get(),
            errorTracker = get(),
            transactionId = transactionId,
            savedStateHandle = get(),
        )
    }

    viewModel {
        SettingsViewModel(
            settingsUseCases = get(),
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
            engagementTracker = get(),
        )
    }

    viewModel {
        SettingsDataViewModel(
            settingsRepository = get(),
            categoryRepository = get(),
            deleteAllTransactionsUseCase = get(),
            backupService = get(),
            autoBackupScheduler = get(),
            googleSignInManager = get(),
            performanceTracker = get(),
            errorTracker = get(),
        )
    }

    viewModel {
        ReceiptScanViewModel(
            scanReceiptUseCase = get(),
            createTransactionUseCase = get(),
            getCategoriesUseCase = get(),
            getTransactionSuggestionsUseCase = get(),
            analyticsManager = get(),
            performanceTracker = get(),
            errorTracker = get(),
        )
    }

    viewModelOf(::TransactionDetailsViewModel)
    viewModelOf(::ThemeViewModel)
}

