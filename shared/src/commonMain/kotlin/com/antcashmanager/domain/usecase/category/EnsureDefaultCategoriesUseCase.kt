package com.antcashmanager.domain.usecase.category

import com.antcashmanager.domain.model.DEFAULT_EXPENSE_CATEGORIES
import com.antcashmanager.domain.model.DEFAULT_INCOME_CATEGORIES
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.usecase.base.NoParamsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first

/**
 * Garantisce che le categorie di default siano presenti nel profilo attivo.
 *
 * Strategia: inserisce le default solo se per quel tipo non esiste ancora NESSUNA categoria
 * con [Category.isDefault] = true. Se l'utente ha rinominato o riordinato le default, il
 * conteggio è già > 0 e non vengono inseriti duplicati.
 *
 * Va chiamata all'avvio (dopo ProfileManager.initialize) e alla creazione di ogni nuovo profilo.
 */
public class EnsureDefaultCategoriesUseCase(
    private val categoryRepository: CategoryRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsUseCase<Unit>(dispatcher) {

    override suspend fun execute(params: Unit) {
        val existing = categoryRepository.getAllCategories().first()
        val expenseDefaultCount = existing.count { it.isDefault && it.type == "EXPENSE" }
        val incomeDefaultCount = existing.count { it.isDefault && it.type == "INCOME" }

        if (expenseDefaultCount == 0) {
            DEFAULT_EXPENSE_CATEGORIES.forEach { categoryRepository.insertCategory(it) }
        }
        if (incomeDefaultCount == 0) {
            DEFAULT_INCOME_CATEGORIES.forEach { categoryRepository.insertCategory(it) }
        }
    }
}
