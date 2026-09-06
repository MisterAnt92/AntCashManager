package com.antcashmanager.android

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.window.layout.DisplayFeature
import androidx.window.layout.WindowInfoTracker
import co.touchlab.kermit.Logger
import com.antcashmanager.android.analytics.tracker.SessionTracker
import com.antcashmanager.android.navigation.AntCashManagerNavHost
import com.antcashmanager.android.ui.LocalLocale
import com.antcashmanager.android.ui.base.LocalMultiPaneCoordinator
import com.antcashmanager.android.ui.base.MultiPaneCoordinator
import com.antcashmanager.android.ui.components.layout.LocalDisplayFeatures
import com.antcashmanager.android.ui.theme.AppThemeProvider
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.usecase.settings.GetLanguageUseCase
import com.antcashmanager.domain.usecase.settings.GetThemeUseCase
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val getLanguageUseCase: GetLanguageUseCase by inject()
    private val getThemeUseCase: GetThemeUseCase by inject()
    private val sessionTracker: SessionTracker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        // installSplashScreen() must be called BEFORE super.onCreate() / setContent.
        // setKeepOnScreenCondition keeps the OS splash visible until DataStore settings
        // are pre-loaded; avoids the blank white window between cold start and first frame.
        var settingsLoaded = false
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !settingsLoaded }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Logger.d(tag = "MainActivity") { "onCreate: initializing app with settings" }

        // Track app launch event
        val appVersion =
            try {
                packageManager.getPackageInfo(packageName, 0).versionName ?: "unknown"
            } catch (e: Exception) {
                "unknown"
            }
        sessionTracker.onAppLaunched(appVersion, isFirstLaunch = false)

        // Pre-load settings from DataStore to ensure they're available immediately
        lifecycleScope.launch {
            try {
                Logger.d(tag = "MainActivity") { "Pre-loading theme and language from DataStore..." }
                getLanguageUseCase().first() // Force load from DataStore
                getThemeUseCase().first() // Force load from DataStore
                Logger.d(tag = "MainActivity") { "Pre-load complete, launching Compose UI" }
            } catch (e: Exception) {
                Logger.e(tag = "MainActivity", throwable = e) { "Error pre-loading settings" }
            } finally {
                // Release the OS splash screen — Compose SplashScreen in NavGraph takes over.
                settingsLoaded = true
            }

            // Now setContent with data already loaded
            setContent {
                // Combine language + theme in one state for synchronized updates
                val settingsState by combine(
                    getLanguageUseCase(),
                    getThemeUseCase(),
                ) { langResult, themeResult ->
                    val language = langResult.getOrElse { AppLanguage.SYSTEM }
                    val theme = themeResult.getOrElse { AppTheme.SYSTEM }
                    SettingsSnapshot(language, theme)
                }.collectAsStateWithLifecycle(
                    initialValue = SettingsSnapshot(AppLanguage.SYSTEM, AppTheme.SYSTEM),
                )

                Logger.d(tag = "MainActivity") {
                    "Settings loaded: language=${settingsState.language}, theme=${settingsState.theme}"
                }

                // Track display features (folds, hinges) for foldable device support
                var displayFeatures by remember { mutableStateOf<List<DisplayFeature>>(emptyList()) }
                LaunchedEffect(Unit) {
                    val windowInfoTracker = WindowInfoTracker.getOrCreate(this@MainActivity)
                    windowInfoTracker.windowLayoutInfo(this@MainActivity).collect { layoutInfo ->
                        displayFeatures = layoutInfo.displayFeatures
                        Logger.d(tag = "MainActivity") {
                            "Display features updated: ${displayFeatures.size} feature(s)"
                        }
                    }
                }

                // Create multi-pane coordinator for synchronized state across panes
                val multiPaneCoordinator = remember { MultiPaneCoordinator() }

                WithAppLocale(language = settingsState.language) {
                    AppThemeProvider(currentTheme = settingsState.theme) {
                        WithDisplayFeatures(displayFeatures = displayFeatures) {
                            WithMultiPaneCoordinator(coordinator = multiPaneCoordinator) {
                                AntCashManagerNavHost()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Logger.d(tag = "MainActivity") { "onResume: session started" }
        sessionTracker.onSessionStarted()
    }

    override fun onPause() {
        super.onPause()
        Logger.d(tag = "MainActivity") { "onPause: session ended" }
        sessionTracker.onSessionEnded()
    }
}

data class SettingsSnapshot(
    val language: AppLanguage,
    val theme: AppTheme,
)

@Composable
fun WithAppLocale(
    language: AppLanguage,
    content: @Composable () -> Unit,
) {
    if (language == AppLanguage.SYSTEM) {
        Logger.d(tag = "Language") { "WithAppLocale: using SYSTEM locale" }
        val systemLocale = Locale.getDefault()
        CompositionLocalProvider(LocalLocale provides systemLocale) {
            content()
        }
    } else {
        val context = LocalContext.current
        val activityResultRegistryOwner = LocalActivityResultRegistryOwner.current
        val locale = Locale.forLanguageTag(language.code)
        Logger.d(tag = "Language") { "WithAppLocale: applying locale ${language.code} (${locale.displayName})" }
        val config =
            Configuration(LocalConfiguration.current).apply {
                setLocale(locale)
            }
        val localizedContext = context.createConfigurationContext(config)
        if (activityResultRegistryOwner != null) {
            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalActivityResultRegistryOwner provides activityResultRegistryOwner,
                LocalLocale provides locale, // Expose locale globally
            ) {
                content()
            }
        } else {
            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalLocale provides locale, // Expose locale globally
            ) {
                content()
            }
        }
    }
}

/**
 * Provides display features (folds, hinges) to all composables below.
 * Used for foldable device support (Samsung Galaxy Z Fold, Z Flip, etc.).
 *
 * @param displayFeatures List of display features detected on this device
 * @param content Composable content that can access display features via LocalDisplayFeatures
 */
@Composable
fun WithDisplayFeatures(
    displayFeatures: List<DisplayFeature>,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalDisplayFeatures provides displayFeatures) {
        content()
    }
}

/**
 * Provides multi-pane coordinator to all composables below.
 * Used for synchronizing state between panes on foldable devices or tablets in split-view.
 *
 * @param coordinator MultiPaneCoordinator instance managing pane synchronization
 * @param content Composable content that can access coordinator via LocalMultiPaneCoordinator
 */
@Composable
fun WithMultiPaneCoordinator(
    coordinator: MultiPaneCoordinator,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalMultiPaneCoordinator provides coordinator) {
        content()
    }
}
