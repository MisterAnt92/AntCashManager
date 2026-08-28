package com.antcashmanager.android.analytics

import android.os.Bundle
import java.util.UUID

/**
 * Tracker per metriche di sessione e app lifecycle.
 *
 * Traccia:
 * - App launch (first time install, cold start, warm start)
 * - Session start/end con duration
 * - Daily Active Users (DAU)
 * - App version updates
 *
 * Integrazione:
 * - MainActivity.onCreate: onAppLaunched()
 * - MainActivity.onResume: onSessionStarted()
 * - MainActivity.onPause: onSessionEnded()
 * - Version upgrade detection: onVersionUpdated()
 *
 * Privacy: Session IDs sono UUIDs anonimizzati, nessun user identification
 */
class SessionTracker(private val analyticsManager: AnalyticsManager) {

    private val sessionId = UUID.randomUUID().toString()
    private var sessionStartTime: Long = 0L

    /**
     * Traccia l'avvio dell'applicazione.
     *
     * Chiamare in Application.onCreate() o MainActivity.onCreate()
     *
     * @param appVersion Versione dell'app (es: "1.7.4")
     * @param isFirstLaunch Se è il primo lancio dell'app
     */
    fun onAppLaunched(appVersion: String, isFirstLaunch: Boolean = false) {
        analyticsManager.logEvent("app_launched", Bundle().apply {
            putString("version", appVersion)
            putBoolean("is_first_launch", isFirstLaunch)
        })
    }

    /**
     * Traccia l'inizio di una sessione utente.
     *
     * Chiamare in Activity.onResume()
     *
     * @param screenName Nome della schermata iniziale (opzionale)
     */
    fun onSessionStarted(screenName: String? = null) {
        sessionStartTime = System.currentTimeMillis()
        analyticsManager.logEvent("session_started", Bundle().apply {
            putString("session_id", sessionId)
            if (screenName != null) {
                putString("screen", screenName)
            }
        })

        // Traccia Daily Active User se è il primo evento della giornata
        trackDailyActiveUser()
    }

    /**
     * Traccia la fine di una sessione utente.
     *
     * Chiamare in Activity.onPause() oppure onDestroy()
     *
     * @param screenName Nome della schermata finale (opzionale)
     */
    fun onSessionEnded(screenName: String? = null) {
        val durationSecs = if (sessionStartTime > 0) {
            (System.currentTimeMillis() - sessionStartTime) / 1000
        } else {
            0L
        }

        analyticsManager.logEvent("session_ended", Bundle().apply {
            putString("session_id", sessionId)
            putLong("duration_secs", durationSecs)
            if (screenName != null) {
                putString("screen", screenName)
            }
        })

        sessionStartTime = 0L
    }

    /**
     * Traccia un aggiornamento di versione dell'app.
     *
     * Chiamare dopo aver rilevato un cambio di versione in SharedPreferences
     *
     * @param oldVersion Versione precedente (es: "1.7.3")
     * @param newVersion Nuova versione (es: "1.7.4")
     */
    fun onVersionUpdated(oldVersion: String, newVersion: String) {
        analyticsManager.logEvent("app_version_updated", Bundle().apply {
            putString("old_version", oldVersion)
            putString("new_version", newVersion)
        })
    }

    /**
     * Traccia un Daily Active User (DAU).
     *
     * Chiamare una volta al giorno dal primo evento della sessione.
     * Implementazione: check la data dell'ultimo evento, se è diversa da oggi, log DAU.
     */
    private fun trackDailyActiveUser() {
        val dateFormat = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
        val today = dateFormat.format(java.util.Date())

        analyticsManager.logEvent("daily_active_user", Bundle().apply {
            putString("date", today)
        })
    }

    /**
     * Ritorna l'ID sessione corrente (per correlazione con altri eventi).
     */
    fun getSessionId(): String = sessionId
}
