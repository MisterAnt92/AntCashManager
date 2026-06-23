package com.antcashmanager.android.ui.screen.home.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.screen.home.model.HomeTopCardType
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

@Composable
fun HomeTopCardsOrderDialog(
    order: List<HomeTopCardType>,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { AppText(text = stringResource(R.string.home_customize_top_cards_title)) },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(order) { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppText(
                            text = stringResource(item.titleResId),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = { onMoveUp(index) },
                            enabled = index > 0,
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = stringResource(R.string.home_move_up),
                            )
                        }
                        IconButton(
                            onClick = { onMoveDown(index) },
                            enabled = index < order.lastIndex,
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = stringResource(R.string.home_move_down),
                            )
                        }
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

@Preview(name = "HomeTopCardsOrderDialog - Light", showBackground = true)
@Composable
private fun HomeTopCardsOrderDialogPreviewLight() {
    AntCashManagerTheme(dynamicColor = false) {
        HomeTopCardsOrderDialog(
            order = HomeTopCardType.defaultOrder,
            onMoveUp = {},
            onMoveDown = {},
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@Preview(
    name = "HomeTopCardsOrderDialog - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HomeTopCardsOrderDialogPreviewDark() {
    HomeTopCardsOrderDialogPreviewLight()
}
