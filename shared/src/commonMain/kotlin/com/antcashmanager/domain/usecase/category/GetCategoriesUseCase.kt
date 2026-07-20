package com.antcashmanager.domain.usecase.category

import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.usecase.NoParamsResultFlowUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

/**
 * UseCase che fornisce la lista delle categorie come Flow di Result.
 */
class GetCategoriesUseCase(
    private val categoryRepository: CategoryRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsResultFlowUseCase<List<Category>>(dispatcher) {

    override fun execute(): Flow<List<Category>> = categoryRepository.getAllCategories()
}
