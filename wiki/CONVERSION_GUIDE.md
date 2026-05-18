# PiggyBank Pro → AntCashManager Data Conversion

> 🔗 **[← Torna all'Indice Principale](./INDEX.md)** | **[README.md](./README.md)** | **[SCRIPT_CONVERSION_README.md](./SCRIPT_CONVERSION_README.md)**

---

## Scopo
Questa guida descrive la conversione dei dati esportati da PiggyBank Pro nel formato
`debug_initial_data.json` usato da AntCashManager.

## Informazioni Progetto

| Campo | Valore |
|---|---|
| App | `AntCashManager` |
| Versione | `1.4.6` |
| Package name (`applicationId`) | `com.sformica.ant_cashmanager` |
| Script ufficiale | `scripts/convert_to_debug_data.py` |

## Prerequisiti

- Python 3.8+
- File input JSON disponibile
- Repository locale in `/opt/src/GIT/app/AntCashManager`

## Uso Rapido

```bash
cd /opt/src/GIT/app/AntCashManager
python3 scripts/convert_to_debug_data.py
```

Comportamento di default:
- legge: `androidApp/src/main/assets/piggybankpro_data.json`
- scrive: `androidApp/src/main/assets/debug_initial_data.json`

Uso con input personalizzato:

```bash
cd /opt/src/GIT/app/AntCashManager
python3 scripts/convert_to_debug_data.py /path/to/input.json
```

## Mapping Dati

### Transactions
- `title`: fallback a `"Senza titolo"` se nullo/vuoto
- `amount`: mantiene segno e valore originali
- `type`: da `category_type` (`0=EXPENSE`, `1=INCOME`) con fallback sul segno importo
- `category`: da `category_name`
- `notes`: da `description`
- campi extra supportati: `payee`, `location`, `tags`, `isRecurring`, `recurrenceInterval`, `paymentType`

### Categories
- `type`: `0=EXPENSE`, `1=INCOME`
- `color`: conversione da formato `A:R:G:B` (es. `255:129:199:132`) a valore colore compatibile app
- `icon`: preservata
- `is_archived`: normalizzato a boolean

## Output

- File output: `androidApp/src/main/assets/debug_initial_data.json`
- Struttura: oggetto con array `transactions` e `categories`
- File sovrascritto a ogni esecuzione (operazione idempotente lato struttura)

## Verifica Rapida

1. Esegui script.
2. Verifica presenza file output.
3. Avvia compilazione Android per controllo integrazione assets:

```bash
cd /opt/src/GIT/app/AntCashManager
./gradlew :androidApp:mergeDebugAssets :androidApp:compileDebugKotlin --no-daemon
```

## Troubleshooting

- **Input non trovato**: verifica path e permessi.
- **JSON non valido**: valida il file input con un JSON validator.
- **Campi mancanti**: lo script applica fallback; controlla comunque la qualità del dataset sorgente.

## Riferimenti

- Guida script dettagliata: `wiki/SCRIPT_CONVERSION_README.md`
- Architettura progetto: `wiki/ARCHITECTURE_GUIDELINES.md`
