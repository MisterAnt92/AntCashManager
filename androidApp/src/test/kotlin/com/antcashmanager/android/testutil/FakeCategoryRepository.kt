package com.antcashmanager.android.testutil

import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Fake unico e completo di [CategoryRepository] per i test dei ViewModel, backed
 * da un'unica sorgente reattiva ([categories]). `open` per permettere override
 * puntuali nei rari casi con comportamento unico invece di duplicare l'intero fake.
 */
open class FakeCategoryRepository(
    initialCategories: List<Category> = emptyList(),
) : CategoryRepository {
    val categories = MutableStateFlow(initialCategories)

    var errorToThrow: Throwable? = null

    private var nextId = (initialCategories.maxOfOrNull { it.id } ?: 0L) + 1

    override fun getAllCategories(): Flow<List<Category>> = categories

    override suspend fun getCategoryById(id: Long): Category? = categories.value.find { it.id == id }

    override suspend fun getCategoryByName(name: String): Category? = categories.value.find { it.name == name }

    override suspend fun insertCategory(category: Category): Long {
        errorToThrow?.let { throw it }
        val id = if (category.id != 0L) category.id else nextId++
        categories.update { it + category.copy(id = id) }
        return id
    }

    override suspend fun updateCategory(category: Category) {
        errorToThrow?.let { throw it }
        categories.update { list -> list.map { if (it.id == category.id) category else it } }
    }

    override suspend fun deleteCategory(category: Category) {
        errorToThrow?.let { throw it }
        categories.update { list -> list.filterNot { it.id == category.id } }
    }

    override suspend fun deleteAllCategories() {
        errorToThrow?.let { throw it }
        categories.value = categories.value.filter { it.isDefault }
    }

    override fun getCategoriesByType(type: String): Flow<List<Category>> =
        categories.map { list -> list.filter { it.type == type } }

    override suspend fun getDefaultCategoryCount(): Int = categories.value.count { it.isDefault }

    /**
     * Helper method for tests: remove a category by ID
     */
    fun removeCategory(id: Long) {
        categories.update { list -> list.filterNot { it.id == id } }
    }
}
