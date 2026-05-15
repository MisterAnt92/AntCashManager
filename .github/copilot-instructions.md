# Copilot Instructions per AntCashManager

## Obiettivo
Ottimizza l'efficienza, la qualità e la manutenibilità del codice seguendo Clean Architecture, best practice Android, e principi KMP. Applica SEMPRE queste regole quando generi, modifichi o suggerisci codice per questo progetto.

## Metodologia di lavoro (obbligatoria)
- Esegui SEMPRE task e procedimenti **step by step**: analisi → pianificazione → implementazione → verifica → conferma.

---

## 1. Architettura e Struttura
- **Segui la Clean Architecture a 3 layer:**
  - Presentation (androidApp) → Domain (shared/commonMain) → Data (shared/androidMain)
- **Presentation** dipende SOLO da Domain.
- **Domain**: solo Kotlin puro, NO dipendenze Android.
- **Data**: implementa le interfacce del Domain.
- **Organizza per feature** (package-by-feature), non per tipo tecnico.

---

## 2. Convenzioni Kotlin/Android
- Usa Kotlin idiomatico, preferisci `val` a `var`.
- Evita `!!`, usa safe call (`?.`) e Elvis (`?:`).
- Limita la lunghezza delle linee a 120 caratteri.
- Usa nomi descrittivi per classi/funzioni, brevi per variabili locali.
- Rimuovi SEMPRE import non usati e verifica il package name.
- Commenta solo logica complessa, preferisci codice autoesplicativo.
- Usa trailing comma in liste multi-linea.

## 2.1 Regole Trasversali (Sempre Attive)
- ✅ Esegui SEMPRE task e procedimenti **step by step** (analisi → pianificazione → implementazione → verifica → conferma)
- ✅ Quando crei o aggiorni unit test, **mantieni lo scopo originale del test** anche se cambia il codice implementativo
- ✅ Quando crei o modifichi classi/file, **rimuovi sempre gli import non necessari** prima di chiudere la modifica
- ✅ Quando crei o modifichi classi/file, **verifica sempre il `package name`** e correggilo se non corrisponde alla directory reale
- ✅ Negli UseCase, usa SEMPRE `Result<T>` per restituire il valore desiderato o una custom exception di dominio, nel rispetto della Clean Architecture
- ✅ Se una feature ha piu costanti condivise (tag, timeout, default, chiavi, pattern), crea SEMPRE una classe/object `<Feature>Constants` nel package della feature
- ✅ Se crei una `data class` di feature, crea/usa SEMPRE il sotto-package `model` interno alla feature (es. `ui/screen/settings/model/SettingsUiModel.kt`)
- ✅ Se una `data class` rappresenta lo **State** di Screen/ViewModel, deve rimanere nel file/classe `...State` della feature: non creare classi aggiuntive o `typealias` per quello stato
- ✅ **Keep simple**: privilegia soluzioni semplici, leggibili e facili da mantenere
- ✅ **Evita over engineering**: implementa solo ciò che serve al requisito attuale (YAGNI)
- ✅ Esegui la build **solo al termine** delle modifiche/implementazioni, salvo necessità specifiche di diagnosi immediata, per velocizzare il flusso, ridurre chiamate inutili e ottimizzare l'uso delle risorse (tempo/token)

---

## 3. Android & Compose
- Usa Jetpack Compose per la UI.
- Gestisci la navigazione con NavController e definisci le route in modo chiaro.
- Usa ViewModel per la gestione dello stato, NO logica di business nei composable.
- Implementa la lifecycle awareness (es. `LaunchedEffect`).
- Tutte le stringhe user-facing DEVONO essere in `strings.xml` (5 lingue: en, it, fr, de, es).
- E' VIETATO hardcodare stringhe: usa sempre `stringResource(R.string.*)`.
- Usa SEMPRE i componenti UI esistenti in `ui/components/` prima di crearne di nuovi.
- Applica MaterialTheme per colori, tipografia e spacing.

---

## 4. UseCase & Result Pattern
- Ogni UseCase deve:
  - Estendere la base class corretta (`BaseUseCase`, `FlowUseCase`, ecc.)
  - Accettare un `CoroutineDispatcher` nel costruttore (default: `Dispatchers.Default`)
  - Restituire SEMPRE `Result<T>` (NO eccezioni lanciate direttamente)
  - Usare `Result<T>` per incapsulare sempre il valore desiderato o una custom exception di dominio, nel rispetto della Clean Architecture
  - Implementare SOLO `execute()`, MAI `invoke()`
  - KDoc obbligatoria
- Le custom exception vanno SOLO in `shared/commonMain/domain/exception/`

---

## 5. ViewModel
- Espone solo StateFlow pubblico, MutableStateFlow privato.
- Consuma `Result<T>` con `onSuccess`/`onFailure`.
- Usa Kermit Logger per logging.
- NO logica di business, NO riferimenti a Context.
- Se una classe usa poche costanti locali e private, puoi tenerle nel `companion object`.
- Se le costanti sono condivise da piu file della stessa feature, crea SEMPRE `<Feature>Constants` nel package della feature (es. `SettingsConstants` in `ui/screen/settings/`).
- Usa `companion object` solo per costanti realmente private alla singola classe.
- Usa pattern `activeJob?.cancel()` per operazioni annullabili.

---

## 5.1 Regola Model per Feature
- Le `data class` di Presentation devono stare nel package `model` della feature.
- Eccezione: la `data class` che definisce lo stato UI (`<Feature>State`) resta nel file/classe `State` della feature.
- Per lo stato UI di Screen/ViewModel non usare classi duplicate o `typealias`.
- Esempio: per la feature Settings usa `androidApp/.../ui/screen/settings/model/`.
- Evita di definire nuove `data class` dentro file Screen/ViewModel se sono riutilizzabili o parte del contratto UI.

---

## 6. Testing
- Ogni feature DEVE avere test (happy path, errori, cancellazione).
- Usa fake repository, NON Mockito.
- Mantieni lo scopo originale dei test quando aggiorni.
- Usa `@OptIn(ExperimentalCoroutinesApi::class)` e `runTest(testDispatcher)`.
- Test naming: `method_shouldExpectedBehavior_whenCondition`.

---

## 7. Performance & Sicurezza
- Minimizza allocazioni in hot path.
- Proteggi dati sensibili (EncryptedSharedPreferences).
- Evita memory leak: ViewModel non deve mantenere riferimenti a Context.

---

## 8. Anti-pattern da EVITARE
- Logica di business nei composable.
- Stringhe hardcoded.
- Color/font hardcoded.
- UseCase senza dispatcher injection.
- Custom exception fuori dal domain layer.
- Import non usati o package errato.
- runBlocking fuori dai test.
- Override di `invoke()` nelle subclass di UseCase.
- Inghiottire `CancellationException` senza rilanciarla.
- Molte costanti duplicate in piu file senza `<Feature>Constants`.
- `data class` di feature sparse fuori da `.../model/`.
- Duplicare lo state di Screen/ViewModel con classi aggiuntive o `typealias`.

---

## 9. Pre-commit Checklist
- [ ] Import puliti e package name corretto
- [ ] Clean Architecture rispettata
- [ ] UseCase con dispatcher injection e Result
- [ ] Custom exception solo nel domain layer
- [ ] ViewModel senza logica di business
- [ ] State immutabile
- [ ] UI solo in composable, NO logica
- [ ] Tutte le stringhe in `strings.xml` (5 lingue)
- [ ] Componenti UI esistenti riutilizzati
- [ ] Test inclusi e aggiornati
- [ ] Logger Kermit usato
- [ ] Limiti di lunghezza rispettati
- [ ] KDoc per API pubbliche
- [ ] @Preview per composable principali
- [ ] Spacing consistente (8.dp tra card)
- [ ] Costanti feature centralizzate in `<Feature>Constants` (no magic numbers/string)
- [ ] Data class di feature collocate in `.../model/`
- [ ] La `data class` di stato (`<Feature>State`) resta nello State file, senza classi duplicate o `typealias`

---

## 10. Riferimenti
- Consulta i file in `wiki/` e le implementazioni di esempio (DisplayScreen, SettingsScreen, HomeScreen) per dubbi architetturali.
- In caso di incertezza, preferisci codice pulito, testabile e manutenibile.
