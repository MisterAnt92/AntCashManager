package com.antcashmanager.android.ui.screen.charts.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.button.ReorderButtons
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.charts.model.ChartCardType

/**
 * Dialog to reorder chart cards in Charts Screen.
 * Allows users to customize the display order of all personalizable cards.
 *
 * Accessibility:
 * - All buttons have contentDescription
 * - Up/Down buttons disabled appropriately (first/last item)
 * - Semantic structure for screen readers
 */
@Composable
fun ChartsCardsOrderDialog(
    order: List<ChartCardType>,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { AppText(text = stringResource(R.string.charts_customize_cards_title)) },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(order) { index, item ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Card title
                        AppText(
                            text = stringResource(item.titleResId),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )

                        // Reorder buttons (up/down)
                        ReorderButtons(
                            onMoveUp = { onMoveUp(index) },
                            onMoveDown = { onMoveDown(index) },
                            canMoveUp = index > 0,
                            canMoveDown = index < order.lastIndex,
                            upDescription =
                                stringResource(
                                    R.string.home_move_up,
                                    stringResource(item.titleResId),
                                ),
                            downDescription =
                                stringResource(
                                    R.string.home_move_down,
                                    stringResource(item.titleResId),
                                ),
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                AppText(stringResource(R.string.common_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                AppText(stringResource(R.string.common_cancel))
            }
        },
    )
}
