Agent usage - AntCashManager
============================

Overview
--------
Questo repository contiene il file principale di istruzioni per l'AI agent in:

- `.github/agents/agent-android-clean-architecture.agent.md` (source-of-truth)
- `.github/agents/agent-code-cleanup.agent.md` (agent dedicato alla pulizia codice)
- `.github/agents/agent-unit-tests-mockk.agent.md` (agent dedicato alla creazione e manutenzione degli unit test)
- `.github/agents/README.md` (indice rapido degli agenti e dei casi d'uso)

Il file `.github/ai-assistant.yml` punta a quel Markdown e fornisce un punto di riferimento standard per gli strumenti.

Quick start (developer)
-----------------------
1. Leggi le regole in `.github/agents/agent-android-clean-architecture.agent.md`.
2. Quando generi codice, segui la checklist "Pre-Commit" definita in quel file.
3. Per verifiche rapide:

   ```zsh
   cd /opt/src/GIT/app/AntCashManager
   # Lista file .github
   ls -la .github

   # Verifica che il file di istruzioni sia presente
   test -f .github/agents/agent-android-clean-architecture.agent.md && echo "FOUND: agent instructions"

   # Verifica che il file di configurazione dell'assistente esista
   test -f .github/ai-assistant.yml && echo "OK: ai-assistant.yml present"

   # Cerca pattern chiave
   grep -nE "OBBLIGATORIO|REGOLA CRITICA|Pre-Commit Checklist" .github/agents/agent-android-clean-architecture.agent.md || true
   ```

Quando usare gli agenti specializzati
-------------------------------------
- `agent-android-clean-architecture`: per implementazioni e refactor che toccano layer, UseCase, ViewModel, Screen e convenzioni KMP.
- `agent-code-cleanup`: per pulizia sicura di import, simboli inutilizzati, directory vuote e risorse non referenziate.
- `agent-unit-tests-mockk`: per creare o aggiornare unit test di ViewModel, UseCase, helper/parser/formatter, mapper e repository con logica.

### Focus dell'agent unit test
- Usa **MockK** come libreria standard di mocking.
- Nei test host-side Android in `androidApp/src/test/kotlin`, usa `com.antcashmanager.android.BaseUnitTest` come base comune.
- Non duplicare nei singoli test `Dispatchers.setMain`, `Dispatchers.resetMain`, `StandardTestDispatcher()` o `runTest(testDispatcher)`: usa `runViewModelTest` e gli helper condivisi della base.
- I nuovi test **non devono usare backtick** nel nome del metodo.
- Naming obbligatorio: `method_shouldExpectedBehavior_whenCondition`.
- Mantieni sempre lo **scopo del test** anche quando aggiorni test esistenti.
- Copri ViewModel e classi con logica reale, in particolare helper, parser, formatter, mapper e repository.
- Rispetta i source set del progetto:
  - `androidApp/src/test/kotlin` per ViewModel e logica Android host-side
  - `shared/src/commonTest/kotlin` per logica KMP `commonMain`
  - `shared/src/androidHostTest/kotlin` per repository/data test host-side

How to propose changes
----------------------
1. Apri una branch feature/agent-updates
2. Modifica il file agente corretto in `.github/agents/` oppure aggiungi un nuovo agente specializzato se necessario
3. Apri una PR descrivendo le modifiche; linka esempi e motivazioni

Optional: pre-commit checks
---------------------------
Il repository include uno script di utilità `.github/pre-commit-check.sh` che può essere utilizzato localmente per verificare la presenza dei file di agent. Per usarlo come hook locale:

```zsh
# dalla root del repo
cp .github/pre-commit-check.sh .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit

# testare lo script
.git/hooks/pre-commit || echo "pre-commit check failed"
```

Notes
-----
- Se la tua installazione di Copilot/AI assistant richiede un file con nome diverso (es. `.github/copilot.yml` o `.copilot/instructions.md`), aggiungi un file con quel nome che punti allo stesso contenuto.
- Per enforcement condiviso, considera l'aggiunta di un job CI (GitHub Actions) che esegue i controlli su PR.
