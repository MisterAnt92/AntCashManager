package com.antcashmanager.android.ui.screen.tutorial

/** Keys identifying revealable UI elements across the tutorial flow. */
enum class TutorialKey {
    WELCOME,
    BALANCE_CARD,
    TRANSACTIONS,
    CHARTS_NAV,
    CATEGORIES_NAV,
    SETTINGS_NAV,
}

/** Total number of tutorial steps (0..TOTAL_STEPS-1, plus a finish state). */
const val TUTORIAL_TOTAL_STEPS = 6

/**
 * Maps a step index (0–5) to its [TutorialKey].
 * Returns null for the finish state (step >= [TUTORIAL_TOTAL_STEPS]).
 */
fun stepKeyFor(step: Int): TutorialKey? =
    when (step) {
        0 -> TutorialKey.WELCOME
        1 -> TutorialKey.BALANCE_CARD
        2 -> TutorialKey.TRANSACTIONS
        3 -> TutorialKey.CHARTS_NAV
        4 -> TutorialKey.CATEGORIES_NAV
        5 -> TutorialKey.SETTINGS_NAV
        else -> null
    }
