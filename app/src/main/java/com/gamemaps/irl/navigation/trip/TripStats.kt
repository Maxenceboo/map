package com.gamemaps.irl.navigation.trip

/** Bilan d'un trajet terminé, affiché sur l'écran "Mission accomplie". */
data class TripStats(
    val distanceMeters: Double = 0.0,
    val durationSeconds: Double = 0.0,
) {
    /** Vitesse moyenne en km/h (0 si le trajet a duré moins d'une seconde). */
    val averageSpeedKmh: Double
        get() = if (durationSeconds < 1) 0.0 else distanceMeters / durationSeconds * 3.6
}
