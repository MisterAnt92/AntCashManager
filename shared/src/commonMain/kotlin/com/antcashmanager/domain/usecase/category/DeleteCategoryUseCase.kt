package com.antcashmanager.domain.usecase.category
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.usecase.BaseUseCase
class DeleteCategoryUseCase(
    private val categoryRepository: CategoryRepository,
) : BaseUseCase<Category, Unit>() {
    override suspend fun invoke(params: Category) =
        categoryRepository.deleteCategory(params)
}
