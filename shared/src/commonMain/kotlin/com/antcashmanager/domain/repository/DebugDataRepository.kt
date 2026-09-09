package com.antcashmanager.domain.repository

/**
 * Repository per la gestione dei dati di debug.
 *
 * Encapsula l'accesso al file asset `debug_initial_data.json`
 * e l'inserimento dei dati nel database.
 */
public interface DebugDataRepository {
    /**
     * Importa i dati di debug dal file asset.
     *
     * @return Boolean - true se l'import è riuscito, false altrimenti
     * @throws Exception se ci sono errori durante l'import
     */
    public suspend fun importDebugData(): Boolean
}
