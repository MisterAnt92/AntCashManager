package com.antcashmanager.testutil

import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.PaymentType
import com.antcashmanager.domain.model.SavedDateFilter
import com.antcashmanager.domain.model.Transaction
import com.antcashmanager.domain.model.TransactionType

/**
 * Builder pattern para criar test data facilmente sem necessidade de especificar todos os parametros.
 *
 * Uso:
 * ```
 * val tx = testTransaction {
 *     title = "Lunch"
 *     amount = -50.0
 * }
 * ```
 */
class TransactionTestDataBuilder {
    var id: Int = 1
    var title: String = "Test Transaction"
    var amount: Double = -100.0
    var category: String = "Test Category"
    var type: TransactionType = TransactionType.EXPENSE
    var timestamp: Long = System.currentTimeMillis()
    var notes: String = ""
    var payee: String = ""
    var location: String = ""
    var isRecurring: Boolean = false
    var tags: String = ""
    var recurrenceInterval: String? = null
    var paymentType: PaymentType = PaymentType.ELECTRONIC
    var mealVoucherCount: Int = 0
    var categoryIcon: String = ""
    var categoryColor: Long = 0xFF000000L

    fun build(): Transaction = Transaction(
        id = id,
        title = title,
        amount = amount,
        category = category,
        type = type,
        timestamp = timestamp,
        notes = notes,
        payee = payee,
        location = location,
        isRecurring = isRecurring,
        tags = tags,
        recurrenceInterval = recurrenceInterval,
        paymentType = paymentType,
        mealVoucherCount = mealVoucherCount,
        categoryIcon = categoryIcon,
        categoryColor = categoryColor,
    )
}

fun testTransaction(block: TransactionTestDataBuilder.() -> Unit = {}): Transaction {
    return TransactionTestDataBuilder().apply(block).build()
}

/**
 * Builder for Category test data.
 */
class CategoryTestDataBuilder {
    var id: Int = 1
    var name: String = "Test Category"
    var icon: String = "category"
    var color: Long = 0xFF2196F3L
    var type: String = "EXPENSE"
    var isDefault: Boolean = false
    var sortOrder: Int = 0
    var isHidden: Boolean = false

    fun build(): Category = Category(
        id = id,
        name = name,
        icon = icon,
        color = color,
        type = type,
        isDefault = isDefault,
        sortOrder = sortOrder,
        isHidden = isHidden,
    )
}

fun testCategory(block: CategoryTestDataBuilder.() -> Unit = {}): Category {
    return CategoryTestDataBuilder().apply(block).build()
}

/**
 * Builder for SavedDateFilter test data.
 */
class SavedDateFilterTestDataBuilder {
    var presetIndex: Int = 0
    var from: Long = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
    var to: Long = System.currentTimeMillis()

    fun build(): SavedDateFilter = SavedDateFilter(
        presetIndex = presetIndex,
        from = from,
        to = to,
    )
}

fun testSavedDateFilter(block: SavedDateFilterTestDataBuilder.() -> Unit = {}): SavedDateFilter {
    return SavedDateFilterTestDataBuilder().apply(block).build()
}

/**
 * Common test data constants.
 */
object TestDataDefaults {
    const val DEFAULT_ID = 1
    const val DEFAULT_TITLE = "Test Transaction"
    const val DEFAULT_AMOUNT = -100.0
    const val DEFAULT_CATEGORY = "Test Category"
    val DEFAULT_TYPE = TransactionType.EXPENSE
    val DEFAULT_PAYMENT_TYPE = PaymentType.ELECTRONIC
    const val DEFAULT_TIMESTAMP = 1700000000000L
}
