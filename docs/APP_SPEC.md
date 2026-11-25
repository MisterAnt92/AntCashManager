# AntCashManager — App Spec

## Versione corrente: v1.9.3  
_Aggiornato: 2026-11-25_

---

## SDD Process

Seguire questo ciclo per ogni nuova feature:

1. Scegli una feature `[PLANNED]` da questa spec
2. Pianifica in plan mode (questo file + agenti in `.github/agents/`)
3. Implementa step-by-step — compile check a ogni gruppo logico (`compileFullDebugKotlin`)
4. Verifica visiva nell'app reale: golden path + edge case
5. Segna `[DONE]` in questa spec e incrementa la versione header
6. Se hai aggiunto componenti UI: aggiorna l'inventario in `.github/agents/agent-compose-ui.agent.md`

---

## Core features

- `[DONE]` CRUD transazioni (tipo, importo, categoria, payee, location, note, tag, ricorrenza, buoni pasto)
- `[DONE]` Multi-profilo con avatar, colore, condivisione transazioni
- `[DONE]` Categorie per profilo (default + personalizzate, nascoste, riordinabili, non-nascondibili default, ripristino default)
- `[DONE]` Receipt scan OCR (ML Kit, 3 step: capture → processing → review)
- `[DONE]` Backup/Restore locale + Google Drive (AES-GCM v4, WorkManager, frequenze configurabili)
- `[DONE]` 14 lingue (en, it, fr, de, es, hi, ja, ko, pl, ru, uk, zh, zh-rTW)
- `[DONE]` Material You (dynamic color), multi-tema (light/dark/system), accessibilità
- `[DONE]` Foldable support (split-pane Home/Transactions/Categories) + tablet grid layouts
- `[DONE]` Widget per saldo rapido

---

## Analytics & comparison

- `[DONE]` Firebase Analytics con whitelist eventi + GDPR consent
- `[DONE]` SpendingForecastCard: media 3 mesi + proiezione fine mese (attiva dal 3° giorno)
- `[DONE]` Period comparison: delta% income/expense + TrendDirection reale (UP/DOWN/NEUTRAL, soglia 5%)
- `[DONE]` TrendBadge su IncomeExpenseRow (chip colorato con freccia + percentuale)
- `[DONE]` TrendBadge enhancement: valore assoluto delta + animazione `animateContentSize` + contentDescription accessibilità
- `[DONE]` Savings rate / obiettivi: SavingsRateCard + HomeState (savingsRate, savingsAmount, savingsGoalRate) + SettingsRepository.getSavingsGoalRate()
- `[PLANNED]` Budget per categoria: Room entity, 3 UseCase, UI chip + progress bar su CategoriesScreen
- `[PLANNED]` Analytics eventi trend/budget (whitelist in `AnalyticsConstants`)

---

## Settings & display

- `[DONE]` Display settings: tema, lingua, font scale, reduce motion, high contrast
- `[DONE]` Material You toggle (dynamic color)
- `[DONE]` Mask amounts (privacy)
- `[DONE]` Transaction icon display (trend / category / none)
- `[DONE]` Payment type breakdown toggle
- `[DONE]` Quick insights card toggle
- `[PLANNED]` Savings goal rate setting (default 20%)

---

## Data & sicurezza

- `[DONE]` Encryption AES-GCM per note/payee sensibili
- `[DONE]` Encrypted search (load-decrypt-filter in-memory quando encryption ON)
- `[DONE]` Database Room v9→v10 migration
- `[PLANNED]` Database migration v10→v11 (category_budgets table)

---

## Note architetturali

- KMP Android-only: `commonMain` (domain, pure Kotlin), `androidMain` (Room/DataStore), `androidApp` (UI/Compose)
- Tutti i ViewModel seguono il pattern UDF: `AndroidBaseViewModel<E,S>`, `emitEvent()`, `handleEvent()`
- Navigazione type-safe via `AppRoute` + `NavigationExtensions.kt`
- DI Koin: `dataModule` / `useCaseModule` / `presentationModule`
- Testing: MockK only, `BaseUnitTest`, `BaseUseCaseTest`, `FakeXxxRepository`
