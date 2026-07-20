package com.antcashmanager.android.data.backup

/**
 * Shared constants for backup data layer.
 */
object BackupConstants {
    /**
     * v1: transactions + categories.
     * v2: aggiunge paymentType/mealVoucherCount/categoryIcon/categoryColor alle transazioni
     * e un blocco `settings` opzionale (tema, lingua, accessibilità, visualizzazione).
     * Un backup v1 resta leggibile: i campi mancanti usano i default e `settings` resta null.
     */
    const val CURRENT_VERSION = 2
}

