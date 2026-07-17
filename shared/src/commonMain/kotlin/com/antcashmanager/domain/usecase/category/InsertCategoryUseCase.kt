package com.antcashmanager.domain.usecase.category

import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.usecase.BaseResultUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * UseCase per l'inserimento di una categoria.
 */
class InsertCategoryUseCase(
    private val categoryRepository: CategoryRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : BaseResultUseCase<Category, Long>(dispatcher) {

    override suspend fun execute(params: Category): Long =
        categoryRepository.insertCategory(params)
}
