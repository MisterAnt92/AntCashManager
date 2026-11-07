---
name: antcashmanager-android-kmp
description: >
  Guida per sviluppare su AntCashManager, una finanza-personale Android KMP app in Kotlin.
  Usa questa skill per ogni task su questo progetto: creare feature, aggiungere UseCase,
  ViewModel, schermate Compose, test, DI Koin, stringhe i18n, o fix bug.
  TRIGGERA sempre quando l'utente parla di AntCashManager, aggiunge transazioni/categorie,
  implementa schermate Home/Charts/Settings/Transactions, o menziona feature come
  Meal Vouchers, backup, ricevute, grafici, importazione CSV.
---

# AntCashManager — Android KMP Dev Guide

Progetto: app finanza personale Android (KMP), Kotlin + Compose + Koin + Room.
Root: `/opt/src/GIT/app/AntCashManager/`

## Architettura

```
Presentation (androidApp)  →  Domain (shared/commonMain)  →  Data (shared/androidMain)
```

- **Package-by-feature**. KMP scope: Android only. `commonMain` è platform-free.
- Feature reference: `ui/screen/home`, `ui/screen/categories`, `ui/screen/settings`.
- Flavors: `full` / `lite` (dimensione `variant`). Usa `Full` nei task Gradle.

---

## UseCase (domain — `shared/commonMain`)

Quattro classi base in `domain/usecase/base/`:

| Params | Flow | Classe base | Implementa |
|---|---|---|---|
| sì | no | `UseCase<P, R>` | `suspend fun execute(params: P): R` |
| no | no | `NoParamsUseCase<R>` | `suspend fun execute(): R` |
| sì | sì | `ObservableUseCase<P, R>` | `fun execute(params: P): Flow<R>` |
| no | sì | `NoParamsObservableUseCase<R>` | `fun execute(): Flow<R>` |

**Regole critiche:**
- `execute()` restituisce il valore grezzo e può lanciare. Non restituire mai `Result<R>` da `execute()` (double-wrap).
- `invoke()` è `final`: wrappa in `Result<R>`, gira sul dispatcher, preserva `CancellationException`. Non sovrascrivere `invoke()`.
- Dispatcher nel costruttore: `Dispatchers.Default` (non `Dispatchers.IO` in `commonMain`).
- Eccezioni dominio: sealed class in `domain/exception/`.
- Settings get/set: usa `GetSettingUseCase<T>` / `SetSettingUseCase<T>` generici con lambda DI — non creare classi per-setting.
- Limite: 250 righe, KDoc sulla classe.

```kotlin
// Esempio corretto
class GetTransactionsUseCase(
    private val repository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsObservableUseCase<List<Transaction>>(dispatcher) {
    override fun execute(): Flow<List<Transaction>> = repository.getTransactions()
}
```

---

## ViewModel — UDF (`androidApp`)

Tutti i ViewModel estendono `BaseViewModel<E>` dove `E` = sealed class `<Feature>Event` (o `Nothing`).

### Pattern obbligatorio

```kotlin
class FeatureViewModel(
    private val getSomethingUseCase: GetSomethingUseCase,
    private val settingsRepository: SettingsRepository,  // eccezione documentata
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : BaseViewModel<FeatureEvent>(dispatcher) {

    private val _state = MutableStateFlow(FeatureState())
    val state: StateFlow<FeatureState> = _state.asStateFlow()

    init {
        loadData()
    }

    override fun onEvent(event: FeatureEvent) = when (event) {
        is FeatureEvent.DoSomething -> doSomething(event.param)
        is FeatureEvent.Reset       -> reset()
    }

    private fun doSomething(param: String) {
        viewModelScope.launch(dispatcher) {
            getSomethingUseCase(param).handleError { err ->
                _state.update { it.copy(errorState = err) }
            }?.let { result ->
                _state.update { it.copy(data = result) }
            }
        }
    }
}
```

**Regole:**
- **Unico entry point pubblico: `onEvent()`** con `when` che delega a metodi privati.
- Stato: un solo `private val _state = MutableStateFlow(FeatureState())`.
- Aggiorna con `_state.update { it.copy(...) }`.
- Flow reattivi: `combine(...).stateIn(viewModelScope, WhileSubscribed(5_000), default)`.
- Result: `useCase(p).handleError { ... }` (restituisce valore o `null`).
- Logging: `logDebug/logInfo/logWarn/logError` (dal base). Mai `Log`, `println`, `TAG` manuale.
- Eccezione documentata: `SettingsRepository` si può iniettare direttamente solo per leggere preference Flow o scrivere in `viewModelScope.launch`. Nessun altro repository nel ViewModel.
- `activeJob?.cancel()` per operazioni riavviabili.
- Limiti: ViewModel ≤ 300 righe, State ≤ 100 righe.

---

## State & Event

```
ui/screen/<feature>/
  <Feature>Screen.kt    ≤ 400 righe
  <Feature>State.kt     ≤ 100 righe   (data class, in file proprio)
  <Feature>ViewModel.kt ≤ 300 righe
  event/
    <Feature>Event.kt   (sealed interface)
  model/                (sub-models UI)
  view/                 (composable interni)
```

- State: `data class FeatureState(...)`, niente `typealias`.
- Event: `sealed interface FeatureEvent { data class X(...): FeatureEvent; data object Y: FeatureEvent }`.

---

## Screen (Compose)

```kotlin
@Composable
fun FeatureScreen(navController: NavController, modifier: Modifier = Modifier) {
    val viewModel: FeatureViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    FeatureContent(state = state, onEvent = viewModel::onEvent,
                   navController = navController, modifier = modifier)
}
```

- UI legge `state`, emette `onEvent(...)`. Zero accesso a repository, zero logica di business.
- Navigazione solo via `navigation/NavigationExtensions.kt` (`navigateToHome()`, ecc.) e `AppRoute`.
- Colori/tipografia da `MaterialTheme`. Colori semantici extra permessi: `IncomeGreen`/`ExpenseRed`.
- Spaziatura: 8/16/24 dp.
- Riusa `ui/components/` prima di creare nuovi composable.
- `@Preview` light+dark nei componenti; le Screen root non hanno preview.

---

## DI — Koin (`androidApp/.../di/AppModule.kt`)

Tre moduli:
- `dataModule`: Room, repository, service.
- `useCaseModule`: `factory { }` per ogni UseCase.
- `presentationModule`: `viewModel { }` o `viewModelOf(::X)`. Parametrizzato: `viewModel { (id: Long?) -> ... }`.

Registra sempre ogni nuovo UseCase e ViewModel.

```kotlin
// In useCaseModule:
factory { GetTransactionsUseCase(get()) }

// In presentationModule:
viewModelOf(::HomeViewModel)
viewModel { (id: Long?) -> AddTransactionViewModel(get(), get(), get(), id) }
```

---

## Testing

| Scope | Source set | Base / helper |
|---|---|---|
| ViewModel, Android host | `androidApp/src/test/kotlin` | `BaseUnitTest` → `runViewModelTest {}`, `testDispatcher` |
| Compose component | `androidApp/src/test/kotlin` | `BaseComposeUnitTest` → `composeTestRule` |
| UseCase / domain | `shared/src/commonTest/kotlin` | `BaseUseCaseTest` → `runUnitTest {}`, `TestDataBuilder` |
| Repository / data | `shared/src/androidHostTest/kotlin` | JUnit4 + MockK |

**Regole:**
- **Solo MockK** (`mockk`, `every`, `coEvery`, `coVerify`). Mockito/PowerMock vietati.
- Usa Fake da `testutil/` quando serve comportamento stateful o Flow.
- Mai `Dispatchers.setMain`, `StandardTestDispatcher()`, `runTest` a mano in `androidApp/src/test`.
- Naming: `method_shouldExpectedBehavior_whenCondition` (no backtick).
- Testa ViewModel tramite `viewModel.onEvent(Event.X)` + `advanceUntilIdle()`, poi assertisci su `state.value`.
- Niente Robolectric, Room, DataStore, `Context`, file I/O in `src/test`.

```kotlin
@Test
fun loadTransactions_shouldPopulateState_whenRepositoryReturnsData() = runViewModelTest {
    viewModel.onEvent(HomeEvent.SelectPreset(0))
    advanceUntilIdle()
    assertEquals(expected, viewModel.state.value.transactions)
}
```

---

## i18n — 13 locale

Directory: `values/` (en) + `values-{it,fr,de,es,hi,ja,ko,pl,ru,uk,zh,zh-rTW}/strings.xml`.

1. `grep -rn "chiave\|testo" androidApp/src/main/res/values*/` — riusa se esiste, mai duplicati semantici.
2. **Intraducibile** (nome proprio, simbolo, pattern): aggiungi **una volta** a `values/untranslable.xml` con `translatable="false"`. Non copiare nei file locale.
3. **Traducibile**: aggiungi una traduzione in **tutti e 13** i `strings.xml`.
4. Naming: `<screen>_<componente>_<descrizione>`.
5. Usa sempre `stringResource(R.string.key)`, mai stringhe letterali nella UI.

---

## Build & Comandi

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64   # sempre, build fallisce senza

# Check veloce (dopo ogni modifica)
./gradlew :androidApp:compileFullDebugKotlin 2>&1 | tail -30

# Test mirati
./gradlew :androidApp:testFullDebugUnitTest --tests "*HomeViewModel*" 2>&1 | tail -30
./gradlew :shared:testAndroidHostTest 2>&1 | tail -30

# Build finale (solo fine task)
./gradlew :androidApp:assembleDebug 2>&1 | tail -30
```

---

## Git policy (zero eccezioni)

- **Mai** `git add/commit/push` senza approvazione esplicita nel messaggio corrente.
- Approvazioni valide: `procedi`, `vai`, `sì`, `yes`, `commit`, `push`, `go ahead`.
- Prima di chiedere: mostra `git diff --stat`, lista file, proponi commit message, riporta risultato build/test.
- Mai committare su `main`. Mai committare lavoro parziale.

---

## Checklist per nuova feature

1. **Domain**: crea UseCase in `shared/commonMain/.../domain/usecase/<feature>/` → testa in `commonTest`.
2. **State + Event**: crea `FeatureState.kt` e `event/FeatureEvent.kt` in `ui/screen/<feature>/`.
3. **ViewModel**: crea `FeatureViewModel.kt`, estende `BaseViewModel<FeatureEvent>`.
4. **Screen**: crea `FeatureScreen.kt` seguendo il pattern in §Screen.
5. **DI**: registra UseCase in `useCaseModule` e ViewModel in `presentationModule`.
6. **i18n**: aggiungi tutte le stringhe in tutti e 13 i file.
7. **Test**: `FeatureViewModelTest` in `androidApp/src/test`, UseCase test in `commonTest`.
8. **Build**: `compileFullDebugKotlin` prima di dichiarare fatto.
