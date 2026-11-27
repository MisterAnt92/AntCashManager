---
description: "Compose Screen and UI components: koinViewModel + state + onEvent wiring, component inventory to reuse, previews, navigation extensions, 13-locale i18n. Use when creating or editing Screens, view/ sub-composables or ui/components."
---

# Agent: Compose UI

Rules live in [AGENTS.md §8, §9, §11](../../AGENTS.md). This file gives the real wiring and the component inventory.

## Screen wiring (from `home/HomeScreen.kt`)

```kotlin
@Composable
fun HomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    val viewModel: HomeViewModel = koinViewModel()               // org.koin.androidx.compose
    val state by viewModel.state.collectAsStateWithLifecycle()   // androidx.lifecycle.compose

    HomeContent(
        state = state,
        onEvent = viewModel::emitEvent,                          // ← emitEvent, NOT onEvent
        onAddTransaction = { navController.navigateToAddTransaction() },
        modifier = modifier,
    )
}

@Composable
internal fun HomeContent(state: HomeState, onEvent: (HomeEvent) -> Unit, onAddTransaction: () -> Unit, modifier: Modifier = Modifier) { … }
```
- `Content` gets plain values + lambdas → previewable and testable. Sub-composables in `view/`.
- Navigation: only `NavigationExtensions.kt` and `AppRoute`. No string literals. Full list:
  `navigateToHome()`, `navigateToCharts()`, `navigateToTransactions()`, `navigateToCategories()`,
  `navigateToSettingsMain()`, `navigateToDisplaySettings()`, `navigateToDataManagement()`,
  `navigateToTutorial()`, `navigateToReceiptScan()`, `navigateToAddTransaction()`,
  `navigateToEditTransaction(transactionId: Long)`, `navigateToProfiles()`,
  `navigateToBottomTab(route)`, `navigateToSettings(route)`, `navigateToTransaction(route)`,
  `safePopBackStack()`.
- Header: `LocalScreenHeaderConfigCallback` + `ScreenHeaderConfig` (see HomeScreen) instead of a custom top bar.

## Component inventory — `ui/components/` (reuse, never recreate)

| Need | Use |
|---|---|
| Screen scaffold / header | `AntScreenScaffold`, `ScreenHeader`, `OptimizedScaffold` |
| Cards | `AppCard`, `AppCardSectionHeader`, `AppCategoryCard`, `AppSelectionItemCard`, `AnimatedCard`, `ExpandableAnimatedCard` |
| Buttons / links | `AppButton`, `TextLink`, `VisibilityToggleButton`, `ReorderButtons`, `HelpButton` |
| Inputs | `AppTextField`, `AutocompleteTextField`, `AppSwitch`, `AppRadioButton`, `AppSlider`, `AppUnitDropdown` |
| Lists | `AppListItem`, `AppCategoryListItem`, `AnimatedListItem` |
| Text / money | `AppText`, `MoneyText`, `CompactMoneyText`, `TransactionAmountText`, `BalanceText`, `AnimatedCounter` |
| State | `AntEmptyState`, `AntErrorState`, `SkeletonLoader`, `TransactionSkeletonLoader`, `BlockingProgressDialog` |
| Filters / search | `SearchComponent`, `DateRangeFilter` |
| Dialogs | `AppHelpDialog`, `HelpDialogContent`, `AppExitConfirmationDialog`, `AnalyticsConsentDialog` |
| Layout | `FoldableAwareLayout`, `rememberAdaptiveLayoutInfo()`, `VerticalSpacer`, `HorizontalSpacer`, `AppDivider`, `NavigationRailTablet`, `LeftSidebar` |
| Motion | `FadeInOnAppear`, `SlideInOnAppear`, `PulsingElement`, `BouncingElement` (respect `LocalReduceMotion`) |

### Screen-specific composables (non in `ui/components/`, ma riusabili come pattern)

| Composable | File | Note |
|---|---|---|
| `IncomeExpenseRow` | `home/view/IncomeExpenseRow.kt` | Carte income/expense con `TrendBadge` opzionale (`incomeDeltaPercent`, `expenseDeltaPercent: Double?`) |
| `TrendBadge` | `home/view/IncomeExpenseRow.kt` | Chip colorato con icona freccia e delta% — `private`, usato dentro `IncomeExpenseRow` |
| `SpendingForecastCard` | `charts/view/ChartCards.kt` | Media 3 mesi + proiezione fine mese (attiva dal giorno 3 del mese) |
| `QuickStatsCard` | `charts/view/ChartCards.kt` | Statistiche giornaliere (max, avg, giorni tracciati) |
| `ChartsDetailsBottomSheet` | `charts/view/ChartsDetailsBottomSheet.kt` | Bottom sheet con breakdown categoria + `TrendDirection` reale |
| `SavingsRateCard` | `home/view/SavingsRateCard.kt` | Risparmio del periodo + tasso% + barra progresso vs goal. Visibile quando `totalIncome > 0`. Params: `savingsAmount`, `savingsRate`, `savingsGoalRate` |
| `BudgetEditDialog` | `categories/view/BudgetEditDialog.kt` | Dialog per impostare/aggiornare/eliminare il budget mensile di una categoria. Params: `categoryName`, `existingBudget`, `onConfirm`, `onDelete`, `onDismiss` |

Regenerate: `grep -rhoE "^(internal )?fun [A-Z]\w+\(" androidApp/src/main/kotlin/com/antcashmanager/android/ui/components | sort -u`

## Theme & spacing

- `MaterialTheme.colorScheme.*` / `MaterialTheme.typography.*` only. Semantic exceptions: `IncomeGreen`, `ExpenseRed` (`ui/theme/Color.kt`). Never `Color(0x…)` or `fontSize = N.sp` in screens.
- Spacing: 8 dp between items, 16 dp screen padding, 24 dp section breaks. `Arrangement.spacedBy(8.dp)` for card lists.
- Touch targets via `AppButton` / `clickable(indication = ripple())`.

## Previews

**REQUIRED for every `@Composable` — including root `*Screen` composables, `ui/components/`, and `screen/<feature>/view/`: 3 variants (Light, Dark, 2x font scale). This applies to ALL composables without exception (AGENTS.md §8).**

Every new `@Composable` created must include these 3 previews immediately:

```kotlin
@Preview(showBackground = true, name = "ComponentName - Light")
@Composable
private fun ComponentNameLightPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        ComponentName(/* realistic sample args */)
    }
}

@Preview(showBackground = true, name = "ComponentName - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ComponentNameDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        ComponentName(/* realistic sample args */)
    }
}

@Preview(showBackground = true, name = "ComponentName - 2x", fontScale = 2.0f)
@Composable
private fun ComponentNameLargeTextPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        ComponentName(/* realistic sample args */)
    }
}
```

**Guidelines:**
- `showBackground = true` on all 3
- Dark uses `darkTheme = true, dynamicColor = false` (not `uiMode` inside theme)
- 2x uses `fontScale = 2.0f` in annotation; same light theme
- Function names: `<ComposableName><Variant>Preview`; all `private`
- Sample args always realistic (never empty `""`, `0`, or defaults that hide real behavior)

## Strings — 13 locales

`values/` + `values-{it,fr,de,es,hi,ja,ko,pl,ru,uk,zh,zh-rTW}`. `grep` first. Translatable → all 13 `strings.xml`; untranslatable (names, symbols, patterns) → only `values/untranslable.xml` with `translatable="false"` (AGENTS.md §11).

## Checklist

- [ ] `koinViewModel()`, `collectAsStateWithLifecycle()`, `viewModel::onEvent`; no repository/business logic
- [ ] Navigation via `NavigationExtensions` / `AppRoute`
- [ ] Reused inventory components; new component only if none fits, placed in `ui/components/<kind>/`
- [ ] Strings via `stringResource`, present in all 13 locales
- [ ] Colors/typography from `MaterialTheme`; spacing 8/16/24
- [ ] Previews Light+Dark+2x for **every** composable (components, `view/`, and root `*Screen`)
- [ ] Screen ≤ 400 lines; sub-composables in `view/`
- [ ] Imports clean, package matches directory
