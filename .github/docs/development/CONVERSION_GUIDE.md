# CONVERSION_GUIDE.md

## Conversione di codice legacy in AntCashManager

1. **Analizza la feature**
   - Identifica layer, dipendenze e responsabilità
2. **Sposta la logica di business in UseCase**
   - NO logica di business in ViewModel o UI
3. **Rendi la UI dichiarativa**
   - Usa Compose, componenti riutilizzabili, MaterialTheme
4. **Localizza tutte le stringhe**
   - Aggiorna strings.xml in tutte le lingue
5. **Testa la conversione**
   - Aggiorna/crea test, mantieni scopo originale
6. **Verifica la Clean Architecture**
   - Nessuna dipendenza inversa, package-by-feature

---

Consulta anche copilot-instructions.md, copilot-prompt.md, ARCHITECTURE_GUIDELINES.md, IMPLEMENTATION_GUIDE.md.
