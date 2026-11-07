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

- **Package-by-feature**. KMP scope: Android only. **Nessun import Android in `commonMain`** (domain è pure Kotlin).
- **iOS fuori scope**: non creare `iosMain`, target iOS, né `expect/actual` per iOS.
- Feature reference: `ui/screen/home`, `ui/screen/categories`, `ui/screen/settings`.
- Flavors: `full` / `lite` (dimensione `variant`). Usa `Full` nei task Gradle.

---

## Workflow (obbligatorio — AGENTS.md §2)

1. **grep/rg prima di Read** — leggi solo i file che toccherai.
2. **Multi-step**: elenca i file da cambiare, attendi conferma prima di iniziare.
3. **Un file alla volta.** Dopo ogni file: verifica imports e `package`.
4. **Verifica light** — rileggi solo la regione modificata, non l'intero file.
5. **Compila una volta per gruppo logico** con `compileFullDebugKotlin`.
6. **Fermati** — riassumi cosa è cambiato; non passare al passo successivo senza ok.

---

## Token discipline (AGENTS.md §3)

- Non rileggere un file appena scritto o modificato.
- Gradle output: sempre `2>&1 | tail -30`.
- Test mirati (`--tests "*ClassName*"`), non tutta la suite.
- Nelle risposte: solo il diff rilevante, mai il file intero.
- Non spawnare subagenti, non scrivere doc/README/report se non richiesto.
- Non narrare cosa si sta per fare; farlo, poi riportare l'esito.

---

## UseCase (domain — `shared/commonMain`)

Quattro classi base in `domain/usecase/base/`:

| Params | Flow | Classe base | Implementa |
|---|---|---|---|
| sì | no | `UseCase<P, R>` | `suspend fun execute(params: P): R` |
| no | no | `NoParamsUseCase<R>` | `suspend fun execute(): R` |
| sì | sì | `ObservableUseCase<P, R>` | `fun execute(params: P): Flow<R>` |
| no | sì | `NoParamsObservableUseCase<R>` | `fun execute(params: Unit): Flow<R>` |

**Regole critiche:**
- `execute()` restituisce il valore grezzo e può lanciare. Non restituire mai `Result<R>` da `execute()` (double-wrap).
- **Non sovrascrivere `invoke()`**: wrappa in `Result<R>`, gira sul dispatcher, preserva `CancellationException`. La restrizione è convenzionale (KDoc), non un modificatore `final` del compilatore.
- Dispatcher nel costruttore: `Dispatchers.Default` (non `Dispatchers.IO` in `commonMain`).
- Eccezioni dominio: sealed class in `domain/exception/`.
- Settings get/set: usa `GetSettingUseCase<T>` / `SetSettingUseCase<T>` generici con lambda DI — non creare classi per-setting. Le classi per-setting esistenti sono `@Deprecated` e in migrazione verso il pattern generico.
- Limite: 250 righe, KDoc sulla classe.

```kotlin
// Esempio corretto
class GetTransactionsUseCase(
    private val repository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsObservableUseCase<List<Transaction>>(dispatcher) {
    override fun execute(params: Unit): Flow<List<Transaction>> = repository.getTransactions()
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
- **Mai `Context`** nel ViewModel.
- Eccezione documentata: `SettingsRepository` si può iniettare direttamente solo per leggere preference Flow o scrivere in `viewModelScope.launch`. Nessun altro repository nel ViewModel.
- Per `Flow<Result<R>>` reattivi: `.onSuccess { }.onFailure { if (it is CancellationException) throw it; logError(...) }`.
- `activeJob?.cancel()` per operazioni riavviabili.
- Limiti: ViewModel ≤ 300 righe, State ≤ 100 righe.

---

## State & Event

```
ui/screen/<feature>/
  <Feature>Screen.kt    ≤ 400 righe
  <Feature>State.kt     ≤ 100 righe   (data class, in file proprio)
  <Feature>ViewModel.kt ≤ 300 righe
  <Feature>Constant.kt (shared consts)
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
- Riusa `ui/components/` prima di creare nuovi composable. Per l'inventario componenti consulta `.github/agents/agent-compose-ui.agent.md`.
- `@Preview` light+dark per composable in `components/` **e in `view/`**; le Screen root non hanno preview.

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

// In presentationModule (ViewModel senza wiring aggiuntivo):
viewModelOf(::ThemeViewModel)
// ViewModel con analytics/tracker (argomenti named):
viewModel { HomeViewModel(get(), get(), analyticsManager = get()) }
// ViewModel parametrizzati:
viewModel { (id: Long?) -> AddTransactionViewModel(get(), get(), get(), id) }
```

---

## Testing

| Scope | Source set | Base / helper |
|---|---|---|
| ViewModel, Android host | `androidApp/src/test/kotlin` | `BaseUnitTest` → `runViewModelTest {}`, `testDispatcher` |
| Compose component | `androidApp/src/test/kotlin` | `BaseComposeUnitTest` (extends `BaseUnitTest`) → `composeTestRule` |
| UseCase / domain | `shared/src/commonTest/kotlin` | `BaseUseCaseTest` → `runUnitTest {}`, `TestDataBuilder` |
| Repository / data | `shared/src/androidHostTest/kotlin` | JUnit4 + MockK |
| Instrumentation | `androidApp/src/androidTest/kotlin` | `AndroidJUnit4`, `createAndroidComposeRule`; **Robolectric solo qui** |

**Regole:**
- **Solo MockK** (`mockk`, `every`, `coEvery`, `coVerify`). Mockito/PowerMock vietati.
- Usa `Fake*Repository` da `testutil/` quando serve comportamento stateful o Flow.
- Mai `Dispatchers.setMain`, `StandardTestDispatcher()`, `runTest` a mano in `androidApp/src/test`.
- Naming: `method_shouldExpectedBehavior_whenCondition` (no backtick). Mantieni l'intent originale quando aggiorni un test.
- Testa ViewModel tramite `viewModel.onEvent(Event.X)` + `advanceUntilIdle()`, poi assertisci su `state.value`.
- Niente Robolectric, Room, DataStore, `Context`, file I/O in `src/test` (Robolectric **consentito** solo in `src/androidTest`).

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
- **NON valide**: `ok`, `looks good`, silenzio, approvazione da un messaggio precedente.
- Prima di chiedere: mostra `git diff --stat`, lista file, proponi commit message, riporta risultato build/test.
- Mai committare su `main`. Mai committare lavoro parziale.

---

## Analytics & file protetti (AGENTS.md §13)

- Logga solo eventi presenti nella whitelist in `analytics/`; **mai** contenuto utente (note, importi, payee).
- **Non modificare mai**: `build/`, `.gradle/`, `.idea/`, `*.jks`, `google-services.json`, `local.properties`, `secrets.properties`.

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
