package com.gamemaps.irl.map.camera

import com.gamemaps.irl.data.settings.Perspective

/**
 * Réglages de la caméra "poursuite".
 *
 * @property topPaddingRatio part de la hauteur réservée en haut : pousse le véhicule vers le bas
 * de l'écran pour voir plus de route devant.
 */
data class CameraConfig(
    val zoom: Double,
    val tilt: Double,
    val topPaddingRatio: Double,
    val animationMillis: Int,
    /** Aperçu d'un trajet : part de l'écran masquée à gauche et en bas par le panneau de l'aperçu. */
    val overviewLeftRatio: Double = 0.0,
    val overviewBottomRatio: Double = 0.58,
) {
    /** Variante "vue de dessus" : pas d'inclinaison, un peu plus haut, véhicule moins bas à l'écran. */
    fun topDown(): CameraConfig = copy(zoom = zoom - 0.5, tilt = 0.0, topPaddingRatio = topPaddingRatio * 0.5)

    fun forPerspective(perspective: Perspective): CameraConfig = when (perspective) {
        Perspective.COCKPIT_3D -> this
        Perspective.TOP_DOWN_2D -> topDown()
    }

    companion object {
        val PHONE = CameraConfig(zoom = 17.0, tilt = 55.0, topPaddingRatio = 0.45, animationMillis = 900)
        // Dans la voiture, la liste des trajets occupe la moitié gauche de l'écran.
        val CAR = CameraConfig(zoom = 16.5, tilt = 50.0, topPaddingRatio = 0.35, animationMillis = 900, overviewLeftRatio = 0.5, overviewBottomRatio = 0.12)
    }
}
