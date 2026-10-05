package com.gamemaps.irl.navigation.radar

/**
 * Longueur d'une zone de danger selon la route, comme le font les assistants d'aide à la conduite
 * en France : 4 km sur autoroute, 2 km hors agglomération, 300 m en agglomération.
 */
object DangerZoneSize {

    private const val MOTORWAY_METERS = 4_000.0
    private const val RURAL_METERS = 2_000.0
    private const val URBAN_METERS = 300.0

    /** @param maxSpeedKmh vitesse autorisée sur la route ; inconnue, on prend la zone hors agglomération. */
    fun lengthMeters(maxSpeedKmh: Int?): Double = when {
        maxSpeedKmh == null -> RURAL_METERS
        maxSpeedKmh >= 110 -> MOTORWAY_METERS
        maxSpeedKmh > 50 -> RURAL_METERS
        else -> URBAN_METERS
    }
}
