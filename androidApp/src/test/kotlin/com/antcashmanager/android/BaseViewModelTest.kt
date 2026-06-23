package com.antcashmanager.android

/**
 * Compatibilità retroattiva per i test ViewModel già esistenti.
 *
 * Preferire [BaseUnitTest] per tutti i nuovi test unitari Android.
 */
@Deprecated(
    message = "Usa BaseUnitTest per i test unitari Android.",
    replaceWith = ReplaceWith("BaseUnitTest"),
)
abstract class BaseViewModelTest : BaseUnitTest()

