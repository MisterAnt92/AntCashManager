# Text Components

## Overview

Text components provide semantic, accessible text rendering following Material Design 3 typography scales.

## Files

- **AppText.kt** - Semantic text with Material 3 styles (headline, body, label)
- **AppTextField.kt** - Text input field with validation and error states
- **MoneyDisplay.kt** - Formatted currency display with masking support
- **TextLink.kt** - Clickable text for navigation and actions

## Usage

### AppText (Semantic Sizing)

```kotlin
// Always use semantic styles, never hardcode sizes
AppText("Main Title", style = MaterialTheme.typography.headlineLarge)
AppText("Subtitle", style = MaterialTheme.typography.headlineSmall)
AppText("Body text", style = MaterialTheme.typography.bodyMedium)
AppText("Caption", style = MaterialTheme.typography.labelSmall)
```

### AppTextField

```kotlin
var email by remember { mutableStateOf("") }

AppTextField(
    value = email,
    onValueChange = { email = it },
    label = "Email",
    isError = !isValidEmail(email)
)
```

### MoneyDisplay

```kotlin
// Formats currency with locale-aware formatting
MoneyDisplay(
    amount = 123.45,
    currency = "EUR",
    textStyle = MaterialTheme.typography.bodyLarge
)

// Supports masking for privacy
MoneyDisplay(
    amount = 123.45,
    isMasked = amountsMasked
)
```

### TextLink

```kotlin
TextLink(
    text = "Learn more",
    onClick = { navigateToLearnMore() }
)
```

## Guidelines

- ✅ Use semantic Material 3 styles (AppText)
- ✅ Never hardcode font sizes
- ✅ Use MoneyDisplay for all currency
- ❌ Don't use Text() directly for main content
- ❌ Don't hardcode colors for text

## Accessibility

- Large text scaling: use `scaledTypography(factor: Float)`
- High contrast mode: use MaterialTheme colors
- Screen reader support: all components have proper semantics

## Testing

- Light & Dark theme previews
- 2× font scale (accessibility)
- Large text (1.5× scaling)

---

**See:** ../README.md for full component documentation
