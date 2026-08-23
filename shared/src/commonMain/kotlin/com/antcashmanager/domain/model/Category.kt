package com.antcashmanager.domain.model

public data class Category(
    val id: Long = 0,
    val name: String,
    val icon: String = "category",
    val color: Long = 0xFF9E9E9E,
    val type: String = "EXPENSE",
    val isDefault: Boolean = false,
    val sortOrder: Int = 0,
    val isHidden: Boolean = false,
)
