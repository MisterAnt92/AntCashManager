package com.antcashmanager.domain.usecase.transaction

import com.antcashmanager.domain.model.TransactionSuggestions
import com.antcashmanager.domain.repository.SettingsRepository
import com.antcashmanager.domain.repository.TransactionRepository
import com.antcashmanager.domain.usecase.base.NoParamsObservableUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/**
 * UseCase per ottenere suggerimenti per i campi transazione basati sullo storico.
 *
 * Recupera valori distinti dai campi delle transazioni precedenti per fornire
 * suggerimenti intelligenti durante l'inserimento di nuove transazioni.
 *
 * I suggerimenti rispettano due impostazioni utente: se disabilitati globalmente
 * ([SettingsRepository.getSuggestionsEnabled]) non viene restituito nulla; se l'utente ha
 * richiesto di "dimenticarli" ([SettingsRepository.getSuggestionsClearedAt]), vengono
 * calcolati solo dalle transazioni successive a quel timestamp, senza toccare le
 * transazioni stesse (non hanno uno storage proprio).
 *
 * Il dispatcher è iniettabile per garantire testabilità con [kotlinx.coroutines.test.TestDispatcher].
 *
 * @param repository Repository per l'accesso ai dati delle transazioni
 * @param settingsRepository Repository per le preferenze di abilitazione/cancellazione suggerimenti
 * @param dispatcher Dispatcher su cui viene prodotto il Flow. Default [Dispatchers.Default].
 */
@OptIn(ExperimentalCoroutinesApi::class)
public class GetTransactionSuggestionsUseCase(
    private val repository: TransactionRepository,
    private val settingsRepository: SettingsRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoParamsObservableUseCase<TransactionSuggestions>(dispatcher) {
    /**
     * Recupera i suggerimenti per tutti i campi transazione.
     * Il Flow è cancellabile: la cancellazione del collector cancella la produzione.
     * La wrappatura in [Result] è gestita dalla base class [NoParamsObservableUseCase].
     *
     * @return Flow contenente [TransactionSuggestions] con tutti i suggerimenti disponibili
     */
    override fun execute(params: Unit): Flow<TransactionSuggestions> = combine(
        settingsRepository.getSuggestionsEnabled(),
        settingsRepository.getSuggestionsClearedAt(),
    ) { enabled, clearedAt -> enabled to clearedAt }
        .flatMapLatest { (enabled, clearedAt) ->
            if (!enabled) {
                flowOf(TransactionSuggestions())
            } else {
                val since = clearedAt ?: 0L
                // Use unified query: 1 query instead of 5 separate queries
                flow {
                    emit(repository.getSuggestions(since))
                }
            }
        }
}
