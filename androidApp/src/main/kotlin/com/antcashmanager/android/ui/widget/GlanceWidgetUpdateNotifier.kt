package com.antcashmanager.android.ui.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.antcashmanager.domain.service.WidgetUpdateNotifier

class GlanceWidgetUpdateNotifier(
    private val context: Context,
) : WidgetUpdateNotifier {
    override suspend fun notifyTransactionsChanged() {
        RecentTransactionsWidget().updateAll(context)
        CategoryBreakdownWidget().updateAll(context)
    }
}
