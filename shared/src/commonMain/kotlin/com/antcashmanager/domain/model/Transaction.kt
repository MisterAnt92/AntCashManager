package com.antcashmanager.domain.model

data class Transaction(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val type: TransactionType,
    val timestamp: Long = System.currentTimeMillis()
)

enum class TransactionType {
    INCOME, EXPENSE
}
