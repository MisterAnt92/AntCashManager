Agent usage - AntCashManager
============================

Overview
--------
Questo repository contiene il file principale di istruzioni per l'AI agent in:

- `.github/agent-android-clean-architecture.md` (source-of-truth)

Il file `.github/ai-assistant.yml` punta a quel Markdown e fornisce un punto di riferimento standard per gli strumenti.

Quick start (developer)
-----------------------
1. Leggi le regole in `.github/agent-android-clean-architecture.md`.
2. Quando generi codice, segui la checklist "Pre-Commit" definita in quel file.
3. Per verifiche rapide:

   ```zsh
   cd /opt/src/GIT/app/AntCashManager
   # Lista file .github
   ls -la .github

   # Verifica che il file di istruzioni sia presente
   test -f .github/agent-android-clean-architecture.md && echo "FOUND: agent instructions"

   # Verifica che il file di configurazione dell'assistente esista
   test -f .github/ai-assistant.yml && echo "OK: ai-assistant.yml present"

   # Cerca pattern chiave
   grep -nE "OBBLIGATORIO|REGOLA CRITICA|Pre-Commit Checklist" .github/agent-android-clean-architecture.md || true
   ```

How to propose changes
----------------------
1. Apri una branch feature/agent-updates
2. Modifica `.github/agent-android-clean-architecture.md` o crea una versione pulita `.github/agent-android-clean-architecture.cleaned.md` se necessario
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

