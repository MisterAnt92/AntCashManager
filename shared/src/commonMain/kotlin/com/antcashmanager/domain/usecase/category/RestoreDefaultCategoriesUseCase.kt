package com.antcashmanager.domain.usecase.category

import com.antcashmanager.domain.model.ALL_DEFAULT_CATEGORIES
import com.antcashmanager.domain.repository.CategoryRepository
import com.antcashmanager.domain.usecase.base.NoParamsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first

/**
 * Ripristina le categorie di default mancanti per il profilo attivo.
 *
 * Confronta le categorie di default esistenti per nome+tipo con [ALL_DEFAULT_CATEGORIES]
 * e inserisce solo quelle assenti, evitando duplicati anche se l'utente ha rinominato
 * alcune categorie di default.
 */
public class RestoreDefaultCategoriesUseCase(
    private val categoryRepository: CategoryRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsUseCase<Unit>(dispatcher) {

    override suspend fun execute(params: Unit) {
        val existing = categoryRepository.getAllCategories().first()
        val existingDefaultKeys = existing
            .filter { it.isDefault }
            .map { "${it.name}|${it.type}" }
            .toSet()

        ALL_DEFAULT_CATEGORIES
            .filter { "${it.name}|${it.type}" !in existingDefaultKeys }
            .forEach { categoryRepository.insertCategory(it) }
    }
}
