package com.antcashmanager.android.di

import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.repository.SettingsRepository
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
import kotlinx.coroutines.flow.map
import org.koin.dsl.module

/**
 * Modulo Koin per il Domain Layer – Use Cases.
 *
 * Seguendo il pattern Clean Architecture, ogni UseCase è registrato come `factory`
 * in modo che ogni ViewModel ne riceva un'istanza fresca e indipendente.
 *
 * I use case relativi alle impostazioni sfruttano il generic [GetSettingUseCase]/[SetSettingUseCase]
 * internamente per eliminare boilerplate (33 classi → 2 generics), mantenendo
 * retrocompatibilità verso il layer ViewModel.
 */
val useCaseModule = module {

    // ── Transaction use cases ──────────────────────────────────────────────────
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

    // ── Category use cases ────────────────────────────────────────────────────
    factory { GetCategoriesUseCase(categoryRepository = get()) }
    factory { InsertCategoryUseCase(categoryRepository = get()) }
    factory { UpdateCategoryUseCase(categoryRepository = get()) }
    factory { DeleteCategoryUseCase(categoryRepository = get()) }

    // ── Receipt use cases ─────────────────────────────────────────────────────
    factory { ScanReceiptUseCase(ocrService = get()) }
    factory { CreateTransactionFromReceiptUseCase(transactionRepository = get()) }

    // ── Share use case ────────────────────────────────────────────────────────
    factory { ShareTransactionUseCase() }

    // ── Settings use cases (specific – maintained for ViewModel backward compatibility) ──
    factory { GetThemeUseCase(settingsRepository = get()) }
    factory { SetThemeUseCase(settingsRepository = get()) }
    factory { GetLanguageUseCase(settingsRepository = get()) }
    factory { SetLanguageUseCase(settingsRepository = get()) }
    factory { GetHomeDateFilterStateUseCase(settingsRepository = get()) }
    factory { SetHomeDateFilterStateUseCase(settingsRepository = get()) }
    factory { GetChartsDateFilterStateUseCase(settingsRepository = get()) }
    factory { SetChartsDateFilterStateUseCase(settingsRepository = get()) }
    factory { GetTransactionsDateFilterStateUseCase(settingsRepository = get()) }
    factory { SetTransactionsDateFilterStateUseCase(settingsRepository = get()) }
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
    factory { GetDecimalSeparatorUseCase(settingsRepository = get()) }
    factory { SetDecimalSeparatorUseCase(settingsRepository = get()) }
    factory { GetThousandsSeparatorUseCase(settingsRepository = get()) }
    factory { SetThousandsSeparatorUseCase(settingsRepository = get()) }
    factory { GetDecimalDigitsUseCase(settingsRepository = get()) }
    factory { SetDecimalDigitsUseCase(settingsRepository = get()) }
    factory { GetTransactionDisplayTypeUseCase(settingsRepository = get()) }
    factory { SetTransactionDisplayTypeUseCase(settingsRepository = get()) }
    factory { GetMealVoucherValueUseCase(settingsRepository = get()) }
    factory { SetTutorialCompletedUseCase(settingsRepository = get()) }
    factory { ResetAllPreferencesUseCase(settingsRepository = get()) }

    // ── SettingsUseCasesProvider (generic approach, eliminates 33 boilerplate classes) ──
    factory {
        val repo = get<SettingsRepository>()
        SettingsUseCasesProvider(
            getTheme = GetSettingUseCase(getter = { repo.getTheme().map { it.name } }),
            setTheme = SetSettingUseCase(setter = { repo.setTheme(AppTheme.valueOf(it)) }),
            getLanguage = GetSettingUseCase(getter = { repo.getLanguage().map { it.name } }),
            setLanguage = SetSettingUseCase(setter = { repo.setLanguage(AppLanguage.valueOf(it)) }),
            getShowCharts = GetSettingUseCase(getter = { repo.getShowCharts() }),
            setShowCharts = SetSettingUseCase(setter = { repo.setShowCharts(it) }),
            getHighContrast = GetSettingUseCase(getter = { repo.getHighContrast() }),
            setHighContrast = SetSettingUseCase(setter = { repo.setHighContrast(it) }),
            getLargeText = GetSettingUseCase(getter = { repo.getLargeText() }),
            setLargeText = SetSettingUseCase(setter = { repo.setLargeText(it) }),
            getReduceMotion = GetSettingUseCase(getter = { repo.getReduceMotion() }),
            setReduceMotion = SetSettingUseCase(setter = { repo.setReduceMotion(it) }),
            getShowTransactionNotes = GetSettingUseCase(getter = { repo.getShowTransactionNotes() }),
            getCurrencySymbol = GetSettingUseCase(getter = { repo.getCurrencySymbol() }),
            setCurrencySymbol = SetSettingUseCase(setter = { repo.setCurrencySymbol(it) }),
            getDecimalDigits = GetSettingUseCase(getter = { repo.getDecimalDigits() }),
            setDecimalDigits = SetSettingUseCase(setter = { repo.setDecimalDigits(it) }),
            getDecimalSeparator = GetSettingUseCase(getter = { repo.getDecimalSeparator() }),
            setDecimalSeparator = SetSettingUseCase(setter = { repo.setDecimalSeparator(it) }),
            getThousandsSeparator = GetSettingUseCase(getter = { repo.getThousandsSeparator() }),
            setThousandsSeparator = SetSettingUseCase(setter = { repo.setThousandsSeparator(it) }),
            getTransactionDisplayType = GetSettingUseCase(
                getter = { repo.getTransactionDisplayType().map { it.name } }
            ),
            setTransactionDisplayType = SetSettingUseCase(
                setter = { repo.setTransactionDisplayType(TransactionDisplayType.valueOf(it)) }
            ),
            setTutorialCompleted = SetSettingUseCase(setter = { repo.setIsTutorialCompleted(it) }),
            resetAllPreferences = get(),
        )
    }
}

