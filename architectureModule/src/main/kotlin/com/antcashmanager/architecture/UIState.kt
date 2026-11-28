package com.antcashmanager.architecture

/** Marker bound per tutti gli State dei ViewModel. Ogni state eredita [isLoading] per la gestione uniforme del caricamento. */
public abstract class UIState {
    public open val isLoading: Boolean get() = false
}
