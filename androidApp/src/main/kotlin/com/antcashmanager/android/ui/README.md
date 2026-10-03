# AntCashManager UI Layer Architecture

## Overview

The UI layer follows a **Clean Architecture** pattern with **Unidirectional Data Flow (UDF)**:

```
Presentation (Screen)  ←→  ViewModel  ←→  UseCase  ←→  Repository  ←→  Data
```

## Package Structure

### `/components` - Reusable UI Components (38 files, 17 categories)

All composable building blocks used across screens. Organized by semantic purpose:

- **`animation/`** - Reusable animation composables (5 files)
  - `AntAnimation.kt`, `CardAnimations.kt`, `ContentAnimations.kt`, `EffectAnimations.kt`, `SkeletonLoaders.kt`
  - Usage: Use for consistent motion design across app

- **`button/`** - Button variants (3 files)
  - `AppButton.kt` (primary button with variants)
  - `ReorderButtons.kt` (drag-to-reorder UI)
  - `VisibilityToggleButton.kt` (show/hide passwords)

- **`card/`** - Card containers (2 files)
  - `AppCard.kt` (generic card wrapper)
  - `AppCategoryCard.kt` (category-specific card)

- **`common/`** - Common utilities (2 files)
  - `AppComposables.kt` (utility functions)
  - `AppIcon.kt` (icon rendering)

- **`dialog/`** - Dialog components (5 files)
  - Used for user confirmations, analytics consent, progress indicators

- **`dropdown/`** - Dropdown selectors (1 file)
  - `AppUnitDropdown.kt` (unit selection)

- **`filter/`** - Filter & search (2 files)
  - `DateRangeFilter.kt`, `SearchComponent.kt`

- **`input/`** - Text input components (1 file)
  - `AutocompleteTextField.kt`

- **`layout/`** - Layout utilities (7 files)
  - `AdaptiveLayoutInfo.kt` - Fold/tablet detection
  - `FoldableAwareLayout.kt` - Split-pane for foldables
  - `AppSpacer.kt` - Consistent spacing
  - `WindowInsetsCompat.kt` - Inset handling
  - `AntScreenScaffold.kt` - Root screen wrapper

- **`list/`** - List item components (1 file)
  - `AppCategoryListItem.kt`

- **`navigation/`** - Navigation components (1 file)
  - `NavigationRailCustom.kt`

- **`overlay/`** - Overlay components (1 file)
  - `TutorialOverlay.kt`

- **`selection/`** - Selection UI (1 file)
  - `AppSelectionItemCard.kt`

- **`state/`** - State visualization (1 file)
  - `AntStateViews.kt` (empty, loading, error states)

- **`text/`** - Text components (4 files)
  - `AppText.kt` (semantic text sizes)
  - `AppTextField.kt` (input field)
  - `MoneyDisplay.kt` (formatted currency display)
  - `TextLink.kt` (clickable text)

- **`transaction/`** - Transaction-specific (1 file)
  - `TransactionDetailsPane.kt` (detail view for transactions)

### `/theme` - Design System (3 files)

Centralized design tokens and Material 3 theming:

- **`Color.kt`** - Color definitions (Light/Dark/Anna schemes)
  - Primary, Secondary, Tertiary, Error, and semantic colors
  - Light mode: `PrimaryLight`, `OnPrimaryLight`, etc.
  - Dark mode: `PrimaryDark`, `OnPrimaryDark`, etc.
  - Anna theme: `AnnaPrimaryLight`, etc.

- **`Type.kt`** - Typography system
  - Google Fonts (Poppins) with 5 weight variants
  - Material 3 styles: headlineLarge/Medium/Small, titleLarge/Medium/Small, bodyLarge/Medium/Small, labelLarge/Medium/Small
  - `scaledTypography()` function for accessibility (1.25× font scaling)

- **`Theme.kt`** - Material 3 theme composable
  - `AntCashManagerTheme()` - Root theme wrapper
  - Supports dynamic color, dark mode, high contrast
  - Integrates Locals for runtime theme switching

### `/screen` - App Screens (10+ root screens)

Each screen follows the UDF pattern:

```kotlin
@Composable
fun FeatureScreen(navController: NavController, modifier: Modifier = Modifier) {
    val viewModel: FeatureViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    FeatureContent(state = state, onEvent = viewModel::onEvent, navController = navController, modifier = modifier)
}
```

Screens include:
- `home/` - Dashboard
- `transactions/` - Transaction list & filtering
- `categories/` - Category management
- `charts/` - Analytics & insights
- `settings/` - App settings (Theme, Language, Backup, etc.)
- `addTransaction/` - Add/edit transaction flow
- `receiptScan/` - Receipt OCR
- `tutorial/` - Onboarding

## Naming Conventions

### Components
- Composable functions: **PascalCase** (e.g., `AppButton`, `TransactionCard`)
- Private helper composables: **camelCase** with suffix `_Preview` or `Content`

### Events & State
- Event sealed classes: **`FeatureEvent`** (e.g., `HomeEvent`, `SettingsEvent`)
- State data classes: **`FeatureState`** (e.g., `HomeState`, `SettingsState`)
- Event handlers: **`onEvent(event: E)`** (only public method in ViewModel)

### Files
- Screens: `FeatureScreen.kt`
- State: `FeatureState.kt`
- Events: `FeatureEvent.kt`
- ViewModels: `FeatureViewModel.kt`
- Sub-composables: `FeatureComponents.kt` or separate files in `view/` subdirectory

## Key Patterns

### 1. Unidirectional Data Flow (UDF)
```kotlin
// ViewModel
@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    HomeContent(
        state = state,
        onEvent = viewModel::onEvent,
        navController = navController
    )
}

// Content composable (pure UI)
@Composable
private fun HomeContent(state: HomeState, onEvent: (HomeEvent) -> Unit, ...) {
    // Only reads from state, emits events via onEvent(event)
}

// ViewModel
class HomeViewModel(useCases: HomeUseCases) : BaseViewModel<HomeEvent>() {
    override fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnSearch -> onSearch(event.query)
            is HomeEvent.OnCategoryFilter -> onCategoryFilter(event.categoryId)
            // ... all public paths through onEvent()
        }
    }
    
    private fun onSearch(query: String) {
        // Handle event
    }
}
```

### 2. Component Usage
```kotlin
// Simple: Wrap with theme
@Composable
fun MyScreen() {
    AntCashManagerTheme {
        Column {
            AppText("Title", style = MaterialTheme.typography.headlineMedium)
            AppButton(label = "Click me", onClick = { /* ... */ })
        }
    }
}
```

### 3. Adaptive Layout (Foldable/Tablet)
```kotlin
@Composable
fun TransactionsScreen(navController: NavController) {
    val adaptiveLayoutInfo = rememberAdaptiveLayoutInfo()
    
    if (adaptiveLayoutInfo.isExpanded) {
        // Tablet/Landscape: 2-column grid
        TransactionsGrid(columns = 2, ...)
    } else {
        // Phone: Single column
        TransactionsList(...)
    }
}
```

## Color System

### Light Theme (Default)
- Primary: Material 3 Blue
- Secondary: Material 3 Green
- Tertiary: Material 3 Purple
- Income: `IncomeGreen` (#2E7D32)
- Expense: `ExpenseRed` (#C62828)

### Dark Theme
- Inverted luminosity, high contrast

### Anna Theme
- Alternative color scheme for user preference

**Usage:**
```kotlin
Text("Income", color = IncomeGreen)  // Income transactions
Text("Expense", color = ExpenseRed)  // Expense transactions
```

## Typography

All text should use semantic Material 3 styles:

```kotlin
AppText("Large Title", style = MaterialTheme.typography.headlineLarge)
AppText("Subtitle", style = MaterialTheme.typography.bodyMedium)
AppText("Label", style = MaterialTheme.typography.labelSmall)
```

**Accessibility:** Use `scaledTypography(1.5f)` for large text setting.

## Layout Constants

Spacing: 8dp, 16dp, 24dp units (via `AppSpacer`)

```kotlin
Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    // Items with 16dp spacing
}
```

## Preview Strategy

All components and screens have @Preview annotations:

- **Light** theme
- **Dark** theme  
- **2x font scale** (accessibility)
- **Tablet 10"** (840×1280) for screens
- **Foldable** (split-pane) for adaptive layouts

## References

- Architecture: See `AGENTS.md` § 4-9
- Components inventory: See `agent-compose-ui.agent.md`
- Testing: See `AGENTS.md` § 12
- i18n: See `AGENTS.md` § 11 (13 locales)

---

**Maintained by:** Claude (auto-agent)  
**Last updated:** 2026-10-03  
**Status:** Production-ready
