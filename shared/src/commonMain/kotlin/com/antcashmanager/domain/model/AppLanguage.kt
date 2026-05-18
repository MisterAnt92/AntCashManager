package com.antcashmanager.domain.model

/**
 * Supported languages for the app.
 * @param code The ISO 639-1 language code. Empty string means "follow system".
 */
enum class AppLanguage(val code: String) {
    SYSTEM(""),
    ENGLISH("en"),
    ITALIAN("it"),
    FRENCH("fr"),
    GERMAN("de"),
    SPANISH("es"),
}

