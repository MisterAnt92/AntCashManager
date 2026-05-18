package com.antcashmanager.domain.model

/**
 * Contiene i suggerimenti per i campi transazione basati sullo storico.
 *
 * @property titles Lista di titoli distinti dalle transazioni precedenti
 * @property payees Lista di beneficiari distinti dalle transazioni precedenti
 * @property notes Lista di note distinte dalle transazioni precedenti
 * @property locations Lista di luoghi distinti dalle transazioni precedenti
 * @property tags Lista di tag distinti dalle transazioni precedenti
 */
data class TransactionSuggestions(
    val titles: List<String> = emptyList(),
    val payees: List<String> = emptyList(),
    val notes: List<String> = emptyList(),
    val locations: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
)

