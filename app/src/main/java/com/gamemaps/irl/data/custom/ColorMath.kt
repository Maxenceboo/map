package com.gamemaps.irl.data.custom

import kotlin.math.roundToInt

/** Petits calculs sur les couleurs au format "#RRGGBB". */
object ColorMath {

    private val HEX = Regex("^#?([0-9a-fA-F]{6})$")

    /** "#a1b2c3" en minuscules, ou null si [text] n'est pas une couleur à six chiffres. */
    fun normalizeHex(text: String): String? =
        HEX.matchEntire(text.trim())?.let { "#" + it.groupValues[1].lowercase() }

    /** La même teinte, assombrie : [factor] = 0 donne du noir, 1 la couleur d'origine. */
    fun darken(hex: String, factor: Double): String {
        val color = normalizeHex(hex) ?: return hex
        val channels = (0..2).map { i ->
            (color.substring(1 + i * 2, 3 + i * 2).toInt(16) * factor.coerceIn(0.0, 1.0)).roundToInt()
        }
        return "#" + channels.joinToString("") { it.toString(16).padStart(2, '0') }
    }
}
