package com.antcashmanager.domain.model

public data class MealVoucherSummary(
    val received: Int,
    val used: Int,
    val remaining: Int,
    val voucherValue: Double,
    val remainingValue: Double,
    val usedValue: Double,
)
