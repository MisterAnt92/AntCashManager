package com.antcashmanager.android.ui.screen.home.event

import com.antcashmanager.domain.model.Transaction

/**
 * UI Events for Home screen.
 */
sealed interface HomeEvent {
    data class SelectPreset(
        val index: Int,
    ) : HomeEvent

    data class SetDateRange(
        val from: Long,
        val to: Long,
    ) : HomeEvent

    data class ShowTransactionDetails(
        val transaction: Transaction,
    ) : HomeEvent

    data object DismissTransactionDetails : HomeEvent

    // Search events
    data class UpdateSearchQuery(
        val query: String,
    ) : HomeEvent

    data object ToggleSearchExpanded : HomeEvent

    // Top cards reorder dialog
    data object StartTopCardsReorder : HomeEvent

    data class MoveTopCard(
        val index: Int,
        val up: Boolean,
    ) : HomeEvent

    data object CancelTopCardsReorder : HomeEvent

    data object ConfirmTopCardsOrder : HomeEvent

    // Settings events
    data class SetIsTutorialCompleted(
        val completed: Boolean,
    ) : HomeEvent

    data class SetDateFilterExpanded(
        val expanded: Boolean,
    ) : HomeEvent
}
