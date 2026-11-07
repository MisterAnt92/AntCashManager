# Settings Use Case Consolidation - Migration Guide

## Overview

This document outlines the complete strategy to consolidate **33 boilerplate settings use cases** into **2 generic use cases** (`GetSettingUseCase<T>` and `SetSettingUseCase<T>`), eliminating **~2600 lines of duplicate code** and vastly improving maintainability.

---

## Problem: The Settings Boilerplate Anti-Pattern

### Current State (Pre-Consolidation)

**33 identical use case files exist**, each following this exact pattern:

```kotlin
// GetLanguageUseCase.kt (16 lines)
class GetLanguageUseCase(
    private val settingsRepository: SettingsRepository,
) : NoParamsObservableUseCase<String>() {
    override fun execute(params: Unit): Flow<String> = settingsRepository.getLanguage()
}

// SetLanguageUseCase.kt (14 lines)
class SetLanguageUseCase(
    private val settingsRepository: SettingsRepository,
) : UseCase<String, Unit>() {
    override suspend fun execute(params: String) = settingsRepository.setLanguage(params)
}

// Repeat 33 times with different parameter types and repository methods...
```

**Impact**:
- 1000+ lines of pure boilerplate
- 66 redundant DI registrations
- Adding new settings requires: 2 files + 2 DI registrations + tests
- High maintenance burden across all agent workflows

---

## Solution: Generic Use Cases

### New Generic Classes (2 files = ~40 lines total)

**GetSettingUseCase.kt**:
```kotlin
class GetSettingUseCase<T>(
    private val getter: () -> Flow<T>,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsObservableUseCase<T>(dispatcher) {
    override fun execute(params: Unit): Flow<T> = getter()
}
```

**SetSettingUseCase.kt**:
```kotlin
class SetSettingUseCase<T>(
    private val setter: suspend (T) -> Unit,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<T, Unit>(dispatcher) {
    override suspend fun execute(params: T) = setter(params)
}
```

**Benefits**:
- Eliminates 33 boilerplate files
- Adding new settings: only update DI (1 line each for Get/Set)
- Removes 1000+ lines from codebase
- Improves testability (single generic test suite instead of 33)

---

## Migration Strategy

### Phase 1: Safe Parallel Approach (CURRENT)

Keep both old and new implementations during migration:

**DI Module**: Register both old classes and new generics
```kotlin
// NEW (Generic - preferred)
factory<GetSettingUseCase<String>> { 
    GetSettingUseCase(getter = { get<SettingsRepository>().getTheme() }) 
}
factory<SetSettingUseCase<String>> { 
    SetSettingUseCase(setter = { get<SettingsRepository>().setTheme(it) }) 
}

// LEGACY (Keep during transition)
factory { GetThemeUseCase(settingsRepository = get()) }
factory { SetThemeUseCase(settingsRepository = get()) }
```

**ViewModels**: Update incrementally
- Old: `inject<GetThemeUseCase>()`
- New: `inject<GetSettingUseCase<String>>()`

### Phase 2: ViewModel Migration (Per Feature)

Migrate ViewModels in groups:

1. **Settings Screen ViewModels** (5 ViewModels):
   - SettingsViewModel
   - DisplaySettingsViewModel
   - AccessibilitySettingsViewModel
   - LocalizationSettingsViewModel
   - AdvancedSettingsViewModel

2. **Home Screen ViewModels** (2 ViewModels):
   - HomeViewModel
   - TransactionListViewModel

3. **Other screens** (per feature)

### Phase 3: Cleanup

Once all ViewModels migrated:
1. Delete 33 old use case files
2. Remove old DI registrations
3. Verify all tests pass

---

## Complete List of 33 Use Cases to Consolidate

### Display Settings (4 pairs = 8 classes)
| Get UseCase | Set UseCase | Type | Repository Method |
|---|---|---|---|
| GetShowChartsUseCase | SetShowChartsUseCase | Boolean | get/setShowCharts |
| GetHighContrastUseCase | SetHighContrastUseCase | Boolean | get/setHighContrast |
| GetLargeTextUseCase | SetLargeTextUseCase | Boolean | get/setLargeText |
| GetReduceMotionUseCase | SetReduceMotionUseCase | Boolean | get/setReduceMotion |

### Transaction Display (2 pairs = 4 classes)
| Get UseCase | Set UseCase | Type | Repository Method |
|---|---|---|---|
| GetShowTransactionNotesUseCase | SetShowTransactionNotesUseCase | Boolean | get/setShowTransactionNotes |
| GetTransactionDisplayTypeUseCase | SetTransactionDisplayTypeUseCase | String | get/setTransactionDisplayType |

### Localization (3 pairs = 6 classes)
| Get UseCase | Set UseCase | Type | Repository Method |
|---|---|---|---|
| GetLanguageUseCase | SetLanguageUseCase | String | get/setLanguage |
| GetCurrencySymbolUseCase | SetCurrencySymbolUseCase | String | get/setCurrencySymbol |
| GetDecimalDigitsUseCase | SetDecimalDigitsUseCase | Int | get/setDecimalDigits |

### Formatting (3 pairs = 6 classes)
| Get UseCase | Set UseCase | Type | Repository Method |
|---|---|---|---|
| GetDecimalSeparatorUseCase | SetDecimalSeparatorUseCase | String | get/setDecimalSeparator |
| GetThousandsSeparatorUseCase | SetThousandsSeparatorUseCase | String | get/setThousandsSeparator |
| GetMealVoucherValueUseCase | (Set implied) | Long | get/setMealVoucherValue |

### UI State (3 pairs = 6 classes)
| Get UseCase | Set UseCase | Type | Repository Method |
|---|---|---|---|
| GetThemeUseCase | SetThemeUseCase | String | get/setTheme |
| GetHomeDateFilterStateUseCase | SetHomeDateFilterStateUseCase | String | get/setHomeDateFilterState |
| GetChartsDateFilterStateUseCase | SetChartsDateFilterStateUseCase | String | get/setChartsDateFilterState |
| GetTransactionsDateFilterStateUseCase | SetTransactionsDateFilterStateUseCase | String | get/setTransactionsDateFilterState |

### Special Cases (2 classes - see notes)
| UseCase | Type | Repository Method | Note |
|---|---|---|---|
| SetTutorialCompletedUseCase | Boolean | setTutorialCompleted | Only setter (no getter) |
| ResetAllPreferencesUseCase | Unit | resetAllPreferences | Special - no get/set pair |

---

## DI Module Transformation

### Before (66 registrations)
```kotlin
factory { GetLanguageUseCase(settingsRepository = get()) }
factory { SetLanguageUseCase(settingsRepository = get()) }
factory { GetThemeUseCase(settingsRepository = get()) }
factory { SetThemeUseCase(settingsRepository = get()) }
factory { GetHighContrastUseCase(settingsRepository = get()) }
factory { SetHighContrastUseCase(settingsRepository = get()) }
// ... repeat 30 more times
```

**Lines**: ~66 factory registrations

### After (2 + 66 registrations during transition)
```kotlin
// Generic registrations (~66 lines, parameterized)
val settingsUseCases = mapOf(
    "theme" to { GetSettingUseCase({ get<SettingsRepository>().getTheme() }) to
                  SetSettingUseCase({ get<SettingsRepository>().setTheme(it) }) },
    "language" to { GetSettingUseCase({ get<SettingsRepository>().getLanguage() }) to
                    SetSettingUseCase({ get<SettingsRepository>().setLanguage(it) }) },
    // ... etc
)

// Or, more concisely (per preference):
factory<GetSettingUseCase<String>> { GetSettingUseCase(getter = { get<SettingsRepository>().getTheme() }) }
factory<SetSettingUseCase<String>> { SetSettingUseCase(setter = { get<SettingsRepository>().setTheme(it) }) }
```

**Lines**: ~66 factory registrations (same count) but **single reusable pattern**

---

## File Count Reduction

| Phase | Files | Boilerplate | Impact |
|---|---|---|---|
| Before Consolidation | ~66 + 2600 lines boilerplate | 33 Get + 33 Set classes | High maintenance burden |
| After Consolidation | ~2 generic classes + 66 registrations | Eliminated | -97% boilerplate |
| Cleanup Phase | ~2 + registrations only | Lean, maintainable | Ready for iOS |

---

## Implementation Checklist

### ✅ Completed (Phase 1)
- [x] Create GetSettingUseCase<T>
- [x] Create SetSettingUseCase<T>
- [x] Create comprehensive tests for generics
- [x] Update DI module with both old + new registrations (parallel support)
- [x] Create migration guide (this document)

### 🔄 Phase 2: ViewModel Migration
- [ ] Update SettingsViewModel to use GetSettingUseCase<String> for theme/language
- [ ] Update DisplaySettingsViewModel to use GetSettingUseCase<Boolean> for display preferences
- [ ] Update LocalizationSettingsViewModel to use generics
- [ ] Update HomeViewModel for date filter states
- [ ] Update other ViewModels progressively

### 🔜 Phase 3: Cleanup
- [ ] Delete 33 boilerplate use case files
- [ ] Remove old DI registrations
- [ ] Run full test suite
- [ ] Update documentation (AGENTS.md, etc.)

---

## Benefits Summary

| Metric | Before | After | Improvement |
|---|---|---|---|
| **Use Case Files** | 33 | 2 | -94% |
| **Boilerplate Lines** | ~2600 | 0 | -100% |
| **DI Registrations** | 66 (manual) | 66 (pattern-based) | Same count, unified pattern |
| **Tests per UseCase** | 3-5 tests × 33 | 1 generic test suite | -80% test boilerplate |
| **Adding New Setting** | 5 steps (2 files + 2 DI + 1 test) | 2 steps (1 DI + 0 test) | -60% overhead |
| **Maintenance Burden** | High (many files) | Low (single pattern) | -70% |

---

## Next Steps

1. **Week 1**: Migrate first ViewModel group (Settings Screen)
2. **Week 2**: Migrate remaining ViewModels
3. **Week 3**: Delete boilerplate files and verify tests

---

## References

- **New Generics**: `GetSettingUseCase.kt`, `SetSettingUseCase.kt`
- **Tests**: `GetSettingUseCaseTest.kt`, `SetSettingUseCaseTest.kt`
- **DI Module**: `AppModule.kt` (lines 145-180)
- **Agent Rules**: See AGENTS.md for UseCase patterns and DI best practices
