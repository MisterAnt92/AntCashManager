package com.antcashmanager.android.ui.screen.tutorial

/** Keys identifying revealable UI elements across the tutorial flow. */
enum class TutorialKey {
    HOME_TRANSACTIONS,
    CHARTS_NAV,
    TRANSACTIONS_NAV,
    CATEGORIES_NAV,
    /** Hamburger / sidebar-toggle button shown on phone layouts (non-rail navigation). */
    SIDEBAR_TOGGLE,
}

/** Total number of tutorial steps (0..TOTAL_STEPS-1, plus a finish state). */
const val TUTORIAL_TOTAL_STEPS = 4

/**
 * Maps a step index (0–3) to its [TutorialKey].
 *
 * On phone layouts ([preferRailNavigation] = false) step 3 highlights the sidebar toggle button
 * instead of a Categories nav-bar item (which is not present in the bottom bar on phones).
 * Returns null for the finish state (step >= [TUTORIAL_TOTAL_STEPS]).
 */
fun stepKeyFor(step: Int, preferRailNavigation: Boolean = true): TutorialKey? =
    when (step) {
        0 -> TutorialKey.HOME_TRANSACTIONS
        1 -> TutorialKey.CHARTS_NAV
        2 -> TutorialKey.TRANSACTIONS_NAV
        3 -> if (preferRailNavigation) TutorialKey.CATEGORIES_NAV else TutorialKey.SIDEBAR_TOGGLE
        else -> null
    }
