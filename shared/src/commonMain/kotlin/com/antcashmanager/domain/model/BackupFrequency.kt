package com.antcashmanager.domain.model

/**
 * Frequenza del backup automatico.
 *
 * - WEEKLY: Backup every 7 days
 * - BIWEEKLY: Backup every 14 days
 * - MONTHLY: Backup every 30 days
 */
public enum class BackupFrequency(
    public val intervalDays: Long,
) {
    WEEKLY(7L),
    BIWEEKLY(14L),
    MONTHLY(30L),
}
