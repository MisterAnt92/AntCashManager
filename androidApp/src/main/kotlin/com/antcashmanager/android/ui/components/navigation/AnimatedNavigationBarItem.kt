package com.antcashmanager.android.ui.components.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.theme.LocalReduceMotion

/**
 * Material3 NavigationBarItem con animazione di scale significativa
 * per item selezionati vs non selezionati.
 *
 * SCALE FACTORS:
 * - Selected:   icon 24→32dp (+33%), text 12→14sp (+17%)
 * - Unselected: icon 24→20dp (-17%), text 12→10sp (-17%)
 *
 * ANIMATION:
 * - Duration: 300ms (Material 3 standard)
 * - Easing: FastOutSlowInEasing (natural deceleration)
 * - Reduce Motion: 0ms durata if LocalReduceMotion.current == true
 *
 * ACCESSIBILITY:
 * - Content descriptions always present (icon.contentDescription = label)
 * - Semantics handled by Material3 (Role.Tab auto)
 * - Supports: TalkBack, High Contrast, Font Scaling, Keyboard Nav
 *
 * @param selected Whether this item is currently selected
 * @param onClick Callback when item is clicked
 * @param icon The icon to display
 * @param label The text label for accessibility and display
 * @param modifier Modifier for styling
 * @param enabled Whether this item is enabled
 * @param alwaysShowLabel Whether to always show the label
 * @param colors Color configuration for selected/unselected states
 */
@Composable
fun AnimatedNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    alwaysShowLabel: Boolean = true,
    colors: NavigationBarItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ),
) {
    val reduceMotion = LocalReduceMotion.current

    // Durata: 300ms per movimento fluido, 0ms se reduceMotion abilitato
    val animationDurationMs = if (reduceMotion) 0 else 300

    // Icon size animation (24dp → 32dp for selected, 24dp → 20dp for unselected)
    val iconSize by animateDpAsState(
        targetValue = if (selected) 32.dp else 20.dp,
        animationSpec = tween(
            durationMillis = animationDurationMs,
            easing = FastOutSlowInEasing,
        ),
        label = "nav_item_icon_size",
    )

    // Font size animation (12sp → 14sp for selected, 12sp → 10sp for unselected)
    // Note: Compose non ha animateSpAsState, quindi usiamo animateDpAsState
    // e convertiamo a Sp internamente
    val fontSize by animateDpAsState(
        targetValue = if (selected) 14.dp else 10.dp,
        animationSpec = tween(
            durationMillis = animationDurationMs,
            easing = FastOutSlowInEasing,
        ),
        label = "nav_item_font_size",
    )

    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label, // Accessibility: content description for screen readers
                modifier = Modifier.size(iconSize),
            )
        },
        label = {
            AppText(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = fontSize.value.sp, // Converti Dp a Sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        enabled = enabled,
        alwaysShowLabel = alwaysShowLabel,
        colors = colors,
        modifier = modifier,
    )
}
