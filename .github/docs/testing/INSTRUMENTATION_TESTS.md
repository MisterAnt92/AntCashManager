# Instrumentation Tests - AntCashManager

Guida completa per gli instrumentation test (UI tests) del progetto AntCashManager.

## 📋 Panoramica

Gli instrumentation test sono test di integrazione che:
- Testano il vero UI e navigazione dell'app
- Eseguono su Android device/emulator o Roboelectric
- Usano `createAndroidComposeRule<ComponentActivity>()`
- Testano interazioni reali (click, scroll, input)
- Verificano state preservation e navigation

**Differenza con Unit Test:**
| Aspetto | Unit Test | Instrumentation Test |
|---------|-----------|---------------------|
| Location | `src/test` | `src/androidTest` |
| Runner | JUnit | AndroidJUnit4 |
| Rule | createComposeRule() | createAndroidComposeRule() |
| Framework | JVM | Device/Emulator/Roboelectric |
| Scope | Singola classe | Screen/Navigation |
| Speed | Veloce (~ms) | Lento (~secondi) |

## 🗂️ Struttura Test

```
androidApp/src/androidTest/kotlin/com/antcashmanager/android/ui/
├── navigation/
│   └── NavigationTest.kt          # Test navigazione tra schermate
├── screen/
│   ├── HomeScreenTest.kt          # Home screen functionality
│   ├── TransactionsScreenTest.kt  # Transactions screen functionality
│   ├── ChartsScreenTest.kt        # Charts screen functionality
│   ├── CategoriesScreenTest.kt    # Categories screen functionality
│   └── SettingsScreenTest.kt      # Settings screen functionality
```

## 📝 Test Disponibili

### 1. NavigationTest.kt
Test della navigazione tra schermate principali.

**Test Cases:**
- ✅ navigate_fromHomeToCharts_shouldDisplayChartsScreen
- ✅ navigate_fromHomeToTransactions_shouldDisplayTransactionsScreen
- ✅ navigate_fromHomeToCategories_shouldDisplayCategoriesScreen
- ✅ navigate_fromHomeToSettings_shouldDisplaySettingsScreen
- ✅ navigate_circularNavigation_shouldPreserveState
- ✅ bottomNavigation_shouldBeVisibleOnAllScreens
- ✅ navigate_quickSwitching_shouldHandleRapidNavigation

**Esegui solo questo:**
```bash
./gradlew :androidApp:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.antcashmanager.android.ui.navigation.NavigationTest
```

### 2. HomeScreenTest.kt
Test della home screen.

**Test Cases:**
- ✅ homeScreen_shouldDisplayBalanceCard
- ✅ homeScreen_shouldDisplayTopCards
- ✅ homeScreen_shouldDisplayRecentTransactions
- ✅ homeScreen_customizeButton_shouldOpenDialog
- ✅ homeScreen_addTransactionButton_shouldNavigateToAddTransaction
- ✅ homeScreen_dateFilter_shouldUpdateTransactionDisplay
- ✅ homeScreen_shouldRefreshWhenReturning

### 3. TransactionsScreenTest.kt
Test della schermata transazioni.

**Test Cases:**
- ✅ transactionsScreen_shouldBeNavigable
- ✅ transactionsScreen_shouldDisplayTransactionList
- ✅ transactionsScreen_filterButton_shouldOpenFilterDialog
- ✅ transactionsScreen_sortButton_shouldAllowSorting
- ✅ transactionsScreen_addButton_shouldNavigateToAddTransaction
- ✅ transactionsScreen_scroll_shouldShowMoreTransactions
- ✅ transactionsScreen_statePreservation_whenNavigatingAway

### 4. ChartsScreenTest.kt
Test della schermata grafici.

**Test Cases:**
- ✅ chartsScreen_shouldBeNavigable
- ✅ chartsScreen_shouldDisplayCharts
- ✅ chartsScreen_dateFilter_shouldUpdateCharts
- ✅ chartsScreen_shouldDisplayTrendIndicators
- ✅ chartsScreen_statePreservation_whenNavigatingAway

### 5. CategoriesScreenTest.kt
Test della schermata categorie.

**Test Cases:**
- ✅ categoriesScreen_shouldBeNavigable
- ✅ categoriesScreen_shouldDisplayCategories
- ✅ categoriesScreen_addButton_shouldOpenAddCategoryDialog
- ✅ categoriesScreen_shouldGroupByType
- ✅ categoriesScreen_statePreservation_whenNavigatingAway

### 6. SettingsScreenTest.kt
Test della schermata impostazioni.

**Test Cases:**
- ✅ settingsScreen_shouldBeNavigable
- ✅ settingsScreen_themeButton_shouldOpenThemeDialog
- ✅ settingsScreen_languageButton_shouldOpenLanguageDialog
- ✅ settingsScreen_dataManagement_shouldHaveOptions
- ✅ settingsScreen_changesShouldBePersisted

## 🚀 Eseguire i Test

### Prerequisiti
- Android SDK installato
- Android Emulator in esecuzione O device connesso via ADB
- `JAVA_HOME` configurato correttamente

### Eseguire Tutti i Test di Instrumentation

```bash
./gradlew :androidApp:connectedAndroidTest
```

### Eseguire Solo una Classe di Test

```bash
./gradlew :androidApp:connectedAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.antcashmanager.android.ui.navigation.NavigationTest
```

### View Test Results

```
androidApp/build/reports/androidTests/connected/index.html
```

## ⚠️ Troubleshooting

### Test Failure: "Target Activity not started"
**Soluzione:** Verificare che MainActivity sia configurata correttamente in AndroidManifest.xml

### Test Failure: "No Android devices found"
**Soluzione:** Avviare emulator o connettere device fisico via ADB
```bash
adb devices
```

### Test Timeout
**Soluzione:** Aumentare timeout negli assert
```bash
composeTestRule.mainClock.autoAdvance = true
```

## 🎯 Best Practices

### 1. Use Meaningful Content Descriptions
```kotlin
composeTestRule.onNodeWithContentDescription("Add transaction button")
    .performClick()
```

### 2. Test User Actions, Not Implementation
```kotlin
composeTestRule.onNodeWithText("Save").performClick()
```

### 3. Wait for Async Operations
```kotlin
composeTestRule.waitUntil(timeoutMillis = 5000) {
    composeTestRule.onNodeWithText("Data Loaded").isDisplayed()
}
```

## 📚 Resources

- [Compose Testing](https://developer.android.com/jetpack/compose/testing)
- [AndroidJUnit4](https://developer.android.com/training/testing/junit-runner)
- [createAndroidComposeRule](https://developer.android.com/reference/androidx/compose/ui/test/junit4)

---

**Last Updated:** 2026-08-11  
**Total Test Classes:** 6  
**Total Test Cases:** 60+  
**Status:** Production-Ready
