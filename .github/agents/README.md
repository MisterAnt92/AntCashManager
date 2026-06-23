# Agents README - AntCashManager

Indice rapido degli agenti specializzati disponibili nel repository.

## Obiettivo
Questa cartella raccoglie agenti dedicati a task ricorrenti del progetto `AntCashManager`, con focus su Clean Architecture, Android, KMP e qualità del codice.

## Agenti disponibili

### `agent-android-clean-architecture.agent.md`
**Scopo**
- agente principale per implementazioni, refactor e verifiche architetturali

**Usalo quando**
- devi creare o modificare `UseCase`, `ViewModel`, `State`, `Screen`
- devi mantenere i boundary tra Presentation, Domain e Data
- devi allineare il codice alle convenzioni Android/KMP del progetto

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
- `shared/src/test/kotlin` → repository/data host-side e logica JVM-specific

## Regole trasversali
- Leggi sempre `.gitignore` prima di analizzare file o directory.
- Non modificare file esclusi o generati.
- Non modificare mai `androidApp/google-services.json`.
- Mantieni package name e import coerenti nei file toccati.
- Applica sempre il flusso: analisi → pianificazione → implementazione → verifica → conferma.

## Esempi interni utili per i test
- `androidApp/src/test/kotlin/com/antcashmanager/android/ui/home/HomeViewModelMockkTest.kt`
- `androidApp/src/test/kotlin/com/antcashmanager/android/ui/settings/SettingsViewModelMockkTest.kt`
- `shared/src/commonTest/kotlin/com/antcashmanager/domain/usecase/settings/TransactionsDateFilterStateUseCaseMockkTest.kt`
- `shared/src/test/kotlin/com/antcashmanager/data/repository/TransactionRepositoryImplTest.kt`

## Nota
Se aggiungi un nuovo agente, aggiorna anche:
- `.github/AGENT_USAGE.md`
- `.github/ai-assistant.yml`
- questa `README.md` se l'agente deve essere discoverable per il team

