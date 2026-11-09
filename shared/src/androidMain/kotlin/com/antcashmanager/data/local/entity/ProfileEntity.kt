package com.antcashmanager.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "profiles",
    indices = [
        Index("is_active"), // WHERE is_active = 1
    ],
)
public data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val uuid: String,
    @ColumnInfo(name = "created_at", defaultValue = "0")
    val createdAt: Long = 0,
    @ColumnInfo(name = "is_active", defaultValue = "0")
    val isActive: Boolean = false,
    @ColumnInfo(name = "color_argb", defaultValue = "4284955319")
    val colorArgb: Long = 0xFF6650A4,
    @ColumnInfo(name = "avatar_emoji", defaultValue = "")
    val avatarEmoji: String = "",
)
