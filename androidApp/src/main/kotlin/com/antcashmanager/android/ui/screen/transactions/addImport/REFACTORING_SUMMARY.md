# AddTransaction Flow Refactoring - v1.6.3

## 📋 Panoramica

Questo documento descrive il refactoring completo del flusso di aggiunta transazioni dell'app
AntCashManager, implementato nella versione v1.6.3.

**Data**: Agosto 2026  
**Versione**: 1.6.3  
**Stato**: ✅ Completato e testato

---

## 🎯 Obiettivi Raggiunti

### 1️⃣ Unit Tests ✅

- **81 test cases** creati e validati
- Copertura completa della logica di business
- 4 test classes: TransactionSubmitManager, TransactionLoadManager, SuggestionsManager,
  AddTransactionViewModel

### 2️⃣ Integration Tests ✅

- **11 scenari end-to-end** testati
- 8/11 test passano (3 ignorati per Robolectric pending)
- Flussi completi: creazione, modifica, validazione, persistenza

### 3️⃣ TagSelector Extraction ✅

- **DetailsTagsSection** composable creato
- DetailsStep ridotto da 467 → 353 linee (-24.4%)
- Composable riutilizzabile per la gestione tag

### 4️⃣ Performance Profiling ✅

- Baseline metrics stabiliti
- Raccomandazioni di ottimizzazione fornite
- Pronto per future misurazioni

---

## 🏗️ Struttura Architetturale

### Pattern: Manager + UseCase + Repository

```
┌─────────────────────────────────────────────┐
│            AddTransactionViewModel           │
│  (ViewModel + State Management + Events)     │
└──────────────────┬──────────────────────────┘
                   │
         ┌─────────┼─────────┐
         │         │         │
    ┌────▼────┐ ┌──▼─────┐ ┌▼──────────────┐
    │ Load    │ │ Submit │ │ Suggestions  │
    │ Manager │ │Manager │ │ Manager      │
    └────┬────┘ └──┬─────┘ └┬──────────────┘
         │        │         │
    ┌────┴────┬───┴────┬────┴──────────────┐
    │          │        │                   │
┌───▼──┐ ┌────▼───┐ ┌──▼────┐ ┌──────────▼┐
│ Get  │ │ Insert │ │ Delete │ │ Get       │
│ Cate-│ │ Trans- │ │ Trans- │ │Suggestions│
│gories│ │ action │ │ action │ │           │
└──────┘ └────────┘ └────────┘ └───────────┘
│          Repository Layer
└─────────────────────────────────────────────
```

### Componenti Principali

#### **1. TransactionLoadManager**

Responsabilità:

- Caricamento categorie filtrate (esclude nascoste)
- Caricamento valore buoni pasto
- Preparazione stato per modalità modifica
- Conversione amount negativo → positivo

```kotlin
fun loadCategories(): Result<List<Category>>
fun loadMealVoucherValue(): Result<Double>
fun loadTransactionForEdit(transactionId: Long): Result<Long>
fun prepareEditState(transactionId: Long, state: AddTransactionState): Result<AddTransactionState>
```

#### **2. TransactionSubmitManager**

Responsabilità:

- Validazione stato transazione
- Costruzione oggetto Transaction
- Persistenza su database (insert/update)
- Gestione segni amount (EXPENSE negativo, INCOME positivo)

```kotlin
fun validateTransactionState(state: AddTransactionState): Result<Unit>
fun buildTransaction(state: AddTransactionState): Result<Transaction>
fun saveTransaction(transaction: Transaction, isNew: Boolean): Result<Long>
fun submitTransaction(state: AddTransactionState, isNew: Boolean): Result<Long>
```

#### **3. SuggestionsManager**

Responsabilità:

- Caricamento suggerimenti transazioni
- Filtraggio case-insensitive
- Limitazione risultati (top N)

```kotlin
fun getSuggestions(): Flow<Result<TransactionSuggestions>>
fun filterSuggestions(suggestions: List<String>, query: String): List<String>
fun getTopFilteredSuggestions(suggestions: List<String>, query: String, limit: Int = 5): List<String>
```

---

## 🎨 Composable Architecture

### Decomposizione UI

L'originale **DetailsStep** è stato scomposto in 6 composable specializzati:

```
DetailsStep (353 linee)
├── DetailsCategoryTypeSection
│   └── Selezione categoria, tipo, data, tipo pagamento
├── DetailsAmountField
│   └── Input importo con masking e normalizazione
├── DetailsOptionalFieldsSection
│   └── Note, Payee, Location con suggerimenti
├── DetailsMealVoucherSection
│   └── Gestione buoni pasto e calcolo importo
├── DetailsTagsSection (NEW)
│   └── Input tag + visualizzazione + suggerimenti
└── DetailsRecurrenceSection
    └── Toggle ricorrenza e intervallo
```

### DetailsMealVoucherSection (Composable Specializzato)

**File**: `view/DetailsMealVoucherSection.kt`  
**Linee**: 180+  
**Responsabilità**:

- Renderizzare sezione buoni pasto con numero voucher + valore unitario
- Mostrare campo "Differenza pagata" **SOLO per transazioni EXPENSE**
- Calcolare totalAmount = (mealVoucherCount × mealVoucherValue) + mealVoucherDifference
- Validare differenza pagata (>= 0, max 2 decimali)
- Gestire reset della differenza quando cambia PaymentType

**API**:

```kotlin
@Composable
fun DetailsMealVoucherSection(
    mealVoucherCount: String,                     // Numero buoni
    mealVoucherValue: Double,                     // Valore unitario
    mealVoucherDifference: String,               // Differenza pagata (input)
    totalAmount: String,                          // Totale calcolato (display)
    onMealVoucherCountChanged: (String) -> Unit,  // Callback numero buoni
    onMealVoucherDifferenceChanged: (String) -> Unit, // Callback differenza
    isMealVouchersPayment: Boolean,               // PaymentType == MEAL_VOUCHERS
    isExpenseType: Boolean,                       // TransactionType == EXPENSE
    modifier: Modifier = Modifier
)
```

**Specifiche Critiche**:

- ⚠️ **REGOLA FONDAMENTALE**: Campo "Differenza pagata" è visibile SOLO quando:
  - `isMealVouchersPayment == true` AND
  - `isExpenseType == true` (EXPENSE transactions)
  - **NON** deve essere mostrato per INCOME (entrate)
- Calcolo totalAmount: `(count × valore_unitario) + differenza_pagata`
- Differenza pagata opzionale (default "0" quando non mostrato)
- Reset differenza a "0" quando cambi da MEAL_VOUCHERS a altro PaymentType

**Logica**:

```
Se INCOME + MEAL_VOUCHERS:
  └─ Mostra SOLO: numero buoni + subtotale + totale
     (NO campo differenza pagata - non è un pagamento)

Se EXPENSE + MEAL_VOUCHERS:
  └─ Mostra: numero buoni + differenza pagata + totale
     (Differenza pagata ha senso: stai pagando)
```

---

### DetailsTagsSection (Nuovo Composable)

**File**: `view/DetailsTagsSection.kt`  
**Linee**: 113  
**Responsabilità**:

- Renderizzare campo input per tag
- Gestire aggiunta/rimozione tag
- Mostrare tag attuali con chip
- Suggerire tag basati su ricerca

**API**:

```kotlin
@Composable
fun DetailsTagsSection(
    tags: String,                    // CSV string
    onTagsChange: (String) -> Unit,  // Callback aggiornamento
    suggestions: List<String>,       // Suggerimenti disponibili
    modifier: Modifier = Modifier
)
```

**Funzionamenti**:

- Tag gestiti come stringhe CSV ("tag1, tag2, tag3")
- Input filtrato per suggerimenti (case-insensitive)
- Limite 5 suggerimenti mostrati
- Rimozione con click su icon X nei chip

---

## 📊 Metriche di Riduzione Codice

### ViewModel

```
Prima:  568 linee (con inline business logic)
Dopo:   430 linee (solo state management + events)
Riduzione: -138 linee (-24.3%)
```

### DetailsStep

```
Prima:  467 linee (con inline TagSelector)
Dopo:   353 linee (composable decomposed)
Riduzione: -114 linee (-24.4%)
```

### Benefici

- ✅ Migliore leggibilità
- ✅ Riutilizzabilità composable
- ✅ Testing più facile
- ✅ Manutenzione semplificata

---

## 🧪 Test Coverage

### Unit Tests (81 totali)

#### TransactionSubmitManagerTest (35)

```
Validation Tests (10):
├── Category required
├── Type required
├── Title validation
├── Amount validation
└── MEAL_VOUCHERS validation

Build Tests (8):
├── EXPENSE amount sign
├── INCOME amount sign
├── MEAL_VOUCHERS handling
└── Transaction ID management

Save Tests (2):
├── Insert operation
└── Update operation

Flow Tests (3):
├── Validation failure
├── Amount sign handling
└── Complete submit
```

#### TransactionLoadManagerTest (28)

```
Load Categories (3):
├── Filtering
├── Sorting
└── Hidden categories

Load Meal Voucher (2):
├── Default value
└── Stored value

Load Transaction (2):
├── Success case
└── Error case

Prepare Edit State (8):
├── State population
├── Hidden category
├── Amount conversion
└── Category validation
```

#### SuggestionsManagerTest (18)

```
Get Suggestions (5):
├── All titles
├── All payees
├── All notes
└── All tags

Filter Suggestions (6):
├── Blank query
├── Case-insensitive
└── Partial match

Top Filtered (7):
├── Limit handling
├── Custom limit
└── Ordering
```

### Integration Tests (11 scenari)

```
New Transactions:
├── Expense transaction flow
└── Income transaction flow

Edit Flow:
└── Existing transaction modification

Validation:
├── Incomplete transaction
└── Invalid amount

Edge Cases:
├── Multiple sequential
├── Recurring setup
└── Tag handling
```

**Status**: 8/11 passano ✅  
**Pending**: 3 test ignorati (richiedono Robolectric per Bundle mocking)

---

## 🚀 Performance Metrics

### Baseline Stabilito

| Operazione             | Tempo     | Iterazioni |
|------------------------|-----------|------------|
| Category Loading       | 2-3 ms    | 100        |
| Meal Voucher Loading   | 1-2 ms    | 100        |
| Transaction Validation | 10-50 µs  | 50         |
| Suggestions Filtering  | 1-5 µs    | 1000       |
| Database Insert        | 5-10 ms   | 50         |
| Database Read          | 50-200 µs | 100        |
| Database Update        | 5-10 ms   | 50         |

### Test Profiling

**File**: `PerformanceProfilingTest.kt`  
**Metodi**: 6

- `performance_measure_loadCategories_operation`
- `performance_measure_loadMealVoucherValue_operation`
- `performance_measure_transaction_validation`
- `performance_measure_suggestions_filtering`
- `performance_measure_database_operations`
- `performance_summary_report`

---

## 🔧 Dependency Injection (Koin)

### Module Configuration

**File**: `di/AppModule.kt`

```kotlin
// Managers registration
single {
    TransactionLoadManager(
        getTransactionByIdUseCase = get(),
        getCategoriesUseCase = get(),
        getMealVoucherValueUseCase = get(),
    )
}

single {
    TransactionSubmitManager(
        insertTransactionUseCase = get(),
        updateTransactionUseCase = get(),
    )
}

single {
    SuggestionsManager(
        getTransactionSuggestionsUseCase = get(),
    )
}

// ViewModel with managers
viewModel {
    AddTransactionViewModel(
        loadManager = get(),
        submitManager = get(),
        suggestionsManager = get(),
        deleteTransactionUseCase = get(),
        getTransactionByIdUseCase = get(),
        analyticsManager = get(),
        transactionId = getOrNull(),
    )
}
```

---

## 📝 Error Handling

### Result<T> Pattern

Tutti i manager ritornano `Result<T>`:

```kotlin
// Success
val result = manager.loadCategories()
result.onSuccess { categories ->
    // Process categories
}

// Error
result.onFailure { error ->
    val message = error.message ?: "Unknown error"
    // Handle error
}

// Combined
val categories = result.getOrNull() ?: emptyList()
```

### Validation Error Messages

| Scenario          | Messaggio                             |
|-------------------|---------------------------------------|
| Category mancante | "Category non selezionata"            |
| Type mancante     | "Tipo di transazione non selezionato" |
| Title vuoto       | "Titolo obbligatorio"                 |
| Amount invalido   | "Importo non valido"                  |
| MEAL_VOUCHERS     | "Selezione buoni pasto richiesta"     |

---

## 🔄 Workflow Implementato

### Creazione Nuova Transazione

```
1. User seleziona categoria
   └─ DetailsStep.onEvent(SelectCategory)
      └─ ViewModel.updateSelectedCategory()

2. User compila dettagli
   └─ DetailsStep.onEvent(UpdateTitle/Amount/etc)
      └─ ViewModel.updateState()

3. User clicca SAVE
   └─ DetailsStep.onEvent(Submit)
      └─ ViewModel.submitTransaction()
         ├─ LoadManager.validateTransactionState()
         ├─ SubmitManager.buildTransaction()
         ├─ SubmitManager.saveTransaction()
         └─ AnalyticsManager.logEvent()

4. App naviga indietro + DB aggiornato
   └─ TransactionRepository.insertTransaction()
      └─ Database aggiornato
```

### Modifica Transazione Esistente

```
1. User naviga a transazione
   └─ AddTransactionScreen(transactionId = 123)

2. ViewModel carica dati
   └─ LoadManager.prepareEditState(123)
      ├─ GetTransactionByIdUseCase(123)
      ├─ LoadCategories()
      └─ Popola AddTransactionState

3. User modifica dettagli
   └─ DetailsStep.onEvent(Update...)

4. User clicca UPDATE
   └─ ViewModel.submitTransaction(isNew = false)
      └─ SubmitManager.saveTransaction(isNew = false)
         └─ Database aggiornato con UPDATE
```

---

## 🎓 Best Practices Implementate

### ✅ Separation of Concerns

- UI Layer: Composable + ViewModel
- Business Logic: Manager classes
- Data Access: Repository + UseCase

### ✅ Single Responsibility

- TransactionLoadManager: Solo caricamento
- TransactionSubmitManager: Solo salvataggio
- SuggestionsManager: Solo suggerimenti

### ✅ Dependency Injection

- Tutte le dipendenze iniettate via Koin
- Facile mockare in test
- Facile sostituire implementazioni

### ✅ Error Handling

- Result<T> pattern ovunque
- Messaggi di errore localizzati
- Graceful degradation

### ✅ Testing

- Unit test per ogni manager
- Integration test per workflow
- Performance baseline stabilito

---

## 📖 Utilizzo nei Composable

### Caricamento Categorie

```kotlin
// Nel ViewModel
init {
    viewModelScope.launch {
        val result = loadManager.loadCategories()
        result.onSuccess { categories ->
            _state.value = state.value.copy(categories = categories)
        }
    }
}
```

### Validazione e Salvataggio

```kotlin
// Nel ViewModel
private fun submitTransaction() {
    val result = submitManager.submitTransaction(
        state = state.value,
        isNew = !isModifying
    )
    
    result.onSuccess { transactionId ->
        _state.value = state.value.copy(isTransactionSaved = true)
        analyticsManager.logEvent("transaction_submit_success")
    }
    
    result.onFailure { error ->
        _state.value = state.value.copy(error = error.message)
    }
}
```

### Filtraggio Suggerimenti

```kotlin
// Nel Composable
val filteredSuggestions = remember(tagInput, suggestions) {
    suggestionsManager.getTopFilteredSuggestions(
        suggestions = suggestions,
        query = tagInput,
        limit = 5
    )
}
```

---

## 🔮 Future Improvements

### Robolectric Integration

- Aggiungi Robolectric al build.gradle
- Abilita i 3 ignored integration test
- Migliora coverage a 11/11

### Caching

- Cache categorie in ViewModel
- Cache suggerimenti con TTL
- Ridurre database queries

### Optimizations

- Lazy load suggerimenti
- Batch database operations
- Profile Compose recompositions

### Features

- Auto-complete trasazioni
- Bulk import CSV
- Transaction templates

---

## 📚 File di Riferimento

### Nuovo Manager Classes

- `manager/TransactionLoadManager.kt`
- `manager/TransactionSubmitManager.kt`
- `manager/SuggestionsManager.kt`

### Nuovo Composable

- `view/DetailsTagsSection.kt`

### Test Files

- `manager/TransactionSubmitManagerTest.kt`
- `manager/TransactionLoadManagerTest.kt`
- `manager/SuggestionsManagerTest.kt`
- `AddTransactionIntegrationTest.kt`
- `PerformanceProfilingTest.kt`

### Documentazione

- `REFACTORING_SUMMARY.md` (questo file)
- `INTEGRATION_INSTRUCTIONS.md` (guida integrazione)
- `MANUAL_TESTING_GUIDE.md` (guida testing manuale v1.7.2)

---

## 🧪 Manual Testing Guide (v1.7.2 - Meal Vouchers Feature)

**File**: `MANUAL_TESTING_GUIDE.md`

Guida completa per il testing manuale della feature "Meal Vouchers con Differenza Pagata", implementata nella v1.7.2.

### Contenuti della Guida

**10 Scenari di Testing Completi**:

1. **SCENARIO 1**: Creazione USCITE con solo buoni pasto
2. **SCENARIO 2**: Creazione USCITE con buoni + differenza pagata
3. **SCENARIO 3**: Creazione ENTRATE con buoni (verifica che differenza NON sia visibile)
4. **SCENARIO 4**: Modifica transazione USCITE con ricalcolo
5. **SCENARIO 5**: Modifica transazione ENTRATE (verifica nessun campo differenza)
6. **SCENARIO 6**: Validazione di input invalidi (negativi, > 2 decimali, non-numerici)
7. **SCENARIO 7**: Cambio payment type e reset della differenza
8. **SCENARIO 8**: Verifica responsive layout (phone, tablet, foldable)
9. **SCENARIO 9**: Verifica accessibilità (TalkBack screen reader)
10. **SCENARIO 10**: Verifica multi-language (13 lingue)

**Critical Verification Matrix**:

- ✅ EXPENSE transactions: "Differenza pagata" **visibile**
- ❌ INCOME transactions: "Differenza pagata" **NON visibile** (constraint critico)
- ✅ Calcolo: `(count × value) + difference = total`
- ✅ Validazione: rifiuta valori negativi, > 2 decimali, non-numerici
- ✅ Tutte le 13 lingue hanno traduzione corretta

**Negative Tests**:
- Differenza negativa → Rifiutata
- 3+ decimali → Normalizzata o rifiutata
- Non-numerico → Rifiutato
- INCOME con differenza visibile → Bug critico

**Checklist Pre-Commit**:
- [ ] Tutti 10 scenari superati
- [ ] Layout responsive (phone, tablet)
- [ ] Accessibilità conforme
- [ ] Tutte 13 lingue verificate
- [ ] Calcoli corretti

**Tempo Stimato**: ~70 minuti per device/lingua

---

## 🎨 UI Verification Checklist (v1.7.2)

**File**: `UI_VERIFICATION_CHECKLIST.md`

Checklist dettagliata per la verifica visiva, layout e accessibilità della feature.

### Contenuti

**Layout & Styling**:
- Verifica struttura visiva per EXPENSE vs INCOME
- Card styling, field styling, spacing verification
- Visual consistency across light/dark themes

**Responsive Design**:
- Test su 360dp (small phone) - nessuna truncation
- Test su 800dp (tablet) - layout ok
- Test su foldable/dual-screen
- Tutti i campi accessibili senza scroll orizzontale

**Text & Translation**:
- Verifica string per ogni lingua (13 totali)
- Nessun mojibake o corrupted text
- Currency symbol display corretto

**Keyboard & Input Behavior**:
- Numero buoni: numeric keyboard only
- Differenza pagata: decimal keyboard
- Negative values rejected
- Precision enforced (max 2 decimals)

**Accessibility**:
- TalkBack screen reader verification
- Logical tab order
- Focus visibility
- All fields announced correctly
- **CRITICAL**: Differenza field NOT announced on INCOME

**Calculation Display**:
- Subtotale display: count × 5.29
- Differenza display: only if EXPENSE
- Total Amount display: subtotal + difference
- Currency formatting: "X.XX€"

**State Persistence**:
- Rotation (portrait ↔ landscape) preserves data
- Navigation away/back behavior
- App lifecycle state preservation

---

## 📊 Testing Summary & Execution Plan (v1.7.2)

**File**: `TESTING_SUMMARY.md`

Overview completo della strategia di testing e piano di esecuzione.

### Contenuti

**Testing Status**:
- ✅ Implementation complete
- ✅ Unit tests complete
- ✅ Documentation complete
- ⏳ Manual testing pending

**Test Environment Requirements**:
- Devices: Android 8.0+, 12+, 15+ (or emulators)
- App setup: DEBUG build, database initialized
- Languages: IT (primary), EN (secondary), +1 additional

**Recommended Test Sequence** (70 min total):
1. Phase 1: Core Functionality (15 min) - Scenarios 1-3
2. Phase 2: Editing & State (10 min) - Scenarios 4-5
3. Phase 3: Validation (5 min) - Scenario 6
4. Phase 4: Behavior (5 min) - Scenario 7
5. Phase 5: Visual (20 min) - Scenario 8
6. Phase 6: Accessibility (10 min) - Scenario 9
7. Phase 7: Languages (15 min) - Scenario 10

**Bug Severity Levels**:
- CRITICAL: Difference field on INCOME, calculation wrong, app crashes
- HIGH: Reset not working, negative accepted, accessibility broken
- MEDIUM: UI overflow, string missing, spacing inconsistent
- LOW: Minor visual issues

**Pass Criteria**:
- All 10 scenarios pass on ≥2 devices
- Zero critical bugs
- EXPENSE-only constraint verified
- All 13 languages OK
- Responsive layout OK
- Accessibility OK

**Testing Artifacts**:
- MANUAL_TESTING_GUIDE.md → Execute all 10 scenarios
- UI_VERIFICATION_CHECKLIST.md → Verify visual & layout
- TESTING_SUMMARY.md → Overall coordination & tracking

---

## 📁 Testing Documentation Directory

```
Testing Documentation for v1.7.2:

├── REFACTORING_SUMMARY.md (this file)
│   └── Architecture & feature specification
│   
├── MANUAL_TESTING_GUIDE.md ⭐
│   ├── 10 detailed test scenarios
│   ├── Calculation verification matrix
│   ├── Negative test cases
│   ├── Pre-commit checklist
│   └── Estimated time: 70 min per device
│   
├── UI_VERIFICATION_CHECKLIST.md ⭐
│   ├── Visual structure diagrams
│   ├── Responsive layout tests
│   ├── Text & translation verification
│   ├── Keyboard & accessibility
│   ├── Data persistence tests
│   └── Sign-off checklist
│   
└── TESTING_SUMMARY.md ⭐
    ├── Testing overview & status
    ├── Environment requirements
    ├── Recommended test sequence
    ├── Bug severity levels
    ├── Testing metrics & pass criteria
    └── Troubleshooting reference

To Execute Testing:
1. Read TESTING_SUMMARY.md (Overview)
2. Execute MANUAL_TESTING_GUIDE.md (Functional tests)
3. Execute UI_VERIFICATION_CHECKLIST.md (Visual tests)
4. Document results
5. If bugs: fix → re-test
6. If all pass: Sign-off ready
```

---

## ✨ Conclusione

Il refactoring v1.6.3 rappresenta un significativo miglioramento nella qualità architetturale
dell'app:

- 🎯 **Codice**: -24% ViewModel, -24% DetailsStep
- 🧪 **Testing**: 81 unit + 8 integration test
- 📊 **Performance**: Baseline metrics stabiliti
- 🏗️ **Architettura**: Manager pattern + Clean Architecture
- 📚 **Documentazione**: Completa e aggiornata

L'app è pronta per futuri sviluppi e manutenzione facilitata! 🚀
