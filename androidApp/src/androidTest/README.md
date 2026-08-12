# Android Instrumentation Tests

Instrumentation tests per AntCashManager - testano navigazione e funzionalità UI reale.

## 📂 Struttura

```
androidTest/kotlin/com/antcashmanager/android/ui/
├── navigation/
│   └── NavigationTest.kt          # Navigazione tra schermate
└── screen/
    ├── HomeScreenTest.kt          # Home screen
    ├── TransactionsScreenTest.kt  # Transactions screen
    ├── ChartsScreenTest.kt        # Charts screen
    ├── CategoriesScreenTest.kt    # Categories screen
    └── SettingsScreenTest.kt      # Settings screen
```

## 🚀 Quick Start

### Prerequisiti
```bash
# Verificare che un emulator sia in esecuzione
adb devices

# Oppure avviare emulator
emulator -avd <emulator_name>
```

### Eseguire Tutti i Test
```bash
./gradlew :androidApp:connectedAndroidTest
```

### Eseguire Solo un Test
```bash
# Navigation test
./gradlew :androidApp:connectedAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.antcashmanager.android.ui.navigation.NavigationTest

# Home screen test
./gradlew :androidApp:connectedAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.antcashmanager.android.ui.screen.HomeScreenTest
```

## 📊 Test Count

- **NavigationTest**: 7 test methods
- **HomeScreenTest**: 9 test methods
- **TransactionsScreenTest**: 11 test methods
- **ChartsScreenTest**: 12 test methods
- **CategoriesScreenTest**: 12 test methods
- **SettingsScreenTest**: 14 test methods

**Total: 65+ instrumentation test methods**

## 🎯 Test Coverage

| Screen | Tested Functionality |
|--------|---------------------|
| Home | Balance cards, top cards, transactions, filters, customization |
| Transactions | List, filtering, sorting, search, add/edit, date range |
| Charts | Display, customization, trends, date filtering, details |
| Categories | List, add/edit/delete, visibility, reordering, grouping |
| Settings | Theme, language, currency, data management, info |
| Navigation | Screen transitions, bottom nav, state preservation |

## 📝 Common Test Patterns

### Test Navigation
```kotlin
@Test
fun navigate_shouldShowCorrectScreen() {
    composeTestRule.onNodeWithContentDescription("Charts")
        .performClick()
    
    composeTestRule.onNodeWithText("Charts")
        .assertExists()
}
```

### Test User Interactions
```kotlin
@Test
fun button_shouldOpenDialog() {
    composeTestRule.onNodeWithText("Settings")
        .performClick()
    
    composeTestRule.onNodeWithContentDescription("Theme")
        .performClick()
    
    composeTestRule.onNodeWithText("Light")
        .assertExists()
}
```

### Test Data Filtering
```kotlin
@Test
fun filter_shouldUpdateDisplay() {
    composeTestRule.onNodeWithContentDescription("Filter")
        .performClick()
    
    // Apply filter
    composeTestRule.onNodeWithText("Category")
        .performClick()
    
    // Verify results
    composeTestRule.onNodeWithText("Filtered Results")
        .assertExists()
}
```

## ⚠️ Troubleshooting

### Error: "No Android devices found"
```bash
adb devices  # Verificare dispositivi
adb reconnect  # Riconnettere device
```

### Error: "Target Activity not started"
- Verificare che MainActivity sia lanciabile
- Controllare AndroidManifest.xml

### Error: "Assertion failed - element not found"
- Verificare che l'elemento sia visibile (scroll se necessario)
- Usare `composeTestRule.onRoot().printToLog("TAG")` per debug

### Test Timeout
```kotlin
composeTestRule.mainClock.autoAdvance = true  // Auto-advance time
```

## 📈 View Results

```
androidApp/build/reports/androidTests/connected/index.html
```

## 🔍 Debugging Tips

### Print Debug Info
```kotlin
composeTestRule.onRoot().printToLog("MyTag")
```

### Wait for Elements
```kotlin
composeTestRule.waitUntil(timeoutMillis = 5000) {
    composeTestRule.onNodeWithText("Loaded")
        .isDisplayed()
}
```

### Screenshot Capture
```bash
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png
```

## 📚 Resources

- [Compose Testing Docs](https://developer.android.com/jetpack/compose/testing)
- [Android Testing Guide](https://developer.android.com/training/testing)
- [AndroidJUnit4 Reference](https://developer.android.com/reference/androidx/test/ext/junit/runners/AndroidJUnit4)

## ✅ Checklist

Quando aggiungi nuovi instrumentation test:

- [ ] Test è in `src/androidTest/kotlin`
- [ ] Usa `@RunWith(AndroidJUnit4::class)`
- [ ] Usa `createAndroidComposeRule<MainActivity>()`
- [ ] Naming: `method_shouldExpectedBehavior_whenCondition`
- [ ] Content descriptions sono significative
- [ ] State è pulito tra test
- [ ] Nessun flakiness (test deterministico)
- [ ] Documentazione aggiornata

## 📋 CI/CD Integration

Per eseguire in CI/CD:

```yaml
- name: Run Instrumentation Tests
  run: ./gradlew :androidApp:connectedDebugAndroidTest
  
- name: Upload Results
  uses: actions/upload-artifact@v2
  if: always()
  with:
    name: android-test-results
    path: androidApp/build/reports/androidTests/
```

---

**Last Updated:** 2026-08-11  
**Status:** Production-Ready
