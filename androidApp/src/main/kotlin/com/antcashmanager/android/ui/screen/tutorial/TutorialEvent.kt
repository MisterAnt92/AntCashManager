package com.antcashmanager.android.ui.screen.tutorial

import com.antcashmanager.architecture.UIEvent

sealed class TutorialEvent : UIEvent() {
    /** Start the tutorial (called on first launch or after reset). Upgrade safeguard applies. */
    data object StartTutorial : TutorialEvent()

    /** Force-start the tutorial bypassing the upgrade safeguard (replay from Settings). */
    data object ForceStartTutorial : TutorialEvent()

    /** Advance to the next step, or enter finish state from the last step. */
    data object NextStep : TutorialEvent()

    /** Go back one step; on step 0 behaves like [SkipTutorial]. */
    data object PreviousStep : TutorialEvent()

    /** User dismissed the tutorial before completion. */
    data object SkipTutorial : TutorialEvent()

    /** User confirmed the finish card — tutorial is fully complete. */
    data object CompleteTutorial : TutorialEvent()
}
