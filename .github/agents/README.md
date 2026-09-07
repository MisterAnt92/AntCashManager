# Agents — AntCashManager

Le regole trasversali (git, workflow step-by-step, token, architettura, i18n, testing) sono in **[AGENTS.md](../../AGENTS.md)**: unica fonte, letta sempre. Gli agent qui sotto aggiungono solo template e checklist del proprio layer.

| Task | Agent |
|---|---|
| Creare/modificare un UseCase o una domain exception | `agent-usecase-pattern.agent.md` |
| Creare/refactorare un ViewModel, Event o State (UDF) | `agent-viewmodel-stateflow.agent.md` |
| Creare/modificare Screen, `view/` o `ui/components/` | `agent-compose-ui.agent.md` |
| Scrivere o aggiornare unit test | `agent-unit-tests-mockk.agent.md` |
| Rimuovere codice/risorse non usati | `agent-code-cleanup.agent.md` |
| Feature completa (UseCase → VM → Screen → test) | i quattro agent sopra, in quest'ordine, uno step alla volta |

## Convenzioni per questa cartella

- Ogni file `*.agent.md` inizia con frontmatter YAML `description:` (richiesto per la discovery).
- Nessuna regola duplicata da `AGENTS.md`: linka la sezione (`AGENTS.md §7`).
- Template copiati da codice reale del repo, non inventati; cita il file sorgente.
- Se aggiungi un agent: aggiorna questa tabella e `.github/ai-assistant.yml`.
