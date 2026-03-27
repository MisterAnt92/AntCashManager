package com.antcashmanager.android.ui.components.text

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R

/**
 * AppText - composable testuale riutilizzabile con valori di default coerenti al tema.
 *
 * Usa AppStyles.text per scegliere lo stile appropriato al contesto:
 * - Titoli di sezione screen   → AppStyles.text.sectionTitle
 * - Titoli di screen           → AppStyles.text.screenTitle
 * - Titoli di card             → AppStyles.text.cardTitle
 * - Sotto-titoli di card       → AppStyles.text.cardSubtitle
 * - Corpo testo                → AppStyles.text.body (default)
 * - Corpo secondario           → AppStyles.text.bodySecondary
 * - Label / etichette          → AppStyles.text.label
 * - Label piccole / note       → AppStyles.text.labelSmall
 * - Testo pulsante             → AppStyles.text.button
 * - Messaggi di errore         → AppStyles.text.error
 *
 * Parametri principali:
 * - text: il testo da visualizzare
 * - style: TextStyle di base (default MaterialTheme.typography.bodyMedium)
 * - color: colore del testo (default prende il LocalContentColor se non specificato)
 * - fontWeight: opzionale per sovrascrivere il peso
 * - textAlign: opzionale per l'allineamento
 * - maxLines, overflow, modifier
 * - icon: icona opzionale da mostrare accanto al testo
 * - iconPosition: posizione dell'icona (Left o Right, default Left)
 * - iconSize: dimensione opzionale dell'icona (default 24.dp)
 * - iconPadding: padding opzionale tra icona e testo (default 8.dp)
 * - onClick: callback opzionale per rendere il testo cliccabile
 * - allCaps: se true, trasforma il testo in maiuscolo
 */
enum class IconPosition {
    Left, Right
}

@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color? = null,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    icon: Painter? = null,
    iconPosition: IconPosition = IconPosition.Left,
    iconSize: Dp? = null,
    iconPadding: Dp? = null,
    onClick: (() -> Unit)? = null,
    allCaps: Boolean = false,
) {
    val finalText = if (allCaps) text.uppercase() else text
    val finalStyle = if (fontWeight != null) style.copy(fontWeight = fontWeight) else style
    val finalColor = color ?: LocalContentColor.current
    val defaultIconSize = 24.dp
    val defaultIconPadding = 8.dp

    if (icon != null) {
        Row(
            modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconPosition == IconPosition.Left) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = finalColor,
                    modifier = Modifier
                        .size(iconSize ?: defaultIconSize)
                )
                Spacer(modifier = Modifier.width(iconPadding ?: defaultIconPadding))
                Text(
                    text = finalText,
                    style = finalStyle,
                    color = finalColor,
                    textAlign = textAlign,
                    maxLines = maxLines,
                    overflow = overflow
                )
            } else {
                Text(
                    text = finalText,
                    style = finalStyle,
                    color = finalColor,
                    textAlign = textAlign,
                    maxLines = maxLines,
                    overflow = overflow
                )
                Spacer(modifier = Modifier.width(iconPadding ?: 0.dp))
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = finalColor,
                    modifier = Modifier.size(iconSize ?: defaultIconSize)
                )
            }
        }
    } else {
        Text(
            text = finalText,
            modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
            style = finalStyle,
            color = finalColor,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow
        )
    }
}



@Preview(name = "AppText - Light", showBackground = true)
@Composable
private fun AppTextPreviewLight() {
    AppText(
        text = "Titolo di Esempio",
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center
    )
}

@Preview(name = "AppText - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppTextPreviewDark() {
    AppText(
        text = "Titolo Scuro",
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Preview(name = "AppText Clickable", showBackground = true)
@Composable
private fun AppTextPreviewClickable() {
    AppText(
        text = "Testo Cliccabile",
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyLarge,
        onClick = { /* Do something */ }
    )
}

@Preview(name = "AppText All Caps", showBackground = true)
@Composable
private fun AppTextPreviewAllCaps() {
    AppText(
        text = "this text is in all caps",
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyLarge,
        allCaps = true
    )
}


