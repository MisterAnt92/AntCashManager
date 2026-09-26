package com.antcashmanager.android.ui.screen.categories.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddCategoryDialog(
    currentType: String,
    onConfirm: (String, String, Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableLongStateOf(categoryColors.first()) }
    var selectedIcon by remember { mutableStateOf("category") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { AppText(stringResource(R.string.categories_add)) },
        text = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
            ) {
                // Category Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { AppText(stringResource(R.string.categories_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                VerticalSpacer(SpacingSize.MD)

                // Icon Selection
                AppText(
                    text = stringResource(R.string.categories_icon_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                VerticalSpacer(SpacingSize.XS)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    categoryIconMap.forEach { (iconKey, iconVector) ->
                        val isSelected = iconKey == selectedIcon
                        Box(
                            modifier =
                                Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        },
                                    ).then(
                                        if (isSelected) {
                                            Modifier.border(
                                                2.dp,
                                                MaterialTheme.colorScheme.primary,
                                                CircleShape,
                                            )
                                        } else {
                                            Modifier
                                        },
                                    ).selectable(
                                        selected = isSelected,
                                        onClick = { selectedIcon = iconKey },
                                        role = Role.RadioButton,
                                    ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = getIconContentDescription(iconKey),
                                tint =
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
                VerticalSpacer(SpacingSize.MD)

                // Color Selection
                AppText(
                    text = stringResource(R.string.categories_color_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                VerticalSpacer(SpacingSize.XS)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    categoryColors.forEach { color ->
                        Box(
                            modifier =
                                Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(color))
                                    .then(
                                        if (color == selectedColor) {
                                            Modifier.border(
                                                3.dp,
                                                MaterialTheme.colorScheme.primary,
                                                CircleShape,
                                            )
                                        } else {
                                            Modifier
                                        },
                                    ).clickable { selectedColor = color },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (color == selectedColor) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = stringResource(R.string.categories_selected),
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            name.trim(),
                            selectedIcon,
                            selectedColor,
                        )
                    }
                },
                enabled = name.isNotBlank(),
            ) {
                AppText(stringResource(R.string.dialog_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { AppText(stringResource(R.string.common_cancel)) }
        },
    )
}

@Preview(showBackground = true, name = "AddCategoryDialog - Light")
@Composable
private fun AddCategoryDialogLightPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        AddCategoryDialog(
            currentType = "EXPENSE",
            onConfirm = { _, _, _ -> },
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true, name = "AddCategoryDialog - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AddCategoryDialogDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        AddCategoryDialog(
            currentType = "EXPENSE",
            onConfirm = { _, _, _ -> },
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true, name = "AddCategoryDialog - 2x", fontScale = 2.0f)
@Composable
private fun AddCategoryDialogLargeTextPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        AddCategoryDialog(
            currentType = "EXPENSE",
            onConfirm = { _, _, _ -> },
            onDismiss = {},
        )
    }
}
