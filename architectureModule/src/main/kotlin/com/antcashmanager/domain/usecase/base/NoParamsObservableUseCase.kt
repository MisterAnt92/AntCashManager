package com.antcashmanager.domain.usecase.base

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

/**
 * Variante di [ObservableUseCase] per logica di streaming senza parametri di input.
 *
 * Aggiunge un membro `invoke()` senza argomenti, delegato a `invoke(Unit)` della superclasse.
 * Questa è una classe concreta (non astratta) perché il membro `invoke()` è sufficiente per i
 * test: MockK può stubbarlo con `every { useCase() }` (non è possibile con un'extension function).
 *
 * @param R Tipo del valore di successo emesso nel [Result] dal flusso restituito da [invoke]
 * @param dispatcher Dispatcher su cui viene prodotto il Flow. Default [Dispatchers.Default].
 */
public abstract class NoParamsObservableUseCase<R>(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ObservableUseCase<Unit, R>(dispatcher) {
    /**
     * Restituisce il flusso di risultati. Delega a [ObservableUseCase.invoke] con parametro Unit.
     */
    public operator fun invoke(): Flow<Result<R>> = invoke(Unit)
}
