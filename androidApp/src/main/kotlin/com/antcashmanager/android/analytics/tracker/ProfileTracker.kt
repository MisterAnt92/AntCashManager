package com.antcashmanager.android.analytics.tracker

import android.os.Bundle
import com.antcashmanager.android.analytics.AnalyticsConstants.Events
import com.antcashmanager.android.analytics.AnalyticsConstants.Params
import com.antcashmanager.android.analytics.AnalyticsManager

/**
 * Tracker per le azioni utente sulla sezione Profili.
 *
 * Traccia:
 * - Creazione, modifica, eliminazione e switch di profili
 * - Toggle share-transactions
 *
 * Privacy: nessun nome, colore, emoji o foto viene inviato; solo flag booleani e contatori.
 */
class ProfileTracker(
    private val analyticsManager: AnalyticsManager,
) {
    fun trackProfileCreated(
        hasPhoto: Boolean,
        shareTransactions: Boolean,
    ) {
        analyticsManager.logEvent(
            Events.PROFILE_CREATED,
            Bundle().apply {
                putString(Params.HAS_PHOTO, if (hasPhoto) Params.VALUE_YES else Params.VALUE_NO)
                putString(Params.SHARE_TRANSACTIONS, if (shareTransactions) Params.VALUE_YES else Params.VALUE_NO)
            },
        )
    }

    fun trackProfileSwitched(profileCount: Int) {
        analyticsManager.logEvent(
            Events.PROFILE_SWITCHED,
            Bundle().apply {
                putInt(Params.PROFILE_COUNT, profileCount)
            },
        )
    }

    fun trackProfileDeleted(remainingCount: Int) {
        analyticsManager.logEvent(
            Events.PROFILE_DELETED,
            Bundle().apply {
                putInt(Params.REMAINING_COUNT, remainingCount)
            },
        )
    }

    fun trackProfileUpdated(
        photoChanged: Boolean,
        shareTransactionsChanged: Boolean,
    ) {
        analyticsManager.logEvent(
            Events.PROFILE_UPDATED,
            Bundle().apply {
                putString(Params.PHOTO_CHANGED, if (photoChanged) Params.VALUE_YES else Params.VALUE_NO)
                putString(Params.SHARE_TRANSACTIONS_CHANGED, if (shareTransactionsChanged) Params.VALUE_YES else Params.VALUE_NO)
            },
        )
    }

    fun trackShareTransactionsToggled(enabled: Boolean) {
        analyticsManager.logEvent(
            Events.PROFILE_SHARE_TRANSACTIONS_TOGGLED,
            Bundle().apply {
                putString(Params.ENABLED, if (enabled) Params.VALUE_YES else Params.VALUE_NO)
            },
        )
    }
}
