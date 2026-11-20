package com.antcashmanager.domain.usecase.category

import com.antcashmanager.domain.model.ALL_DEFAULT_CATEGORIES
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.usecase.base.NoParamsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first

/**
 * Garantisce che tutte le categorie di default ([ALL_DEFAULT_CATEGORIES]) siano presenti nel
 * profilo attivo. L'operazione è idempotente: inserisce solo le categorie mancanti (identificate
 * per `name + type`) e non modifica né elimina le categorie custom dell'utente.
 *
 * Va chiamata all'avvio (dopo ProfileManager.initialize) e alla creazione di ogni nuovo profilo.
 */
public class EnsureDefaultCategoriesUseCase(
    private val categoryRepository: CategoryRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsUseCase<Unit>(dispatcher) {

    override suspend fun execute(params: Unit) {
        val existing = categoryRepository.getAllCategories().first()
        ALL_DEFAULT_CATEGORIES.forEach { default ->
            val exists = existing.any { it.name == default.name && it.type == default.type }
            if (!exists) {
                categoryRepository.insertCategory(default)
            }
        }
    }
}
