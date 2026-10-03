# Dialog Components

## Overview

Dialog components for user confirmations, consent, and blocking states.

## Files

- **AppExitConfirmationDialog.kt** - Confirm exit/close action
- **BlockingProgressDialog.kt** - Loading indicator with blocking overlay
- **AppHelpDialog.kt** - Help/info dialog
- **HelpDialog.kt** - Reusable help content
- **AnalyticsConsentDialog.kt** - GDPR/analytics consent

## Usage

### AppExitConfirmationDialog

```kotlin
var showExitDialog by remember { mutableStateOf(false) }

AppExitConfirmationDialog(
    isVisible = showExitDialog,
    title = "Exit app?",
    message = "Unsaved changes will be lost.",
    onConfirm = { exitApp() },
    onDismiss = { showExitDialog = false }
)
```

### BlockingProgressDialog

```kotlin
var isLoading by remember { mutableStateOf(false) }

BlockingProgressDialog(
    isVisible = isLoading,
    title = "Saving..."
)
```

### AnalyticsConsentDialog

```kotlin
var showConsent by remember { mutableStateOf(false) }

AnalyticsConsentDialog(
    isVisible = showConsent,
    onConsent = { 
        enableAnalytics()
        showConsent = false
    },
    onDecline = {
        disableAnalytics()
        showConsent = false
    }
)
```

## Guidelines

- ✅ Use for critical confirmations only
- ✅ Keep messages concise and clear
- ✅ Use blocking overlay for long operations
- ❌ Don't use for info messages (use Snackbar instead)
- ❌ Don't over-use dialogs

## Localization

All dialogs support 13 locales via `stringResource()`:
- English, Italian, French, German, Spanish
- Hindi, Japanese, Korean, Polish
- Russian, Ukrainian, Chinese, Traditional Chinese

## Testing

- Light & Dark theme previews
- Different message lengths
- Mobile & tablet layouts

---

**See:** ../README.md for full component documentation
