package com.antcashmanager.android.work

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.antcashmanager.android.R

/**
 * Gestisce la creazione del NotificationChannel per i fallimenti del backup automatico
 * e l'invio delle notifiche di errore (con degradazione grazioso se permesso non concesso).
 *
 * Uso: Chiamare [ensureChannel] una volta in [Application.onCreate()], poi usare
 * [notifyFailure] quando il backup automatico fallisce.
 */
object AutoBackupNotifier {
    private const val CHANNEL_ID = "auto_backup_errors"
    private const val NOTIFICATION_ID = 1001

    /**
     * Crea il NotificationChannel (idempotente su API 26+, no-op su API < 26).
     * Deve essere chiamato una volta all'avvio dell'app.
     */
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.auto_backup_notification_channel_name)
            val description = context.getString(R.string.auto_backup_notification_channel_description)
            val importance = android.app.NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                android.app.NotificationChannel(CHANNEL_ID, name, importance).apply {
                    this.description = description
                }
            val notificationManager = context.getSystemService(android.app.NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Mostra una notifica di fallimento del backup automatico.
     * La notifica è visibile solo se il permesso POST_NOTIFICATIONS è concesso (API 33+).
     * Su API < 33, le notifiche non richiedono permessi e vengono mostrate sempre.
     */
    fun notifyFailure(context: Context) {
        try {
            val title = context.getString(R.string.auto_backup_notification_failure_title)
            val message = context.getString(R.string.auto_backup_notification_failure_message)

            val notification =
                NotificationCompat
                    .Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher) // Icona app
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                    .build()

            // Prova a mostrare la notifica; su API 33+ con permesso mancante, fallirà gracefully
            try {
                NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            } catch (e: SecurityException) {
                // Permesso POST_NOTIFICATIONS non concesso (API 33+) — ignora silenziosamente
            }
        } catch (e: Exception) {
            // Qualunque altro errore — ignora per non crashare l'app
        }
    }
}
