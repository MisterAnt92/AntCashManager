package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.TransactionSuggestions
import com.antcashmanager.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

/**
 * UseCase per ottenere suggerimenti per i campi transazione basati sullo storico.
 *
 * Recupera valori distinti dai campi delle transazioni precedenti per fornire
 * suggerimenti intelligenti durante l'inserimento di nuove transazioni.
 *
 * Il dispatcher è iniettabile per garantire testabilità con [kotlinx.coroutines.test.TestDispatcher].
 *
 * @param repository Repository per l'accesso ai dati delle transazioni
 * @param dispatcher Dispatcher su cui viene prodotto il Flow. Default [Dispatchers.Default].
 */
class GetTransactionSuggestionsUseCase(
    private val repository: TransactionRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    /**
     * Recupera i suggerimenti per tutti i campi transazione.
     * Il Flow è cancellabile: la cancellazione del collector cancella la produzione.
     *
     * @return Flow contenente [TransactionSuggestions] con tutti i suggerimenti disponibili
     */
    operator fun invoke(): Flow<TransactionSuggestions> = combine(
        repository.getDistinctTitles(),
        repository.getDistinctPayees(),
        repository.getDistinctNotes(),
        repository.getDistinctLocations(),
        repository.getDistinctTags(),
    ) { titles, payees, notes, locations, tags ->
        TransactionSuggestions(
            titles = titles,
            payees = payees,
            notes = notes,
            locations = locations,
            tags = tags,
        )
    }.flowOn(dispatcher)
}
