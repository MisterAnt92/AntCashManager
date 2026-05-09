package com.antcashmanager.domain.repository

import com.antcashmanager.domain.model.AppLanguage
import com.antcashmanager.domain.model.AppTheme
import com.antcashmanager.domain.model.TransactionDisplayType
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getTheme(): Flow<AppTheme>
    suspend fun setTheme(theme: AppTheme)
    fun getLanguage(): Flow<AppLanguage>
    suspend fun setLanguage(language: AppLanguage)
    fun getShowCharts(): Flow<Boolean>
    suspend fun setShowCharts(show: Boolean)
    fun getHighContrast(): Flow<Boolean>
    suspend fun setHighContrast(enabled: Boolean)
    fun getLargeText(): Flow<Boolean>
    suspend fun setLargeText(enabled: Boolean)
    fun getReduceMotion(): Flow<Boolean>
    suspend fun setReduceMotion(enabled: Boolean)
    fun getShowTransactionNotes(): Flow<Boolean>
    suspend fun setShowTransactionNotes(show: Boolean)

    // ── Currency / number format ──
    fun getCurrencySymbol(): Flow<String>
    suspend fun setCurrencySymbol(symbol: String)
    fun getDecimalDigits(): Flow<Int>
    suspend fun setDecimalDigits(digits: Int)
    fun getDecimalSeparator(): Flow<String>
    suspend fun setDecimalSeparator(separator: String)
    fun getThousandsSeparator(): Flow<String>
    suspend fun setThousandsSeparator(separator: String)

    // ── Date format ──
    fun getDateFormat(): Flow<String>
    suspend fun setDateFormat(pattern: String)

    // ── Date Filter UI State ──
    fun getDateFilterExpanded(): Flow<Boolean>
    suspend fun setDateFilterExpanded(expanded: Boolean)

    fun getHomeDateFilterPreset(): Flow<Int>
    suspend fun setHomeDateFilterPreset(index: Int)

    fun getTransactionsDateFilterPreset(): Flow<Int>
    suspend fun setTransactionsDateFilterPreset(index: Int)

    fun getChartsDateFilterPreset(): Flow<Int>
    suspend fun setChartsDateFilterPreset(index: Int)

    fun getChartsZoomEnabled(): Flow<Boolean>
    suspend fun setChartsZoomEnabled(enabled: Boolean)

    // ── Payment Type Breakdown ──
    fun getShowPaymentTypeBreakdown(): Flow<Boolean>
    suspend fun setShowPaymentTypeBreakdown(show: Boolean)

    // ── Transaction Display Type ──
    fun getTransactionDisplayType(): Flow<TransactionDisplayType>
    suspend fun setTransactionDisplayType(displayType: TransactionDisplayType)

    fun getTransactionsTransactionDisplayType(): Flow<TransactionDisplayType>
    suspend fun setTransactionsTransactionDisplayType(displayType: TransactionDisplayType)

    // ── Onboarding / Tutorial ──
    fun getIsTutorialCompleted(): Flow<Boolean>
    suspend fun setIsTutorialCompleted(completed: Boolean)

    // ── Security ──
    fun getDataEncryptionEnabled(): Flow<Boolean>
    suspend fun setDataEncryptionEnabled(enabled: Boolean)

    /** Resets every preference to its factory default. */
    suspend fun resetAllPreferences()
}
