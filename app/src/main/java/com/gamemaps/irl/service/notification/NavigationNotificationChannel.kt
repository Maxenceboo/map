package com.gamemaps.irl.service.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

/**
 * Canal "Guidage" : importance basse = la notification reste affichée en permanence
 * mais sans son ni vibration à chaque mise à jour (le guidage a déjà sa propre voix).
 */
object NavigationNotificationChannel {

    const val ID = "navigation_guidance"

    fun ensureCreated(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(ID) != null) return
        val channel = NotificationChannel(ID, "Guidage", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Prochaine manœuvre et heure d'arrivée pendant un trajet"
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }
}
