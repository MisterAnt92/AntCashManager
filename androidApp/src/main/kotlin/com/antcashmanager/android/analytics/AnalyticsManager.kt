package com.antcashmanager.android.analytics

import android.content.Context
import android.os.Bundle
import co.touchlab.kermit.Logger
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Wrapper centralizzato per Firebase Analytics.
 * Espone metodi semplici per tracciare schermate ed eventi custom.
 */
open class AnalyticsManager {

    private val firebaseAnalytics: FirebaseAnalytics?

    constructor() {
        firebaseAnalytics = null
    }

    constructor(context: Context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context.applicationContext)
    }

    fun logScreenView(route: String) {
        val screenName = sanitizeName(route.toAnalyticsName())
        if (screenName.isBlank()) return

        runCatching {
            val params = Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString(
                    FirebaseAnalytics.Param.SCREEN_CLASS,
                    AnalyticsConstants.SCREEN_CLASS_COMPOSE_NAV_HOST,
                )
            }
            firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)
        }.onFailure { error ->
            Logger.e(AnalyticsConstants.TAG, error) { "Failed to log screen view for route=$route" }
        }
    }

    fun logEvent(eventName: String, params: Bundle = Bundle()) {
        val sanitizedName = sanitizeName(eventName)
        if (sanitizedName.isBlank()) return

        runCatching {
            firebaseAnalytics?.logEvent(sanitizedName, params)
        }.onFailure { error ->
            Logger.e(AnalyticsConstants.TAG, error) { "Failed to log event=$sanitizedName" }
        }
    }

    private fun sanitizeName(rawValue: String): String =
        rawValue
            .lowercase()
            .replace(Regex("[^a-z0-9_]"), "_")
            .trim('_')
            .take(AnalyticsConstants.MAX_NAME_LENGTH)

    private fun String.toAnalyticsName(): String =
        substringBefore('?')
            .substringBefore('/').ifBlank { this }
}

