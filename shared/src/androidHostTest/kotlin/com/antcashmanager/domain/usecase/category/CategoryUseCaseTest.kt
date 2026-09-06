package com.antcashmanager.domain.usecase.category

import com.antcashmanager.domain.model.Category
import com.antcashmanager.testutil.FakeCategoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CategoryUseCaseTest {
    private lateinit var fakeRepo: FakeCategoryRepository
    private lateinit var getCategoriesUseCase: GetCategoriesUseCase
    private lateinit var insertCategoryUseCase: InsertCategoryUseCase
    private lateinit var deleteCategoryUseCase: DeleteCategoryUseCase
    private lateinit var updateCategoryUseCase: UpdateCategoryUseCase

    @Before
    fun setup() {
        fakeRepo = FakeCategoryRepository()
        getCategoriesUseCase = GetCategoriesUseCase(fakeRepo)
        insertCategoryUseCase = InsertCategoryUseCase(fakeRepo)
        deleteCategoryUseCase = DeleteCategoryUseCase(fakeRepo)
        updateCategoryUseCase = UpdateCategoryUseCase(fakeRepo)
    }

    @Test
    fun getCategoriesUseCaseReturnsAllCategories() =
        runTest {
            val category = Category(id = 1, name = "Food", icon = "category", color = 0xFFE57373)
            fakeRepo.categories.value = listOf(category)
            val result = getCategoriesUseCase().first().getOrThrow()
            assertEquals(1, result.size)
            assertEquals("Food", result.first().name)
        }

    @Test
    fun insertCategoryUseCaseAddsCategory() =
        runTest {
            val category = Category(name = "Transport", icon = "bus", color = 0xFF4FC3F7)
            insertCategoryUseCase(category)
            val result = getCategoriesUseCase().first().getOrThrow()
            assertEquals(1, result.size)
        }

    @Test
    fun deleteCategoryUseCaseRemovesCategory() =
        runTest {
            val category = Category(id = 1, name = "Food", icon = "category", color = 0xFFE57373)
            fakeRepo.categories.value = listOf(category)
            deleteCategoryUseCase(category)
            val result = getCategoriesUseCase().first().getOrThrow()
            assertTrue(result.isEmpty())
        }

    @Test
    fun updateCategoryUseCaseUpdatesCategory() =
        runTest {
            val category = Category(id = 1, name = "Food", icon = "category", color = 0xFFE57373)
            fakeRepo.categories.value = listOf(category)
            updateCategoryUseCase(category.copy(name = "Groceries"))
            val result = getCategoriesUseCase().first().getOrThrow()
            assertEquals("Groceries", result.first().name)
        }
}
