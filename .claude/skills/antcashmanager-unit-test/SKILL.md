---
name: antcashmanager-unit-test
description: >
  Guida per scrivere unit test su AntCashManager: ViewModel test con Fake repository,
  UseCase test con TestDataBuilder, test di modelli domain, workers e utility.
  Usa questa skill SEMPRE quando si chiede di scrivere, aggiungere o correggere test,
  implementare copertura per un ViewModel/UseCase/Worker, o si menziona JUnit, MockK,
  BaseUnitTest, BaseUseCaseTest, Fake repository, o advanceUntilIdle.
  Triggera anche per "testa questo", "aggiungi test per", "coverage", "unit test",
  "test fallisce", "cosa testare in", "scrivi test per".
---

# AntCashManager — Unit Test Guide

Progetto: `/opt/src/GIT/app/AntCashManager/`

---

## Localizzazione dei test

| Scope | Source set | Base class |
|---|---|---|
| ViewModel, Android host | `androidApp/src/test/kotlin/…` | `BaseUnitTest` |
| Compose component | `androidApp/src/test/kotlin/…` | `BaseComposeUnitTest` (vedi nota) |
| UseCase / domain | `shared/src/commonTest/kotlin/…` | `BaseUseCaseTest` |
| Repository / data | `shared/src/androidHostTest/kotlin/…` | JUnit4 + MockK |
| Instrumentation | `androidApp/src/androidTest/kotlin/…` | `AndroidJUnit4` |

> **`BaseComposeUnitTest`** è marcata `@Ignore`: i test Compose vanno in `src/androidTest`, non in `src/test`.

---

## Regola 1 — Usare MockK per tutte le dipendenze, inclusi Flow e stato

Usa `mockk()` per **tutte** le dipendenze, anche quando restituiscono `Flow`. I Fake in `testutil/` esistono ma sono un approccio legacy: preferire sempre MockK per controllo esplicito e leggibilità.

```kotlin
// Repository con Flow → every + flowOf
private val transactionRepository: TransactionRepository = mockk()
private val settingsRepository: SettingsRepository = mockk(relaxed = true)

@Before
fun setup() {
    every { transactionRepository.getAllTransactions() } returns flowOf(emptyList())
    every { settingsRepository.getAutoBackupEnabled() } returns flowOf(true)
}

// Repository con stato mutabile → MutableStateFlow esposto tramite mockk
private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
every { transactionRepository.getAllTransactions() } returns _transactions

// Nel test:
_transactions.value = listOf(testTransaction { title = "Coffee" })
advanceUntilIdle()
assertEquals(1, viewModel.state.value.items.size)
```

**Dipendenze senza stato (tracker, analytics, services):**

```kotlin
val segmentationTracker: SegmentationTracker = mockk(relaxed = true)
val analyticsManager: AnalyticsManager = mockk(relaxed = true)
val backupService: BackupService = mockk(relaxed = true)
coEvery { backupService.createBackup() } returns Result.success("{}")
```

---

## Regola 2 — Struttura di un ViewModel test

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MyFeatureViewModelTest : BaseUnitTest() {

    private lateinit var fakeRepo: FakeTransactionRepository
    private lateinit var viewModel: MyFeatureViewModel

    @Before
    fun setup() {
        fakeRepo = FakeTransactionRepository()
        viewModel = MyFeatureViewModel(
            transactionRepository = fakeRepo,
            dispatcher = testDispatcher,       // SEMPRE iniettare testDispatcher
        )
    }

    @After
    override fun tearDownDispatchers() {
        if (::viewModel.isInitialized) {
            viewModel.viewModelScope.cancel()
            testDispatcher.scheduler.advanceUntilIdle()
        }
        super.tearDownDispatchers()
    }

    @Test
    fun state_shouldBeEmpty_whenInitialized() = runViewModelTest {
        assertTrue(viewModel.state.value.items.isEmpty())
    }

    @Test
    fun items_shouldUpdate_whenTransactionAdded() = runViewModelTest {
        fakeRepo.transactions.value = listOf(testTransaction { title = "Coffee" })
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.items.size)
    }

    @Test
    fun state_shouldShowError_whenRepositoryFails() = runViewModelTest {
        fakeRepo.errorToThrow = RuntimeException("DB error")
        viewModel.onEvent(MyFeatureEvent.Load)
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.errorState)
    }
}
```

**Regole chiave:**

- `runViewModelTest { }` per ogni test coroutine (mai `runTest` nudo nei ViewModel test).
- `advanceUntilIdle()` dopo ogni `onEvent(…)` prima di assertare lo state.
- Testare sempre lo **stato iniziale** prima degli eventi.
- Cancellare il `viewModelScope` in `@After` (evita leak tra test).
- Disabilitare debounce con `searchDebounceMs = 0L` nel costruttore quando disponibile.

---

## Regola 3 — Struttura di un UseCase test

```kotlin
class MyUseCaseTest : BaseUseCaseTest() {

    private val fakeRepo = FakeTransactionRepository()
    private val useCase = MyUseCase(
        transactionRepository = fakeRepo,
        dispatcher = testDispatcher,
    )

    @Test
    fun execute_shouldReturnFilteredList_whenCategoryMatches() = runUnitTest {
        fakeRepo.transactions.value = listOf(
            testTransaction { category = "Food" },
            testTransaction { category = "Transport" },
        )

        val result = useCase(MyUseCase.Params(category = "Food")).getOrThrow()

        assertEquals(1, result.size)
        assertEquals("Food", result.first().category)
    }

    @Test
    fun execute_shouldReturnFailure_whenRepositoryThrows() = runUnitTest {
        fakeRepo.errorToThrow = IOException("network error")

        val result = useCase(MyUseCase.Params(category = "Food"))

        assertTrue(result.isFailure)
    }
}
```

**Regole chiave:**

- Invocare tramite `useCase(params)` (chiama `invoke()`, non `execute()` direttamente).
- `getOrThrow()` nei casi happy-path; `isFailure` / `exceptionOrNull()` nei casi di errore.
- Non fare `coVerify` sulle interazioni col repository in UseCase test: preferire assertare il **risultato**.

---

## Regola 4 — TestDataBuilder

Usa sempre `testTransaction { }` e `testCategory { }` invece di costruire oggetti raw.
I builder si trovano in `shared/src/commonTest/kotlin/…/testutil/TestDataBuilder.kt`.

```kotlin
// Corretto
val tx = testTransaction {
    title = "Lunch"
    amount = -12.50
    type = TransactionType.EXPENSE
    category = "Food"
    timestamp = 1_700_000_000_000L   // fisso: evita flakiness da clock
}

// Sbagliato — parametri non necessari rendono il test fragile
val tx = Transaction(
    id = 1L, title = "Lunch", amount = -12.50, category = "Food",
    type = TransactionType.EXPENSE, timestamp = System.currentTimeMillis(),
    notes = "", payee = "", location = "", isRecurring = false,
    tags = "", recurrenceInterval = "", paymentType = PaymentType.ELECTRONIC,
    mealVoucherCount = 0, mealVoucherDifference = 0.0,
    categoryIcon = "", categoryColor = 0xFF000000L,
)
```

Usa timestamp **fissi** (es. `1_700_000_000_000L`) nei test: `System.currentTimeMillis()` rende i test time-sensitive.

---

## Regola 5 — Naming dei test

```
method_shouldExpectedBehavior_whenCondition
```

- Niente backtick.
- `method` = nome del metodo/evento/funzione testata.
- `shouldExpectedBehavior` = il risultato atteso.
- `whenCondition` = la precondizione o input rilevante.

```kotlin
// Corretto
fun onSearchEvent_shouldFilterTransactions_whenQueryMatchesTitle()
fun execute_shouldReturnNull_whenListIsEmpty()
fun state_shouldShowLoading_whenOperationInProgress()

// Sbagliato
fun `test search`()
fun testFiltering()
fun shouldWork()
```

---

## Regola 6 — Un comportamento per test

Ogni test verifica **un solo comportamento**. Se serve verificare più proprietà dello stesso state, è accettabile asserire più campi dello stesso oggetto — ma mai due eventi diversi nello stesso test.

```kotlin
// Corretto: un evento, più proprietà correlate dello state
@Test
fun onDeleteEvent_shouldRemoveTransactionAndShowConfirmation_whenIdIsValid() = runViewModelTest {
    fakeRepo.transactions.value = listOf(testTransaction { id = 42L })
    viewModel.onEvent(MyEvent.Delete(id = 42L))
    advanceUntilIdle()

    assertTrue(fakeRepo.transactions.value.isEmpty())
    assertTrue(viewModel.state.value.deletionConfirmed)
}

// Sbagliato: due eventi in un test
@Test
fun testAddAndDelete() = runViewModelTest {
    viewModel.onEvent(MyEvent.Add(...))
    advanceUntilIdle()
    viewModel.onEvent(MyEvent.Delete(...))
    advanceUntilIdle()
    // Qual è il comportamento che si sta testando?
}
```

---

## Regola 7 — Testare i parametri degli analytics

Verifica sia che il tracker sia stato chiamato **sia i parametri** dell'evento. I parametri analytics fanno parte del contratto osservabile del ViewModel.

```kotlin
// Verifica chiamata + parametri specifici
coVerify {
    analyticsManager.track(match { event ->
        event.name == "home_opened" && event.params["source"] == "nav"
    })
}

// Verifica numero di chiamate + parametri
coVerify(exactly = 1) {
    segmentationTracker.trackTransaction(match { tx ->
        tx.type == TransactionType.EXPENSE && tx.amount == -12.50
    })
}

// Verifica che NON sia stato chiamato in un path di errore
coVerify(exactly = 0) { analyticsManager.track(any()) }
```

Non testare mai il **contenuto** di note, titoli, payee o qualsiasi dato inserito dall'utente negli eventi analytics — sono dati personali protetti dalla whitelist in `analytics/`.

---

## Regola 8 — Testare i path di errore con `fakeRepo.errorToThrow`

Non limitarsi ai casi happy-path. Ogni UseCase e ViewModel che gestisce errori deve avere almeno un test di fallimento.

```kotlin
@Test
fun state_shouldExposeError_whenLoadFails() = runViewModelTest {
    fakeRepo.errorToThrow = RuntimeException("DB unavailable")
    viewModel.onEvent(HomeEvent.Load)
    advanceUntilIdle()

    assertNotNull(viewModel.state.value.errorState)
}
```

Per testare la cancellazione, usare `fakeRepo.operationDelayMs = 500L` e poi cancellare il job.

---

## Regola 9 — MockK: `coEvery` per suspend, `every` per Flow

```kotlin
// suspend function → coEvery
coEvery { repo.saveTransaction(any()) } returns Unit

// Flow (non suspend) → every
every { repo.getAllTransactions() } returns flowOf(emptyList())

// Flow da SettingsRepository → sempre every (non suspend)
every { settingsRepository.getAutoBackupEnabled() } returns flowOf(true)
```

Non usare `coEvery` su funzioni non-suspend: il test compilerà ma il comportamento sarà inconsistente.

---

## Regola 10 — Comandi Gradle per eseguire i test

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# ViewModel e Android host test (targetted)
./gradlew :androidApp:testFullDebugUnitTest --tests "*MyViewModelTest*" 2>&1 | tail -30

# UseCase / domain
./gradlew :shared:testAndroidHostTest 2>&1 | tail -30

# Tutti i test unitari androidApp
./gradlew :androidApp:testFullDebugUnitTest 2>&1 | tail -30
```

Esegui sempre test targetted (`--tests "*ClassName*"`) durante lo sviluppo.
La suite completa solo alla fine del task.
