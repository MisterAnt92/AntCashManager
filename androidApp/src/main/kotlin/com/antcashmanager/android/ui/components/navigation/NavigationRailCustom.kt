package com.antcashmanager.android.ui.components.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.components.layout.rememberAdaptiveLayoutInfo
import com.antcashmanager.android.ui.components.text.AppText

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
        modifier = modifier
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
                label = if (showLabels) {
                    {
                        AppText(
                            text = item.label,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 2,
                        )
                    }
                } else null,
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
