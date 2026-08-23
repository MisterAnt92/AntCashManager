package com.antcashmanager.android.ui.screen.settings.displaySettings

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.ui.base.BaseViewModel
import com.antcashmanager.domain.model.None
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.service.NoOpWidgetUpdateNotifier
import com.antcashmanager.domain.service.WidgetUpdateNotifier
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


/**
 * ViewModel per la gestione delle preferenze di visualizzazione.
 * Espone lo stato tramite StateFlow e fornisce metodi per aggiornare le preferenze.
 * Tutti i valori di default sono centralizzati in costanti private.
 */
class DisplayViewModel(
    private val settingsRepository: SettingsRepository,
    private val widgetUpdateNotifier: WidgetUpdateNotifier = NoOpWidgetUpdateNotifier,
    private val analyticsManager: AnalyticsManager,
) : BaseViewModel<None>() {

    // Espone il simbolo valuta attuale
    val currencySymbol = settingsRepository.getCurrencySymbol()
        .map(::sanitizeCurrencySymbol)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT),
            DisplayConstant.DEFAULT_CURRENCY_SYMBOL,
        )

    // Espone il numero di cifre decimali
    val decimalDigits = settingsRepository.getDecimalDigits()
        .map(::sanitizeDecimalDigits)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT),
            DisplayConstant.DEFAULT_DECIMAL_DIGITS,
        )

    // Espone il separatore decimale
    val decimalSeparator = settingsRepository.getDecimalSeparator()
        .map(::sanitizeDecimalSeparator)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT),
            DisplayConstant.DEFAULT_DECIMAL_SEPARATOR,
        )

    // Espone il separatore delle migliaia
    val thousandsSeparator = combine(
        settingsRepository.getThousandsSeparator(),
        settingsRepository.getDecimalSeparator().map(::sanitizeDecimalSeparator),
    ) { thousands, decimal ->
        sanitizeThousandsSeparator(thousands, decimal)
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT),
            DisplayConstant.DEFAULT_THOUSANDS_SEPARATOR,
        )


    // Espone il valore del buono pasto
    val mealVoucherValue = settingsRepository.getMealVoucherValue()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT),
            DisplayConstant.DEFAULT_MEAL_VOUCHER_VALUE,
        )

    // Espone la preferenza per mostrare la sezione grafici
    val showChartsSection = settingsRepository.getShowCharts()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT),
            DisplayConstant.DEFAULT_SHOW_CHARTS_SECTION,
        )

    // Espone la preferenza per lo zoom nei grafici
    val chartsZoomEnabled = settingsRepository.getChartsZoomEnabled()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT),
            DisplayConstant.DEFAULT_SHOW_CHARTS_ZOOM,
        )

    // Espone il formato data
    val dateFormat = settingsRepository.getDateFormat()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT),
            DisplayConstant.DEFAULT_DATE_FORMAT,
        )

    // Espone la preferenza per mostrare le note delle transazioni
    val showTransactionNotes = settingsRepository.getShowTransactionNotes()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT),
            DisplayConstant.DEFAULT_SHOW_TRANSACTION_NOTES,
        )

    // Espone la preferenza per mascherare gli importi con asterischi
    val maskAmounts = settingsRepository.getMaskAmounts()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT),
            DisplayConstant.DEFAULT_MASK_AMOUNTS,
        )

    // Espone la preferenza per mostrare il breakdown dei pagamenti
    val showPaymentTypeBreakdown = settingsRepository.getShowPaymentTypeBreakdown()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT),
            DisplayConstant.DEFAULT_SHOW_PAYMENT_BREAKDOWN,
        )

    // Espone la preferenza per mostrare la card Insight rapidi
    val showQuickInsightsCard = settingsRepository.getShowQuickInsightsCard()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT),
            DisplayConstant.DEFAULT_SHOW_QUICK_INSIGHTS_CARD,
        )

    // Espone il tipo di pagamento predefinito
    val defaultPaymentType = settingsRepository.getDefaultPaymentType()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT),
            DisplayConstant.DEFAULT_PAYMENT_TYPE,
        )

    // Espone il tipo di visualizzazione delle transazioni (Home)
    val transactionDisplayType = settingsRepository.getTransactionDisplayType()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT),
            DisplayConstant.DEFAULT_TRANSACTION_DISPLAY_TYPE,
        )

    // Espone il tipo di visualizzazione delle transazioni (Transazioni)
    val transactionsTransactionDisplayType =
        settingsRepository.getTransactionsTransactionDisplayType()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT),
                DisplayConstant.DEFAULT_TRANSACTION_DISPLAY_TYPE,
            )

    val showInitialAnimation = settingsRepository.getShowInitialAnimation()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT),
            true,
        )

    // Espone il colore di sfondo dei widget della home screen
    val widgetBackgroundColor = settingsRepository.getWidgetBackgroundColor()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT),
            DisplayConstant.DEFAULT_WIDGET_BACKGROUND_COLOR,
        )

    // Espone l'opacità dei widget della home screen (0-100)
    val widgetOpacity = settingsRepository.getWidgetOpacity()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT),
            DisplayConstant.DEFAULT_WIDGET_OPACITY,
        )

    /**
     * Aggiorna il simbolo valuta.
     */
    fun setCurrencySymbol(symbol: String) = updatePreference(
        logMsg = "Setting currency symbol: $symbol",
        action = {
            settingsRepository.setCurrencySymbol(sanitizeCurrencySymbol(symbol))
        },
    )

    /**
     * Aggiorna il numero di cifre decimali.
     */
    fun setDecimalDigits(digits: Int) = updatePreference(
        logMsg = "Setting decimal digits: $digits",
        action = {
            settingsRepository.setDecimalDigits(sanitizeDecimalDigits(digits))
        },
    )

    /**
     * Aggiorna il separatore decimale.
     */
    fun setDecimalSeparator(separator: String) = updatePreference(
        logMsg = "Setting decimal separator: $separator",
        action = {
            val safeDecimal = sanitizeDecimalSeparator(separator)
            settingsRepository.setDecimalSeparator(safeDecimal)
            if (safeDecimal == thousandsSeparator.value) {
                settingsRepository.setThousandsSeparator(DisplayConstant.DEFAULT_THOUSANDS_SEPARATOR)
            }
        },
    )

    /**
     * Aggiorna il separatore delle migliaia.
     */
    fun setThousandsSeparator(separator: String) = updatePreference(
        logMsg = "Setting thousands separator: $separator",
        action = {
            val safeThousands = if (separator in DisplayConstant.SUPPORTED_THOUSANDS_SEPARATORS) {
                separator
            } else {
                DisplayConstant.DEFAULT_THOUSANDS_SEPARATOR
            }
            settingsRepository.setThousandsSeparator(safeThousands)
        },
    )


    /**
     * Aggiorna il valore del buono pasto.
     */
    fun setMealVoucherValue(value: Double) {
        // Track meal voucher details update
        analyticsManager.logEvent("meal_voucher_details_updated", Bundle().apply {
            putDouble("value", value)
        })
        updatePreference(
            logMsg = "Setting meal voucher value: $value",
            action = {
                settingsRepository.setMealVoucherValue(value.coerceAtLeast(0.0))
            },
        )
    }

    /**
     * Aggiorna la preferenza per la visualizzazione della sezione grafici.
     */
    fun setShowChartsSection(show: Boolean) = updatePreference(
        logMsg = "Setting show charts section: $show",
        action = { settingsRepository.setShowCharts(show) },
    )

    /**
     * Aggiorna la preferenza per lo zoom nei grafici.
     */
    fun setChartsZoomEnabled(enabled: Boolean) = updatePreference(
        logMsg = "Setting charts zoom enabled: $enabled",
        action = { settingsRepository.setChartsZoomEnabled(enabled) },
    )

    /**
     * Aggiorna il formato data.
     */
    fun setDateFormat(pattern: String) = updatePreference(
        logMsg = "Setting date format: $pattern",
        action = { settingsRepository.setDateFormat(pattern) },
    )

    /**
     * Aggiorna la preferenza per mostrare le note delle transazioni.
     */
    fun setShowTransactionNotes(show: Boolean) = updatePreference(
        logMsg = "Setting show transaction notes: $show",
        action = { settingsRepository.setShowTransactionNotes(show) },
    )

    /**
     * Aggiorna la preferenza per mascherare gli importi con asterischi.
     */
    fun setMaskAmounts(mask: Boolean) = updatePreference(
        logMsg = "Setting mask amounts: $mask",
        action = { settingsRepository.setMaskAmounts(mask) },
    )

    /**
     * Aggiorna la preferenza per mostrare il breakdown dei pagamenti.
     */
    fun setShowPaymentTypeBreakdown(show: Boolean) = updatePreference(
        logMsg = "Setting show payment type breakdown: $show",
        action = { settingsRepository.setShowPaymentTypeBreakdown(show) },
    )

    /**
     * Aggiorna la preferenza per mostrare la card Insight rapidi in Home.
     */
    fun setShowQuickInsightsCard(show: Boolean) = updatePreference(
        logMsg = "Setting show quick insights card: $show",
        action = { settingsRepository.setShowQuickInsightsCard(show) },
    )

    /**
     * Aggiorna il tipo di pagamento predefinito.
     */
    fun setDefaultPaymentType(paymentType: String) = updatePreference(
        logMsg = "Setting default payment type: $paymentType",
        action = { settingsRepository.setDefaultPaymentType(paymentType) },
    )

    /**
     * Aggiorna il tipo di visualizzazione delle transazioni (Home).
     */
    fun setTransactionDisplayType(displayType: TransactionDisplayType) = updatePreference(
        logMsg = "Setting home transaction display type: $displayType",
        action = { settingsRepository.setTransactionDisplayType(displayType) },
    )

    /**
     * Aggiorna il tipo di visualizzazione delle transazioni (Transazioni).
     */
    fun setTransactionsTransactionDisplayType(displayType: TransactionDisplayType) =
        updatePreference(
            logMsg = "Setting transactions transaction display type: $displayType",
            action = { settingsRepository.setTransactionsTransactionDisplayType(displayType) },
        )

    fun setShowInitialAnimation(show: Boolean) = updatePreference(
        logMsg = "Setting show initial animation: $show",
        action = { settingsRepository.setShowInitialAnimation(show) },
    )

    /**
     * Aggiorna il colore di sfondo dei widget e ne forza il refresh immediato.
     */
    fun setWidgetBackgroundColor(color: Long) = updatePreference(
        logMsg = "Setting widget background color: $color",
        action = {
            settingsRepository.setWidgetBackgroundColor(color)
            widgetUpdateNotifier.notifyTransactionsChanged()
        },
    )

    /**
     * Aggiorna l'opacità dei widget e ne forza il refresh immediato.
     */
    fun setWidgetOpacity(opacity: Int) = updatePreference(
        logMsg = "Setting widget opacity: $opacity",
        action = {
            settingsRepository.setWidgetOpacity(opacity.coerceIn(0, 100))
            widgetUpdateNotifier.notifyTransactionsChanged()
        },
    )

    /**
     * Ripristina tutte le preferenze ai valori di default.
     */
    fun resetAllPreferences() = updatePreference(
        logMsg = "Resetting all preferences",
        action = { settingsRepository.resetAllPreferences() },
    )

    /**
     * Funzione di utilità per loggare e lanciare l'azione in coroutine.
     */
    private fun updatePreference(logMsg: String, action: suspend () -> Unit) {
        logDebug(logMsg)
        viewModelScope.launch { action() }
    }

    private fun sanitizeCurrencySymbol(symbol: String): String =
        if (symbol in DisplayConstant.SUPPORTED_CURRENCY_SYMBOLS) {
            symbol
        } else {
            DisplayConstant.DEFAULT_CURRENCY_SYMBOL
        }

    private fun sanitizeDecimalDigits(digits: Int): Int = digits.coerceIn(0, 4)

    private fun sanitizeDecimalSeparator(separator: String): String =
        if (separator in DisplayConstant.SUPPORTED_DECIMAL_SEPARATORS) {
            separator
        } else {
            DisplayConstant.DEFAULT_DECIMAL_SEPARATOR
        }

    private fun sanitizeThousandsSeparator(thousands: String, decimal: String): String {
        val normalized = if (thousands in DisplayConstant.SUPPORTED_THOUSANDS_SEPARATORS) {
            thousands
        } else {
            DisplayConstant.DEFAULT_THOUSANDS_SEPARATOR
        }

        return if (normalized == decimal) {
            DisplayConstant.DEFAULT_THOUSANDS_SEPARATOR
        } else {
            normalized
        }
    }
}
