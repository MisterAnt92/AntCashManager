package com.antcashmanager.android.util

import android.content.Context
import android.net.Uri
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Manages profile photo files in internal app storage. */
class ProfilePhotoStorage(private val context: Context) {

    private val profileDir: File
        get() = File(context.filesDir, "profiles").also { it.mkdirs() }

    /**
     * Copies the image at [uri] into internal storage for the given [profileId],
     * compresses it, and returns the absolute file path.
     */
    suspend fun savePhoto(profileId: Long, uri: Uri): String = withContext(Dispatchers.IO) {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Cannot read image from URI: $uri")
        val compressed = try {
            ImageCompressionHelper.compressImage(bytes)
        } catch (e: Exception) {
            Logger.e(throwable = e) { "Photo compression failed, using original bytes" }
            bytes
        }
        val file = File(profileDir, "profile_$profileId.jpg")
        file.writeBytes(compressed)
        file.absolutePath
    }

    fun deletePhoto(profileId: Long) {
        File(profileDir, "profile_$profileId.jpg").takeIf { it.exists() }?.delete()
    }
}
