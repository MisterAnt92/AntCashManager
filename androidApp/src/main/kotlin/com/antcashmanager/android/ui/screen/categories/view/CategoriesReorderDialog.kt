package com.antcashmanager.android.ui.screen.categories.view

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.util.translateCategory
import com.antcashmanager.domain.model.Category

@Composable
fun CategoriesReorderDialog(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (List<Category>) -> Unit,
) {
    var order by remember(categories) { mutableStateOf(categories) }

    fun moveUp(index: Int) {
        if (index <= 0) return
        order = order.toMutableList().apply {
            val item = removeAt(index)
            add(index - 1, item)
        }
    }

    fun moveDown(index: Int) {
        if (index >= order.lastIndex) return
        order = order.toMutableList().apply {
            val item = removeAt(index)
            add(index + 1, item)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { AppText(text = stringResource(R.string.categories_reorder)) },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(order, key = { _, category -> category.id }) { index, category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(category.color)),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        AppText(
                            text = translateCategory(category.name),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = { moveUp(index) },
                            enabled = index > 0,
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = stringResource(R.string.home_move_up),
                            )
                        }
                        IconButton(
                            onClick = { moveDown(index) },
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
            TextButton(onClick = { onConfirm(order) }) {
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

@Preview(name = "CategoriesReorderDialog - Light", showBackground = true)
@Composable
private fun CategoriesReorderDialogPreviewLight() {
    AntCashManagerTheme(dynamicColor = false) {
        CategoriesReorderDialog(
            categories = listOf(
                Category(id = 1, name = "Casa", icon = "home", color = 0xFF4FC3F7, type = "EXPENSE", sortOrder = 0),
                Category(id = 2, name = "Cibo", icon = "restaurant", color = 0xFFE57373, type = "EXPENSE", sortOrder = 1),
                Category(id = 3, name = "Shopping", icon = "shopping_bag", color = 0xFFDCE775, type = "EXPENSE", sortOrder = 2),
            ),
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@Preview(
    name = "CategoriesReorderDialog - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun CategoriesReorderDialogPreviewDark() {
    CategoriesReorderDialogPreviewLight()
}
