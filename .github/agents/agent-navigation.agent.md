---
description: "Navigation rules for AntCashManager: AppRoute sealed classes, NavigationExtensions.kt type-safe functions, NavGraph wiring, adding new screens, deep links, top/bottom bar visibility. Use when creating or editing navigation, adding new screens, or modifying routing logic."
---

# Agent: Navigation

Rules live in [AGENTS.md §14](../../AGENTS.md). This file gives the complete reference.

## Route hierarchy (AppRoute.kt)

```kotlin
AppRoute.BottomRoute:      Home ("home"), Charts ("charts"), Transactions ("transactions"),
                           Categories ("categories"), Settings ("settings")
AppRoute.TransactionRoute: List ("transactions"), Add ("add_transaction"),
                           Edit.createRoute(id) → "add_transaction?transactionId=$id",
                           ReceiptScan ("receipt_scan")
AppRoute.SettingsRoute:    Main ("settings"), Display ("display"),
                           DataManagement ("settings_data"), Profiles ("profiles")
AppRoute.ChartRoute:       Charts ("charts")
AppRoute.CategoryRoute:    Categories ("categories")
AppRoute.HomeRoute:        Home ("home")
AppRoute.ReceiptRoute:     ReceiptScan ("receipt_scan")
```

**`isSettingsRoute(route)`**: true for Main, Display, DataManagement, Profiles.  
**`isSubScreen(route)`**: true for `add_transaction` (startsWith), `receipt_scan`, `display`, `settings_data`, `profiles` — hides global top bar and bottom bar.

## Type-safe navigation functions (NavigationExtensions.kt)

| Function | Destination | Behavior |
|---|---|---|
| `navigateToHome()` | BottomRoute.Home | navigateToBottomTab (saveState, restoreState) |
| `navigateToCharts()` | BottomRoute.Charts | navigateToBottomTab |
| `navigateToTransactions()` | BottomRoute.Transactions | navigateToBottomTab |
| `navigateToCategories()` | BottomRoute.Categories | navigateToBottomTab |
| `navigateToSettingsMain()` | SettingsRoute.Main | navigateToSettings (launchSingleTop) |
| `navigateToDisplaySettings()` | SettingsRoute.Display | navigateToSettings |
| `navigateToDataManagement()` | SettingsRoute.DataManagement | navigateToSettings |
| `navigateToProfiles()` | SettingsRoute.Profiles | navigateToSettings |
| `navigateToReceiptScan()` | TransactionRoute.ReceiptScan | navigateToTransaction (launchSingleTop) |
| `navigateToAddTransaction()` | TransactionRoute.Add | navigateToTransaction |
| `navigateToEditTransaction(id)` | TransactionRoute.Edit.createRoute(id) | navigateToTransaction |
| `safePopBackStack()` | (back) | popBackStack() se previousBackStackEntry != null |

**NEVER** use `navController.navigate("string_literal")` in composables. Always use the functions above.

**Legitimate direct navigation** (only inside NavGraph.kt):
- `navController.navigate(AppRoute.SettingsRoute.Profiles.route)` from LeftSidebar header
- `navController.navigateToBottomTab(item.route)` from dynamic nav items

## Tutorial (not a route)

The tutorial is a Reveal spotlight overlay — **not** a screen or route. Managed by `TutorialViewModel` inside `NavGraph.kt`.
- First run: `tutorialViewModel.emitEvent(TutorialEvent.StartTutorial)` (has upgrade safeguard)
- Manual replay from Settings: `tutorialViewModel.emitEvent(TutorialEvent.ForceStartTutorial)` (bypasses safeguard)
- **Do not** add a tutorial route. **Do not** add `navigateToTutorial()`.

## TransactionDetails (not a route)

`TransactionDetailsDialog` is an inline dialog, not a separate route. Shown:
- From `HomeScreen` via `ShowTransactionDetailsEvent` → navigates to edit via `navigateToEditTransaction(id)`
- From `TransactionsScreen` as a split-pane detail on foldable, or as a dialog on phone

## NavGraph.kt — structure

```
NavHost(startDestination = "home") {
  composable("home") { HomeScreen }              // deep links registered
  composable("charts") { ChartsScreen }
  composable("transactions") { TransactionsScreen }  // deep links registered
  composable("categories") { CategoriesScreen }  // deep links registered
  navigation(startDestination = "settings", route = "settings_graph") {
    composable("settings") { SettingsScreen }
    composable("display") { DisplayScreen }
    composable("settings_data") { SettingsDataScreen }
    composable("profiles") { ProfilesScreen }
  }
  composable("add_transaction?transactionId={transactionId}") { AddTransactionScreen }
  composable("receipt_scan") { ReceiptScanScreen }
}
```

## Phone vs Tablet navigation

| | Phone | Tablet / Foldable |
|---|---|---|
| Primary nav | BottomNavigationBar: Home, [Charts], Transactions | NavigationRail: Home, [Charts], Transactions, Categories, Settings |
| Categories / Settings | LeftSidebar (hamburger) only | NavigationRail |
| Sub-screens | Full-screen, all bars hidden | Full-screen, all bars hidden |
| `isSubScreen` | hides BottomBar + top bar | hides Rail top bar + top bar row |
| Split-pane | CategoriesScreen (fold) | Home, Transactions, Categories |

## Deep links configured

| Screen | URI patterns |
|---|---|
| Home | `https://antcashmanager.com/app/home`, `antcashmanager://app/home` |
| Transactions | `https://antcashmanager.com/app/transactions`, `antcashmanager://app/transactions` |
| Categories | `https://antcashmanager.com/app/categories`, `antcashmanager://app/categories` |

## How to add a new screen — full checklist

```
1. AppRoute.kt          → add sealed class entry: object NewScreen : AppRoute.XxxRoute("new_screen")
2. AppRoute.kt          → add to isSubScreen() / isSettingsRoute() if applicable
3. NavigationExtensions → add navigateToNewScreen() using navigateToTransaction() / navigateToSettings()
4. NavGraph.kt          → register composable() (or inside navigation() block for settings sub)
5. NewScreen.kt         → @Composable with 3 @Preview (Light/Dark/2x) — AGENTS.md §8
6. docs/navigation-flow.drawio → add node + edges + annotation box
7. agent-navigation.agent.md → update route table
8. strings.xml (13 locales) → screen title if shown in top bar
```

## Checklist

- [ ] Only `AppRoute.*` and `NavigationExtensions.kt` — no string literals in composables
- [ ] New route registered in `AppRoute.kt` + `NavGraph.kt` + `NavigationExtensions.kt`
- [ ] If sub-screen: added to `isSubScreen()`
- [ ] `docs/navigation-flow.drawio` updated (node + edges + annotation)
- [ ] `agent-navigation.agent.md` route table updated
