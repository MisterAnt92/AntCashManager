package com.antcashmanager.android.ui.screen.categories

import com.antcashmanager.domain.model.Category

/**
 * UDF Pattern: Events for Categories screen.
 *
 * All user interactions and operations emit events that the ViewModel processes
 * to update the state. This decouples the UI layer from business logic and
 * provides a clear audit trail of all actions.
 */
sealed class CategoryEvent {
    data class AddCategory(val name: String, val icon: String, val color: Long, val type: String = "EXPENSE") : CategoryEvent()
    data class UpdateCategory(val category: Category) : CategoryEvent()
    data class DeleteCategory(val category: Category) : CategoryEvent()
    data class SetCategoryHidden(val category: Category, val hidden: Boolean) : CategoryEvent()
    data class ReorderCategories(val reordered: List<Category>) : CategoryEvent()
    data object RetryLastOperation : CategoryEvent()
}
