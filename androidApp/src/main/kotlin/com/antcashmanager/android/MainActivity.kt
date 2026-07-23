package com.antcashmanager.android

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import co.touchlab.kermit.Logger
import com.antcashmanager.android.navigation.AntCashManagerNavHost
import com.antcashmanager.android.ui.LocalLocale
import com.antcashmanager.android.ui.theme.AppThemeProvider
import com.antcashmanager.android.ui.theme.LocalThemeViewModel
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Logger.d(tag = "MainActivity") { "onCreate: initializing app with settings" }

        // Pre-load settings from DataStore to ensure they're available immediately
        lifecycleScope.launch {
            try {
                Logger.d(tag = "MainActivity") { "Pre-loading theme and language from DataStore..." }
                getLanguageUseCase().first()  // Force load from DataStore
                getThemeUseCase().first()     // Force load from DataStore
                Logger.d(tag = "MainActivity") { "Pre-load complete, launching Compose UI" }
            } catch (e: Exception) {
                Logger.e(tag = "MainActivity", throwable = e) { "Error pre-loading settings" }
            }

            // Now setContent with data already loaded
            setContent {
                // Combine language + theme in one state for synchronized updates
                val settingsState by combine(
                    getLanguageUseCase(),
                    getThemeUseCase()
                ) { langResult, themeResult ->
                    val language = langResult.getOrElse { AppLanguage.SYSTEM }
                    val theme = themeResult.getOrElse { AppTheme.SYSTEM }
                    SettingsSnapshot(language, theme)
                }.collectAsState(
                    initial = SettingsSnapshot(AppLanguage.SYSTEM, AppTheme.SYSTEM)
                )

                Logger.d(tag = "MainActivity") {
                    "Settings loaded: language=${settingsState.language}, theme=${settingsState.theme}"
                }

                WithAppLocale(language = settingsState.language) {
                    AppThemeProvider(currentTheme = settingsState.theme) {
                        AntCashManagerNavHost()
                    }
                }
            }
        }
    }
}

data class SettingsSnapshot(
    val language: AppLanguage,
    val theme: AppTheme,
)

@Composable
fun WithAppLocale(language: AppLanguage, content: @Composable () -> Unit) {
    if (language == AppLanguage.SYSTEM) {
        Logger.d(tag = "Language") { "WithAppLocale: using SYSTEM locale" }
        val systemLocale = Locale.getDefault()
        CompositionLocalProvider(LocalLocale provides systemLocale) {
            content()
        }
    } else {
        val context = LocalContext.current
        val activityResultRegistryOwner = LocalActivityResultRegistryOwner.current
        val locale = Locale(language.code)
        Logger.d(tag = "Language") { "WithAppLocale: applying locale ${language.code} (${locale.displayName})" }
        val config = Configuration(LocalConfiguration.current).apply {
            setLocale(locale)
        }
        val localizedContext = context.createConfigurationContext(config)
        if (activityResultRegistryOwner != null) {
            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalActivityResultRegistryOwner provides activityResultRegistryOwner,
                LocalLocale provides locale,  // Expose locale globally
            ) {
                content()
            }
        } else {
            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalLocale provides locale,  // Expose locale globally
            ) {
                content()
            }
        }
    }
}
