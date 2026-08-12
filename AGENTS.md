# AGENTS.md – AntCashManager

**Complete guide for AI coding agents working in this codebase.**

**🚀 New to the project?** Start with [ARCHITECTURE_OVERVIEW.md](.github/docs/architecture/ARCHITECTURE_OVERVIEW.md) for a visual guide.  
**📚 Need specialized guidance?** See [.github/agents/README.md](.github/agents/README.md) for agent selection.

---

---

## ⚠️ CRITICAL: Never Commit Changes Without Asking

**AGENTS (AI assistants) MUST NEVER create git commits or push changes without explicit human authorization. NEVER. EVER.**

### Non-Negotiable Policy:
1. **ALWAYS ASK FIRST** – Before executing ANY git operation, ask the human for explicit permission
   - Show what changes will be staged
   - Show the proposed commit message
   - Wait for clear approval (e.g., "procedi", "vai", "sì", "yes", "commit")
   - Do NOT assume approval from unrelated prior messages

2. **NO `git add`** – Do not stage any files automatically without asking
3. **NO `git commit`** – Do not create commits under any circumstances without asking
4. **NO `git push`** – Do not push to remote or any branch without asking
5. **NO automated commits** – Even if authorized for a task, ask first before committing
6. **NO exceptions** – This rule has zero exceptions, zero edge cases

### Correct Workflow:
1. **WRITE & TEST** – Write code, edit files, create new files as needed for the task
2. **VERIFY** – Run tests and verify changes locally
3. **DESCRIBE** – Describe changes clearly to the human user with:
   - Summary of what was changed
   - Why each change was made
   - Any potential side effects or risks
4. **SHOW DIFFS** – Show `git diff` or detailed explanation of modifications
5. **ASK FOR PERMISSION** – Before ANY git operation, explicitly ask:
   - "Ready to commit these changes. Here's what will be staged: [list files]"
   - "Proposed commit message: [show message]"
   - "Should I proceed with commit? (yes/no)"
6. **WAIT FOR CLEAR APPROVAL** – Only after explicit human approval like:
   - "procedi" (Italian: proceed)
   - "vai" (Italian: go)
   - "yes" / "sì" (English/Italian: yes)
   - "commit" / "push"
   - "go ahead"
7. **EXECUTE** – Only then execute `git add`/`git commit`/`git push`

**IMPORTANT:** Do not assume approval from context or prior messages. If unsure, ask again.

### Why This Rule Exists:
- **Human Oversight**: All code changes must be reviewed and approved by a human
- **Repository Integrity**: Prevents accidental, unreviewed, or conflicting commits
- **Accountability**: Clear audit trail of who authorized what changes
- **Quality Control**: Humans can verify intent and correctness before commits
- **Safety**: Prevents automated commits that might break the build or introduce bugs

### Example Scenarios:
- ❌ WRONG: "Task complete, changes committed and pushed"
- ✅ RIGHT: "Task complete. Changes made to file.kt and test.kt. Ready for your review before committing?"

- ❌ WRONG: Commits made during multi-step task execution
- ✅ RIGHT: Collects all changes, presents them, waits for human "go ahead" signal

- ❌ WRONG: "I'll commit these changes as part of the refactoring"
- ✅ RIGHT: "Here are the refactored components. Ready to commit when you approve?"

This ensures human oversight on all code changes and maintains repository integrity.

---

## Architecture Overview

3-layer **Clean Architecture** with strict dependency direction:

```
Presentation (androidApp)  →  Domain (shared/commonMain)  →  Data (shared/androidMain)
```

- **`androidApp/`** – Compose UI, ViewModels, Navigation, DI wiring (Koin), Android-specific utilities, Glance Widgets.
- **`shared/src/commonMain/`** – Pure Kotlin domain: models, use case base classes, repository interfaces, domain exceptions.
- **`shared/src/androidMain/`** – Android data layer: Room DB, DataStore, repository implementations.

Code is organized **package-by-feature**, not by technical type. Reference screens: `HomeScreen`, `SettingsScreen`, `DisplayScreen`, `ReceiptScanScreen`.

**Widget Layer** (`androidApp/.../ui/widget/`): Glance API home screen widgets:
- `RecentTransactionsWidget` – displays latest transactions
- `CategoryBreakdownWidget` – displays category spending breakdown
- `GlanceWidgetUpdateNotifier` – implementation of domain's `WidgetUpdateNotifier` interface

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

All use cases extend one of these base classes from `shared/commonMain/domain/usecase/base/`:

### UseCase Base Class Selection Matrix

| Need Params? | Need Flow/Stream? | Base Class | Method | Example |
|---|---|---|---|---|
| ✅ Yes | ❌ No | `UseCase<P, R>` | `suspend fun execute(params: P): R` | Insert transaction |
| ❌ No | ❌ No | `NoParamsUseCase<R>` | `suspend fun execute(): R` | Get current balance |
| ✅ Yes | ✅ Yes | `ObservableUseCase<P, R>` | `fun execute(params: P): Flow<R>` | Observe filtered transactions |
| ❌ No | ✅ Yes | `NoParamsObservableUseCase<R>` | `fun execute(): Flow<R>` | Observe all settings changes |

**Quick Decision Flow**:
1. Does it need input parameters? → YES: use Params variant, NO: use NoParams variant
2. Does it need continuous stream (Flow)? → YES: use Observable variant, NO: use regular variant

**Rules:**
- Implement `execute()`, never override `invoke()`.
- Always inject a `CoroutineDispatcher` (default: `Dispatchers.Default`).
- `execute()` returns the raw value type `R`; `invoke()` wraps it in `Result<R>`.
- Never throw domain exceptions directly from `execute()` – they're caught by `invoke()` and wrapped in `Result.failure`.
- Custom exceptions live **only** in `shared/commonMain/domain/exception/`.
- Use `runSuspendCatching` utility (in `domain/util/`) which preserves `CancellationException` propagation.

```kotlin
class InsertTransactionUseCase(
    private val transactionRepository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<Transaction, Unit>(dispatcher) {
    override suspend fun execute(params: Transaction): Unit = 
        transactionRepository.insert(params)
}
// Consumer calls: insertUseCase(transaction).onSuccess { }.onFailure { }
```

---

## ViewModel Pattern

- Expose **only** `StateFlow` (public), keep `MutableStateFlow` private.
- Consume `Result<T>` via `onSuccess`/`onFailure`.
- Use **Kermit** for logging (`co.touchlab:kermit`) – never use `Log` or `println`.
- No `Context` references, no business logic.
- Use `activeJob?.cancel()` pattern for cancellable operations.
- **IMPORTANT**: ViewModel constructor accepts **UseCase instances only**, never Repository or other data-layer classes directly.
  - ViewModel receives fully-formed, testable UseCase dependencies via DI (Koin).
  - If you need data access in ViewModel, create a corresponding UseCase and inject it.
  - This ensures Clean Architecture separation: presentation layer depends on domain (UseCase), not on data layer.

---

## Feature File Structure (Consolidated)

**Single source of truth for organizing features** (organized by-feature, not by-type):

```
ui/screen/<feature>/
│
├── <Feature>Screen.kt              # Main Composable (max 400 lines)
├── <Feature>ViewModel.kt           # State management (max 300 lines)
├── <Feature>State.kt               # UI state data class (max 100 lines)
├── <Feature>Constants.kt           # Feature constants (if needed)
│
├── model/                          # Feature-specific reusable data classes
│   ├── <Model1>.kt
│   └── <Model2>.kt
│
└── view/                           # Sub-composables (if Screen > 150 lines)
    ├── <Component1>View.kt
    ├── <Component2>View.kt
    └── <Component3>View.kt
```

### Rules (CRITICAL)

| Rule | Requirement | Impact |
|------|---|---|
| **State location** | `<Feature>State` lives in `<Feature>State.kt` ONLY | Single source of truth |
| **No typealias** | Never alias state with `typealias` | Type safety, IDE navigation |
| **Line limits** | Screen ≤400, ViewModel ≤300, State ≤100 | Maintainability, testability |
| **Sub-composables** | Extract to `view/` if Screen > 150 lines | Code organization |
| **Constants** | Create `<Feature>Constants.kt` if shared across files | Avoid string duplication |
| **Models** | Reusable feature classes → `model/` | Modularity |

### Example: TransactionsScreen Feature Structure

```
ui/screen/transactions/
├── TransactionsScreen.kt           (175 lines)
├── TransactionsViewModel.kt        (240 lines)
├── TransactionsState.kt            (45 lines)
├── TransactionsConstants.kt        (8 lines)
├── model/
│   ├── TransactionFilterOption.kt
│   └── TransactionListItem.kt
└── view/
    ├── TransactionItemView.kt
    ├── FilterDialogView.kt
    └── EmptyStateView.kt
```

---

## Dependency Injection (Koin)

All DI is wired in `androidApp/.../di/AppModule.kt` with three modules aggregated as `appModules`:
- `dataModule` – Room DB, repositories, services (`BackupService`, `MlKitReceiptOcrService`), `GlanceWidgetUpdateNotifier`
- `useCaseModule` – use case factories
- `presentationModule` – ViewModel registrations

**ViewModel registration patterns**:
- Standard: `viewModel { ClassName(...) }` – explicit constructor with injected dependencies
- Shorthand: `viewModelOf(::ClassName)` – for ViewModels with no extra parameters (e.g., `DisplayViewModel`, `ThemeViewModel`)
- Parameterized: `viewModel { (param: Type?) -> ... }` – `AddTransactionViewModel` uses `parametersOf(transactionId)` for optional parameter injection

---

## Navigation

Routes are string literals defined inline in `NavGraph.kt`. `BottomNavItem` is a `sealed class` (not enum) with `data object` instances for each top-level tab (Home, Charts, Transactions, Categories, Settings). 

Sub-routes use query parameters:
- `"add_transaction?transactionId={transactionId}"` – create new or edit existing transaction
- `"display"` – display settings screen
- `"settings_data"` – data management (backup/restore)
- `"receipt_scan"` – ML Kit OCR receipt scanning

**Adaptive Navigation**: `NavGraph.kt` uses `rememberAdaptiveLayoutInfo()` to switch between bottom bar (phones) and navigation rail (tablets/foldables) based on screen size and form factor.

**Special case**: The root composable (`AntCashManagerNavHost`) directly injects `SettingsRepository` via Koin to read reactive display preferences – this is an intentional exception to the "ViewModels-only consume UseCases" rule for composition-level configuration.

---

## Receipt Scanning (ML Kit OCR)

The app includes receipt scanning via Google ML Kit Text Recognition v2:
- **Feature screen**: `ReceiptScanScreen` (route: `"receipt_scan"`)
- **Domain service interface**: `ReceiptOcrService` in `shared/commonMain/domain/service/`
- **Implementation**: `MlKitReceiptOcrService` in `androidApp/.../data/receipt/`
- **Use cases**:
  - `ScanReceiptUseCase` – extracts text from image bitmap
  - `CreateTransactionFromReceiptUseCase` – parses OCR result into transaction data
- **ViewModel**: `ReceiptScanViewModel` orchestrates camera capture → OCR → transaction creation flow
- **ProGuard**: ML Kit rules included in `androidApp/proguard-rules.pro` for R8 compatibility

---

## UI / Compose Rules

- All user-facing strings in `strings.xml` – **5 locales required**: `en`, `it`, `fr`, `de`, `es`. Never hardcode strings.
- **Before adding a new string**: verify it doesn't already exist in any of the `values*/strings.xml` files using `grep`. Example: `grep -r "string_key_name" androidApp/src/main/res/values*/`.
- Use `stringResource(R.string.*)` everywhere.
- Reuse existing components from `androidApp/.../ui/components/` before creating new ones.
- Every new `@Composable` **must** have at least two `@Preview`s: one light, one dark (`uiMode = Configuration.UI_MODE_NIGHT_YES`).
- Apply `MaterialTheme` for all colors, typography, and spacing (8.dp between cards).

### Common Reusable Components (Check Before Creating New)

| Component | Location | Use Case | Example |
|---|---|---|---|
| `AppCard` | `ui/components/card/` | Elevated card with consistent styling | List item container |
| `AppButton` | `ui/components/button/` | Styled button with ripple feedback | Save/Delete actions |
| `ScreenHeader` | `ui/components/layout/` | Screen title + navigation | Top of screen |
| `LoadingIndicator` | `ui/components/` | Circular progress | Data loading |
| `ErrorMessage` | `ui/components/` | Error display + retry | Failed operations |
| `EmptyState` | `ui/components/` | Empty list placeholder | No transactions |
| `BalanceCard` | `ui/components/card/` | Transaction/category display | Home dashboard |

**RULE**: Search components directory BEFORE creating new ones. Reuse saves ~20% codebase size.

---

## Testing

| Scope | Source Set | Base Class | Framework |
|---|---|---|---|
| ViewModel | `androidApp/src/test/kotlin` | `com.antcashmanager.android.BaseUnitTest` | JUnit 4 + MockK + Compose UI Test |
| Domain (commonMain) | `shared/src/commonTest/kotlin` | — | JUnit 4 + MockK |
| Data/Repository | `shared/src/androidHostTest/kotlin` | — | JUnit 4 + MockK |
| Instrumentation (UI) | `androidApp/src/androidTest/kotlin` | — | AndroidJUnit4 + Compose UI Test |

### 🔴 CRITICAL: Mocking & Test Libraries Rule

**ALWAYS use ONLY MockK for all test mocking. NO exceptions.**

- ✅ **MockK ONLY** – All unit tests, domain tests, repository tests use `io.mockk:mockk`
- ❌ **Mockito is COMPLETELY FORBIDDEN** – Never use `mockito-core`, `mockito-kotlin`, or any Mockito variant
- ❌ **No other mocking libraries** – No PowerMock, no EasyMock, no manual test doubles (unless Fake repositories)
- ✅ **Use Fake repositories** from `com.antcashmanager.testutil.Fake*` package for data layer isolation when mocking is insufficient
- ❌ Never import real database libraries (Room, DataStore) in unit tests – use Fakes or MockK

### Unit Test Rules

**MockK Usage Pattern:**
```kotlin
// ✅ CORRECT - MockK only
private val repo = mockk<TransactionRepository>()
coEvery { repo.getTransaction(any()) } returns Result.success(mockTransaction)
coVerify { repo.getTransaction(1L) }

// ❌ WRONG - Any other mocking library is forbidden
@Mock private lateinit var repo: TransactionRepository  // Mockito - FORBIDDEN
```

**Roboelectric Strategy: Instrumentation Tests Only**
- ❌ **DO NOT use in unit tests** (`src/test`) – Use Compose UI Test v2 instead (simpler, faster)
- ✅ **CAN use in instrumentation tests** (`src/androidTest`) – When simulating Android Framework without real device
  - Faster than device emulator (seconds vs minutes)
  - Good for integration testing data layer with Android framework
  - Use `@RunWith(RobolectricTestRunner::class)` with Compose UI Test v2 for framework simulation
  - Example: Repository tests with Room/DataStore, navigation flows, settings integration
- ✅ **COMPLEMENT with real instrumentation tests** (`src/androidTest` on device/emulator) – For critical user flows
  - Test actual user interactions (tap, swipe, real touch handling)
  - GPU rendering validation
  - Performance profiling on real hardware
  - Accessibility testing (screen reader, contrast)
  - Example: "Add Transaction" full flow, "Navigation" flow, "Search" flow

**Test Data:**
- ✅ Use `TestDataBuilder` pattern for creating test data (`shared/src/commonTest/testutil/TestDataBuilder.kt`)
- ✅ Create builder-style APIs: `testTransaction { title = "Lunch"; amount = -50.0 }`
- ❌ Never hardcode complex test data directly in test methods

**Compose UI Testing Strategy:**

**Unit Tests** (`src/test`):
- ✅ **Use `androidx.compose.ui.test.junit4.v2.createComposeRule()`** (v2 API with StandardTestDispatcher)
- ❌ **DO NOT use Roboelectric** – Use Compose UI Test v2 assertions only (simpler, faster, sufficient for unit testing)
  - Compose UI Test v2 handles state verification, callback testing, layout assertions
  - No Android Framework needed for unit test component verification
  - Fast feedback (milliseconds) without Roboelectric overhead
- ❌ **NEVER use deprecated v1** (`androidx.compose.ui.test.junit4.createComposeRule()`)
- ❌ Never use Roboelectric for complex touch interactions (drag, swipe, multi-touch)

**Instrumentation Tests** (`src/androidTest`, real device/emulator):
- ✅ Use `createAndroidComposeRule<ComponentActivity>()` for full interaction testing
- ✅ Test actual user interactions: tap, drag, swipe, long-press
- ✅ Verify visual rendering, animations, color/layout correctness
- ✅ Test integration with Android framework (navigation, system bars, dialogs)
- ✅ Primary use case: end-to-end user flows ("Add Transaction flow", "Navigation", "Search")

**Roboelectric-based Instrumentation Tests** (`src/androidTest` with `@RunWith(RobolectricTestRunner::class)`):
- ✅ Fast alternative to real device when full interaction testing not needed
- ✅ Test Android Framework integration (SharedPreferences, DataStore, Bundle, Resources)
- ✅ Test navigation flows, screen transitions
- ⚠️ NOT suitable for: Touch events, gestures, GPU rendering, performance profiling, sensors

**Test Naming:**
- ✅ Pattern: `method_shouldExpectedBehavior_whenCondition` (no backticks)
- Example: `insertTransaction_shouldPersistAndRetrieve_whenValidDataProvided`

### Dispatcher Decision Matrix

When to use which dispatcher in tests:

| Scenario | Dispatcher | Use Case | Example |
|----------|-----------|----------|---------|
| **Default (eager execution)** | `UnconfinedTestDispatcher` | Flow collectors, background jobs, most ViewModel tests | StateFlow emission before assertions |
| **Deferred execution** | `StandardTestDispatcher` | Debounce, delay, retry with backoff | Testing 500ms debounce operator |
| **Override in test** | Change in test method | Special timing needs | `testDispatcher = StandardTestDispatcher()` + `advanceUntilIdle()` |
| **Production code** | `Dispatchers.Default` | UseCase async work | Default dispatcher in UseCase constructor |

**Quick Decision**: Use `BaseUnitTest` (UnconfinedTestDispatcher) by default. Only override if test needs `.advanceUntilIdle()`.

### Testing Strategy Quick Reference

| Test Type | Source Set | Base Class | Framework | Device? |
|---|---|---|---|---|
| **ViewModel** | `androidApp/src/test/` | `BaseUnitTest` | JUnit 4 + MockK + Compose v2 | ❌ |
| **Domain UseCase** | `shared/src/commonTest/` | `BaseUseCaseTest` | JUnit 4 + MockK | ❌ |
| **Repository** | `shared/src/androidHostTest/` | — | JUnit 4 + MockK | ❌ |
| **UI Integration** | `androidApp/src/androidTest/` | — | AndroidJUnit4 + Compose | ✅ or Roboelectric |
| **Full E2E** | `androidApp/src/androidTest/` | — | AndroidJUnit4 | ✅ Real device |

**Key Points**:
- ✅ Unit test: MockK only, Compose UI Test v2 (NO Roboelectric)
- ✅ Instrumentation: Roboelectric OK for simulation OR real device for E2E
- ✅ Naming: `method_shouldExpectedBehavior_whenCondition`

### Exception Handling Quick Reference

| Layer | Exception Type | Location | Pattern |
|---|---|---|---|
| **Domain** | Custom sealed class | `domain/exception/` | Define & throw from UseCase |
| **Data** | Standard exceptions | Repository | Catch, map to domain exception |
| **Presentation** | Never thrown | ViewModel | Consume Result only |
| **Coroutines** | `CancellationException` | Any async | **ALWAYS re-throw** |

**Critical Pattern**:
```kotlin
// ✅ CORRECT - Re-throw CancellationException
try { repository.operation() }
catch (e: Exception) {
    if (e is CancellationException) throw e // RE-THROW!
    throw DomainException.Failed(e)
}

// ❌ WRONG - Swallows CancellationException
catch (e: Exception) { throw DomainException.Failed(e) }
```

**BaseUnitTest Utilities** (`androidApp/src/test/kotlin/com/antcashmanager/android/BaseUnitTest.kt`):
- **Always extend `BaseUnitTest`** in `androidApp/src/test/kotlin` for ViewModel and Android host-side tests
- `BaseUnitTest` automatically provides:
  - `testDispatcher: TestDispatcher` – pre-configured as `Dispatchers.Main` for the test scope
  - `runUnitTest { ... }` – shorthand for `runTest(testDispatcher) { ... }` (wraps coroutine with test dispatcher)
  - `runViewModelTest { ... }` – semantic alias of `runUnitTest` for ViewModel-specific tests
  - `launchInBackground { ... }` – launches coroutines in `backgroundScope` for Flow collectors, LiveData observers, or long-running background jobs that need to complete before test ends
  - `advanceUntilIdle()` – available inside test block to advance dispatcher until all pending coroutines complete
- **Do NOT manually set up:**
  - ❌ `Dispatchers.setMain()` / `Dispatchers.resetMain()` – `BaseUnitTest` handles this in `setUp()`/`tearDown()`
  - ❌ `StandardTestDispatcher()` – already created and assigned as Main dispatcher
  - ❌ `runTest()` – use `runViewModelTest()` instead for consistency

**Example:**
```kotlin
class MyViewModelTest : BaseUnitTest() {
    private val repo = mockk<Repository>()
    private lateinit var viewModel: MyViewModel

    @Before
    fun setup() {
        viewModel = MyViewModel(repo)
    }

    @Test
    fun loadData_shouldUpdateState() = runViewModelTest {
        coEvery { repo.getData() } returns Result.success(listOf("item1"))
        
        viewModel.loadData()
        advanceUntilIdle()
        
        assertEquals(listOf("item1"), viewModel.state.value)
    }
}
```

### Instrumentation Test Rules (Hybrid Strategy)

**For REAL Device/Emulator Testing** (`src/androidTest` on actual Android environment):
- ✅ Use `@RunWith(AndroidJUnit4::class)` for tests running on device/emulator
- ✅ Use `createAndroidComposeRule<ComponentActivity>()` for full Compose UI interaction testing
- ✅ Test real database operations, file I/O, GPS, camera, sensors
- ✅ Test actual touch events, gestures (swipe, long-press, drag)
- ✅ Test performance on real hardware (frame rate, memory, battery impact)
- ✅ Focus on critical user flows: "Add Transaction" → "Save" → "Verify in List", "Navigation", "Search/Filter", "Settings Changes"
- ✅ Use for accessibility testing (screen reader, font scaling, contrast)

**For Framework Simulation Testing** (Roboelectric, `src/androidTest` or `src/test`):
- ✅ Use `@RunWith(RobolectricTestRunner::class)` for Android Framework simulation without device
- ✅ Faster execution for integration testing (~seconds vs minutes)
- ✅ Use `createComposeRule()` or `createAndroidComposeRule<ComponentActivity>()` for Compose UI testing
- ✅ Good for data layer integration + Android Framework operations (SharedPreferences, DataStore, Bundle)
- ❌ Do NOT use for testing touch events, sensors, or performance on real hardware
- ⚠️ Remember: Roboelectric simulates SDK 34-35, app compileSdk is 37 (some SDK 37 features may not be fully simulated)

### Forbidden Imports in Tests

**STRICTLY FORBIDDEN - ZERO TOLERANCE:**
- ❌ `org.mockito.*` – use MockK (`io.mockk`) ONLY
- ❌ `com.nhaarman.mockitokotlin2.*` – use MockK ONLY
- ❌ `org.powermock.*` – never use PowerMock
- ❌ `org.easymock.*` – never use EasyMock
- ❌ Any mocking library except MockK – zero exceptions

**Best Practices:**
- ❌ Real Room database/DataStore implementations in unit tests – use Fakes
- ❌ Direct `Context`, `SharedPreferences`, or `File` I/O in unit tests
- ❌ `org.robolectric.*` in unit test files – move to instrumentation tests if needed
- ❌ `android.` imports in domain layer tests (`shared/src/commonTest/`)

---

## Quick Implementation Checklist

Use this checklist when implementing a new feature. For detailed guidance, refer to relevant sections above.

### 1. Feature Structure Setup
- [ ] Create UseCase in `shared/commonMain/domain/usecase/<feature>/`
- [ ] Create ViewModel in `androidApp/ui/screen/<feature>/`
- [ ] Create State data class in `androidApp/ui/screen/<feature>/<Feature>State.kt`
- [ ] Create Screen composable in `androidApp/ui/screen/<feature>/<Feature>Screen.kt`
- [ ] Add UI components to `androidApp/ui/components/` (reusable) or `view/` sub-package (feature-specific)

### 2. Clean Architecture Verification
- [ ] UseCase: NO dependency on ViewModel or Presentation layer
- [ ] ViewModel: Depends ONLY on UseCase(s), NOT on Repository directly
- [ ] Domain layer: Pure Kotlin ONLY, NO Android imports
- [ ] No reversed dependencies (Data → Domain, Presentation → Domain)

### 3. UseCase Implementation
- [ ] Extend appropriate base class (`UseCase<P,R>`, `NoParamsUseCase<R>`, `ObservableUseCase<P,R>`, or `NoParamsObservableUseCase<R>`)
- [ ] Accept `CoroutineDispatcher` parameter (default: `Dispatchers.Default`)
- [ ] Implement `execute()` method ONLY (NOT `invoke()`)
- [ ] Return value directly; base class wraps in `Result<T>`
- [ ] Add KDoc documentation
- [ ] Keep under 250 lines

### 4. ViewModel Implementation
- [ ] Expose `StateFlow` (public)
- [ ] Keep `MutableStateFlow` private
- [ ] Accept UseCase instances as constructor parameters
- [ ] Consume `Result<T>` with `.onSuccess { }` and `.onFailure { }`
- [ ] Use Kermit for logging (never `Log.d()` or `println()`)
- [ ] Keep under 300 lines
- [ ] Add `viewModelScope.cancel()` cleanup in tests (if `@After` method needed)

### 5. State & Screen
- [ ] `<Feature>State` data class stays in dedicated file (`<Feature>State.kt`)
- [ ] NO typealias or aliases for state
- [ ] Screen composable: NO business logic, only UI composition
- [ ] Add at least 2 `@Preview` functions (light mode + dark mode)
- [ ] Keep Screen under 400 lines
- [ ] Reuse components from `androidApp/ui/components/` before creating new ones

### 6. Localization
- [ ] ALL user-facing strings in `strings.xml` (NOT hardcoded)
- [ ] Check for existing strings: `grep -r "your_key" androidApp/src/main/res/values*/`
- [ ] Add translations to ALL 5 locale files:
  - `values/strings.xml` (English)
  - `values-it/strings.xml` (Italian)
  - `values-fr/strings.xml` (French)
  - `values-de/strings.xml` (German)
  - `values-es/strings.xml` (Spanish)
- [ ] Use `stringResource(R.string.key)` in Compose

### 7. Testing
- [ ] Create ViewModel test in `androidApp/src/test/kotlin/com/antcashmanager/android/ui/screen/<feature>/<Feature>ViewModelTest.kt`
- [ ] Extend `BaseUnitTest`
- [ ] Use test naming: `method_shouldExpectedBehavior_whenCondition` (no backticks)
- [ ] Mock UseCase with MockK (`mockk()`, `coEvery`, `coVerify`)
- [ ] Test both happy path AND failure scenarios
- [ ] Create UseCase test in `shared/src/commonTest/`
- [ ] Use `TestDataBuilder` for test data

### 8. Pre-Commit Verification
- [ ] Imports: all used, no unused imports
- [ ] Package name: matches directory structure
- [ ] Build succeeds: `./gradlew build`
- [ ] Tests pass: `./gradlew test`
- [ ] No hardcoded strings/colors/fonts
- [ ] No `runBlocking()` outside tests
- [ ] Code under line limits (UseCase 250, ViewModel 300, Screen 400, State 100)
- [ ] Material Design compliance (colors from MaterialTheme, proper spacing)

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

