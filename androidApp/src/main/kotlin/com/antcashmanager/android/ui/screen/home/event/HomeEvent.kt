package com.antcashmanager.android.ui.screen.home.event

/**
 * UI Events for Home screen.
 */
sealed interface HomeEvent {
    data class SelectPreset(val index: Int) : HomeEvent
    data class SetDateRange(val from: Long, val to: Long) : HomeEvent
    data class ShowTransactionDetails(val transaction: com.antcashmanager.domain.model.Transaction) :
        HomeEvent

    data object DismissTransactionDetails : HomeEvent

    // Search events
    data class UpdateSearchQuery(val query: String) : HomeEvent
    data object ToggleSearchExpanded : HomeEvent

    // Settings events
    data class SetIsTutorialCompleted(val completed: Boolean) : HomeEvent
    data class SetHomeTopCardsOrder(val order: String) : HomeEvent
    data class SetDateFilterExpanded(val expanded: Boolean) : HomeEvent
}