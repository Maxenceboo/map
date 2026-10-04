package com.gamemaps.irl.navigation

/** Excès de vitesse si on dépasse la limite de plus de 3 km/h (cahier des charges §6.3). */
object SpeedingDetector {

    private const val TOLERANCE_KMH = 3

    fun isSpeeding(speedKmh: Int, limitKmh: Int?): Boolean =
        limitKmh != null && speedKmh > limitKmh + TOLERANCE_KMH
}
