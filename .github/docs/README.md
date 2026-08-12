# AntCashManager Documentation

Documentazione organizzata per sviluppatori e agenti AI che lavorano nel progetto.

## 📑 Indice delle Cartelle

### 📐 [architecture/](./architecture/)
**Guida all'architettura del progetto**
- `ARCHITECTURE_OVERVIEW.md` - 3-layer Clean Architecture, dependency flow, feature layout
- Regole fondamentali di design della app

**Usa quando**: Devi comprendere la struttura della app, implementare nuove feature, capire le dipendenze tra layer

### 🛠️ [development/](./development/)
**Regole, convenzioni e guide di sviluppo**
- `copilot-instructions.md` - Standard coding obbligatori (Clean Architecture, Kotlin idioms, testing)
- `copilot-prompt.md` - Prompt di sistema per LLM (istruzioni generali)
- `CONVERSION_GUIDE.md` - Come convertire codice legacy alla Clean Architecture

**Usa quando**: Devi scrivere nuovo codice, seguire i standard del progetto, convertire codice legacy

### 🧪 [testing/](./testing/)
**Test strategy, patterns e guide**
- `INSTRUMENTATION_TESTS.md` - UI/integration test patterns (androidTest)
- `COVERAGE_STRATEGY.md` - Strategia di test coverage complessiva
- `TEST_COVERAGE_ACTION_PLAN.md` - Piano d'azione per raggiungere coverage target
- `TEST_COVERAGE_SUMMARY.md` - Stato attuale del coverage per feature

**Usa quando**: Devi scrivere unit test, test UI, capire la strategia di testing

### 🚀 [deployment/](./deployment/)
**Guide per release, debug e deployment**
- `DEPLOY_NOW.md` - Checklist e procedure per il deploy
- `DEBUG_SYMBOLS_GUIDE.md` - Come configurare simboli di debug per stack trace leggibili

**Usa quando**: Devi fare un deploy, debuggare crash, analizzare stack trace

### ✨ [features/](./features/)
**Feature-specific documentation**
- `RECEIPT_SCAN_FEATURE.md` - Implementazione receipt scanning (ML Kit OCR)

**Usa quando**: Devi lavorare su feature specifiche, capire implementazione di una feature

### 📚 [reference/](./reference/)
**Materiale di riferimento**
- `.gitignore.reference.md` - Spiegazione della .gitignore

**Usa quando**: Hai dubbi su cosa ignorare in git, quali file sono importanti

---

## 🔗 Link Utili

- **[AGENTS.md](../../AGENTS.md)** - Guida principale per agenti AI (regole critical, best practices)
- **[README.md](../../README.md)** - Overview app e info di progetto
- **[IMPROVEMENT_ROADMAP_STATUS.md](../../IMPROVEMENT_ROADMAP_STATUS.md)** - Roadmap corrente e status settimanale
- **[SETTINGS_CONSOLIDATION_MIGRATION.md](../../SETTINGS_CONSOLIDATION_MIGRATION.md)** - Guida alla migrazione settings

---

## 📋 File Manager

- **File Utili in Root**: AGENTS.md, README.md, IMPROVEMENT_ROADMAP_STATUS.md, SETTINGS_CONSOLIDATION_MIGRATION.md
- **File Utili in .github/agents/**: agent-*.agent.md (specializzati per task ricorrenti)
- **File Eliminati**: Session summari, week reports, completion summaries (storage cleanup)

---

## ✅ Checklist per Nuovi Developer

1. Leggi `architecture/ARCHITECTURE_OVERVIEW.md` per capire la struttura
2. Leggi `development/copilot-instructions.md` per gli standard di codice
3. Per test: `testing/INSTRUMENTATION_TESTS.md` (UI) o AGENTS.md → agent-unit-tests-mockk (unit)
4. Per deploy: `deployment/DEPLOY_NOW.md`
5. Consulta `AGENTS.md` per le regole críticas (no auto-commit, etc.)

---

**Last Updated**: 2026-08-12
