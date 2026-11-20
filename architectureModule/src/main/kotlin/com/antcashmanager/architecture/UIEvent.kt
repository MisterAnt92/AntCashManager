package com.antcashmanager.architecture

/**
 * Classe base per tutti gli eventi UI dell'applicazione.
 *
 * Ogni istanza porta un [id] UUID univoco generato alla costruzione, usato per
 * logging e tracing per-evento nel consumer del Channel in [BaseViewModel].
 */
public abstract class UIEvent {
    public val id: String = java.util.UUID.randomUUID().toString()
}
