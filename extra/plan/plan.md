# Piani di Implementazione — AntCashManager

**Progetto:** AntCashManager
**Ultimo aggiornamento:** 9 agosto 2026

---

# PIANO 1 — Nuovi Grafici, Migliorie e Responsive Layout — Screen Charts

**Feature:** Charts Screen — nuovi grafici spese, previsioni e ottimizzazione layout adattivo
**Stato:** 📋 In attesa di implementazione

---

## Contesto

La screen Charts mostra attualmente 7 sezioni:
- 2 pie chart per categoria (entrate/uscite)
- 2 top-category list (entrate/uscite)
- 1 pie chart ripartizione metodo di pagamento
- 1 bar chart mensile (entrate/uscite per mese)
- 1 bar chart annuale (entrate/uscite per anno)

---

## File Coinvolti

| File | Ruolo |
|------|-------|
| `androidApp/.../ui/screen/charts/ChartData.kt` | Modelli dati grafici |
| `androidApp/.../ui/screen/charts/ChartsViewModel.kt` | Logica aggregazione dati |
| `androidApp/.../ui/screen/charts/ChartsConstant.kt` | Costanti condivise |
| `androidApp/.../ui/screen/charts/view/ChartComponents.kt` | Componenti Canvas grafici |
| `androidApp/.../ui/screen/charts/view/ChartsScreen.kt` | UI principale + card |
| `androidApp/src/main/res/values*/strings.xml` | Stringhe localizzate (5 lingue) |
| `androidApp/.../ui/charts/ChartsViewModelTest.kt` | Unit test ViewModel |

---

## PARTE 1 — Nuovi Grafici

### Step 1 — Estendere `ChartData.kt`

Aggiungere:
- `data class DailyAmount(val dateLabel: String, val expense: Double)`
- Campo `dailyTimeline: List<DailyAmount> = emptyList()`
- Campo `expenseByWeekday: Map<Int, Double> = emptyMap()` (Int 1=Lun … 7=Dom)

### Step 2 — Aggiornare `ChartsViewModel.kt`

Estendere `buildChartData()`:

**a) `expenseByWeekday`:**
```
EXPENSE transactions
  → groupBy { Calendar.DAY_OF_WEEK }
  → mapValues { media spesa }
  → normalizzare a 1=Lun … 7=Dom (convenzione UE)
```

**b) `dailyTimeline`:**
```
EXPENSE transactions
  → groupBy { "yyyy-MM-dd" }
  → mapValues { somma assoluta spese del giorno }
  → sorted ascending
  → mapped to List<DailyAmount>
```

### Step 3 — Aggiornare `ChartsConstant.kt`

```kotlin
const val LINE_CHART_HEIGHT_COMPACT_DP = 180
const val LINE_CHART_HEIGHT_MEDIUM_DP = 220
const val LINE_CHART_HEIGHT_TABLET_DP = 280
const val WEEKDAY_CHART_HEIGHT_COMPACT_DP = 160
const val WEEKDAY_CHART_HEIGHT_MEDIUM_DP = 200
const val WEEKDAY_CHART_HEIGHT_TABLET_DP = 220
const val FORECAST_MONTHS_LOOKBACK = 3
const val SAVINGS_GAUGE_STROKE_DP = 14
const val BAR_CHART_HEIGHT_MEDIUM_DP = 220
const val BAR_CHART_HEIGHT_TABLET_DP = 300
const val BAR_CHART_FOLDABLE_MIN_WIDTH_DP = 240
const val PERIOD_FILTER_CHIP_SPACING_DP = 4
```

### Step 4 — Nuovi componenti Canvas in `ChartComponents.kt`

#### 4a. `ExpenseLineChart`
- Linea con punti + area sfumata (Brush `primaryContainer` → transparent) + grid orizzontali
- Parametri: `data: List<DailyAmount>`, `modifier: Modifier`

#### 4b. `WeekdayBarChart`
- 7 barre verticali Lun–Dom, barra col valore massimo evidenziata
- Parametri: `data: Map<Int, Double>`, `modifier: Modifier`

#### 4c. `SavingsRateGauge`
- Arco circolare Canvas da 135° con sweep 270°
- `secondaryContainer` se tasso > 0, `errorContainer` se negativo
- Percentuale al centro con `typography.headlineMedium`
- Parametri: `savingsRate: Double`, `modifier: Modifier`

### Step 5 — Nuove card in `ChartsScreen.kt`

| Card | Dati | Condizione visibilità |
|------|------|-----------------------|
| `SavingsRateCard` | `totalIncome`, `totalExpense` (già presenti) | `totalIncome > 0` |
| `SpendingForecastCard` | media ultimi `FORECAST_MONTHS_LOOKBACK` mesi da `monthlyData` | `monthlyData.size >= 2` |
| `QuickStatsCard` | griglia 2×2 da `monthlyData` (`remember`) | `monthlyData.isNotEmpty()` |
| `DailyExpenseLineChartCard` | `chartData.dailyTimeline` | `dailyTimeline.size >= 3` |
| `WeekdayExpenseCard` | `chartData.expenseByWeekday` | `expenseByWeekday.isNotEmpty()` |

> `SpendingForecastCard` e `QuickStatsCard` sono **puramente presentazionali**: calcolate con `remember` in `ChartsContent`, nessun UseCase aggiuntivo (YAGNI).

**Posizione nel layout phone (ordine finale):**
1. ScreenHeader + PeriodFilter *(sticky)*
2. ChartsSummaryRow *(sticky)*
3. **SavingsRateCard** ← NUOVO
4. **SpendingForecastCard** ← NUOVO
5. **QuickStatsCard** ← NUOVO
6. Pie income + TopIncome
7. Pie expense + TopExpense
8. Pie ripartizione pagamenti
9. MonthlyBarChart
10. YearlyBarChart
11. **DailyExpenseLineChartCard** ← NUOVO
12. **WeekdayExpenseCard** ← NUOVO

### Step 6 — Stringhe localizzate (5 lingue)

```xml
<!-- Savings Rate -->
<string name="charts_savings_rate_title">Tasso di Risparmio</string>
<string name="charts_savings_rate_subtitle">del reddito risparmiato</string>
<string name="charts_savings_rate_positive">Bilancio positivo</string>
<string name="charts_savings_rate_negative">Bilancio negativo</string>

<!-- Forecast -->
<string name="charts_forecast_title">Previsione Spese</string>
<string name="charts_forecast_subtitle">Basata sugli ultimi 3 mesi</string>
<string name="charts_forecast_under">Sotto la previsione</string>
<string name="charts_forecast_over">Sopra la previsione</string>
<string name="charts_forecast_current_label">Spesa attuale</string>
<string name="charts_forecast_predicted_label">Previsione</string>

<!-- Quick Stats -->
<string name="charts_quick_stats_title">Statistiche Rapide</string>
<string name="charts_quick_stats_avg_income">Media mensile entrate</string>
<string name="charts_quick_stats_avg_expense">Media mensile uscite</string>
<string name="charts_quick_stats_best_month">Miglior mese</string>
<string name="charts_quick_stats_worst_month">Mese più costoso</string>

<!-- Daily Trend -->
<string name="charts_daily_trend_title">Trend Spese Giornaliere</string>
<string name="charts_daily_trend_peak">Picco di spesa</string>

<!-- Weekday Distribution -->
<string name="charts_weekday_title">Distribuzione per Giorno</string>
<string name="charts_weekday_subtitle">Spesa media per giorno della settimana</string>
<string name="charts_weekday_mon">Lun</string>
<string name="charts_weekday_tue">Mar</string>
<string name="charts_weekday_wed">Mer</string>
<string name="charts_weekday_thu">Gio</string>
<string name="charts_weekday_fri">Ven</string>
<string name="charts_weekday_sat">Sab</string>
<string name="charts_weekday_sun">Dom</string>
```

### Step 7 — Nuovi test `ChartsViewModelTest`

- `buildChartData_shouldCalculateWeekdayDistribution_whenExpenseTransactionsPresent`
- `buildChartData_shouldBuildDailyTimeline_whenTransactionsInRange`
- `buildChartData_shouldReturnEmptyWeekday_whenNoExpenseTransactions`
- `buildChartData_shouldNormalizeWeekdayKeys_toEuropeanConvention`

---

## PARTE 2 — Migliorie Layout Adattivo (Tablet e Smartphone)

### Problemi attuali

| Dispositivo | Problema |
|-------------|----------|
| **Phone** | Tutto in colonna singola: molto scroll, nessuna visione d'insieme |
| **Phone** | Chip periodo in scroll orizzontale, poco intuitivo |
| **Tablet 7" isMedium** | Ripartizione pagamenti a piena larghezza crea asimmetria |
| **Tablet 10"+ isExpanded** | Grafici bar nelle colonne troppo stretti |
| **Foldable** | Rischio overflow dalla NavigationRail |

### Migliorie Phone

| ID | Miglioria |
|----|-----------|
| P1 | `LazyColumn + stickyHeader` per PeriodFilter sempre visibile durante lo scroll |
| P2 | Card collapsable: chevron `ExpandMore/ExpandLess` + `AnimatedVisibility` sul body |
| P3 | `ChartsSummaryRow` sticky nello stesso `stickyHeader` del filtro periodo |
| P4 | FAB scroll-to-top (`KeyboardArrowUp`) visibile dopo > 200dp scroll |

### Migliorie Tablet 7" (`isMedium` → 600–839dp)

| ID | Miglioria |
|----|-----------|
| T1 | 2 colonne bilanciate: col1 = income+savings, col2 = expense+forecast; riga 2 = payment(50%)+quickstats(50%) |
| T2 | `BAR_CHART_HEIGHT_MEDIUM_DP = 220` (era identico a compact 180dp) |
| T3 | Chip periodo in `FlowRow` automatico, senza scroll orizzontale |

### Migliorie Tablet 10"+ (`isExpanded` → ≥840dp)

| ID | Miglioria |
|----|-----------|
| E1 | `Row(1f,1f,1f)` per SavingsRate+Forecast+QuickStats; `Row(2f,1f)` per DailyLine+Weekday |
| E2 | `MonthlyBarChart` e `YearlyBarChart` a **piena larghezza** (non affiancati) per leggibilità asse X |
| E3 | Chip periodo su riga singola senza scroll + date from/to inline accanto ai chip |
| E4 | `BAR_CHART_HEIGHT_TABLET_DP = 300` (da 260dp) |

### Migliorie Foldable (`isFoldableDevice`)

| ID | Miglioria |
|----|-----------|
| F1 | Spacing colonne 8dp (da `TABLET_COLUMNS_SPACING_DP = 16dp`) |
| F2 | `coerceAtLeast(BAR_CHART_FOLDABLE_MIN_WIDTH_DP)` = 240dp (da 300dp) per evitare overflow |

---

## Checklist — Charts

### Nuovi Grafici
- [ ] Step 1: `ChartData` esteso (`DailyAmount`, `dailyTimeline`, `expenseByWeekday`)
- [ ] Step 2: ViewModel aggiornato (`buildChartData`)
- [ ] Step 3: `ChartsConstant` aggiornato
- [ ] Step 4a: `ExpenseLineChart` Canvas
- [ ] Step 4b: `WeekdayBarChart` Canvas
- [ ] Step 4c: `SavingsRateGauge` Canvas
- [ ] Step 5a: `SavingsRateCard`
- [ ] Step 5b: `SpendingForecastCard`
- [ ] Step 5c: `QuickStatsCard`
- [ ] Step 5d: `DailyExpenseLineChartCard`
- [ ] Step 5e: `WeekdayExpenseCard`
- [ ] Step 6: Stringhe 5 lingue
- [ ] Step 7: Test ViewModel aggiornati

### Layout Adattivo
- [ ] P1: `LazyColumn + stickyHeader` su phone
- [ ] P2: Card collapsable su phone
- [ ] P3: `ChartsSummaryRow` sticky
- [ ] P4: FAB scroll-to-top
- [ ] T1: 2 colonne bilanciate su `isMedium`
- [ ] T2: `BAR_CHART_HEIGHT_MEDIUM_DP = 220`
- [ ] T3: Chip in `FlowRow` su `isMedium`
- [ ] E1: 3 colonne nuove card su `isExpanded`
- [ ] E2: Bar chart full-width su `isExpanded`
- [ ] E3: `PeriodFilterCard` espanso su `isExpanded`
- [ ] E4: `BAR_CHART_HEIGHT_TABLET_DP = 300`
- [ ] F1: Spacing 8dp su foldable
- [ ] F2: `coerceAtLeast(240)` bar chart foldable

### Qualità
- [ ] `@Preview` light + dark per ogni nuova card
- [ ] Nessuna stringa hardcoded
- [ ] Import puliti su tutti i file modificati
- [ ] Package name corretto su tutti i file nuovi

---

---

# PIANO 2 — Sezione "Porting da App Esterne" — Gestione Dati

**Feature:** Settings > Gestione Dati — nuova sezione Porting con export schema DB per AI esterna
**Stato:** 📋 In attesa di implementazione

---

## Contesto

La schermata `SettingsDataScreen` gestisce backup/ripristino, sicurezza e suggerimenti.
La nuova sezione **"Porting da app esterne"** permette all'utente di:
1. Esportare un file JSON statico che **descrive la struttura del database** (schema) — non i dati reali
2. Usare quel file + l'export della propria app precedente come input per una AI esterna
3. L'AI produce un file di ripristino compatibile → importabile con "Ripristino Dati" esistente

---

## File Coinvolti

| File | Tipo | Ruolo |
|------|------|-------|
| `androidApp/.../data/backup/SchemaExportService.kt` | **NUOVO** | Genera il JSON schema del DB |
| `androidApp/.../dataManagement/SettingsDataConstant.kt` | Modifica | Costanti schema export |
| `androidApp/.../dataManagement/SettingsDataState.kt` | Modifica | Stati + `SchemaExportResult` |
| `androidApp/.../dataManagement/SettingsDataViewModel.kt` | Modifica | Logica export schema |
| `androidApp/.../dataManagement/SettingsDataScreen.kt` | Modifica | `PortingSection` + launcher + dialog |
| `androidApp/.../di/AppModule.kt` | Modifica | Registrare `SchemaExportService` in Koin |
| `androidApp/src/main/res/values*/strings.xml` | Modifica | Stringhe 5 lingue |
| `androidApp/.../dataManagement/SettingsDataViewModelTest.kt` | Modifica | Nuovi test unitari |

---

## Step 1 — Creare `SchemaExportService.kt`

**Package:** `com.antcashmanager.android.data.backup`

Servizio puro: zero accesso DB, zero dipendenze di dominio.
Usa `kotlinx.serialization` con `Json { prettyPrint = true }`.
Il campo `format_version` legge `BackupConstants.CURRENT_VERSION` a runtime (mai hardcoded).

**Struttura JSON prodotto:**

```json
{
  "app": "AntCashManager",
  "format_version": 2,
  "generated_at": "<ISO timestamp>",
  "description": "Schema del formato backup/ripristino di AntCashManager. Usalo insieme all'export della tua app precedente per chiedere a una AI di creare un file di ripristino compatibile.",
  "instructions_for_ai": "L'utente ti fornirà questo schema e i propri dati esportati da un'altra app. Converti i dati nel formato JSON descritto in backup_structure rispettando tipi, valori enum e default. Imposta sempre settings: null.",
  "backup_structure": {
    "version":   { "type": "Int",  "fixed_value": 2 },
    "timestamp": { "type": "Long", "description": "Unix millis", "example": 1750000000000 },
    "transactions": {
      "type": "Array<TransactionBackup>",
      "item_schema": {
        "id":                 { "type": "Long",    "description": "ID univoco sequenziale da 1", "example": 1 },
        "title":              { "type": "String",  "example": "Supermercato" },
        "amount":             { "type": "Double",  "description": "Sempre positivo (valore assoluto)", "example": 45.50 },
        "category":           { "type": "String",  "description": "Deve corrispondere a un 'name' nell'array categories", "example": "Alimentari" },
        "type":               { "type": "Enum",    "values": ["INCOME", "EXPENSE"] },
        "timestamp":          { "type": "Long",    "description": "Data transazione Unix millis", "example": 1749000000000 },
        "notes":              { "type": "String",  "default": "" },
        "payee":              { "type": "String",  "default": "" },
        "location":           { "type": "String",  "default": "" },
        "isRecurring":        { "type": "Boolean", "default": false },
        "tags":               { "type": "String",  "description": "Tag separati da virgola", "default": "" },
        "recurrenceInterval": { "type": "Enum",    "values": ["", "DAILY", "WEEKLY", "MONTHLY", "YEARLY"], "default": "" },
        "paymentType":        { "type": "Enum",    "values": ["ELECTRONIC", "CASH", "MEAL_VOUCHERS"], "default": "ELECTRONIC" },
        "mealVoucherCount":   { "type": "Int",     "default": 0 },
        "categoryIcon":       { "type": "String",  "description": "Emoji es. 🛒", "default": "" },
        "categoryColor":      { "type": "Long",    "description": "ARGB Long decimale es. 4283215696 = #FF90A4AE", "default": 4283215696 }
      }
    },
    "categories": {
      "type": "Array<CategoryBackup>",
      "item_schema": {
        "id":        { "type": "Long",    "description": "ID univoco sequenziale da 1", "example": 1 },
        "name":      { "type": "String",  "example": "Alimentari" },
        "icon":      { "type": "String",  "description": "Emoji", "example": "🛒", "default": "category" },
        "color":     { "type": "Long",    "description": "ARGB Long decimale", "example": 4283215696 },
        "type":      { "type": "Enum",    "values": ["INCOME", "EXPENSE"], "default": "EXPENSE" },
        "isDefault": { "type": "Boolean", "default": false },
        "sortOrder": { "type": "Int",     "default": 0 },
        "isHidden":  { "type": "Boolean", "default": false }
      }
    },
    "settings": {
      "type": "SettingsBackup | null",
      "description": "Raccomandato: null — non sovrascrive le impostazioni correnti dell'utente"
    }
  },
  "minimal_example": {
    "version": 2,
    "timestamp": 1750000000000,
    "transactions": [
      { "id": 1, "title": "Stipendio",    "amount": 2500.0, "category": "Lavoro",     "type": "INCOME",  "timestamp": 1749000000000, "paymentType": "ELECTRONIC" },
      { "id": 2, "title": "Supermercato", "amount": 45.50,  "category": "Alimentari", "type": "EXPENSE", "timestamp": 1749100000000, "paymentType": "CASH" },
      { "id": 3, "title": "Bolletta",     "amount": 80.00,  "category": "Casa",        "type": "EXPENSE", "timestamp": 1749200000000, "paymentType": "ELECTRONIC" }
    ],
    "categories": [
      { "id": 1, "name": "Lavoro",     "icon": "💼", "color": 4284913919, "type": "INCOME"  },
      { "id": 2, "name": "Alimentari", "icon": "🛒", "color": 4283215696, "type": "EXPENSE" },
      { "id": 3, "name": "Casa",       "icon": "🏠", "color": 4284375552, "type": "EXPENSE" }
    ],
    "settings": null
  }
}
```

---

## Step 2 — `SettingsDataConstant.kt`

```kotlin
const val SCHEMA_FILE_PREFIX = "antcashmanager_schema_"
const val SCHEMA_FILE_SUFFIX = ".json"
```

---

## Step 3 — `SettingsDataState.kt`

Aggiungere a `SettingsDataState`:

```kotlin
val pendingSchemaExportData: String? = null,
val pendingSchemaExportFileName: String? = null,
val showSchemaExportSuccessDialog: Boolean = false,
val schemaExportResult: SchemaExportResult = SchemaExportResult.Idle,
val schemaExportErrorMessage: String = "",
```

Nuovo sealed interface:

```kotlin
sealed interface SchemaExportResult {
    data object Idle : SchemaExportResult
    data object Loading : SchemaExportResult
    data object Success : SchemaExportResult
    data class Error(val message: String) : SchemaExportResult
}
```

---

## Step 4 — `SettingsDataViewModel.kt`

Aggiungere `private val schemaExportService: SchemaExportService` al costruttore primario.

Funzioni da aggiungere:
- `exportDatabaseSchema()` — chiama `schemaExportService.buildSchemaJson()`, imposta `pendingSchemaExportData` e `pendingSchemaExportFileName` con timestamp
- `onSchemaExportFileSaved()` — pulisce pending, imposta `showSchemaExportSuccessDialog = true`
- `onSchemaExportFileSaveError(message: String)` — aggiorna errore stato
- `clearPendingSchemaExport()` — pulisce pending senza successo (utente annulla file picker)
- `dismissSchemaExportSuccessDialog()` — chiude dialog

---

## Step 5 — `SettingsDataScreen.kt`

**Nuovi parametri in `SettingsDataContent`:**
```kotlin
onExportDatabaseSchema: () -> Unit = {},
onSchemaExportFileSaved: () -> Unit = {},
onSchemaExportFileSaveError: (String) -> Unit = {},
onClearPendingSchemaExport: () -> Unit = {},
onDismissSchemaExportSuccessDialog: () -> Unit = {},
```

**Nuovo launcher** (stesso pattern di `backupLauncher`):
```kotlin
val schemaExportLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/json")
) { uri ->
    if (uri == null) { onClearPendingSchemaExport(); return@rememberLauncherForActivityResult }
    val jsonData = state.pendingSchemaExportData
    if (jsonData.isNullOrBlank()) { onSchemaExportFileSaveError(SettingsDataConstant.UNKNOWN_ERROR); return@... }
    try {
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(jsonData.toByteArray(StandardCharsets.UTF_8))
        }
        analyticsManager.logEvent("schema_export_file_saved")
        onSchemaExportFileSaved()
    } catch (e: Exception) {
        onSchemaExportFileSaveError(e.message ?: SettingsDataConstant.UNKNOWN_ERROR)
    }
}
```

**LaunchedEffect su `state.pendingSchemaExportFileName`:**
```kotlin
LaunchedEffect(state.pendingSchemaExportFileName) {
    state.pendingSchemaExportFileName?.let { fileName ->
        schemaExportLauncher?.launch(fileName) ?: onSchemaExportFileSaveError(filePickerUnavailableMessage)
    }
}
```

**Composable privato `PortingSection`:**
```kotlin
@Composable
private fun PortingSection(onExportDatabaseSchema: () -> Unit) {
    AppCardSectionHeader(title = stringResource(R.string.settings_porting_title))
    Spacer(modifier = Modifier.height(SettingsDataConstant.CARD_SPACING_DP.dp))
    Column(verticalArrangement = Arrangement.spacedBy(SettingsDataConstant.CARD_SPACING_DP.dp)) {
        // Card descrittiva con istruzioni passo per passo (non cliccabile)
        AppCard(
            title = stringResource(R.string.settings_porting_how_it_works),
            subtitle = stringResource(R.string.settings_porting_description),
            leadingIcon = Icons.Default.Info,
            showChevron = false,
        )
        // Pulsante export schema
        AppCard(
            title = stringResource(R.string.settings_porting_export_schema),
            subtitle = stringResource(R.string.settings_porting_export_schema_subtitle),
            leadingIcon = Icons.AutoMirrored.Filled.OpenInNew,
            iconBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
            onClick = onExportDatabaseSchema,
        )
    }
}
```

**Dialog di successo:**
```kotlin
if (state.showSchemaExportSuccessDialog) {
    AlertDialog(
        onDismissRequest = onDismissSchemaExportSuccessDialog,
        title = { AppText(stringResource(R.string.settings_porting_success_title)) },
        text = { AppText(stringResource(R.string.settings_porting_success_message)) },
        confirmButton = {
            TextButton(onClick = onDismissSchemaExportSuccessDialog) {
                AppText(stringResource(R.string.dialog_ok))
            }
        }
    )
}
```

**Posizione nel layout:**
- **Phone** (`isCompact`): `item { PortingSection(...) }` come ultimo elemento in `LazyColumn`
- **Tablet** (`!isCompact`): `PortingSection(...)` in fondo alla seconda `Column`

---

## Migliorie Layout Adattivo — Sezione Porting

### Phone
- Card descrittiva multi-riga: verificare che `AppCard` mostri il subtitle completo (`maxLines = Int.MAX_VALUE`)
- Su schermi < 360dp: aggiungere collapsable per le istruzioni (stesso pattern chevron pianificato per Charts)

### Tablet 7" (`isMedium`)
- `PortingSection` in fondo alla **seconda colonna** (con `SecuritySection` + `SuggestionsSection`)
- Se la seconda colonna risulta troppo carica: spostare `PortingSection` a piena larghezza sotto entrambe le colonne

### Tablet 10"+ (`isExpanded`)
- Stessa posizione di `isMedium`
- Le istruzioni su schermi larghi possono essere presentate come lista puntata numerata anziché stringa multi-riga per maggiore leggibilità

### Foldable
- Nessuna gestione speciale: segue automaticamente il layout tablet della seconda colonna

---

## Step 6 — Stringhe localizzate (5 lingue)

| Chiave | IT | EN | FR | DE | ES |
|--------|----|----|----|----|-----|
| `settings_porting_title` | Porting da app esterne | Import from other apps | Import depuis d'autres apps | Import aus anderen Apps | Importar desde otras apps |
| `settings_porting_how_it_works` | Come funziona | How it works | Comment ça marche | So funktioniert es | Cómo funciona |
| `settings_porting_description` | 1. Tocca "Esporta struttura database" e salva il file\n2. Esporta i dati dall\'app precedente (CSV/Excel/JSON)\n3. Apri un assistente AI (ChatGPT, Gemini, Claude...)\n4. Carica entrambi i file nell\'AI\n5. Chiedi: "Converti i miei dati nel formato AntCashManager descritto nello schema"\n6. Salva il file JSON prodotto dall\'AI\n7. Usa "Ripristino Dati" qui sotto per importarlo | *(EN analoga)* | *(FR analoga)* | *(DE analoga)* | *(ES analoga)* |
| `settings_porting_export_schema` | Esporta struttura database | Export database schema | Exporter le schéma | Datenbankschema exportieren | Exportar esquema de BD |
| `settings_porting_export_schema_subtitle` | Genera il file schema da dare all\'AI | Generate the schema file for the AI | Générer le fichier schéma pour l\'IA | Schema-Datei für KI generieren | Generar archivo esquema para IA |
| `settings_porting_success_title` | Schema esportato | Schema exported | Schéma exporté | Schema exportiert | Esquema exportado |
| `settings_porting_success_message` | File schema salvato. Segui le istruzioni nella sezione per completare il porting con l\'AI. | Schema file saved. Follow the section instructions to complete the porting with AI. | *(FR analoga)* | *(DE analoga)* | *(ES analoga)* |

---

## Step 7 — `AppModule.kt`

```kotlin
// Nel dataModule
single { SchemaExportService() }

// Nel viewModel SettingsDataViewModel — aggiungere get<SchemaExportService>()
```

---

## Step 8 — Test unitari `SettingsDataViewModelTest`

- `exportDatabaseSchema_shouldSetPendingSchemaData_whenServiceSucceeds`
- `exportDatabaseSchema_shouldSetError_whenServiceFails`
- `onSchemaExportFileSaved_shouldShowSuccessDialogAndClearPending`
- `onSchemaExportFileSaveError_shouldSetErrorMessageAndClear`
- `clearPendingSchemaExport_shouldClearBothDataAndFileName`
- `dismissSchemaExportSuccessDialog_shouldResetDialogAndResult`

---

## Note Architetturali

- **`SchemaExportService` puro**: zero DB, zero dominio. Non richiede UseCase (YAGNI).
- **Nessuna cifratura**: il file schema non contiene dati utente; non usare `BackupPayloadCipher`.
- **`format_version`** legge `BackupConstants.CURRENT_VERSION` a runtime (mai hardcoded).
- **`@Preview`** light + dark obbligatorie per `PortingSection`.

---

## Checklist — Porting

- [ ] Step 1: `SchemaExportService` con JSON completo + `minimal_example`
- [ ] Step 2: Costanti in `SettingsDataConstant`
- [ ] Step 3: `SettingsDataState` aggiornato + `SchemaExportResult`
- [ ] Step 4: `SettingsDataViewModel` con tutte le funzioni
- [ ] Step 5: `SettingsDataScreen` (launcher, LaunchedEffect, `PortingSection`, dialog, firme aggiornate)
- [ ] Step 6: Stringhe 5 lingue con istruzioni passo per passo
- [ ] Step 7: `AppModule` aggiornato
- [ ] Step 8: Test unitari
- [ ] `@Preview` light + dark per `PortingSection`
- [ ] `format_version` usa `BackupConstants.CURRENT_VERSION` a runtime
- [ ] Nessuna stringa hardcoded
- [ ] Import puliti su tutti i file modificati

