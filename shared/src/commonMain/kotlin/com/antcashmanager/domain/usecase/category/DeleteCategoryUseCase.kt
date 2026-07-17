package com.antcashmanager.domain.usecase.category

import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.usecase.BaseResultUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * UseCase per la cancellazione di una categoria.
 */
class DeleteCategoryUseCase(
    private val categoryRepository: CategoryRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : BaseResultUseCase<Category, Unit>(dispatcher) {

    override suspend fun execute(params: Category): Unit =
        categoryRepository.deleteCategory(params)
}
