package com.gamemaps.irl.map.theme

/**
 * Un thème de carte (cahier des charges §4) : ses couleurs et, parfois, ses textures.
 * Les thèmes fournis sont dans [entries] ; ceux créés en mode développeur s'y ajoutent
 * (voir `CustomThemeSpec`).
 *
 * @property id identifiant stable, enregistré dans les réglages.
 * @property isCustom true pour un thème créé par l'utilisateur.
 */
data class MapTheme(
    val id: String,
    val label: String,
    val description: String,
    val palette: MapPalette,
    val textures: MapTextures = MapTextures.NONE,
    val isCustom: Boolean = false,
) {
    companion object {
        val GTA_RADAR = MapTheme(
            id = "GTA_RADAR",
            label = "Radar nocturne",
            description = "Bleu nuit, routes ardoise, itinéraire violet",
            palette = MapPalette(
                background = "#10131a",
                landcover = "#141924",
                water = "#0b1d2e",
                building = "#1b2130",
                road = "#2a3242",
                majorRoad = "#3b485e",
                boundary = "#2a3242",
                label = "#9aa3b2",
                labelHalo = "#10131a",
                route = "#c084fc",
                routeCasing = "#3b0764",
                vehicle = "#ffffff",
                vehicleOutline = "#10131a",
            ),
        )

        val WAZE_NIGHT = MapTheme(
            id = "WAZE_NIGHT",
            label = "Néon cyan",
            description = "Noir profond, grands axes cyan, itinéraire bleu électrique",
            palette = MapPalette(
                background = "#0d0f14",
                landcover = "#11151c",
                water = "#0a2236",
                building = "#161b24",
                road = "#1f2a36",
                majorRoad = "#0e7490",
                boundary = "#1f2a36",
                label = "#a5f3fc",
                labelHalo = "#0d0f14",
                route = "#38bdf8",
                routeCasing = "#0c4a6e",
                vehicle = "#38bdf8",
                vehicleOutline = "#0d0f14",
            ),
        )

        /** Monde cubique : herbe, eau et arbres en pixel art, routes grises, tracé redstone, cochon. */
        val MINECRAFT = MapTheme(
            id = "MINECRAFT",
            label = "Monde cubique",
            description = "Herbe, eau et arbres en pixel art, tracé rouge, cochon",
            palette = MapPalette(
                background = "#528330",
                landcover = "#385e1e",
                water = "#3c63cc",
                building = "#71717a",
                road = "#6b7280",
                majorRoad = "#9ca3af",
                boundary = "#374151",
                label = "#ffffff",
                labelHalo = "#1f2937",
                route = "#ef4444",
                routeCasing = "#7f1d1d",
                vehicle = "#f9a8d4",
                vehicleOutline = "#1f2937",
            ),
            textures = MapTextures.MINECRAFT,
        )

        /** Thèmes fournis avec l'application. */
        val entries: List<MapTheme> = listOf(GTA_RADAR, WAZE_NIGHT, MINECRAFT)
    }
}
