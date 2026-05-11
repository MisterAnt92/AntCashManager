package com.antcashmanager.android.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.antcashmanager.android.R

/**
 * Maps hardcoded category names (from seeding or DB) to string resources for localization.
 */
@Composable
fun translateCategory(categoryName: String): String {
    val resId = when (categoryName) {
        "Non categorizzato" -> R.string.category_uncategorized
        "Casa" -> R.string.category_home
        "Trasporti" -> R.string.category_transport
        "Cibo" -> R.string.category_food
        "Bollette" -> R.string.category_bills
        "Pranzi/Cene fuori" -> R.string.category_dining
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

    return if (resId != null) stringResource(resId) else categoryName
}
