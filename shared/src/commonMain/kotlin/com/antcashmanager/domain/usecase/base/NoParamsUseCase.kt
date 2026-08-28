package com.antcashmanager.domain.usecase.base

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Variante di [UseCase] per logica senza parametri di input.
 *
 * Aggiunge un membro `invoke()` senza argomenti, delegato a `invoke(Unit)` della superclasse.
 * Questa è una classe concreta (non astratta) perché il membro `invoke()` è sufficiente per i
 * test: MockK può stubbarlo con `coEvery { useCase() }` (non è possibile con un'extension function).
 *
 * @param R Tipo del valore di successo racchiuso nel [Result] restituito da [invoke]
 * @param dispatcher Dispatcher su cui viene eseguita la logica. Default [Dispatchers.Default].
 */
public abstract class NoParamsUseCase<R>(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<Unit, R>(dispatcher) {
    /**
     * Esegue il UseCase. Delega a [UseCase.invoke] con parametro Unit.
     */
    public suspend operator fun invoke(): Result<R> = invoke(Unit)
}
