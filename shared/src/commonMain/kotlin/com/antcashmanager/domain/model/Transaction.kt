package com.antcashmanager.domain.model

data class Transaction(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val type: TransactionType,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val payee: String = "",
    val location: String = "",
    val isRecurring: Boolean = false,
    val tags: String = "",
    val recurrenceInterval: String = "",
)

enum class TransactionType {
    INCOME, EXPENSE
}

enum class RecurrenceInterval {
    DAILY, WEEKLY, MONTHLY, YEARLY
}

