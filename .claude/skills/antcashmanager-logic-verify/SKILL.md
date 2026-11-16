---
name: antcashmanager-logic-verify
description: >
  Skill per verificare la correttezza delle logiche di business di AntCashManager.
  Usala quando devi controllare che UseCase, ViewModel, Screen, DI, o i18n rispettino
  le regole del dominio e i pattern architetturali del progetto.
  TRIGGERA quando l'utente chiede "verifica", "controlla", "è corretto", "funziona",
  "bug", "sbagliato", "review", o quando si implementa una nuova feature e si vuole
  validarne la correttezza logica prima di committare.
---

# AntCashManager — Verifica Logiche di Business

Skill complementare a `antcashmanager-android-kmp`. Contiene le **invarianti di dominio**
e le **checklist di verifica** per ogni layer. Leggila PRIMA di dichiarare completo
qualsiasi task che tocchi calcoli finanziari, filtri, stato UI, o routing eventi.

---

## 1. Modello di dominio — `Transaction`

```
Transaction(
  id, title, amount: Double, category, type: TransactionType,
  timestamp, notes, payee, location, isRecurring, tags,
  recurrenceInterval, paymentType: PaymentType,
  mealVoucherCount: Int, mealVoucherDifference: Double,
  categoryIcon, categoryColor
)
TransactionType  = INCOME | EXPENSE
PaymentType      = ELECTRONIC | CASH | MEAL_VOUCHERS
RecurrenceInterval = DAILY | WEEKLY | MONTHLY | YEARLY
```

**Invarianti obbligatorie:**
- `amount` è sempre **positivo** nel DB (segno canonico: `abs(amount)`).
- In memoria (dopo `withCorrectAmounts()`): INCOME → `+abs(amount)`, EXPENSE → `-abs(amount)`.
- `mealVoucherDifference` è usato **solo** per le transazioni EXPENSE con `paymentType == MEAL_VOUCHERS`.
- `mealVoucherCount` ≥ 0.
- `timestamp` in millisecondi Unix (UTC).

---

## 2. Calcoli finanziari — invarianti

| Calcolo | Formula corretta | Errore comune |
|---|---|---|
| `calculateTotalIncome()` | `filter { INCOME }.sumOf { amount }` → positivo | Sommare tutti gli importi (include expense) |
| `calculateTotalExpense()` | `filter { EXPENSE }.sumOf { amount }` → negativo | Usare `abs()` → perde il segno |
| `calculateBalance()` | `sumOf { amount }` su lista già normalizzata | Sommare su lista non normalizzata (tutti pos) |
| `netBalance` | alias di `balance` (già in `HomeState`) | Ricalcolarlo diversamente |
| `averageAmount` | `(abs(totalIncome) + abs(totalExpense)) / transactionCount` | Dividere per 0 se `transactionCount == 0` |
| `dailyAverageExpense` | `abs(totalExpense) / days`; `days = (to - from) / ONE_DAY_MS` | Dividere per 0 se `days == 0` |

**Check veloce:**
```bash
grep -n "calculateBalance\|calculateTotalExpense\|calculateTotalIncome\|sumOf" \
  androidApp/src/main/kotlin/com/antcashmanager/android/util/TransactionExtensions.kt
```

Regola di normalizzazione (`withCorrectAmounts`) → applicata **una sola volta** prima di
entrare in `transactionsFlow`. Se trovi `withCorrectAmount()` chiamata più volte sulle
stesse transazioni, è un bug (doppia negazione).

---

## 3. Filtri transazioni — `FilterTransactionsUseCase`

Ogni predicato è indipendente (AND logico). Ordine di applicazione:

1. **Date range** `timestamp in dateFrom..dateTo` — sempre applicato, corto-circuito prima.
2. **Search query** (case-insensitive) — se blank → passa tutto.
   - Cerca in `title`.
   - Cerca in `amount.toString()` e `abs(amount).toString()` (permette di trovare "10.5").
   - Confronto numerico esatto con epsilon `0.000001`.
   - Separatore decimale normalizzato (`,` → `.`).
3. **Category** — exact match su `transaction.category`; `null` → passa tutto.
4. **TransactionType** — `null` → passa tutto.
5. **PaymentType** — `null` → passa tutto.

**Verifica:** se aggiungi un filtro, assicurati:
- Che `null` sia il "nessun filtro" (non `""` né `0`).
- Che sia aggiunto a `TransactionFilterParams` (domain) e non in un VM.
- Che sia testato in `FilterTransactionsUseCaseTest`.

---

## 4. Meal Vouchers — regole

### Invariante critica — `remaining` è SEMPRE ≥ 0

I buoni pasto sono un credito consumabile: si ricevono (INCOME) e si usano (EXPENSE).
**Non esiste saldo negativo.** Non si può spendere ciò che non si ha.

```kotlin
// CORRETTO — clamp obbligatorio
val remaining = maxOf(0, received - used)

// SBAGLIATO — può produrre valori negativi
val remaining = received - used  // ← bug se used > received
```

Questa regola si applica in **tutti e due** i posti dove il calcolo è implementato:
- `GetMealVoucherSummaryUseCase` (domain, `shared/commonMain`)
- Calcolo inline in `HomeViewModel` (presentazione)

### Tabella regole complete

| Regola | Dove applicata |
|---|---|
| Solo `paymentType == MEAL_VOUCHERS` conta | `GetMealVoucherSummaryUseCase`, `HomeViewModel` |
| `received` = somma `mealVoucherCount` di INCOME MV | entrambi |
| `used` = somma `mealVoucherCount` di EXPENSE MV | entrambi |
| `remaining = maxOf(0, received - used)` ← **mai negativo** | entrambi |
| `remainingValue = remaining * voucherValue` | entrambi |
| `usedValue = used * voucherValue` | entrambi |
| Ritorna `null` se lista vuota O se `received == 0 && used == 0` | entrambi |
| `mealVoucherDifference` ≠ 0 **solo** su EXPENSE | validazione AddTransaction UI |
| `mealVoucherCount` ≥ 0 per ogni singola transazione | input normalizer (strip `-`) |

### Guard sull'inserimento — TODO noto

`AddTransactionState.isFormValid` ha un guard `voucherCount <= availableMealVouchers`
che impedisce di registrare più buoni di quanti disponibili.
**Problema attuale:** `AddTransactionViewModel.loadAvailableMealVouchers()` imposta
`availableMealVouchers = 999` (placeholder) invece di calcolare il saldo reale.

Formula corretta da implementare prima di completare qualsiasi feature MV:
```kotlin
// availableMealVouchers = saldo reale disponibile (mai negativo)
availableMealVouchers = maxOf(0,
    transactions.filter { it.paymentType == MEAL_VOUCHERS && it.type == INCOME }
                .sumOf { it.mealVoucherCount }
    -
    transactions.filter { it.paymentType == MEAL_VOUCHERS && it.type == EXPENSE }
                .sumOf { it.mealVoucherCount }
)
```

Finché il TODO non è risolto, un utente può registrare EXPENSE con count > available,
causando `received < used` nella sessione corrente.

### Checklist verifica

- [ ] `remaining` calcolato con `maxOf(0, received - used)` in `GetMealVoucherSummaryUseCase`.
- [ ] `remaining` calcolato con `maxOf(0, received - used)` nel calcolo inline di `HomeViewModel`.
- [ ] I due calcoli producono lo stesso risultato per gli stessi input.
- [ ] `availableMealVouchers` in `AddTransactionViewModel` non è hardcoded a 999.
- [ ] `MealVoucherCard` non riceve mai `summary.remaining < 0` (il clamp upstream lo garantisce).
- [ ] `mealVoucherDifference` ≠ 0 solo su transazioni EXPENSE.

**Verifica summary:** `GetMealVoucherSummaryUseCase` e il calcolo inline in `HomeViewModel`
devono produrre lo stesso risultato per gli stessi input. Se ne aggiorni uno, aggiorna l'altro.

---

## 5. Validazione transazione — `TransactionValidatorImpl`

Regole di validazione del dominio (tutte obbligatorie):

| Campo | Regola | Codice errore |
|---|---|---|
| `title` | non blank | `TRANSACTION_TITLE_EMPTY` |
| `amount` | > 0 (strettamente positivo) | `TRANSACTION_AMOUNT_NOT_POSITIVE` |
| `timestamp` | ≤ `Clock.System.now()` | `TRANSACTION_TIMESTAMP_FUTURE` |
| `category` | non blank | `TRANSACTION_CATEGORY_EMPTY` |

`ValidatedInsertTransactionUseCase` lancia `ValidationException(errors: List<ValidationError>)`
prima di toccare il repository. Il ViewModel gestisce con:
```kotlin
useCase(tx)
  .handleValidationError { errors -> _state.update { it.copy(validationErrors = errors) } }
  .handleOtherError { e -> _state.update { it.copy(errorState = ...) } }
```
**Non** usare `handleError {}` per `ValidationException` — perderebbe le singole `ValidationError`.

---

## 6. Date preset (Home / Transactions / Charts)

I preset sono indici interi definiti in `DateRangePreset`:

| Index | Intervallo |
|---|---|
| 0 | Oggi |
| 1 | Ultima settimana (default) |
| 2 | Ultimo mese (30 gg) |
| 3 | Ultimi 3 mesi |
| 4 | Ultimi 6 mesi |
| 5 | Ultimo anno |
| 6 | 2 anni |
| 7 | 3 anni |
| 8 | 5 anni |
| 9+ | Tutto il tempo |
| `CUSTOM_PRESET_INDEX` | Range personalizzato (from/to fissi) |

**Regola critica:** per preset non-custom, `from` e `to` vanno **ricalcolati dinamicamente**
al momento del filtraggio (non memorizzati come timestamp fissi). `HomeViewModel.filteredTransactionsFlow`
lo fa correttamente: rileggi quel pattern prima di clonarlo.

`setDateRange(from, to)` normalizza sempre: `normalizedFrom = min(from, to)`, `normalizedTo = max(from, to)`.

---

## 7. Verifica layer UseCase

**Checklist:**
- [ ] Estende la classe base corretta (vedi tabella in `antcashmanager-android-kmp`).
- [ ] `execute()` restituisce `R` grezzo, non `Result<R>`.
- [ ] Nessun `try/catch CancellationException` manuale (lo fa la base).
- [ ] Nessun `Log`, `println`, `android.*` import in `commonMain`.
- [ ] Dispatcher nel costruttore con default `Dispatchers.Default` (non `IO`).
- [ ] Eccezioni dominio: solo sealed class in `domain/exception/`.
- [ ] ≤ 250 righe, KDoc sulla classe.
- [ ] Registrato in `useCaseModule` con `factory { }`.
- [ ] Test in `commonTest` con `BaseUseCaseTest`.

```bash
# Verifica import Android proibiti in commonMain
grep -rn "android\." shared/src/commonMain/kotlin/ --include="*.kt"
# Verifica double-wrap Result
grep -rn "Result\." shared/src/commonMain/kotlin/com/antcashmanager/domain/usecase/ --include="*.kt" \
  | grep "execute"
```

---

## 8. Verifica layer ViewModel

**Checklist:**
- [ ] Estende `BaseViewModel<FeatureEvent>`.
- [ ] Un solo `private val _state = MutableStateFlow(FeatureState())`.
- [ ] `val state: StateFlow<FeatureState>` esposto pubblicamente.
- [ ] `override fun onEvent(event: E)` unico entry point pubblico, con `when` exhaustivo.
- [ ] Tutti i metodi che mutano stato sono `private`.
- [ ] Nessun `Context` iniettato.
- [ ] Nessun repository eccetto `SettingsRepository` (eccezione documentata).
- [ ] Logging via `logDebug/logInfo/logWarn/logError` (mai `Log.d`, `println`, `TAG`).
- [ ] `Flow<Result<R>>`: `.onSuccess { }.onFailure { if (it is CancellationException) throw it }`.
- [ ] `activeJob?.cancel()` prima di riavviare operazioni long-running.
- [ ] ≤ 300 righe; `FeatureState.kt` ≤ 100 righe in file separato.

```bash
# Verifica Context nei ViewModel
grep -rn "Context" androidApp/src/main/kotlin/com/antcashmanager/android/ui/screen/ \
  --include="*ViewModel.kt"
# Verifica repository non-Settings iniettati
grep -rn "Repository" androidApp/src/main/kotlin/com/antcashmanager/android/ui/screen/ \
  --include="*ViewModel.kt" | grep -v "SettingsRepository\|//\|import"
```

---

## 9. Verifica layer Screen (Compose)

**Checklist:**
- [ ] `FeatureScreen` inietta VM con `koinViewModel()`, raccoglie con `collectAsStateWithLifecycle()`.
- [ ] Passa `state` e `onEvent` al composable `FeatureContent`; non passa il VM.
- [ ] Zero accesso a repository o logica di business nella Screen.
- [ ] Navigazione solo tramite `NavigationExtensions.kt` e `AppRoute`.
- [ ] Colori: solo `MaterialTheme.colorScheme.*`, `IncomeGreen`, `ExpenseRed`.
- [ ] Spaziatura: 8/16/24 dp.
- [ ] `@Preview` light+dark+2x per composable in `view/` e `ui/components/`; nessun preview su Screen root.
- [ ] Nessuna stringa letterale nella UI (`stringResource(R.string.key)`).

```bash
# Verifica stringhe letterali nella UI
grep -rn '"[A-Za-z ]\{3,\}"' androidApp/src/main/kotlin/com/antcashmanager/android/ui/screen/ \
  --include="*.kt" | grep -v "//\|import\|package\|Constant\|@"
```

---

## 10. Verifica DI (Koin)

**Checklist:**
- [ ] Ogni nuovo UseCase in `useCaseModule` con `factory { }`.
- [ ] Ogni nuovo ViewModel in `presentationModule` con `viewModel { }` o `viewModelOf(::X)`.
- [ ] ViewModel parametrizzato (ID opzionale): `viewModel { (id: Long?) -> FeatureViewModel(get(), get(), id) }`.
- [ ] Repository/service solo in `dataModule`.
- [ ] Nessun `get()` di tipo sbagliato (tipo Koin inferito dal tipo del parametro costruttore).

```bash
grep -n "factory\|viewModel" \
  androidApp/src/main/kotlin/com/antcashmanager/android/di/AppModule.kt | tail -30
```

---

## 11. Verifica i18n — 13 locale

Directory target: `values/` + `values-{it,fr,de,es,hi,ja,ko,pl,ru,uk,zh,zh-rTW}/strings.xml`.

**Checklist:**
- [ ] Cerca prima se la stringa esiste (`grep -rn "candidate" androidApp/src/main/res/values*/`).
- [ ] Intraducibile (nome proprio, simbolo, pattern): solo in `values/untranslable.xml` con `translatable="false"`.
- [ ] Traducibile: aggiunta in tutti e 13 i `strings.xml`.
- [ ] Naming: `<screen>_<componente>_<descrizione>`.
- [ ] Nessun duplicato semantico (stessa stringa con chiave diversa).

```bash
# Conta le chiavi per locale (devono essere ≥ quella del values/ EN)
for dir in androidApp/src/main/res/values*/; do
  echo -n "$dir: "; grep -c "<string " "$dir/strings.xml" 2>/dev/null || echo "0"
done
```

---

## 12. Checklist completa per nuova feature

Prima di dichiarare "done", verifica nell'ordine:

1. **Build pulita** `./gradlew :androidApp:compileFullDebugKotlin 2>&1 | tail -30` → 0 errori.
2. **Domain**: UseCase ha `execute()` che restituisce raw value; test in `commonTest` passano.
3. **Calcoli**: tutti i calcoli finanziari usano `withCorrectAmounts()` prima di sommare.
4. **Validation**: `amount > 0` e `title` non blank verificati a livello domain, non solo UI.
5. **ViewModel**: `onEvent()` exhaustivo; stato aggiornato via `_state.update { it.copy(...) }`.
6. **Screen**: zero logica di business; navigazione via `NavigationExtensions.kt`.
7. **DI**: UseCase e ViewModel registrati in `AppModule.kt`.
8. **i18n**: tutte le 13 locale aggiornate.
9. **Test mirati**: `./gradlew :androidApp:testFullDebugUnitTest --tests "*FeatureViewModel*" 2>&1 | tail -30`.
10. **Git policy**: mostra `git diff --stat`, proponi commit message, attendi `procedi`/`vai`/`sì`.
