# ARCHITECTURE_GUIDELINES.md

## Architettura Clean per AntCashManager

- 3 layer: Presentation (androidApp) → Domain (shared/commonMain) → Data (shared/androidMain)
- Presentation dipende solo da Domain
- Domain: solo Kotlin puro, nessuna dipendenza Android
- Data: implementa le interfacce del Domain
- Organizzazione per feature (package-by-feature)

### Pattern principali
- UseCase → ViewModel → State → Screen
- Ogni UseCase accetta dispatcher, restituisce Result<T>, implementa solo execute()
- ViewModel: StateFlow pubblico, MutableStateFlow privato, consuma Result con onSuccess/onFailure
- UI: solo in composable, nessuna logica di business

### Best practice
- Tutte le stringhe user-facing in strings.xml (5 lingue)
- Componenti UI riutilizzabili in ui/components/
- MaterialTheme per colori, tipografia, spacing
- Logger: usa Kermit
- Test: fake repository, mantieni scopo originale, naming chiaro

---

Consulta anche copilot-instructions.md e copilot-prompt.md per dettagli operativi.
