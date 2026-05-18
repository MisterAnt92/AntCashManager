package com.antcashmanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String,
    val color: Long,
    @ColumnInfo(defaultValue = "EXPENSE")
    val type: String = "EXPENSE",
    @ColumnInfo(name = "is_default", defaultValue = "0")
    val isDefault: Boolean = false,
)
