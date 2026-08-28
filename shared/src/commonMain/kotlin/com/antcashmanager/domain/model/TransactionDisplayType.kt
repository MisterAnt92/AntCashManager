package com.antcashmanager.domain.model

/**
 * Defines how transaction icons are displayed in the UI.
 */
public enum class TransactionDisplayType {
    /**
     * Show trend arrows (up for income, down for expense).
     */
    TREND,

    /**
     * Show category icon/name.
     */
    CATEGORY,

    /**
     * Don't show any icon.
     */
    NONE
}

