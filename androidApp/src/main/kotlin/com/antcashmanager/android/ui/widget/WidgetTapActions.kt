package com.antcashmanager.android.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.antcashmanager.android.MainActivity

/**
 * Apre l'app e traccia l'evento di utilizzo del widget al tap. Usato al posto di
 * `actionStartActivity<MainActivity>()` (che non permette di eseguire codice, es. logging,
 * prima della navigazione) per entrambi i widget della home screen.
 */
private fun openApp(context: Context) {
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}

class RecentTransactionsWidgetTapAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        WidgetDependencies.analyticsManager.logEvent("widget_recent_transactions_opened")
        openApp(context)
    }
}

class CategoryBreakdownWidgetTapAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        WidgetDependencies.analyticsManager.logEvent("widget_category_breakdown_opened")
        openApp(context)
    }
}
