package com.antcashmanager.android.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import com.antcashmanager.android.navigation.LocalScreenHeaderConfigCallback
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.antcashmanager.android.BuildConfig
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.ui.components.animation.AntEasterEggAnimation
import com.antcashmanager.android.ui.components.animation.AntSplashScreen
import com.antcashmanager.android.ui.components.dialog.AppExitConfirmationDialog
import com.antcashmanager.android.ui.components.layout.AntScreenScaffold
import com.antcashmanager.android.ui.components.layout.LeftSidebar
import com.antcashmanager.android.ui.components.layout.rememberAdaptiveLayoutInfo
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.categories.view.CategoriesScreen
import com.antcashmanager.android.ui.screen.charts.view.ChartsScreen
import com.antcashmanager.android.ui.screen.home.HomeScreen
import com.antcashmanager.android.ui.screen.receiptScan.view.ReceiptScanScreen
import com.antcashmanager.android.ui.screen.settings.dataManagement.SettingsDataScreen
import com.antcashmanager.android.ui.screen.settings.displaySettings.DisplayScreen
import com.antcashmanager.android.ui.screen.settings.view.SettingsScreen
import com.antcashmanager.android.ui.screen.transactions.addImport.AddTransactionScreen
import com.antcashmanager.android.ui.screen.transactions.view.TransactionsScreen
import com.antcashmanager.android.ui.screen.tutorial.TutorialScreen
import com.antcashmanager.android.util.LocalAmountsMasked
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
    val isTutorialCompleted by settingsRepository.getIsTutorialCompleted()
        .collectAsState(initial = true)
    val currencySymbol by settingsRepository.getCurrencySymbol().collectAsState(initial = "\u20ac")
    val decimalDigits by settingsRepository.getDecimalDigits().collectAsState(initial = 2)
    val decimalSeparator by settingsRepository.getDecimalSeparator().collectAsState(initial = ",")
    val thousandsSeparator by settingsRepository.getThousandsSeparator()
        .collectAsState(initial = "")
    val maskAmounts by settingsRepository.getMaskAmounts().collectAsState(initial = false)

    val showInitialAnimation = false
    var showSplash by rememberSaveable { mutableStateOf(true) }

    if (showSplash && showInitialAnimation) {
        AntSplashScreen(onAnimationFinished = { showSplash = false })
        return
    }

    val currencyFormat = CurrencyFormat(
        currencySymbol = currencySymbol,
        decimalDigits = decimalDigits,
        decimalSeparator = decimalSeparator,
        thousandsSeparator = thousandsSeparator,
    )

    CompositionLocalProvider(
        LocalCurrencyFormat provides currencyFormat,
        LocalAmountsMasked provides maskAmounts,
    ) {
        val adaptiveLayoutInfo = rememberAdaptiveLayoutInfo()
        // ... existing AntScreenScaffold code ...

        val visibleNavItems = buildList {
            add(BottomNavItem.Home)
            if (showCharts) add(BottomNavItem.Charts)
            add(BottomNavItem.Transactions)
            // Categories, Settings e Tutorial solo su tablet (rail navigation)
            // Su phone, sono accessibili dalla sidebar laterale
            if (adaptiveLayoutInfo.preferRailNavigation) {
                add(BottomNavItem.Categories)
                add(BottomNavItem.Settings)
                add(BottomNavItem.Tutorial)
            }
        }

        // Sidebar mostra sempre tutti gli item (incluso Charts se abilitato)
        val sidebarNavItems = buildList {
            add(BottomNavItem.Home)
            if (showCharts) add(BottomNavItem.Charts)
            add(BottomNavItem.Transactions)
            add(BottomNavItem.Categories)
            add(BottomNavItem.Settings)
            add(BottomNavItem.Tutorial)
        }

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        val context = LocalContext.current
        var showExitDialog by rememberSaveable { mutableStateOf(false) }
        var isSidebarOpen by rememberSaveable { mutableStateOf(false) }
        var showAntAnimation by rememberSaveable { mutableStateOf(false) }
        var screenHeaderConfig by remember { mutableStateOf(ScreenHeaderConfig()) }
        val railContainerWidth = if (adaptiveLayoutInfo.isFoldableDevice) 84.dp else 92.dp
        val railPaddingStart = if (adaptiveLayoutInfo.isFoldableDevice) 8.dp else 12.dp
        val railPaddingEnd = if (adaptiveLayoutInfo.isFoldableDevice) 6.dp else 8.dp
        val isOnTopLevelRoute = currentDestination?.route?.let { currentRoute ->
            visibleNavItems.any { item -> item.route == currentRoute }
        } == true

        val isOnTutorial = currentDestination?.route == BottomNavItem.Tutorial.route

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
                // Bottom bar non visibile su Categories, Settings e Tutorial
                val isOnCategoriesSettingsOrTutorial =
                    currentDestination?.route?.let { currentRoute ->
                        currentRoute == BottomNavItem.Categories.route ||
                                currentRoute == BottomNavItem.Settings.route ||
                                currentRoute == BottomNavItem.Tutorial.route
                    } == true

                if (isTutorialCompleted && !adaptiveLayoutInfo.preferRailNavigation && !isSidebarOpen && !isOnCategoriesSettingsOrTutorial) {
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
                                val isSelected =
                                    currentDestination?.hierarchy?.any { it.route == item.route } == true
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        val params = android.os.Bundle().apply {
                                            putString("destination", item.route)
                                        }
                                        analyticsManager.logEvent("sidebar_navigation_clicked", params)
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
                                            item.icon,
                                            contentDescription = stringResource(item.titleResId),
                                        )
                                    },
                                    label = {
                                        AppText(
                                            stringResource(item.titleResId),
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    ),
                                )
                            }
                        }
                    }
                }
            },
        ) { innerPadding ->
            val navHostContent: @Composable (Modifier) -> Unit = { navModifier ->
                CompositionLocalProvider(
                    LocalScreenHeaderConfigCallback provides { config ->
                        screenHeaderConfig = config
                    }
                ) {
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
                        composable(BottomNavItem.Tutorial.route) {
                            TutorialScreen(
                                onNavigateBack = { navController.popBackStack() },
                            )
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
            }

            if (isTutorialCompleted && adaptiveLayoutInfo.preferRailNavigation && !isOnTutorial) {
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
                                        val params = android.os.Bundle().apply {
                                            putString("destination", item.route)
                                        }
                                        analyticsManager.logEvent("sidebar_navigation_clicked", params)
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
            } else if (isTutorialCompleted) {
                // Per dispositivi NON tablet: Sidebar scorrevole (drawer-style) + Content con BottomBar
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (isOnTutorial) PaddingValues() else innerPadding),
                ) {
                    // Content con pulsante hamburger
                    Column(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        // Top bar con hamburger menu - nascosto su Tutorial
                        if (!isOnTutorial) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    // Hamburger Menu
                                    IconButton(
                                        onClick = {
                                            val params = android.os.Bundle().apply {
                                                putBoolean("isOpen", !isSidebarOpen)
                                            }
                                            analyticsManager.logEvent("sidebar_toggled", params)
                                            isSidebarOpen = !isSidebarOpen
                                        },
                                        modifier = Modifier.size(40.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "Toggle Navigation",
                                            modifier = Modifier.size(24.dp),
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    }

                                    // Screen Title (dynamic)
                                    if (screenHeaderConfig.title.isNotEmpty()) {
                                        AppText(
                                            text = screenHeaderConfig.title,
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground,
                                        )
                                    }
                                }

                                // Action Buttons (dynamic)
                                if (screenHeaderConfig.actions != null) {
                                    screenHeaderConfig.actions!!()
                                }
                            }
                        }

                        // Content
                        navHostContent(Modifier.weight(1f))
                    }

                    // Sidebar animata (slide in/out)
                    AnimatedVisibility(
                        visible = isSidebarOpen,
                        enter = slideInHorizontally(initialOffsetX = { -it }),
                        exit = slideOutHorizontally(targetOffsetX = { -it }),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .clickable(
                                    enabled = isSidebarOpen,
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) {
                                    isSidebarOpen = false
                                },
                        ) {
                            LeftSidebar(
                                selectedRoute = currentDestination?.route,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                    // Chiudi la sidebar dopo la navigazione
                                    isSidebarOpen = false
                                },
                                visibleNavItems = sidebarNavItems,
                                onHeaderClick = { showAntAnimation = true },
                                modifier = Modifier
                                    .padding(top = 8.dp, bottom = 8.dp),
                            )
                        }
                    }
                }
            } else if (isOnTutorial && adaptiveLayoutInfo.preferRailNavigation) {
                // Tutorial in fullscreen su tablet
                navHostContent(Modifier.fillMaxSize())
            } else {
                // Caso di fallback (tutorial non completato)
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

        if (showAntAnimation) {
            AntEasterEggAnimation(
                versionName = BuildConfig.VERSION_NAME,
                onDismiss = { showAntAnimation = false },
            )
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
