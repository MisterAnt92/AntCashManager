# Theme System - Design Tokens & Material 3

## Overview

The theme system centralizes all design decisions (colors, typography, shapes) in one place following **Material Design 3** principles. All screens are wrapped with `AntCashManagerTheme()` to ensure consistent styling.

## Files

### `Color.kt`
Defines all color palettes. Supports 3 themes:

1. **Default (System)**
   - Light mode: `PrimaryLight`, `OnPrimaryLight`, etc.
   - Dark mode: `PrimaryDark`, `OnPrimaryDark`, etc.

2. **Dark Theme Variant**
   - Used when device is in dark mode or user selects dark theme

3. **Anna Theme (User Preference)**
   - Alternative color scheme with warmer tones
   - `AnnaPrimaryLight`, `AnnaPrimaryDark`, etc.

**Semantic colors:**
- `IncomeGreen = Color(0xFF2E7D32)` - Used for income transactions
- `ExpenseRed = Color(0xFFC62828)` - Used for expense transactions

### `Type.kt`
Typography system with **Google Fonts (Poppins)**.

**Font weights:**
- Light (300)
- Normal (400)
- Medium (500)
- SemiBold (600)
- Bold (700)

**Material 3 Styles:**
```
headlineLarge  → 32sp, bold
headlineMedium → 28sp, bold
headlineSmall  → 20sp, semibold
titleLarge     → 22sp, semibold
titleMedium    → 16sp, medium
titleSmall     → 14sp, medium
bodyLarge      → 16sp, normal
bodyMedium     → 14sp, normal
bodySmall      → 12sp, normal
labelLarge     → 14sp, medium
labelMedium    → 12sp, medium
labelSmall     → 11sp, medium
```

**Accessibility:**
```kotlin
val scaledTypo = scaledTypography(factor = 1.5f)  // 1.5× font scale
```

### `Theme.kt`
Root Material 3 theme composable.

**Features:**
- ✅ Dynamic color (Material You on Android 12+)
- ✅ Dark mode support
- ✅ High contrast mode
- ✅ Large text accessibility
- ✅ Reduce motion support
- ✅ Runtime theme switching (via Locals)

**Usage:**
```kotlin
// Wrap entire app or screen
AntCashManagerTheme(
    darkTheme = false,
    dynamicColor = true  // Material You
) {
    MyScreen()
}
```

## Design Token Usage

### Colors

**Primary Actions:**
```kotlin
Button(
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary
    )
)
```

**Text:**
```kotlin
Text(
    "Title",
    color = MaterialTheme.colorScheme.onBackground,
    style = MaterialTheme.typography.headlineSmall
)
```

**Status Indicators:**
```kotlin
Text("Income", color = IncomeGreen)
Text("Expense", color = ExpenseRed)
```

**Backgrounds:**
```kotlin
Surface(
    color = MaterialTheme.colorScheme.background,
    modifier = Modifier.fillMaxSize()
) {
    // Content
}
```

### Typography

**Never hardcode font sizes.** Use Material 3 styles:

```kotlin
// ✅ CORRECT
Text("Title", style = MaterialTheme.typography.headlineLarge)

// ❌ WRONG
Text("Title", fontSize = 32.sp, fontWeight = FontWeight.Bold)
```

**Semantic text mapping:**
- Headlines (h1, h2, h3) → `headlineSmall/Medium/Large`
- Body text → `bodySmall/Medium/Large`
- Labels/captions → `labelSmall/Medium/Large`

### Spacing

Use **dp units: 8, 16, 24** (multiples of 8):

```kotlin
Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    // Items with 16dp spacing
}

Box(modifier = Modifier.padding(16.dp)) {
    // 16dp padding on all sides
}
```

## Composition Locals (Runtime Theme Control)

### Available Locals

```kotlin
LocalReduceMotion       // Disable animations for accessibility
LocalResponsiveTypography  // Scaled typography for large text
LocalHighContrast       // High contrast mode
```

**Access in composables:**
```kotlin
val reduceMotion = LocalReduceMotion.current
val typography = LocalResponsiveTypography.current

if (reduceMotion) {
    // Skip animations
} else {
    // Play animation
}
```

## Theme Switching

### Change Theme at Runtime

Use `ThemeViewModel` to switch themes:

```kotlin
// Inject into ViewModel
class SettingsViewModel(val themeViewModel: ThemeViewModel) : ... {
    private fun onThemeChange(theme: AppTheme) {
        themeViewModel.setTheme(theme)  // Updates all screens via Local
    }
}
```

**Supported themes:**
- `LIGHT` - Light mode
- `DARK` - Dark mode
- `SYSTEM` - Follow device setting
- `ANNA` - Anna theme variant

## Testing & Previews

### Preview with Themes

```kotlin
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MyScreenPreview() {
    AntCashManagerTheme {
        MyScreen()
    }
}
```

### Testing with Different Themes

```kotlin
@Test
fun testScreenInLightTheme() {
    composeTestRule.setContent {
        AntCashManagerTheme(darkTheme = false) {
            MyScreen()
        }
    }
    // Assertions...
}
```

## Color Reference

### Light Theme
| Token | Color | Usage |
|-------|-------|-------|
| Primary | Material Blue | Primary buttons, links |
| Secondary | Material Green | Secondary actions |
| Tertiary | Material Purple | Tertiary actions |
| Error | Material Red | Errors, destructive actions |
| Background | White | Screen background |
| Surface | Light gray | Card backgrounds |
| IncomeGreen | #2E7D32 | Income transactions |
| ExpenseRed | #C62828 | Expense transactions |

### Dark Theme
(Inverted luminosity, same semantic meanings)

## Accessibility

### Large Text
Enable via Settings → Display → Large Text

```kotlin
// Automatically scaled if user enables large text
Text("Body text", style = MaterialTheme.typography.bodyLarge)
```

### Reduced Motion
Enable via Settings → Accessibility → Reduce Motion

```kotlin
val reduceMotion = LocalReduceMotion.current
if (!reduceMotion) {
    // Play animation
}
```

### High Contrast
Enable via Settings → Accessibility → High Contrast

```kotlin
val highContrast = LocalHighContrast.current
val color = if (highContrast) Color.Black else MaterialTheme.colorScheme.surface
```

## Guidelines

### ✅ DO

- Use `MaterialTheme.colorScheme.*` for standard colors
- Use `MaterialTheme.typography.*` for all text
- Wrap screens with `AntCashManagerTheme()`
- Use semantic color names (`IncomeGreen`, `ExpenseRed`)
- Test with Light, Dark, and 2× font scale

### ❌ DON'T

- Hardcode colors (e.g., `Color(0xFF123456)`)
- Hardcode font sizes (e.g., `fontSize = 16.sp`)
- Use custom colors outside of design system
- Skip theme wrapping for screens
- Ignore accessibility locals

## Future Enhancements

1. **Theme customization UI** - Let users pick custom primary color
2. **Per-screen theme overrides** - Special branding for settings
3. **Animated theme transitions** - Smooth color changes when switching
4. **Export design tokens** - Generate CSS/JSON for web

---

**Maintained by:** Claude (auto-agent)  
**Last updated:** 2026-10-03  
**Material Design Version:** 3.0
