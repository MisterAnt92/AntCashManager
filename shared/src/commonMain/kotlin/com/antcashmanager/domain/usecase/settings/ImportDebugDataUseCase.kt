package com.antcashmanager.domain.usecase.settings

import com.antcashmanager.domain.repository.DebugDataRepository
import com.antcashmanager.domain.usecase.base.NoParamsUseCase
import kotlinx.coroutines.Dispatchers

/**
 * UseCase per importare i dati di debug dal file asset.
 *
 * Encapsula la logica di lettura dell'asset e inserimento dei dati,
 * rimuovendo la dipendenza da Context dal ViewModel.
 * Esecuzione solo in DEBUG build.
 */
public class ImportDebugDataUseCase(
    private val debugDataRepository: DebugDataRepository,
    dispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.Default,
) : NoParamsUseCase<Boolean>(dispatcher) {
    /**
     * Importa i dati di debug dall'asset.
     *
     * @param params Unit (non utilizzato)
     * @return Boolean - true se l'import è riuscito, false altrimenti
     */
    public override suspend fun execute(params: Unit): Boolean {
        return debugDataRepository.importDebugData()
    }
}
