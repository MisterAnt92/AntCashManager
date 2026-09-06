package com.antcashmanager.android.ui.screen.home.model

import com.antcashmanager.android.R

enum class HomeTopCardType(
    val storageKey: String,
    val titleResId: Int,
) {
    BALANCE("balance", R.string.home_total_balance),
    INCOME_EXPENSE("income_expense", R.string.home_income_expense_card_title),
    QUICK_INSIGHTS("quick_insights", R.string.home_quick_insights_title),
    ;

    companion object {
        val defaultOrder = listOf(BALANCE, INCOME_EXPENSE, QUICK_INSIGHTS)

        fun parse(raw: String): List<HomeTopCardType> {
            val ordered =
                raw
                    .split(',')
                    .map(String::trim)
                    .mapNotNull { key -> entries.find { it.storageKey == key } }
                    .distinct()
                    .toMutableList()

            defaultOrder.forEach { type ->
                if (type !in ordered) {
                    ordered += type
                }
            }

            return ordered
        }

        fun serialize(order: List<HomeTopCardType>): String = order.joinToString(separator = ",") { it.storageKey }
    }
}
