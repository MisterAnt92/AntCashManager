package com.antcashmanager.domain.usecase.category

import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.usecase.BaseUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * UseCase per l'aggiornamento di una categoria.
 */
class UpdateCategoryUseCase(
    private val categoryRepository: CategoryRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : BaseUseCase<Category, Result<Unit>>(dispatcher) {

    override suspend fun execute(params: Category): Result<Unit> = runCatching {
        categoryRepository.updateCategory(params)
    }
}
