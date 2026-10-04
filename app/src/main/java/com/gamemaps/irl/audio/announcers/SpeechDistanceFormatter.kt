package com.gamemaps.irl.audio.announcers

import java.util.Locale
import kotlin.math.roundToInt

/** Distance à prononcer : "300 mètres", "1,5 kilomètre", "12 kilomètres". */
object SpeechDistanceFormatter {

    fun format(meters: Double): String = when {
        meters < 1_000 -> "${roundTo50(meters)} mètres"
        meters < 10_000 -> {
            val km = String.format(Locale.FRANCE, "%.1f", meters / 1_000).removeSuffix(",0")
            if (km == "1") "1 kilomètre" else "$km kilomètres"
        }
        else -> "${(meters / 1_000).roundToInt()} kilomètres"
    }

    /** Arrondi aux 50 m (jamais 0 : on dit au moins "50 mètres"). */
    private fun roundTo50(meters: Double): Int = ((meters / 50).roundToInt() * 50).coerceAtLeast(50)
}
