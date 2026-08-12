# Copilot Prompt Context - AntCashManager

Questo file fornisce contesto locale per GitHub Copilot. **Tutte le regole dettagliate sono in [AGENTS.md](../../AGENTS.md)** - consultalo come source of truth.

## Quick Reference

**Project**: Android KMP app, Clean Architecture 3-layer  
**Key Frameworks**: Jetpack Compose, Kotlin Coroutines, Room, DataStore, Koin DI  
**Localization**: 5 languages (en, it, fr, de, es)

## Architecture Reminder

```
Presentation (androidApp)  
  ↓ depends on Domain only  
Domain (shared/commonMain)  
  ↓ implemented by Data  
Data (shared/androidMain)
```

**Organization**: Package-by-feature (not by technical type)

## Essential Rules (See AGENTS.md for Complete Details)

### UseCase Pattern
- Extend base class: `UseCase<P,R>`, `NoParamsUseCase<R>`, `ObservableUseCase<P,R>`, or `NoParamsObservableUseCase<R>`
- Implement `execute()` (NOT `invoke()`)
- Accept `dispatcher` parameter
- Return `Result<T>` (wrapping handled by base class)
- **See AGENTS.md lines 111-136**

### ViewModel Pattern  
- Expose: `StateFlow` (public)
- Keep private: `MutableStateFlow`
- Use Kermit for logging (never Log/println)
- Accept UseCase instances ONLY (not repositories)
- **See AGENTS.md lines 140-151**

### Feature File Structure
```
ui/screen/<feature>/
  <Feature>Screen.kt        # Composable
  <Feature>ViewModel.kt     # State management
  <Feature>State.kt         # UI state data class
  <Feature>Constants.kt     # Feature-specific constants (if needed)
  model/                    # Reusable feature models
  view/                     # Sub-composables
```
**See AGENTS.md lines 154-168**

### Testing Standards
- **Framework**: JUnit 4 + MockK (Mockito forbidden)
- **Base Class**: `BaseUnitTest` (ViewModel tests), `BaseUseCaseTest` (domain tests)
- **Naming**: `method_shouldExpectedBehavior_whenCondition` (no backticks)
- **Roboelectric**: ONLY instrumentation tests (`src/androidTest`) - NOT unit tests
- **See AGENTS.md lines 227-354**

### String Localization
- ALL user-facing strings → `strings.xml` (5 locales: en, it, fr, de, es)
- Use `stringResource(R.string.key)` everywhere
- Check for duplicates: `grep -r "string_key" androidApp/src/main/res/values*/`
- **See AGENTS.md lines 217-224**

## Quick Links

- **Complete Architecture Guide**: [AGENTS.md](../../AGENTS.md)
- **Testing Agent**: [.github/agents/agent-unit-tests-mockk.agent.md](.github/agents/agent-unit-tests-mockk.agent.md)
- **Clean Architecture Agent**: [.github/agents/agent-android-clean-architecture.agent.md](.github/agents/agent-android-clean-architecture.agent.md)
- **Agent Index**: [.github/agents/README.md](.github/agents/README.md)

## When in Doubt

1. **Read AGENTS.md** - it's the source of truth
2. **Consult relevant agent file** - for specialized guidance (testing, architecture, cleanup)
3. **Use existing patterns** - reference `HomeScreen`, `SettingsScreen`, `DisplayScreen`, `ReceiptScanScreen`
4. **Prioritize**: Clean code > Speed, Testability > Clever code

---

**Last Updated**: 2026-08-12  
**Note**: This is a reference file. For complete, authoritative rules, always check AGENTS.md.
