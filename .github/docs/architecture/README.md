# Architecture Documentation

Documentazione sulla struttura e architettura della app AntCashManager.

## 📄 File in questa cartella

### ARCHITECTURE_OVERVIEW.md
**3-layer Clean Architecture overview**

Contenuto:
- Struttura a 3 layer (Presentation → Domain → Data)
- Dependency flow (come i layer dipendono uno dall'altro)
- Feature implementation flow
- Component structure (Screens, ViewModels, UseCases, Repositories)
- Widget layer (Glance API per home screen widgets)

**Usa quando**:
- Devi comprendere come è strutturata la app
- Devi implementare una nuova feature
- Devi capire dove mettere il codice (quale layer?)
- Devi comprendere le dipendenze tra i moduli

## 🎯 Concetti Chiave

### Clean Architecture (3 Layer)
```
Presentation (androidApp)  →  Domain (shared/commonMain)  →  Data (shared/androidMain)
```

- **Presentation**: UI, ViewModels, Navigation, DI wiring (Koin)
- **Domain**: Business logic (UseCases), Models, Repository interfaces, Exceptions (puro Kotlin)
- **Data**: Database (Room), DataStore, Repository implementations

### Golden Rule
> Dependency flows DOWN only. Presentation → Domain → Data. No backward dependencies.

### Package-by-Feature
Il codice è organizzato per feature, non per tipo tecnico:
```
shared/src/commonMain/domain/
├── transaction/          # Feature: Transaction management
│   ├── model/
│   ├── usecase/
│   └── repository/       # Interface only
├── category/             # Feature: Category management
├── chart/
└── settings/
```

## 📚 Additional Resources

- Vedi [AGENTS.md](../../../AGENTS.md) per dettagli su UseCase patterns, ViewModel patterns, Compose patterns
- Vedi [development/copilot-instructions.md](../development/copilot-instructions.md) per le convenzioni di coding
- Vedi [testing/](../testing/) per strategie di testing per ogni layer

**Last Updated**: 2026-08-12
