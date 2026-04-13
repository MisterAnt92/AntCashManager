package com.antcashmanager.android.ui.screen.charts.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.runtime.Composable
import com.antcashmanager.android.ui.components.HelpDialogContent
import com.antcashmanager.android.ui.components.SimpleHelpFeature

@Composable
internal fun HelpDialog(onDismiss: () -> Unit) {
    val helpFeatures = listOf(
        SimpleHelpFeature(
            title = "Visualizzazione Grafici",
            description = "Vedi i tuoi dati finanziari in formato grafico con pie chart per le categorie",
            icon = Icons.Default.BarChart,
        ),
        SimpleHelpFeature(
            title = "Filtri Temporali",
            description = "Seleziona periodi predefiniti o personalizzati per analizzare i tuoi dati",
            icon = Icons.Default.CalendarMonth,
        ),
        SimpleHelpFeature(
            title = "Analisi Dettagliata",
            description = "Visualizza il riepilogo mensile e l'analisi per categoria",
            icon = Icons.AutoMirrored.Default.TrendingUp,
        ),
    )

    HelpDialogContent(
        isVisible = true,
        title = "Guida Grafici",
        description = "Visualizza grafici e analisi dei tuoi dati finanziari!",
        features = helpFeatures,
        onDismiss = onDismiss,
    )
}

