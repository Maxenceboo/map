package com.gamemaps.irl.map.theme

/**
 * Identifiants des images de motif installées dans le style pour le thème actif.
 * null = pas de motif pour cette surface (couleur unie).
 */
data class ThemePatterns(
    val ground: String? = null,
    val water: String? = null,
    val woods: String? = null,
) {
    companion object {
        val NONE = ThemePatterns()
    }
}
