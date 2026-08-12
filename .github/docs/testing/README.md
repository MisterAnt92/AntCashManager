# Testing Documentation

Strategie, patterns e guide per testing in AntCashManager.

## 📄 File in questa cartella

### INSTRUMENTATION_TESTS.md
**UI/Integration Test Guide**

Contenuto:
- Differenza tra Unit Test (src/test) e Instrumentation Test (src/androidTest)
- Struttura test (location, naming conventions)
- Test disponibili (Navigation, Home, Transactions, Charts, Categories, Settings)
- Come scrivere instrumentation test
- Framework: AndroidJUnit4 + createAndroidComposeRule()

**Usa quando**:
- Devi scrivere UI test
- Vuoi testare navigazione tra screen
- Vuoi testare interazioni reali (click, scroll, input)
- Vuoi testare state preservation

### COVERAGE_STRATEGY.md
**Overall Test Coverage Strategy**

Contenuto:
- Strategia di coverage per layer (Presentation, Domain, Data)
- Target di coverage per ogni feature
- Phases di implementazione (Phase 1: Domain, Phase 2: Presentation, Phase 3: Integration)
- Metrics e monitoraggio

**Usa quando**:
- Devi capire la strategia generale di testing
- Vuoi sapere quali file/layer hanno priority di testing
- Vuoi capire come è tracciato il coverage

### TEST_COVERAGE_ACTION_PLAN.md
**Piano d'azione per raggiungere coverage target**

Contenuto:
- Fase 1: Coverage per Domain layer (UseCase, mapper, formatter, parser)
- Fase 2: Coverage per Presentation layer (ViewModel, State, UI logic)
- Fase 3: Integration tests (navigazione, flussi completi)
- Task breakdown per feature
- Timeline e priorità

**Usa quando**:
- Devi sapere quali test scrivere per una feature
- Vuoi capire il piano per le prossime settimane

### TEST_COVERAGE_SUMMARY.md
**Stato attuale del coverage**

Snapshot dello stato di coverage per feature:
- Quanti test per ogni feature
- Qual è il coverage target
- Cosa manca

**Usa quando**:
- Vuoi un quick reference dello stato di testing
- Devi sapere quali feature hanno coverage insufficiente

## 🎯 Testing Stack (KMP)

```
compose.uiTest v2
+ kotlinx-coroutines-test
+ MockK (NO Roboelectric/Mockito)
+ JUnit
+ org.junit
```

## 📚 Patterns Verificati

### Unit Test (src/test)
- Location: `shared/src/androidTest/kotlin/...`
- Base: `BaseUnitTest` + Fake repos
- Framework: JUnit + MockK
- Speed: ~ms

### Instrumentation Test (src/androidTest)
- Location: `androidApp/src/androidTest/kotlin/...`
- Rule: `createAndroidComposeRule<ComponentActivity>()`
- Runner: AndroidJUnit4
- Speed: ~secondi (test reali su device/emulator)

### Hybrid Strategy
- Unit test per domain logic
- Instrumentation test per UI/navigation
- Integration test per flussi critici

## 📚 Additional Resources

- Vedi [AGENTS.md](../../../AGENTS.md) → agent-unit-tests-mockk per pattern di unit test
- Vedi [development/](../development/) per standard di coding durante test

**Last Updated**: 2026-08-12
