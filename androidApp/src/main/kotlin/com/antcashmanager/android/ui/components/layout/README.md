# Layout Components

## Overview

Layout utilities for consistent spacing, adaptive layouts (foldable/tablet), and screen structure.

## Files

- **AntScreenScaffold.kt** - Root screen wrapper with safe area + top/bottom bars
- **AppSpacer.kt** - Consistent spacing using 8/16/24dp units
- **FoldableAwareLayout.kt** - Split-pane layout for foldable devices (vertical/horizontal)
- **AdaptiveLayoutInfo.kt** - Fold detection and device-specific layout hints
- **WindowInsetsCompat.kt** - Handle system insets (notches, nav bars)
- **LeftSidebar.kt** - Navigation sidebar for tablet mode
- **DisplayFeaturesLocal.kt** - Composition Local for fold/display features

## Usage

### AntScreenScaffold

```kotlin
@Composable
fun MyScreen(navController: NavController) {
    AntScreenScaffold(
        title = "My Screen",
        onBack = { navController.popBackStack() },
        actions = {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Settings, contentDescription = null)
            }
        }
    ) {
        // Screen content here
    }
}
```

### AppSpacer (Consistent Spacing)

```kotlin
Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    // Items automatically spaced 16dp apart
}

Box(modifier = Modifier.padding(16.dp)) {
    // Standard 16dp padding
}

VerticalSpacer(height = 24.dp)  // Explicit spacer
```

### FoldableAwareLayout (Foldable Devices)

```kotlin
@Composable
fun MyScreen() {
    val adaptiveLayout = rememberAdaptiveLayoutInfo()
    
    FoldableAwareLayout(
        displayFeatures = adaptiveLayout.displayFeatures,
        mainContent = {
            // Primary content
        },
        detailsContent = {
            // Details pane (shown beside on fold)
        }
    )
}
```

### AdaptiveLayoutInfo (Responsive Logic)

```kotlin
@Composable
fun TransactionsList() {
    val adaptiveLayout = rememberAdaptiveLayoutInfo()
    
    when {
        adaptiveLayout.isExpanded -> {
            // Tablet: 2-column grid
            LazyVerticalGrid(columns = GridCells.Fixed(2)) {
                // Grid content
            }
        }
        adaptiveLayout.isFoldableDevice -> {
            // Foldable: use split pane
            FoldableAwareLayout(...)
        }
        else -> {
            // Phone: single column
            LazyColumn {
                // List content
            }
        }
    }
}
```

### WindowInsetsCompat (Safe Areas)

```kotlin
// Automatically handled by AntScreenScaffold
// Manually control if needed:
val insets = rememberWindowInsetsCompat()

Box(
    modifier = Modifier.padding(insets.asPaddingValues())
)
```

## Spacing System

| Value | Usage |
|-------|-------|
| 8dp | Small spacing between tight elements |
| 16dp | Standard spacing between elements |
| 24dp | Large spacing between sections |

**Never use other values (4dp, 12dp, 20dp, etc.)**

## Adaptive Breakpoints

| Device | Type | Grid Cols | Layout |
|--------|------|-----------|--------|
| Phone | Compact | 1 | Single column |
| Tablet | Expanded | 2-3 | Multi-column grid |
| Foldable | Fold | 1+1 | Split pane |

## Guidelines

- ✅ Use AntScreenScaffold for all screens
- ✅ Use 8/16/24dp spacing units
- ✅ Check `adaptiveLayout.isExpanded` for tablet
- ✅ Use FoldableAwareLayout for foldables
- ❌ Don't hardcode margins/padding
- ❌ Don't ignore fold lines
- ❌ Don't use fixed width layouts

## Testing

- Phone preview (compact)
- Tablet 10" preview (840×1280)
- Foldable preview (vertical fold)
- Light & Dark themes

---

**See:** ../README.md for full component documentation
