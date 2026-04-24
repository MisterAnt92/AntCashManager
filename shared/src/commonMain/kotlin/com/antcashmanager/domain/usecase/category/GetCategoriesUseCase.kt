package com.antcashmanager.domain.usecase.category

import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.usecase.NoParamsFlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * UseCase che fornisce la lista delle categorie come Flow di Result.
 */
class GetCategoriesUseCase(
    private val categoryRepository: CategoryRepository,
) : NoParamsFlowUseCase<Result<List<Category>>>() {

    override fun execute(): Flow<Result<List<Category>>> =
        categoryRepository.getAllCategories()
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
}
