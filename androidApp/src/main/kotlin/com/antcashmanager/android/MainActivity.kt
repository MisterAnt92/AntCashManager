package com.antcashmanager.android

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.antcashmanager.android.navigation.AntCashManagerNavHost
import com.antcashmanager.android.ui.theme.AppThemeProvider
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.usecase.settings.GetLanguageUseCase
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as AntCashManagerApp
        val getLanguageUseCase = GetLanguageUseCase(app.settingsRepository)

        setContent {
            val languageResult by getLanguageUseCase().collectAsState(
                initial = Result.success(
                    AppLanguage.SYSTEM
                )
            )
            val language = languageResult.getOrElse { AppLanguage.SYSTEM }

            WithAppLocale(language = language) {
                // Centralized theme provider reads theme & accessibility preferences
                AppThemeProvider(settingsRepository = app.settingsRepository) {
                    AntCashManagerNavHost(
                        analyticsManager = app.analyticsManager,
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
