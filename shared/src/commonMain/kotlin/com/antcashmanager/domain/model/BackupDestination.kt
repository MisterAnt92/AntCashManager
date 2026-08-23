package com.antcashmanager.domain.model

/**
 * Enumerazione delle destinazioni disponibili per il backup automatico.
 *
 * - LOCAL: Salva i backup localmente nel dispositivo (via Storage Access Framework)
 * - GOOGLE_DRIVE: Salva i backup in Google Drive (requires OAuth 2.0 authentication)
 */
public enum class BackupDestination {
    LOCAL,
    GOOGLE_DRIVE,
}
