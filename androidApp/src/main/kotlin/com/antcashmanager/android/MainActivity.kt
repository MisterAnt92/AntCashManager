package com.antcashmanager.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.antcashmanager.android.navigation.AntCashManagerNavHost
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.usecase.settings.GetThemeUseCase

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as AntCashManagerApp
        val getThemeUseCase = GetThemeUseCase(app.settingsRepository)

        setContent {
            val theme by getThemeUseCase().collectAsState(initial = AppTheme.SYSTEM)
            val isDark = when (theme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }
            AntCashManagerTheme(darkTheme = isDark) {
                AntCashManagerNavHost(
                    transactionRepository = app.transactionRepository,
                    settingsRepository = app.settingsRepository,
                    categoryRepository = app.categoryRepository,
                )
            }
        }
    }
}
