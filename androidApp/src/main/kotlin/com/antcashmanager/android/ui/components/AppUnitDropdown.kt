package com.antcashmanager.android.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.ui.theme.AntCashManagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUnitDropdown(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    selectedValue: String,
    label: String,
    onValueChange: (String) -> Unit,
    menuItems: List<String>,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        TextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = shape,
            modifier = modifier
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            menuItems.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit) },
                    onClick = {
                        onValueChange(unit)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppUnitDropdownPreview() {
    AntCashManagerTheme {
        var expanded by remember { mutableStateOf(false) }
        var selected by remember { mutableStateOf("mm") }

        AppUnitDropdown(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            selectedValue = selected,
            label = "Length Unit",
            onValueChange = { selected = it; expanded = false },
            menuItems = listOf("mm", "cm", "in")
        )
    }
}
