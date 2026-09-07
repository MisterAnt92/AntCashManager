package com.antcashmanager.data.mapper

import com.antcashmanager.data.local.entity.TransactionEntity
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType

public fun TransactionEntity.toDomain(): Transaction =
    Transaction(
        id = id,
        title = title,
        amount = amount,
        category = category,
        type = TransactionType.valueOf(type),
        timestamp = timestamp,
        notes = notes,
        payee = payee,
        location = location,
        isRecurring = isRecurring,
        tags = tags,
        recurrenceInterval = recurrenceInterval,
        paymentType =
            try {
                PaymentType.valueOf(paymentType)
            } catch (_: IllegalArgumentException) {
                PaymentType.ELECTRONIC
            },
        mealVoucherCount = mealVoucherCount,
        mealVoucherDifference = mealVoucherDifference,
        categoryIcon = categoryIcon,
        categoryColor = categoryColor,
    )

public fun Transaction.toEntity(): TransactionEntity =
    TransactionEntity(
        id = id,
        title = title,
        amount = amount,
        category = category,
        type = type.name,
        timestamp = timestamp,
        notes = notes,
        payee = payee,
        location = location,
        isRecurring = isRecurring,
        tags = tags,
        recurrenceInterval = recurrenceInterval,
        paymentType = paymentType.name,
        mealVoucherCount = mealVoucherCount,
        mealVoucherDifference = mealVoucherDifference,
        categoryIcon = categoryIcon,
        categoryColor = categoryColor,
    )
