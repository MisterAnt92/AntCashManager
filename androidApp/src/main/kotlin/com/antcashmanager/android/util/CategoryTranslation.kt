package com.antcashmanager.android.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.antcashmanager.android.R

/**
 * Maps hardcoded category names (from seeding or DB) to string resources for localization.
 */
private fun categoryResId(categoryName: String): Int? =
    when (categoryName) {
        // Resource keys stored as plain names in DB
        "category_uncategorized" -> R.string.category_uncategorized
        "category_home" -> R.string.category_home
        "category_transport" -> R.string.category_transport
        "category_food" -> R.string.category_food
        "category_bills" -> R.string.category_bills
        "category_dining" -> R.string.category_dining
        "category_entertainment" -> R.string.category_entertainment
        "category_health" -> R.string.category_health
        "category_shopping" -> R.string.category_shopping
        "category_education" -> R.string.category_education
        "category_other" -> R.string.category_other
        "category_salary" -> R.string.category_salary
        "category_allowance" -> R.string.category_allowance
        "category_reimbursement" -> R.string.category_reimbursement
        "category_investments" -> R.string.category_investments
        "category_freelance" -> R.string.category_freelance
        "category_gifts" -> R.string.category_gifts
        "category_subscriptions" -> R.string.category_subscriptions
        "category_personal_care" -> R.string.category_personal_care

        "Non categorizzato" -> R.string.category_uncategorized
        "Casa" -> R.string.category_home
        "Trasporti" -> R.string.category_transport
        "Cibo" -> R.string.category_food
        "Bollette" -> R.string.category_bills
        "Pranzi/Cene fuori" -> R.string.category_dining
        "Pranzi/Cenette fuori" -> R.string.category_dining
        "Divertimento" -> R.string.category_entertainment
        "Salute" -> R.string.category_health
        "Shopping" -> R.string.category_shopping
        "Istruzione" -> R.string.category_education
        "Altro" -> R.string.category_other
        "Stipendio" -> R.string.category_salary
        "Paghetta" -> R.string.category_allowance
        "Rimborso" -> R.string.category_reimbursement
        "Investimenti" -> R.string.category_investments
        "Freelance" -> R.string.category_freelance
        "Regali" -> R.string.category_gifts
        "Abbonamenti" -> R.string.category_subscriptions
        "Cura personale" -> R.string.category_personal_care
        // English keys (for future proofing or if they were seeded in English)
        "Uncategorized" -> R.string.category_uncategorized
        "Home" -> R.string.category_home
        "Transport" -> R.string.category_transport
        "Food" -> R.string.category_food
        "Bills" -> R.string.category_bills
        "Dining out" -> R.string.category_dining
        "Entertainment" -> R.string.category_entertainment
        "Health" -> R.string.category_health
        "Education" -> R.string.category_education
        "Other" -> R.string.category_other
        "Salary" -> R.string.category_salary
        "Allowance" -> R.string.category_allowance
        "Reimbursement" -> R.string.category_reimbursement
        "Investments" -> R.string.category_investments
        else -> null
    }

@Composable
fun translateCategory(categoryName: String): String {
    val resId = categoryResId(categoryName)
    return if (resId != null) stringResource(resId) else categoryName
}

/**
 * Non-Composable variant for contexts without a Compose runtime (e.g. Glance widgets).
 */
fun translateCategoryPlain(
    context: Context,
    categoryName: String,
): String {
    val resId = categoryResId(categoryName)
    return if (resId != null) context.getString(resId) else categoryName
}
