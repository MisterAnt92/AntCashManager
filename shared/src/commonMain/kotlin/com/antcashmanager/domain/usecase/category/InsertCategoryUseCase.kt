package com.antcashmanager.domain.usecase.category
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.usecase.BaseUseCase
class InsertCategoryUseCase(
    private val categoryRepository: CategoryRepository,
) : BaseUseCase<Category, Long>() {
    override suspend fun invoke(params: Category): Long =
        categoryRepository.insertCategory(params)
}
