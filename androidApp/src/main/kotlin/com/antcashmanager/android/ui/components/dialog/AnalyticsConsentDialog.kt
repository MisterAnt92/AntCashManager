package com.antcashmanager.android.ui.components.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.text.AppText

/**
 * GDPR analytics consent dialog shown once after the tutorial.
 *
 * Non-dismissible (no onDismissRequest action) so the user must make
 * an explicit choice. Accepts or declines anonymous usage statistics.
 * No financial data is ever sent (enforced by AnalyticsManager whitelist).
 */
@Composable
fun AnalyticsConsentDialog(
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { /* non-dismissible: user must choose */ },
        title = { AppText(text = stringResource(R.string.consent_title)) },
        text = { AppText(text = stringResource(R.string.consent_body)) },
        confirmButton = {
            TextButton(onClick = onAccept) {
                AppText(text = stringResource(R.string.consent_accept))
            }
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                AppText(text = stringResource(R.string.consent_decline))
            }
        },
    )
}
