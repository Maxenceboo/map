package com.gamemaps.irl.core.format

import java.util.Locale
import kotlin.math.roundToInt

/** Affichage des distances à la française : "250 m", "1,2 km", "14 km". */
object DistanceFormatter {

    fun format(meters: Double): String = when {
        meters < 1_000 -> "${roundTo10(meters)} m"
        meters < 10_000 -> String.format(Locale.FRANCE, "%.1f km", meters / 1_000)
        else -> "${(meters / 1_000).roundToInt()} km"
    }

    /** Arrondi aux 10 m pour éviter un chiffre qui défile en permanence. */
    private fun roundTo10(meters: Double): Int = ((meters / 10).roundToInt() * 10).coerceAtLeast(0)
}
