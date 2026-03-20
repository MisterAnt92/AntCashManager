package com.antcashmanager.android

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.antcashmanager.android.navigation.AntCashManagerNavHost
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.usecase.settings.GetLanguageUseCase
import com.antcashmanager.domain.usecase.settings.GetThemeUseCase
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as AntCashManagerApp
        val getThemeUseCase = GetThemeUseCase(app.settingsRepository)
        val getLanguageUseCase = GetLanguageUseCase(app.settingsRepository)

        setContent {
            val theme by getThemeUseCase().collectAsState(initial = AppTheme.SYSTEM)
            val language by getLanguageUseCase().collectAsState(initial = AppLanguage.SYSTEM)
            val highContrast by app.settingsRepository.getHighContrast()
                .collectAsState(initial = false)
            val largeText by app.settingsRepository.getLargeText()
                .collectAsState(initial = false)
            val isDark = when (theme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            WithAppLocale(language = language) {
                AntCashManagerTheme(
                    darkTheme = isDark,
                    highContrast = highContrast,
                    largeText = largeText,
                ) {
                    AntCashManagerNavHost(
                        transactionRepository = app.transactionRepository,
                        settingsRepository = app.settingsRepository,
                        categoryRepository = app.categoryRepository,
                    )
                }
            }
        }
    }
}

/**
 * Wraps content with a localized context so that [stringResource] calls
 * resolve to the correct language-specific strings.xml.
 */
@Composable
fun WithAppLocale(language: AppLanguage, content: @Composable () -> Unit) {
    if (language == AppLanguage.SYSTEM) {
        content()
    } else {
        val context = LocalContext.current
        val locale = Locale(language.code)
        val config = Configuration(context.resources.configuration).apply {
            setLocale(locale)
        }
        val localizedContext = context.createConfigurationContext(config)
        CompositionLocalProvider(LocalContext provides localizedContext) {
            content()
        }
    }
}
