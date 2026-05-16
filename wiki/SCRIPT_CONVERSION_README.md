# Script di Conversione Dati - AntCashManager

## Panoramica
Script unificato per convertire dati di backup/PiggyBank Pro nel formato
`debug_initial_data.json` compatibile con AntCashManager.

## Informazioni Progetto

| Campo | Valore |
|---|---|
| App | `AntCashManager` |
| Versione | `1.4.6` |
| Package name (`applicationId`) | `com.sformica.ant_cashmanager` |
| Script | `scripts/convert_to_debug_data.py` |

## Prerequisiti

- Python 3.8+
- File JSON di input valido
- Repository locale disponibile

## Utilizzo

### Modalità standard (input di default)

```bash
cd /opt/src/GIT/app/AntCashManager
python3 scripts/convert_to_debug_data.py
```

- Input: `androidApp/src/main/assets/piggybankpro_data.json`
- Output: `androidApp/src/main/assets/debug_initial_data.json`

### Modalità custom (input esplicito)

```bash
cd /opt/src/GIT/app/AntCashManager
python3 scripts/convert_to_debug_data.py /path/to/input.json
```

- Output sempre: `androidApp/src/main/assets/debug_initial_data.json`

## Formato Supportato

### Input
Lo script supporta JSON con struttura simile a:
```json
{
  "records": [
    {
      "id": 1,
      "title": "Descrizione",
      "value": -15.50,
      "category_name": "Category",
      "category_type": 0,
      "datetime": 1708096800000,
      "description": "Note",
      "payee": "Beneficiario",
      "location": "Luogo",
      "tags": "tag1,tag2",
      "isRecurring": false,
      "paymentType": "CASH",
      "recurrenceInterval": ""
    }
  ],
  "categories": [
    {
      "id": 1,
      "name": "Food",
      "category_type": 0,
      "color": "255:255:107:107",
      "icon": "🍔",
      "is_archived": 0
    }
  ]
}
```
### Output (Transaction)
```kotlin
data class Transaction(
    val id: Long,
    val title: String,
    val amount: Double,
    val category: String,
    val type: TransactionType,
    val timestamp: Long,
    val notes: String = "",
    val payee: String = "",
    val location: String = "",
    val isRecurring: Boolean = false,
    val tags: String = "",              // comma-separated
    val recurrenceInterval: String = "",
    val paymentType: PaymentType = PaymentType.ELECTRONIC,
    val categoryIcon: String = "",
    val categoryColor: Long = 0xFF90A4AE,
)
```

## Caratteristiche Principali

### 1) Gestione Tag
- Input: lista array, stringa comma-separated, null
- Output: stringa comma-separated pulita
- Esempio: `"tags": "food,dinner,friends"`

### 2) Gestione Null/Vuoti
- Non inserisce stringhe letterali `"null"`
- Campi opzionali rimangono vuoti `""`
- Fallback intelligenti (es. titolo → "Senza titolo")

### 3) PaymentType
- Se presente e valido: utilizza il valore
- Se mancante o invalido: assegna random (50% ELECTRONIC, 30% CASH, 20% MEAL_VOUCHERS)
- Seed fisso (42) per conversioni riproducibili

### 4) Lookup Categorie
- Associa automaticamente `categoryIcon` e `categoryColor` dalle categorie
- Se categoria non trovata: colore di default (grigio 0xFF90A4AE)

### 5) Conversione Colori
- Input: formato PiggyBank `"255:129:199:132"` (A:R:G:B)
- Output: Long hex `0xAARRGGBB` (es. `4294929259` → `0xFFFF6B6B`)
- Default se mancante: `0xFF90A4AE`

### 6) Tipo Transazione
- Determinato da `category_type`: 0 = EXPENSE, 1 = INCOME
- Fallback: segno dell'importo (negativo = EXPENSE, positivo = INCOME)

## Esempi Output

### Transazione completa
```json
{
  "id": 1,
  "title": "Pizza",
  "amount": -15.5,
  "category": "Food",
  "type": "EXPENSE",
  "timestamp": 1708096800000,
  "notes": "Cena con amici",
  "payee": "Pizzeria Roma",
  "location": "Via Roma 1",
  "isRecurring": false,
  "tags": "food,dinner,friends",
  "recurrenceInterval": "",
  "paymentType": "CASH",
  "categoryIcon": "🍔",
  "categoryColor": 4294929259
}
```

### Transazione minima
```json
{
  "id": 2,
  "title": "Senza titolo",
  "amount": 2000.0,
  "category": "Salary",
  "type": "INCOME",
  "timestamp": 1708010400000,
  "notes": "",
  "payee": "",
  "location": "",
  "isRecurring": false,
  "tags": "",
  "recurrenceInterval": "",
  "paymentType": "CASH",
  "categoryIcon": "💰",
  "categoryColor": 4283549286
}
```

## Validazioni Eseguite
Lo script effettua le seguenti validazioni:

1. ✅ File di input esiste
2. ✅ JSON è valido
3. ✅ Transazioni senza valore vengono saltate
4. ✅ Categorie vengono normalizzate
5. ✅ Campi opzionali sono puliti da null/vuoti

## Output Log
Esempio di output di esecuzione:
```
📖 Lettura dati da: /path/to/input.json
📊 Trovate 2 transazioni e 2 categorie
✅ Convertite 2 transazioni valide
✅ Convertite 2 categorie
✅ Output scritto in: androidApp/src/main/assets/debug_initial_data.json
📋 Schema Transaction supportato:
   - id, title, amount, category, type
   - timestamp, notes, payee, location
   - isRecurring, tags (comma-separated), recurrenceInterval
   - paymentType (ELECTRONIC|CASH|MEAL_VOUCHERS)
   - categoryIcon, categoryColor
```

## Codici Errore

| Codice | Significato |
|--------|------------|
| 0 | ✅ Conversione completata con successo |
| 1 | ❌ Errore di input/output o parsing |

## Tecnologie

- Python 3.6+
- Standard library: json, sys, pathlib, time, random

## Note Finali

- Lo script è pensato per essere rieseguito in sicurezza (sovrascrive l'output).
- Per dettagli funzionali rapidi usa anche `wiki/CONVERSION_GUIDE.md`.
