package com.gamemaps.irl.data.speedlimit

import kotlin.math.roundToInt

/**
 * Interprète la valeur du tag OSM `maxspeed`.
 *
 * Exemples : "50" → 50, "30 mph" → 48, "FR:urban" → 50, "FR:zone30" → 30, "none" → null.
 * Référence : https://wiki.openstreetmap.org/wiki/Key:maxspeed
 */
object MaxSpeedParser {

    /** Valeurs implicites françaises (le reste de l'Europe pourra être ajouté ici). */
    private val IMPLICIT = mapOf(
        "fr:urban" to 50,
        "fr:rural" to 80,
        "fr:trunk" to 110,
        "fr:motorway" to 130,
        "fr:zone30" to 30,
        "fr:zone20" to 20,
        "fr:walk" to 6,
        "walk" to 6,
    )

    fun parse(raw: String): Int? {
        val value = raw.trim().lowercase()
        IMPLICIT[value]?.let { return it }

        val number = value.takeWhile { it.isDigit() }.toIntOrNull() ?: return null
        return if (value.endsWith("mph")) (number * 1.609).roundToInt() else number
    }
}
