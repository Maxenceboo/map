package com.gamemaps.irl.car.alerts

import com.gamemaps.irl.core.format.DistanceFormatter
import com.gamemaps.irl.navigation.radar.RadarAlert
import com.gamemaps.irl.navigation.radar.RadarAlertLevel
import com.gamemaps.irl.navigation.radar.RadarTypeText

/**
 * Décide quoi faire de l'alerte radar à l'écran de la voiture (logique pure, testable).
 *
 * - Nouveau radar : afficher.
 * - Même radar qui passe en urgent : réafficher (texte "URGENT").
 * - Plus d'alerte (radar passé) : retirer.
 * - Sinon : ne rien faire (on ne rafraîchit pas l'alerte à chaque mètre, ce serait distrayant).
 */
class CarRadarAlertPolicy {

    sealed interface Decision {
        data class Show(val alertId: Int, val title: String, val subtitle: String, val alert: RadarAlert) : Decision
        data class Dismiss(val alertId: Int) : Decision
        data object Nothing : Decision
    }

    private var shownRadarId: String? = null
    private var shownUrgent = false

    fun onAlert(alert: RadarAlert?): Decision {
        if (alert == null) {
            val previous = shownRadarId ?: return Decision.Nothing
            shownRadarId = null
            shownUrgent = false
            return Decision.Dismiss(alertIdOf(previous))
        }
        val urgent = alert.level == RadarAlertLevel.URGENT
        if (alert.radar.id == shownRadarId && (shownUrgent || !urgent)) return Decision.Nothing
        shownRadarId = alert.radar.id
        shownUrgent = urgent
        return Decision.Show(
            alertId = alertIdOf(alert.radar.id),
            title = titleOf(alert, urgent),
            subtitle = "dans ${DistanceFormatter.format(alert.distanceMeters)}",
            alert = alert,
        )
    }

    private fun titleOf(alert: RadarAlert, urgent: Boolean): String {
        val name = RadarTypeText.banner(alert.radar.type)
        val speed = alert.radar.maxSpeedKmh?.let { " · $it km/h" }.orEmpty()
        return (if (urgent) "⚠ " else "") + name + speed
    }

    private fun alertIdOf(radarId: String): Int = radarId.hashCode()
}
