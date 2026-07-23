package com.antcashmanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index("timestamp"),           // ORDER BY timestamp
        Index("is_recurring"),        // WHERE is_recurring = 1
        Index("category"),            // WHERE category = :category
        Index("type")                 // WHERE type = :type
    ]
)
data class TransactionEntity(
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
    @ColumnInfo(name = "category_icon", defaultValue = "")
    val categoryIcon: String = "",
    @ColumnInfo(name = "category_color", defaultValue = "9474862")
    val categoryColor: Long = 9474862, // 0xFF90A4AE in decimal
)
