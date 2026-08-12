# Agents README - AntCashManager

Indice rapido degli agenti specializzati disponibili nel repository.

## Obiettivo

Questa cartella raccoglie agenti dedicati a task ricorrenti del progetto `AntCashManager`, con focus
su Clean Architecture, Android, KMP e qualità del codice.

## Agenti disponibili

### `agent-usecase-pattern.agent.md` ⭐ NUOVO

**Scopo**

- Implementazione domain UseCase con Result<T> pattern, custom exceptions, dispatcher injection

**Usalo quando**

- devi creare un nuovo UseCase
- devi testare UseCase con MockK
- devi capire quando usare UseCase<P,R> vs NoParamsUseCase<R> vs ObservableUseCase

**Token savings**: Focused on UseCase only (~200 lines, -30% token cost)

### `agent-viewmodel-stateflow.agent.md` ⭐ NUOVO

**Scopo**

- ViewModel with StateFlow state management, Result consumption, logging with Kermit

**Usalo quando**

- devi creare un nuovo ViewModel
- devi gestire reactive state with Flow/StateFlow
- devi testare ViewModel con BaseUnitTest

**Token savings**: Focused on ViewModel/State (~150 lines, -25% token cost)

### `agent-compose-ui.agent.md` ⭐ NUOVO

**Scopo**

- Screen composables, component reuse, Material Design 3, Preview requirements, i18n

**Usalo quando**

- devi creare un nuovo Screen composable
- devi capire quali componenti riutilizzare
- devi implementare Preview correttamente

**Token savings**: Focused on UI/Compose (~250 lines, -30% token cost)

### `agent-android-clean-architecture.agent.md` (LEGACY - See New Agents Above)

**NOTA**: Questo file è stato refactor in 3 agent specializzati (vedi sopra).  
Consulta i 3 nuovi agent per guidance focalizzato su UseCase/ViewModel/UI.

**Ancora utile quando**

- devi una overview completa di tutti i pattern
- devi capire come i 3 layer interagiscono
- cerchi esempi di end-to-end feature implementation

### `agent-code-cleanup.agent.md`

**Scopo**

- pulizia sicura di codice e risorse inutilizzate

**Usalo quando**

- devi rimuovere import non usati
- devi verificare classi, variabili o risorse non referenziate
- devi fare cleanup senza cambiare il comportamento applicativo

### `agent-unit-tests-mockk.agent.md`

**Scopo**

- creazione e manutenzione degli unit test Kotlin/Android/KMP con MockK

**Usalo quando**

- devi scrivere o aggiornare test di `ViewModel`
- devi testare `UseCase`, helper, parser, formatter, mapper o repository con logica
- vuoi applicare lo standard test del progetto:
    - MockK come libreria di mocking principale
    - naming `method_shouldExpectedBehavior_whenCondition`
    - nessun backtick nei nomi dei test
    - mantenimento dello scopo originale del test

### `chat-agent.agent.md`

**Scopo**

- modalità conversazionale generica

**Usalo quando**

- serve solo supporto descrittivo o organizzativo, senza specializzazione forte

## Source set di test da rispettare

- `androidApp/src/test/kotlin` → `ViewModel` e logica Android host-side
- `shared/src/commonTest/kotlin` → logica KMP `commonMain`
- `shared/src/androidHostTest/kotlin` → repository/data host-side e logica JVM-specific

## Regole trasversali

- Leggi sempre `.gitignore` prima di analizzare file o directory.
- Non modificare file esclusi o generati.
- Non modificare mai `androidApp/google-services.json`.
- Mantieni package name e import coerenti nei file toccati.
- Applica sempre il flusso: analisi → pianificazione → implementazione → verifica → conferma.

## Esempi interni utili per i test

- `androidApp/src/test/kotlin/com/antcashmanager/android/ui/home/HomeViewModelMockkTest.kt`
- `androidApp/src/test/kotlin/com/antcashmanager/android/ui/settings/SettingsViewModelMockkTest.kt`
-
`shared/src/commonTest/kotlin/com/antcashmanager/domain/usecase/settings/TransactionsDateFilterStateUseCaseMockkTest.kt`
-
`shared/src/androidHostTest/kotlin/com/antcashmanager/data/repository/TransactionRepositoryImplTest.kt`

## Nota

Se aggiungi un nuovo agente, aggiorna anche:

- `.github/AGENT_USAGE.md`
- `.github/ai-assistant.yml`
- questa `README.md` se l'agente deve essere discoverable per il team

