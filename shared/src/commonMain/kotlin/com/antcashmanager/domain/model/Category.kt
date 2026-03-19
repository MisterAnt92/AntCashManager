package com.antcashmanager.domain.model
data class Category(
    val id: Long = 0,
    val name: String,
    val icon: String = "category",
    val color: Long = 0xFF9E9E9E,
)
