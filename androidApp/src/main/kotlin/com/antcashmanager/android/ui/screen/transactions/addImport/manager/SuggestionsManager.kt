package com.antcashmanager.android.ui.screen.transactions.addImport.manager

import com.antcashmanager.domain.model.TransactionSuggestions
import com.antcashmanager.domain.usecase.transaction.GetTransactionSuggestionsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Manager responsabile del caricamento e della gestione dei suggerimenti.
 *
 * Responsabilità:
 * - Caricare i suggerimenti per i campi di input (titolo, payee, note, location, tags)
 * - Gestire il flusso di dati dei suggerimenti
 * - Logging e debug dei suggerimenti caricati
 *
 * Return type: Flow<Result<TransactionSuggestions>> per reattività
 */
class SuggestionsManager(
    private val getTransactionSuggestionsUseCase: GetTransactionSuggestionsUseCase,
) {

    /**
     * Carica i suggerimenti per tutti i campi di input.
     *
     * I suggerimenti includono:
     * - titles: Titoli usati nelle transazioni precedenti
     * - payees: Beneficiari/Paganti usati
     * - notes: Note usate
     * - locations: Locazioni usate
     * - tags: Tag usati
     *
     * @return Flow di Result<TransactionSuggestions> che emette i suggerimenti al caricamento
     */
    fun getSuggestions(): Flow<Result<TransactionSuggestions>> {
        return getTransactionSuggestionsUseCase()
    }

    /**
     * Filtra i suggerimenti in base a un termine di ricerca.
     *
     * @param suggestions La lista di suggerimenti
     * @param query Il termine di ricerca
     * @return Lista di suggerimenti filtrati che contengono la query (case-insensitive)
     */
    fun filterSuggestions(suggestions: List<String>, query: String): List<String> {
        if (query.isBlank()) return emptyList()
        return suggestions.filter { it.contains(query, ignoreCase = true) }
    }

    /**
     * Seleziona i top N suggerimenti filtrati.
     *
     * @param suggestions La lista di suggerimenti
     * @param query Il termine di ricerca
     * @param limit Il numero massimo di suggerimenti da ritornare (default: 5)
     * @return Lista di suggerimenti limitata a [limit] elementi
     */
    fun getTopFilteredSuggestions(suggestions: List<String>, query: String, limit: Int = 5): List<String> {
        return filterSuggestions(suggestions, query).take(limit)
    }
}
