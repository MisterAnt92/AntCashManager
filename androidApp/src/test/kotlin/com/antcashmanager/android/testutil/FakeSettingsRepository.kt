package com.antcashmanager.android.testutil

import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.TransactionDisplayType
import com.antcashmanager.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Fake unico e completo di [SettingsRepository] per i test dei ViewModel.
 *
 * Ogni impostazione è un [MutableStateFlow] pubblico inizializzato al valore di
 * default reale dell'app: nessun metodo lancia eccezioni, quindi una chiamata
 * introdotta da una futura modifica al ViewModel non rompe i test esistenti.
 * Per personalizzare un valore in un test basta assegnare `.value` al campo
 * corrispondente, prima o dopo la creazione del ViewModel.
 */
open class FakeSettingsRepository : SettingsRepository {

    companion object {
        private const val WEEK_IN_MILLIS = 7L * 24 * 60 * 60 * 1000
        private const val MONTH_IN_MILLIS = 30L * 24 * 60 * 60 * 1000

        private fun defaultDateFilter(durationMs: Long): SavedDateFilter {
            val now = System.currentTimeMillis()
            return SavedDateFilter(presetIndex = 1, from = now - durationMs, to = now)
        }
    }

    val theme = MutableStateFlow(AppTheme.SYSTEM)
    val language = MutableStateFlow(AppLanguage.SYSTEM)
    val showCharts = MutableStateFlow(true)
    val highContrast = MutableStateFlow(false)
    val largeText = MutableStateFlow(false)
    val reduceMotion = MutableStateFlow(false)
    val showTransactionNotes = MutableStateFlow(true)
    val maskAmounts = MutableStateFlow(false)
    val currencySymbol = MutableStateFlow("€")
    val decimalDigits = MutableStateFlow(2)
    val decimalSeparator = MutableStateFlow(",")
    val thousandsSeparator = MutableStateFlow("")
    val mealVoucherValue = MutableStateFlow(5.29)
    val dateFormat = MutableStateFlow("dd/MM/yyyy")
    val dateFilterExpanded = MutableStateFlow(true)
    val homeDateFilterPreset = MutableStateFlow(1)
    val homeDateFilterState = MutableStateFlow(defaultDateFilter(WEEK_IN_MILLIS))
    val transactionsDateFilterPreset = MutableStateFlow(1)
    val transactionsDateFilterState = MutableStateFlow(defaultDateFilter(WEEK_IN_MILLIS))
    val chartsDateFilterPreset = MutableStateFlow(1)
    val chartsDateFilterState = MutableStateFlow(defaultDateFilter(MONTH_IN_MILLIS))
    val chartsZoomEnabled = MutableStateFlow(false)
    val showPaymentTypeBreakdown = MutableStateFlow(false)
    val showQuickInsightsCard = MutableStateFlow(true)
    val transactionDisplayType = MutableStateFlow(TransactionDisplayType.TREND)
    val transactionsTransactionDisplayType = MutableStateFlow(TransactionDisplayType.TREND)
    val showInitialAnimation = MutableStateFlow(false)
    val isTutorialCompleted = MutableStateFlow(false)
    val categorySortOrderInitialized = MutableStateFlow(false)
    val dataEncryptionEnabled = MutableStateFlow(false)
    val lastBackupTimestamp = MutableStateFlow<Long?>(null)
    val lastRestoreTimestamp = MutableStateFlow<Long?>(null)
    val suggestionsEnabled = MutableStateFlow(true)
    val suggestionsClearedAt = MutableStateFlow<Long?>(null)
    val widgetBackgroundColor = MutableStateFlow(0xFFFFFFFFL)
    val widgetOpacity = MutableStateFlow(100)

    override fun getTheme(): Flow<AppTheme> = theme
    override suspend fun setTheme(theme: AppTheme) {
        this.theme.value = theme
    }

    override fun getLanguage(): Flow<AppLanguage> = language
    override suspend fun setLanguage(language: AppLanguage) {
        this.language.value = language
    }

    override fun getShowCharts(): Flow<Boolean> = showCharts
    override suspend fun setShowCharts(show: Boolean) {
        showCharts.value = show
    }

    override fun getHighContrast(): Flow<Boolean> = highContrast
    override suspend fun setHighContrast(enabled: Boolean) {
        highContrast.value = enabled
    }

    override fun getLargeText(): Flow<Boolean> = largeText
    override suspend fun setLargeText(enabled: Boolean) {
        largeText.value = enabled
    }

    override fun getReduceMotion(): Flow<Boolean> = reduceMotion
    override suspend fun setReduceMotion(enabled: Boolean) {
        reduceMotion.value = enabled
    }

    override fun getShowTransactionNotes(): Flow<Boolean> = showTransactionNotes
    override suspend fun setShowTransactionNotes(show: Boolean) {
        showTransactionNotes.value = show
    }

    override fun getMaskAmounts(): Flow<Boolean> = maskAmounts
    override suspend fun setMaskAmounts(mask: Boolean) {
        maskAmounts.value = mask
    }

    override fun getCurrencySymbol(): Flow<String> = currencySymbol
    override suspend fun setCurrencySymbol(symbol: String) {
        currencySymbol.value = symbol
    }

    override fun getDecimalDigits(): Flow<Int> = decimalDigits
    override suspend fun setDecimalDigits(digits: Int) {
        decimalDigits.value = digits
    }

    override fun getDecimalSeparator(): Flow<String> = decimalSeparator
    override suspend fun setDecimalSeparator(separator: String) {
        decimalSeparator.value = separator
    }

    override fun getThousandsSeparator(): Flow<String> = thousandsSeparator
    override suspend fun setThousandsSeparator(separator: String) {
        thousandsSeparator.value = separator
    }

    override fun getMealVoucherValue(): Flow<Double> = mealVoucherValue
    override suspend fun setMealVoucherValue(value: Double) {
        mealVoucherValue.value = value
    }

    override fun getDateFormat(): Flow<String> = dateFormat
    override suspend fun setDateFormat(pattern: String) {
        dateFormat.value = pattern
    }

    override fun getDateFilterExpanded(): Flow<Boolean> = dateFilterExpanded
    override suspend fun setDateFilterExpanded(expanded: Boolean) {
        dateFilterExpanded.value = expanded
    }

    override fun getHomeDateFilterPreset(): Flow<Int> = homeDateFilterPreset
    override suspend fun setHomeDateFilterPreset(index: Int) {
        homeDateFilterPreset.value = index
    }

    override fun getHomeDateFilterState(): Flow<SavedDateFilter> = homeDateFilterState
    override suspend fun setHomeDateFilterState(filter: SavedDateFilter) {
        homeDateFilterState.value = filter
    }

    override fun getTransactionsDateFilterPreset(): Flow<Int> = transactionsDateFilterPreset
    override suspend fun setTransactionsDateFilterPreset(index: Int) {
        transactionsDateFilterPreset.value = index
    }

    override fun getTransactionsDateFilterState(): Flow<SavedDateFilter> = transactionsDateFilterState
    override suspend fun setTransactionsDateFilterState(filter: SavedDateFilter) {
        transactionsDateFilterState.value = filter
    }

    override fun getChartsDateFilterPreset(): Flow<Int> = chartsDateFilterPreset
    override suspend fun setChartsDateFilterPreset(index: Int) {
        chartsDateFilterPreset.value = index
    }

    override fun getChartsDateFilterState(): Flow<SavedDateFilter> = chartsDateFilterState
    override suspend fun setChartsDateFilterState(filter: SavedDateFilter) {
        chartsDateFilterState.value = filter
    }

    override fun getChartsZoomEnabled(): Flow<Boolean> = chartsZoomEnabled
    override suspend fun setChartsZoomEnabled(enabled: Boolean) {
        chartsZoomEnabled.value = enabled
    }

    override fun getShowPaymentTypeBreakdown(): Flow<Boolean> = showPaymentTypeBreakdown
    override suspend fun setShowPaymentTypeBreakdown(show: Boolean) {
        showPaymentTypeBreakdown.value = show
    }

    override fun getShowQuickInsightsCard(): Flow<Boolean> = showQuickInsightsCard
    override suspend fun setShowQuickInsightsCard(show: Boolean) {
        showQuickInsightsCard.value = show
    }

    override fun getTransactionDisplayType(): Flow<TransactionDisplayType> = transactionDisplayType
    override suspend fun setTransactionDisplayType(displayType: TransactionDisplayType) {
        transactionDisplayType.value = displayType
    }

    override fun getTransactionsTransactionDisplayType(): Flow<TransactionDisplayType> =
        transactionsTransactionDisplayType

    override suspend fun setTransactionsTransactionDisplayType(displayType: TransactionDisplayType) {
        transactionsTransactionDisplayType.value = displayType
    }

    override fun getShowInitialAnimation(): Flow<Boolean> = showInitialAnimation
    override suspend fun setShowInitialAnimation(show: Boolean) {
        showInitialAnimation.value = show
    }

    override fun getIsTutorialCompleted(): Flow<Boolean> = isTutorialCompleted
    override suspend fun setIsTutorialCompleted(completed: Boolean) {
        isTutorialCompleted.value = completed
    }

    override fun getCategorySortOrderInitialized(): Flow<Boolean> = categorySortOrderInitialized
    override suspend fun setCategorySortOrderInitialized(initialized: Boolean) {
        categorySortOrderInitialized.value = initialized
    }

    override fun getDataEncryptionEnabled(): Flow<Boolean> = dataEncryptionEnabled
    override suspend fun setDataEncryptionEnabled(enabled: Boolean) {
        dataEncryptionEnabled.value = enabled
    }

    override fun getLastBackupTimestamp(): Flow<Long?> = lastBackupTimestamp
    override suspend fun setLastBackupTimestamp(timestamp: Long) {
        lastBackupTimestamp.value = timestamp
    }

    override fun getLastRestoreTimestamp(): Flow<Long?> = lastRestoreTimestamp
    override suspend fun setLastRestoreTimestamp(timestamp: Long) {
        lastRestoreTimestamp.value = timestamp
    }

    override fun getSuggestionsEnabled(): Flow<Boolean> = suggestionsEnabled
    override suspend fun setSuggestionsEnabled(enabled: Boolean) {
        suggestionsEnabled.value = enabled
    }

    override fun getSuggestionsClearedAt(): Flow<Long?> = suggestionsClearedAt
    override suspend fun setSuggestionsClearedAt(timestamp: Long) {
        suggestionsClearedAt.value = timestamp
    }

    override fun getWidgetBackgroundColor(): Flow<Long> = widgetBackgroundColor
    override suspend fun setWidgetBackgroundColor(color: Long) {
        widgetBackgroundColor.value = color
    }

    override fun getWidgetOpacity(): Flow<Int> = widgetOpacity
    override suspend fun setWidgetOpacity(opacity: Int) {
        widgetOpacity.value = opacity
    }

    override suspend fun resetAllPreferences() {
        theme.value = AppTheme.SYSTEM
        language.value = AppLanguage.SYSTEM
        showCharts.value = true
        highContrast.value = false
        largeText.value = false
        reduceMotion.value = false
        showTransactionNotes.value = true
        maskAmounts.value = false
        currencySymbol.value = "€"
        decimalDigits.value = 2
        decimalSeparator.value = ","
        thousandsSeparator.value = ""
        mealVoucherValue.value = 5.29
        dateFormat.value = "dd/MM/yyyy"
        dateFilterExpanded.value = true
        chartsZoomEnabled.value = false
        showPaymentTypeBreakdown.value = false
        showQuickInsightsCard.value = true
        transactionDisplayType.value = TransactionDisplayType.TREND
        transactionsTransactionDisplayType.value = TransactionDisplayType.TREND
        showInitialAnimation.value = false
        dataEncryptionEnabled.value = false
        suggestionsEnabled.value = true
        suggestionsClearedAt.value = null
        widgetBackgroundColor.value = 0xFFFFFFFFL
        widgetOpacity.value = 100
    }
}
