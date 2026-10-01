package com.gamemaps.irl.map.camera

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
) {
    companion object {
        val PHONE = CameraConfig(zoom = 17.0, tilt = 55.0, topPaddingRatio = 0.45, animationMillis = 900)
        val CAR = CameraConfig(zoom = 16.5, tilt = 50.0, topPaddingRatio = 0.35, animationMillis = 900)
    }
}
