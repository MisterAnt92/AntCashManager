package com.antcashmanager.android.ui.screen.settings.displaySettings

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.antcashmanager.android.analytics.AnalyticsManager
import com.antcashmanager.android.analytics.tracker.EngagementTracker
import com.antcashmanager.android.ui.base.BaseViewModel
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.service.NoOpWidgetUpdateNotifier
import com.antcashmanager.domain.service.WidgetUpdateNotifier
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    private val engagementTracker: EngagementTracker,
) : BaseViewModel<DisplayEvent>() {
    private var settingsModifiedCount = 0

    // Split into minimal 2-flow combines to avoid Kotlin 2.0 type inference issues
    private val thousandsSeparatorFlow: kotlinx.coroutines.flow.Flow<String> =
        combine(
            settingsRepository.getThousandsSeparator(),
            settingsRepository.getDecimalSeparator().map(::sanitizeDecimalSeparator),
        ) { thousands, decimal -> sanitizeThousandsSeparator(thousands, decimal) }

    private val currencyAndDigitsFlow: kotlinx.coroutines.flow.Flow<Pair<String, Int>> =
        combine(
            settingsRepository.getCurrencySymbol().map(::sanitizeCurrencySymbol),
            settingsRepository.getDecimalDigits().map(::sanitizeDecimalDigits),
        ) { currency, digits -> Pair(currency, digits) }

    private val separatorsFlow: kotlinx.coroutines.flow.Flow<Pair<String, String>> =
        combine(
            settingsRepository.getDecimalSeparator().map(::sanitizeDecimalSeparator),
            thousandsSeparatorFlow,
        ) { decimal, thousands -> Pair(decimal, thousands) }

    /**
     * UDF Pattern: Consolidated state for Display Settings screen.
     * Combines all 16 flow using minimal 2-flow combines to avoid Kotlin type inference issues.
     */
    val state: StateFlow<DisplayState> =

        kotlinx.coroutines.flow.combine(
            currencyAndDigitsFlow,
            separatorsFlow,
            settingsRepository.getMealVoucherValue(),
            settingsRepository.getShowCharts(),
            settingsRepository.getChartsZoomEnabled(),
            settingsRepository.getDateFormat(),
            settingsRepository.getShowTransactionNotes(),
            settingsRepository.getMaskAmounts(),
            settingsRepository.getShowPaymentTypeBreakdown(),
            settingsRepository.getShowQuickInsightsCard(),
            settingsRepository.getDefaultPaymentType(),
            settingsRepository.getTransactionDisplayType(),
            settingsRepository.getTransactionsTransactionDisplayType(),
            settingsRepository.getWidgetBackgroundColor(),
            settingsRepository.getWidgetOpacity(),
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            val values = args as Array<Any?>
            @Suppress("UNCHECKED_CAST")
            val currencyDigits = values[0] as Pair<String, Int>
            @Suppress("UNCHECKED_CAST")
            val separators = values[1] as Pair<String, String>
            DisplayState(
                currencySymbol = currencyDigits.first,
                decimalDigits = currencyDigits.second,
                decimalSeparator = separators.first,
                thousandsSeparator = separators.second,
                mealVoucherValue = values[2] as Double,
                showChartsSection = values[3] as Boolean,
                chartsZoomEnabled = values[4] as Boolean,
                dateFormat = values[5] as String,
                showTransactionNotes = values[6] as Boolean,
                maskAmounts = values[7] as Boolean,
                showPaymentTypeBreakdown = values[8] as Boolean,
                showQuickInsightsCard = values[9] as Boolean,
                defaultPaymentType = values[10] as String,
                transactionDisplayType = values[11] as TransactionDisplayType,
                transactionsTransactionDisplayType = values[12] as TransactionDisplayType,
                widgetBackgroundColor = values[13] as Long,
                widgetOpacity = values[14] as Int,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT),
            DisplayState(),
        )

    // Backward-compatibility accessors for UI layer that expects individual StateFlow
    // These read from the consolidated state but present the same interface as before
    val currencySymbol: StateFlow<String> get() = state.map { it.currencySymbol }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT), DisplayConstant.DEFAULT_CURRENCY_SYMBOL)
    val decimalDigits: StateFlow<Int> get() = state.map { it.decimalDigits }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT), DisplayConstant.DEFAULT_DECIMAL_DIGITS)
    val decimalSeparator: StateFlow<String> get() = state.map { it.decimalSeparator }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT), DisplayConstant.DEFAULT_DECIMAL_SEPARATOR)
    val thousandsSeparator: StateFlow<String> get() = state.map { it.thousandsSeparator }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT), DisplayConstant.DEFAULT_THOUSANDS_SEPARATOR)
    val mealVoucherValue: StateFlow<Double> get() = state.map { it.mealVoucherValue }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT), DisplayConstant.DEFAULT_MEAL_VOUCHER_VALUE)
    val showChartsSection: StateFlow<Boolean> get() = state.map { it.showChartsSection }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT), DisplayConstant.DEFAULT_SHOW_CHARTS_SECTION)
    val chartsZoomEnabled: StateFlow<Boolean> get() = state.map { it.chartsZoomEnabled }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT), DisplayConstant.DEFAULT_SHOW_CHARTS_ZOOM)
    val dateFormat: StateFlow<String> get() = state.map { it.dateFormat }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT), DisplayConstant.DEFAULT_DATE_FORMAT)
    val showTransactionNotes: StateFlow<Boolean> get() = state.map { it.showTransactionNotes }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT), DisplayConstant.DEFAULT_SHOW_TRANSACTION_NOTES)
    val maskAmounts: StateFlow<Boolean> get() = state.map { it.maskAmounts }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT), DisplayConstant.DEFAULT_MASK_AMOUNTS)
    val showPaymentTypeBreakdown: StateFlow<Boolean> get() = state.map { it.showPaymentTypeBreakdown }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT), DisplayConstant.DEFAULT_SHOW_PAYMENT_BREAKDOWN)
    val showQuickInsightsCard: StateFlow<Boolean> get() = state.map { it.showQuickInsightsCard }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT), DisplayConstant.DEFAULT_SHOW_QUICK_INSIGHTS_CARD)
    val defaultPaymentType: StateFlow<String> get() = state.map { it.defaultPaymentType }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT), DisplayConstant.DEFAULT_PAYMENT_TYPE)
    val transactionDisplayType: StateFlow<TransactionDisplayType> get() = state.map { it.transactionDisplayType }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT), DisplayConstant.DEFAULT_TRANSACTION_DISPLAY_TYPE)
    val transactionsTransactionDisplayType: StateFlow<TransactionDisplayType> get() = state.map { it.transactionsTransactionDisplayType }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT), DisplayConstant.DEFAULT_TRANSACTION_DISPLAY_TYPE)
    val widgetBackgroundColor: StateFlow<Long> get() = state.map { it.widgetBackgroundColor }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT), DisplayConstant.DEFAULT_WIDGET_BACKGROUND_COLOR)
    val widgetOpacity: StateFlow<Int> get() = state.map { it.widgetOpacity }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(DisplayConstant.SHARING_TIMEOUT), DisplayConstant.DEFAULT_WIDGET_OPACITY)

    override fun onEvent(event: DisplayEvent) {
        logDebug("Event: $event")
        when (event) {
            is DisplayEvent.SetCurrencySymbol -> setCurrencySymbol(event.symbol)
            is DisplayEvent.SetDecimalDigits -> setDecimalDigits(event.digits)
            is DisplayEvent.SetDecimalSeparator -> setDecimalSeparator(event.separator)
            is DisplayEvent.SetThousandsSeparator -> setThousandsSeparator(event.separator)
            is DisplayEvent.SetMealVoucherValue -> setMealVoucherValue(event.value)
            is DisplayEvent.SetShowChartsSection -> setShowChartsSection(event.show)
            is DisplayEvent.SetChartsZoomEnabled -> setChartsZoomEnabled(event.enabled)
            is DisplayEvent.SetDateFormat -> setDateFormat(event.pattern)
            is DisplayEvent.SetShowTransactionNotes -> setShowTransactionNotes(event.show)
            is DisplayEvent.SetMaskAmounts -> setMaskAmounts(event.mask)
            is DisplayEvent.SetShowPaymentTypeBreakdown -> setShowPaymentTypeBreakdown(event.show)
            is DisplayEvent.SetShowQuickInsightsCard -> setShowQuickInsightsCard(event.show)
            is DisplayEvent.SetDefaultPaymentType -> setDefaultPaymentType(event.paymentType)
            is DisplayEvent.SetTransactionDisplayType -> setTransactionDisplayType(event.displayType)
            is DisplayEvent.SetTransactionsTransactionDisplayType ->
                setTransactionsTransactionDisplayType(
                    event.displayType,
                )
            is DisplayEvent.SetWidgetBackgroundColor -> setWidgetBackgroundColor(event.color)
            is DisplayEvent.SetWidgetOpacity -> setWidgetOpacity(event.opacity)
            is DisplayEvent.RetryLastOperation -> logInfo("Retry requested")
        }
    }

    /**
     * Aggiorna il simbolo valuta.
     */
    private fun setCurrencySymbol(symbol: String) =
        updatePreference(
            logMsg = "Setting currency symbol: $symbol",
            action = {
                settingsRepository.setCurrencySymbol(sanitizeCurrencySymbol(symbol))
            },
        )

    /**
     * Aggiorna il numero di cifre decimali.
     */
    private fun setDecimalDigits(digits: Int) =
        updatePreference(
            logMsg = "Setting decimal digits: $digits",
            action = {
                settingsRepository.setDecimalDigits(sanitizeDecimalDigits(digits))
            },
        )

    /**
     * Aggiorna il separatore decimale.
     */
    private fun setDecimalSeparator(separator: String) =
        updatePreference(
            logMsg = "Setting decimal separator: $separator",
            action = {
                val safeDecimal = sanitizeDecimalSeparator(separator)
                settingsRepository.setDecimalSeparator(safeDecimal)
                if (safeDecimal == state.value.thousandsSeparator) {
                    settingsRepository.setThousandsSeparator(DisplayConstant.DEFAULT_THOUSANDS_SEPARATOR)
                }
            },
        )

    /**
     * Aggiorna il separatore delle migliaia.
     */
    private fun setThousandsSeparator(separator: String) =
        updatePreference(
            logMsg = "Setting thousands separator: $separator",
            action = {
                val safeThousands =
                    if (separator in DisplayConstant.SUPPORTED_THOUSANDS_SEPARATORS) {
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
    private fun setMealVoucherValue(value: Double) {
        // Track meal voucher details update
        analyticsManager.logEvent(
            "meal_voucher_details_updated",
            Bundle().apply {
                putDouble("value", value)
            },
        )
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
    private fun setShowChartsSection(show: Boolean) =
        updatePreference(
            logMsg = "Setting show charts section: $show",
            action = { settingsRepository.setShowCharts(show) },
        )

    /**
     * Aggiorna la preferenza per lo zoom nei grafici.
     */
    private fun setChartsZoomEnabled(enabled: Boolean) =
        updatePreference(
            logMsg = "Setting charts zoom enabled: $enabled",
            action = { settingsRepository.setChartsZoomEnabled(enabled) },
        )

    /**
     * Aggiorna il formato data.
     */
    private fun setDateFormat(pattern: String) =
        updatePreference(
            logMsg = "Setting date format: $pattern",
            action = { settingsRepository.setDateFormat(pattern) },
        )

    /**
     * Aggiorna la preferenza per mostrare le note delle transazioni.
     */
    private fun setShowTransactionNotes(show: Boolean) =
        updatePreference(
            logMsg = "Setting show transaction notes: $show",
            action = { settingsRepository.setShowTransactionNotes(show) },
        )

    /**
     * Aggiorna la preferenza per mascherare gli importi con asterischi.
     */
    private fun setMaskAmounts(mask: Boolean) =
        updatePreference(
            logMsg = "Setting mask amounts: $mask",
            action = { settingsRepository.setMaskAmounts(mask) },
        )

    /**
     * Aggiorna la preferenza per mostrare il breakdown dei pagamenti.
     */
    private fun setShowPaymentTypeBreakdown(show: Boolean) =
        updatePreference(
            logMsg = "Setting show payment type breakdown: $show",
            action = { settingsRepository.setShowPaymentTypeBreakdown(show) },
        )

    /**
     * Aggiorna la preferenza per mostrare la card Insight rapidi in Home.
     */
    private fun setShowQuickInsightsCard(show: Boolean) =
        updatePreference(
            logMsg = "Setting show quick insights card: $show",
            action = { settingsRepository.setShowQuickInsightsCard(show) },
        )

    /**
     * Aggiorna il tipo di pagamento predefinito.
     */
    private fun setDefaultPaymentType(paymentType: String) =
        updatePreference(
            logMsg = "Setting default payment type: $paymentType",
            action = { settingsRepository.setDefaultPaymentType(paymentType) },
        )

    /**
     * Aggiorna il tipo di visualizzazione delle transazioni (Home).
     */
    private fun setTransactionDisplayType(displayType: TransactionDisplayType) =
        updatePreference(
            logMsg = "Setting home transaction display type: $displayType",
            action = { settingsRepository.setTransactionDisplayType(displayType) },
        )

    /**
     * Aggiorna il tipo di visualizzazione delle transazioni (Transazioni).
     */
    private fun setTransactionsTransactionDisplayType(displayType: TransactionDisplayType) =
        updatePreference(
            logMsg = "Setting transactions transaction display type: $displayType",
            action = { settingsRepository.setTransactionsTransactionDisplayType(displayType) },
        )

    /**
     * Aggiorna il colore di sfondo dei widget e ne forza il refresh immediato.
     */
    private fun setWidgetBackgroundColor(color: Long) =
        updatePreference(
            logMsg = "Setting widget background color: $color",
            action = {
                settingsRepository.setWidgetBackgroundColor(color)
                widgetUpdateNotifier.notifyTransactionsChanged()
            },
        )

    /**
     * Aggiorna l'opacità dei widget e ne forza il refresh immediato.
     */
    private fun setWidgetOpacity(opacity: Int) =
        updatePreference(
            logMsg = "Setting widget opacity: $opacity",
            action = {
                settingsRepository.setWidgetOpacity(opacity.coerceIn(0, 100))
                widgetUpdateNotifier.notifyTransactionsChanged()
            },
        )

    /**
     * Ripristina tutte le preferenze ai valori di default.
     */
    fun resetAllPreferences() =
        updatePreference(
            logMsg = "Resetting all preferences",
            action = { settingsRepository.resetAllPreferences() },
        )

    /**
     * Funzione di utilità per loggare e lanciare l'azione in coroutine.
     */
    private fun updatePreference(
        logMsg: String,
        action: suspend () -> Unit,
    ) {
        logDebug(logMsg)
        settingsModifiedCount++
        viewModelScope.launch {
            action()
            // Track settings customization score after each preference update
            engagementTracker.trackSettingsCustomizationScore(
                settingsModifiedCount = settingsModifiedCount,
                accessibilityEnabled = state.value.maskAmounts, // Use mask amounts as proxy for accessibility
            )
        }
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

    private fun sanitizeThousandsSeparator(
        thousands: String,
        decimal: String,
    ): String {
        val normalized =
            if (thousands in DisplayConstant.SUPPORTED_THOUSANDS_SEPARATORS) {
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
