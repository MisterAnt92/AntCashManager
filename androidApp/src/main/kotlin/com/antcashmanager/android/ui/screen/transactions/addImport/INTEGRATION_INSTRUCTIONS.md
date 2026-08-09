# Istruzioni di Integrazione: Composable Estratti

## Situazione Attuale

### Composable Estratti Pronti (FASE 5.2 ✅)
- ✅ `DetailsAmountField.kt` - Campo importo con normalizzazione
- ✅ `DetailsMealVoucherSection.kt` - Sezione buoni pasto
- ✅ `DetailsRecurrenceSection.kt` - Toggle ricorrenza + dropdown intervallo
- ✅ `DetailsOptionalFieldsSection.kt` - Note, Payee, Location (NO Tags yet)
- ✅ `DetailsCategoryTypeSection.kt` - Categoria, Tipo, Data, Payment Type

### DetailsStep.kt Status
- Stato: **724 linee** (non ancora aggiornato)
- Contiene ancora il codice inline che potrebbe essere estratto
- **Importante**: I composable estratti compilano correttamente e sono pronti per l'uso

---

## Come Integrare i Composable in DetailsStep

### Passo 1: Aggiungere Import

Aggiungi questi import all'inizio di `DetailsStep.kt`:

```kotlin
// Nuovi composable estratti
import com.antcashmanager.android.ui.screen.transactions.addImport.view.DetailsAmountField
import com.antcashmanager.android.ui.screen.transactions.addImport.view.DetailsMealVoucherSection
import com.antcashmanager.android.ui.screen.transactions.addImport.view.DetailsRecurrenceSection
import com.antcashmanager.android.ui.screen.transactions.addImport.view.DetailsOptionalFieldsSection
import com.antcashmanager.android.ui.screen.transactions.addImport.view.DetailsCategoryTypeSection
```

### Passo 2: Sostituire il Codice Inline

#### A. DetailsCategoryTypeSection (linee 234-261 in DetailsStep.kt)

**PRIMA:**
```kotlin
// ── Categoria – sempre editabile al tap ──
AppSelectionItemCard(
    label = stringResource(R.string.add_transaction_category),
    // ... 10+ linee di codice
)
Spacer(modifier = Modifier.height(12.dp))

// ── Tipo – sempre editabile al tap ──
AppSelectionItemCard(
    // ... 15+ linee di codice
)
Spacer(modifier = Modifier.height(12.dp))

// ── Titolo ──
AutocompleteTextField(
    // ... linee di titolo
)
Spacer(modifier = Modifier.height(12.dp))

// ── Importo ──
// ... 50+ linee di codice

// ── Data – sempre editabile al tap ──
AppSelectionItemCard(
    // ... linee di data
)
Spacer(modifier = Modifier.height(12.dp))
```

**DOPO:**
```kotlin
// ── Categoria, Tipo, Data, Payment Type ──
DetailsCategoryTypeSection(
    selectedCategory = state.selectedCategory,
    selectedType = state.selectedType,
    selectedPaymentType = state.selectedPaymentType,
    timestamp = state.timestamp,
    onEditCategory = { onEvent(AddTransactionEvent.EditCategory) },
    onEditType = { onEvent(AddTransactionEvent.EditType) },
    onEditDate = { onEvent(AddTransactionEvent.EditDate) },
    onEditPaymentType = { onEvent(AddTransactionEvent.EditPaymentType) },
)

// ── Titolo ──
AutocompleteTextField(
    value = state.title,
    onValueChange = { onEvent(AddTransactionEvent.UpdateTitle(it)) },
    suggestions = state.titleSuggestions,
    label = stringResource(R.string.add_transaction_title_required),
    modifier = Modifier.fillMaxWidth(),
)
Spacer(modifier = Modifier.height(12.dp))

// ── Importo ──
DetailsAmountField(
    amount = state.amount,
    onAmountChanged = { onEvent(AddTransactionEvent.UpdateAmount(it)) },
    isMealVouchersPayment = state.isMealVouchersPayment,
)
```

#### B. DetailsMealVoucherSection (linee 390-460 in DetailsStep.kt)

**PRIMA:**
```kotlin
if (isMealVouchersPayment) {
    Column(
        // ... 70+ linee di codice per meal vouchers
    )
}
```

**DOPO:**
```kotlin
DetailsMealVoucherSection(
    mealVoucherCount = state.mealVoucherCount,
    mealVoucherValue = state.mealVoucherValue,
    totalAmount = state.amount,
    onMealVoucherCountChanged = { onEvent(AddTransactionEvent.UpdateMealVoucherCount(it)) },
    isMealVouchersPayment = state.isMealVouchersPayment,
)
```

#### C. DetailsOptionalFieldsSection (linee 346-365 in DetailsStep.kt)

**PRIMA:**
```kotlin
// ── Note ──
AutocompleteTextField(
    value = state.notes,
    // ...
)
Spacer(modifier = Modifier.height(12.dp))

// ── Beneficiario ──
AutocompleteTextField(
    value = state.payee,
    // ...
)
Spacer(modifier = Modifier.height(12.dp))

// ── Luogo ──
AutocompleteTextField(
    value = state.location,
    // ...
)
```

**DOPO:**
```kotlin
// ── Note, Payee, Location ──
DetailsOptionalFieldsSection(
    notes = state.notes,
    payee = state.payee,
    location = state.location,
    notesSuggestions = state.notesSuggestions,
    payeeSuggestions = state.payeeSuggestions,
    locationSuggestions = state.locationSuggestions,
    onNotesChanged = { onEvent(AddTransactionEvent.UpdateNotes(it)) },
    onPayeeChanged = { onEvent(AddTransactionEvent.UpdatePayee(it)) },
    onLocationChanged = { onEvent(AddTransactionEvent.UpdateLocation(it)) },
)
```

#### D. DetailsRecurrenceSection (linee 506-551 in DetailsStep.kt)

**PRIMA:**
```kotlin
// ── Ricorrente ──
Row(
    // ... 45+ linee di codice
)

if (state.isRecurring) {
    Spacer(modifier = Modifier.height(12.dp))
    RecurrenceIntervalDropdown(
        // ...
    )
}
```

**DOPO:**
```kotlin
// ── Ricorrenza ──
DetailsRecurrenceSection(
    isRecurring = state.isRecurring,
    recurrenceInterval = state.recurrenceInterval,
    onRecurringChanged = { setRecurring(it) },
    onIntervalChanged = { onEvent(AddTransactionEvent.UpdateRecurrenceInterval(it)) },
)
```

### Passo 3: Pulizia di DetailsStep.kt

Dopo l'integrazione:
1. Rimuovere il codice inline sostituito
2. Verificare che non ci siano import non utilizzati
3. Eseguire `Optimize Imports` (Ctrl+Alt+O in IntelliJ)

### Passo 4: Verificare il Comportamento

```bash
# Compilare
./gradlew androidApp:compileDebugKotlin

# Eseguire i test
./gradlew androidApp:testDebugUnitTest

# Test composable (su emulatore)
./gradlew androidApp:connectedAndroidTest
```

### Passo 5: Screenshot Test

Confrontare i screenshot prima/dopo per verificare:
- ✅ Layout identico
- ✅ Spaziatura identica
- ✅ Colori identici
- ✅ Comportamento dei tap identico

---

## Checklist di Verifica

- [ ] Import dei nuovi composable aggiunti
- [ ] DetailsCategoryTypeSection integrata
- [ ] DetailsAmountField integrato
- [ ] DetailsMealVoucherSection integrato
- [ ] DetailsOptionalFieldsSection integrato
- [ ] DetailsRecurrenceSection integrato
- [ ] Compilazione successful
- [ ] Tutti i test passano
- [ ] Layout visualmente identico
- [ ] Nessun comportamento cambiato
- [ ] Import non utilizzati rimossi

---

## Risultati Attesi Dopo l'Integrazione

| Metrica | Prima | Dopo | Target |
|---------|-------|------|--------|
| Linee DetailsStep | 724 | ~450-500 | 300-400 |
| Componenti | 1 monolitico | 6 modulari | ✅ |
| Testabilità | Media | Alta | ✅ |
| Riutilizzabilità | Bassa | Alta | ✅ |
| Manutenibilità | Difficile | Facile | ✅ |

---

## Problemi Comuni e Soluzioni

### Problema 1: "Unresolved reference: DetailsAmountField"
**Causa**: Import mancante
**Soluzione**: Aggiungere `import com.antcashmanager.android.ui.screen.transactions.addImport.view.DetailsAmountField`

### Problema 2: Build fallisce con "Type mismatch"
**Causa**: Firma dei callback non corretta
**Soluzione**: Verificare che `onEvent(AddTransactionEvent....)` sia il tipo corretto

### Problema 3: UI cambia dopo l'integrazione
**Causa**: Modifier o padding diversi
**Soluzione**: Verificare che i `Spacer(height = 12.dp)` siano presenti tra i composable

### Problema 4: Test falliscono
**Causa**: Lo stato nel test non corrisponde al composable
**Soluzione**: Verificare che il test stia settando `state.isMealVouchersPayment`, `state.isRecurring`, etc correttamente

---

## Note Importanti

⚠️ **Non fare queste cose:**
- ❌ Modificare le signature dei callback durante l'integrazione
- ❌ Cambiare l'ordine dei composable (rompe layout)
- ❌ Rimuovere i Spacer tra i composable
- ❌ Aggiungere nuovi parametri al state

✅ **Fare queste cose:**
- ✅ Testare frequentemente durante l'integrazione
- ✅ Commitare step-by-step (ogni composable integrato = 1 commit)
- ✅ Verificare visivamente il layout
- ✅ Eseguire i test composable su emulatore

---

## Timeline Stimato

- Lettura e comprensione: 10 min
- Integrazione composable: 30 min
- Testing e debug: 20 min
- Screenshot verification: 10 min
- **Totale: ~70 minuti**

---

**Ultima aggiornazione**: 2026-08-08
**Composable Pronti**: 5/5 ✅
**Status**: Pronto per l'integrazione
