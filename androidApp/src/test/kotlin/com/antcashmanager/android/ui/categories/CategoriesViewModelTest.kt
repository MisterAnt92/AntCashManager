package com.antcashmanager.android.ui.categories
import com.antcashmanager.android.ui.screen.home.categories.CategoriesViewModel
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeCategoryRepository
    private lateinit var viewModel: CategoriesViewModel
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeCategoryRepository()
        viewModel = CategoriesViewModel(fakeRepo)
    }
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    @Test
    fun `initial categories list is empty`() = runTest(testDispatcher) {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.categories.collect {}
        }
        advanceUntilIdle()
        assertTrue(viewModel.categories.value.isEmpty())
        collectJob.cancel()
    }
    @Test
    fun `addCategory adds a new expense category`() = runTest(testDispatcher) {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.categories.collect {}
        }
        advanceUntilIdle()
        viewModel.addCategory("Food", "category", 0xFFE57373, "EXPENSE")
        advanceUntilIdle()
        assertEquals(1, viewModel.categories.value.size)
        assertEquals("Food", viewModel.categories.value.first().name)
        assertEquals("EXPENSE", viewModel.categories.value.first().type)
        collectJob.cancel()
    }

    @Test
    fun `addCategory adds a new income category`() = runTest(testDispatcher) {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.categories.collect {}
        }
        advanceUntilIdle()
        viewModel.addCategory("Salary", "payments", 0xFF81C784, "INCOME")
        advanceUntilIdle()
        assertEquals(1, viewModel.categories.value.size)
        assertEquals("Salary", viewModel.categories.value.first().name)
        assertEquals("INCOME", viewModel.categories.value.first().type)
        collectJob.cancel()
    }

    @Test
    fun `expenseCategories filters by EXPENSE type`() = runTest(testDispatcher) {
        fakeRepo.categories.value = listOf(
            Category(id = 1, name = "Food", icon = "category", color = 0xFFE57373, type = "EXPENSE"),
            Category(id = 2, name = "Salary", icon = "payments", color = 0xFF81C784, type = "INCOME"),
        )
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.expenseCategories.collect {}
        }
        advanceUntilIdle()
        assertEquals(1, viewModel.expenseCategories.value.size)
        assertEquals("Food", viewModel.expenseCategories.value.first().name)
        collectJob.cancel()
    }

    @Test
    fun `incomeCategories filters by INCOME type`() = runTest(testDispatcher) {
        fakeRepo.categories.value = listOf(
            Category(id = 1, name = "Food", icon = "category", color = 0xFFE57373, type = "EXPENSE"),
            Category(id = 2, name = "Salary", icon = "payments", color = 0xFF81C784, type = "INCOME"),
        )
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.incomeCategories.collect {}
        }
        advanceUntilIdle()
        assertEquals(1, viewModel.incomeCategories.value.size)
        assertEquals("Salary", viewModel.incomeCategories.value.first().name)
        collectJob.cancel()
    }
    @Test
    fun `deleteCategory removes category`() = runTest(testDispatcher) {
        val category = Category(id = 1, name = "Food", icon = "category", color = 0xFFE57373)
        fakeRepo.categories.value = listOf(category)
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.categories.collect {}
        }
        advanceUntilIdle()
        viewModel.deleteCategory(category)
        advanceUntilIdle()
        assertTrue(viewModel.categories.value.isEmpty())
        collectJob.cancel()
    }
    @Test
    fun `updateCategory updates existing category`() = runTest(testDispatcher) {
        val category = Category(id = 1, name = "Food", icon = "category", color = 0xFFE57373)
        fakeRepo.categories.value = listOf(category)
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.categories.collect {}
        }
        advanceUntilIdle()
        val updated = category.copy(name = "Groceries")
        viewModel.updateCategory(updated)
        advanceUntilIdle()
        assertEquals("Groceries", viewModel.categories.value.first().name)
        collectJob.cancel()
    }
}
private class FakeCategoryRepository : CategoryRepository {
    val categories = MutableStateFlow<List<Category>>(emptyList())
    override fun getAllCategories(): Flow<List<Category>> = categories
    override suspend fun getCategoryById(id: Long): Category? =
        categories.value.find { it.id == id }
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
