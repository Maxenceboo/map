package com.gamemaps.irl.data.custom

import com.gamemaps.irl.map.theme.MapPalette

/**
 * Les couleurs qu'on règle dans l'éditeur de thème. Une couleur choisie peut en entraîner
 * d'autres pour que la carte reste lisible (contour des textes = fond, liseré du tracé = tracé assombri).
 */
enum class ThemeColorSlot(val label: String) {
    BACKGROUND("Fond"),
    LANDCOVER("Végétation"),
    WATER("Eau"),
    BUILDING("Bâtiments"),
    ROAD("Routes"),
    MAJOR_ROAD("Grands axes"),
    LABEL("Noms de rues"),
    ROUTE("Itinéraire"),
    ;

    fun read(palette: MapPalette): String = when (this) {
        BACKGROUND -> palette.background
        LANDCOVER -> palette.landcover
        WATER -> palette.water
        BUILDING -> palette.building
        ROAD -> palette.road
        MAJOR_ROAD -> palette.majorRoad
        LABEL -> palette.label
        ROUTE -> palette.route
    }

    fun write(palette: MapPalette, hex: String): MapPalette = when (this) {
        BACKGROUND -> palette.copy(background = hex, labelHalo = hex, vehicleOutline = hex)
        LANDCOVER -> palette.copy(landcover = hex)
        WATER -> palette.copy(water = hex)
        BUILDING -> palette.copy(building = hex)
        ROAD -> palette.copy(road = hex, boundary = hex)
        MAJOR_ROAD -> palette.copy(majorRoad = hex)
        LABEL -> palette.copy(label = hex)
        ROUTE -> palette.copy(route = hex, routeCasing = ColorMath.darken(hex, ROUTE_CASING_DARKNESS))
    }

    private companion object {
        const val ROUTE_CASING_DARKNESS = 0.35
    }
}
