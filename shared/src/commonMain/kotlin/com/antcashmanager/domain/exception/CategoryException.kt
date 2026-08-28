package com.antcashmanager.domain.exception

/**
 * Eccezioni di dominio per le categorie.
 */
public sealed class CategoryException(message: String) : Exception(message) {
    public class NotFound(name: String) : CategoryException("Category '$name' not found")
    public class DuplicateName(name: String) : CategoryException("Category '$name' already exists")
}
