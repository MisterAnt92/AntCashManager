package com.antcashmanager.android.ui.screen.settings.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.AppListItem
import com.antcashmanager.android.ui.components.AppRadioButton
import com.antcashmanager.domain.model.DateFormatType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DateFormatDialog(
    currentFormat: String,
    onFormatSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val formats = listOf(
        DateFormatType.DD_MM_YYYY.pattern to stringResource(R.string.date_format_ddmmyyyy),
        DateFormatType.MM_DD_YYYY.pattern to stringResource(R.string.date_format_mmddyyyy),
        DateFormatType.YYYY_MM_DD.pattern to stringResource(R.string.date_format_yyyymmdd),
        DateFormatType.DD_MMM_YYYY.pattern to stringResource(R.string.date_format_ddmmmyyyy),
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null) },
        title = { Text(stringResource(R.string.dialog_choose_date_format)) },
        text = {
            Column {
                formats.forEach { (pattern, label) ->
                    val exampleDate = kotlin.runCatching {
                        SimpleDateFormat(pattern, Locale.getDefault()).format(Date())
                    }.getOrElse { pattern }

                    AppListItem(
                        headlineContent = { Text(label) },
                        supportingContent = { Text(exampleDate) },
                        leadingContent = {
                            AppRadioButton(selected = pattern == currentFormat, onClick = { onFormatSelected(pattern) })
                        },
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) } },
    )
}

