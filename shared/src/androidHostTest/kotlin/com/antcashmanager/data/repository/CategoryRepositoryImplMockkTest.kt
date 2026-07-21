package com.antcashmanager.data.repository

import com.antcashmanager.data.local.dao.CategoryDao
import com.antcashmanager.data.local.entity.CategoryEntity
import com.antcashmanager.domain.model.Category
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryRepositoryImplMockkTest {

    private lateinit var categoryDao: CategoryDao
    private lateinit var repository: CategoryRepositoryImpl

    @Before
    fun setup() {
        categoryDao = mockk(relaxed = true)
        repository = CategoryRepositoryImpl(categoryDao)
    }

    @Test
    fun getAllCategories_shouldMapEntitiesToDomain_whenDaoEmitsCategories() = runTest {
        val entity = sampleEntity(id = 5L, name = "Food", isDefault = true)
        every { categoryDao.getAllCategories() } returns flowOf(listOf(entity))

        val result = repository.getAllCategories().first()

        assertEquals(1, result.size)
        assertEquals(sampleCategory(id = 5L, name = "Food", isDefault = true), result.first())
        verify(exactly = 1) { categoryDao.getAllCategories() }
    }

    @Test
    fun getAllCategories_shouldMapSortOrderAndIsHidden_whenEntityHasNonDefaultValues() = runTest {
        val entity = sampleEntity(id = 7L, name = "Hobby", sortOrder = 3, isHidden = true)
        every { categoryDao.getAllCategories() } returns flowOf(listOf(entity))

        val result = repository.getAllCategories().first()

        assertEquals(sampleCategory(id = 7L, name = "Hobby", sortOrder = 3, isHidden = true), result.first())
    }

    @Test
    fun getCategoryByName_shouldReturnMappedCategory_whenDaoReturnsEntity() = runTest {
        val entity = sampleEntity(id = 9L, name = "Transport")
        coEvery { categoryDao.getCategoryByName("Transport") } returns entity

        val result = repository.getCategoryByName("Transport")

        assertEquals(sampleCategory(id = 9L, name = "Transport"), result)
        coVerify(exactly = 1) { categoryDao.getCategoryByName("Transport") }
    }

    @Test
    fun getCategoryByName_shouldReturnNull_whenDaoReturnsNull() = runTest {
        coEvery { categoryDao.getCategoryByName("Missing") } returns null

        val result = repository.getCategoryByName("Missing")

        assertNull(result)
        coVerify(exactly = 1) { categoryDao.getCategoryByName("Missing") }
    }

    @Test
    fun deleteCategory_shouldNotCallDao_whenCategoryIsDefault() = runTest {
        val category = sampleCategory(id = 1L, name = "Income", isDefault = true)

        repository.deleteCategory(category)

        coVerify(exactly = 0) { categoryDao.deleteCategory(any()) }
    }

    @Test
    fun deleteCategory_shouldCallDao_whenCategoryIsNotDefault() = runTest {
        val category = sampleCategory(id = 2L, name = "Food", isDefault = false)

        repository.deleteCategory(category)

        coVerify(exactly = 1) {
            categoryDao.deleteCategory(
                match {
                    it.id == category.id &&
                            it.name == category.name &&
                            it.icon == category.icon &&
                            it.color == category.color &&
                            it.type == category.type &&
                            it.isDefault == category.isDefault
                },
            )
        }
    }

    @Test
    fun deleteAllCategories_shouldDeleteOnlyNonDefaultCategories_whenCalled() = runTest {
        repository.deleteAllCategories()

        coVerify(exactly = 1) { categoryDao.deleteAllNonDefaultCategories() }
        coVerify(exactly = 0) { categoryDao.deleteAllCategories() }
    }

    private fun sampleEntity(
        id: Long,
        name: String,
        isDefault: Boolean = false,
        sortOrder: Int = 0,
        isHidden: Boolean = false,
    ): CategoryEntity = CategoryEntity(
        id = id,
        name = name,
        icon = "category",
        color = 0xFF90A4AE,
        type = "EXPENSE",
        isDefault = isDefault,
        sortOrder = sortOrder,
        isHidden = isHidden,
    )

    private fun sampleCategory(
        id: Long,
        name: String,
        isDefault: Boolean = false,
        sortOrder: Int = 0,
        isHidden: Boolean = false,
    ): Category = Category(
        id = id,
        name = name,
        icon = "category",
        color = 0xFF90A4AE,
        type = "EXPENSE",
        isDefault = isDefault,
        sortOrder = sortOrder,
        isHidden = isHidden,
    )
}

