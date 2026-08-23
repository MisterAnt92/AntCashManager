package com.antcashmanager.domain.model

import com.antcashmanager.domain.model.SavedDateFilter.Companion.CUSTOM_PRESET_INDEX


/**
 * Stato persistito del filtro temporale per uno screen.
 *
 * @property presetIndex indice del preset selezionato; [CUSTOM_PRESET_INDEX] indica un range personalizzato
 * @property from timestamp iniziale in millisecondi
 * @property to timestamp finale in millisecondi
 */
public data class SavedDateFilter(
    public val presetIndex: Int,
    public val from: Long,
    public val to: Long,
) {
    public val isCustom: Boolean
        get() = presetIndex == CUSTOM_PRESET_INDEX

    public companion object {
        public const val CUSTOM_PRESET_INDEX: Int = -1
    }
}

