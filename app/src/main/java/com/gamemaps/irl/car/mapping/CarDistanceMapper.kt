package com.gamemaps.irl.car.mapping

import androidx.car.app.model.Distance
import kotlin.math.roundToInt

/** Mètres → [Distance] Android Auto, avec la même logique d'arrondi que le téléphone. */
object CarDistanceMapper {

    fun map(meters: Double): Distance = when {
        meters < 1_000 -> Distance.create(((meters / 10).roundToInt() * 10).toDouble(), Distance.UNIT_METERS)
        meters < 10_000 -> Distance.create(meters / 1_000, Distance.UNIT_KILOMETERS_P1)
        else -> Distance.create(meters / 1_000, Distance.UNIT_KILOMETERS)
    }
}
