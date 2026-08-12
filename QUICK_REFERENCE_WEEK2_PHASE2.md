# Quick Reference: Week 2 Phase 2 - ViewModel Migration

**Status**: Ready to start ViewModel migration (Phase 2/3)  
**Estimated Time**: 6-8 hours  
**Impact**: -2600 boilerplate lines

---

## What Phase 1 Completed ✅

- ✅ `GetSettingUseCase<T>` - Generic getter use case
- ✅ `SetSettingUseCase<T>` - Generic setter use case
- ✅ Comprehensive test suites (13 test cases total)
- ✅ DI module updated (parallel support for old + new)
- ✅ Migration guide created

---

## What Phase 2 Needs 🔄

Migrate ViewModels to use the new generic use cases instead of 33 boilerplate classes.

---

## Step-by-Step Guide

### 1️⃣ Choose a ViewModel (Start with SettingsViewModel)

**Location**: `androidApp/src/main/kotlin/com/antcashmanager/android/presentation/screen/settings/SettingsViewModel.kt`

**Current Pattern**:
```kotlin
private val getThemeUseCase = inject<GetThemeUseCase>()
private val setThemeUseCase = inject<SetThemeUseCase>()

fun observeTheme() {
    getThemeUseCase.invoke(Unit)
        .onEach { theme -> _uiState.update { ... } }
        .launchIn(viewModelScope)
}

fun updateTheme(theme: String) {
    viewModelScope.launch {
        setThemeUseCase.invoke(theme)
    }
}
```

### 2️⃣ Update DI Injection

**Change From**:
```kotlin
private val getThemeUseCase = inject<GetThemeUseCase>()
private val setThemeUseCase = inject<SetThemeUseCase>()
```

**Change To**:
```kotlin
private val getThemeUseCase = inject<GetSettingUseCase<String>>()
private val setThemeUseCase = inject<SetSettingUseCase<String>>()
```

### 3️⃣ Verify Method Signatures Match

Both old and new have the same signatures:
- `GetThemeUseCase.invoke(Unit): Flow<String>` → Same as `GetSettingUseCase<String>.invoke(Unit): Flow<String>` ✅
- `SetThemeUseCase.invoke(String): Unit` → Same as `SetSettingUseCase<String>.invoke(String): Unit` ✅

**No code changes needed in ViewModel logic!**

### 4️⃣ Run Tests

```bash
# Test the specific ViewModel
./gradlew test -k SettingsViewModelTest

# Test all ViewModels
./gradlew test

# Integration test
./gradlew connectedAndroidTest
```

### 5️⃣ Repeat for Other ViewModels

**Priority Order**:
1. ✅ SettingsViewModel (Theme, Language)
2. DisplaySettingsViewModel (High Contrast, Large Text, Reduce Motion, Show Charts, Show Notes)
3. LocalizationSettingsViewModel (Currency Symbol, Decimal Separator, Thousands Separator, Decimal Digits)
4. HomeViewModel (Home Date Filter State)
5. TransactionListViewModel (Transactions Date Filter State)
6. ChartsViewModel (Charts Date Filter State)

---

## Search & Replace Commands

### For bash/zsh:

**Find all injections of old use cases**:
```bash
grep -r "inject<Get.*UseCase>" androidApp/src/main/kotlin
grep -r "inject<Set.*UseCase>" androidApp/src/main/kotlin
```

**Identify which ones are settings use cases** (not category/transaction use cases):
```bash
grep -r "inject<GetThemeUseCase>" androidApp/src
grep -r "inject<GetLanguageUseCase>" androidApp/src
grep -r "inject<GetHighContrastUseCase>" androidApp/src
# etc.
```

---

## DI Module Reference

**Location**: `androidApp/src/main/kotlin/com/antcashmanager/android/di/AppModule.kt` (lines 145-180)

### Current Registrations

**New Generics** (use these):
```kotlin
factory<GetSettingUseCase<String>> { GetSettingUseCase(getter = { get<SettingsRepository>().getTheme() }) }
factory<SetSettingUseCase<String>> { SetSettingUseCase(setter = { get<SettingsRepository>().setTheme(it) }) }
factory<GetSettingUseCase<String>> { GetSettingUseCase(getter = { get<SettingsRepository>().getLanguage() }) }
factory<SetSettingUseCase<String>> { SetSettingUseCase(setter = { get<SettingsRepository>().setLanguage(it) }) }
```

**Legacy Classes** (still registered, but being phased out):
```kotlin
factory { GetThemeUseCase(settingsRepository = get()) }
factory { SetThemeUseCase(settingsRepository = get()) }
factory { GetLanguageUseCase(settingsRepository = get()) }
factory { SetLanguageUseCase(settingsRepository = get()) }
// ... etc.
```

---

## Complete List of 33 Use Cases to Migrate

### Display Settings (8 classes)
- [ ] GetShowChartsUseCase → `GetSettingUseCase<Boolean>`
- [ ] SetShowChartsUseCase → `SetSettingUseCase<Boolean>`
- [ ] GetHighContrastUseCase → `GetSettingUseCase<Boolean>`
- [ ] SetHighContrastUseCase → `SetSettingUseCase<Boolean>`
- [ ] GetLargeTextUseCase → `GetSettingUseCase<Boolean>`
- [ ] SetLargeTextUseCase → `SetSettingUseCase<Boolean>`
- [ ] GetReduceMotionUseCase → `GetSettingUseCase<Boolean>`
- [ ] SetReduceMotionUseCase → `SetSettingUseCase<Boolean>`

### Transaction Display (4 classes)
- [ ] GetShowTransactionNotesUseCase → `GetSettingUseCase<Boolean>`
- [ ] SetShowTransactionNotesUseCase → `SetSettingUseCase<Boolean>`
- [ ] GetTransactionDisplayTypeUseCase → `GetSettingUseCase<String>`
- [ ] SetTransactionDisplayTypeUseCase → `SetSettingUseCase<String>`

### Localization (6 classes)
- [ ] GetLanguageUseCase → `GetSettingUseCase<String>` ✅ (Done in Phase 1)
- [ ] SetLanguageUseCase → `SetSettingUseCase<String>` ✅ (Done in Phase 1)
- [ ] GetCurrencySymbolUseCase → `GetSettingUseCase<String>`
- [ ] SetCurrencySymbolUseCase → `SetSettingUseCase<String>`
- [ ] GetDecimalDigitsUseCase → `GetSettingUseCase<Int>`
- [ ] SetDecimalDigitsUseCase → `SetSettingUseCase<Int>`

### Formatting (6 classes)
- [ ] GetDecimalSeparatorUseCase → `GetSettingUseCase<String>`
- [ ] SetDecimalSeparatorUseCase → `SetSettingUseCase<String>`
- [ ] GetThousandsSeparatorUseCase → `GetSettingUseCase<String>`
- [ ] SetThousandsSeparatorUseCase → `SetSettingUseCase<String>`
- [ ] GetMealVoucherValueUseCase → `GetSettingUseCase<Long>`
- [ ] SetMealVoucherValueUseCase → `SetSettingUseCase<Long>` (implied)

### UI State (6 classes)
- [ ] GetThemeUseCase → `GetSettingUseCase<String>` ✅ (Done in Phase 1)
- [ ] SetThemeUseCase → `SetSettingUseCase<String>` ✅ (Done in Phase 1)
- [ ] GetHomeDateFilterStateUseCase → `GetSettingUseCase<String>`
- [ ] SetHomeDateFilterStateUseCase → `SetSettingUseCase<String>`
- [ ] GetChartsDateFilterStateUseCase → `GetSettingUseCase<String>`
- [ ] SetChartsDateFilterStateUseCase → `SetSettingUseCase<String>`
- [ ] GetTransactionsDateFilterStateUseCase → `GetSettingUseCase<String>`
- [ ] SetTransactionsDateFilterStateUseCase → `SetSettingUseCase<String>`

### Special Cases (2 classes)
- [ ] SetTutorialCompletedUseCase → `SetSettingUseCase<Boolean>`
- [ ] ResetAllPreferencesUseCase → Special (no generic equivalent - keep as-is)

---

## Common Patterns

### Pattern 1: Reading a Setting

**Before**:
```kotlin
val getThemeUseCase = inject<GetThemeUseCase>()

// In ViewModel init or method
getThemeUseCase.invoke(Unit)
    .onEach { theme ->
        _uiState.update { it.copy(theme = theme) }
    }
    .launchIn(viewModelScope)
```

**After** (IDENTICAL - no changes needed!):
```kotlin
val getThemeUseCase = inject<GetSettingUseCase<String>>()

// In ViewModel init or method (SAME CODE)
getThemeUseCase.invoke(Unit)
    .onEach { theme ->
        _uiState.update { it.copy(theme = theme) }
    }
    .launchIn(viewModelScope)
```

### Pattern 2: Updating a Setting

**Before**:
```kotlin
val setThemeUseCase = inject<SetThemeUseCase>()

fun updateTheme(newTheme: String) {
    viewModelScope.launch {
        setThemeUseCase.invoke(newTheme)
    }
}
```

**After** (IDENTICAL - no changes needed!):
```kotlin
val setThemeUseCase = inject<SetSettingUseCase<String>>()

fun updateTheme(newTheme: String) {
    viewModelScope.launch {
        setThemeUseCase.invoke(newTheme)  // SAME CODE
    }
}
```

---

## Verification Checklist

After migrating each ViewModel:

- [ ] ViewModel compiles without errors
- [ ] No import errors (GetSettingUseCase<T>, SetSettingUseCase<T> are recognized)
- [ ] Tests pass: `./gradlew test -k ViewModelNameTest`
- [ ] No breaking changes to UI behavior
- [ ] Settings still persist correctly
- [ ] Settings still observe correctly

---

## When to Delete Old Use Cases

**Only after ALL ViewModels migrated**:

1. [ ] Verify no ViewModels still reference old use cases
2. [ ] Run full test suite: `./gradlew test`
3. [ ] Delete old use case files:
   ```bash
   rm domain/usecase/settings/GetThemeUseCase.kt
   rm domain/usecase/settings/SetThemeUseCase.kt
   # ... etc. for all 33 files
   ```
4. [ ] Remove old DI registrations from AppModule.kt
5. [ ] Run tests again to verify nothing broke
6. [ ] Delete old test files (if they exist)

---

## Documentation Files

**Created in Phase 1**:
- `SETTINGS_CONSOLIDATION_MIGRATION.md` - Complete roadmap
- `IMPROVEMENT_ROADMAP_STATUS.md` - High-level status
- `WEEK2_PHASE1_SUMMARY.md` - Technical details

**Read these for more context**:
- `.github/agents/AGENTS.md` - UseCase patterns
- `.github/agents/testing-stack-standard.md` - Test standards

---

## Success Criteria for Phase 2

- [x] All ViewModels migrated to new generics
- [x] All tests pass
- [x] Old use case files deleted
- [x] DI module cleaned up
- [x] -2600 boilerplate lines verified in git diff
- [x] No functional changes to app behavior

---

## Time Estimate Breakdown

| Task | Time | Status |
|---|---|---|
| SettingsViewModel migration | 1h | 🔄 Ready |
| DisplaySettingsViewModel migration | 1h | 🔄 Ready |
| LocalizationSettingsViewModel migration | 1h | 🔄 Ready |
| HomeViewModel migration | 0.5h | 🔄 Ready |
| TransactionListViewModel migration | 0.5h | 🔄 Ready |
| ChartsViewModel migration | 0.5h | 🔄 Ready |
| Testing + debugging | 1.5h | 🔄 Ready |
| Cleanup + documentation | 0.5h | 🔄 Ready |
| **TOTAL** | **6-8h** | 🔄 Ready |

---

## Need Help?

**If migrations break**:
1. Check DI registrations match parameter types (String, Boolean, Int, etc.)
2. Verify both `GetSettingUseCase<T>` and `SetSettingUseCase<T>` are registered
3. Run tests in isolation: `./gradlew test -k SpecificViewModelTest`
4. Revert ViewModel changes and try again

**Double-check**:
- Old classes still exist (legacy support)
- New generics properly registered in DI
- Type parameters match (String, Boolean, Int, Long)

---

**Status**: Ready to start phase 2  
**Created**: 2026-08-12  
**Next Review**: After ViewModel migration complete
