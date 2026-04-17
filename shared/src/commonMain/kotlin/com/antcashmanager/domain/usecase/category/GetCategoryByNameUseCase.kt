package com.antcashmanager.domain.usecase.category

import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.usecase.BaseUseCase

/**
 * UseCase per recuperare una categoria dato il suo nome.
 * Se la categoria non viene trovata, restituisce una categoria di fallback "Non categorizzato".
 */
class GetCategoryByNameUseCase(
    private val categoryRepository: CategoryRepository,
) : BaseUseCase<GetCategoryByNameUseCase.Params, Category>() {

    override suspend fun invoke(params: Params): Category {
        val category = categoryRepository.getCategoryByName(params.name)

        // Se la categoria esiste, restituiscila
        if (category != null) {
            return category
        }

        // Altrimenti, restituisci categoria fallback "Non categorizzato"
        return Category(
            id = 0,
            name = "Non categorizzato",
            icon = "more_horiz",
            color = 0xFF90A4AE,
            type = params.type,
            isDefault = true,
        )
    }

    data class Params(
        val name: String,
        val type: String = "EXPENSE",
    )
}

