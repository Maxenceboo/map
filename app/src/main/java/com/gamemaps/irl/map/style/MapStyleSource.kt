package com.gamemaps.irl.map.style

/**
 * Style de base : tuiles vectorielles OpenFreeMap (OpenStreetMap), gratuites et sans clé.
 * Positron est choisi car il est sobre (peu de calques) : facile à repeindre par MapThemeApplier.
 */
object MapStyleSource {
    const val BASE_STYLE_URL = "https://tiles.openfreemap.org/styles/positron"
}
