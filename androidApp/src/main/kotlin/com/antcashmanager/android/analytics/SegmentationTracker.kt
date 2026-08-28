package com.antcashmanager.android.analytics

import android.os.Bundle

/**
 * Tracker per user segmentation e advanced cohort analysis.
 *
 * Traccia:
 * - User cohort assignment (first time install, acquisition date)
 * - Spending patterns (daily/weekly/monthly frequency)
 * - Income sources (tracking income by category)
 * - Budget exceeded alerts (over-spending detection)
 * - Category preference shifts (category change patterns)
 * - Payment method preferences (user payment distribution)
 *
 * Integrazione:
 * - ViewModel: Periodically analyze transaction patterns
 * - Charts: Detect spending patterns from chart data
 * - Display: Track payment method preferences
 * - Settings: Track customization score
 *
 * Privacy: Solo metriche aggregate, nessun dato personale
 */
class SegmentationTracker(private val analyticsManager: AnalyticsManager) {

    /**
     * Traccia l'assegnazione della cohort al primo lancio dell'app.
     *
     * Usare in MainActivity.onCreate() o Application.onCreate()
     *
     * @param cohortDate Data di acquisizione (YYYYMMDD)
     * @param appVersion Versione dell'app al primo lancio
     * @param deviceType Tipo di device (es: "phone", "tablet")
     */
    fun trackUserCohortIdentified(cohortDate: String, appVersion: String, deviceType: String = "phone") {
        analyticsManager.logEvent("user_cohort_identified", Bundle().apply {
            putString("cohort_date", cohortDate)
            putString("app_version", appVersion)
            putString("device_type", deviceType)
        })
    }

    /**
     * Traccia un pattern di spesa rilevato nei dati storici.
     *
     * Usare in ChartsViewModel o TransactionsViewModel quando si analizzano i dati
     *
     * @param pattern Tipo di pattern (es: "daily", "weekly", "monthly", "spike")
     * @param category Categoria principale del pattern
     * @param avgAmount Importo medio del pattern
     */
    fun trackSpendingPatternDetected(pattern: String, category: String, avgAmount: Double) {
        analyticsManager.logEvent("spending_pattern_detected", Bundle().apply {
            putString("pattern", pattern)
            putString("category", category)
            putDouble("avg_amount", avgAmount)
        })
    }

    /**
     * Traccia le fonti di reddito principali dell'utente.
     *
     * Usare in ChartsViewModel o HomeViewModel quando si analizzano le entrate
     *
     * @param sourceCategory Categoria principale di reddito
     * @param frequency Frequenza (es: "monthly", "weekly", "daily")
     * @param avgAmount Importo medio per transazione
     */
    fun trackIncomeSourceTracking(sourceCategory: String, frequency: String, avgAmount: Double) {
        analyticsManager.logEvent("income_source_tracking", Bundle().apply {
            putString("source_category", sourceCategory)
            putString("frequency", frequency)
            putDouble("avg_amount", avgAmount)
        })
    }

    /**
     * Traccia un avviso di budget superato.
     *
     * Usare quando si rileva che la spesa supera un budget implicito o esplicito
     *
     * @param category Categoria che ha superato il budget
     * @param budget Importo del budget
     * @param spent Importo speso
     */
    fun trackBudgetExceededAlert(category: String, budget: Double, spent: Double) {
        val percentageOver = ((spent - budget) / budget * 100).toInt()
        analyticsManager.logEvent("budget_exceeded_alert", Bundle().apply {
            putString("category", category)
            putDouble("budget", budget)
            putDouble("spent", spent)
            putInt("percentage_over", percentageOver)
        })
    }

    /**
     * Traccia un cambio di preferenza in una categoria nel tempo.
     *
     * Usare quando si rileva che la categoria di spesa principale cambia tra periodi
     *
     * @param oldCategory Categoria precedente
     * @param newCategory Nuova categoria
     * @param daysDiff Giorni tra i due periodi
     */
    fun trackCategoryPreferenceShift(oldCategory: String, newCategory: String, daysDiff: Int) {
        analyticsManager.logEvent("category_preference_shift", Bundle().apply {
            putString("old_category", oldCategory)
            putString("new_category", newCategory)
            putInt("days_diff", daysDiff)
        })
    }

    /**
     * Traccia la preferenza di metodo di pagamento principale dell'utente.
     *
     * Usare periodicamente per monitorare la distribuzione dei metodi di pagamento
     *
     * @param preferredMethod Metodo di pagamento preferito (ELECTRONIC, CASH, MEAL_VOUCHERS)
     * @param usagePercent Percentuale di utilizzo (0-100)
     */
    fun trackPaymentMethodPreference(preferredMethod: String, usagePercent: Int) {
        analyticsManager.logEvent("payment_method_preference", Bundle().apply {
            putString("preferred_method", preferredMethod)
            putInt("usage_percent", usagePercent)
        })
    }
}
