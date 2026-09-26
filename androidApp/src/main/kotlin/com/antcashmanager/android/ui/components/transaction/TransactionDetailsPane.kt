package com.antcashmanager.android.ui.components.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.ui.mapper.labelRes
import com.antcashmanager.android.util.LocalAmountsMasked
import com.antcashmanager.android.util.LocalCurrencyFormat
import com.antcashmanager.android.util.formatAmount
import com.antcashmanager.android.util.isProtectedSalaryTransaction
import com.antcashmanager.android.util.isValidNote
import com.antcashmanager.android.util.maskDigits
import com.antcashmanager.android.util.translateCategory
import com.antcashmanager.domain.model.Transaction

/**
 * Right pane of the split layout on foldables/tablets: details of the selected transaction,
 * or an empty hint when nothing is selected. Shared by Home and Transactions screens.
 */
@Composable
fun TransactionDetailsPane(
    transaction: Transaction?,
    modifier: Modifier = Modifier,
) {
    if (transaction == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AppText(
                text = stringResource(R.string.transactions_details_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val amountText = formatAmount(transaction.amount, LocalCurrencyFormat.current)
    val masked = LocalAmountsMasked.current && isProtectedSalaryTransaction(transaction)

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppText(
            text = stringResource(R.string.transaction_details_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        AppText(
            text = transaction.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        AppText(
            text = "${translateCategory(transaction.category)} • ${stringResource(transaction.type.labelRes())}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AppText(
            text = if (masked) maskDigits(amountText) else amountText,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        if (transaction.notes.isValidNote()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            AppText(
                text = stringResource(R.string.transaction_details_notes),
                style = MaterialTheme.typography.labelMedium,
            )
            AppText(
                text = transaction.notes,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Preview(showBackground = true, name = "TransactionDetailsPane - Light")
@Composable
private fun TransactionDetailsPaneLightPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        TransactionDetailsPane(
            transaction = Transaction(
                id = 1L,
                amount = 50.0,
                type = com.antcashmanager.domain.model.TransactionType.EXPENSE,
                title = "Caffè",
                category = "Food",
                timestamp = System.currentTimeMillis(),
                notes = "",
                mealVoucherDifference = 0.0,
            )
        )
    }
}

@Preview(showBackground = true, name = "TransactionDetailsPane - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TransactionDetailsPaneDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        TransactionDetailsPane(
            transaction = Transaction(
                id = 1L,
                amount = 150.50,
                type = com.antcashmanager.domain.model.TransactionType.INCOME,
                title = "Stipendio",
                category = "Salary",
                timestamp = System.currentTimeMillis(),
                notes = "Stipendio mensile",
                mealVoucherDifference = 0.0,
            )
        )
    }
}

@Preview(showBackground = true, name = "TransactionDetailsPane - 2x", fontScale = 2.0f)
@Composable
private fun TransactionDetailsPaneLargeTextPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        TransactionDetailsPane(
            transaction = Transaction(
                id = 1L,
                amount = 75.25,
                type = com.antcashmanager.domain.model.TransactionType.EXPENSE,
                title = "Cena",
                category = "Food",
                timestamp = System.currentTimeMillis(),
                notes = "Con colleghi",
                mealVoucherDifference = 0.0,
            )
        )
    }
}
