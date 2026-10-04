package com.gamemaps.irl.data.radar

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.speedlimit.MaxSpeedParser
import org.json.JSONObject

/** Lit la réponse Overpass des radars OSM : `elements[] = { id, lat, lon, tags.maxspeed? }`. */
object RadarResponseParser {

    fun parse(json: String): List<Radar> {
        val elements = JSONObject(json).optJSONArray("elements") ?: return emptyList()
        return (0 until elements.length()).mapNotNull { index ->
            val node = elements.getJSONObject(index)
            if (!node.has("lat") || !node.has("lon")) return@mapNotNull null
            val tags = node.optJSONObject("tags")
            Radar(
                id = "osm_${node.getLong("id")}",
                position = LatLng(node.getDouble("lat"), node.getDouble("lon")),
                type = RadarType.SPEED,
                maxSpeedKmh = tags?.optString("maxspeed")?.let(MaxSpeedParser::parse),
                road = tags?.optString("ref")?.ifBlank { null },
            )
        }
    }
}
