package com.antcashmanager.android.ui.screen.home.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.animation.AnimatedCard
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.components.text.MoneyText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.ui.theme.ExpenseRed
import com.antcashmanager.android.ui.theme.IncomeGreen
import kotlin.math.abs

/**
 * Card showing the period savings amount, savings rate percentage, and progress toward the savings goal.
 * Only visible when totalIncome > 0.
 */
@Composable
fun SavingsRateCard(
    savingsAmount: Double,
    savingsRate: Double,
    savingsGoalRate: Float,
    modifier: Modifier = Modifier,
) {
    val isAboveGoal = savingsRate >= savingsGoalRate
    val isNegative = savingsAmount < 0
    val accentColor: Color = when {
        isNegative    -> ExpenseRed
        isAboveGoal   -> IncomeGreen
        else          -> MaterialTheme.colorScheme.primary
    }
    val progress = (savingsRate / savingsGoalRate).toFloat().coerceIn(0f, 1f)

    AnimatedCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppText(
                    text = stringResource(R.string.home_savings_rate),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                AppText(
                    text = "${"%.0f".format(abs(savingsRate))}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                )
            }
            VerticalSpacer(SpacingSize.XXS)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppText(
                    text = stringResource(R.string.home_savings_amount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                )
                MoneyText(
                    amount = savingsAmount,
                    color = accentColor,
                    fontWeight = FontWeight.Medium,
                )
            }
            VerticalSpacer(SpacingSize.XXS)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.15f),
            )
            VerticalSpacer(SpacingSize.XXXS)
            AppText(
                text = "${stringResource(R.string.home_savings_goal)}: ${"%.0f".format(savingsGoalRate)}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
            )
        }
    }
}

@Preview(showBackground = true, name = "SavingsRateCard - Light")
@Composable
private fun SavingsRateCardLightPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        SavingsRateCard(savingsAmount = 450.0, savingsRate = 22.5, savingsGoalRate = 20f)
    }
}

@Preview(showBackground = true, name = "SavingsRateCard - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SavingsRateCardDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        SavingsRateCard(savingsAmount = 450.0, savingsRate = 22.5, savingsGoalRate = 20f)
    }
}

@Preview(showBackground = true, name = "SavingsRateCard - 2x", fontScale = 2.0f)
@Composable
private fun SavingsRateCardLargeTextPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        SavingsRateCard(savingsAmount = 450.0, savingsRate = 22.5, savingsGoalRate = 20f)
    }
}
