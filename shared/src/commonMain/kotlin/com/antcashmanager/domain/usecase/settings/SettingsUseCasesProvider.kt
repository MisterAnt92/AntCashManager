package com.antcashmanager.domain.usecase.settings

/**
 * Provider for all settings-related use cases (Get/Set).
 *
 * **Purpose**: Simplify dependency injection for ViewModels by providing
 * all settings use cases in a single object, eliminating need for
 * 40+ individual constructor parameters.
 *
 * **Pattern**: Replaces having to inject 40+ individual settings use cases
 * (GetThemeUseCase, SetThemeUseCase, GetLanguageUseCase, SetLanguageUseCase, etc.)
 * with a single SettingsUseCasesProvider parameter.
 *
 * **Usage in ViewModel**:
 * ```kotlin
 * class SettingsViewModel(
 *     private val settingsUseCases: SettingsUseCasesProvider,
 * ) : BaseViewModel<None>() {
 *     // Use: settingsUseCases.getTheme, settingsUseCases.setTheme, etc.
 * }
 * ```
 *
 * **Usage in DI**:
 * ```kotlin
 * factory {
 *     SettingsUseCasesProvider(
 *         getTheme = GetSettingUseCase(getter = { get<SettingsRepository>().getTheme() }),
 *         setTheme = SetSettingUseCase(setter = { get<SettingsRepository>().setTheme(it) }),
 *         // ... etc
 *     )
 * }
 * ```
 *
 * **Benefits**:
 * - Single DI registration instead of 40+
 * - Cleaner ViewModel constructor
 * - Type-safe access to all use cases
 * - Easy to add new settings (just add parameter)
 * - Strongly typed (no string-based access)
 */
public data class SettingsUseCasesProvider(
    // Theme & Language (UI state)
    val getTheme: GetSettingUseCase<String>,
    val setTheme: SetSettingUseCase<String>,
    val getLanguage: GetSettingUseCase<String>,
    val setLanguage: SetSettingUseCase<String>,

    // Display preferences (accessibility)
    val getShowCharts: GetSettingUseCase<Boolean>,
    val setShowCharts: SetSettingUseCase<Boolean>,
    val getHighContrast: GetSettingUseCase<Boolean>,
    val setHighContrast: SetSettingUseCase<Boolean>,
    val getLargeText: GetSettingUseCase<Boolean>,
    val setLargeText: SetSettingUseCase<Boolean>,
    val getReduceMotion: GetSettingUseCase<Boolean>,
    val setReduceMotion: SetSettingUseCase<Boolean>,
    val getShowTransactionNotes: GetSettingUseCase<Boolean>,

    // Number formatting
    val getCurrencySymbol: GetSettingUseCase<String>,
    val setCurrencySymbol: SetSettingUseCase<String>,
    val getDecimalDigits: GetSettingUseCase<Int>,
    val setDecimalDigits: SetSettingUseCase<Int>,
    val getDecimalSeparator: GetSettingUseCase<String>,
    val setDecimalSeparator: SetSettingUseCase<String>,
    val getThousandsSeparator: GetSettingUseCase<String>,
    val setThousandsSeparator: SetSettingUseCase<String>,

    // Date filtering & display
    val getTransactionDisplayType: GetSettingUseCase<String>,
    val setTransactionDisplayType: SetSettingUseCase<String>,

    // Other settings
    val setTutorialCompleted: SetSettingUseCase<Boolean>,
    val resetAllPreferences: ResetAllPreferencesUseCase,
)
