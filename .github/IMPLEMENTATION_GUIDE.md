# IMPLEMENTATION_GUIDE.md

## Guida pratica per implementare feature in AntCashManager

1. **Crea la struttura per feature**
    - UseCase in shared/commonMain/domain/usecase/feature/
    - ViewModel, State, Screen in androidApp/ui/screen/feature/
    - Componenti UI in androidApp/ui/components/

2. **Rispetta la Clean Architecture**
    - Nessuna dipendenza inversa tra i layer
    - Usa solo Kotlin puro in Domain

3. **Pattern UseCase**
    - Estendi la base class corretta
    - Accetta dispatcher, restituisce Result<T>, implementa solo execute()
    - KDoc obbligatoria

4. **Pattern ViewModel**
    - StateFlow pubblico, MutableStateFlow privato
    - Consuma Result con onSuccess/onFailure
    - Logger: usa Kermit

5. **UI**
    - Solo composable, nessuna logica di business
    - Usa componenti esistenti
    - Tutte le stringhe in strings.xml (5 lingue)

6. **Testing**
    - Fake repository, mantieni scopo originale, naming chiaro
    - runTest(testDispatcher), @OptIn(ExperimentalCoroutinesApi::class)

---

Consulta anche copilot-instructions.md, copilot-prompt.md e ARCHITECTURE_GUIDELINES.md.
