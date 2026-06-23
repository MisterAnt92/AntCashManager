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
        if (sanitizedName !in AnalyticsConstants.ALLOWED_USAGE_EVENTS) {
            Logger.w(AnalyticsConstants.TAG) { "Blocked non-whitelisted analytics event=$sanitizedName" }
            return
        }

        runCatching {
            firebaseAnalytics?.logEvent(sanitizedName, sanitizeParams(params))
        }.onFailure { error ->
            Logger.e(AnalyticsConstants.TAG, error) { "Failed to log event=$sanitizedName" }
        }
    }

    private fun sanitizeParams(rawParams: Bundle): Bundle {
        val safeParams = Bundle()
        rawParams.keySet().forEach { key ->
            if (!isAllowedAnalyticsKey(key)) return@forEach

            when (val value = rawParams.get(key)) {
                is String -> {
                    if (value.matches(Regex("^[a-zA-Z0-9_-]{1,40}$"))) {
                        safeParams.putString(key, value)
                    }
                }

                is Int -> safeParams.putInt(key, value)
                is Long -> safeParams.putLong(key, value)
                is Double -> safeParams.putDouble(key, value)
                is Float -> safeParams.putFloat(key, value)
                is Boolean -> safeParams.putString(key, if (value) "true" else "false")
            }
        }
        return safeParams
    }

    private fun isAllowedAnalyticsKey(key: String): Boolean {
        val normalizedKey = key.lowercase()
        val blockedFragments = listOf(
            "email",
            "query",
            "message",
            "error",
            "title",
            "notes",
            "payee",
            "location",
            "tags",
            "amount",
        )
        return blockedFragments.none { fragment -> normalizedKey.contains(fragment) }
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

