package com.gamemaps.irl.core.format

import kotlin.math.roundToLong

/** Affichage d'une durée restante : "8 min", "1 h 05". */
object DurationFormatter {

    fun format(seconds: Double): String {
        val totalMinutes = (seconds / 60).roundToLong().coerceAtLeast(1)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours == 0L) "$minutes min" else "$hours h ${minutes.toString().padStart(2, '0')}"
    }
}
