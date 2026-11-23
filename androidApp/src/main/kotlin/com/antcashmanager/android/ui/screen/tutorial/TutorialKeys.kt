package com.antcashmanager.android.ui.screen.tutorial

/** Keys identifying revealable UI elements across the tutorial flow. */
enum class TutorialKey {
    HOME_TRANSACTIONS,
    CHARTS_NAV,
    TRANSACTIONS_NAV,
    CATEGORIES_NAV,
}

/** Total number of tutorial steps (0..TOTAL_STEPS-1, plus a finish state). */
const val TUTORIAL_TOTAL_STEPS = 4

/**
 * Maps a step index (0–3) to its [TutorialKey].
 * Returns null for the finish state (step >= [TUTORIAL_TOTAL_STEPS]).
 */
fun stepKeyFor(step: Int): TutorialKey? =
    when (step) {
        0 -> TutorialKey.HOME_TRANSACTIONS
        1 -> TutorialKey.CHARTS_NAV
        2 -> TutorialKey.TRANSACTIONS_NAV
        3 -> TutorialKey.CATEGORIES_NAV
        else -> null
    }
