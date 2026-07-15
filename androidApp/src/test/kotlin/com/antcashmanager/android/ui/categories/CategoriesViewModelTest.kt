package com.antcashmanager.android.ui.categories

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.ui.screen.categories.CategoriesViewModel
import com.antcashmanager.domain.exception.CategoryException
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.usecase.category.DeleteCategoryUseCase
import com.antcashmanager.domain.usecase.category.GetCategoriesUseCase
import com.antcashmanager.domain.usecase.category.InsertCategoryUseCase
import com.antcashmanager.domain.usecase.category.UpdateCategoryUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest : BaseUnitTest() {

    private lateinit var getCategoriesUseCase: GetCategoriesUseCase
    private lateinit var insertCategoryUseCase: InsertCategoryUseCase
    private lateinit var updateCategoryUseCase: UpdateCategoryUseCase
    private lateinit var deleteCategoryUseCase: DeleteCategoryUseCase

    @Before
    fun setup() {
        getCategoriesUseCase = mockk()
        insertCategoryUseCase = mockk()
        updateCategoryUseCase = mockk()
        deleteCategoryUseCase = mockk()

        every { getCategoriesUseCase() } returns flowOf(Result.success(emptyList()))
        coEvery { insertCategoryUseCase(any()) } returns Result.success(1L)
        coEvery { updateCategoryUseCase(any()) } returns Result.success(Unit)
        coEvery { deleteCategoryUseCase(any()) } returns Result.success(Unit)
    }

    // ── HAPPY PATH ──────────────────────────────────────────────────────────

    @Test
    fun state_shouldHaveEmptyCategories_whenGetCategoriesUseCaseReturnsEmptyList() = runViewModelTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.categories.isEmpty())
    }

    @Test
    fun addCategory_shouldInvokeInsertUseCase_whenAddingExpenseCategory() = runViewModelTest {
        val viewModel = buildViewModel()

        viewModel.addCategory("Food", "category", 0xFFE57373, "EXPENSE")
        advanceUntilIdle()

        coVerify(exactly = 1) {
            insertCategoryUseCase(match { it.name == "Food" && it.type == "EXPENSE" })
        }
    }

    @Test
    fun updateCategory_shouldInvokeUpdateUseCase_whenCalled() = runViewModelTest {
        val viewModel = buildViewModel()
        val category = Category(id = 1, name = "Food", icon = "category", color = 0xFFE57373)

        viewModel.updateCategory(category)
        advanceUntilIdle()

        coVerify(exactly = 1) { updateCategoryUseCase(category) }
    }

    @Test
    fun deleteCategory_shouldInvokeDeleteUseCase_whenCalled() = runViewModelTest {
        val viewModel = buildViewModel()
        val category = Category(id = 1, name = "Food", icon = "category", color = 0xFFE57373)

        viewModel.deleteCategory(category)
        advanceUntilIdle()

        coVerify(exactly = 1) { deleteCategoryUseCase(category) }
    }

    @Test
    fun state_shouldContainCategories_whenGetCategoriesUseCaseSucceeds() = runViewModelTest {
        val categories = listOf(
            Category(id = 1, name = "Food", icon = "category", color = 0xFFE57373, type = "EXPENSE"),
            Category(id = 2, name = "Salary", icon = "payments", color = 0xFF81C784, type = "INCOME"),
        )
        every { getCategoriesUseCase() } returns flowOf(Result.success(categories))
        val viewModel = buildViewModel()

        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.categories.size)
        assertEquals("Food", viewModel.state.value.categories[0].name)
        assertEquals("Salary", viewModel.state.value.categories[1].name)
    }

    @Test
    fun state_shouldFilterExpenseCategories_whenCategoriesContainMixedTypes() = runViewModelTest {
        val categories = listOf(
            Category(id = 1, name = "Food", icon = "category", color = 0xFFE57373, type = "EXPENSE"),
            Category(id = 2, name = "Salary", icon = "payments", color = 0xFF81C784, type = "INCOME"),
        )
        every { getCategoriesUseCase() } returns flowOf(Result.success(categories))
        val viewModel = buildViewModel()

        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.expenseCategories.size)
        assertEquals("Food", viewModel.state.value.expenseCategories.first().name)
    }

    @Test
    fun state_shouldFilterIncomeCategories_whenCategoriesContainMixedTypes() = runViewModelTest {
        val categories = listOf(
            Category(id = 1, name = "Food", icon = "category", color = 0xFFE57373, type = "EXPENSE"),
            Category(id = 2, name = "Salary", icon = "payments", color = 0xFF81C784, type = "INCOME"),
        )
        every { getCategoriesUseCase() } returns flowOf(Result.success(categories))
        val viewModel = buildViewModel()

        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.incomeCategories.size)
        assertEquals("Salary", viewModel.state.value.incomeCategories.first().name)
    }

    // ── ERROR HANDLING ──────────────────────────────────────────────────────

    @Test
    fun addCategory_shouldNotUpdateState_whenInsertUseCaseReturnsDuplicateNameFailure() = runViewModelTest {
        coEvery { insertCategoryUseCase(any()) } returns
            Result.failure(CategoryException.DuplicateName("Food"))
        val viewModel = buildViewModel()

        viewModel.addCategory("Food", "category", 0xFFE57373, "EXPENSE")
        advanceUntilIdle()

        coVerify(exactly = 1) { insertCategoryUseCase(any()) }
        assertTrue(viewModel.state.value.categories.isEmpty())
    }

    @Test
    fun addCategory_shouldNotUpdateState_whenInsertUseCaseReturnsInvalidAmountFailure() = runViewModelTest {
        coEvery { insertCategoryUseCase(any()) } returns
            Result.failure(IllegalArgumentException("Invalid amount"))
        val viewModel = buildViewModel()

        viewModel.addCategory("Food", "category", 0xFFE57373, "EXPENSE")
        advanceUntilIdle()

        coVerify(exactly = 1) { insertCategoryUseCase(any()) }
        assertTrue(viewModel.state.value.categories.isEmpty())
    }

    @Test
    fun updateCategory_shouldCompleteWithoutThrowing_whenUpdateUseCaseReturnsNotFoundFailure() = runViewModelTest {
        coEvery { updateCategoryUseCase(any()) } returns
            Result.failure(CategoryException.NotFound("Nonexistent"))
        val viewModel = buildViewModel()
        val category = Category(id = 99, name = "Nonexistent", icon = "icon", color = 0xFF000000)

        viewModel.updateCategory(category)
        advanceUntilIdle()

        coVerify(exactly = 1) { updateCategoryUseCase(category) }
    }

    @Test
    fun deleteCategory_shouldCompleteWithoutThrowing_whenDeleteUseCaseReturnsNotFoundFailure() = runViewModelTest {
        coEvery { deleteCategoryUseCase(any()) } returns
            Result.failure(CategoryException.NotFound("Nonexistent"))
        val viewModel = buildViewModel()
        val category = Category(id = 99, name = "Nonexistent", icon = "icon", color = 0xFF000000)

        viewModel.deleteCategory(category)
        advanceUntilIdle()

        coVerify(exactly = 1) { deleteCategoryUseCase(category) }
    }

    @Test
    fun state_shouldHaveEmptyCategories_whenGetCategoriesUseCaseFails() = runViewModelTest {
        every { getCategoriesUseCase() } returns
            flowOf(Result.failure(RuntimeException("Failed to load categories")))
        val viewModel = buildViewModel()

        advanceUntilIdle()

        assertTrue(viewModel.state.value.categories.isEmpty())
    }

    private fun buildViewModel(): CategoriesViewModel = CategoriesViewModel(
        getCategoriesUseCase = getCategoriesUseCase,
        insertCategoryUseCase = insertCategoryUseCase,
        updateCategoryUseCase = updateCategoryUseCase,
        deleteCategoryUseCase = deleteCategoryUseCase,
    )
}
