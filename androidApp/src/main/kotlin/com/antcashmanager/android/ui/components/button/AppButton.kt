package com.antcashmanager.android.ui.components.button

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.text.AppText
import androidx.compose.material3.Text

/**
 * Custom button allineato ad [AppStyles.button].
 *
 * - I colori e la forma default vengono da [AppStyles.button]
 * - icon: Int = 0 -> nessuna icona
 * - optional shape, elevation e contentPadding per personalizzazione
 */
@Composable
fun AppButton(
    text: String = "",
    modifier: Modifier = Modifier,
    icon: Int = 0,
    onClick: () -> Unit,
    buttonColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    textColor: Color = Color.Unspecified,
    // optional customization params
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    elevation: ButtonElevation? = null,
    textStyle: TextStyle? = null,
    iconSize: Dp? = null,
    // optional tint to apply to the icon; when null icon is rendered as-is
    iconTint: Color? = null,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    useDefaultSize: Boolean = true,
    content: (@Composable () -> Unit)? = null,
) {
    // Animazione di scala al press
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        label = "buttonPressScale",
    )

    val resolvedElevation = elevation ?: ButtonDefaults.buttonElevation(
        defaultElevation = 4.dp,
        pressedElevation = 8.dp,
    )
    val resolvedIconSize = iconSize ?: 24.dp
    val resolvedTextStyle = textStyle ?: androidx.compose.material3.MaterialTheme.typography.labelLarge

    // Clean text
    val cleanedText = text.trim().replace(Regex("\\s+"), " ")

    val sizeModifier = if (useDefaultSize) {
        modifier
            .height(48.dp)
            .defaultMinSize(minWidth = 90.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
    } else modifier.graphicsLayer { scaleX = scale; scaleY = scale }

    Button(
        onClick = onClick,
        modifier = sizeModifier,
        shape = shape,
        elevation = resolvedElevation,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (buttonColor != Color.Unspecified) buttonColor else ButtonDefaults.buttonColors().containerColor,
            contentColor = if (contentColor != Color.Unspecified) contentColor else ButtonDefaults.buttonColors().contentColor,
        ),
        enabled = enabled,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    ) {
        if (content != null) {
            content()
        } else {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != 0) {
                    Image(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(resolvedIconSize),
                        // apply tint if provided
                        colorFilter = iconTint?.let { ColorFilter.tint(it) }
                    )
                }

                AppText(
                    text = cleanedText,
                    style = resolvedTextStyle,
                    color = if (textColor != Color.Unspecified) textColor else Color.Unspecified,
                    modifier = Modifier.padding(start = if (icon != 0) 8.dp else 0.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppButtonPreviews() {
    Column {
        // Default button
        AppButton(text = "Default Button", onClick = {})

        Spacer(modifier = Modifier.height(8.dp))

        // Button with icon (assuming icon resource exists, e.g., android.R.drawable.ic_menu_add)
        AppButton(
            text = "With Icon",
            icon = android.R.drawable.ic_menu_add,
            onClick = {})

        Spacer(modifier = Modifier.height(8.dp))

        // Disabled button
        AppButton(text = "Disabled", enabled = false, onClick = {})

        Spacer(modifier = Modifier.height(8.dp))

        // Custom colors
        AppButton(
            text = "Custom Color",
            buttonColor = Color.Red,
            textColor = Color.White,
            onClick = {}
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Text only, no default size
        AppButton(
            text = "No Default Size",
            useDefaultSize = false,
            onClick = {})

        Spacer(modifier = Modifier.height(8.dp))

        // With custom content
        AppButton(onClick = {}) {
            AppText(text = "Custom Content")
        }
    }
}

// ---------------------------------------------------------------------------
// Previews AppStyles.button – varianti colore
// ---------------------------------------------------------------------------

@Preview(name = "AppButton Styles – Light", showBackground = true, widthDp = 360)
@Composable
private fun AppButtonStylesPreviewLight() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppButton(
            text = "Primario",
            buttonColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
            textColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
            onClick = {},
        )
        AppButton(
            text = "Secondario",
            buttonColor = androidx.compose.material3.MaterialTheme.colorScheme.secondary,
            textColor = androidx.compose.material3.MaterialTheme.colorScheme.onSecondary,
            onClick = {},
        )
        AppButton(
            text = "Pericolo / Errore",
            buttonColor = androidx.compose.material3.MaterialTheme.colorScheme.error,
            textColor = androidx.compose.material3.MaterialTheme.colorScheme.onError,
            onClick = {},
        )
        AppButton(text = "Disabilitato", enabled = false, onClick = {})
    }
}

@Preview(
    name = "AppButton Styles – Dark",
    showBackground = true,
    widthDp = 360,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun AppButtonStylesPreviewDark() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppButton(
            text = "Primario",
            buttonColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
            textColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
            onClick = {},
        )
        AppButton(
            text = "Secondario",
            buttonColor = androidx.compose.material3.MaterialTheme.colorScheme.secondary,
            textColor = androidx.compose.material3.MaterialTheme.colorScheme.onSecondary,
            onClick = {},
        )
        AppButton(
            text = "Pericolo / Errore",
            buttonColor = androidx.compose.material3.MaterialTheme.colorScheme.error,
            textColor = androidx.compose.material3.MaterialTheme.colorScheme.onError,
            onClick = {},
        )
        AppButton(text = "Disabilitato", enabled = false, onClick = {})
    }
}
