package com.antcashmanager.android.ui.base

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Coordinates state synchronization between multiple panes on foldable devices.
 *
 * When a foldable device is opened and the app spans across the hinge in split-view mode,
 * this coordinator ensures that selections and navigation in one pane are reflected in the other.
 *
 * Example use cases:
 * - User selects transaction in left pane → show details in right pane
 * - User selects category in left pane → filter transactions in right pane
 * - Navigation state synchronized across panes
 *
 * For tablet devices (non-foldable), this can also support true multi-pane layouts.
 */
class MultiPaneCoordinator {
    // ── Transaction Selection ──
    private val _selectedTransaction = MutableStateFlow<Transaction?>(null)
    val selectedTransaction: StateFlow<Transaction?> = _selectedTransaction.asStateFlow()

    // ── Category Selection ──
    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    // ── Navigation State ──
    private val _selectedNavRoute = MutableStateFlow<String?>(null)
    val selectedNavRoute: StateFlow<String?> = _selectedNavRoute.asStateFlow()

    // ── Search/Filter State ──
    private val _searchQuery = MutableStateFlow<String>("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isFilterExpanded = MutableStateFlow<Boolean>(false)
    val isFilterExpanded: StateFlow<Boolean> = _isFilterExpanded.asStateFlow()

    // ── UI State Flags ──
    private val _showDetailsPane = MutableStateFlow<Boolean>(false)
    val showDetailsPane: StateFlow<Boolean> = _showDetailsPane.asStateFlow()

    /**
     * Select a transaction to display details.
     * Updates selectedTransaction and optionally navigates to details pane.
     *
     * @param transaction Transaction to select, or null to deselect
     * @param navigateToDetailsPane Whether to show the details pane (for split-view)
     */
    fun selectTransaction(transaction: Transaction?, navigateToDetailsPane: Boolean = false) {
        _selectedTransaction.value = transaction
        if (navigateToDetailsPane && transaction != null) {
            _showDetailsPane.value = true
        }
    }

    /**
     * Select a category for filtering.
     * Updates selectedCategory which can trigger filtered views in other panes.
     *
     * @param category Category to select, or null to deselect
     */
    fun selectCategory(category: Category?) {
        _selectedCategory.value = category
    }

    /**
     * Navigate to a route (top-level screen or nested destination).
     * Synchronizes navigation across panes when in split-view.
     *
     * @param route Navigation route (e.g., "home", "transactions", "charts")
     */
    fun navigateTo(route: String?) {
        _selectedNavRoute.value = route
    }

    /**
     * Update search query.
     * Useful for synchronized search across panes.
     *
     * @param query Search query string
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Toggle filter panel expansion.
     *
     * @param expanded True to show filter panel, false to hide
     */
    fun setFilterExpanded(expanded: Boolean) {
        _isFilterExpanded.value = expanded
    }

    /**
     * Show or hide details pane (relevant for split-view layouts).
     *
     * @param show True to show details pane, false to hide
     */
    fun setShowDetailsPane(show: Boolean) {
        _showDetailsPane.value = show
        if (!show) {
            // Clear selection when hiding details pane
            _selectedTransaction.value = null
        }
    }

    /**
     * Reset all state to defaults.
     * Useful when switching between split-view and single-pane layouts.
     */
    fun reset() {
        _selectedTransaction.value = null
        _selectedCategory.value = null
        _selectedNavRoute.value = null
        _searchQuery.value = ""
        _isFilterExpanded.value = false
        _showDetailsPane.value = false
    }
}

/**
 * CompositionLocal for accessing MultiPaneCoordinator from any Composable.
 *
 * Provides global access to multi-pane state without passing through many parameters.
 * Typically initialized in MainActivity or a high-level wrapper composable.
 *
 * Usage:
 * ```kotlin
 * val coordinator = LocalMultiPaneCoordinator.current
 * coordinator?.selectTransaction(transaction)
 * ```
 */
val LocalMultiPaneCoordinator = compositionLocalOf<MultiPaneCoordinator?> { null }
