package com.antcashmanager.android.ui.mapper

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector
import com.antcashmanager.android.R
import com.antcashmanager.domain.model.PaymentType

/** Single place that maps [PaymentType] to its UI representation (label, icon, emoji). */
@StringRes
fun PaymentType.labelRes(): Int =
    when (this) {
        PaymentType.ELECTRONIC -> R.string.payment_type_electronic
        PaymentType.CASH -> R.string.payment_type_cash
        PaymentType.MEAL_VOUCHERS -> R.string.payment_type_meal_vouchers
    }

fun PaymentType.icon(): ImageVector =
    when (this) {
        PaymentType.ELECTRONIC -> Icons.Default.CreditCard
        PaymentType.CASH -> Icons.Default.Money
        PaymentType.MEAL_VOUCHERS -> Icons.Default.Restaurant
    }

fun PaymentType.emoji(): String =
    when (this) {
        PaymentType.ELECTRONIC -> "💳"
        PaymentType.CASH -> "💵"
        PaymentType.MEAL_VOUCHERS -> "🎫"
    }
