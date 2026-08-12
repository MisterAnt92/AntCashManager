package com.antcashmanager.android.ui.components.layout

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.layout.HorizontalSpacer
import com.antcashmanager.android.R
import com.antcashmanager.android.navigation.BottomNavItem
import com.antcashmanager.android.ui.components.text.AppText

/**
 * Sidebar di navigazione laterale sinistra con UX migliorata.
 *
 * Mostra:
 * - Logo della formichina + nome app in header card elegante
 * - Voci di navigazione con hover effects e animazioni
 * - Indicatore animato della schermata attiva
 * - Version badge stylizzato
 *
 * Caratteristiche:
 * - Animazioni fluide su selezione e hover
 * - Responsive: si adatta alle diverse larghezze di schermo
 * - Dark mode: automaticamente supportato
 * - Feedback tattile con ripple effect
 */
@Composable
fun LeftSidebar(
    selectedRoute: String?,
    onNavigate: (String) -> Unit,
    visibleNavItems: List<BottomNavItem>,
    modifier: Modifier = Modifier,
    onHeaderClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Header Card: Logo + App Name ──
            SidebarHeader(onHeaderClick = onHeaderClick)

            // ── Navigation Items ──
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                visibleNavItems.forEach { item ->
                    SidebarMenuItem(
                        item = item,
                        isSelected = selectedRoute == item.route,
                        onClick = { onNavigate(item.route) },
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // ── Footer Info ──
                SidebarFooter()
            }
        }
    }
}

/**
 * Header della sidebar con launcher icon e nome app (trasparente).
 */
@Composable
private fun SidebarHeader(onHeaderClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxHeight(0.18f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onHeaderClick,
            )
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Launcher Icon (quadrato Google Play style)
        Image(
            painter = painterResource(id = R.drawable.ic_ant_mascot),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.size(64.dp),
        )

        VerticalSpacer(SpacingSize.SM)

        // App Name
        AppText(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * Voce di menu della sidebar con animazioni e feedback tattile.
 */
@Composable
private fun SidebarMenuItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    // Animazioni
    val backgroundColor = animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            androidx.compose.material3.LocalContentColor.current.copy(alpha = 0f),
        label = "menuItemBgColor",
    )

    val textColor = animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.onSurface,
        label = "menuItemTextColor",
    )

    val iconScale = animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        label = "menuItemIconScale",
    )

    val borderOpacity = animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        label = "menuItemBorderOpacity",
    )

    Row(
        modifier = Modifier
            .height(56.dp)
            .clickable(
                enabled = true,
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
            )
            .background(
                color = backgroundColor.value,
                shape = RoundedCornerShape(14.dp),
            )
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left Border Indicator (animato)
        Spacer(
            modifier = Modifier
                .width(4.dp)
                .height(32.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(
                        alpha = borderOpacity.value,
                    ),
                    shape = RoundedCornerShape(2.dp),
                )
        )

        // Icon with scale animation
        Icon(
            imageVector = item.icon,
            contentDescription = stringResource(item.titleResId),
            modifier = Modifier
                .size(24.dp)
                .width(24.dp * iconScale.value),
            tint = textColor.value,
        )

        // Label with color animation
        AppText(
            text = stringResource(item.titleResId),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor.value,
        )

        // Animated ant indicator (small mascot)
        if (isSelected) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                painter = painterResource(id = R.drawable.ic_ant_mascot),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = androidx.compose.material3.LocalContentColor.current,
            )
        }
    }
}

/**
 * Footer della sidebar con version badge stylizzato.
 */
@Composable
private fun SidebarFooter() {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Elegant divider line
        Surface(
            modifier = Modifier
                .fillMaxHeight(0.001f)
                .height(1.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        ) {}

        VerticalSpacer(SpacingSize.MD)

        // Version badge (transparent)
        AppText(
            text = "v1.6.3",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * Preview della sidebar per lo sviluppo.
 */
@Composable
private fun SidebarPreview() {
    LeftSidebar(
        selectedRoute = "home",
        onNavigate = {},
        visibleNavItems = listOf(
            BottomNavItem.Home,
            BottomNavItem.Charts,
            BottomNavItem.Transactions,
            BottomNavItem.Categories,
            BottomNavItem.Settings,
        ),
    )
}
