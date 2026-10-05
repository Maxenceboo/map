package com.gamemaps.irl.car.alerts

import com.gamemaps.irl.navigation.radar.RadarAlert

/**
 * Décide quoi faire de l'alerte "zone de danger" à l'écran de la voiture (logique pure, testable).
 *
 * - Entrée dans une nouvelle zone : afficher.
 * - Sortie de zone : retirer.
 * - Sinon : ne rien faire (on ne rafraîchit pas l'alerte en continu, ce serait distrayant).
 */
class CarRadarAlertPolicy {

    sealed interface Decision {
        data class Show(val alertId: Int, val title: String, val subtitle: String) : Decision
        data class Dismiss(val alertId: Int) : Decision
        data object Nothing : Decision
    }

    private var shownZoneId: String? = null

    fun onAlert(alert: RadarAlert?): Decision {
        if (alert == null) {
            val previous = shownZoneId ?: return Decision.Nothing
            shownZoneId = null
            return Decision.Dismiss(alertIdOf(previous))
        }
        if (alert.zoneId == shownZoneId) return Decision.Nothing
        shownZoneId = alert.zoneId
        return Decision.Show(
            alertId = alertIdOf(alert.zoneId),
            title = "Zone de danger",
            subtitle = alert.maxSpeedKmh?.let { "Limitée à $it km/h" } ?: "Restez vigilant",
        )
    }

    private fun alertIdOf(zoneId: String): Int = zoneId.hashCode()
}
