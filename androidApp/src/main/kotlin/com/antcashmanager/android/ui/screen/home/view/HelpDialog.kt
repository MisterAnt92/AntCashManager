package com.antcashmanager.android.ui.screen.home.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.runtime.Composable
import com.antcashmanager.android.ui.components.HelpDialogContent
import com.antcashmanager.android.ui.components.SimpleHelpFeature

// ══════════════════════════════════════════════════════════════════════════════
// HELP DIALOG
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    val helpFeatures = listOf(
        SimpleHelpFeature(
            title = "Dashboard",
            description = "Visualizza il saldo totale, entrate e uscite nel periodo selezionato.",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
        ),
        SimpleHelpFeature(
            title = "Filtri Intervallo Date",
            description = "Filtra le transazioni per date specifiche o usa i preset disponibili.",
            icon = Icons.Default.ArrowUpward,
        ),
        SimpleHelpFeature(
            title = "Transazioni Recenti",
            description = "Visualizza le ultime transazioni aggiunte con dettagli e categoria.",
            icon = Icons.Default.Repeat,
        ),
    )

    HelpDialogContent(
        isVisible = true,
        title = "Guida Dashboard",
        description = "Benvenuto nel Dashboard! Qui puoi visualizzare il riepilogo finanziario e le transazioni recenti.",
        features = helpFeatures,
        onDismiss = onDismiss,
    )
}