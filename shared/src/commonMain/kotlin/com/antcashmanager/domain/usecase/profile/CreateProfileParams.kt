package com.antcashmanager.domain.usecase.profile

public data class CreateProfileParams(
    val name: String,
    val colorArgb: Long = 0xFF6650A4,
    val avatarEmoji: String = "",
    val shareTransactions: Boolean = true,
)
