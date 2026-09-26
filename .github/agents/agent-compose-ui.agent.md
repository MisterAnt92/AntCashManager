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
        onEvent = viewModel::onEvent,
        onAddTransaction = { navController.navigateToAddTransaction() },
        modifier = modifier,
    )
}

@Composable
internal fun HomeContent(state: HomeState, onEvent: (HomeEvent) -> Unit, onAddTransaction: () -> Unit, modifier: Modifier = Modifier) { … }
```
- `Content` gets plain values + lambdas → previewable and testable. Sub-composables in `view/`.
- Navigation: only `NavigationExtensions.kt` (`navigateToHome/Charts/Transactions/Categories/SettingsMain/DisplaySettings/DataManagement/Tutorial/ReceiptScan/AddTransaction()`, `navigateToTransaction(route)`, `safePopBackStack()`) and `AppRoute`. No string literals.
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

Regenerate: `grep -rhoE "^(internal )?fun [A-Z]\w+\(" androidApp/src/main/kotlin/com/antcashmanager/android/ui/components | sort -u`

## Theme & spacing

- `MaterialTheme.colorScheme.*` / `MaterialTheme.typography.*` only. Semantic exceptions: `IncomeGreen`, `ExpenseRed` (`ui/theme/Color.kt`). Never `Color(0x…)` or `fontSize = N.sp` in screens.
- Spacing: 8 dp between items, 16 dp screen padding, 24 dp section breaks. `Arrangement.spacedBy(8.dp)` for card lists.
- Touch targets via `AppButton` / `clickable(indication = ripple())`.

## Previews

**REQUIRED for every composable in `ui/components/` and `screen/<feature>/view/`: 3 variants (Light, Dark, 2x font scale).** Root `*Screen` composables have **no** previews (removed in FASE 7a — do not add them back).

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
- [ ] Previews light+dark for components and `view/` composables
- [ ] Screen ≤ 400 lines; sub-composables in `view/`
- [ ] Imports clean, package matches directory
