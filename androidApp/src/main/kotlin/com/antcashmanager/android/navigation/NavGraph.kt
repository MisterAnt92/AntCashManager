package com.antcashmanager.android.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import co.touchlab.kermit.Logger
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.analytics.tracker.PerformanceTracker
import com.antcashmanager.android.ui.components.animation.AntEasterEggAnimation
import com.antcashmanager.android.ui.components.dialog.AppExitConfirmationDialog
import com.antcashmanager.android.ui.components.layout.AntScreenScaffold
import com.antcashmanager.android.ui.components.layout.LeftSidebar
import com.antcashmanager.android.ui.components.layout.LocalDisplayFeatures
import com.antcashmanager.android.ui.components.layout.rememberAdaptiveLayoutInfo
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.categories.view.CategoriesScreen
import com.antcashmanager.android.ui.screen.charts.view.ChartsScreen
import com.antcashmanager.android.ui.screen.home.HomeScreen
import com.antcashmanager.android.ui.screen.receiptScan.view.ReceiptScanScreen
import com.antcashmanager.android.ui.screen.settings.dataManagement.SettingsDataScreen
import com.antcashmanager.android.ui.screen.settings.displaySettings.DisplayScreen
import com.antcashmanager.android.ui.screen.settings.view.SettingsScreen
import com.antcashmanager.android.ui.screen.splash.SplashScreen
import com.antcashmanager.android.ui.screen.transactions.addImport.AddTransactionScreen
import com.antcashmanager.android.ui.screen.transactions.view.TransactionsScreen
import com.antcashmanager.android.ui.screen.tutorial.TutorialScreen
import com.antcashmanager.android.util.AppExitManager.safeFinish
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
    val performanceTracker: PerformanceTracker = koinInject()

    val navController = rememberNavController()
    val showCharts by settingsRepository.getShowCharts().collectAsStateWithLifecycle(initialValue = true)
    val isTutorialCompleted by settingsRepository
        .getIsTutorialCompleted()
        .collectAsStateWithLifecycle(initialValue = null)
    val currencySymbol by settingsRepository.getCurrencySymbol().collectAsStateWithLifecycle(initialValue = "\u20ac")
    val decimalDigits by settingsRepository.getDecimalDigits().collectAsStateWithLifecycle(initialValue = 2)
    val decimalSeparator by settingsRepository.getDecimalSeparator().collectAsStateWithLifecycle(initialValue = ",")
    val thousandsSeparator by settingsRepository
        .getThousandsSeparator()
        .collectAsStateWithLifecycle(initialValue = "")
    val maskAmounts by settingsRepository.getMaskAmounts().collectAsStateWithLifecycle(initialValue = false)

    val currencyFormat =
        CurrencyFormat(
            currencySymbol = currencySymbol,
            decimalDigits = decimalDigits,
            decimalSeparator = decimalSeparator,
            thousandsSeparator = thousandsSeparator,
        )

    if (isTutorialCompleted != null) {
        // Mostra il contenuto principale solo dopo che i dati critici sono caricati
        CompositionLocalProvider(
            LocalCurrencyFormat provides currencyFormat,
            LocalAmountsMasked provides maskAmounts,
        ) {
            // FASE 1: Get display features from CompositionLocal and pass to adaptive layout
            val displayFeatures = LocalDisplayFeatures.current
            val adaptiveLayoutInfo = rememberAdaptiveLayoutInfo(displayFeatures = displayFeatures)
            // ... existing AntScreenScaffold code ...

            val visibleNavItems =
                buildList {
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
            val sidebarNavItems =
                buildList {
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
            // CRITICAL FIX: Capture context at the moment exit dialog is shown to prevent
            // race conditions when language change causes WithAppLocale recomposition.
            // Without this, the context becomes stale during the 300ms dismissal delay.
            // Use remember (not rememberSaveable) because Context cannot be saved to Bundle.
            // Capture the context value when showExitDialog becomes true via LaunchedEffect.
            var exitDialogContext by remember { mutableStateOf(context) }

            var isSidebarOpen by rememberSaveable { mutableStateOf(false) }
            var showAntAnimation by rememberSaveable { mutableStateOf(false) }
            var screenHeaderConfig by remember { mutableStateOf(ScreenHeaderConfig()) }
            val railContainerWidth = if (adaptiveLayoutInfo.isFoldableDevice) 84.dp else 92.dp
            val railPaddingStart = if (adaptiveLayoutInfo.isFoldableDevice) 8.dp else 12.dp
            val railPaddingEnd = if (adaptiveLayoutInfo.isFoldableDevice) 6.dp else 8.dp
            val isOnTopLevelRoute =
                currentDestination?.route?.let { currentRoute ->
                    visibleNavItems.any { item -> item.route == currentRoute }
                } == true

            val isOnTutorial = currentDestination?.route == AppRoute.BottomRoute.Tutorial.route

            BackHandler {
                when {
                    isSidebarOpen -> isSidebarOpen = false
                    showExitDialog -> showExitDialog = false
                    isOnTopLevelRoute -> {
                        showExitDialog = true
                        // FIX 1: Capture stable context BEFORE dialog shows
                        // This prevents race condition with language change recompositions
                        exitDialogContext = context
                    }
                    !navController.popBackStack() -> {
                        showExitDialog = true
                        // FIX 1: Capture stable context BEFORE dialog shows
                        exitDialogContext = context
                    }
                }
            }

            LaunchedEffect(currentDestination?.route) {
                currentDestination?.route?.let { route ->
                    val startTime = System.currentTimeMillis()
                    analyticsManager.logScreenView(route)
                    performanceTracker.trackScreenLoadTime(route, System.currentTimeMillis() - startTime)
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                AntScreenScaffold(
                    showTopBar = false,
                    bottomBar = {
                        // Bottom bar non visibile su Categories, Settings e Tutorial
                        val isOnCategoriesSettingsOrTutorial =
                            currentDestination?.route?.let { currentRoute ->
                                currentRoute == AppRoute.BottomRoute.Categories.route ||
                                    AppRoute.isSettingsRoute(currentRoute) ||
                                    currentRoute == AppRoute.BottomRoute.Tutorial.route
                            } == true

                        if (isTutorialCompleted == true &&
                            !adaptiveLayoutInfo.preferRailNavigation &&
                            !isSidebarOpen &&
                            !isOnCategoriesSettingsOrTutorial
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 6.dp,
                                shadowElevation = 8.dp,
                                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                            ) {
                                NavigationBar(
                                    modifier = Modifier.testTag("bottom_nav_bar"),
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 0.dp,
                                ) {
                                    visibleNavItems.forEach { item ->
                                        val isSelected =
                                            currentDestination?.hierarchy?.any { it.route == item.route } == true
                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = {
                                                val params =
                                                    android.os.Bundle().apply {
                                                        putString("destination", item.route)
                                                    }
                                                analyticsManager.logEvent("sidebar_navigation_clicked", params)
                                                navController.navigateToBottomTab(item.route)
                                            },
                                            modifier = Modifier.testTag("nav_${item.route}"),
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
                                            colors =
                                                NavigationBarItemDefaults.colors(
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
                            },
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = AppRoute.BottomRoute.Home.route,
                                modifier = navModifier,
                            ) {
                                composable(
                                    route = AppRoute.BottomRoute.Home.route,
                                    deepLinks =
                                        listOf(
                                            androidx.navigation.navDeepLink {
                                                uriPattern = "https://antcashmanager.com/app/home"
                                            },
                                            androidx.navigation.navDeepLink {
                                                uriPattern = "antcashmanager://app/home"
                                            },
                                        ),
                                ) {
                                    HomeScreen(navController = navController)
                                }
                                composable(AppRoute.BottomRoute.Charts.route) {
                                    ChartsScreen()
                                }
                                composable(
                                    route = AppRoute.BottomRoute.Transactions.route,
                                    deepLinks =
                                        listOf(
                                            androidx.navigation.navDeepLink {
                                                uriPattern = "https://antcashmanager.com/app/transactions"
                                            },
                                            androidx.navigation.navDeepLink {
                                                uriPattern = "antcashmanager://app/transactions"
                                            },
                                        ),
                                ) {
                                    TransactionsScreen(navController = navController)
                                }
                                composable(
                                    route = AppRoute.BottomRoute.Categories.route,
                                    deepLinks =
                                        listOf(
                                            androidx.navigation.navDeepLink {
                                                uriPattern = "https://antcashmanager.com/app/categories"
                                            },
                                            androidx.navigation.navDeepLink {
                                                uriPattern = "antcashmanager://app/categories"
                                            },
                                        ),
                                ) {
                                    CategoriesScreen()
                                }
                                // Nested navigation graph per Settings e sub-screens
                                navigation(
                                    startDestination = AppRoute.SettingsRoute.Main.route,
                                    route = "settings_graph",
                                ) {
                                    composable(AppRoute.SettingsRoute.Main.route) {
                                        SettingsScreen(navController = navController)
                                    }
                                    composable(AppRoute.SettingsRoute.Display.route) {
                                        DisplayScreen(navController = navController)
                                    }
                                    composable(AppRoute.SettingsRoute.DataManagement.route) {
                                        SettingsDataScreen(navController = navController)
                                    }
                                }

                                composable(AppRoute.BottomRoute.Tutorial.route) {
                                    TutorialScreen(
                                        navController = navController,
                                    )
                                }
                                composable(
                                    route = AppRoute.TransactionRoute.Add.route + "?transactionId={transactionId}",
                                    arguments =
                                        listOf(
                                            androidx.navigation.navArgument("transactionId") {
                                                type = androidx.navigation.NavType.LongType
                                                defaultValue = -1L
                                            },
                                        ),
                                ) { backStackEntry ->
                                    val transactionId =
                                        backStackEntry.arguments?.getLong("transactionId")?.takeIf { it != -1L }
                                    AddTransactionScreen(
                                        transactionId = transactionId,
                                        navController = navController,
                                    )
                                }
                                composable(AppRoute.TransactionRoute.ReceiptScan.route) {
                                    ReceiptScanScreen(
                                        navController = navController,
                                    )
                                }
                            }
                        }
                    }

                    if (isTutorialCompleted == true && adaptiveLayoutInfo.preferRailNavigation && !isOnTutorial) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                        ) {
                            // Top bar con pulsanti (Search, Filter, Sort, Helper) - senza Hamburger menu su tablet
                            if (!isOnTutorial) {
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    // Screen Title (left side)
                                    if (screenHeaderConfig.title.isNotEmpty()) {
                                        AppText(
                                            text = screenHeaderConfig.title,
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground,
                                        )
                                    }

                                    // Filter/Order/Search Icons (right side) - Tablet version (senza hamburger)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        // Search Icon
                                        if (screenHeaderConfig.showSearchIcon) {
                                            IconButton(
                                                onClick = { screenHeaderConfig.onSearchClick?.invoke() },
                                                modifier =
                                                    Modifier
                                                        .size(48.dp)
                                                        .testTag("header_search_icon"),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = "Search",
                                                    modifier = Modifier.size(24.dp),
                                                    tint = MaterialTheme.colorScheme.primary,
                                                )
                                            }
                                        }

                                        // Filter Icon with Badge
                                        if (screenHeaderConfig.onFilterClick != null) {
                                            IconButton(
                                                onClick = { screenHeaderConfig.onFilterClick!!.invoke() },
                                                modifier = Modifier.size(48.dp),
                                            ) {
                                                Box {
                                                    Icon(
                                                        imageVector = Icons.Default.FilterList,
                                                        contentDescription = "Filtri",
                                                        modifier = Modifier.size(24.dp),
                                                        tint = MaterialTheme.colorScheme.primary,
                                                    )
                                                    // Badge for active filters
                                                    if (screenHeaderConfig.filterCount > 0) {
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = MaterialTheme.colorScheme.error,
                                                            modifier =
                                                                Modifier
                                                                    .size(18.dp)
                                                                    .align(Alignment.TopEnd)
                                                                    .padding(top = 2.dp, end = 2.dp),
                                                        ) {
                                                            AppText(
                                                                text = screenHeaderConfig.filterCount.toString(),
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onError,
                                                                modifier =
                                                                    Modifier
                                                                        .padding(2.dp)
                                                                        .align(Alignment.Center),
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Sort/Order Icon
                                        if (screenHeaderConfig.hasOrderOption) {
                                            IconButton(
                                                onClick = { screenHeaderConfig.onOrderClick?.invoke() },
                                                modifier = Modifier.size(48.dp),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Sort,
                                                    contentDescription = "Ordinamento",
                                                    modifier = Modifier.size(24.dp),
                                                    tint = MaterialTheme.colorScheme.primary,
                                                )
                                            }
                                        }

                                        // Action Buttons (dynamic) - Helper button
                                        if (screenHeaderConfig.actions != null) {
                                            screenHeaderConfig.actions!!()
                                        }
                                    }
                                }
                            }

                            // Navigation Rail + Content
                            Row(
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                Surface(
                                    tonalElevation = 4.dp,
                                    shadowElevation = 6.dp,
                                    shape = RoundedCornerShape(24.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    modifier =
                                        Modifier
                                            .padding(
                                                start = railPaddingStart,
                                                top = 12.dp,
                                                end = railPaddingEnd,
                                                bottom = 12.dp,
                                            ).width(railContainerWidth)
                                            .fillMaxHeight(),
                                ) {
                                    NavigationRail(
                                        modifier =
                                            Modifier
                                                .fillMaxHeight()
                                                .padding(vertical = 8.dp),
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    ) {
                                        visibleNavItems.forEach { item ->
                                            NavigationRailItem(
                                                selected =
                                                    currentDestination?.hierarchy?.any {
                                                        it.route == item.route
                                                    } == true,
                                                onClick = {
                                                    val params =
                                                        android.os.Bundle().apply {
                                                            putString("destination", item.route)
                                                        }
                                                    analyticsManager.logEvent("sidebar_navigation_clicked", params)
                                                    navController.navigateToBottomTab(item.route)
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
                        }
                    } else if (isTutorialCompleted == true) {
                        // Per dispositivi NON tablet: Sidebar scorrevole (drawer-style) + Content con BottomBar
                        Box(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            // Content con pulsante hamburger + innerPadding
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .padding(if (isOnTutorial) PaddingValues() else innerPadding),
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    // Top bar con hamburger menu - nascosto su Tutorial
                                    if (!isOnTutorial) {
                                        Row(
                                            modifier =
                                                Modifier
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
                                                        val params =
                                                            android.os.Bundle().apply {
                                                                putBoolean("isOpen", !isSidebarOpen)
                                                            }
                                                        analyticsManager.logEvent("sidebar_toggled", params)
                                                        isSidebarOpen = !isSidebarOpen
                                                    },
                                                    modifier = Modifier.size(48.dp),
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

                                            // Filter/Order/Search Icons (dynamic)
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            ) {
                                                // Search Icon
                                                if (screenHeaderConfig.showSearchIcon) {
                                                    IconButton(
                                                        onClick = { screenHeaderConfig.onSearchClick?.invoke() },
                                                        modifier =
                                                            Modifier
                                                                .size(48.dp)
                                                                .testTag("header_search_icon"),
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Search,
                                                            contentDescription = "Search",
                                                            modifier = Modifier.size(24.dp),
                                                            tint = MaterialTheme.colorScheme.primary,
                                                        )
                                                    }
                                                }

                                                // Filter Icon with Badge
                                                if (screenHeaderConfig.onFilterClick != null) {
                                                    IconButton(
                                                        onClick = { screenHeaderConfig.onFilterClick!!.invoke() },
                                                        modifier = Modifier.size(48.dp),
                                                    ) {
                                                        Box {
                                                            Icon(
                                                                imageVector = Icons.Default.FilterList,
                                                                contentDescription = "Filtri",
                                                                modifier = Modifier.size(24.dp),
                                                                tint = MaterialTheme.colorScheme.primary,
                                                            )
                                                            // Badge for active filters
                                                            if (screenHeaderConfig.filterCount > 0) {
                                                                Surface(
                                                                    shape = CircleShape,
                                                                    color = MaterialTheme.colorScheme.error,
                                                                    modifier =
                                                                        Modifier
                                                                            .size(18.dp)
                                                                            .align(Alignment.TopEnd)
                                                                            .padding(top = 2.dp, end = 2.dp),
                                                                ) {
                                                                    AppText(
                                                                        text = screenHeaderConfig.filterCount.toString(),
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        color = MaterialTheme.colorScheme.onError,
                                                                        modifier =
                                                                            Modifier
                                                                                .padding(2.dp)
                                                                                .align(Alignment.Center),
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                // Sort/Order Icon
                                                if (screenHeaderConfig.hasOrderOption) {
                                                    IconButton(
                                                        onClick = { screenHeaderConfig.onOrderClick?.invoke() },
                                                        modifier = Modifier.size(48.dp),
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Sort,
                                                            contentDescription = "Ordinamento",
                                                            modifier = Modifier.size(24.dp),
                                                            tint = MaterialTheme.colorScheme.primary,
                                                        )
                                                    }
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

                // Sidebar animata (slide in/out) con dimming overlay - LIVELLO TOP-LEVEL FUORI DALLO SCAFFOLD
                // Posizionato SOPRA lo Scaffold per coprire tutta la superficie dello schermo (inclusi i margini)
                AnimatedVisibility(
                    visible = isSidebarOpen,
                    enter = slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)),
                    exit = slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        // Dimming overlay - copre TUTTA la superficie dello schermo senza alcun margine
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .clickable(
                                        enabled = isSidebarOpen,
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                    ) {
                                        isSidebarOpen = false
                                    },
                        )

                        // Sidebar - non propagates clicks al dimming overlay
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
                            modifier =
                                Modifier
                                    .padding(top = 8.dp, bottom = 8.dp),
                        )
                    }
                }
            }

            AppExitConfirmationDialog(
                isVisible = showExitDialog,
                onDismiss = {
                    Logger.d(tag = "AppExit") { "Exit dialog dismissed" }
                    showExitDialog = false
                },
                onConfirmExit = {
                    // Dialog invokes this after a 500ms delay (handles WithAppLocale recomposition timing)
                    Logger.d(tag = "AppExit") { "Exit confirmed, calling Activity.safeFinish()" }
                    // FIX 1: Use captured context (exitDialogContext) to prevent race condition
                    // with language change. exitDialogContext is captured synchronously in BackHandler
                    // and is stable even if WithAppLocale recomposes LocalContext.
                    val activity = exitDialogContext.findActivity()
                    if (activity != null) {
                        activity.safeFinish()
                    } else {
                        Logger.e(tag = "AppExit") { "findActivity() returned null — falling back to System.exit(0)" }
                        System.exit(0)
                    }
                },
            )

            if (showAntAnimation) {
                AntEasterEggAnimation(
                    versionName = com.antcashmanager.android.BuildConfig.VERSION_NAME,
                    onDismiss = { showAntAnimation = false },
                )
            }
        }
    } else {
        // Mostra il splash screen mentre i dati critici vengono caricati
        SplashScreen()
    }
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
