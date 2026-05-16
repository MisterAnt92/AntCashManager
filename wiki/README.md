# Wiki - AntCashManager

Documentazione tecnica e operativa del progetto.

## Informazioni Progetto

| Campo | Valore |
|---|---|
| App name | `AntCashManager` |
| Versione corrente | `1.4.6` |
| Package name (`applicationId`) | `com.sformica.ant_cashmanager` |
| Namespace Android | `com.antcashmanager.android` |
| Moduli principali | `androidApp`, `shared` |

## Indice Documenti

### 1) Architettura
- File: `wiki/ARCHITECTURE_GUIDELINES.md`
- Contiene: Clean Architecture, pattern UseCase/ViewModel/State/Screen, checklist e anti-pattern.
- Quando usarlo: prima di modifiche strutturali o refactor.

### 2) Guida Implementativa (storico feature)
- File: `wiki/IMPLEMENTATION_GUIDE.md`
- Contiene: dettagli implementazione di skeleton loading e form transazioni esteso.
- Quando usarlo: per capire decisioni UI/UX introdotte in quella milestone.

### 3) Conversione dati PiggyBank Pro -> Debug
- File: `wiki/CONVERSION_GUIDE.md`
- Contiene: flusso rapido di conversione dati e mapping campi.
- Script ufficiale: `scripts/convert_to_debug_data.py`.

### 4) Script Conversione (dettaglio tecnico)
- File: `wiki/SCRIPT_CONVERSION_README.md`
- Contiene: schema input/output, validazioni, esempi log, codici errore.
- Quando usarlo: manutenzione o estensione script Python.

## Flusso Consigliato

1. Parti da `wiki/ARCHITECTURE_GUIDELINES.md` per verificare i vincoli architetturali.
2. Usa `wiki/CONVERSION_GUIDE.md` per conversioni rapide dati.
3. Consulta `wiki/SCRIPT_CONVERSION_README.md` per dettagli avanzati dello script.
4. Leggi `wiki/IMPLEMENTATION_GUIDE.md` solo se stai toccando l'area transazioni/skeleton descritta.

## Note di Manutenzione Wiki

- Mantieni allineati versione e package con `androidApp/build.gradle.kts`.
- Se cambi script di conversione, aggiorna sia `wiki/CONVERSION_GUIDE.md` sia `wiki/SCRIPT_CONVERSION_README.md`.
- Se cambi pattern architetturali, aggiorna prima `wiki/ARCHITECTURE_GUIDELINES.md`.

