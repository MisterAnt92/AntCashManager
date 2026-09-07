package com.antcashmanager.android.data.backup

import android.os.Build

/**
 * Sealed class per errori di backup/restore con diagnostica API-level.
 *
 * Provides specific error types for granular error handling and user-facing messages.
 */
sealed class BackupError(
    errorMessage: String,
    cause: Throwable? = null,
) : Exception(errorMessage, cause) {
    // File I/O errors
    data class FileNotFound(
        val path: String,
    ) : BackupError("File not found: $path")

    data class FileAccessDenied(
        val path: String,
    ) : BackupError("Access denied: $path")

    data class FileReadError(
        val errorMessage: String,
    ) : BackupError("File read failed: $errorMessage")

    data class FileWriteError(
        val errorMessage: String,
    ) : BackupError("File write failed: $errorMessage")

    // Charset/encoding errors
    data class CharsetError(
        val attemptedCharset: String,
    ) : BackupError(
            "Cannot decode with charset: $attemptedCharset",
        )

    data class EncodingError(
        val reason: String,
    ) : BackupError("Encoding error: $reason")

    // Encryption/decryption errors
    data class DecryptionError(
        val reason: String,
    ) : BackupError("Decryption failed: $reason")

    data class EncryptionError(
        val reason: String,
    ) : BackupError("Encryption failed: $reason")

    data class InvalidPayloadFormat(
        val expected: String,
    ) : BackupError(
            "Invalid payload format: $expected",
        )

    // JSON parsing errors
    data class JsonParseError(
        val reason: String,
    ) : BackupError("JSON parse failed: $reason")

    data class JsonSchemaError(
        val reason: String,
    ) : BackupError("JSON schema error: $reason")

    // Version errors
    data class UnsupportedVersion(
        val backupVersion: Int,
        val supportedVersion: Int,
    ) : BackupError(
            "Backup version $backupVersion is not supported (current: $supportedVersion)",
        )

    // Generic error
    data class Unknown(
        val originalError: Throwable,
    ) : BackupError(
            "Unknown error: ${originalError.message}",
            originalError,
        )

    /**
     * Utility function per logare debug info con API level.
     * Useful for Crashlytics and other analytics.
     */
    fun getDebugInfo(): String {
        val apiLevel = Build.VERSION.SDK_INT
        return "API Level: $apiLevel | Error: ${this.message}"
    }
}
