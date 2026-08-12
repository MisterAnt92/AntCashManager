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

        assertEquals(
            sampleCategory(id = 7L, name = "Hobby", sortOrder = 3, isHidden = true),
            result.first()
        )
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

     @Test
     fun getCategoryById_shouldReturnMappedCategory_whenDaoReturnsEntity() = runTest {
         val entity = sampleEntity(id = 3L, name = "Utilities")
         coEvery { categoryDao.getCategoryById(3L) } returns entity

         val result = repository.getCategoryById(3L)

         assertEquals(sampleCategory(id = 3L, name = "Utilities"), result)
         coVerify(exactly = 1) { categoryDao.getCategoryById(3L) }
     }

     @Test
     fun getCategoryById_shouldReturnNull_whenDaoReturnsNull() = runTest {
         coEvery { categoryDao.getCategoryById(999L) } returns null

         val result = repository.getCategoryById(999L)

         assertNull(result)
     }

     @Test
     fun insertCategory_shouldCallDaoAndReturnCategoryId_whenCategoryProvided() = runTest {
         val category = sampleCategory(id = 4L, name = "Health")
         val expectedId = 4L
         coEvery { categoryDao.insertCategory(any()) } returns expectedId

         val result = repository.insertCategory(category)

         assertEquals(expectedId, result)
         coVerify(exactly = 1) { categoryDao.insertCategory(any()) }
     }

     @Test
     fun updateCategory_shouldCallDaoWithMappedEntity_whenCategoryProvided() = runTest {
         val category = sampleCategory(id = 5L, name = "Sports", sortOrder = 2)

         repository.updateCategory(category)

         coVerify(exactly = 1) {
             categoryDao.updateCategory(
                 match {
                     it.id == category.id && it.name == category.name && it.sortOrder == category.sortOrder
                 },
             )
         }
     }

     @Test
     fun getCategoriesByType_shouldReturnMappedCategoriesFilteredByType_whenDaoEmitsEntities() = runTest {
         val expenseEntity = sampleEntity(id = 6L, name = "Transport", sortOrder = 1)
             .copy(type = "EXPENSE")
         val incomeEntity = sampleEntity(id = 7L, name = "Salary", sortOrder = 0)
             .copy(type = "INCOME")
         every { categoryDao.getCategoriesByType("EXPENSE") } returns flowOf(listOf(expenseEntity))

         val result = repository.getCategoriesByType("EXPENSE").first()

         assertEquals(1, result.size)
         assertEquals("Transport", result.first().name)
         assertEquals("EXPENSE", result.first().type)
     }

     @Test
     fun getDefaultCategoryCount_shouldReturnCountOfDefaultCategories_whenQueried() = runTest {
         coEvery { categoryDao.getDefaultCategoryCount() } returns 5

         val result = repository.getDefaultCategoryCount()

         assertEquals(5, result)
         coVerify(exactly = 1) { categoryDao.getDefaultCategoryCount() }
     }

     @Test
     fun updateCategory_shouldPreserveAllFields_whenCategoryHasComplexData() = runTest {
         val category = sampleCategory(
             id = 8L,
             name = "Updated Category",
             isDefault = false,
             sortOrder = 10,
             isHidden = true,
         )

         repository.updateCategory(category)

         coVerify(exactly = 1) {
             categoryDao.updateCategory(
                 match {
                     it.id == 8L &&
                     it.name == "Updated Category" &&
                     it.isDefault == false &&
                     it.sortOrder == 10 &&
                     it.isHidden == true
                 },
             )
         }
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

