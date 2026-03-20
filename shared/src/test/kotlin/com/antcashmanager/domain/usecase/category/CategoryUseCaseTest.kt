package com.antcashmanager.domain.usecase.category
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
    fun `GetCategoriesUseCase returns all categories`() = runTest {
        val category = Category(id = 1, name = "Food", icon = "category", color = 0xFFE57373)
        fakeRepo.categories.value = listOf(category)
        val result = getCategoriesUseCase().first()
        assertEquals(1, result.size)
        assertEquals("Food", result.first().name)
    }
    @Test
    fun `InsertCategoryUseCase adds category`() = runTest {
        val category = Category(name = "Transport", icon = "bus", color = 0xFF4FC3F7)
        insertCategoryUseCase(category)
        val result = getCategoriesUseCase().first()
        assertEquals(1, result.size)
    }
    @Test
    fun `DeleteCategoryUseCase removes category`() = runTest {
        val category = Category(id = 1, name = "Food", icon = "category", color = 0xFFE57373)
        fakeRepo.categories.value = listOf(category)
        deleteCategoryUseCase(category)
        val result = getCategoriesUseCase().first()
        assertTrue(result.isEmpty())
    }
    @Test
    fun `UpdateCategoryUseCase updates category`() = runTest {
        val category = Category(id = 1, name = "Food", icon = "category", color = 0xFFE57373)
        fakeRepo.categories.value = listOf(category)
        updateCategoryUseCase(category.copy(name = "Groceries"))
        val result = getCategoriesUseCase().first()
        assertEquals("Groceries", result.first().name)
    }
}
private class FakeCategoryRepository : CategoryRepository {
    val categories = MutableStateFlow<List<Category>>(emptyList())
    override fun getAllCategories(): Flow<List<Category>> = categories
    override suspend fun getCategoryById(id: Long): Category? = categories.value.find { it.id == id }
    override suspend fun insertCategory(category: Category): Long {
        categories.value = categories.value + category
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
