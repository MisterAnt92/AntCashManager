package com.antcashmanager.android.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.ui.components.AntScreenScaffold
import com.antcashmanager.android.ui.components.rememberAdaptiveLayoutInfo
import com.antcashmanager.android.ui.components.dialog.AppExitConfirmationDialog
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.categories.CategoriesScreen
import com.antcashmanager.android.ui.screen.charts.ChartsScreen
import com.antcashmanager.android.ui.screen.home.HomeScreen
import com.antcashmanager.android.ui.screen.receiptScan.ReceiptScanScreen
import com.antcashmanager.android.ui.screen.settings.SettingsScreen
import com.antcashmanager.android.ui.screen.settingsData.SettingsDataScreen
import com.antcashmanager.android.ui.screen.settingsDisplay.DisplayScreen
import com.antcashmanager.android.ui.screen.transactionAdd.AddTransactionScreen
import com.antcashmanager.android.ui.screen.transactions.TransactionsScreen
import com.antcashmanager.android.util.LocalCurrencyFormat
import com.antcashmanager.domain.model.CurrencyFormat
import com.antcashmanager.domain.repository.SettingsRepository
import org.koin.compose.koinInject

/**
 * Host di navigazione principale dell'app.
 * Tutte le dipendenze (repository, analyticsManager) vengono risolte tramite Koin.
 */
@Composable
fun AntCashManagerNavHost() {
    val settingsRepository: SettingsRepository = koinInject()
    val analyticsManager: AnalyticsManager = koinInject()

    val navController = rememberNavController()
    val showCharts by settingsRepository.getShowCharts().collectAsState(initial = true)
    val isTutorialCompleted by settingsRepository.getIsTutorialCompleted().collectAsState(initial = true)
    val currencySymbol by settingsRepository.getCurrencySymbol().collectAsState(initial = "\u20ac")
    val decimalDigits by settingsRepository.getDecimalDigits().collectAsState(initial = 2)
    val decimalSeparator by settingsRepository.getDecimalSeparator().collectAsState(initial = ",")
    val thousandsSeparator by settingsRepository.getThousandsSeparator().collectAsState(initial = ".")

    val currencyFormat = CurrencyFormat(
        currencySymbol = currencySymbol,
        decimalDigits = decimalDigits,
        decimalSeparator = decimalSeparator,
        thousandsSeparator = thousandsSeparator,
    )

    CompositionLocalProvider(LocalCurrencyFormat provides currencyFormat) {
        val adaptiveLayoutInfo = rememberAdaptiveLayoutInfo()

        val visibleNavItems = buildList {
            add(BottomNavItem.Home)
            if (showCharts) add(BottomNavItem.Charts)
            add(BottomNavItem.Transactions)
            add(BottomNavItem.Categories)
            add(BottomNavItem.Settings)
        }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val context = LocalContext.current
    var showExitDialog by rememberSaveable { mutableStateOf(false) }
    val railContainerWidth = if (adaptiveLayoutInfo.isFoldableDevice) 84.dp else 92.dp
    val railPaddingStart = if (adaptiveLayoutInfo.isFoldableDevice) 8.dp else 12.dp
    val railPaddingEnd = if (adaptiveLayoutInfo.isFoldableDevice) 6.dp else 8.dp
    val isOnTopLevelRoute = currentDestination?.route?.let { currentRoute ->
        visibleNavItems.any { item -> item.route == currentRoute }
    } == true

    BackHandler {
        when {
            showExitDialog -> showExitDialog = false
            isOnTopLevelRoute -> showExitDialog = true
            !navController.popBackStack() -> showExitDialog = true
        }
    }

    LaunchedEffect(currentDestination?.route) {
        currentDestination?.route?.let(analyticsManager::logScreenView)
    }

        AntScreenScaffold(
            showTopBar = false,
            bottomBar = {
                if (isTutorialCompleted && !adaptiveLayoutInfo.preferRailNavigation) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp,
                        shadowElevation = 8.dp,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    ) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp,
                        ) {
                            visibleNavItems.forEach { item ->
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            item.icon,
                                            contentDescription = stringResource(item.titleResId)
                                        )
                                    },
                                    label = {
                                        AppText(
                                            stringResource(item.titleResId),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    },
                                    selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                                    onClick = {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            },
        ) { innerPadding ->
            val navHostContent: @Composable (Modifier) -> Unit = { navModifier ->
                NavHost(
                    navController = navController,
                    startDestination = BottomNavItem.Home.route,
                    modifier = navModifier,
                ) {
                    composable(BottomNavItem.Home.route) {
                        HomeScreen(navController = navController)
                    }
                    composable(BottomNavItem.Charts.route) {
                        ChartsScreen()
                    }
                    composable(BottomNavItem.Transactions.route) {
                        TransactionsScreen(navController = navController)
                    }
                    composable(BottomNavItem.Categories.route) {
                        CategoriesScreen()
                    }
                    composable(BottomNavItem.Settings.route) {
                        SettingsScreen(navController = navController)
                    }
                    composable("display") {
                        DisplayScreen(navController = navController)
                    }
                    composable("settings_data") {
                        SettingsDataScreen(navController = navController)
                    }
                    composable(
                        route = "add_transaction?transactionId={transactionId}",
                        arguments = listOf(
                            androidx.navigation.navArgument("transactionId") {
                                type = androidx.navigation.NavType.LongType
                                defaultValue = -1L
                            }
                        )
                    ) { backStackEntry ->
                        val transactionId =
                            backStackEntry.arguments?.getLong("transactionId")?.takeIf { it != -1L }
                        AddTransactionScreen(
                            transactionId = transactionId,
                            onNavigateBack = { navController.popBackStack() },
                            onTransactionAdded = { navController.popBackStack() },
                        )
                    }
                    composable("receipt_scan") {
                        ReceiptScanScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onTransactionSaved = { navController.popBackStack() },
                        )
                    }
                }
            }

            if (isTutorialCompleted && adaptiveLayoutInfo.preferRailNavigation) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    Surface(
                        tonalElevation = 4.dp,
                        shadowElevation = 6.dp,
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier
                            .padding(
                                start = railPaddingStart,
                                top = 12.dp,
                                end = railPaddingEnd,
                                bottom = 12.dp,
                            )
                            .width(railContainerWidth)
                            .fillMaxHeight(),
                    ) {
                        NavigationRail(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(vertical = 8.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        ) {
                            visibleNavItems.forEach { item ->
                                NavigationRailItem(
                                    selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                                    onClick = {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = stringResource(item.titleResId),
                                        )
                                    },
                                    label = {
                                        AppText(
                                            text = stringResource(item.titleResId),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    },
                                    alwaysShowLabel = true,
                                )
                            }
                        }
                    }

                    navHostContent(Modifier.weight(1f))
                }
            } else {
                navHostContent(Modifier.padding(innerPadding))
            }
        }

        AppExitConfirmationDialog(
            isVisible = showExitDialog,
            onDismiss = { showExitDialog = false },
            onConfirmExit = {
                showExitDialog = false
                context.findActivity()?.finish()
            },
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
