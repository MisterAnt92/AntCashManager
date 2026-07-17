package com.antcashmanager.domain.usecase.transaction

import co.touchlab.kermit.Logger
import com.antcashmanager.domain.model.Category
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.BaseResultUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * UseCase per propagare la modifica di una categoria (nome, icona, colore) a tutte le
 * transazioni esistenti che referenziano ancora il nome precedente.
 *
 * Le transazioni salvano la categoria come stringa denormalizzata: senza questa
 * propagazione, rinominare una categoria lascia le transazioni esistenti "orfane" con
 * il nome vecchio, che compare come categoria a sé nei Grafici invece di essere
 * accorpata a quella rinominata.
 *
 * Il dispatcher è iniettabile per testabilità. Restituisce [Result<Unit>].
 */
class SyncTransactionCategoriesUseCase(
    private val transactionRepository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : BaseResultUseCase<SyncTransactionCategoriesUseCase.Params, Unit>(dispatcher) {

    override suspend fun execute(params: Params) {
        Logger.d("SyncTransactionCategoriesUseCase") {
            "Syncing category data: ${params.oldCategoryName} -> ${params.category.name}"
        }
        transactionRepository.renameCategory(
            oldCategoryName = params.oldCategoryName,
            newCategoryName = params.category.name,
            icon = params.category.icon,
            color = params.category.color,
        )
    }

    data class Params(
        val oldCategoryName: String,
        val category: Category,
    )
}
