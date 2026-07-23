# AGENTS.md – AntCashManager

Guide for AI coding agents working in this codebase. Read before making any changes.

---

## Architecture Overview

3-layer **Clean Architecture** with strict dependency direction:

```
Presentation (androidApp)  →  Domain (shared/commonMain)  →  Data (shared/androidMain)
```

- **`androidApp/`** – Compose UI, ViewModels, Navigation, DI wiring (Koin), Android-specific utilities.
- **`shared/src/commonMain/`** – Pure Kotlin domain: models, use case base classes, repository interfaces, domain exceptions.
- **`shared/src/androidMain/`** – Android data layer: Room DB, DataStore, repository implementations.

Code is organized **package-by-feature**, not by technical type. Reference screens: `HomeScreen`, `SettingsScreen`, `DisplayScreen`.

---

## Key Commands

```bash
# Build debug APK
./gradlew :androidApp:assembleDebug

# Run unit tests (shared domain layer)
./gradlew :shared:testAndroidHostTest

# Run unit tests (androidApp ViewModel layer)
./gradlew :androidApp:testDebugUnitTest

# Instrumentation tests (requires device/emulator)
./gradlew :androidApp:connectedDebugAndroidTest

# Release build
./gradlew :androidApp:assembleRelease
```

Build **only after all changes are complete** – avoid incremental builds during implementation.

---

## UseCase Pattern

All use cases extend one of these base classes from `shared/commonMain/domain/usecase/`:
- `BaseUseCase<Params, Result>` – single suspend call
- `FlowUseCase`, `NoParamsFlowUseCase`, `NoParamsUseCase` – variants for flows and parameterless cases

**Rules:**
- Implement `execute()`, never override `invoke()`.
- Always inject a `CoroutineDispatcher` (default: `Dispatchers.Default`).
- Always return `Result<T>` – never throw domain exceptions directly.
- Custom exceptions live **only** in `shared/commonMain/domain/exception/`.

```kotlin
class InsertTransactionUseCase(
    private val transactionRepository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : BaseUseCase<Transaction, Result<Unit>>(dispatcher) {
    override suspend fun execute(params: Transaction): Result<Unit> = ...
}
```

---

## ViewModel Pattern

- Expose **only** `StateFlow` (public), keep `MutableStateFlow` private.
- Consume `Result<T>` via `onSuccess`/`onFailure`.
- Use **Kermit** for logging (`co.touchlab:kermit`) – never use `Log` or `println`.
- No `Context` references, no business logic.
- Use `activeJob?.cancel()` pattern for cancellable operations.

---

## Feature File Structure

```
ui/screen/<feature>/
    <Feature>Screen.kt        # Composable UI
    <Feature>ViewModel.kt     # State management
    <Feature>State.kt         # data class for UI state (stays HERE only)
    <Feature>Constants.kt     # Shared constants for the feature
    model/                    # Other feature-specific data classes
    view/                     # Sub-composables
```

- `<Feature>State` **must not** be duplicated or aliased via `typealias`.
- Feature `data class`es that are reusable belong in the `model/` sub-package.
- If a feature shares constants across files, create `<Feature>Constants` (e.g., `SettingsConstant.kt`).

---

## Dependency Injection (Koin)

All DI is wired in `androidApp/.../di/AppModule.kt` with three modules aggregated as `appModules`:
- `dataModule` – Room DB, repositories, services
- `useCaseModule` – use case factories
- `presentationModule` – ViewModel registrations

`AddTransactionViewModel` uses `parametersOf(transactionId)` for optional parameter injection.

---

## Navigation

Routes are string literals defined inline in `NavGraph.kt`. `BottomNavItem` enumerates the top-level destinations. Sub-routes use query parameters (e.g., `"add_transaction?transactionId={transactionId}"`).

---

## UI / Compose Rules

- All user-facing strings in `strings.xml` – **5 locales required**: `en`, `it`, `fr`, `de`, `es`. Never hardcode strings.
- **Before adding a new string**: verify it doesn't already exist in any of the `values*/strings.xml` files using `grep`. Example: `grep -r "string_key_name" androidApp/src/main/res/values*/`.
- Use `stringResource(R.string.*)` everywhere.
- Reuse existing components from `androidApp/.../ui/components/` before creating new ones.
- Every new `@Composable` **must** have at least two `@Preview`s: one light, one dark (`uiMode = Configuration.UI_MODE_NIGHT_YES`).
- Apply `MaterialTheme` for all colors, typography, and spacing (8.dp between cards).

---

## Testing

| Scope | Source Set | Base Class |
|---|---|---|
| ViewModel | `androidApp/src/test/kotlin` | `com.antcashmanager.android.BaseUnitTest` |
| Domain (commonMain) | `shared/src/commonTest/kotlin` | — |
| Data/Repository | `shared/src/androidHostTest/kotlin` | — |

- Use **MockK** for mocking; Mockito is forbidden.
- Test naming: `method_shouldExpectedBehavior_whenCondition` (no backticks).
- `BaseUnitTest` handles `Dispatchers.setMain`/`resetMain` and `StandardTestDispatcher` – don't duplicate this setup.

---

## Analytics

Only log the usage events listed in `README.md`. Never include user content (notes, amounts, payee names, etc.) in analytics events. Firebase standard `screen_view` is tracked automatically via `NavGraph.kt`.

---

## R8 Minification & PlayStore Release

### Configuration
- **R8 enabled** with `proguard-android-optimize.txt` (most aggressive preset)
- **Resource shrinking enabled** – removes unused XML, drawable, layout resources
- **ProGuard rules:** `androidApp/proguard-rules.pro` (comprehensive coverage: Kotlin, Room, Firebase, Koin, Compose, ML Kit, security)
- **Crashlytics mapping upload** – automatic via Firebase (stack trace deobfuscation in production)

### Pre-Release Checklist

Before submitting to PlayStore:

```bash
# 1. Run all tests
./gradlew test connectedAndroidTest

# 2. Build release bundle
./gradlew clean :androidApp:bundleRelease

# 3. Test on real device/emulator
# Deploy the AAB to a physical device and verify:
# - App launches without crash
# - Koin DI resolves all dependencies
# - Room database queries work correctly
# - ML Kit OCR functions properly
# - Serialization deserializes JSON payloads
# - No unexpected crashes in Crashlytics console
```

### PlayStore Upload
1. **Google Play Console** → App → **Release** → **Create New Release**
2. Upload `androidApp/build/outputs/bundle/release/app-release.aab`
3. Verify **App Signing** (Google Play manages keys)
4. Add **Release Notes** and review content policies
5. Submit for review or internal testing first

### Post-Release Monitoring
- Check **Crashlytics** console for deobfuscated stack traces (should resolve within 24h)
- Monitor **Play Console** → **Quality** → **Crashes and ANRs** for production issues
- Watch user reviews for crashes in first 48 hours

### Size Optimization Tips
- `proguard-rules.pro` includes log stripping (`Log.d/v/i` removed in release)
- If size still > 100 MB, check for unused dependencies using `./gradlew :androidApp:dependencies`
- Use `bundletool` to analyze bundle: `bundletool inspect-bundle --bundle=app-release.aab --mode=summary`

---

## Files to Ignore

Never modify files matching `.gitignore` patterns: `build/`, `.gradle/`, `.idea/`, `*.jks`, `google-services.json`, `local.properties`, `secrets.properties`.

