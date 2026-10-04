package com.gamemaps.irl.data.custom

import com.gamemaps.irl.map.theme.MapPalette
import org.json.JSONArray
import org.json.JSONObject

/** Thèmes et véhicules personnalisés ⇄ JSON (pour les garder sur le téléphone). Une entrée illisible est ignorée. */
object CustomContentSerializer {

    fun themesToJson(themes: List<CustomThemeSpec>): String = JSONArray().apply {
        themes.forEach { theme ->
            put(JSONObject().put("id", theme.id).put("name", theme.name).put("palette", paletteToJson(theme.palette)))
        }
    }.toString()

    fun themesFromJson(json: String?): List<CustomThemeSpec> = objects(json).mapNotNull { item ->
        runCatching {
            CustomThemeSpec(item.getString("id"), item.getString("name"), paletteFromJson(item.getJSONObject("palette")))
        }.getOrNull()
    }

    fun vehiclesToJson(vehicles: List<CustomVehicleSpec>): String = JSONArray().apply {
        vehicles.forEach { v ->
            put(
                JSONObject()
                    .put("id", v.id).put("name", v.name)
                    .put("length", v.length).put("width", v.width)
                    .put("bodyHeight", v.bodyHeight).put("groundClearance", v.groundClearance)
                    .put("cabinRatio", v.cabinRatio).put("cabinHeight", v.cabinHeight)
                    .put("wheelDiameter", v.wheelDiameter).put("spoiler", v.spoiler),
            )
        }
    }.toString()

    fun vehiclesFromJson(json: String?): List<CustomVehicleSpec> = objects(json).mapNotNull { item ->
        runCatching {
            CustomVehicleSpec(
                id = item.getString("id"),
                name = item.getString("name"),
                length = item.getDouble("length"),
                width = item.getDouble("width"),
                bodyHeight = item.getDouble("bodyHeight"),
                groundClearance = item.getDouble("groundClearance"),
                cabinRatio = item.getDouble("cabinRatio"),
                cabinHeight = item.getDouble("cabinHeight"),
                wheelDiameter = item.getDouble("wheelDiameter"),
                spoiler = item.optBoolean("spoiler", false),
            ).clamped()
        }.getOrNull()
    }

    private fun objects(json: String?): List<JSONObject> {
        if (json.isNullOrBlank()) return emptyList()
        val array = runCatching { JSONArray(json) }.getOrNull() ?: return emptyList()
        return (0 until array.length()).mapNotNull { array.optJSONObject(it) }
    }

    private fun paletteToJson(p: MapPalette) = JSONObject()
        .put("background", p.background).put("landcover", p.landcover).put("water", p.water)
        .put("building", p.building).put("road", p.road).put("majorRoad", p.majorRoad)
        .put("boundary", p.boundary).put("label", p.label).put("labelHalo", p.labelHalo)
        .put("route", p.route).put("routeCasing", p.routeCasing)
        .put("vehicle", p.vehicle).put("vehicleOutline", p.vehicleOutline)

    private fun paletteFromJson(o: JSONObject): MapPalette {
        fun color(key: String) = ColorMath.normalizeHex(o.getString(key)) ?: error("couleur invalide : $key")
        return MapPalette(
            background = color("background"), landcover = color("landcover"), water = color("water"),
            building = color("building"), road = color("road"), majorRoad = color("majorRoad"),
            boundary = color("boundary"), label = color("label"), labelHalo = color("labelHalo"),
            route = color("route"), routeCasing = color("routeCasing"),
            vehicle = color("vehicle"), vehicleOutline = color("vehicleOutline"),
        )
    }
}
