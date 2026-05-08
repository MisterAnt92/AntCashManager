# GitHub Copilot Instructions - AntCashManager

## Project Overview

AntCashManager è un'app Android di gestione finanziaria personale costruita con Jetpack Compose,
Kotlin Multiplatform, e principi di Clean Architecture. Quando generi codice, segui SEMPRE queste
linee guida architetturali.

---

## ⚙️ Metodologia di Lavoro - OBBLIGATORIO

### Esecuzione per Step

**REGOLA CRITICA**: Esegui SEMPRE task e procedimenti **step by step**:

1. **Analizza** il contesto e i file esistenti prima di scrivere codice
2. **Pianifica** i cambiamenti necessari
3. **Implementa** un file/componente alla volta
4. **Verifica** errori dopo ogni modifica prima di procedere al passo successivo
5. **Conferma** il completamento di ogni step prima di passare al successivo

### Pulizia Import e Package Name

**REGOLA CRITICA**: Ogni volta che crei o modifichi una classe/file Kotlin:

- ✅ **Rimuovi TUTTI gli import non utilizzati** prima di salvare
- ✅ **Verifica che il `package` corrisponda** esattamente alla struttura di directory del file
- ✅ Correggi il package name se errato (es. file in `ui/screen/home/` →
  `package com.antcashmanager.android.ui.screen.home`)
- ❌ MAI lasciare import `unused` o package name sbagliato

```kotlin
// ✅ CORRECT - import puliti, package corretto
package com.antcashmanager.android.ui.screen.transactions

import com.antcashmanager.domain.model.Transaction
import kotlinx.coroutines.flow.StateFlow

// ❌ WRONG - import inutili, package errato
package com.antcashmanager.android.ui  // WRONG package!

import com.antcashmanager.domain.model.Transaction
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.ui.graphics.Color  // unused import!
import java.util.Date                       // unused import!
```

### Regole Trasversali (Sempre Attive)

**REGOLA CRITICA**: Queste regole valgono in ogni task, anche durante refactor o migrazioni:

- ✅ Quando crei o aggiorni unit test, **mantieni lo scopo originale del test** anche se cambia il
  codice implementativo
- ✅ Quando crei o modifichi classi/file, **rimuovi sempre gli import non necessari** prima di
  chiudere la modifica
- ✅ Quando crei o modifichi classi/file, **verifica sempre il `package name`** e correggilo se non
  corrisponde alla directory reale

---

## 🏗️ Clean Architecture - Layer Structure

### Struttura a 3 Layer

```
Presentation (androidApp) → Domain (shared/commonMain) → Data (shared/androidMain)
```

**Regole CRITICHE**:

- ✅ Presentation dipende SOLO da Domain
- ✅ Domain è puro Kotlin (NO dipendenze Android/platform)
- ✅ Data implementa le interfacce di Domain
- ❌ Domain NON deve MAI dipendere da Presentation o Data

---

## 🎯 Pattern Architetturale: UseCase → ViewModel → State → Screen

### 1. UseCase (Domain Layer)

Quando crei un UseCase, estendi **sempre** una delle base class con dispatcher injection:

```kotlin
package com.antcashmanager.domain.usecase.yourfeature

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * UseCase per [descrizione funzionalità].
 * @param repository Repository necessario
 * @param dispatcher Dispatcher per eseguire l'operazione (default: Dispatchers.Default)
 */
class YourFeatureUseCase(
    private val repository: YourRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : BaseUseCase<String, Result<Data>>(dispatcher) {
    /**
     * Esegue [operazione]. Chiamato da `invoke()` sul dispatcher corretto.
     * @param params Parametro necessario
     * @return Result con il valore desiderato o una custom domain exception
     */
    override suspend fun execute(params: String): Result<Data> = runCatching {
        repository.getData(params) ?: throw YourFeatureException.NotFound(params)
    }
}
```

**Base class da estendere in base al tipo:**

| Tipo operazione         | Base class                       | Metodo da implementare                          |
|-------------------------|----------------------------------|-------------------------------------------------|
| suspend con parametri   | `BaseUseCase<Params, Result<T>>` | `override suspend fun execute(params)`          |
| suspend senza parametri | `NoParamsUseCase<Result<T>>`     | `override suspend fun execute()`                |
| Flow con parametri      | `FlowUseCase<Params, Result<T>>` | `override fun execute(params): Flow<Result<T>>` |
| Flow senza parametri    | `NoParamsFlowUseCase<Result<T>>` | `override fun execute(): Flow<Result<T>>`       |

**Requirements**:

- Nome termina con `UseCase`
- Single responsibility
- Usa `operator fun invoke()` per renderlo callable (ereditato dalla base class)
- Implementa `execute()` (NON `invoke()` — è `final` nella base class)
- **Usa SEMPRE `Result<T>` come tipo di ritorno** per incapsulare successo o custom exception
- `dispatcher` nel costruttore per testabilità (default `Dispatchers.Default`)
- Max 250 righe
- KDoc documentation completa
- NO logica UI
- NO accesso diretto a database/network

### Pattern Result negli UseCase - OBBLIGATORIO

**REGOLA CRITICA**: Gli UseCase DEVONO restituire `Result<T>` per gestire successo e failure in modo
esplicito, seguendo la Clean Architecture.

#### Custom Domain Exceptions

Definisci le eccezioni nel **Domain Layer** (`shared/commonMain/domain/exception/`):

```kotlin
package com.antcashmanager.domain.exception

/**
 * Eccezioni di dominio per operazioni sulle transazioni.
 */
sealed class TransactionException(message: String) : Exception(message) {
    class NotFound(id: Long) : TransactionException("Transaction $id not found")
    class InvalidAmount(amount: Double) : TransactionException("Invalid amount: $amount")
    class InsertFailed(cause: Throwable? = null) : TransactionException("Failed to insert transaction")
}

sealed class CategoryException(message: String) : Exception(message) {
    class NotFound(name: String) : CategoryException("Category '$name' not found")
    class DuplicateName(name: String) : CategoryException("Category '$name' already exists")
}
```

#### UseCase con Result

```kotlin
// ✅ CORRECT - UseCase suspend con Result<T>
class InsertTransactionUseCase(
    private val repository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : BaseUseCase<Transaction, Result<Long>>(dispatcher) {

    override suspend fun execute(params: Transaction): Result<Long> = runCatching {
        require(params.amount > 0) { throw TransactionException.InvalidAmount(params.amount) }
        repository.insertTransaction(params)
    }
}

// ✅ CORRECT - Flow UseCase con Result<T>
class GetTransactionsUseCase(
    private val repository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsFlowUseCase<Result<List<Transaction>>>(dispatcher) {

    override fun execute(): Flow<Result<List<Transaction>>> =
        repository.getAllTransactions()
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
}
```

#### ViewModel consuma Result

```kotlin
// ✅ CORRECT - ViewModel gestisce Result con onSuccess/onFailure
fun insertTransaction(transaction: Transaction) {
    activeJob?.cancel()
    activeJob = viewModelScope.launch {
        Logger.d(TAG) { "Inserting transaction" }
        useCase(transaction)
            .onSuccess { id ->
                Logger.i(TAG) { "Transaction inserted with id=$id" }
                _state.update { it.copy(isLoading = false) }
            }
            .onFailure { error ->
                if (error is CancellationException) throw error // ✅ re-throw sempre!
                Logger.e(TAG, error) { "Insert failed" }
                val message = when (error) {
                    is TransactionException.InvalidAmount -> "Importo non valido"
                    is TransactionException.InsertFailed -> "Errore durante il salvataggio"
                    else -> "Errore sconosciuto"
                }
                _state.update { it.copy(error = message, isLoading = false) }
            }
    }
}
```

### 2. ViewModel (Presentation Layer)

Quando crei un ViewModel:

```kotlin
package com.antcashmanager.android.ui.screen.yourfeature

class YourFeatureViewModel(
    private val useCase: YourFeatureUseCase,
    private val repository: YourRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "YourFeatureViewModel"
        private const val SHARING_TIMEOUT = 5_000L
    }

    // StateFlow per state management
    val state: StateFlow<YourFeatureState> = combine(
        repository.getData1(),
        repository.getData2(),
    ) { data1, data2 ->
        YourFeatureState(data1 = data1, data2 = data2)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
        YourFeatureState()
    )

    fun performAction(param: String) {
        Logger.d(TAG) { "Performing action: $param" }
        viewModelScope.launch {
            try {
                useCase(param).collect { result ->
                    // Update state
                }
            } catch (e: Exception) {
                Logger.e(TAG, e) { "Error performing action" }
            }
        }
    }

    private fun updatePreference(logMsg: String, action: suspend () -> Unit) {
        Logger.d(TAG) { logMsg }
        viewModelScope.launch { action() }
    }
}
```

**Requirements**:

- `StateFlow` per esporre stato immutabile
- `MutableStateFlow` privato per update interni
- Kermit Logger per logging (`import co.touchlab.kermit.Logger`)
- Consuma `Result<T>` con `onSuccess`/`onFailure` (no try-catch diretto su UseCase)
- Costanti in `companion object`
- Max 300 righe
- NO business logic (delegare a UseCase)
- MAI hold Context reference (passarlo come parametro metodo)

### 3. State (Presentation Layer)

Quando crei uno State:

```kotlin
package com.antcashmanager.android.ui.screen.yourfeature

/**
 * UI State per YourFeatureScreen.
 * @property isLoading Loading indicator
 * @property data Dati da visualizzare
 * @property error Error message, null se no error
 */
data class YourFeatureState(
    val isLoading: Boolean = false,
    val data: List<YourData> = emptyList(),
    val error: String? = null,
    val filterType: FilterType = FilterType.ALL,
)

sealed class YourFeatureEvent {
    data class LoadData(val filter: String) : YourFeatureEvent()
    object Refresh : YourFeatureEvent()
}

sealed class YourFeatureSideEffect {
    data class ShowToast(val message: String) : YourFeatureSideEffect()
    data class NavigateTo(val route: String) : YourFeatureSideEffect()
}
```

**Requirements**:

- Immutable data class
- Default values ragionevoli
- KDoc documentation
- Sealed classes per eventi e side-effects
- Max 100 righe
- NO logica
- NO riferimenti a Context o View

### 4. Screen (Composables)

Quando crei uno Screen:

```kotlin
package com.antcashmanager.android.ui.screen.yourfeature

@Composable
fun YourFeatureScreen(
    repository: YourRepository,
    navController: NavController,
) {
    val viewModel: YourFeatureViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                YourFeatureViewModel(repository) as T
        },
    )
    
    val state by viewModel.state.collectAsState()
    
    YourFeatureContent(
        state = state,
        onAction = viewModel::performAction,
        onNavigateBack = { navController.popBackStack() },
    )
}

@Composable
internal fun YourFeatureContent(
    state: YourFeatureState,
    onAction: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = stringResource(R.string.your_feature_title))
        
        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorView(state.error)
            state.data.isEmpty() -> EmptyView()
            else -> DataList(state.data, onAction)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun YourFeatureContentPreview() {
    AntCashManagerTheme {
        YourFeatureContent(
            state = YourFeatureState(),
            onAction = {},
            onNavigateBack = {},
        )
    }
}
```

**Requirements**:

- Separare Screen wrapper da Content composable (testabilità)
- SEMPRE `stringResource(R.string.*)` per stringhe (MAI hardcoded)
- `@Preview` per ogni composable principale
- Max 400 righe (estrarre sub-composables se più lungo)
- ZERO business logic
- NO accesso diretto a repository/database

---

## 🌍 Internazionalizzazione - OBBLIGATORIO

**CRITICO**: TUTTE le stringhe user-facing DEVONO essere localizzate.

### Quando aggiungi QUALSIASI stringa:

1. **Aggiungi a 5 file lingua**:
    - `androidApp/src/main/res/values/strings.xml` (English)
    - `androidApp/src/main/res/values-it/strings.xml` (Italian)
    - `androidApp/src/main/res/values-fr/strings.xml` (French)
    - `androidApp/src/main/res/values-de/strings.xml` (German)
    - `androidApp/src/main/res/values-es/strings.xml` (Spanish)

2. **Naming convention**: `<screen>_<component>_<description>`
   ```xml
   <string name="transactions_title">Transactions</string>
   <string name="transactions_empty">No transactions</string>
   <string name="transactions_error">Error: %s</string>
   ```

3. **Usage nel codice**:
   ```kotlin
   // ✅ CORRECT
   Text(text = stringResource(R.string.your_feature_title))
   
   // ✅ Con parametri
   Text(text = stringResource(R.string.error_message, errorDetails))
   
   // ❌ WRONG - MAI FARE QUESTO
   Text(text = "Hardcoded string") // FORBIDDEN!
   ```

---

## 🧩 Componenti UI - USO OBBLIGATORIO DEI COMPONENTI ESISTENTI

**REGOLA CRITICA**: Prima di creare qualsiasi componente UI, VERIFICA SEMPRE se esiste in
`ui/components/`.

### Componenti Disponibili (USARE QUESTI!)

#### Layout & Structure

```kotlin
// ✅ Header schermate
ScreenHeader(
    title = stringResource(R.string.screen_title),
    actions = { /* Optional */ }
)

// ✅ Divider
AppDivider()

// ✅ Help button
HelpButton(onHelpClick = { showHelpDialog = true })
```

#### Card Components

```kotlin
// ✅ Card standard
AppCard(
    title = stringResource(R.string.title),
    subtitle = stringResource(R.string.subtitle),
    leadingIcon = Icons.Default.YourIcon,
    iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
    onClick = { /* action */ },
    trailingContent = { /* Optional: Switch, Badge, etc */ }
)

// ✅ Section header
AppCardSectionHeader(title = stringResource(R.string.section))
```

#### Input Components

```kotlin
// ✅ Switch
AppSwitch(
    checked = state,
    onCheckedChange = { /* update */ }
)

// ✅ Radio button
AppRadioButton(
    selected = isSelected,
    onClick = { /* select */ }
)

// ✅ List item
AppListItem(
    headlineContent = { Text("Title") },
    leadingContent = { /* Icon/Radio */ }
)
```

#### Text Components

```kotlin
// ✅ Text styled
AppText(
    text = stringResource(R.string.text),
    style = MaterialTheme.typography.bodyLarge
)
```

### Pattern Layout Sezioni

```kotlin
// ✅ CORRECT Pattern
AppCardSectionHeader(title = stringResource(R.string.section_title))

Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    AppCard(
        title = stringResource(R.string.item1),
        subtitle = stringResource(R.string.item1_desc),
        leadingIcon = Icons.Default.Icon1,
        onClick = { /* action */ }
    )
    
    AppCard(
        title = stringResource(R.string.item2),
        subtitle = stringResource(R.string.item2_desc),
        leadingIcon = Icons.Default.Icon2,
        trailingContent = {
            AppSwitch(checked = state, onCheckedChange = { /* update */ })
        },
        onClick = { /* toggle */ }
    )
}
```

### Workflow Componenti

1. **VERIFICA** in `ui/components/` se componente esiste
2. **USA** componente esistente (AppCard, ScreenHeader, ecc.)
3. **CREA** nuovo solo se non esiste, nel package corretto

```kotlin
// ✅ CORRECT
AppCard(
    title = stringResource(R.string.settings_theme),
    subtitle = themeLabel,
    leadingIcon = Icons.Default.Palette,
    onClick = { showThemeDialog = true }
)

// ❌ WRONG - NON ricreare se AppCard esiste!
@Composable
fun CustomCard(...) {  // FORBIDDEN!
    Card { /* reimplementazione */ }
}
```

---

## 🎨 Tema & Styling - OBBLIGATORIO

Usa SEMPRE MaterialTheme centralizzato:

### Colors

```kotlin
// ✅ CORRECT
Card(
    containerColor = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.onSurface,
)
Icon(tint = MaterialTheme.colorScheme.primary)

// ❌ WRONG
Card(containerColor = Color(0xFF123456)) // FORBIDDEN!
```

### Typography

```kotlin
// ✅ CORRECT
Text(
    text = title,
    style = MaterialTheme.typography.headlineMedium,
    fontWeight = FontWeight.Bold,
)

// ❌ WRONG
Text(fontSize = 24.sp) // FORBIDDEN!
```

### Spacing

```kotlin
// ✅ Spacing consistente
modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)

// ✅ Spacing tra card
Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { /* cards */ }
```

---

## 🧪 Testing Requirements

Quando crei una feature, genera SEMPRE i test e non usare mai i back tick per i nomi dei test (usare
`testName_shouldExpectedBehavior_whenCondition`).

### Regola: Mantieni lo Scopo del Test

**REGOLA CRITICA**: Quando aggiorni un test esistente (es. perché il codice cambia):

- ✅ **Mantieni SEMPRE lo scopo originale** del test (cosa si sta verificando)
- ✅ Aggiorna solo le asserzioni per riflettere il nuovo contratto (es. `Result<T>` invece di valore
  diretto)
- ✅ Il nome del test deve restare descrittivo dello scopo, non dell'implementazione
- ❌ NON cambiare cosa si testa solo perché cambia come funziona internamente

```kotlin
// Originale (prima di Result)
@Test
fun `invoke should return id when transaction is inserted`() = runTest {
    val result = useCase(transaction)
    assertEquals(1L, result)
}

// ✅ CORRECT aggiornamento (dopo introduzione Result) — scopo invariato
@Test
fun `invoke should return id when transaction is inserted`() = runTest {
    val result = useCase(transaction)
    assertEquals(1L, result.getOrThrow()) // scopo invariato: verifica l'id tornato
}

// ❌ WRONG — scopo cambiato inutilmente
@Test
fun `invoke should return Result success`() = runTest { // troppo generico, scopo perso
    assertTrue(useCase(transaction).isSuccess)
}
```

### UseCase Test con Dispatcher, Cancellazione e Result

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class YourFeatureUseCaseTest {

    // StandardTestDispatcher: deterministico, richiede advanceUntilIdle()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeYourRepository
    private lateinit var useCase: YourFeatureUseCase

    @Before
    fun setup() {
        repository = FakeYourRepository()
        // UnconfinedTestDispatcher: emette immediatamente (ideale per Flow tests)
        useCase = YourFeatureUseCase(repository, UnconfinedTestDispatcher(testDispatcher.scheduler))
    }

    // ── Happy Path ──────────────────────────────────────────────────────────────

    @Test
    fun `invoke should return data from repository`() = runTest(testDispatcher) {
        // Given
        val expectedData = YourData(1, "Test")
        repository.dataToReturn = expectedData

        // When
        val result = useCase("test")

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedData, result.getOrNull())
    }

    // ── Error Handling con Result e custom exception ─────────────────────────────

    @Test
    fun `invoke should return Result failure with domain exception on repository error`() = runTest(testDispatcher) {
        // Given
        repository.shouldThrow = true

        // When
        val result = useCase("test")

        // Then
        assertTrue(result.isFailure)
        assertIs<YourFeatureException>(result.exceptionOrNull())
    }

    // ── Dispatcher Injection ────────────────────────────────────────────────────

    @Test
    fun `invoke with StandardTestDispatcher needs advanceUntilIdle`() = runTest(testDispatcher) {
        val standardUseCase = YourFeatureUseCase(repository, testDispatcher)
        var result: Result<YourData>? = null

        val job = launch { result = standardUseCase("test") }
        advanceUntilIdle() // necessario con StandardTestDispatcher

        assertNotNull(result)
        assertTrue(result!!.isSuccess)
        job.join()
    }

    // ── Cancellazione ───────────────────────────────────────────────────────────

    @Test
    fun `invoke should be cancellable before completion`() = runTest(testDispatcher) {
        val slowRepo = SlowFakeRepository(delayMs = 10_000L)
        val cancellableUseCase = YourFeatureUseCase(slowRepo, testDispatcher)

        val job: Job = launch { cancellableUseCase("test") }
        job.cancel()
        advanceUntilIdle()

        assertTrue(job.isCancelled)
        assertFalse(slowRepo.operationExecuted)
    }
}

// ── Fake Repository ─────────────────────────────────────────────────────────────

private class FakeYourRepository : YourRepository {
    var dataToReturn: YourData = YourData(0, "")
    var shouldThrow = false

    override suspend fun getData(param: String): YourData? {
        if (shouldThrow) throw RuntimeException("DB error")
        return dataToReturn
    }
}

/** Fake repository lento per testare la cancellazione. */
private class SlowFakeRepository(private val delayMs: Long) : YourRepository {
    var operationExecuted = false

    override suspend fun getData(param: String): YourData? {
        delay(delayMs) // punto di cancellazione cooperativa
        operationExecuted = true
        return YourData(0, "")
    }
}
```

**TestDispatcher — quando usare quale:**

| Dispatcher                            | Comportamento                       | Quando usarlo                       |
|---------------------------------------|-------------------------------------|-------------------------------------|
| `StandardTestDispatcher()`            | Lazy: richiede `advanceUntilIdle()` | Test deterministici, timing preciso |
| `UnconfinedTestDispatcher()`          | Eager: esegue immediatamente        | Flow tests, emissioni immediate     |
| `UnconfinedTestDispatcher(scheduler)` | Eager ma stesso scheduler           | Setup in `@Before`, share scheduler |

### ViewModel Test

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class YourFeatureViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `performAction should update state`() = runTest {
        // Given, When, Then
    }
}
```

**Test Requirements**:

- Happy path + error cases + **cancellazione**
- **Quando aggiorni un test: mantieni SEMPRE lo scopo originale**, anche se cambia l'implementazione
- Test per `Result.isSuccess` / `Result.isFailure` e tipo esatto di eccezione (
  `assertIs<DomainException>`)
- `@OptIn(ExperimentalCoroutinesApi::class)` a livello di classe
- `runTest(testDispatcher)` per condividere lo scheduler
- `SlowFakeRepository` con `delay()` per testare la cancellazione
- Fake classes (preferite a Mockito per KMP)
- Naming: `` `method should expected_behavior when condition` ``
- Given/When/Then structure
- Target > 80% coverage

---

## 📏 Code Length Limits - OBBLIGATORIO

| Component         | Max Lines | Se Superato                  |
|-------------------|-----------|------------------------------|
| UseCase           | 150       | Split in UseCase più piccoli |
| ViewModel         | 300       | Estrarre logica in UseCase   |
| Screen Composable | 400       | Estrarre sub-composables     |
| State/Model       | 100       | Split in subclasses          |

### Extract Sub-Composables

```kotlin
// ❌ BAD - Monolithic
@Composable
fun TransactionsScreen(...) {
    Column { /* 500+ lines */ }
}

// ✅ GOOD - Modular
@Composable
fun TransactionsScreen(...) {
    Column {
        TransactionHeader(...)
        TransactionFilters(...)
        TransactionList(...)
    }
}
```

---

## 📝 Logging con Kermit

```kotlin
import co.touchlab.kermit.Logger

class YourViewModel(...) : ViewModel() {
    companion object {
        private const val TAG = "YourViewModel"
    }

    fun performAction() {
        Logger.d(TAG) { "Starting action" }
        
        try {
            // operation
            Logger.i(TAG) { "Action completed" }
        } catch (e: Exception) {
            Logger.e(TAG, e) { "Error occurred" }
        }
    }
}
```

**Levels**:

- `Logger.v()` - Verbose
- `Logger.d()` - Debug
- `Logger.i()` - Info
- `Logger.w()` - Warning
- `Logger.e()` - Error

---

## 🔄 Coroutines & Threading negli UseCase - OBBLIGATORIO

### Dispatcher Injection Pattern

**REGOLA CRITICA**: Ogni UseCase DEVE accettare un `CoroutineDispatcher` nel costruttore per
garantire testabilità e corretto threading.

```kotlin
// ✅ CORRECT - dispatcher injection + Result
class InsertTransactionUseCase(
    private val repository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : BaseUseCase<Transaction, Result<Long>>(dispatcher) {
    override suspend fun execute(params: Transaction): Result<Long> = runCatching {
        repository.insertTransaction(params)
    }
}

// ❌ WRONG - nessun dispatcher, nessun Result
class InsertTransactionUseCase(
    private val repository: TransactionRepository,
) {
    suspend operator fun invoke(params: Transaction): Long =
        repository.insertTransaction(params)
}
```

### Come funzionano le Base Class

Le base class garantiscono automaticamente il corretto threading:

```kotlin
// BaseUseCase e NoParamsUseCase → withContext(dispatcher)
abstract class BaseUseCase<in Params, out Result>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    protected abstract suspend fun execute(params: Params): Result

    // invoke() è FINAL → le subclass implementano solo execute()
    suspend operator fun invoke(params: Params): Result = withContext(dispatcher) {
        execute(params)
    }
}

// FlowUseCase e NoParamsFlowUseCase → flowOn(dispatcher)
abstract class FlowUseCase<in Params, out Result>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    protected abstract fun execute(params: Params): Flow<Result>

    operator fun invoke(params: Params): Flow<Result> = execute(params).flowOn(dispatcher)
}
```

**Perché `withContext` vs `flowOn`?**

- `withContext`: per `suspend fun` — sposta l'esecuzione sul dispatcher e blocca finché non completa
- `flowOn`: per `Flow` — cambia il dispatcher di *produzione* degli elementi senza bloccare il
  collector

### Tabella Dispatcher

| Dispatcher            | Quando usarlo                            | Esempio                    |
|-----------------------|------------------------------------------|----------------------------|
| `Dispatchers.Default` | Calcoli CPU-intensive, default KMP       | Filtering, sorting         |
| `Dispatchers.IO`      | Operazioni I/O (DB, rete) — Android only | Room DAO, Retrofit         |
| `Dispatchers.Main`    | Aggiornare UI — solo in ViewModel/Screen | setState                   |
| `TestDispatcher`      | Solo nei test                            | `StandardTestDispatcher()` |

> ⚠️ **Nota KMP**: `Dispatchers.IO` non è disponibile in `commonMain`. Usa `Dispatchers.Default`
> come default nelle base class per compatibilità multiplatform. I caller Android possono passare
`Dispatchers.IO` esplicitamente se necessario.

### Cancellazione Cooperativa

Le coroutine in Kotlin supportano la cancellazione *cooperativa*: ogni `suspend fun` è un punto di
cancellazione.

```kotlin
// ✅ Il UseCase è cancellabile automaticamente (delay/IO sono punti di cancellazione)
class SlowOperationUseCase(
    private val repo: MyRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsUseCase<Result<Data>>(dispatcher) {
    override suspend fun execute(): Result<Data> = runCatching {
        // delay() è un punto di cancellazione → la coroutine può essere annullata qui
        repo.slowOperation()
    }
}
```

**Nel ViewModel — pattern activeJob:**

```kotlin
class YourFeatureViewModel(
    private val useCase: YourFeatureUseCase,
) : ViewModel() {

    companion object {
        private const val TAG = "YourFeatureViewModel"
    }

    private var activeJob: Job? = null

    fun startOperation(params: String) {
        // Cancella job precedente (es. nuova ricerca durante ricerca in corso)
        activeJob?.cancel()

        activeJob = viewModelScope.launch {
            Logger.d(TAG) { "Starting operation: $params" }
            useCase(params)
                .onSuccess { result ->
                    // update state on success
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error // ✅ re-throw sempre!
                    Logger.e(TAG, error) { "Error in operation" }
                    // update state with error
                }
        }
    }

    fun cancelOperation() {
        activeJob?.cancel()
        activeJob = null
    }
}
```

### coroutineScope vs supervisorScope

```kotlin
// coroutineScope: fallimento di un figlio → tutti i figli cancellati
// Usare quando le operazioni sono DIPENDENTI tra loro
override suspend fun execute(): Result<Data> = runCatching {
    coroutineScope {
        val data1 = async { repo.getData1() }
        val data2 = async { repo.getData2() } // se data1 fallisce, data2 viene cancellato
        Data(data1.await(), data2.await())
    }
}

// supervisorScope: fallimento di un figlio → gli altri continuano
// Usare quando le operazioni sono INDIPENDENTI tra loro
override suspend fun execute(): Result<BulkData> = runCatching {
    supervisorScope {
        val job1 = async { repo.operation1() }
        val job2 = async { repo.operation2() } // se job1 fallisce, job2 continua
        BulkData(
            result1 = runCatching { job1.await() }.getOrNull(),
            result2 = runCatching { job2.await() }.getOrNull(),
        )
    }
}
```

### Anti-pattern: Inghiottire CancellationException

```kotlin
// ❌ FORBIDDEN! Non inghiottire CancellationException
override suspend fun execute() {
    try {
        delay(Long.MAX_VALUE)
    } catch (e: Exception) {
        // WRONG: cattura CancellationException e non la rilancia
        Logger.e(TAG) { "Error: ${e.message}" }
    }
}

// ✅ CORRECT: lascia passare CancellationException o re-throwa
override suspend fun execute() {
    try {
        delay(Long.MAX_VALUE)
    } catch (e: CancellationException) {
        throw e // sempre re-throw!
    } catch (e: Exception) {
        Logger.e(TAG, e) { "Error occurred" }
    }
}
```

---

## 🚫 Anti-Patterns - MAI FARE QUESTI

### ❌ Business Logic in Composable

```kotlin
// FORBIDDEN!
@Composable
fun Screen() {
    LaunchedEffect(Unit) {
        val data = database.dao().getAll() // WRONG!
    }
}
```

### ❌ Hardcoded Strings

```kotlin
// FORBIDDEN!
Text("Error occurred!") // WRONG!
```

### ❌ ViewModel with Context

```kotlin
// FORBIDDEN!
class YourViewModel(
    private val context: Context, // WRONG! Memory leak
) : ViewModel()
```

### ❌ Hardcoded Colors/Fonts

```kotlin
// FORBIDDEN!
Text(
    fontSize = 24.sp, // WRONG!
    color = Color(0xFF123456), // WRONG!
)
```

### ❌ Ricreare Componenti Esistenti

```kotlin
// FORBIDDEN!
@Composable
fun MyCard(...) { // WRONG se AppCard esiste!
    Card { /* reimplementazione */ }
}
```

### ❌ UseCase senza Dispatcher Injection

```kotlin
// FORBIDDEN!
class InsertTransactionUseCase(
    private val repository: TransactionRepository,
    // WRONG: nessun dispatcher → non testabile, blocca il thread del caller
) {
    suspend operator fun invoke(params: Transaction): Long =
        repository.insertTransaction(params)
}
```

### ❌ Inghiottire CancellationException

```kotlin
// FORBIDDEN!
override suspend fun execute() {
    try {
        delay(Long.MAX_VALUE)
    } catch (e: Exception) { // WRONG: cattura anche CancellationException!
        Logger.e(TAG) { "Error" }
    }
}
```

### ❌ runBlocking negli UseCase o ViewModel

```kotlin
// FORBIDDEN!
fun performAction() {
    runBlocking { // WRONG: blocca il thread!
        useCase()
    }
}
```

### ❌ Implementare invoke() invece di execute() nelle subclass

```kotlin
// FORBIDDEN!
class MyUseCase(repo: MyRepo, dispatcher: CoroutineDispatcher = Dispatchers.Default)
    : BaseUseCase<String, Data>(dispatcher) {

    // WRONG: override di invoke() bypassa il dispatcher della base class!
    override suspend fun invoke(params: String): Data = repo.getData(params)

    // CORRECT: implementa execute()
    // override suspend fun execute(params: String): Data = repo.getData(params)
}
```

### ❌ UseCase che lancia Exception invece di Result

```kotlin
// FORBIDDEN!
class GetTransactionUseCase(...) : BaseUseCase<Long, Transaction>(...) {
    override suspend fun execute(params: Long): Transaction =
        repository.getById(params) ?: throw RuntimeException("Not found") // WRONG!
}

// ✅ CORRECT
class GetTransactionUseCase(...) : BaseUseCase<Long, Result<Transaction>>(...) {
    override suspend fun execute(params: Long): Result<Transaction> = runCatching {
        repository.getById(params) ?: throw TransactionException.NotFound(params)
    }
}
```

### ❌ Custom Exception fuori dal Domain Layer

```kotlin
// FORBIDDEN!
// In androidApp/ o nel Data Layer:
class InsertTransactionException : Exception("error") // WRONG!
// Le exception vanno in: shared/commonMain/domain/exception/
```

### ❌ Import non utilizzati o package name errato

```kotlin
// FORBIDDEN!
package com.antcashmanager.android  // WRONG: file è in ui/screen/home/!

import androidx.compose.ui.graphics.Color      // unused → RIMUOVI!
import java.util.Date                           // unused → RIMUOVI!
import com.antcashmanager.domain.model.Category // unused → RIMUOVI!
```

---

## ✅ Pre-Commit Checklist

Prima di generare/committare codice:

- [ ] Task eseguito per step (analisi → pianificazione → implementazione → verifica)
- [ ] Import non necessari rimossi da ogni file modificato/creato
- [ ] Package name verificato e corretto per ogni file modificato/creato
- [ ] Clean Architecture rispettato (layer boundaries)
- [ ] UseCase per business logic
- [ ] UseCase estende la base class corretta (`BaseUseCase`, `FlowUseCase`, ecc.)
- [ ] UseCase implementa `execute()` (non `invoke()`)
- [ ] UseCase ha `dispatcher: CoroutineDispatcher = Dispatchers.Default` nel costruttore
- [ ] UseCase restituisce `Result<T>` (non valore diretto o eccezione lanciata)
- [ ] Custom exceptions nel Domain Layer (`shared/commonMain/domain/exception/`)
- [ ] ViewModel coordina, NO business logic
- [ ] ViewModel consuma `Result` con `onSuccess`/`onFailure`
- [ ] ViewModel usa `activeJob?.cancel()` per operazioni annullabili
- [ ] `CancellationException` mai inghiottita senza re-throw
- [ ] NO `runBlocking` fuori dai test
- [ ] State immutabile
- [ ] Screen SOLO UI, NO logic
- [ ] TUTTE stringhe in `strings.xml` (5 lingue)
- [ ] NO hardcoded colors/typography
- [ ] Usati componenti esistenti (`AppCard`, `ScreenHeader`, ecc.)
- [ ] Unit tests inclusi (happy path + error con tipo eccezione + cancellazione)
- [ ] Test aggiornati mantengono lo scopo originale
- [ ] Test con `@OptIn(ExperimentalCoroutinesApi::class)` a livello di classe
- [ ] Test con `runTest(testDispatcher)` per condividere lo scheduler
- [ ] Kermit Logger usato (non println/Log)
- [ ] Limiti lunghezza rispettati
- [ ] KDoc per public API
- [ ] `@Preview` per Composables
- [ ] Spacing consistente (8.dp tra card)

---

## 🎯 Quick Reference

### File Naming

- UseCase: `YourFeatureUseCase.kt`
- Domain Exception: `YourFeatureException.kt`
- ViewModel: `YourFeatureViewModel.kt`
- State: `YourFeatureState.kt`
- Screen: `YourFeatureScreen.kt`
- Test: `YourFeatureUseCaseTest.kt`, `YourFeatureViewModelTest.kt`

### Package Structure

```
shared/commonMain/
└── com.antcashmanager/
    ├── domain/
    │   ├── exception/          ← Custom domain exceptions
    │   │   ├── TransactionException.kt
    │   │   └── CategoryException.kt
    │   ├── model/
    │   └── usecase/yourfeature/
androidApp/
└── com.antcashmanager.android/
    ├── ui/screen/yourfeature/
    │   ├── YourFeatureScreen.kt
    │   ├── YourFeatureViewModel.kt
    │   ├── YourFeatureState.kt
    │   └── view/ (sub-composables)
    └── ui/components/ (shared components)
```

### Navigation

```kotlin
// Navigate
navController.navigate(Routes.SETTINGS)
navController.navigate("transaction/$id")

// Back
navController.popBackStack()
```

### Componenti UI Checklist

- [ ] Ho verificato `ui/components/` se componente esiste?
- [ ] Sto usando `AppCard` invece di `Card`?
- [ ] Sto usando `ScreenHeader` per titoli?
- [ ] Sto usando `AppCardSectionHeader` per sezioni?
- [ ] Sto usando `AppSwitch` invece di `Switch`?
- [ ] Pattern `Column(spacedBy(8.dp))` per raggruppare card?

---

## 🔗 Additional Context

- Kotlin Compose con Material3
- Kotlin Multiplatform (shared module)
- Room Database (local storage)
- No Hilt/Dagger (manual DI via factory)
- Target: Android API 24+

---

**Quando in dubbio**: Consulta implementazioni esistenti (DisplayScreen, SettingsScreen, HomeScreen)
come esempi di architettura corretta.

**Ricorda**: Clean, testable, maintainable code > quick hacks. Qualità > velocità.

# AGGIORNAMENTO: Controllo obbligatorio chiavi stringhe

## Prima di aggiungere una nuova stringa in qualsiasi file `strings.xml` (tutte le lingue):

#

# 1. CERCA la chiave proposta in tutti i file `strings.xml` (en, it, fr, de, es).

# 2. Se la chiave ESISTE già, RIUTILIZZALA e NON crearne una nuova.

# 3. Se la chiave NON esiste, crea la nuova chiave in tutte le lingue.

# 4. MAI creare chiavi duplicate o con nomi diversi per lo stesso concetto.

# 5. Se trovi chiavi semanticamente identiche ma con nomi diversi, UNIFICA e aggiorna i riferimenti nel codice.

#

# Esempio:

# - Se esiste già `help_dashboard_title`, NON creare `home_dashboard_title`.

# - Se trovi sia `help_dashboard_title` che `home_dashboard_title`, scegli la più coerente (es.
`help_dashboard_title`), elimina l'altra e aggiorna i riferimenti.

#

# Questo controllo è OBBLIGATORIO per tutte le modifiche future alle stringhe.
