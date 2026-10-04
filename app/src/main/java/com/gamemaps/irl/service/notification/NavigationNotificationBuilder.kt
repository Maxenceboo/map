package com.gamemaps.irl.service.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.gamemaps.irl.MainActivity
import com.gamemaps.irl.R
import com.gamemaps.irl.service.NavigationForegroundService

/** Construit la notification de guidage : un appui rouvre l'app, le bouton "Arrêter" coupe le guidage. */
object NavigationNotificationBuilder {

    fun build(context: Context, content: NavigationNotificationContent): Notification {
        val openApp = PendingIntent.getActivity(
            context,
            REQUEST_OPEN,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val stop = PendingIntent.getService(
            context,
            REQUEST_STOP,
            NavigationForegroundService.stopIntent(context),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(context, NavigationNotificationChannel.ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(content.title)
            .setContentText(content.text)
            .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setContentIntent(openApp)
            .addAction(0, "Arrêter", stop)
            .build()
    }

    private const val REQUEST_OPEN = 1
    private const val REQUEST_STOP = 2
}
