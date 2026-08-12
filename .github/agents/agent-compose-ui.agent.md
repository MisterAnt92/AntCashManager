# Agent: Compose UI & Screen Implementation

**Purpose**: Specialized guidance for Screen composables, component reuse, and UI patterns.

**See Also**: [AGENTS.md](../../AGENTS.md) for complete UI rules, i18n, and component guidelines.

---

## Screen Composable Pattern

All feature screens follow this structure:

```kotlin
package com.antcashmanager.android.ui.screen.yourfeature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.antcashmanager.android.R

@Composable
fun YourFeatureScreen(
    modifier: Modifier = Modifier,
    viewModel: YourFeatureViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    YourFeatureContent(
        state = state,
        onAction = { action -> viewModel.handleAction(action) },
        modifier = modifier,
    )
}

@Composable
private fun YourFeatureContent(
    state: YourFeatureState,
    onAction: (YourFeatureAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(16.dp)) {
        // UI composition here
        if (state.isLoading) {
            LoadingIndicator()
        } else if (state.error != null) {
            ErrorMessage(state.error, onRetry = { onAction(YourFeatureAction.Retry) })
        } else {
            // Content
        }
    }
}
```

**Rules**:
- ✅ One composable per screen
- ✅ Accept ViewModel as parameter (for testability)
- ✅ Extract sub-composables to `view/` sub-package
- ✅ NO business logic in composable
- ✅ All strings via `stringResource(R.string.key)`
- ✅ All colors via `MaterialTheme.colorScheme`
- ❌ NO hardcoded strings, colors, dimensions

---

## Code Organization

```
ui/screen/<feature>/
  <Feature>Screen.kt          # Main screen + root composable
  <Feature>ViewModel.kt       # State management
  <Feature>State.kt           # UI state data class
  <Feature>Constants.kt       # Feature constants (if needed)
  model/                      # Reusable feature data classes
  view/                       # Sub-composables
    <Component>View.kt
    <AnotherComponent>View.kt
```

**Max line limits**:
- Screen composable: **400 lines** (including sub-composables if in same file)
- If larger: split into multiple files in `view/`

---

## Component Reuse (CRITICAL)

**Before creating a NEW component, check if one exists**:

```bash
# Search for similar components
find androidApp/src/main/kotlin/com/antcashmanager/android/ui/components -name "*Card*"
find androidApp/src/main/kotlin/com/antcashmanager/android/ui/components -name "*Button*"
```

**Common reusable components** (DO NOT recreate):
- `AppCard` - Elevated card with consistent styling
- `AppButton` - Styled button with ripple
- `ScreenHeader` - Screen title + back button
- `LoadingIndicator` - Circular progress
- `ErrorMessage` - Error display with retry
- `EmptyState` - Empty list placeholder

**Pattern**:
```kotlin
// ✅ CORRECT - Reuse existing component
Column {
    ScreenHeader(
        title = stringResource(R.string.transactions_title),
        onBack = { navController.navigateUp() },
    )
    AppCard {
        // Content
    }
}

// ❌ WRONG - Recreate existing component
Column {
    Row {
        Text("Transactions", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        IconButton(onClick = { navController.navigateUp() }) {
            Icon(Icons.Default.Back, contentDescription = null)
        }
    }
    Surface(elevation = 4.dp, shape = RoundedCornerShape(8.dp)) {
        // Content
    }
}
```

---

## Preview Requirements

Every composable MUST have 2+ previews:

```kotlin
@Preview(name = "Light Mode", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun YourFeatureScreenPreview() {
    AntCashManagerTheme {
        YourFeatureScreen()
    }
}

// Additional preview for different states
@Preview
@Composable
private fun YourFeatureScreenLoadingPreview() {
    AntCashManagerTheme {
        YourFeatureContent(
            state = YourFeatureState(isLoading = true),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun YourFeatureScreenErrorPreview() {
    AntCashManagerTheme {
        YourFeatureContent(
            state = YourFeatureState(error = "Failed to load"),
            onAction = {},
        )
    }
}
```

**Rules**:
- ✅ At least 2: Light + Dark mode
- ✅ Additional previews for key states (loading, error, empty)
- ✅ Wrap with `AntCashManagerTheme`
- ❌ NO hardcoded preview data - use state builders

---

## Material Design 3 Compliance

Use ONLY MaterialTheme colors/typography:

```kotlin
// ✅ CORRECT - MaterialTheme
Text(
    text = "Amount",
    color = MaterialTheme.colorScheme.onSurface,
    style = MaterialTheme.typography.bodyMedium,
)

// ❌ WRONG - Hardcoded colors
Text(
    text = "Amount",
    color = Color(0xFF000000),
    fontSize = 16.sp,
)
```

**Available colors**:
- `primary`, `onPrimary`
- `secondary`, `onSecondary`
- `surface`, `onSurface`
- `background`, `onBackground`
- `error`, `onError`
- `primaryContainer`, `secondaryContainer`
- `outlineVariant`

**Available typography**:
- `displayLarge`, `displayMedium`, `displaySmall`
- `headlineLarge`, `headlineMedium`, `headlineSmall`
- `titleLarge`, `titleMedium`, `titleSmall`
- `bodyLarge`, `bodyMedium`, `bodySmall`
- `labelLarge`, `labelMedium`, `labelSmall`

---

## Localization (5 Languages Required)

**CRITICAL**: ALL user-facing strings in `strings.xml`

```kotlin
// ✅ CORRECT - Localized string
Text(text = stringResource(R.string.amount_label))

// ❌ WRONG - Hardcoded string
Text(text = "Amount")
```

**Before adding NEW string**:
```bash
# Check if string already exists in any locale
grep -r "amount_label" androidApp/src/main/res/values*/

# If not found, add to ALL 5 files:
# values/strings.xml (English)
# values-it/strings.xml (Italian)
# values-fr/strings.xml (French)
# values-de/strings.xml (German)
# values-es/strings.xml (Spanish)
```

---

## Common UI Patterns

### Loading State
```kotlin
if (state.isLoading) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
```

### Error State
```kotlin
if (state.error != null) {
    ErrorMessage(
        message = state.error,
        onRetry = { viewModel.retry() },
    )
}
```

### Empty State
```kotlin
if (state.items.isEmpty()) {
    EmptyState(
        title = stringResource(R.string.no_transactions),
        subtitle = stringResource(R.string.add_first_transaction),
        icon = Icons.Default.Info,
    )
}
```

### List with Lazy Column
```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
) {
    items(state.items.size) { index ->
        ItemCard(item = state.items[index])
    }
}
```

---

## Spacing & Layout

Use MaterialTheme spacing:

```kotlin
// ✅ CORRECT - Consistent spacing
Column(
    modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
) {
    // Items
}

// ❌ WRONG - Hardcoded spacing
Column(
    modifier = Modifier
        .fillMaxWidth()
        .padding(15.dp), // Inconsistent
    verticalArrangement = Arrangement.spacedBy(7.dp),
) {
    // Items
}
```

**Standard spacing**:
- `8.dp` - Tight spacing (between list items)
- `16.dp` - Standard padding (screen edges, sections)
- `24.dp` - Large spacing (section breaks)

---

## Touch Interaction & Ripples

```kotlin
// ✅ CORRECT - Proper ripple feedback
Row(
    modifier = Modifier
        .fillMaxWidth()
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(),
            onClick = { viewModel.onItemClick(item.id) },
        )
) {
    // Content
}

// Use AppButton for buttons
AppButton(
    text = stringResource(R.string.save),
    onClick = { viewModel.save() },
)
```

---

## Pre-Commit Checklist

- [ ] Screen composable under 400 lines
- [ ] All user strings via `stringResource()`
- [ ] All colors via `MaterialTheme.colorScheme`
- [ ] All typography via `MaterialTheme.typography`
- [ ] At least 2 previews (light + dark)
- [ ] Reused existing components (no recreated buttons/cards)
- [ ] NO hardcoded strings, colors, dimensions
- [ ] NO business logic in composable
- [ ] Proper spacing (8/16/24.dp)
- [ ] Ripple feedback on clickable items
- [ ] State data class passed as parameter
- [ ] ViewModel injected as parameter
- [ ] Sub-composables extracted to `view/`
- [ ] Imports clean, package correct

---

## Quick Links

- **Full UI Rules**: [AGENTS.md](../../AGENTS.md)
- **ViewModel Pattern**: [agent-viewmodel-stateflow.agent.md](agent-viewmodel-stateflow.agent.md)
- **Testing Guide**: [agent-unit-tests-mockk.agent.md](agent-unit-tests-mockk.agent.md)
- **Component Library**: `androidApp/src/main/kotlin/com/antcashmanager/android/ui/components/`
