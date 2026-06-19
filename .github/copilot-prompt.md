# Copilot Prompt per AntCashManager

Questo file serve come prompt locale per Copilot, ottimizzando la generazione di codice e suggerimenti per il progetto AntCashManager. Segui SEMPRE queste linee guida contestuali:

---

## Contesto Progetto
- App Android per gestione finanze personali
- Architettura Clean a 3 layer: Presentation (androidApp) → Domain (shared/commonMain) → Data (shared/androidMain)
- Kotlin Multiplatform (KMP), Jetpack Compose, nessun Hilt/Dagger (DI manuale)
- UI moderna, localizzazione in 5 lingue, componenti riutilizzabili

---

## Regole Chiave per Copilot
- Rispetta la Clean Architecture: nessuna dipendenza inversa tra i layer
- Organizza per feature, non per tipo tecnico
- Usa Kotlin idiomatico, safe call, val dove possibile
- NO logica di business nei composable, solo ViewModel e UseCase
- Tutte le stringhe user-facing in strings.xml (en, it, fr, de, es)
- Usa componenti UI esistenti in ui/components/ prima di crearne di nuovi
- Applica MaterialTheme per colori, tipografia e spacing
- Ogni UseCase accetta dispatcher, restituisce Result<T>, implementa solo execute()
- ViewModel: StateFlow pubblico, MutableStateFlow privato, consuma Result con onSuccess/onFailure
- Se una data class rappresenta lo stato di Screen/ViewModel (`<Feature>State`), mantienila nella classe/file State e non creare classi duplicate o `typealias`
- Logger: usa Kermit, mai println/Log
- Test: usa **MockK** come standard, fake repository solo quando aiutano con stato/Flow/cancellazione
- Test Android host-side: usa `com.antcashmanager.android.BaseUnitTest` come base comune e riusa `runViewModelTest`/`testDispatcher`
- Test: mantieni lo scopo originale, naming `method_shouldExpectedBehavior_whenCondition`, **senza backtick**
- Dai priorita ai test di `ViewModel`, helper, parser, formatter, mapper e repository con logica
- NO hardcoded string/color/font, NO runBlocking fuori dai test
- Pre-commit: import puliti, package corretto, checklist rispettata

---

## Suggerimenti Prompt
- Quando generi codice, pensa sempre a: layer corretto, riuso componenti, localizzazione, testabilità
- Se devi scegliere tra più soluzioni, preferisci quella più pulita, idiomatica e facilmente testabile
- Consulta i file in wiki/ e le implementazioni DisplayScreen, SettingsScreen, HomeScreen per esempi
- Per task specializzati, consulta gli agenti in `.github/agents/`, in particolare `agent-unit-tests-mockk.agent.md` per gli unit test

---

## Esempio di struttura file
- UseCase: shared/commonMain/domain/usecase/feature/NomeUseCase.kt
- ViewModel: androidApp/ui/screen/feature/NomeViewModel.kt
- State: androidApp/ui/screen/feature/NomeState.kt
- Screen: androidApp/ui/screen/feature/NomeScreen.kt
- Componenti UI: androidApp/ui/components/
- Test ViewModel: androidApp/src/test/
- Test domain commonMain: shared/commonTest/
- Test repository/data host-side: shared/src/test/

---

## Nota finale
Quando in dubbio, scegli sempre la soluzione più pulita, manutenibile e testabile. La qualità del codice ha priorità sulla velocità.
