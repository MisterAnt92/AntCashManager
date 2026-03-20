package com.antcashmanager.domain.usecase.category
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.usecase.NoParamsFlowUseCase
import kotlinx.coroutines.flow.Flow
class GetCategoriesUseCase(
    private val categoryRepository: CategoryRepository,
) : NoParamsFlowUseCase<List<Category>>() {
    override fun invoke(): Flow<List<Category>> =
        categoryRepository.getAllCategories()
}
