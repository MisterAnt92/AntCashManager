package com.antcashmanager.android.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.ui.components.AntScreenScaffold
import com.antcashmanager.android.ui.screen.categories.CategoriesScreen
import com.antcashmanager.android.ui.screen.charts.ChartsScreen
import com.antcashmanager.android.ui.screen.home.HomeScreen
import com.antcashmanager.android.ui.screen.receiptScan.ReceiptScanScreen
import com.antcashmanager.android.ui.screen.settings.SettingsScreen
import com.antcashmanager.android.ui.screen.settingsData.SettingsDataScreen
import com.antcashmanager.android.ui.screen.settingsDisplay.DisplayScreen
import com.antcashmanager.android.ui.screen.transactionAdd.AddTransactionScreen
import com.antcashmanager.android.ui.screen.transactions.TransactionsScreen
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.util.LocalCurrencyFormat
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.CurrencyFormat
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.flowOf

@Composable
fun AntCashManagerNavHost(
    analyticsManager: AnalyticsManager,
    transactionRepository: TransactionRepository,
    settingsRepository: SettingsRepository,
    categoryRepository: CategoryRepository,
) {
    val navController = rememberNavController()
    val showCharts by settingsRepository.getShowCharts().collectAsState(initial = true)
    val isTutorialCompleted by settingsRepository.getIsTutorialCompleted().collectAsState(initial = true)
    val currencySymbol by settingsRepository.getCurrencySymbol().collectAsState(initial = "\u20ac")
    val decimalDigits by settingsRepository.getDecimalDigits().collectAsState(initial = 2)
    val decimalSeparator by settingsRepository.getDecimalSeparator().collectAsState(initial = ",")
    val thousandsSeparator by settingsRepository.getThousandsSeparator()
        .collectAsState(initial = ".")

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

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        LaunchedEffect(currentDestination?.route) {
            currentDestination?.route?.let(analyticsManager::logScreenView)
        }

        AntScreenScaffold(
            showTopBar = false, // Top bar nascosta sugli screen principali
            bottomBar = {
                if (isTutorialCompleted) {
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
                                        Text(
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
            NavHost(
                navController = navController,
                startDestination = BottomNavItem.Home.route,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(BottomNavItem.Home.route) {
                    HomeScreen(
                        transactionRepository = transactionRepository,
                        settingsRepository = settingsRepository,
                        categoryRepository = categoryRepository,
                    )
                }
                composable(BottomNavItem.Charts.route) {
                    ChartsScreen(
                        transactionRepository = transactionRepository,
                        settingsRepository = settingsRepository,
                    )
                }
                composable(BottomNavItem.Transactions.route) {
                    TransactionsScreen(
                        transactionRepository = transactionRepository,
                        categoryRepository = categoryRepository,
                        settingsRepository = settingsRepository,
                        navController = navController,
                    )
                }
                composable(BottomNavItem.Categories.route) {
                    CategoriesScreen(categoryRepository = categoryRepository)
                }
                composable(BottomNavItem.Settings.route) {
                    SettingsScreen(
                        settingsRepository = settingsRepository,
                        transactionRepository = transactionRepository,
                        navController = navController,
                    )
                }
                composable("display") {
                    DisplayScreen(
                        settingsRepository = settingsRepository,
                        navController = navController,
                    )
                }
                composable("settings_data") {
                    SettingsDataScreen(
                        settingsRepository = settingsRepository,
                        transactionRepository = transactionRepository,
                        categoryRepository = categoryRepository,
                        navController = navController,
                    )
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
                        transactionRepository = transactionRepository,
                        categoryRepository = categoryRepository,
                        transactionId = transactionId,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onTransactionAdded = {
                            navController.popBackStack()
                        },
                    )
                }
                composable("receipt_scan") {
                    ReceiptScanScreen(
                        transactionRepository = transactionRepository,
                        categoryRepository = categoryRepository,
                        onNavigateBack = { navController.popBackStack() },
                        onTransactionSaved = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// PREVIEWS
// ══════════════════════════════════════════════════════════════════════════════

@Preview(showBackground = true)
@Composable
private fun AntCashManagerNavHostPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        AntCashManagerNavHost(
            analyticsManager = PreviewAnalyticsManager(),
            transactionRepository = PreviewTransactionRepository(),
            settingsRepository = PreviewSettingsRepository(),
            categoryRepository = PreviewCategoryRepository(),
        )
    }
}

private class PreviewAnalyticsManager : AnalyticsManager()

private class PreviewSettingsRepository : SettingsRepository {
    override fun getTheme() = flowOf(AppTheme.SYSTEM)
    override suspend fun setTheme(theme: AppTheme) {}
    override fun getLanguage() = flowOf(AppLanguage.SYSTEM)
    override suspend fun setLanguage(language: AppLanguage) {}
    override fun getShowCharts() = flowOf(true)
    override suspend fun setShowCharts(show: Boolean) {}
    override fun getHighContrast() = flowOf(false)
    override suspend fun setHighContrast(enabled: Boolean) {}
    override fun getLargeText() = flowOf(false)
    override suspend fun setLargeText(enabled: Boolean) {}
    override fun getReduceMotion() = flowOf(false)
    override suspend fun setReduceMotion(enabled: Boolean) {}
    override fun getShowTransactionNotes() = flowOf(true)
    override suspend fun setShowTransactionNotes(show: Boolean) {}
    override fun getCurrencySymbol() = flowOf("€")
    override suspend fun setCurrencySymbol(symbol: String) {}
    override fun getDecimalDigits() = flowOf(2)
    override suspend fun setDecimalDigits(digits: Int) {}
    override fun getDecimalSeparator() = flowOf(",")
    override suspend fun setDecimalSeparator(separator: String) {}
    override fun getThousandsSeparator() = flowOf(".")
    override suspend fun setThousandsSeparator(separator: String) {}
    override fun getDateFormat() = flowOf("dd/MM/yyyy")
    override suspend fun setDateFormat(pattern: String) {}
    override fun getDateFilterExpanded() = flowOf(true)
    override suspend fun setDateFilterExpanded(expanded: Boolean) {}
    override fun getHomeDateFilterPreset() = flowOf(1)
    override suspend fun setHomeDateFilterPreset(index: Int) {}
    override fun getHomeDateFilterState() = flowOf(
        SavedDateFilter(
            presetIndex = 1,
            from = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000),
            to = System.currentTimeMillis(),
        ),
    )

    override suspend fun setHomeDateFilterState(filter: SavedDateFilter) {}
    override fun getTransactionsDateFilterPreset() = flowOf(1)
    override suspend fun setTransactionsDateFilterPreset(index: Int) {}
    override fun getTransactionsDateFilterState() = flowOf(
        SavedDateFilter(
            presetIndex = 1,
            from = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000),
            to = System.currentTimeMillis(),
        ),
    )

    override suspend fun setTransactionsDateFilterState(filter: SavedDateFilter) {}
    override fun getChartsDateFilterPreset() = flowOf(1)
    override suspend fun setChartsDateFilterPreset(index: Int) {}
    override fun getChartsDateFilterState() = flowOf(
        SavedDateFilter(
            presetIndex = 1,
            from = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000),
            to = System.currentTimeMillis(),
        ),
    )

    override suspend fun setChartsDateFilterState(filter: SavedDateFilter) {}
    override fun getChartsZoomEnabled() = flowOf(true)
    override suspend fun setChartsZoomEnabled(enabled: Boolean) {}
    override fun getShowPaymentTypeBreakdown() = flowOf(true)
    override suspend fun setShowPaymentTypeBreakdown(show: Boolean) {}
    override fun getTransactionDisplayType() = flowOf(TransactionDisplayType.TREND)
    override suspend fun setTransactionDisplayType(displayType: TransactionDisplayType) {}
    override fun getTransactionsTransactionDisplayType() = flowOf(TransactionDisplayType.TREND)
    override suspend fun setTransactionsTransactionDisplayType(displayType: TransactionDisplayType) {}
    override fun getIsTutorialCompleted() = flowOf(true)
    override suspend fun setIsTutorialCompleted(completed: Boolean) {}
    override fun getDataEncryptionEnabled() = flowOf(false)
    override suspend fun setDataEncryptionEnabled(enabled: Boolean) {}
    override suspend fun resetAllPreferences() {}
}

private class PreviewTransactionRepository : TransactionRepository {
    override fun getAllTransactions() = flowOf(emptyList<Transaction>())
    override suspend fun getTransactionById(id: Long) = null
    override suspend fun insertTransaction(transaction: Transaction) = 0L
    override suspend fun updateTransaction(transaction: Transaction) {}
    override suspend fun deleteTransaction(transaction: Transaction) {}
    override suspend fun deleteAllTransactions() {}
    override fun getTransactionsByDateRange(from: Long, to: Long) = flowOf(emptyList<Transaction>())
    override fun getRecurringTransactions() = flowOf(emptyList<Transaction>())
    override suspend fun updateCategoryData(categoryName: String, icon: String, color: Long) {}
    override fun getDistinctTitles() = flowOf(emptyList<String>())
    override fun getDistinctPayees() = flowOf(emptyList<String>())
    override fun getDistinctNotes() = flowOf(emptyList<String>())
    override fun getDistinctLocations() = flowOf(emptyList<String>())
    override fun getDistinctTags() = flowOf(emptyList<String>())
}

private class PreviewCategoryRepository : CategoryRepository {
    override fun getAllCategories() = flowOf(emptyList<Category>())
    override suspend fun getCategoryById(id: Long) = null
    override suspend fun getCategoryByName(name: String) = null
    override suspend fun insertCategory(category: Category) = 0L
    override suspend fun updateCategory(category: Category) {}
    override suspend fun deleteCategory(category: Category) {}
    override suspend fun deleteAllCategories() {}
    override fun getCategoriesByType(type: String) = flowOf(emptyList<Category>())
    override suspend fun getDefaultCategoryCount() = 0
}
