package com.antcashmanager.android.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Helper function to determine the display text for the date range
 * based on the selected preset index and actual date range values
 */
@Composable
private fun getRangeDisplayText(
    selectedPresetIndex: Int,
    dateRangeFrom: Long,
    dateRangeTo: Long,
): String {
    val currentTime = System.currentTimeMillis()
    val dayInMillis = 24 * 60 * 60 * 1000L

    // Check if current range matches preset calculations
    val isMatchingPreset = when (selectedPresetIndex) {
        0 -> { // Today
            val expectedFrom = currentTime - dayInMillis
            abs(dateRangeFrom - expectedFrom) < (60 * 60 * 1000) && // Within 1 hour tolerance
                    abs(dateRangeTo - currentTime) < (60 * 60 * 1000)
        }

        1 -> { // Week (7 days)
            val expectedFrom = currentTime - (7 * dayInMillis)
            abs(dateRangeFrom - expectedFrom) < (2 * 60 * 60 * 1000) && // Within 2 hours tolerance
                    abs(dateRangeTo - currentTime) < (2 * 60 * 60 * 1000)
        }

        2 -> { // Month (30 days)
            val expectedFrom = currentTime - (30L * dayInMillis)
            abs(dateRangeFrom - expectedFrom) < (24 * 60 * 60 * 1000) && // Within 1 day tolerance
                    abs(dateRangeTo - currentTime) < (24 * 60 * 60 * 1000)
        }

        3 -> { // Year (365 days)
            val expectedFrom = currentTime - (365L * dayInMillis)
            abs(dateRangeFrom - expectedFrom) < (7L * 24 * 60 * 60 * 1000) && // Within 1 week tolerance
                    abs(dateRangeTo - currentTime) < (7L * 24 * 60 * 60 * 1000)
        }

        else -> false
    }

    return if (isMatchingPreset) {
        when (selectedPresetIndex) {
            0 -> stringResource(R.string.range_label_today)
            1 -> stringResource(R.string.range_label_this_week)
            2 -> stringResource(R.string.range_label_this_month)
            3 -> stringResource(R.string.range_label_this_year)
            else -> stringResource(R.string.charts_period)
        }
    } else {
        // Custom range - format dates
        val shortDateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        val fromDate = shortDateFormat.format(Date(dateRangeFrom))
        val toDate = shortDateFormat.format(Date(dateRangeTo))
        stringResource(R.string.range_label_custom, fromDate, toDate)
    }
}

/**
 * Componente collapsibile per filtrare per intervallo di date.
 * Mostra preset e picker personalizzati con stato di espansione persistente.
 *
 * @param expanded Stato di espansione del componente
 * @param onExpandedChange Callback quando lo stato di espansione cambia
 */
@Composable
fun DateRangeFilter(
    selectedPresetIndex: Int,
    presets: List<Pair<Int, String>>,
    dateRangeFrom: Long,
    dateRangeTo: Long,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onPresetSelected: (Int) -> Unit,
    onFromDateEdit: () -> Unit,
    onToDateEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(300),
        label = "chevron_rotation"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header - Collapsible trigger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        enabled = true,
                        onClickLabel = if (expanded) stringResource(R.string.common_collapse) else stringResource(
                            R.string.common_expand
                        ),
                    ) { onExpandedChange(!expanded) }
                    .semantics {
                        contentDescription = if (expanded) {
                            "Filtro date espanso, tocca per comprimere"
                        } else {
                            "Filtro date compresso, tocca per espandere"
                        }
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = getRangeDisplayText(
                        selectedPresetIndex = selectedPresetIndex,
                        dateRangeFrom = dateRangeFrom,
                        dateRangeTo = dateRangeTo,
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) stringResource(R.string.common_collapse) else stringResource(
                        R.string.common_expand
                    ),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .graphicsLayer(rotationZ = rotationAngle)
                        .size(24.dp),
                )
            }

            // Expandable content
            if (expanded) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    // Preset chips - wrappate e colorate
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        presets.forEachIndexed { index, (labelRes, _) ->
                            FilterChip(
                                selected = selectedPresetIndex == index,
                                onClick = { onPresetSelected(index) },
                                label = {
                                    Text(
                                        text = stringResource(labelRes),
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                shape = RoundedCornerShape(50),
                                colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Custom date range
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = stringResource(
                                R.string.charts_from,
                                dateFormat.format(Date(dateRangeFrom))
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = onFromDateEdit,
                            modifier = Modifier
                                .size(32.dp)
                                .semantics {
                                    contentDescription = "Modifica data inizio: ${
                                        dateFormat.format(
                                            Date(dateRangeFrom)
                                        )
                                    }"
                                },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }

                        Text(
                            text = stringResource(
                                R.string.charts_to,
                                dateFormat.format(Date(dateRangeTo))
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = onToDateEdit,
                            modifier = Modifier
                                .size(32.dp)
                                .semantics {
                                    contentDescription =
                                        "Modifica data fine: ${dateFormat.format(Date(dateRangeTo))}"
                                },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun DateRangeFilterPreview() {
    AntCashManagerTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            // Today preset - collapsed
            DateRangeFilter(
                selectedPresetIndex = 0,
                presets = listOf(
                    R.string.range_label_today to "today",
                    R.string.range_week to "week",
                    R.string.range_month to "month",
                    R.string.range_year to "year",
                ),
                dateRangeFrom = System.currentTimeMillis() - (24 * 60 * 60 * 1000),
                dateRangeTo = System.currentTimeMillis(),
                expanded = false,
                onExpandedChange = {},
                onPresetSelected = {},
                onFromDateEdit = {},
                onToDateEdit = {},
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Week preset - collapsed
            DateRangeFilter(
                selectedPresetIndex = 1,
                presets = listOf(
                    R.string.range_label_today to "today",
                    R.string.range_week to "week",
                    R.string.range_month to "month",
                    R.string.range_year to "year",
                ),
                dateRangeFrom = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000),
                dateRangeTo = System.currentTimeMillis(),
                expanded = false,
                onExpandedChange = {},
                onPresetSelected = {},
                onFromDateEdit = {},
                onToDateEdit = {},
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Custom range - collapsed
            DateRangeFilter(
                selectedPresetIndex = 1, // But with different dates to show custom
                presets = listOf(
                    R.string.range_label_today to "today",
                    R.string.range_week to "week",
                    R.string.range_month to "month",
                    R.string.range_year to "year",
                ),
                dateRangeFrom = System.currentTimeMillis() - (15 * 24 * 60 * 60 * 1000), // 15 days ago
                dateRangeTo = System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000), // 5 days ago
                expanded = false,
                onExpandedChange = {},
                onPresetSelected = {},
                onFromDateEdit = {},
                onToDateEdit = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DateRangeFilterExpandedPreview() {
    AntCashManagerTheme {
        DateRangeFilter(
            selectedPresetIndex = 2,
            presets = listOf(
                R.string.range_label_today to "today",
                R.string.range_week to "week",
                R.string.range_month to "month",
                R.string.range_year to "year",
            ),
            dateRangeFrom = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000),
            dateRangeTo = System.currentTimeMillis(),
            expanded = true,
            onExpandedChange = {},
            onPresetSelected = {},
            onFromDateEdit = {},
            onToDateEdit = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(
    name = "DateRangeFilter - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DateRangeFilterPreviewDark() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        DateRangeFilter(
            selectedPresetIndex = 1,
            presets = listOf(
                R.string.range_label_today to "today",
                R.string.range_week to "week",
                R.string.range_month to "month",
                R.string.range_year to "year",
            ),
            dateRangeFrom = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000),
            dateRangeTo = System.currentTimeMillis(),
            expanded = true,
            onExpandedChange = {},
            onPresetSelected = {},
            onFromDateEdit = {},
            onToDateEdit = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

