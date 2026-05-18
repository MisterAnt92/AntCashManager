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
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val getLanguageUseCase: GetLanguageUseCase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val languageResult by getLanguageUseCase().collectAsState(
                initial = Result.success(AppLanguage.SYSTEM)
            )
            val language = languageResult.getOrElse { AppLanguage.SYSTEM }

            WithAppLocale(language = language) {
                AppThemeProvider {
                    AntCashManagerNavHost()
                }
            }
        }
    }
}

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
