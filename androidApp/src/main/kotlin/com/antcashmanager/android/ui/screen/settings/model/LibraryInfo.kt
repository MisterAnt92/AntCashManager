package com.antcashmanager.android.ui.screen.settings.model

data class LibraryInfo(
    val name: String,
    val url: String,
)

val thirdPartyLibraries = listOf(
    LibraryInfo("Jetpack Compose", "https://developer.android.com/jetpack/compose"),
    LibraryInfo("Room Database", "https://developer.android.com/training/data-storage/room"),
    LibraryInfo("Navigation Compose", "https://developer.android.com/jetpack/compose/navigation"),
    LibraryInfo(
        "DataStore Preferences",
        "https://developer.android.com/topic/libraries/architecture/datastore"
    ),
    LibraryInfo("Material Icons Extended", "https://fonts.google.com/icons"),
    LibraryInfo("Kotlinx Coroutines", "https://github.com/Kotlin/kotlinx.coroutines"),
    LibraryInfo("Kermit Logger", "https://github.com/touchlab/Kermit"),
)