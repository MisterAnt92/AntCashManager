# AntCashManager - Architecture Guidelines

## Panoramica
Questo documento definisce le linee guida architetturali per lo sviluppo dell'app **AntCashManager**, garantendo codice pulito, manutenibile e testabile seguendo i principi di Clean Architecture.

## Aggiornamenti Critici (allineamento corrente)

Questa guida contiene esempi storici: applicare sempre questi aggiornamenti come fonte prioritaria.

### Metadati Progetto

| Campo | Valore |
|---|---|
| Versione riferimento | `1.4.6` |
| Package name (`applicationId`) | `com.sformica.ant_cashmanager` |
| Namespace Android | `com.antcashmanager.android` |

### Correzioni operative

- Dependency Injection: usare **Koin** (non manual DI via container in Activity).
- UseCase: estendere le base class (`BaseUseCase`, `FlowUseCase`, ecc.) e implementare `execute()`.
- UseCase output: usare `Result<T>` con eccezioni dominio in `shared/commonMain/domain/exception/`.
- ViewModel: consumare `Result` con `onSuccess`/`onFailure`, rilanciando `CancellationException`.
- Testing: preferire fake repository a Mockito nelle aree KMP.

Per dettagli aggiornati su script e conversioni dati consultare:
- `wiki/CONVERSION_GUIDE.md`
- `wiki/SCRIPT_CONVERSION_README.md`

---

## 1. Clean Architecture - Struttura a Layer

L'applicazione segue una struttura a **3 layer**:

```
┌─────────────────────────────────────────┐
│   PRESENTATION LAYER (androidApp)      │
│   - Screen (Composables)                │
│   - ViewModel                           │
│   - UI State                            │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│   DOMAIN LAYER (shared/commonMain)      │
│   - Use Cases                           │
│   - Repository Interfaces               │
│   - Domain Models                       │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│   DATA LAYER (shared/androidMain)       │
│   - Repository Implementations          │
│   - Data Sources (Local/Remote)         │
│   - Database (Room)                     │
│   - Entity/DTOs                         │
└─────────────────────────────────────────┘
```

### Regole di Dipendenza
- ✅ **Presentation** può dipendere da **Domain**
- ✅ **Domain** NON dipende da nessuno (puro Kotlin)
- ✅ **Data** implementa le interfacce di **Domain**
- ❌ **Domain** NON deve mai dipendere da **Presentation** o **Data**

---

## 2. Pattern Architetturale: UseCase → ViewModel → State → Screen

### 2.1 UseCase (Domain Layer)

**Responsabilità**: Incapsulare una singola logica di business.

**Template**:
```kotlin
package com.antcashmanager.domain.usecase

import com.antcashmanager.domain.repository.YourRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase per [descrizione funzionalità].
 * 
 * @param repository Repository necessario per l'operazione
 */
class YourFeatureUseCase(
    private val repository: YourRepository,
) {
    /**
     * Esegue [operazione].
     * 
     * @param param Parametro necessario
     * @return Flow con il risultato dell'operazione
     */
    operator fun invoke(param: String): Flow<Result<Data>> {
        return repository.getData(param)
    }
}
```

**Best Practices**:
- ✅ Nome descrittivo che termina con `UseCase`
- ✅ Una sola responsabilità (Single Responsibility Principle)
- ✅ Usare `operator fun invoke()` per rendere la classe callable
- ✅ Massimo 150 righe di codice
- ✅ Documentazione KDoc completa
- ❌ NON contenere logica di UI
- ❌ NON accedere direttamente a database o network

**Esempio Reale**:
```kotlin
// androidApp/src/main/kotlin/com/antcashmanager/android/domain/usecase/feedback/SendFeedbackEmailUseCase.kt
class SendFeedbackEmailUseCase {
    fun sendFeedbackEmail(
        applicationContext: Context,
        emailBody: String,
        versionName: String,
    ): Boolean {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("misterant.developer@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "AntCashManager Feedback - v$versionName")
            putExtra(Intent.EXTRA_TEXT, emailBody)
        }
        return if (emailIntent.resolveActivity(applicationContext.packageManager) != null) {
            applicationContext.startActivity(emailIntent)
            true
        } else {
            false
        }
    }
}
```

---

### 2.2 ViewModel (Presentation Layer)

**Responsabilità**: Gestire lo stato UI e coordinare i UseCase.

**Template**:
```kotlin
package com.antcashmanager.android.ui.screen.yourfeature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class YourFeatureViewModel(
    private val yourUseCase: YourFeatureUseCase,
    private val repository: YourRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "YourFeatureViewModel"
        private const val SHARING_TIMEOUT = 5_000L
        private const val DEFAULT_VALUE = ""
    }

    // State management con StateFlow
    private val _state = MutableStateFlow(YourFeatureState())
    val state: StateFlow<YourFeatureState> = _state.asStateFlow()

    // Alternative: Combine multiple flows
    val combinedState: StateFlow<YourFeatureState> = combine(
        repository.getData1(),
        repository.getData2(),
    ) { data1, data2 ->
        YourFeatureState(
            data1 = data1,
            data2 = data2,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(SHARING_TIMEOUT),
        YourFeatureState()
    )

    // Public methods for UI actions
    fun performAction(param: String) {
        Logger.d(TAG) { "Performing action with param: $param" }
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                yourUseCase(param).collect { result ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            data = result,
                            error = null,
                        )
                    }
                }
            } catch (e: Exception) {
                Logger.e(TAG, e) { "Error performing action" }
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message,
                    )
                }
            }
        }
    }

    private fun updatePreference(logMsg: String, action: suspend () -> Unit) {
        Logger.d(TAG) { logMsg }
        viewModelScope.launch { action() }
    }
}
```

**Best Practices**:
- ✅ Usare `StateFlow` per esporre lo stato immutabile
- ✅ Usare `MutableStateFlow` privato per aggiornamenti interni
- ✅ Logging con Timber/Kermit per debug
- ✅ Try-catch per gestire errori
- ✅ Costanti in `companion object`
- ✅ Massimo 300 righe di codice
- ❌ NON contenere logica di business (delegare ai UseCase)
- ❌ NON accedere direttamente al Context (passarlo come parametro)

**Esempio Reale**:
```kotlin
// androidApp/src/main/kotlin/com/antcashmanager/android/ui/screen/settings/SettingsViewModel.kt
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {
    
    val state: StateFlow<SettingsState> = combine(
        settingsRepository.getTheme(),
        settingsRepository.getLanguage(),
        // ... altri flow
    ) { theme, language, ... ->
        SettingsState(
            theme = theme,
            language = language,
            // ...
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), SettingsState())

    fun setTheme(theme: AppTheme) = updatePreference(
        logMsg = "Setting theme: $theme",
        action = { settingsRepository.setTheme(theme) },
    )
}
```

---

### 2.3 State (Presentation Layer)

**Responsabilità**: Rappresentare lo stato immutabile di uno Screen.

**Template**:
```kotlin
package com.antcashmanager.android.ui.screen.yourfeature

/**
 * Stato UI per YourFeatureScreen.
 * 
 * @property isLoading Indica se è in corso un caricamento
 * @property data Dati da visualizzare
 * @property error Messaggio di errore, null se nessun errore
 */
data class YourFeatureState(
    val isLoading: Boolean = false,
    val data: List<YourData> = emptyList(),
    val error: String? = null,
    val filterType: FilterType = FilterType.ALL,
    val sortOrder: SortOrder = SortOrder.DATE_DESC,
)

/**
 * Eventi UI che l'utente può triggerare.
 */
sealed class YourFeatureEvent {
    data class LoadData(val filter: String) : YourFeatureEvent()
    data class DeleteItem(val id: Long) : YourFeatureEvent()
    object Refresh : YourFeatureEvent()
}

/**
 * Side-effects che richiedono azione UI (navigazione, toast, etc).
 */
sealed class YourFeatureSideEffect {
    data class ShowToast(val message: String) : YourFeatureSideEffect()
    data class NavigateTo(val route: String) : YourFeatureSideEffect()
}
```

**Best Practices**:
- ✅ Data class immutabile
- ✅ Valori di default ragionevoli
- ✅ Usare sealed class per eventi e side-effects
- ✅ Documentazione KDoc
- ❌ NON contenere logica
- ❌ NON contenere riferimenti a Context o View

---

### 2.4 Screen (Presentation Layer - Composables)

**Responsabilità**: Renderizzare UI basata sullo stato, delegare azioni al ViewModel.

**Template**:
```kotlin
package com.antcashmanager.android.ui.screen.yourfeature

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun YourFeatureScreen(
    repository: YourRepository,
    navController: NavController,
) {
    val viewModel: YourFeatureViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                YourFeatureViewModel(repository) as T
        },
    )

    val state by viewModel.state.collectAsState()

    YourFeatureContent(
        state = state,
        onAction = viewModel::performAction,
        onNavigateBack = { navController.popBackStack() },
    )
}

@Composable
internal fun YourFeatureContent(
    state: YourFeatureState,
    onAction: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Header
        ScreenHeader(
            title = stringResource(R.string.your_feature_title),
        )
        
        // Content based on state
        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorView(state.error)
            state.data.isEmpty() -> EmptyView()
            else -> DataList(state.data, onAction)
        }
    }
}

// Preview
@Preview(showBackground = true)
@Composable
private fun YourFeatureContentPreview() {
    AntCashManagerTheme {
        YourFeatureContent(
            state = YourFeatureState(data = listOf(/* ... */)),
            onAction = {},
            onNavigateBack = {},
        )
    }
}
```

**Best Practices**:
- ✅ Separare Screen wrapper da Content composable (per testabilità)
- ✅ Usare `stringResource()` per tutte le stringhe
- ✅ Preview per ogni composable principale
- ✅ Parametri opzionali con default values
- ✅ Massimo 400 righe per file (estrarre sub-composables)
- ❌ ZERO logica di business
- ❌ NON accedere direttamente a repository/database

**Esempio Reale**:
```kotlin
// androidApp/src/main/kotlin/com/antcashmanager/android/ui/screen/settings/SettingsScreen.kt
@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    navController: NavController,
) {
    val viewModel: SettingsViewModel = viewModel(...)
    val state by viewModel.state.collectAsState()

    SettingsContent(
        currentTheme = state.theme,
        onThemeSelected = { viewModel.setTheme(it) },
        // ...
    )
}
```

---

## 3. Unit Testing

### 3.1 Test per UseCase

**Template**:
```kotlin
package com.antcashmanager.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import kotlin.test.assertEquals

class YourFeatureUseCaseTest {

    private lateinit var repository: YourRepository
    private lateinit var useCase: YourFeatureUseCase

    @Before
    fun setup() {
        repository = mock()
        useCase = YourFeatureUseCase(repository)
    }

    @Test
    fun `invoke should return data from repository`() = runTest {
        // Given
        val expectedData = YourData(id = 1, name = "Test")
        whenever(repository.getData("test")).thenReturn(flowOf(expectedData))

        // When
        val result = useCase.invoke("test").first()

        // Then
        assertEquals(expectedData, result)
        verify(repository).getData("test")
    }

    @Test
    fun `invoke should handle error from repository`() = runTest {
        // Given
        val exception = RuntimeException("Network error")
        whenever(repository.getData("test")).thenThrow(exception)

        // When/Then
        assertFailsWith<RuntimeException> {
            useCase.invoke("test").first()
        }
    }
}
```

### 3.2 Test per ViewModel

**Template**:
```kotlin
package com.antcashmanager.android.ui.screen.yourfeature

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class YourFeatureViewModelTest {

    private lateinit var repository: YourRepository
    private lateinit var useCase: YourFeatureUseCase
    private lateinit var viewModel: YourFeatureViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        useCase = YourFeatureUseCase(repository)
        viewModel = YourFeatureViewModel(useCase, repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `performAction should update state with data`() = runTest {
        // Given
        val expectedData = listOf(YourData(1, "Test"))
        whenever(useCase("test")).thenReturn(flowOf(expectedData))

        // When
        viewModel.performAction("test")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(expectedData, state.data)
            assertEquals(false, state.isLoading)
            assertEquals(null, state.error)
        }
    }
}
```

**Best Practices**:
- ✅ Test tutti i percorsi (happy path + error cases)
- ✅ Usare `runTest` per coroutines
- ✅ Mock delle dipendenze con Mockito
- ✅ Naming: `` `method should expected_behavior when condition` ``
- ✅ Given/When/Then structure
- ✅ Code coverage > 80%

---

## 4. Stringhe Localizzate

### 4.1 Definizione Stringhe

**Location**: `androidApp/src/main/res/values/strings.xml`

**Template**:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- YourFeature Screen -->
    <string name="your_feature_title">Your Feature</string>
    <string name="your_feature_subtitle">Manage your data</string>
    <string name="your_feature_empty">No data available</string>
    <string name="your_feature_error">Error loading data: %s</string>
    
    <!-- Actions -->
    <string name="action_save">Save</string>
    <string name="action_cancel">Cancel</string>
    <string name="action_delete">Delete</string>
    
    <!-- Dialogs -->
    <string name="dialog_confirm_delete_title">Delete Item?</string>
    <string name="dialog_confirm_delete_message">Are you sure you want to delete \"%s\"? This action cannot be undone.</string>
</resources>
```

**Lingue supportate**:
- 🇬🇧 English: `values/strings.xml`
- 🇮🇹 Italian: `values-it/strings.xml`
- 🇫🇷 French: `values-fr/strings.xml`
- 🇩🇪 German: `values-de/strings.xml`
- 🇪🇸 Spanish: `values-es/strings.xml`

**Best Practices**:
- ✅ Nome chiave: `<screen>_<component>_<description>`
- ✅ Commenti per raggruppamento logico
- ✅ Parametri con `%s` (String), `%d` (Int), `%1$s` (multipli)
- ✅ Plurals quando necessari
- ❌ MAI stringhe hardcoded nel codice
- ❌ NON usare `!!` per stringhe nullabili

**Uso nel codice**:
```kotlin
// ✅ Composable
Text(text = stringResource(R.string.your_feature_title))

// ✅ ViewModel/UseCase (passare Context come parametro)
fun performAction(context: Context) {
    val message = context.getString(R.string.success_message)
    showToast(message)
}

// ✅ Con parametri
Text(
    text = stringResource(
        R.string.your_feature_error,
        errorMessage
    )
)

// ❌ EVITARE
Text(text = "Hardcoded string") // WRONG!
```

---

## 5. Struttura Modulare - Evitare Classi Lunghe

### 5.1 Limiti di Lunghezza

| Tipo | Massimo Righe | Azione se Superato |
|------|---------------|---------------------|
| UseCase | 150 | Dividere in UseCase più piccoli |
| ViewModel | 300 | Estrarre logica in UseCase |
| Screen Composable | 400 | Estrarre sub-composables |
| State/Model | 100 | Dividere in sottoclassi |

### 5.2 Estrazione di Sub-Composables

**❌ BAD - Monolitico**:
```kotlin
@Composable
fun TransactionsScreen(...) {
    Column {
        // 100+ righe di header
        // 200+ righe di filtri
        // 300+ righe di lista
        // 100+ righe di dialogs
    }
}
```

**✅ GOOD - Modulare**:
```kotlin
@Composable
fun TransactionsScreen(...) {
    Column {
        TransactionHeader(...)
        TransactionFilters(...)
        TransactionList(...)
    }
    TransactionDialogs(...)
}

@Composable
private fun TransactionHeader(...) { /* ... */ }

@Composable
private fun TransactionFilters(...) { /* ... */ }

@Composable
private fun TransactionList(...) { /* ... */ }
```

### 5.3 Componenti Riusabili - USO OBBLIGATORIO

**REGOLA CRITICA**: Prima di creare qualsiasi componente UI, **VERIFICA SEMPRE** se esiste già nel package `ui/components/`.

#### 5.3.1 Componenti Base Disponibili

##### Layout & Structure (`ui/components/AppComposables.kt`)
```kotlin
// ✅ Header standard per tutte le schermate
ScreenHeader(
    title = stringResource(R.string.screen_title),
    modifier = Modifier,
    actions = { /* Optional: IconButton, HelpButton, etc */ }
)

// ✅ Divider orizzontale
AppDivider(modifier = Modifier)

// ✅ Help button (icona info)
HelpButton(
    onHelpClick = { showHelpDialog = true },
    modifier = Modifier
)
```

##### Card Components (`ui/components/card/`)
```kotlin
// ✅ Card standard con icona, titolo, sottotitolo, chevron
AppCard(
    title = stringResource(R.string.card_title),
    subtitle = stringResource(R.string.card_subtitle),
    leadingIcon = Icons.Default.YourIcon,
    iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
    showChevron = true, // default: true
    onClick = { /* action */ },
    trailingContent = { /* Optional: Switch, Badge, custom */ }
)

// ✅ Section header per raggruppare card
AppCardSectionHeader(
    title = stringResource(R.string.section_title)
)
```

##### Text Components (`ui/components/text/`)
```kotlin
// ✅ Text con stile MaterialTheme
AppText(
    text = stringResource(R.string.text_content),
    style = MaterialTheme.typography.bodyLarge,
    color = MaterialTheme.colorScheme.onSurface,
    fontWeight = FontWeight.Normal,
    modifier = Modifier
)
```

##### Input Components (`ui/components/`)
```kotlin
// ✅ Switch customizzato con MaterialTheme
AppSwitch(
    checked = state.isEnabled,
    onCheckedChange = { viewModel.toggle(it) },
    modifier = Modifier
)

// ✅ Radio button customizzato
AppRadioButton(
    selected = state.selectedOption == option,
    onClick = { viewModel.selectOption(option) },
    modifier = Modifier
)

// ✅ List item con supporto per radio/checkbox/icon
AppListItem(
    headlineContent = { Text(stringResource(R.string.item_title)) },
    supportingContent = { Text(stringResource(R.string.item_subtitle)) },
    leadingContent = { 
        RadioButton(selected = isSelected, onClick = { /* ... */ })
    },
    modifier = Modifier
)
```

##### Dialog Components (`ui/screen/settings/view/`)
```kotlin
// ✅ Currency symbol selection dialog
CurrencySymbolDialog(
    currentSymbol = "€",
    onSymbolSelected = { symbol -> viewModel.setCurrency(symbol) },
    onDismiss = { showDialog = false }
)

// ✅ Decimal digits selection dialog
DecimalDigitsDialog(
    currentDigits = 2,
    onDigitsSelected = { digits -> viewModel.setDigits(digits) },
    onDismiss = { showDialog = false }
)

// ✅ Separator selection (decimal/thousands)
SeparatorDialog(
    title = stringResource(R.string.dialog_choose_separator),
    options = CurrencyFormat.SEPARATORS,
    currentValue = state.separator,
    onSelected = { value -> viewModel.setSeparator(value) },
    onDismiss = { showDialog = false }
)

// ✅ Transaction icon display type selection
TransactionIconDisplayDialog(
    currentDisplayType = TransactionDisplayType.CATEGORY,
    onDisplayTypeSelected = { type -> viewModel.setDisplayType(type) },
    onDismiss = { showDialog = false }
)

// ✅ Help dialog with features list
HelpDialog(
    onDismiss = { showHelpDialog = false }
)
```

#### 5.3.2 Workflow OBBLIGATORIO per Componenti UI

**Step 1: VERIFICA ESISTENZA**

Prima di scrivere QUALSIASI componente UI:

```
Struttura package ui/components/:
├── AppComposables.kt          # ScreenHeader, AppDivider, HelpButton
├── AppListItem.kt             # List items standard
├── AppRadioButton.kt          # Radio button customizzato
├── AppSwitch.kt               # Switch customizzato
├── card/
│   ├── AppCard.kt            # Card con icona/titolo/subtitle
│   └── AppCardSectionHeader.kt # Section headers
├── dialog/
│   └── HelpDialogContent.kt  # Help dialog content
└── text/
    └── AppText.kt            # Text con theme support
```

**Step 2: USA COMPONENTE ESISTENTE**

```kotlin
// ✅ CORRECT - Usa AppCard esistente
import com.antcashmanager.android.ui.components.card.AppCard

AppCard(
    title = stringResource(R.string.settings_theme),
    subtitle = themeLabel,
    leadingIcon = Icons.Default.Palette,
    onClick = { showThemeDialog = true }
)

// ❌ WRONG - NON ricreare da zero!
@Composable
fun SettingsItem(...) {  // FORBIDDEN se AppCard esiste!
    Card(...) {
        Row {
            Icon(...)
            Column { Text(...); Text(...) }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight)
        }
    }
}
```

**Step 3: SE NON ESISTE, CREA NEL PACKAGE CORRETTO**

Se devi creare un nuovo componente:

1. **Valuta se può essere generico** (riutilizzabile in più screen)
2. **Posiziona nel package corretto**:
   - `ui/components/` → Componenti base generici
   - `ui/components/card/` → Varianti di card
   - `ui/components/text/` → Varianti di text
   - `ui/components/dialog/` → Dialog riusabili
   - `ui/screen/yourfeature/view/` → Componenti specifici di una feature
3. **Rendi il componente riusabile** con parametri configurabili

```kotlin
// ✅ GOOD - Componente generico e riusabile
@Composable
fun StatusCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    // Implementation con parametri configurabili
}
```

#### 5.3.3 Pattern per Layout di Sezioni

Pattern standard per organizzare sezioni con card:

```kotlin
// ✅ CORRECT Pattern - Sezione con card raggruppate
@Composable
fun YourFeatureSection(
    state: YourState,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Section header
    AppCardSectionHeader(
        title = stringResource(R.string.section_title)
    )
    
    // Card group con spacing consistente (8.dp standard)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Card con click action
        AppCard(
            title = stringResource(R.string.item1_title),
            subtitle = stringResource(R.string.item1_subtitle),
            leadingIcon = Icons.Default.Settings,
            onClick = { onAction(Action.OpenSettings) }
        )
        
        // Card con switch nel trailing
        AppCard(
            title = stringResource(R.string.item2_title),
            subtitle = stringResource(R.string.item2_subtitle),
            leadingIcon = Icons.Default.Notifications,
            trailingContent = {
                AppSwitch(
                    checked = state.notificationsEnabled,
                    onCheckedChange = { onAction(Action.ToggleNotifications(it)) }
                )
            },
            onClick = { onAction(Action.ToggleNotifications(!state.notificationsEnabled)) }
        )
        
        // Card con icona colorata custom
        AppCard(
            title = stringResource(R.string.item3_title),
            subtitle = stringResource(R.string.item3_subtitle),
            leadingIcon = Icons.Default.Warning,
            iconBackgroundColor = MaterialTheme.colorScheme.errorContainer,
            iconTint = MaterialTheme.colorScheme.onErrorContainer,
            showChevron = false,
            onClick = { onAction(Action.ShowWarning) }
        )
    }
}
```

#### 5.3.4 Esempi dal Progetto

**✅ GOOD - SettingsScreen usa AppCard**
```kotlin
// SettingsScreen.kt - Esempio corretto
AppCardSectionHeader(title = stringResource(R.string.settings_appearance))

Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    AppCard(
        title = stringResource(R.string.settings_theme),
        subtitle = when (currentTheme) {
            AppTheme.LIGHT -> stringResource(R.string.settings_theme_light)
            AppTheme.DARK -> stringResource(R.string.settings_theme_dark)
            AppTheme.SYSTEM -> stringResource(R.string.settings_theme_system)
        },
        leadingIcon = Icons.Default.Palette,
        onClick = { showThemeDialog = true }
    )
    
    AppCard(
        title = stringResource(R.string.settings_language),
        subtitle = languageDisplayName(currentLanguage),
        leadingIcon = Icons.Default.Language,
        onClick = { showLanguageDialog = true }
    )
}
```

**✅ GOOD - DisplayScreen usa ScreenHeader**
```kotlin
// DisplayScreen.kt - Esempio corretto
Scaffold(
    topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.settings_display)) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
        )
    }
) { innerPadding ->
    Column(modifier = Modifier.padding(innerPadding)) {
        // Content con AppCard components
    }
}
```

**❌ BAD - Ricreare componenti esistenti**
```kotlin
// FORBIDDEN! AppCard già esiste
@Composable
fun CustomSettingsCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
        }
    }
}
```

#### 5.3.5 Checklist Componenti UI

Prima di scrivere codice UI, verifica:

- [ ] Ho cercato in `ui/components/` se il componente esiste?
- [ ] Ho verificato negli screen esistenti (Settings, Display, Home) pattern simili?
- [ ] Sto usando `AppCard` invece di `Card` diretto?
- [ ] Sto usando `ScreenHeader` per i titoli delle schermate?
- [ ] Sto usando `AppCardSectionHeader` per separare sezioni?
- [ ] Sto usando `AppSwitch` invece di `Switch` diretto?
- [ ] Sto usando `AppText` dove appropriato invece di `Text` diretto?
- [ ] I dialog seguono il pattern esistente (es. `CurrencySymbolDialog`)?
- [ ] Le card sono raggruppate con `Column(Arrangement.spacedBy(8.dp))`?
- [ ] Ho usato `stringResource()` per tutte le stringhe?

#### 5.3.6 Benefici del Riuso

✅ **Consistenza visiva** - Stesso look & feel in tutta l'app  
✅ **Manutenibilità** - Modifiche centralizzate in un solo posto  
✅ **Tema automatico** - Rispetta sempre MaterialTheme.colorScheme  
✅ **Meno codice** - Non riscrivere ciò che esiste già  
✅ **Testabilità** - Componenti già testati e verificati  
✅ **Velocità** - Sviluppo più rapido con componenti pronti  
✅ **Qualità** - Componenti già ottimizzati per performance e accessibilità

---

## 6. Tema Centralizzato

### 6.1 Definizione Tema

**Location**: `ui/theme/Theme.kt`

```kotlin
@Composable
fun AntCashManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
```

### 6.2 Colori Semantici

**Location**: `ui/theme/Color.kt`

```kotlin
// ✅ Usare colori semantici da MaterialTheme
@Composable
fun MyComponent() {
    Card(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Icon(
            tint = MaterialTheme.colorScheme.primary,
            // ...
        )
    }
}

// ❌ EVITARE hardcoded colors
Card(
    containerColor = Color(0xFF123456), // WRONG!
)
```

### 6.3 Typography

**Location**: `ui/theme/Type.kt`

```kotlin
// ✅ Usare stili da MaterialTheme
Text(
    text = title,
    style = MaterialTheme.typography.headlineMedium,
    fontWeight = FontWeight.Bold,
)

// ❌ EVITARE
Text(
    text = title,
    fontSize = 24.sp, // WRONG!
    fontWeight = FontWeight.Bold,
)
```

### 6.4 Spacing e Dimensioni

Centralizzare in `ui/theme/Dimens.kt`:

```kotlin
object Dimens {
    // Padding
    val paddingTiny = 4.dp
    val paddingSmall = 8.dp
    val paddingMedium = 16.dp
    val paddingLarge = 24.dp
    val paddingXLarge = 32.dp
    
    // Elevation
    val elevationCard = 1.dp
    val elevationDialog = 8.dp
    
    // Corner Radius
    val cornerRadiusSmall = 8.dp
    val cornerRadiusMedium = 12.dp
    val cornerRadiusLarge = 16.dp
}

// Uso
Card(
    modifier = Modifier.padding(Dimens.paddingMedium),
    elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationCard),
)
```

---

## 7. Navigation Pattern

### 7.1 Definizione Routes

**Location**: `ui/navigation/NavGraph.kt`

```kotlin
object Routes {
    const val HOME = "home"
    const val TRANSACTIONS = "transactions"
    const val TRANSACTION_DETAIL = "transaction/{id}"
    const val SETTINGS = "settings"
    const val DISPLAY = "display"
}

@Composable
fun ComposeNavGraph(
    navController: NavHostController,
    repositories: RepositoryContainer,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                repository = repositories.transactionRepository,
                navController = navController,
            )
        }
        
        composable(
            route = Routes.TRANSACTION_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            TransactionDetailScreen(
                transactionId = id,
                navController = navController,
            )
        }
    }
}
```

### 7.2 Navigazione da Screen

```kotlin
// Navigate to route
navController.navigate(Routes.SETTINGS)

// Navigate with arguments
navController.navigate("transaction/$transactionId")

// Navigate back
navController.popBackStack()

// Navigate back to specific destination
navController.popBackStack(Routes.HOME, inclusive = false)
```

---

## 8. Error Handling e Logging

### 8.1 Logging con Kermit

```kotlin
import co.touchlab.kermit.Logger

class YourFeatureViewModel(...) : ViewModel() {
    
    companion object {
        private const val TAG = "YourFeatureViewModel"
    }

    fun performAction() {
        Logger.d(TAG) { "Starting action" }
        
        try {
            // operation
            Logger.i(TAG) { "Action completed successfully" }
        } catch (e: Exception) {
            Logger.e(TAG, e) { "Error performing action" }
        }
    }
}
```

**Livelli**:
- `Logger.v()` - Verbose (dettagli minori)
- `Logger.d()` - Debug (sviluppo)
- `Logger.i()` - Info (eventi importanti)
- `Logger.w()` - Warning (situazioni anomale)
- `Logger.e()` - Error (errori recuperabili)

### 8.2 Gestione Errori

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

// Uso nel ViewModel
viewModelScope.launch {
    _state.update { it.copy(result = Result.Loading) }
    
    try {
        val data = useCase.invoke()
        _state.update { it.copy(result = Result.Success(data)) }
    } catch (e: Exception) {
        Logger.e(TAG, e) { "Error loading data" }
        _state.update { it.copy(result = Result.Error(e)) }
    }
}

// Rendering in Screen
when (state.result) {
    is Result.Loading -> LoadingIndicator()
    is Result.Success -> DataView(state.result.data)
    is Result.Error -> ErrorView(state.result.exception.message)
}
```

---

## 9. Dependency Injection (Manual)

Senza usare Hilt/Dagger, passare dipendenze manualmente:

```kotlin
// Repository Container
data class RepositoryContainer(
    val settingsRepository: SettingsRepository,
    val transactionRepository: TransactionRepository,
    val categoryRepository: CategoryRepository,
)

// MainActivity
class MainActivity : ComponentActivity() {
    private lateinit var repositories: RepositoryContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize repositories
        val database = AppDatabase.getDatabase(applicationContext)
        repositories = RepositoryContainer(
            settingsRepository = SettingsRepositoryImpl(applicationContext),
            transactionRepository = TransactionRepositoryImpl(database.transactionDao()),
            categoryRepository = CategoryRepositoryImpl(database.categoryDao()),
        )

        setContent {
            val navController = rememberNavController()
            ComposeNavGraph(navController, repositories)
        }
    }
}
```

---

## 10. Checklist Pre-Commit

Prima di ogni commit, verificare:

- [ ] **Clean Architecture**: Dipendenze layer corrette
- [ ] **UseCase**: Logica business isolata, testabile
- [ ] **ViewModel**: Nessuna logica business, solo coordinamento
- [ ] **State**: Immutabile, documentato
- [ ] **Screen**: Solo UI, nessuna logica
- [ ] **Unit Tests**: Coverage > 80%, tutti i casi testati
- [ ] **Stringhe**: Zero hardcoded, tutte in `strings.xml` (5 lingue)
- [ ] **Lunghezza**: Nessuna classe > limiti definiti
- [ ] **Tema**: Colori/typography da `MaterialTheme`
- [ ] **Logging**: Uso corretto di Kermit
- [ ] **Naming**: Convenzioni Kotlin rispettate
- [ ] **Documentation**: KDoc per public API
- [ ] **Preview**: Composables hanno `@Preview`
- [ ] **Compilazione**: No errori, solo warning Detekt accettabili

---

## 11. File Structure Example

```
androidApp/
├── src/main/kotlin/com/antcashmanager/android/
│   ├── domain/
│   │   └── usecase/
│   │       ├── transaction/
│   │       │   ├── GetTransactionsUseCase.kt
│   │       │   ├── AddTransactionUseCase.kt
│   │       │   └── DeleteTransactionUseCase.kt
│   │       ├── category/
│   │       │   └── GetCategoriesUseCase.kt
│   │       └── feedback/
│   │           └── SendFeedbackEmailUseCase.kt
│   │
│   ├── ui/
│   │   ├── screen/
│   │   │   ├── home/
│   │   │   │   ├── HomeScreen.kt (~300 lines)
│   │   │   │   ├── HomeViewModel.kt (~200 lines)
│   │   │   │   ├── HomeState.kt (~50 lines)
│   │   │   │   └── view/
│   │   │   │       ├── BalanceCard.kt
│   │   │   │       └── TransactionList.kt
│   │   │   ├── transactions/
│   │   │   ├── settings/
│   │   │   └── ...
│   │   ├── components/
│   │   │   ├── AppComposables.kt
│   │   │   ├── card/
│   │   │   │   ├── AppCard.kt
│   │   │   │   └── AppCardSectionHeader.kt
│   │   │   └── text/
│   │   │       └── AppText.kt
│   │   ├── theme/
│   │   │   ├── Theme.kt
│   │   │   ├── Color.kt
│   │   │   ├── Type.kt
│   │   │   └── Dimens.kt
│   │   └── navigation/
│   │       └── NavGraph.kt
│   │
│   └── MainActivity.kt
│
└── src/test/kotlin/
    └── com/antcashmanager/android/
        ├── domain/usecase/
        │   └── GetTransactionsUseCaseTest.kt
        └── ui/screen/home/
            └── HomeViewModelTest.kt
```

---

## 12. Code Review Guidelines

Quando si revisiona codice, verificare:

### ✅ Architettura
- [ ] Layer boundaries rispettati
- [ ] UseCase usati per business logic
- [ ] ViewModel non contiene business logic
- [ ] Screen contiene solo UI

### ✅ Qualità Codice
- [ ] Naming chiaro e consistente
- [ ] Funzioni < 30 righe
- [ ] Classi rispettano limiti di lunghezza
- [ ] Nessuna duplicazione

### ✅ Testing
- [ ] Unit test presenti
- [ ] Happy path + error cases coperti
- [ ] Mock delle dipendenze

### ✅ UI/UX
- [ ] Tutte stringhe localizzate (5 lingue)
- [ ] Tema centralizzato usato
- [ ] Responsive design
- [ ] Accessibility considerata

### ✅ Performance
- [ ] Nessun blocking su Main thread
- [ ] Coroutines usate correttamente
- [ ] Flow over LiveData
- [ ] Memory leaks evitati

---

## 13. Esempi Pratici dal Progetto

### Esempio 1: DisplayScreen (GOOD)

```kotlin
// ✅ Separazione chiara: Screen wrapper + Content
@Composable
fun DisplayScreen(
    settingsRepository: SettingsRepository,
    navController: NavController,
) {
    val viewModel: DisplayViewModel = viewModel(...)
    val state by viewModel.state.collectAsState()
    
    DisplayContent(
        currencySymbol = state.currencySymbol,
        onCurrencySymbolSelected = { viewModel.setCurrencySymbol(it) },
        // ... altri parametri
    )
}

@Composable
internal fun DisplayContent(...) {
    // UI pura, testabile
}
```

### Esempio 2: SettingsViewModel (GOOD)

```kotlin
// ✅ Combine multiple flows, delegation a repository
val state: StateFlow<SettingsState> = combine(
    settingsRepository.getTheme(),
    settingsRepository.getLanguage(),
    settingsRepository.getShowCharts(),
) { theme, language, showCharts ->
    SettingsState(
        theme = theme,
        language = language,
        showCharts = showCharts,
    )
}.stateIn(...)

// ✅ Helper per update preferences
private fun updatePreference(logMsg: String, action: suspend () -> Unit) {
    Logger.d(TAG) { logMsg }
    viewModelScope.launch { action() }
}

fun setTheme(theme: AppTheme) = updatePreference(
    logMsg = "Setting theme: $theme",
    action = { settingsRepository.setTheme(theme) },
)
```

### Esempio 3: SendFeedbackEmailUseCase (GOOD)

```kotlin
// ✅ UseCase semplice, single responsibility
class SendFeedbackEmailUseCase {
    fun sendFeedbackEmail(
        applicationContext: Context,
        emailBody: String,
        versionName: String,
    ): Boolean {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("misterant.developer@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "AntCashManager Feedback - v$versionName")
            putExtra(Intent.EXTRA_TEXT, emailBody)
        }
        return emailIntent.resolveActivity(applicationContext.packageManager) != null
            .also { if (it) applicationContext.startActivity(emailIntent) }
    }
}
```

---

## 14. Anti-Patterns da Evitare

### ❌ BAD: Business Logic in Screen

```kotlin
@Composable
fun TransactionScreen() {
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        // ❌ WRONG: Business logic in composable
        transactions = database.transactionDao().getAll()
    }
}
```

### ❌ BAD: Hardcoded Strings

```kotlin
@Composable
fun ErrorView() {
    Text("Error occurred!") // ❌ WRONG
}
```

### ❌ BAD: ViewModel with Context

```kotlin
class YourViewModel(
    private val context: Context, // ❌ WRONG: Memory leak
) : ViewModel()
```

### ❌ BAD: Monolithic Class

```kotlin
// ❌ WRONG: 1000+ lines
class TransactionsScreen(...) {
    // Everything in one file
}
```

---

## 15. Tools e Automation

### Gradle Tasks Utili

```bash
# Compile
./gradlew :androidApp:compileDebugKotlin

# Run tests
./gradlew :androidApp:testDebugUnitTest

# Check code style
./gradlew detekt

# Generate test coverage
./gradlew :androidApp:koverHtmlReportDebug
```

### Pre-commit Hook

Creare `.git/hooks/pre-commit`:

```bash
#!/bin/bash
echo "Running pre-commit checks..."

# Check code style
./gradlew detekt
if [ $? -ne 0 ]; then
    echo "❌ Detekt failed. Fix issues before committing."
    exit 1
fi

# Run tests
./gradlew :androidApp:testDebugUnitTest
if [ $? -ne 0 ]; then
    echo "❌ Tests failed. Fix tests before committing."
    exit 1
fi

echo "✅ All checks passed!"
exit 0
```

---

## 16. Risorse Aggiuntive

- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Jetpack Compose Best Practices](https://developer.android.com/jetpack/compose/mental-model)
- [Material Design 3](https://m3.material.io/)
- [Kermit Logging](https://github.com/touchlab/Kermit)

---

**Last Updated**: May 2026  
**Version**: 1.1  
**Maintainer**: AntCashManager Team

