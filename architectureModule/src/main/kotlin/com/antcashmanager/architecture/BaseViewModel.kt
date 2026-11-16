package com.antcashmanager.architecture

/**
 * Contract base per tutti i ViewModel dell'applicazione.
 *
 * Definisce il punto di ingresso unico per gli eventi UI ([onEvent]).
 * Le implementazioni Android estendono [androidx.lifecycle.ViewModel] e implementano questa
 * interfaccia; questo design rispetta il vincolo Kotlin di ereditarietà singola.
 *
 * @param E il tipo dell'evento sealed class. [Nothing] per i ViewModel senza eventi.
 */
public interface BaseViewModel<E : Any> {
    public fun onEvent(event: E)
}
