# Button Components

## Overview

Button components follow Material Design 3 guidelines with semantic variants for different actions.

## Files

- **AppButton.kt** - Primary, secondary, and tertiary buttons with consistent styling
- **ReorderButtons.kt** - Special buttons for drag-to-reorder UI patterns
- **VisibilityToggleButton.kt** - Toggle for show/hide passwords and sensitive data

## Usage

### AppButton

```kotlin
AppButton(
    label = "Save",
    onClick = { onEvent(SaveEvent) },
    modifier = Modifier.fillMaxWidth()
)

// Variants
AppButton(label = "Secondary", onClick = {}, variant = ButtonVariant.Secondary)
AppButton(label = "Tertiary", onClick = {}, variant = ButtonVariant.Tertiary)
AppButton(label = "Destructive", onClick = {}, variant = ButtonVariant.Destructive)
```

### VisibilityToggleButton

```kotlin
var isVisible by remember { mutableStateOf(false) }

VisibilityToggleButton(
    isVisible = isVisible,
    onToggle = { isVisible = !it }
)
```

## Guidelines

- ✅ Always use semantic button variants
- ✅ Use AppButton for consistency
- ❌ Don't hardcode button colors
- ❌ Don't create custom button styles

## Testing

- Light & Dark theme previews
- 2× font scale (accessibility)
- Tablet variant

---

**See:** ../README.md for full component documentation
