package com.gamemaps.irl.map.theme

/** Couleurs d'un thème de carte (format "#RRGGBB"). */
data class MapPalette(
    val background: String,
    val landcover: String,
    val water: String,
    val building: String,
    val road: String,
    val majorRoad: String,
    val boundary: String,
    val label: String,
    val labelHalo: String,
    val route: String,
    val routeCasing: String,
    val vehicle: String,
    val vehicleOutline: String,
)
