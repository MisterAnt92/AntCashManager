package com.antcashmanager.domain.usecase.transaction

import co.touchlab.kermit.Logger
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.BaseUseCase

/**
 * UseCase per sincronizzare i dati categoria (icon, color) su tutte le transazioni
 * quando una categoria viene modificata.
 */
class SyncTransactionCategoriesUseCase(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
) : BaseUseCase<SyncTransactionCategoriesUseCase.Params, Unit>() {

    override suspend fun invoke(params: Params) {
        Logger.d("SyncTransactionCategoriesUseCase") { "Syncing category data for: ${params.categoryName}" }

        val category = categoryRepository.getCategoryByName(params.categoryName)

        if (category != null) {
            transactionRepository.updateCategoryData(
                categoryName = params.categoryName,
                icon = category.icon,
                color = category.color
            )
            Logger.d("SyncTransactionCategoriesUseCase") { "Category data synced successfully" }
        } else {
            Logger.w("SyncTransactionCategoriesUseCase") { "Category not found: ${params.categoryName}" }
        }
    }

    data class Params(
        val categoryName: String,
    )
}

