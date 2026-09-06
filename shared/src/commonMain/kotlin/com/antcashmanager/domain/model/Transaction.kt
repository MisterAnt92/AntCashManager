package com.antcashmanager.domain.model

public data class Transaction(
    public val id: Long = 0,
    public val title: String,
    public val amount: Double,
    public val category: String,
    public val type: TransactionType,
    public val timestamp: Long = System.currentTimeMillis(),
    public val notes: String = "",
    public val payee: String = "",
    public val location: String = "",
    public val isRecurring: Boolean = false,
    public val tags: String = "",
    public val recurrenceInterval: String = "",
    public val paymentType: PaymentType = PaymentType.ELECTRONIC,
    public val mealVoucherCount: Int = 0,
    public val mealVoucherDifference: Double = 0.0,
    public val categoryIcon: String = "",
    public val categoryColor: Long = 0xFF90A4AE,
)

public enum class TransactionType {
    INCOME,
    EXPENSE,
}

public enum class PaymentType {
    ELECTRONIC,
    CASH,
    MEAL_VOUCHERS,
}

public enum class RecurrenceInterval {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
}
