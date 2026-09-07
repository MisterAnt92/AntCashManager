# AGENTS.md — AntCashManager

Single source of rules for AI coding agents. Specialized agents in `.github/agents/` add layer-specific templates only; they never repeat what is here.

---

## 1. Git policy (zero exceptions)

- **Never** run `git add`, `git commit`, `git push` without explicit approval **in the current message**.
- Valid approval: `procedi`, `vai`, `sì`, `yes`, `commit`, `push`, `go ahead`. **Not** valid: `ok`, `looks good`, silence, approval from an earlier message.
- Before asking: show `git diff --stat`, list files, propose the commit message, report test/build result.
- Complete the whole task first; never commit partial work. Never commit to `main`.

## 2. Workflow — step by step (mandatory)

1. **Analyze** — `grep`/`rg` first, then `Read` only the files you will touch. Reuse existing code before writing new.
2. **Plan** — list the files to change, one line each. For multi-step tasks, state the steps and wait for confirmation.
3. **Implement one file at a time.** After each file: check imports and `package` match the directory.
4. **Verify light** — re-read only the edited region, not the whole file. Compile **once per logical group** with `compileFullDebugKotlin`, not after every edit.
5. **Confirm** — summarize what changed and stop; wait before the next step of a multi-step task.

## 3. Token discipline

- Do not re-read a file you just wrote or edited.
- Gradle output: always `2>&1 | tail -30`. Never paste full logs.
- Run targeted tests (`--tests "*ClassName*"`), not the whole suite, unless asked.
- In replies show the relevant diff, never whole files.
- Do not spawn subagents, write summary/README/report files, or add docs unless asked.
- Do not narrate what you are about to do; do it, then report the outcome.

## 4. Architecture

```
Presentation (androidApp)  →  Domain (shared/commonMain)  →  Data (shared/androidMain)
```
- Package-by-feature. Domain is pure Kotlin: no Android imports in `commonMain`.
- **KMP scope: Android only.** iOS is out of scope — do not create `iosMain`, iOS targets, or `expect/actual` for iOS. Keep `commonMain` platform-free so iOS can be added later.
- Reference features: `ui/screen/home`, `ui/screen/categories`, `ui/screen/settings`.

## 5. Commands

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64      # required, build fails without it
./gradlew :androidApp:compileFullDebugKotlin 2>&1 | tail -30      # fast check
./gradlew :androidApp:testFullDebugUnitTest --tests "*HomeViewModel*" 2>&1 | tail -30
./gradlew :shared:testAndroidHostTest 2>&1 | tail -30
./gradlew :androidApp:assembleDebug 2>&1 | tail -30               # end of task only
```
Flavors: `full` / `lite` (dimension `variant`). Use `Full` in task names.

## 6. UseCase (domain)

Base classes in `shared/commonMain/.../domain/usecase/base/`:

| Params | Flow | Base class | Implement |
|---|---|---|---|
| yes | no | `UseCase<P, R>` | `suspend fun execute(params: P): R` |
| no | no | `NoParamsUseCase<R>` | `suspend fun execute(): R` |
| yes | yes | `ObservableUseCase<P, R>` | `fun execute(params: P): Flow<R>` |
| no | yes | `NoParamsObservableUseCase<R>` | `fun execute(): Flow<R>` |

- `execute()` returns the **raw** `R` and may throw. `invoke()` (final) wraps it in `Result<R>`, runs on the injected dispatcher, preserves `CancellationException`, logs via Kermit. **Never** return `Result` from `execute()` (double wrap), never override `invoke()`.
- Constructor: `dispatcher: CoroutineDispatcher = Dispatchers.Default` (no `Dispatchers.IO` in `commonMain`).
- Domain exceptions: sealed classes in `domain/exception/` only.
- Settings get/set: use generic `GetSettingUseCase<T>` / `SetSettingUseCase<T>` registered in DI with a lambda — do not create per-setting classes.
- Max 250 lines, KDoc on the class.

## 7. ViewModel — UDF (presentation)

All ViewModels extend `ui/base/BaseViewModel<E>` (`E` = `<Feature>Event` sealed class, or `Nothing`).

- **Single public entry point: `override fun onEvent(event: E)`** with a `when` that delegates to **private** methods. No other public mutators.
- State: one `private val _state = MutableStateFlow(<Feature>State())`, exposed as `val state: StateFlow<...>`; update with `_state.update { it.copy(...) }`. Reactive sources: `combine(...)` + `stateIn(viewModelScope, WhileSubscribed(5_000), default)`.
- Results: `useCase(p).handleError { err -> _state.update { it.copy(errorState = err) } }` (returns value or `null`). For `Flow<Result<R>>`: `.onSuccess { } .onFailure { if (it is CancellationException) throw it; logError(...) }`.
- Logging: `logDebug/logInfo/logWarn/logError` from the base (tag is automatic). Never `Log`, `println`, or a manual `TAG`.
- Dependencies: business data through **UseCases**. **Documented exception:** `SettingsRepository` may be injected directly **only** to read preference flows / write preferences in `viewModelScope.launch`. No other repository in a ViewModel. No `Context`.
- `activeJob?.cancel()` for restartable operations. Max 300 lines. State class ≤ 100 lines, in `<Feature>State.kt`, no `typealias`.

## 8. Screen (Compose)

```kotlin
@Composable
fun FeatureScreen(navController: NavController, modifier: Modifier = Modifier) {
    val viewModel: FeatureViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    FeatureContent(state = state, onEvent = viewModel::onEvent, navController = navController, modifier = modifier)
}
```
- UI reads `state`, emits `onEvent(...)`. Zero repository access, zero business logic.
- Navigation only via `navigation/NavigationExtensions.kt` (`navigateToHome()`, `navigateToAddTransaction()`, `safePopBackStack()`, …) and `AppRoute` — never string literals.
- Root-only exception: `AntCashManagerNavHost` may inject `SettingsRepository` via Koin for global display prefs.
- Colors/typography from `MaterialTheme`. Only allowed semantic colors outside it: `IncomeGreen`/`ExpenseRed` (`ui/theme/Color.kt`). Spacing 8/16/24 dp.
- Reuse `ui/components/` (inventory in `agent-compose-ui.agent.md`). `@Preview` light+dark for components in `components/` and `view/`; root Screens have no previews.

## 9. Feature layout & limits

```
ui/screen/<feature>/
  <Feature>Screen.kt   ≤ 400 lines     <Feature>ViewModel.kt  ≤ 300
  <Feature>State.kt    ≤ 100           <Feature>Event.kt      (sealed)
  <Feature>Constant.kt (shared consts) model/  view/ (sub-composables)
```

## 10. DI (Koin) — `androidApp/.../di/AppModule.kt`

`dataModule` (Room, repositories, services) · `useCaseModule` (`factory { }`) · `presentationModule` (`viewModel { }` / `viewModelOf(::X)`; parameterized: `viewModel { (id: Long?) -> ... }`). Register every new UseCase and ViewModel.

## 11. Localization — 13 locales

`values/` (en) + `values-{it,fr,de,es,hi,ja,ko,pl,ru,uk,zh,zh-rTW}/strings.xml`. Every user string via `stringResource(R.string.key)`.

1. `grep -rn "candidate_key\|Candidate text" androidApp/src/main/res/values*/` — reuse if it exists; never create semantic duplicates.
2. **Untranslatable** value (proper noun, symbol, format pattern): add **once** to `values/untranslable.xml` with `translatable="false"`. Never copy it into the locale files (duplicate resource → merge error).
3. **Translatable** text: add a translated value to **all 13** `strings.xml`.
4. Key naming: `<screen>_<component>_<description>`.

## 12. Testing

| Scope | Source set | Base class / helpers |
|---|---|---|
| ViewModel, Android host | `androidApp/src/test/kotlin` | `BaseUnitTest` → `runViewModelTest { }`, `testDispatcher`, `advanceUntilIdle()` |
| Compose component | `androidApp/src/test/kotlin` | `BaseComposeUnitTest` → `composeTestRule` (`junit4.v2`), extends `BaseUnitTest` |
| UseCase / domain | `shared/src/commonTest/kotlin` | `BaseUseCaseTest` → `runUnitTest { }`; `TestDataBuilder`; `Fake*Repository` in `testutil/` |
| Repository / data | `shared/src/androidHostTest/kotlin` | JUnit4 + MockK |
| Instrumentation | `androidApp/src/androidTest/kotlin` | `AndroidJUnit4`, `createAndroidComposeRule`; Robolectric allowed here only |

- **MockK only** (`mockk`, `every`, `coEvery`, `coVerify`). Mockito/PowerMock/EasyMock forbidden. Fakes from `testutil/` when stateful/Flow behaviour is clearer.
- Never `Dispatchers.setMain`, `StandardTestDispatcher()`, or `runTest` by hand in `androidApp` tests — the base classes do it.
- No Robolectric, Room, DataStore, `Context`, file I/O in `src/test`.
- Naming: `method_shouldExpectedBehavior_whenCondition`, **no backticks**. Keep the original intent when updating a test.
- Test ViewModels through `viewModel.onEvent(Event.X)` + `advanceUntilIdle()`, then assert `state.value`.

## 13. Analytics & protected files

- Log only events whitelisted in `analytics/`; never user content (notes, amounts, payees).
- Never modify: `build/`, `.gradle/`, `.idea/`, `*.jks`, `google-services.json`, `local.properties`, `secrets.properties`.
