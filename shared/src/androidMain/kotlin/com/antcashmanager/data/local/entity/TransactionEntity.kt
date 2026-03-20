package com.antcashmanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
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
)
