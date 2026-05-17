# Wiki - AntCashManager

Documentazione tecnica e operativa del progetto.

## Accesso rapido come risorse esterne

La wiki è pubblicata anche come insieme di risorse statiche tramite GitHub Pages, così i file possono essere aperti direttamente da browser senza passare dalla vista repository.

### Indice esterno
- `docs/wiki/index.html`
- URL pubblico atteso: `https://misterant92.github.io/AntCashManager/wiki/`

### Privacy Policy pubbliche
- Inglese: `https://misterant92.github.io/AntCashManager/wiki/privacy-policy.html`
- Tedesco: `https://misterant92.github.io/AntCashManager/wiki/privacy-policy-de.html`
- Francese: `https://misterant92.github.io/AntCashManager/wiki/privacy-policy-fr.html`
- Spagnolo: `https://misterant92.github.io/AntCashManager/wiki/privacy-policy-es.html`

> Nota: per rendere i file effettivamente raggiungibili dall'esterno, GitHub Pages deve essere abilitato sul branch `develop` con cartella `/docs`.

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

### 5) Privacy Policy (HTML)
- File: `wiki/privacy-policy.html`
- Contiene: policy privacy ufficiale con focus usage-only analytics (riferimento inglese).
- Quando usarlo: riferimento da README/app store/documentazione esterna.
- Localizzazioni: `wiki/privacy-policy-de.html`, `wiki/privacy-policy-fr.html`, `wiki/privacy-policy-es.html`.

## Flusso Consigliato

1. Parti da `wiki/ARCHITECTURE_GUIDELINES.md` per verificare i vincoli architetturali.
2. Usa `wiki/CONVERSION_GUIDE.md` per conversioni rapide dati.
3. Consulta `wiki/SCRIPT_CONVERSION_README.md` per dettagli avanzati dello script.
4. Leggi `wiki/IMPLEMENTATION_GUIDE.md` solo se stai toccando l'area transazioni/skeleton descritta.

## Note di Manutenzione Wiki

- Mantieni allineati versione e package con `androidApp/build.gradle.kts`.
- Se cambi script di conversione, aggiorna sia `wiki/CONVERSION_GUIDE.md` sia `wiki/SCRIPT_CONVERSION_README.md`.
- Se cambi pattern architetturali, aggiorna prima `wiki/ARCHITECTURE_GUIDELINES.md`.
- Se aggiungi nuove pagine HTML pubbliche, copia anche il file corrispondente dentro `docs/wiki/`.

## Privacy - Usage-Only Analytics

Gli analytics custom devono tracciare solo utilizzo dell'app (no contenuti personali).

Eventi consentiti:

- `transactions_filter_applied`
- `transactions_filter_cleared`
- `transaction_add_opened`
- `receipt_scan_opened`
- `transaction_form_opened`
- `transaction_form_cancelled`
- `transaction_submit_success`
- `backup_create_requested`
- `backup_file_saved`
- `backup_file_save_error`
- `restore_open_requested`
- `restore_file_selected`
- `delete_all_data_confirmed`
- `reset_preferences_confirmed`

Evento standard Firebase consentito:

- `screen_view`

Non consentito inviare in analytics:

- testo libero utente (query, note, titoli)
- dati transazione dettagliati (importo descrittivo, payee, location, tags)
- email/identificatori personali
- messaggi errore raw o stacktrace
