package com.antcashmanager.domain.usecase.category

import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GetCategoryByNameUseCaseMockkTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var categoryRepository: CategoryRepository

    @BeforeTest
    fun setup() {
        categoryRepository = mockk()
    }

    @Test
    fun invoke_shouldReturnRepositoryCategory_whenCategoryExists() = runTest(dispatcher) {
        val expected = Category(
            id = 12L,
            name = "Food",
            icon = "restaurant",
            color = 0xFFE57373,
            type = "EXPENSE",
            isDefault = true,
        )
        coEvery { categoryRepository.getCategoryByName("Food") } returns expected
        val useCase = GetCategoryByNameUseCase(categoryRepository)

        val result = useCase(GetCategoryByNameUseCase.Params(name = "Food", type = "EXPENSE"))

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrThrow())
        coVerify(exactly = 1) { categoryRepository.getCategoryByName("Food") }
    }

    @Test
    fun invoke_shouldReturnFallbackCategory_whenCategoryDoesNotExist() = runTest(dispatcher) {
        coEvery { categoryRepository.getCategoryByName("Unknown") } returns null
        val useCase = GetCategoryByNameUseCase(categoryRepository)

        val result = useCase(GetCategoryByNameUseCase.Params(name = "Unknown", type = "INCOME"))

        assertTrue(result.isSuccess)
        val fallback = result.getOrThrow()
        assertEquals(0L, fallback.id)
        assertEquals("Non categorizzato", fallback.name)
        assertEquals("INCOME", fallback.type)
        assertTrue(fallback.isDefault)
        coVerify(exactly = 1) { categoryRepository.getCategoryByName("Unknown") }
    }

    @Test
    fun invoke_shouldReturnFailure_whenRepositoryThrows() = runTest(dispatcher) {
        val expectedError = IllegalStateException("category-read-failed")
        coEvery { categoryRepository.getCategoryByName("Food") } throws expectedError
        val useCase = GetCategoryByNameUseCase(categoryRepository)

        val result = useCase(GetCategoryByNameUseCase.Params(name = "Food"))

        assertTrue(result.isFailure)
        assertEquals(expectedError, result.exceptionOrNull())
        coVerify(exactly = 1) { categoryRepository.getCategoryByName("Food") }
    }
}

