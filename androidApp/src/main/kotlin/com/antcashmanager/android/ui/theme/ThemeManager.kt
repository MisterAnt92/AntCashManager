package com.antcashmanager.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import co.touchlab.kermit.Logger
import com.antcashmanager.domain.model.AppTheme
import org.koin.androidx.compose.koinViewModel

/**
 * CompositionLocal che espone il ThemeViewModel (opzionale) per chi vuole
 * eseguire operazioni legate al tema senza dover ricreare il ViewModel.
 */
val LocalThemeViewModel = staticCompositionLocalOf<ThemeViewModel?> { null }

/**
 * Provider composable che centralizza la gestione del tema.
 * Il [ThemeViewModel] viene ottenuto tramite Koin (nessun parametro repository necessario).
 *
 * Usage: AppThemeProvider { AppContent() }
 */
@Composable
fun AppThemeProvider(
    dynamicColor: Boolean = true,
    currentTheme: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit,
) {
    val viewModel: ThemeViewModel = koinViewModel()

    val appTheme by viewModel.appTheme.collectAsState(initial = currentTheme)
    val highContrast by viewModel.highContrast.collectAsState(initial = false)
    val largeText by viewModel.largeText.collectAsState(initial = false)
    val reduceMotion by viewModel.reduceMotion.collectAsState(initial = false)

    val darkTheme = when (appTheme) {
        AppTheme.DARK -> {
            Logger.d(tag = "Theme") { "AppThemeProvider applying: DARK" }
            true
        }

        AppTheme.LIGHT -> {
            Logger.d(tag = "Theme") { "AppThemeProvider applying: LIGHT" }
            false
        }

        AppTheme.SYSTEM -> {
            val isDark = isSystemInDarkTheme()
            Logger.d(tag = "Theme") { "AppThemeProvider applying: SYSTEM (isDark=$isDark)" }
            isDark
        }
    }

    CompositionLocalProvider(LocalThemeViewModel provides viewModel) {
        AntCashManagerTheme(
            darkTheme = darkTheme,
            dynamicColor = dynamicColor,
            highContrast = highContrast,
            largeText = largeText,
            reduceMotion = reduceMotion,
        ) {
            content()
        }
    }
}
