package com.antcashmanager.android.ui.components.navigation

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import com.antcashmanager.android.ui.components.layout.rememberAdaptiveLayoutInfo
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

/**
 * Enhanced NavigationRail for foldable devices and tablets.
 *
 * Improvements over standard NavigationRail:
 * - Always show labels on medium/expanded screens (no icon-only mode)
 * - Larger touch targets (48dp minimum) for better usability on tablets
 * - Improved spacing for visual hierarchy
 * - Better label visibility with responsive typography
 *
 * @param modifier Modifier for the rail
 * @param items List of navigation items with labels and icons
 * @param selectedItem Index of the currently selected item
 * @param onItemSelected Callback when item is selected
 */
@Composable
fun NavigationRailTablet(
    items: List<NavigationRailItem>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val adaptiveInfo = rememberAdaptiveLayoutInfo()
    val showLabels = adaptiveInfo.isMedium || adaptiveInfo.isExpanded

    NavigationRail(
        modifier =
            modifier
                .fillMaxHeight()
                .padding(vertical = 8.dp),
    ) {
        items.forEachIndexed { index, item ->
            NavigationRailItem(
                selected = index == selectedItem,
                onClick = { onItemSelected(index) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp),
                    )
                },
                label =
                    if (showLabels) {
                        {
                            AppText(
                                text = item.label,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 2,
                            )
                        }
                    } else {
                        null
                    },
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }
}

/**
 * Data class for navigation rail items.
 *
 * @param label Display label for the item
 * @param icon Icon vector to display
 * @param badge Optional badge count (e.g., for notifications)
 */
data class NavigationRailItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val badge: Int = 0,
)

/**
 * Optimized vertical spacing for tablet navigation rails.
 *
 * Returns different spacing based on screen size:
 * - Compact (phones): 4.dp between items
 * - Medium (7" tablets): 8.dp between items
 * - Expanded (10"+ tablets): 12.dp between items
 */
@Composable
fun getNavigationRailSpacing(): androidx.compose.ui.unit.Dp {
    val adaptiveInfo = rememberAdaptiveLayoutInfo()
    return when {
        adaptiveInfo.isExpanded -> 12.dp
        adaptiveInfo.isMedium -> 8.dp
        else -> 4.dp
    }
}

@Preview(showBackground = true, name = "NavigationRailTablet - Light", widthDp = 100, heightDp = 600)
@Composable
private fun NavigationRailTabletLightPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        NavigationRailTablet(
            items = listOf(
                NavigationRailItem(label = "Home", icon = Icons.Default.Home),
                NavigationRailItem(label = "Charts", icon = Icons.Default.BarChart),
                NavigationRailItem(label = "Transactions", icon = Icons.Default.List),
            ),
            selectedItem = 0,
            onItemSelected = {},
        )
    }
}

@Preview(showBackground = true, name = "NavigationRailTablet - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 100, heightDp = 600)
@Composable
private fun NavigationRailTabletDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        NavigationRailTablet(
            items = listOf(
                NavigationRailItem(label = "Home", icon = Icons.Default.Home),
                NavigationRailItem(label = "Charts", icon = Icons.Default.BarChart),
                NavigationRailItem(label = "Transactions", icon = Icons.Default.List),
            ),
            selectedItem = 0,
            onItemSelected = {},
        )
    }
}

@Preview(showBackground = true, name = "NavigationRailTablet - 2x", fontScale = 2.0f, widthDp = 100, heightDp = 600)
@Composable
private fun NavigationRailTabletLargeTextPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        NavigationRailTablet(
            items = listOf(
                NavigationRailItem(label = "Home", icon = Icons.Default.Home),
                NavigationRailItem(label = "Charts", icon = Icons.Default.BarChart),
                NavigationRailItem(label = "Transactions", icon = Icons.Default.List),
            ),
            selectedItem = 0,
            onItemSelected = {},
        )
    }
}
