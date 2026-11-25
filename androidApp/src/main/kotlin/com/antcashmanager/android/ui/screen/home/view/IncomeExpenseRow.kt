package com.antcashmanager.android.ui.screen.home.view

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.animation.AnimatedCard
import com.antcashmanager.android.ui.components.animation.FadeInOnAppear
import com.antcashmanager.android.ui.components.layout.SpacingSize
import com.antcashmanager.android.ui.components.layout.VerticalSpacer
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.components.text.CompactMoneyText
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.antcashmanager.android.ui.screen.charts.TrendDirection
import com.antcashmanager.android.ui.screen.home.HomeConstant
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.ui.theme.ExpenseRed
import com.antcashmanager.android.ui.theme.IncomeGreen
import kotlin.math.abs

@Composable
fun IncomeExpenseRow(
    totalIncome: Double,
    totalExpense: Double,
    modifier: Modifier = Modifier,
    incomeDeltaPercent: Double? = null,
    expenseDeltaPercent: Double? = null,
) {
    FadeInOnAppear(durationMillis = HomeConstant.ROW_FADE_IN_MS) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Income Card
            AnimatedCard(
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(40.dp)
                                    .background(
                                        IncomeGreen.copy(alpha = HomeConstant.ICON_BADGE_ALPHA),
                                        shape = RoundedCornerShape(HomeConstant.ICON_BADGE_CORNER_DP.dp),
                                    )
                                    .padding(8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = IncomeGreen,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    VerticalSpacer(SpacingSize.XS)
                    AppText(
                        text = stringResource(R.string.home_income),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold,
                    )
                    VerticalSpacer(SpacingSize.XXXS)
                    CompactMoneyText(
                        amount = totalIncome,
                        fontSize = 18,
                        fontWeight = FontWeight.Bold,
                    )
                    if (incomeDeltaPercent != null) {
                        VerticalSpacer(SpacingSize.XXXS)
                        TrendBadge(deltaPercent = incomeDeltaPercent, baseColor = IncomeGreen)
                    }
                }
            }

            // Expense Card
            AnimatedCard(
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.errorContainer,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(40.dp)
                                    .background(
                                        ExpenseRed.copy(alpha = HomeConstant.ICON_BADGE_ALPHA),
                                        shape = RoundedCornerShape(HomeConstant.ICON_BADGE_CORNER_DP.dp),
                                    )
                                    .padding(8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = ExpenseRed,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    VerticalSpacer(SpacingSize.XS)
                    AppText(
                        text = stringResource(R.string.home_expenses),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold,
                    )
                    VerticalSpacer(SpacingSize.XXXS)
                    CompactMoneyText(
                        amount = totalExpense,
                        fontSize = 18,
                        fontWeight = FontWeight.Bold,
                    )
                    if (expenseDeltaPercent != null) {
                        VerticalSpacer(SpacingSize.XXXS)
                        TrendBadge(deltaPercent = expenseDeltaPercent, baseColor = ExpenseRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendBadge(
    deltaPercent: Double,
    baseColor: Color,
) {
    val trend = when {
        deltaPercent > 5.0  -> TrendDirection.UP
        deltaPercent < -5.0 -> TrendDirection.DOWN
        else                -> TrendDirection.NEUTRAL
    }
    val icon = when (trend) {
        TrendDirection.UP      -> Icons.AutoMirrored.Filled.TrendingUp
        TrendDirection.DOWN    -> Icons.AutoMirrored.Filled.TrendingDown
        TrendDirection.NEUTRAL -> Icons.AutoMirrored.Filled.TrendingFlat
    }
    val sign = if (deltaPercent >= 0) "+" else ""
    Row(
        modifier = Modifier
            .background(
                color = baseColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = baseColor,
            modifier = Modifier.size(12.dp),
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = "$sign${"%.0f".format(abs(deltaPercent))}%",
            color = baseColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Preview(showBackground = true, name = "Income Expense Row - Light")
@Composable
private fun IncomeExpenseRowLightPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        IncomeExpenseRow(
            totalIncome = 2500.0,
            totalExpense = 205.5,
        )
    }
}

@Preview(showBackground = true, name = "Income Expense Row - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun IncomeExpenseRowDarkPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        IncomeExpenseRow(
            totalIncome = 2500.0,
            totalExpense = 205.5,
        )
    }
}
