package com.antcashmanager.android.analytics.tracker

import android.os.Bundle
import com.antcashmanager.android.analytics.AnalyticsManager

/**
 * Tracker per user engagement e interaction depth metrics.
 *
 * Traccia:
 * - Search effectiveness (search conversions, result quality)
 * - Chart interaction depth (zoom, tap, time spent)
 * - Widget engagement (active session duration, tap frequency)
 * - Settings customization (settings modified count, feature adoption)
 *
 * Integrazione:
 * - TransactionsScreen: track search effectiveness
 * - ChartsScreen: track chart interactions (zoom, tap, engagement time)
 * - Widget: track engagement session start/end
 * - SettingsScreen: track customization score
 *
 * Privacy: Solo metriche di engagement, nessun dato sensibile
 */
class EngagementTracker(private val analyticsManager: AnalyticsManager) {

    /**
     * Traccia l'efficienza di una ricerca (search-to-result conversion).
     *
     * Usare in TransactionsScreen quando l'utente interagisce coi risultati di ricerca
     *
     * @param queryLength Lunghezza della query
     * @param resultsCount Numero di risultati restituiti
     * @param resultClicked Se l'utente ha fatto clic su un risultato
     */
    fun trackTransactionSearchEffectiveness(queryLength: Int, resultsCount: Int, resultClicked: Boolean) {
        analyticsManager.logEvent("transaction_search_effectiveness", Bundle().apply {
            putInt("query_length", queryLength)
            putInt("results_count", resultsCount)
            putBoolean("result_clicked", resultClicked)
        })
    }

    /**
     * Traccia la profondità di interazione con i grafici.
     *
     * Usare in ChartsScreen dopo interazioni significative (zoom, tap, drag)
     *
     * @param chartType Tipo di grafico (es: "pie", "line", "bar")
     * @param zoomEnabled Se l'utente ha usato zoom
     * @param interactionCount Numero di interazioni durante la sessione
     */
    fun trackChartInteractionDepth(chartType: String, zoomEnabled: Boolean, interactionCount: Int) {
        analyticsManager.logEvent("chart_interaction_depth", Bundle().apply {
            putString("chart_type", chartType)
            putBoolean("zoom_enabled", zoomEnabled)
            putInt("interaction_count", interactionCount)
        })
    }

    /**
     * Traccia l'inizio di una sessione di engagement con i widget.
     *
     * Usare quando l'utente visita la home screen o interagisce con i widget
     *
     * @param widgetType Tipo di widget (es: "recent_transactions", "category_breakdown")
     * @param activeDurationSecs Durata della sessione in secondi
     */
    fun trackWidgetEngagementSession(widgetType: String, activeDurationSecs: Int) {
        analyticsManager.logEvent("widget_engagement_session", Bundle().apply {
            putString("widget_type", widgetType)
            putInt("active_duration_secs", activeDurationSecs)
        })
    }

    /**
     * Traccia il livello di customizzazione dell'app da parte dell'utente.
     *
     * Usare periodicamente (es: ogni sessione o giornalmente) per misurare l'adozione
     *
     * @param settingsModifiedCount Numero di impostazioni che l'utente ha modificato
     * @param accessibilityEnabled Se le opzioni di accessibilità sono abilitate
     */
    fun trackSettingsCustomizationScore(settingsModifiedCount: Int, accessibilityEnabled: Boolean) {
        analyticsManager.logEvent("settings_customization_score", Bundle().apply {
            putInt("settings_modified_count", settingsModifiedCount)
            putBoolean("accessibility_enabled", accessibilityEnabled)
        })
    }
}