package com.gamemaps.irl.data.custom

import com.gamemaps.irl.map.theme.MapPalette
import com.gamemaps.irl.map.theme.MapTheme

/** Un thème créé en mode développeur : un nom et des couleurs. */
data class CustomThemeSpec(
    val id: String,
    val name: String,
    val palette: MapPalette,
) {
    fun toMapTheme() = MapTheme(
        id = id,
        label = name.ifBlank { "Thème sans nom" },
        description = "Thème personnalisé",
        palette = palette,
        isCustom = true,
    )
}
