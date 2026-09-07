package com.antcashmanager.android.analytics

import android.content.Context
import android.os.Bundle
import co.touchlab.kermit.Logger
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Wrapper centralizzato per Firebase Analytics.
 * Espone metodi semplici per tracciare schermate ed eventi custom.
 */
open class AnalyticsManager {
    private val firebaseAnalytics: FirebaseAnalytics?

    @Volatile private var consentGranted: Boolean = false

    constructor(context: Context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context.applicationContext)
    }

    /**
     * Apply GDPR consent decision at runtime.
     * Called from AntCashManagerApp whenever the DataStore value changes.
     * Enables/disables Analytics collection and sets Consent Mode signals.
     */
    fun applyConsent(granted: Boolean) {
        consentGranted = granted
        firebaseAnalytics?.setAnalyticsCollectionEnabled(granted)
        firebaseAnalytics?.setConsent(
            mapOf(
                FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE to
                    if (granted) FirebaseAnalytics.ConsentStatus.GRANTED else FirebaseAnalytics.ConsentStatus.DENIED,
                FirebaseAnalytics.ConsentType.AD_STORAGE to FirebaseAnalytics.ConsentStatus.DENIED,
                FirebaseAnalytics.ConsentType.AD_USER_DATA to FirebaseAnalytics.ConsentStatus.DENIED,
                FirebaseAnalytics.ConsentType.AD_PERSONALIZATION to FirebaseAnalytics.ConsentStatus.DENIED,
            ),
        )
        runCatching { FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = granted }
    }

    fun logScreenView(route: String) {
        if (!consentGranted) return
        val screenName = sanitizeName(route.toAnalyticsName())
        if (screenName.isBlank()) return

        runCatching {
            val params =
                Bundle().apply {
                    putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                    putString(
                        FirebaseAnalytics.Param.SCREEN_CLASS,
                        AnalyticsConstants.SCREEN_CLASS_COMPOSE_NAV_HOST,
                    )
                }
            firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)
        }.onFailure { error ->
            Logger.e(
                throwable = error,
                tag = AnalyticsConstants.TAG,
            ) { "Failed to log screen view for route=$route" }
        }
    }

    fun logEvent(
        eventName: String,
        params: Bundle = Bundle(),
    ) {
        if (!consentGranted) return
        val sanitizedName = sanitizeName(eventName)
        if (sanitizedName.isBlank()) return
        if (sanitizedName !in AnalyticsConstants.ALLOWED_USAGE_EVENTS) {
            Logger.w(tag = AnalyticsConstants.TAG) { "Blocked non-whitelisted analytics event=$sanitizedName" }
            return
        }

        runCatching {
            firebaseAnalytics?.logEvent(sanitizedName, sanitizeParams(params))
        }.onFailure { error ->
            Logger.e(
                throwable = error,
                tag = AnalyticsConstants.TAG,
            ) { "Failed to log event=$sanitizedName" }
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
        // Blocked keys contain personal/sensitive data (email, names, locations, amounts).
        // ALLOWED: error_code (diagnostic, not personal), operation names, type enums.
        val blockedFragments =
            listOf(
                "email", // Personal identifier
                "query", // User search input (potentially sensitive)
                "message", // User-generated text (sensitive)
                // NOTE: "error" removed — error_code is diagnostic, not personal data
                "title", // Transaction/note title (personal content)
                "notes", // Transaction notes (personal content)
                "payee", // Transaction payee name (personal contact)
                "location", // Transaction location (personal location)
                "tags", // User tags (personal classification)
                "amount", // Transaction amounts (personal financial data)
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
            .substringBefore('/')
            .ifBlank { this }
}
