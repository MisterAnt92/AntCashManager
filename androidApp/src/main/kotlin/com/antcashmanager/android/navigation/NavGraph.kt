package com.antcashmanager.android.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.antcashmanager.android.ui.categories.CategoriesScreen
import com.antcashmanager.android.ui.charts.ChartsScreen
import com.antcashmanager.android.ui.home.HomeScreen
import com.antcashmanager.android.ui.settings.SettingsScreen
import com.antcashmanager.android.ui.settings.display.DisplayScreen
import com.antcashmanager.android.ui.transactions.TransactionsScreen
import com.antcashmanager.android.util.LocalCurrencyFormat
import com.antcashmanager.domain.model.CurrencyFormat
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Charts,
    BottomNavItem.Transactions,
    BottomNavItem.Categories,
    BottomNavItem.Settings,
)

@Composable
fun AntCashManagerNavHost(
    transactionRepository: TransactionRepository,
    settingsRepository: SettingsRepository,
    categoryRepository: CategoryRepository,
) {
    val navController = rememberNavController()
    val showCharts by settingsRepository.getShowCharts().collectAsState(initial = true)
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
        val visibleNavItems = buildList {
            add(BottomNavItem.Home)
            if (showCharts) add(BottomNavItem.Charts)
            add(BottomNavItem.Transactions)
            add(BottomNavItem.Categories)
            add(BottomNavItem.Settings)
        }

        Scaffold(
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    visibleNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = stringResource(item.titleResId)) },
                            label = { Text(stringResource(item.titleResId)) },
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
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = BottomNavItem.Home.route,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(BottomNavItem.Home.route) {
                    HomeScreen(transactionRepository = transactionRepository)
                }
                composable(BottomNavItem.Charts.route) {
                    ChartsScreen(transactionRepository = transactionRepository)
                }
                composable(BottomNavItem.Transactions.route) {
                    TransactionsScreen(transactionRepository = transactionRepository)
                }
                composable(BottomNavItem.Categories.route) {
                    CategoriesScreen(categoryRepository = categoryRepository)
                }
                composable(BottomNavItem.Settings.route) {
                    SettingsScreen(
                        settingsRepository = settingsRepository,
                        transactionRepository = transactionRepository,
                        categoryRepository = categoryRepository,
                        navController = navController,
                    )
                }
                composable("display") {
                    DisplayScreen(
                        settingsRepository = settingsRepository,
                        navController = navController,
                    )
                }
            }
        }
    }
}
