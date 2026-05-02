package com.antcashmanager.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.repository.SettingsRepository

/**
 * CompositionLocal che espone il ThemeViewModel (opzionale) per chi vuole
 * eseguire operazioni legate al tema senza dover ricreare il ViewModel.
 */
val LocalThemeViewModel = staticCompositionLocalOf<ThemeViewModel?> { null }

/**
 * Provider composable che centralizza la gestione del tema.
 * Avvolge l'app con `AntCashManagerTheme` usando le preferenze lette dal
 * `SettingsRepository` (via `ThemeViewModel`).
 *
 * Usage:
 * AppThemeProvider(settingsRepository = repo) { AppContent() }
 */
@Composable
fun AppThemeProvider(
    settingsRepository: SettingsRepository,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val viewModel: ThemeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ThemeViewModel(settingsRepository) as T
        }
    )

    val appTheme by viewModel.appTheme.collectAsState(initial = AppTheme.SYSTEM)
    val highContrast by viewModel.highContrast.collectAsState(initial = false)
    val largeText by viewModel.largeText.collectAsState(initial = false)
    val reduceMotion by viewModel.reduceMotion.collectAsState(initial = false)

    val darkTheme = when (appTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        else -> isSystemInDarkTheme()
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

