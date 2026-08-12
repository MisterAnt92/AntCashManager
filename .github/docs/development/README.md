# Development Guidelines

Regole, convenzioni e best practices per sviluppare in AntCashManager.

## 📄 File in questa cartella

### copilot-instructions.md
**Standard obbligatori di sviluppo**

Contenuto:
- Architettura e struttura (Clean Architecture 3-layer)
- Convenzioni Kotlin/Android (val/var, safe calls, line length, naming)
- Regole trasversali (testing, i18n, state management)
- DI (Koin), composables, ViewModel patterns
- Testing obbligatorio (unit test con MockK, coverage targets)
- Analytics e logging (con Kermit)
- Comment policy e code review standards

**Usa quando**:
- Stai scrivendo nuovo codice e vuoi seguire gli standard
- Ti serve una reference veloce sulle regole del progetto
- Devi capire come fare qualcosa nel modo "corretto" del progetto

### copilot-prompt.md
**Prompt di sistema per LLM**

Istruzioni di alto livello per mantenere coerenza del codice generato.

### CONVERSION_GUIDE.md
**Come convertire codice legacy alla Clean Architecture**

Passo-passo per refactoring di codice vecchio:
1. Analizza la feature
2. Sposta la logica di business in UseCase
3. Rendi la UI dichiarativa (Compose)
4. Localizza tutte le stringhe
5. Testa la conversione
6. Verifica Clean Architecture

**Usa quando**:
- Devi refactorare codice vecchio
- Migri feature da un'architettura vecchia a Clean Architecture

## 🎯 Checklist Pre-Commit

Prima di committare codice, verifica:

- [ ] **Clean Architecture** rispettata (dependency flow corretto, layer separation)
- [ ] **Import** puliti (rimossi inutilizzati)
- [ ] **Test** scritti (unit test per UseCase/ViewModel, UI test per Screen se complesso)
- [ ] **i18n** - Tutte le stringhe in strings.xml (EN, IT, FR, DE, ES)
- [ ] **Naming** - Nomi descriptivi per classi/funzioni
- [ ] **Line length** ≤ 120 caratteri
- [ ] **No !!** (usar safe call o Elvis operator)
- [ ] **Logica** semplice e leggibile

## 📚 Additional Resources

- Vedi [architecture/](../architecture/) per Clean Architecture details
- Vedi [testing/](../testing/) per testing strategy e patterns
- Vedi [AGENTS.md](../../../AGENTS.md) per agenti specializzati e regole critiche

**Last Updated**: 2026-08-12
