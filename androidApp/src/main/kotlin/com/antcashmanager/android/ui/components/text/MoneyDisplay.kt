package com.antcashmanager.android.ui.components.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.ui.theme.ExpenseRed
import com.antcashmanager.android.ui.theme.IncomeGreen
import com.antcashmanager.android.util.LocalCurrencyFormat
import com.antcashmanager.android.util.formatAmountWithNegative
import com.antcashmanager.android.util.formatTransactionAmount

/**
 * Reusable composable for displaying monetary amounts with customizable styling.
 * Handles negative values correctly with sign before currency symbol.
 *
 * @param amount The monetary value to display
 * @param modifier Modifier for the Text composable
 * @param style TextStyle for customization (font, size, etc.)
 * @param color Color override for the text
 * @param fontWeight Font weight override
 * @param fontSize Font size override in sp
 * @param showSign Whether to show +/- prefix (default: false for balance, true for transactions)
 */
@Composable
fun MoneyText(
    amount: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    fontSize: Int? = null,
    showSign: Boolean = false,
) {
    val fmt = LocalCurrencyFormat.current

    val formattedAmount = if (showSign) {
        formatTransactionAmount(amount, fmt)
    } else {
        formatAmountWithNegative(amount, fmt)
    }

    val finalStyle = style.let {
        var updated = it
        if (fontWeight != null) {
            updated = updated.copy(fontWeight = fontWeight)
        }
        if (fontSize != null) {
            updated = updated.copy(fontSize = fontSize.sp)
        }
        updated
    }

    Text(
        text = formattedAmount,
        modifier = modifier,
        style = finalStyle,
        color = color,
    )
}

/**
 * Specialized MoneyText for displaying balance values.
 * Automatically colors positive amounts in green and negative in red.
 *
 * @param amount The balance to display
 * @param modifier Modifier for styling
 * @param style TextStyle for the text
 * @param fontWeight Font weight (default: Bold)
 * @param fontSize Font size in sp (default: 24)
 */
@Composable
fun BalanceText(
    amount: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineLarge,
    fontWeight: FontWeight = FontWeight.Bold,
    fontSize: Int = 24,
) {
    val color = if (amount >= 0) IncomeGreen else ExpenseRed

    MoneyText(
        amount = amount,
        modifier = modifier,
        style = style,
        color = color,
        fontWeight = fontWeight,
        fontSize = fontSize,
        showSign = false,
    )
}

/**
 * Specialized MoneyText for displaying transaction amounts with +/- sign.
 * Automatically colors based on the sign (+ green, - red).
 *
 * @param amount The transaction amount (positive for income, negative for expense)
 * @param modifier Modifier for styling
 * @param style TextStyle for the text
 * @param fontWeight Font weight (default: ExtraBold)
 * @param fontSize Font size in sp (default: 14)
 */
@Composable
fun TransactionAmountText(
    amount: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelMedium,
    fontWeight: FontWeight = FontWeight.ExtraBold,
    fontSize: Int = 14,
) {
    val color = if (amount >= 0) IncomeGreen else ExpenseRed

    MoneyText(
        amount = amount,
        modifier = modifier,
        style = style,
        color = color,
        fontWeight = fontWeight,
        fontSize = fontSize,
        showSign = true,
    )
}

/**
 * Compact MoneyText for inline display (e.g., in lists, summaries).
 * Uses smaller font size and no special styling.
 *
 * @param amount The amount to display
 * @param modifier Modifier for styling
 * @param fontWeight Font weight (default: SemiBold)
 * @param fontSize Font size in sp (default: 12)
 */
@Composable
fun CompactMoneyText(
    amount: Double,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.SemiBold,
    fontSize: Int = 12,
) {
    MoneyText(
        amount = amount,
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = fontWeight,
        fontSize = fontSize,
        showSign = false,
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// PREVIEWS
// ══════════════════════════════════════════════════════════════════════════════

@Preview(showBackground = true, name = "MoneyText - Positive")
@Composable
private fun MoneyTextPositivePreview() {
    AntCashManagerTheme(dynamicColor = false) {
        MoneyText(
            amount = 1234.56,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(showBackground = true, name = "MoneyText - Negative")
@Composable
private fun MoneyTextNegativePreview() {
    AntCashManagerTheme(dynamicColor = false) {
        MoneyText(
            amount = -1234.56,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(showBackground = true, name = "BalanceText - Positive")
@Composable
private fun BalanceTextPositivePreview() {
    AntCashManagerTheme(dynamicColor = false) {
        BalanceText(amount = 5432.10)
    }
}

@Preview(showBackground = true, name = "BalanceText - Negative")
@Composable
private fun BalanceTextNegativePreview() {
    AntCashManagerTheme(dynamicColor = false) {
        BalanceText(amount = -500.00)
    }
}

@Preview(showBackground = true, name = "TransactionAmountText - Income")
@Composable
private fun TransactionAmountTextIncomePreview() {
    AntCashManagerTheme(dynamicColor = false) {
        TransactionAmountText(amount = 2500.00)
    }
}

@Preview(showBackground = true, name = "TransactionAmountText - Expense")
@Composable
private fun TransactionAmountTextExpensePreview() {
    AntCashManagerTheme(dynamicColor = false) {
        TransactionAmountText(amount = -85.50)
    }
}

@Preview(showBackground = true, name = "CompactMoneyText")
@Composable
private fun CompactMoneyTextPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        CompactMoneyText(amount = 12345.67)
    }
}

@Preview(showBackground = true, name = "All MoneyText Variants - Dark")
@Composable
private fun AllMoneyTextPreviewDark() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("MoneyText (Positive):")
            MoneyText(amount = 1234.56)

            Text("MoneyText (Negative):")
            MoneyText(amount = -1234.56)

            Text("BalanceText:")
            BalanceText(amount = 5432.10)

            Text("TransactionAmountText:")
            TransactionAmountText(amount = 2500.00)

            Text("CompactMoneyText:")
            CompactMoneyText(amount = 12345.67)
        }
    }
}

