package com.antcashmanager.android.ui.screen.transactionAdd.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.text.AppText

@Composable
internal fun RecurrenceIntervalDropdown(
    selectedInterval: String,
    onIntervalChange: (String) -> Unit,
) {
    val intervals = listOf("daily", "weekly", "monthly", "yearly")
    var expanded by remember { mutableStateOf(false) }

    // Resolve localized strings upfront
    val dailyLabel = stringResource(R.string.recurrence_interval_daily)
    val weeklyLabel = stringResource(R.string.recurrence_interval_weekly)
    val monthlyLabel = stringResource(R.string.recurrence_interval_monthly)
    val yearlyLabel = stringResource(R.string.recurrence_interval_yearly)

    // Map interval to localized string
    val intervalDisplayName: (String) -> String = { interval ->
        when (interval) {
            "daily" -> dailyLabel
            "weekly" -> weeklyLabel
            "monthly" -> monthlyLabel
            "yearly" -> yearlyLabel
            else -> interval.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Label per il dropdown
        AppText(
            text = stringResource(R.string.recurrence_interval_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Dropdown
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppText(
                    text = if (selectedInterval.isNotBlank()) {
                        intervalDisplayName(selectedInterval)
                    } else {
                        dailyLabel
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                // Icona dropdown
                Icon(
                    imageVector = if (expanded) {
                        Icons.Filled.KeyboardArrowUp
                    } else {
                        Icons.Filled.KeyboardArrowDown
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (expanded) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f),
                ) {
                    intervals.forEach { interval ->
                        DropdownMenuItem(
                            text = {
                                AppText(
                                    text = intervalDisplayName(interval),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            },
                            onClick = {
                                onIntervalChange(interval)
                                expanded = false
                            },
                        )
                    }
                }
            }
        }
    }
}

