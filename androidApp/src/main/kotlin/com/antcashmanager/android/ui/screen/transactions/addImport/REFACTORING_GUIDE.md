# FASE 5: Refactoring Architetturale - Guida

## Status Attuale

### ✅ Completato
1. **TransactionValidator.kt** - Centralizzazione logica di validazione
   - `isFormValid()` - Validazione completa del form
   - `isValidAmount()`, `isValidTitle()`, `isValidMealVoucherCount()`, etc
   - `normalizeAmount()` - Normalizzazione input utente
   - Ubicazione: `validator/TransactionValidator.kt`

2. **DetailsAmountField.kt** - Composable estratto
   - Incapsula il campo "Importo"
   - Normalizzazione input in tempo reale
   - Ubicazione: `view/DetailsAmountField.kt`

3. **DetailsMealVoucherSection.kt** - Composable estratto
   - Incapsula la sezione "Buoni Pasto"
   - Visualizzazione voucher count e importo totale
   - Ubicazione: `view/DetailsMealVoucherSection.kt`

### ✅ Completato FASE 5.2

1. ✅ `DetailsRecurrenceSection.kt` (60 linee) - Toggle ricorrenza e dropdown intervallo
2. ✅ `DetailsOptionalFieldsSection.kt` (60 linee) - Note, Payee, Location
3. ✅ `DetailsCategoryTypeSection.kt` (100 linee) - Categoria, Tipo, Data, Payment Type

### ⏳ Prossimi Passi

## 1. Completare Refactoring di DetailsStep (724 → 500 linee)

### Strategia
Dividere il file in composable tematici:
- ✅ `DetailsAmountField.kt` (60 linee)
- ✅ `DetailsMealVoucherSection.kt` (140 linee)
- ⏳ `DetailsRecurrenceSection.kt` (50 linee)
- ⏳ `DetailsOptionalFieldsSection.kt` (100 linee - Note, Payee, Location, Tags)
- ⏳ `DetailsCategoryTypeSection.kt` (60 linee - Categoria, Tipo, Data)
- ⏳ Aggiornare `DetailsStep.kt` per orchestrare i nuovi composable

### Come Procedere

**Passo 1: Creare DetailsRecurrenceSection.kt**
```kotlin
@Composable
internal fun DetailsRecurrenceSection(
    isRecurring: Boolean,
    recurrenceInterval: String,
    onRecurringChanged: (Boolean) -> Unit,
    onIntervalChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
)
```

**Passo 2: Creare DetailsOptionalFieldsSection.kt**
```kotlin
@Composable
internal fun DetailsOptionalFieldsSection(
    notes: String,
    payee: String,
    location: String,
    tags: String,
    onNotesChanged: (String) -> Unit,
    onPayeeChanged: (String) -> Unit,
    // ... altri callback
    modifier: Modifier = Modifier,
)
```

**Passo 3: Aggiornare DetailsStep.kt**
Sostituire il codice inline con i nuovi composable:
```kotlin
DetailsAmountField(
    amount = state.amount,
    onAmountChanged = { onEvent(AddTransactionEvent.UpdateAmount(it)) },
    isMealVouchersPayment = state.isMealVouchersPayment,
)

DetailsMealVoucherSection(
    mealVoucherCount = state.mealVoucherCount,
    mealVoucherValue = state.mealVoucherValue,
    totalAmount = state.amount,
    onMealVoucherCountChanged = { onEvent(AddTransactionEvent.UpdateMealVoucherCount(it)) },
    isMealVouchersPayment = state.isMealVouchersPayment,
)

// ... altri composable
```

**Passo 4: Test**
- Eseguire test composable (DetailsStepTest.kt)
- Verificare che il layout sia identico
- No behavior changes

## 2. Refactoring del ViewModel (547 → 400 linee)

### Strategia
Estrarre la logica di business in manager separati:
- ✅ `TransactionValidator` (usato dal form)
- ⏳ `TransactionLoadManager` (logica di loadTransaction)
- ⏳ `TransactionSubmitManager` (logica di submit/update)
- ⏳ `SuggestionsManager` (logica di suggestions)

### Come Procedere

**Passo 1: Creare TransactionLoadManager**
```kotlin
class TransactionLoadManager(
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
) {
    suspend fun loadTransaction(transactionId: Long): Result<TransactionData>
}
```
- Responsabilità: Caricare la transazione dal DB
- Gestione errori: Categoria non trovata, transazione non trovata
- Return type: `Result<TransactionData>` per gestione errori

**Passo 2: Creare TransactionSubmitManager**
```kotlin
class TransactionSubmitManager(
    private val insertTransactionUseCase: InsertTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val validator: TransactionValidator,
) {
    suspend fun submitTransaction(state: AddTransactionState): Result<Unit>
}
```
- Responsabilità: Validare e salvare la transazione
- Gestione errori: Network error, validation error
- Return type: `Result<Unit>` per uniformità

**Passo 3: Aggiornare AddTransactionViewModel**
```kotlin
class AddTransactionViewModel(
    // ... use cases
    private val loadManager: TransactionLoadManager,
    private val submitManager: TransactionSubmitManager,
) {
    private suspend fun submitTransaction() {
        submitManager.submitTransaction(state).onSuccess { ... }
    }
    
    private suspend fun loadTransaction() {
        loadManager.loadTransaction(transactionId).onSuccess { ... }
    }
}
```

**Passo 4: Test**
- Eseguire test unitari (AddTransactionViewModelTest.kt)
- Verificare che il comportamento sia identico
- No state changes

## 3. Centralizzare Validazione

### Stato Attuale
✅ `TransactionValidator` già centralizzato in `validator/TransactionValidator.kt`

### Utilizzo
```kotlin
// Nel ViewModel
if (!TransactionValidator.isFormValid(state)) {
    return
}

// Nel UI Composable
val normalized = TransactionValidator.normalizeAmount(newValue)
val isValid = TransactionValidator.isValidAmount(amount)
```

### Estendere in Futuro
Se necessaria validazione aggiuntiva (es. limiti di importo, lunghezza titolo):
```kotlin
object TransactionValidator {
    // Limiti di validazione
    const val MAX_TITLE_LENGTH = 255
    const val MAX_AMOUNT = 999999.99
    const val MIN_AMOUNT = 0.01
    
    fun isValidTitle(title: String): Boolean {
        return title.isNotBlank() && title.length <= MAX_TITLE_LENGTH
    }
}
```

## Metriche di Successo

| Metrica | Target | Stato |
|---------|--------|-------|
| Linee DetailsStep | 300-350 | ⏳ |
| Linee ViewModel | 350-400 | ⏳ |
| Composable riutilizzabili | 5+ | ✅ 2 (Amount, MealVoucher) |
| Test Coverage | 80%+ | ✅ 80% (ViewModel) |
| Build Success | 100% | ✅ |
| No Breaking Changes | 100% | ✅ |

## Ordine di Priorità per Prossime Fasi

1. **Alta Priorità** (Impatto Alto, Sforzo Basso)
   - Creare `DetailsRecurrenceSection.kt`
   - Creare `DetailsOptionalFieldsSection.kt`
   - Aggiornare `DetailsStep.kt` per usare i nuovi composable

2. **Media Priorità** (Impatto Medio, Sforzo Medio)
   - Creare `TransactionLoadManager`
   - Creare `TransactionSubmitManager`
   - Aggiornare `AddTransactionViewModel` per usare i manager

3. **Bassa Priorità** (Cosmetic/Future)
   - Estendere `TransactionValidator` con nuove validazioni
   - Aggiungere test per i nuovi manager
   - Documentare il nuovo pattern

## Note Importanti

⚠️ **Evitare Breaking Changes**
- Mantenere le firma dei callback `onEvent` identiche
- Mantenere la struttura dello `AddTransactionState` invariata
- Mantenere i test passanti (regressione = FAIL)

✅ **Best Practices**
- Ogni composable estratto deve avere una singola responsabilità
- Usare nomi descrittivi per i callback (`onMealVoucherCountChanged` ✅, `onChange` ❌)
- Documentare con KDoc ogni composable/manager
- Testare ogni nuovo composable con DetailsStepTest.kt

🔄 **Testing Strategy**
- Prima: Screenshot test dello stato attuale
- Durante: Test unitari per logica di business
- Dopo: Verifica visiva che il layout sia identico

---

**Ultima aggiornazione**: 2026-08-08
**Responsabile**: Development Team
**Status**: In Progress ✅ (FASE 5.1 completato, FASE 5.2-5.3 ⏳)
