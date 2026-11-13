package com.antcashmanager.android.ui.components.layout

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.BuildConfig
import com.antcashmanager.android.R
import com.antcashmanager.android.navigation.BottomNavItem
import com.antcashmanager.android.ui.components.profile.ProfileAvatar
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.domain.model.Profile

/**
 * Sidebar di navigazione laterale sinistra con profilo utente in header.
 *
 * Mostra:
 * - Card profilo attivo (avatar circolare + nome) cliccabile → ProfilesScreen
 * - Voci di navigazione con hover effects e animazioni
 * - Indicatore animato della schermata attiva
 * - Version badge stylizzato
 */
@Composable
fun LeftSidebar(
    selectedRoute: String?,
    onNavigate: (String) -> Unit,
    visibleNavItems: List<BottomNavItem>,
    activeProfile: Profile? = null,
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    onHeaderClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight()
            .clickable(
                enabled = true,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    // Block click propagation to parent (dimming overlay)
                },
            ),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Profile Hero Header (edge-to-edge) ──
            ProfileSidebarHeader(
                activeProfile = activeProfile,
                onProfileClick = onProfileClick,
            )

            // ── Navigation Items ──
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                visibleNavItems.forEach { item ->
                    SidebarMenuItem(
                        item = item,
                        isSelected = selectedRoute == item.route,
                        onClick = { onNavigate(item.route) },
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // ── Footer ──
                SidebarFooter()
            }
        }
    }
}

/**
 * Header della sidebar: banner colorato con avatar che sfora il bordo inferiore.
 */
@Composable
private fun ProfileSidebarHeader(
    activeProfile: Profile?,
    onProfileClick: () -> Unit,
) {
    val bannerColor = if (activeProfile != null) Color(activeProfile.colorArgb)
    else MaterialTheme.colorScheme.primary

    // On-banner text: white su scuri, quasi-nero su chiari
    val onBannerColor = if (bannerColor.luminance() > 0.4f) Color(0xFF1C1B1F) else Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onProfileClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── Banner + avatar in overflow ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp),
        ) {
            // Sfondo colorato
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bannerColor),
            )
            // Avatar centrato, sfora di 33dp verso il basso
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = 33.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (activeProfile != null) {
                    ProfileAvatar(
                        profile = activeProfile,
                        isActive = true,
                        size = 60.dp,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
            }
        }

        // ── Testo profilo (sotto l'avatar) ──
        Spacer(modifier = Modifier.height(40.dp)) // 33dp overflow + 7dp gap
        AppText(
            text = activeProfile?.name ?: stringResource(R.string.profiles_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.height(2.dp))
        AppText(
            text = stringResource(R.string.profiles_section_title),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

/*
 * Original SidebarHeader (ant mascot) — kept but not shown.
 *
 * @Composable
 * private fun SidebarHeader(onHeaderClick: () -> Unit = {}) {
 *     Column(
 *         modifier = Modifier.fillMaxWidth()
 *             .clickable(interactionSource = remember { MutableInteractionSource() },
 *                 indication = null, onClick = onHeaderClick)
 *             .padding(vertical = 16.dp),
 *         horizontalAlignment = Alignment.CenterHorizontally,
 *         verticalArrangement = Arrangement.spacedBy(4.dp),
 *     ) {
 *         Image(painter = painterResource(id = R.drawable.ic_ant_mascot),
 *             contentDescription = stringResource(R.string.app_name),
 *             modifier = Modifier.size(64.dp))
 *         AppText(text = stringResource(R.string.app_name),
 *             style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
 *             color = MaterialTheme.colorScheme.primary)
 *         AppText(text = stringResource(R.string.app_description),
 *             style = MaterialTheme.typography.bodySmall,
 *             color = MaterialTheme.colorScheme.onSurfaceVariant,
 *             textAlign = TextAlign.Center,
 *             modifier = Modifier.fillMaxWidth(0.75f).padding(horizontal = 12.dp),
 *             maxLines = 8, fontWeight = FontWeight.Normal)
 *     }
 * }
 */

/**
 * Voce di menu della sidebar con pill highlight animata.
 */
@Composable
private fun SidebarMenuItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else Color.Transparent,
        label = "menuItemBgColor",
    )

    val contentColor = animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface,
        label = "menuItemContentColor",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color = backgroundColor.value)
            .clickable(
                enabled = true,
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
            )
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = stringResource(item.titleResId),
            modifier = Modifier.size(22.dp),
            tint = contentColor.value,
        )
        AppText(
            text = stringResource(item.titleResId),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor.value,
        )
        // Ant mascot indicator (preserved, hidden):
        // if (isSelected) { Spacer(Modifier.weight(1f))
        //   Icon(painter=painterResource(R.drawable.ic_ant_mascot), Modifier.size(20.dp)) }
    }
}

/**
 * Footer della sidebar: versione centrata.
 */
@Composable
private fun SidebarFooter() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        AppText(
            text = "v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium,
        )
    }
}

private val fakeProfile = Profile(
    id = 1L,
    name = "Mario Rossi",
    uuid = "preview",
    createdAt = 0L,
    isActive = true,
    colorArgb = 0xFF6650A4L,
    avatarEmoji = "👤",
)

@Preview(showBackground = true, name = "LeftSidebar - Light")
@Composable
private fun LeftSidebarLightPreview() {
    AntCashManagerTheme(dynamicColor = false) {
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
            activeProfile = fakeProfile,
        )
    }
}

@Preview(showBackground = true, name = "LeftSidebar - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LeftSidebarDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
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
            activeProfile = fakeProfile,
        )
    }
}

@Preview(showBackground = true, name = "LeftSidebar - 2x", fontScale = 2.0f)
@Composable
private fun LeftSidebarLargeTextPreview() {
    AntCashManagerTheme(dynamicColor = false) {
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
            activeProfile = fakeProfile,
        )
    }
}
