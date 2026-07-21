package com.antcashmanager.android.ui.screen.home.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.antcashmanager.android.R
import com.antcashmanager.android.ui.components.AnimatedCard
import com.antcashmanager.android.ui.components.FadeInOnAppear
import com.antcashmanager.android.ui.components.text.AppText
import com.antcashmanager.android.ui.components.text.BalanceText
import com.antcashmanager.android.ui.components.text.MoneyText
import com.antcashmanager.android.ui.theme.AntCashManagerTheme
import com.antcashmanager.android.ui.theme.ExpenseRed
import com.antcashmanager.android.ui.theme.IncomeGreen
import com.antcashmanager.android.ui.theme.ThemeConstants
import com.antcashmanager.android.util.LocalAmountsMasked
import com.antcashmanager.android.util.LocalCurrencyFormat
import com.antcashmanager.android.util.formatAmount
import com.antcashmanager.android.util.maskDigits
import com.antcashmanager.domain.model.PaymentType

@Composable
fun BalanceCard(
    balance: Double,
    modifier: Modifier = Modifier,
    showPaymentTypeBreakdown: Boolean = false,
    balanceByPaymentType: Map<PaymentType, Double> = emptyMap(),
    reduceMotion: Boolean = false,
) {
    val balanceStateColor by animateColorAsState(
        targetValue = if (balance >= 0) IncomeGreen else ExpenseRed,
        animationSpec = tween(600),
        label = "balance_color",
    )
    val balanceStateContainerColor by animateColorAsState(
        targetValue = if (balance >= 0) {
            MaterialTheme.colorScheme.tertiaryContainer.copy(
                alpha = ThemeConstants.BALANCE_STATUS_POSITIVE_CONTAINER_ALPHA,
            )
        } else {
            MaterialTheme.colorScheme.errorContainer.copy(
                alpha = ThemeConstants.BALANCE_STATUS_NEGATIVE_CONTAINER_ALPHA,
            )
        },
        animationSpec = tween(600),
        label = "balance_state_container_color",
    )

    FadeInOnAppear(durationMillis = 600) {
        AnimatedCard(
            modifier = modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
        ) {
            val balanceStatusText = if (balance >= 0) {
                stringResource(R.string.home_balance_positive)
            } else {
                stringResource(R.string.home_balance_negative)
            }
            val formattedBalance = formatAmount(balance, LocalCurrencyFormat.current).let {
                if (LocalAmountsMasked.current) maskDigits(it) else it
            }
            val balanceSummaryDescription = stringResource(
                R.string.home_balance_summary_cd,
                stringResource(R.string.home_total_balance),
                formattedBalance,
                balanceStatusText,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier.semantics(mergeDescendants = true) {
                        contentDescription = balanceSummaryDescription
                    },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Title
                    AppText(
                        text = stringResource(R.string.home_total_balance),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                            alpha = ThemeConstants.HIGH_EMPHASIS_TEXT_ALPHA,
                        ),
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    BalanceText(
                        amount = balance,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        fontSize = 32,
                        positiveColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        negativeColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Box(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        AppText(
                            text = balanceStatusText,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(balanceStateContainerColor)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = balanceStateColor,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                // Payment Type Breakdown (lo Spacer sopra vive qui dentro: altrimenti
                // occuperebbe 20dp anche quando la ripartizione è nascosta/vuota, che è
                // il caso di default per ogni installazione nuova).
                AnimatedVisibility(
                    visible = showPaymentTypeBreakdown && balanceByPaymentType.isNotEmpty(),
                    enter = if (reduceMotion) {
                        EnterTransition.None
                    } else {
                        fadeIn(animationSpec = tween(400)) + expandVertically()
                    },
                    exit = if (reduceMotion) {
                        ExitTransition.None
                    } else {
                        fadeOut(animationSpec = tween(400)) + shrinkVertically()
                    },
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(20.dp))
                        PaymentTypeBreakdown(
                            balanceByPaymentType = balanceByPaymentType,
                            reduceMotion = reduceMotion,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentTypeBreakdown(
    balanceByPaymentType: Map<PaymentType, Double>,
    reduceMotion: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppText(
            text = stringResource(R.string.payment_breakdown_label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Get ordered payment types (ELECTRONIC, CASH, MEAL_VOUCHERS)
        val orderedPaymentTypes = PaymentType.values().mapNotNull { type ->
            balanceByPaymentType[type]?.let { type to it }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            orderedPaymentTypes.forEach { (paymentType, amount) ->
                FadeInOnAppear(
                    durationMillis = if (reduceMotion) 0 else 300,
                ) {
                    PaymentTypeItem(
                        paymentType = paymentType,
                        amount = amount,
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentTypeItem(
    paymentType: PaymentType,
    amount: Double,
) {
    val (icon, labelRes) = when (paymentType) {
        PaymentType.ELECTRONIC -> Icons.Default.CreditCard to R.string.payment_type_electronic
        PaymentType.CASH -> Icons.Default.Money to R.string.payment_type_cash
        PaymentType.MEAL_VOUCHERS -> Icons.Default.Restaurant to R.string.payment_type_meal_vouchers
    }

    val paymentTypeName = stringResource(labelRes)
    val contentDesc = stringResource(
        R.string.payment_breakdown_item_desc,
        paymentTypeName,
        amount,
    )

    Card(
        modifier = Modifier.semantics {
            contentDescription = contentDesc
            role = Role.Button
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .width(100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(6.dp))
            AppText(
                text = paymentTypeName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            MoneyText(
                amount = amount,
                fontSize = 15,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview(showBackground = true, name = "Balance Card - Positive")
@Composable
private fun BalanceCardPositivePreview() {
    AntCashManagerTheme(dynamicColor = false) {
        BalanceCard(balance = 2294.50)
    }
}

@Preview(showBackground = true, name = "Balance Card - Negative")
@Composable
private fun BalanceCardNegativePreview() {
    AntCashManagerTheme(dynamicColor = false) {
        BalanceCard(balance = -150.25)
    }
}

@Preview(showBackground = true, name = "Balance Card - Zero")
@Composable
private fun BalanceCardZeroPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        BalanceCard(balance = 0.0)
    }
}

@Preview(showBackground = true, name = "Balance Card - With Breakdown (3 Types)")
@Composable
private fun BalanceCardWithBreakdownPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        BalanceCard(
            balance = 3500.75,
            showPaymentTypeBreakdown = true,
            balanceByPaymentType = mapOf(
                PaymentType.ELECTRONIC to 2000.50,
                PaymentType.CASH to 1200.25,
                PaymentType.MEAL_VOUCHERS to 300.0,
            ),
        )
    }
}

@Preview(showBackground = true, name = "Balance Card - With Breakdown (1 Type)")
@Composable
private fun BalanceCardWithBreakdownOneTypePreview() {
    AntCashManagerTheme(dynamicColor = false) {
        BalanceCard(
            balance = 1500.00,
            showPaymentTypeBreakdown = true,
            balanceByPaymentType = mapOf(
                PaymentType.ELECTRONIC to 1500.0,
            ),
        )
    }
}

@Preview(showBackground = true, name = "Balance Card - With Breakdown (Cash Only)")
@Composable
private fun BalanceCardWithBreakdownCashOnlyPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        BalanceCard(
            balance = 850.50,
            showPaymentTypeBreakdown = true,
            balanceByPaymentType = mapOf(
                PaymentType.CASH to 850.50,
            ),
        )
    }
}

@Preview(showBackground = true, name = "Balance Card - Dark Theme with Breakdown")
@Composable
private fun BalanceCardDarkWithBreakdownPreview() {
    AntCashManagerTheme(darkTheme = true, dynamicColor = false) {
        BalanceCard(
            balance = 2500.75,
            showPaymentTypeBreakdown = true,
            balanceByPaymentType = mapOf(
                PaymentType.ELECTRONIC to 1800.50,
                PaymentType.CASH to 700.25,
            ),
        )
    }
}

@Preview(showBackground = true, name = "Balance Card - Reduce Motion")
@Composable
private fun BalanceCardReduceMotionPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        BalanceCard(
            balance = 1250.00,
            showPaymentTypeBreakdown = true,
            balanceByPaymentType = mapOf(
                PaymentType.ELECTRONIC to 1000.0,
                PaymentType.MEAL_VOUCHERS to 250.0,
            ),
            reduceMotion = true,
        )
    }
}

@Preview(showBackground = true, name = "Balance Card - Breakdown Hidden (Empty)")
@Composable
private fun BalanceCardBreakdownHiddenPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        BalanceCard(
            balance = 500.00,
            showPaymentTypeBreakdown = true,
            balanceByPaymentType = emptyMap(),
        )
    }
}

@Preview(showBackground = true, name = "Balance Card - With Long Labels Test")
@Composable
private fun BalanceCardLongLabelsPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        BalanceCard(
            balance = 4500.00,
            showPaymentTypeBreakdown = true,
            balanceByPaymentType = mapOf(
                PaymentType.ELECTRONIC to 2500.0,
                PaymentType.CASH to 1500.0,
                PaymentType.MEAL_VOUCHERS to 500.0,
            ),
        )
    }
}

@Preview(showBackground = true, name = "Balance Card - Large Amounts Test")
@Composable
private fun BalanceCardLargeAmountsPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        BalanceCard(
            balance = 125450.75,
            showPaymentTypeBreakdown = true,
            balanceByPaymentType = mapOf(
                PaymentType.ELECTRONIC to 99999.99,
                PaymentType.CASH to 12345.50,
                PaymentType.MEAL_VOUCHERS to 13105.26,
            ),
        )
    }
}

@Preview(showBackground = true, name = "Balance Card - Two Types Only")
@Composable
private fun BalanceCardTwoTypesPreview() {
    AntCashManagerTheme(dynamicColor = false) {
        BalanceCard(
            balance = 5500.00,
            showPaymentTypeBreakdown = true,
            balanceByPaymentType = mapOf(
                PaymentType.ELECTRONIC to 3500.0,
                PaymentType.MEAL_VOUCHERS to 2000.0,
            ),
        )
    }
}
