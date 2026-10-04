package com.gamemaps.irl.map.vehicle3d

import kotlin.math.cos
import kotlin.math.pow

/**
 * Une vraie voiture de 4,5 m ne ferait que quelques pixels à l'écran. On l'agrandit pour qu'une
 * voiture de référence mesure toujours ~[TARGET_LENGTH_DP] à l'écran, quel que soit le zoom
 * (même principe que les "58 pixels" de la version WebGL, cahier des charges §3.3).
 *
 * Tous les véhicules partagent le même facteur : une moto reste donc plus petite qu'une F1.
 */
object VehicleScale {

    /** Mètres par pixel logique au zoom 0 à l'équateur (tuiles de 512 px). */
    private const val METERS_PER_DP_AT_ZOOM_0 = 78_271.517
    private const val REFERENCE_LENGTH_METERS = 4.6
    private const val TARGET_LENGTH_DP = 64.0

    fun metersPerDp(zoom: Double, latitude: Double): Double =
        METERS_PER_DP_AT_ZOOM_0 * cos(Math.toRadians(latitude)) / 2.0.pow(zoom)

    /** Facteur par lequel multiplier les dimensions réelles du véhicule. */
    fun factor(zoom: Double, latitude: Double): Double =
        TARGET_LENGTH_DP * metersPerDp(zoom, latitude) / REFERENCE_LENGTH_METERS
}
