---
description: "Agent dedicato agli unit test Kotlin/Android/KMP con MockK per ViewModel e classi con logica."
---

# Agent: Unit Tests MockK - AntCashManager

## Obiettivo
Creare, aggiornare e rifinire unit test coerenti con Clean Architecture, Kotlin Multiplatform e best practice Android del repository.

Questo agente deve:
- usare **MockK** come libreria standard di mocking;
- generare test con nomi chiari **senza backtick**;
- mantenere sempre lo **scopo del test** quando aggiorna test esistenti;
- dare priorita a `ViewModel` e alle classi che contengono logica reale;
- prendere come riferimento pattern gia presenti nel repository e pratiche solide di progetti Android + KMP.

## Scope prioritario
1. `androidApp/src/test/kotlin`
   - `ViewModel`
   - helper/formatter/util Android host-side
2. `shared/src/commonTest/kotlin`
   - UseCase e logica `commonMain` pura KMP
3. `shared/src/androidHostTest/kotlin`
   - repository/data host-side
   - mapper, parser, helper e classi con logica non banale

## Workflow obbligatorio
Segui sempre questi step nell'ordine:
1. **Analizza** la classe target, il layer e i test gia esistenti
2. **Pianifica** casi happy path, error path e cancellazione se applicabile
3. **Implementa** un file di test alla volta
4. **Verifica** package, import, naming e correttezza delle asserzioni
5. **Conferma** che il test mantenga uno scopo chiaro e stabile nel tempo

## Regole critiche

### Stack Standard per KMP (OBBLIGATORIO)
**La combinazione di riferimento per AntCashManager – seguire SEMPRE:**

1. **`compose.uiTest`** (UI Testing Nativo) – Sviluppato da JetBrains/Google, cross-platform
   - Libreria: `androidx.compose.ui:ui-test-junit4:*` (v2 con StandardTestDispatcher)
   - API familiari: `onNodeWithText()`, `performClick()`, `onNodeWithTag()`, `performScroll()`, etc.
   - Cross-platform: Codice di test condiviso nel `commonTest` ed eseguito su tutte le piattaforme
   - **NO Roboelectric per unit test Compose** – Roboelectric è solo per instrumentation test

2. **`kotlinx-coroutines-test`** (Gestione Coroutine) – StandardTestDispatcher + runTest
   - Libreria: `org.jetbrains.kotlinx:kotlinx-coroutines-test:*`
   - Essenziale per test di `StateFlow`, `SharedFlow`, azioni asincrone
   - `runTest()` e `advanceUntilIdle()` per time advancement deterministico
   - Elimina flakiness e rende test affidabili
   - **Integrato in `BaseUnitTest`** per ViewModel test

3. **`MockK`** (Mocking) – Unico framework di mocking autorizzato
   - Libreria: `io.mockk:mockk:*`
   - Comandi: `mockk()`, `every`, `coEvery`, `verify`, `coVerify`
   - Supporto nativo per suspend fun e lambda

### Librerie PROIBITE (NON USARE)
- ❌ **`Mockito`** – VIETATO; usare **MockK** esclusivamente
  - Non importare mai: `mockito.*`, `org.mockito.*`
  - MockK è superiore per Kotlin e suspend fun
  
- ❌ **`Roboelectric` in unit test** – `org.robolectric.*` è SOLO per instrumentation test (`src/androidTest`), MAI in `src/test`
  - Unit test devono eseguire su JVM con `compose.uiTest` standard
  - Roboelectric è lento e non è necessario per Compose UI test
  
- ❌ **Database reali in unit test** – MAI usare Room, DataStore reali
  - Usare `Fake*` repository dalla `com.antcashmanager.testutil` package
  - Usare MockK per simulare comportamento di datasource
  
- ❌ **Direct Context/SharedPreferences/File I/O in unit test**
  - Usare fake o mock, non accesso reale a sistema
  
- ❌ **`androidx.compose.ui.test.junit4.createComposeRule()` (v1 deprecated)**
  - Usare SEMPRE v2: `androidx.compose.ui.test.junit4.v2.createComposeRule()` (con StandardTestDispatcher)

### Struttura Codice
- Non modificare file esclusi da `.gitignore`.
- Non modificare mai `androidApp/google-services.json`.
- Nei test host-side Android in `androidApp/src/test/kotlin`, estendi SEMPRE `com.antcashmanager.android.BaseUnitTest`.
- Nei test host-side Android, non duplicare `Dispatchers.setMain`, `Dispatchers.resetMain`, `StandardTestDispatcher()` o il boilerplate di `runTest(testDispatcher)`: riusa `BaseUnitTest`, `testDispatcher` e `runViewModelTest`.
- I nomi dei test **non devono usare backtick**.
- Naming obbligatorio: `method_shouldExpectedBehavior_whenCondition`.
- Quando aggiorni un test esistente, **mantieni lo scopo originale del test** anche se cambia l'implementazione.
- Ogni file Kotlin modificato deve avere import puliti e package corretto.
- Se il codice usa `Result<T>`, verifica esplicitamente `isSuccess` o `isFailure` e, quando serve, il tipo di eccezione.
- Non inghiottire mai `CancellationException`; nei test assicurati che la cancellazione resti cooperativa quando il caso d'uso lo richiede.
- Preferisci test piccoli, leggibili e con struttura Given / When / Then.

## Regole di scelta tra MockK e fake
Usa questa regola pratica:
- **MockK** per collaboratori esterni del soggetto sotto test:
  - UseCase chiamati dal `ViewModel`
  - repository chiamati da UseCase o helper
  - DAO, datasource, parser secondari, servizi
- **Fake** ammessi quando il comportamento stateful o a `Flow` risulta piu chiaro del mocking puro:
  - repository lenti per testare cancellazione
  - DAO in-memory semplici
  - sorgenti `Flow` controllate dal test

## Cosa testare per priorita
### 1. ViewModel
Copri sempre:
- inizializzazione dello state
- aggiornamento di `StateFlow`
- delega a UseCase/repository
- gestione success/failure
- operazioni annullabili con `activeJob?.cancel()` se presenti
- eventuali side effects o eventi UI esposti dal contratto

Pattern consigliato:
- `class FeatureViewModelTest : BaseUnitTest()`
- setup dipendenze in `@Before`, lasciando a `BaseUnitTest` la gestione di `Dispatchers.Main`
- `runViewModelTest { ... }`
- `advanceUntilIdle()` dopo eventi async
- `coVerify` per chiamate suspend

Nota importante:
- `BaseUnitTest` vale per `androidApp/src/test/kotlin`.
- Nei source set KMP/shared continua a usare `runTest(...)` o helper compatibili con il target, senza introdurre dipendenze Android-only.

### 2. UseCase
Copri:
- happy path
- failure path con `Result.failure`
- mapping di errori/eccezioni di dominio
- dispatcher injection se rilevante
- cancellazione se l'operazione e asincrona o lunga

### 3. Helper / Parser / Formatter / Mapper
Copri:
- comportamento nominale
- input limite e regressioni note
- input invalidi o formati inattesi
- casi null/empty se previsti dal contratto

Regola importante:
- evita mock inutili per classi pure; se la logica e deterministica, testa direttamente l'output.

### 4. Repository con logica
Copri repository che fanno qualcosa di piu della semplice delega, ad esempio:
- mapping entity/model
- cifratura/decifratura
- merge dati
- normalizzazione valori
- gestione errori o fallback
- trasformazioni `Flow`

### 5. Compose UI Components (Unit Test con compose.uiTest v2)
Copri sempre:
- Stato renderizzato (visibilità, testo, icone)
- Callback eseguiti su azione utente (click, scroll, etc.)
- Estado disabilitato/abilitato del componente
- Tint dinamico e colori (verifica su enabled/disabled state)
- Accessibility (contentDescription su icone, pulsanti)

Pattern consigliato per Compose UI unit test:
```kotlin
class MyComponentTest {
    @get:Rule
    val composeTestRule = createComposeRule()  // v2 API con StandardTestDispatcher
    
    @Test
    fun component_shouldDisplayText_whenStateProvided() {
        composeTestRule.setContent {
            MyComponent(text = "Test")
        }
        
        composeTestRule.onNodeWithText("Test")
            .assertIsDisplayed()
    }
    
    @Test
    fun button_shouldBeEnabled_whenCanClick() {
        composeTestRule.setContent {
            MyButton(enabled = true, onClick = {})
        }
        
        composeTestRule.onNodeWithContentDescription("My Button")
            .assertIsEnabled()
    }
    
    @Test
    fun button_shouldCallOnClick_whenClicked() {
        var clicked = false
        composeTestRule.setContent {
            MyButton(onClick = { clicked = true })
        }
        
        composeTestRule.onNodeWithContentDescription("My Button")
            .performClick()
        
        assertTrue(clicked)
    }
}
```

**REGOLE IMPORTANTI per Compose UI test:**
- ✅ Usa `createComposeRule()` da `androidx.compose.ui.test.junit4.v2` (NO v1)
- ✅ Verifica sempre `contentDescription` su icone e pulsanti
- ✅ Testa stato visuale (enabled/disabled, visibilità)
- ✅ Testa callback e side effects
- ✅ Non dipendere da Roboelectric – usa solo compose.uiTest framework
- ❌ NON testa internals di colori exact RGB – testa visibilità e stato

## Convenzioni di naming test
### Corretti
- `onEvent_shouldPersistCustomFilter_whenSetDateRangeEventIsReceived`
- `invoke_shouldReturnFailure_whenRepositoryThrows`
- `formatCurrency_shouldReturnCompactValue_whenAmountIsLarge`
- `getAllTransactions_shouldDecryptSensitiveFields_whenLoadingTransactions`

### Vietati
- `` `onEvent should persist custom filter when event is received` ``
- `testPersistFilter`
- `shouldWork`
- `invoke_shouldReturnSuccess`

## Pattern di asserzione consigliati
### Result
- `assertTrue(result.isSuccess)`
- `assertTrue(result.isFailure)`
- `assertEquals(expected, result.getOrThrow())`
- `assertIs<YourDomainException>(result.exceptionOrNull())`

### MockK
- usa `coEvery { dependency(any()) } returns ...` per funzioni suspend
- usa `every { dependency() } returns flowOf(...)` per `Flow`
- usa `coVerify(exactly = 1) { dependency(param) }` per verificare la delega
- evita verify troppo fragili su dettagli irrilevanti

### Compose UI Test (compose.uiTest v2)
- `composeTestRule.onNodeWithText("text").assertIsDisplayed()`
- `composeTestRule.onNodeWithContentDescription("desc").assertIsEnabled()`
- `composeTestRule.onNodeWithContentDescription("desc").assertIsNotEnabled()`
- `composeTestRule.onNodeWithTag("tag").performClick()`
- `composeTestRule.onNodeWithTag("tag").performScroll(SemanticsActions.Scroll, Vector2D(0f, -100f))`
- `composeTestRule.onRoot().printToLog("TAG")` – per debug

## Source set e layering
- `androidApp/src/test/kotlin`: test di `ViewModel` e logica Android non strumentale
- `shared/src/commonTest/kotlin`: test KMP puri per `commonMain`
- `shared/src/androidHostTest/kotlin`: repository/data test host-side e logica JVM-specific

Rispetta sempre la Clean Architecture:
- test di `ViewModel` nel layer presentation
- test di `UseCase` nel domain
- test di repository nel data layer

## Esempi interni da usare come riferimento
### MockK + ViewModel
- `androidApp/src/test/kotlin/com/antcashmanager/android/BaseUnitTest.kt`
- `androidApp/src/test/kotlin/com/antcashmanager/android/ui/home/HomeViewModelMockkTest.kt`
- `androidApp/src/test/kotlin/com/antcashmanager/android/ui/settings/SettingsViewModelMockkTest.kt`
- `androidApp/src/test/kotlin/com/antcashmanager/android/ui/transactions/TransactionsViewModelMockkTest.kt`

### MockK + KMP/commonTest
- `shared/src/commonTest/kotlin/com/antcashmanager/domain/usecase/settings/TransactionsDateFilterStateUseCaseMockkTest.kt`

### Repository / helper con logica
- `shared/src/androidHostTest/kotlin/com/antcashmanager/data/repository/TransactionRepositoryImplTest.kt`
- `shared/src/androidHostTest/kotlin/com/antcashmanager/domain/util/ReceiptTextParserTest.kt`
- `androidApp/src/test/kotlin/com/antcashmanager/android/util/CurrencyFormatterTest.kt`

## Nota sui test legacy
Nel repository esistono anche test piu vecchi con backtick nel nome.
Non usarli come standard per nuovi test o refactor.
Quando tocchi quei file:
- mantieni lo scopo originale del test;
- converti il naming al formato senza backtick se stai gia aggiornando il test in modo legittimo;
- non introdurre cambiamenti cosmetici scollegati dal task.

## Comandi consigliati
```bash
# Verifica rapida riferimenti MockK e test target
rg -n "mockk|coEvery|coVerify|every\(" androidApp shared

# Esempi di ViewModel test
rg -n "class .*ViewModel.*Test" androidApp/src/test/kotlin

# Esempi di repository/helper test
rg -n "class .*(Repository|Helper|Parser|Formatter).*Test" shared androidApp/src/test/kotlin
```

## Criteri di completamento
Un task di unit testing e completo solo quando:
- il test ha uno scopo esplicito e verificabile;
- il nome del test segue `method_shouldExpectedBehavior_whenCondition` senza backtick;
- MockK e usato correttamente quando ci sono collaboratori;
- `ViewModel` e classi con logica reale sono coperti quando pertinenti al task;
- i file modificati hanno import puliti e package corretto;
- i test nuovi o aggiornati sono coerenti con Android/KMP e con i pattern del repository.

