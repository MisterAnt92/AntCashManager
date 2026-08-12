package com.antcashmanager.android.ui.widget

import androidx.compose.ui.graphics.Color
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.CurrencyFormat
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import org.koin.core.context.GlobalContext

/**
 * I widget Glance sono istanziati dal sistema, non da Koin: recuperano le dipendenze dal
 * container globale già avviato in `AntCashManagerApp`.
 */
internal object WidgetDependencies {
    val transactionRepository: TransactionRepository
        get() = safeGet()

    val settingsRepository: SettingsRepository
        get() = safeGet()

    val analyticsManager: AnalyticsManager
        get() = safeGet()

    private inline fun <reified T : Any> safeGet(): T {
        return try {
            GlobalContext.get().get()
        } catch (e: Exception) {
            // Se Koin non è inizializzato (raro in questo processo ma possibile in race conditions)
            // rilanciamo un'eccezione specifica che cattureremo nel widget per mostrare uno stato di errore
            // invece di far crashare l'intero provider del launcher.
            throw IllegalStateException("Koin not initialized or dependency not found: ${T::class.simpleName}", e)
        }
    }
}

internal suspend fun loadLanguage(settingsRepository: SettingsRepository): AppLanguage =
    settingsRepository.getLanguage().first()

internal suspend fun loadCurrencyFormat(settingsRepository: SettingsRepository): CurrencyFormat =
    CurrencyFormat(
        currencySymbol = settingsRepository.getCurrencySymbol().first(),
        decimalDigits = settingsRepository.getDecimalDigits().first(),
        decimalSeparator = settingsRepository.getDecimalSeparator().first(),
        thousandsSeparator = settingsRepository.getThousandsSeparator().first(),
    )

/**
 * Palette risolta per il rendering dei widget, calcolata dallo sfondo/opacità scelti
 * dall'utente. Il testo si adatta alla luminanza dello sfondo per restare leggibile.
 */
internal data class WidgetPalette(
    val background: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val trackBackground: Color,
)

internal suspend fun loadWidgetPalette(settingsRepository: SettingsRepository): WidgetPalette {
    val colorLong = settingsRepository.getWidgetBackgroundColor().first()
    val opacity = settingsRepository.getWidgetOpacity().first()
    val background = Color(colorLong).copy(alpha = opacity / 100f)

    val luminance = 0.299f * background.red + 0.587f * background.green + 0.114f * background.blue
    val isDark = luminance < 0.5f

    return WidgetPalette(
        background = background,
        primaryText = if (isDark) Color(0xFFFFFFFF) else Color(0xFF212121),
        secondaryText = if (isDark) Color(0xFFE0E0E0) else Color(0xFF757575),
        trackBackground = if (isDark) Color(0xFF4A4A4A) else Color(0xFFE0E0E0),
    )
}
