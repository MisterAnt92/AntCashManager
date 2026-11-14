package com.antcashmanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["profile_id", "name"]), // Query by name scoped to profile
        Index(value = ["profile_id", "type"]), // Query categories by type scoped to profile
        Index(value = ["profile_id", "is_hidden", "sort_order"]), // ORDER BY is_hidden, sort_order
    ],
)
public data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String,
    val color: Long,
    @ColumnInfo(defaultValue = "EXPENSE")
    val type: String = "EXPENSE",
    @ColumnInfo(name = "is_default", defaultValue = "0")
    val isDefault: Boolean = false,
    @ColumnInfo(name = "sort_order", defaultValue = "0")
    val sortOrder: Int = 0,
    @ColumnInfo(name = "is_hidden", defaultValue = "0")
    val isHidden: Boolean = false,
    @ColumnInfo(name = "profile_id", defaultValue = "1")
    val profileId: Long = 1,
)
