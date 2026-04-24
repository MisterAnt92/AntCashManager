package com.antcashmanager.domain.usecase.transaction

import co.touchlab.kermit.Logger
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.BaseUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * UseCase per sincronizzare i dati categoria (icon, color) su tutte le transazioni
 * quando una categoria viene modificata.
 *
 * Il dispatcher è iniettabile per testabilità. Restituisce [Result<Unit>].
 */
class SyncTransactionCategoriesUseCase(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : BaseUseCase<SyncTransactionCategoriesUseCase.Params, Result<Unit>>(dispatcher) {

    override suspend fun execute(params: Params): Result<Unit> = runCatching {
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

