package com.antcashmanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["profile_id", "timestamp"]), // scoped ORDER BY timestamp / date range
        Index(value = ["profile_id", "category"]), // scoped WHERE category = :category
        Index(value = ["profile_id", "category", "timestamp"]), // getTransactionsByCategory ORDER BY timestamp
        Index(value = ["profile_id", "is_recurring", "timestamp"]), // getRecurringTransactions ORDER BY timestamp
    ],
)
public data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val type: String,
    val timestamp: Long,
    @ColumnInfo(defaultValue = "")
    val notes: String = "",
    @ColumnInfo(defaultValue = "")
    val payee: String = "",
    @ColumnInfo(defaultValue = "")
    val location: String = "",
    @ColumnInfo(name = "is_recurring", defaultValue = "0")
    val isRecurring: Boolean = false,
    @ColumnInfo(defaultValue = "")
    val tags: String = "",
    @ColumnInfo(name = "recurrence_interval", defaultValue = "")
    val recurrenceInterval: String = "",
    @ColumnInfo(name = "payment_type", defaultValue = "ELECTRONIC")
    val paymentType: String = "ELECTRONIC",
    @ColumnInfo(name = "meal_voucher_count", defaultValue = "0")
    val mealVoucherCount: Int = 0,
    @ColumnInfo(name = "meal_voucher_difference", defaultValue = "0.0")
    val mealVoucherDifference: Double = 0.0,
    @ColumnInfo(name = "category_icon", defaultValue = "")
    val categoryIcon: String = "",
    @ColumnInfo(name = "category_color", defaultValue = "9474862")
    val categoryColor: Long = 9474862, // 0xFF90A4AE in decimal
    // defaultValue = "1" is safe: profile id=1 (Default) cannot be deleted (DeleteProfileUseCase guard)
    @ColumnInfo(name = "profile_id", defaultValue = "1")
    val profileId: Long = 1,
)
