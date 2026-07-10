package com.antcashmanager.android.ui.categories

import com.antcashmanager.android.BaseUnitTest
import com.antcashmanager.android.ui.screen.categories.CategoriesViewModel
import com.antcashmanager.domain.exception.CategoryException
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.usecase.category.DeleteCategoryUseCase
import com.antcashmanager.domain.usecase.category.GetCategoriesUseCase
import com.antcashmanager.domain.usecase.category.InsertCategoryUseCase
import com.antcashmanager.domain.usecase.category.UpdateCategoryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest : BaseUnitTest() {
    private lateinit var fakeGetCategoriesUseCase: FakeGetCategoriesUseCase
    private lateinit var fakeInsertCategoryUseCase: FakeInsertCategoryUseCase
    private lateinit var fakeUpdateCategoryUseCase: FakeUpdateCategoryUseCase
    private lateinit var fakeDeleteCategoryUseCase: FakeDeleteCategoryUseCase
    private lateinit var viewModel: CategoriesViewModel

    @Before
    fun setup() {
        fakeGetCategoriesUseCase = FakeGetCategoriesUseCase(testDispatcher)
        fakeInsertCategoryUseCase = FakeInsertCategoryUseCase(testDispatcher)
        fakeUpdateCategoryUseCase = FakeUpdateCategoryUseCase(testDispatcher)
        fakeDeleteCategoryUseCase = FakeDeleteCategoryUseCase(testDispatcher)
        viewModel = CategoriesViewModel(
            fakeGetCategoriesUseCase,
            fakeInsertCategoryUseCase,
            fakeUpdateCategoryUseCase,
            fakeDeleteCategoryUseCase,
        )
    }

    // ── HAPPY PATH ──────────────────────────────────────────────────────────

    @Test
    fun `initial categories list is empty`() = runViewModelTest {
        advanceUntilIdle()
        assertTrue(viewModel.state.value.categories.isEmpty())
    }

    @Test
    fun `addCategory should add new expense category`() = runViewModelTest {
        val category = Category(
            id = 1,
            name = "Food",
            icon = "category",
            color = 0xFFE57373,
            type = "EXPENSE"
        )
        fakeInsertCategoryUseCase.dataToReturn = category.id

        viewModel.addCategory("Food", "category", 0xFFE57373, "EXPENSE")
        advanceUntilIdle()

        assertTrue(fakeInsertCategoryUseCase.wasInvoked)
    }

    @Test
    fun `updateCategory should call use case`() = runViewModelTest {
        val category = Category(id = 1, name = "Food", icon = "category", color = 0xFFE57373)

        viewModel.updateCategory(category)
        advanceUntilIdle()

        assertTrue(fakeUpdateCategoryUseCase.wasInvoked)
    }

    @Test
    fun `deleteCategory should call use case`() = runViewModelTest {
        val category = Category(id = 1, name = "Food", icon = "category", color = 0xFFE57373)

        viewModel.deleteCategory(category)
        advanceUntilIdle()

        assertTrue(fakeDeleteCategoryUseCase.wasInvoked)
    }

    @Test
    fun `getAllCategories should return success with list of categories`() = runViewModelTest {
        val categories = listOf(
            Category(id = 1, name = "Food", icon = "category", color = 0xFFE57373, type = "EXPENSE"),
            Category(id = 2, name = "Salary", icon = "payments", color = 0xFF81C784, type = "INCOME"),
        )
        fakeGetCategoriesUseCase.categoriesToReturn = categories

        // ViewModel should load categories on init
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.categories.size)
        assertEquals("Food", viewModel.state.value.categories[0].name)
        assertEquals("Salary", viewModel.state.value.categories[1].name)
    }

    @Test
    fun `expenseCategories should filter by EXPENSE type`() = runViewModelTest {
        val categories = listOf(
            Category(id = 1, name = "Food", icon = "category", color = 0xFFE57373, type = "EXPENSE"),
            Category(id = 2, name = "Salary", icon = "payments", color = 0xFF81C784, type = "INCOME"),
        )
        fakeGetCategoriesUseCase.categoriesToReturn = categories

        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.expenseCategories.size)
        assertEquals("Food", viewModel.state.value.expenseCategories.first().name)
    }

    @Test
    fun `incomeCategories should filter by INCOME type`() = runViewModelTest {
        val categories = listOf(
            Category(id = 1, name = "Food", icon = "category", color = 0xFFE57373, type = "EXPENSE"),
            Category(id = 2, name = "Salary", icon = "payments", color = 0xFF81C784, type = "INCOME"),
        )
        fakeGetCategoriesUseCase.categoriesToReturn = categories

        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.incomeCategories.size)
        assertEquals("Salary", viewModel.state.value.incomeCategories.first().name)
    }

    // ── ERROR HANDLING ──────────────────────────────────────────────────────

    @Test
    fun `addCategory should handle DuplicateName exception`() = runViewModelTest {
        fakeInsertCategoryUseCase.shouldThrowDuplicateName = true

        viewModel.addCategory("Food", "category", 0xFFE57373, "EXPENSE")
        advanceUntilIdle()

        assertTrue(fakeInsertCategoryUseCase.wasInvoked)
        // State should remain unchanged on error
        assertTrue(viewModel.state.value.categories.isEmpty())
    }

    @Test
    fun `addCategory should handle InvalidAmount exception`() = runViewModelTest {
        fakeInsertCategoryUseCase.shouldThrowInvalidAmount = true

        viewModel.addCategory("Food", "category", 0xFFE57373, "EXPENSE")
        advanceUntilIdle()

        assertTrue(fakeInsertCategoryUseCase.wasInvoked)
        assertTrue(viewModel.state.value.categories.isEmpty())
    }

    @Test
    fun `updateCategory should handle NotFound exception`() = runViewModelTest {
        fakeUpdateCategoryUseCase.shouldThrowNotFound = true
        val category = Category(id = 99, name = "Nonexistent", icon = "icon", color = 0xFF000000)

        viewModel.updateCategory(category)
        advanceUntilIdle()

        assertTrue(fakeUpdateCategoryUseCase.wasInvoked)
    }

    @Test
    fun `deleteCategory should handle NotFound exception`() = runViewModelTest {
        fakeDeleteCategoryUseCase.shouldThrowNotFound = true
        val category = Category(id = 99, name = "Nonexistent", icon = "icon", color = 0xFF000000)

        viewModel.deleteCategory(category)
        advanceUntilIdle()

        assertTrue(fakeDeleteCategoryUseCase.wasInvoked)
    }

    @Test
    fun `getAllCategories should handle failure on init`() = runViewModelTest {
        // Create new UseCase that returns failure
        val failingGetUseCase = FakeGetCategoriesUseCase(testDispatcher)
        failingGetUseCase.shouldThrow = true

        val vm = CategoriesViewModel(
            failingGetUseCase,
            fakeInsertCategoryUseCase,
            fakeUpdateCategoryUseCase,
            fakeDeleteCategoryUseCase,
        )

        advanceUntilIdle()

        // State should have empty categories after failure
        assertTrue(vm.state.value.categories.isEmpty())
    }

}

// ── FAKE USE CASES ──────────────────────────────────────────────────────────

private class FakeGetCategoriesUseCase(
    private val testDispatcher: TestDispatcher,
) : GetCategoriesUseCase(FakeCategoryRepository()) {

    var categoriesToReturn: List<Category> = emptyList()
    var shouldThrow = false

    override fun invoke(): Flow<Result<List<Category>>> = flow {
        if (shouldThrow) {
            emit(Result.failure(RuntimeException("Failed to load categories")))
        } else {
            emit(Result.success(categoriesToReturn))
        }
    }.let { flowOn(it, testDispatcher) }

    private fun flowOn(flow: Flow<Result<List<Category>>>, dispatcher: TestDispatcher): Flow<Result<List<Category>>> =
        flow.flowOn(dispatcher)
}

private class FakeInsertCategoryUseCase(
    private val testDispatcher: TestDispatcher,
) : InsertCategoryUseCase(FakeCategoryRepository()) {

    var dataToReturn: Long = 1L
    var shouldThrowDuplicateName = false
    var shouldThrowInvalidAmount = false
    var delayMs: Long = 0
    var onComplete: (() -> Unit)? = null
    var wasInvoked = false

    override suspend fun invoke(params: Category): Result<Long> {
        wasInvoked = true
        if (delayMs > 0) delay(delayMs)

        return when {
            shouldThrowDuplicateName -> Result.failure(
                CategoryException.DuplicateName(params.name)
            )

            shouldThrowInvalidAmount -> Result.failure(
                RuntimeException("Invalid amount")
            )

            else -> {
                onComplete?.invoke()
                Result.success(dataToReturn)
            }
        }
    }
}

private class FakeUpdateCategoryUseCase(
    private val testDispatcher: TestDispatcher,
) : UpdateCategoryUseCase(FakeCategoryRepository()) {

    var shouldThrowNotFound = false
    var delayMs: Long = 0
    var onComplete: (() -> Unit)? = null
    var wasInvoked = false

    override suspend fun invoke(params: Category): Result<Unit> {
        wasInvoked = true
        if (delayMs > 0) delay(delayMs)

        return when {
            shouldThrowNotFound -> Result.failure(
                CategoryException.NotFound(params.name)
            )

            else -> {
                onComplete?.invoke()
                Result.success(Unit)
            }
        }
    }
}

private class FakeDeleteCategoryUseCase(
    private val testDispatcher: TestDispatcher,
) : DeleteCategoryUseCase(FakeCategoryRepository()) {

    var shouldThrowNotFound = false
    var delayMs: Long = 0
    var onComplete: (() -> Unit)? = null
    var wasInvoked = false

    override suspend fun invoke(params: Category): Result<Unit> {
        wasInvoked = true
        if (delayMs > 0) delay(delayMs)

        return when {
            shouldThrowNotFound -> Result.failure(
                CategoryException.NotFound(params.name)
            )

            else -> {
                onComplete?.invoke()
                Result.success(Unit)
            }
        }
    }
}

private class FakeCategoryRepository : CategoryRepository {
    val categories = MutableStateFlow<List<Category>>(emptyList())

    override fun getAllCategories(): Flow<List<Category>> = categories

    override suspend fun getCategoryById(id: Long): Category? =
        categories.value.find { it.id == id }

    override suspend fun getCategoryByName(name: String): Category? =
        categories.value.find { it.name == name }

    override suspend fun insertCategory(category: Category): Long {
        categories.value += category
        return category.id
    }

    override suspend fun updateCategory(category: Category) {
        categories.value = categories.value.map { if (it.id == category.id) category else it }
    }

    override suspend fun deleteCategory(category: Category) {
        categories.value = categories.value.filter { it.id != category.id }
    }

    override suspend fun deleteAllCategories() {
        categories.value = emptyList()
    }

    override fun getCategoriesByType(type: String): Flow<List<Category>> =
        categories.map { list -> list.filter { it.type == type } }

    override suspend fun getDefaultCategoryCount(): Int =
        categories.value.count { it.isDefault }
}



