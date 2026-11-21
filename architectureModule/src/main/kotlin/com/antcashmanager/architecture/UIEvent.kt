package com.antcashmanager.architecture

import java.util.concurrent.atomic.AtomicLong

/**
 * Classe base per tutti gli eventi UI dell'applicazione.
 *
 * Ogni istanza porta un [id] univoco generato alla costruzione, usato per
 * logging e tracing per-evento nel consumer del Channel in [BaseViewModel].
 */
public abstract class UIEvent {
    public val id: String = "evt-${sequence.incrementAndGet()}"

    private companion object {
        private val sequence = AtomicLong(0)
    }
}
