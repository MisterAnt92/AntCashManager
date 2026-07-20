package com.antcashmanager.domain.usecase.category

import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.usecase.BaseResultUseCase

/**
 * UseCase per recuperare una categoria dato il suo nome.
 * Se la categoria non viene trovata, restituisce una categoria di fallback "Non categorizzato".
 */
class GetCategoryByNameUseCase(
    private val categoryRepository: CategoryRepository,
) : BaseResultUseCase<GetCategoryByNameUseCase.Params, Category>() {

    override suspend fun execute(params: Params): Category {
        val category = categoryRepository.getCategoryByName(params.name)

        if (category != null) return category

        // Fallback "Non categorizzato"
        return Category(
            id = 0,
            name = "Non categorizzato",
            icon = "more_horiz",
            color = 0xFF90A4AEL,
            type = params.type,
            isDefault = true,
        )
    }

    data class Params(
        val name: String,
        val type: String = "EXPENSE",
    )
}
