package com.antcashmanager.domain.model

/**
 * Monthly spending budget for a single category.
 *
 * [monthlyLimit] is always positive, expressed in the user's display currency.
 */
public data class CategoryBudget(
    val categoryId: Long,
    val monthlyLimit: Double,
)
