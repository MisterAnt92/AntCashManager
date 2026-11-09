package com.antcashmanager.domain.model

public data class Profile(
    val id: Long = 0,
    val name: String,
    val uuid: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = false,
    val colorArgb: Long = 0xFF6650A4,
    val avatarEmoji: String = "",
)
