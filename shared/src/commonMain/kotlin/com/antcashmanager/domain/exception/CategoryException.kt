package com.antcashmanager.domain.exception

/**
 * Eccezioni di dominio per le categorie.
 */
sealed class CategoryException(message: String) : Exception(message) {
    class NotFound(name: String) : CategoryException("Category '$name' not found")
    class DuplicateName(name: String) : CategoryException("Category '$name' already exists")
}
