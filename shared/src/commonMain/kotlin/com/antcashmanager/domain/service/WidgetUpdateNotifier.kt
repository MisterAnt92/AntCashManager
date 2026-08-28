package com.antcashmanager.domain.service

/**
 * Notifica ai widget della home screen (platform-specific, es. Glance su Android) che devono
 * ricaricarsi: sia dopo una modifica alle transazioni, sia dopo un cambio delle impostazioni
 * di aspetto (colore di sfondo, opacità).
 */
public interface WidgetUpdateNotifier {
    public suspend fun notifyTransactionsChanged(): Unit
}

/**
 * Implementazione di default per le piattaforme/contesti senza widget home screen
 * (es. test), così i chiamanti non devono gestire un notifier nullable.
 */
public object NoOpWidgetUpdateNotifier : WidgetUpdateNotifier {
    override suspend fun notifyTransactionsChanged(): Unit = Unit
}
