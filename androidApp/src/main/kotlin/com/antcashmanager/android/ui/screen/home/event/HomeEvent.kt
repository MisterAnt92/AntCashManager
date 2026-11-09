package com.antcashmanager.android.ui.screen.home.event

import com.antcashmanager.domain.model.Transaction

/**
 * UI Events for Home screen.
 */
sealed interface HomeEvent {
    data class SelectPresetEvent(
        val index: Int,
    ) : HomeEvent

    data class SetDateRangeEvent(
        val from: Long,
        val to: Long,
    ) : HomeEvent

    data class ShowTransactionDetailsEvent(
        val transaction: Transaction,
    ) : HomeEvent

    data object DismissTransactionDetailsEvent : HomeEvent

    // Search events
    data class UpdateSearchQueryEvent(
        val query: String,
    ) : HomeEvent

    data object ToggleSearchExpandedEvent : HomeEvent

    // Top cards reorder dialog
    data object StartTopCardsReorderEvent : HomeEvent

    data class MoveTopCardEvent(
        val index: Int,
        val up: Boolean,
    ) : HomeEvent

    data object CancelTopCardsReorderEvent : HomeEvent

    data object ConfirmTopCardsOrderEvent : HomeEvent

    // Settings events
    data class SetIsTutorialCompletedEvent(
        val completed: Boolean,
    ) : HomeEvent

    data class SetDateFilterExpandedEvent(
        val expanded: Boolean,
    ) : HomeEvent
}
