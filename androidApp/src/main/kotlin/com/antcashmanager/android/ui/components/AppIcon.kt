package com.antcashmanager.android.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * AppIcon - composable icon riutilizzabile con valori di default coerenti al tema
 * Parametri principali:
 * - painter: Painter per drawable resources
 * - imageVector: ImageVector per vettori
 * - contentDescription: descrizione per accessibilità
 * - tint: colore dell'icona (default MaterialTheme.colorScheme.primary)
 * - size: dimensione dell'icona (default 24.dp)
 * - modifier: modificatore aggiuntivo
 */
@Composable
fun AppIcon(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    imageVector: ImageVector? = null,
    contentDescription: String? = null,
    tint: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 24.dp
) {
    require(painter != null || imageVector != null) { "Either painter or imageVector must be provided" }

    Icon(
        painter = painter
            ?: rememberVectorPainter(imageVector!!),
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier.then(Modifier.size(size))
    )
}

@Preview(name = "AppIcon - Light", showBackground = true)
@Composable
private fun AppIconPreviewLight() {
    AppIcon(
        painter = painterResource(id = android.R.drawable.ic_menu_search),
        contentDescription = "Search Icon"
    )
}

@Preview(name = "AppIcon - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppIconPreviewDark() {
    AppIcon(
        painter = painterResource(id = android.R.drawable.ic_menu_search),
        contentDescription = "Search Icon"
    )
}

@Preview(name = "AppIcon with Painter", showBackground = true)
@Composable
private fun AppIconPreviewWithPainter() {
    AppIcon(
        painter = painterResource(id = android.R.drawable.ic_menu_search),
        contentDescription = "Search Icon from Painter"
    )
}
