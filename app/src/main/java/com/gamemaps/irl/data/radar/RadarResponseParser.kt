package com.gamemaps.irl.data.radar

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.speedlimit.MaxSpeedParser
import org.json.JSONObject

/** Lit la réponse Overpass des radars : `elements[] = { id, lat, lon, tags.maxspeed? }`. */
object RadarResponseParser {

    fun parse(json: String): List<Radar> {
        val elements = JSONObject(json).optJSONArray("elements") ?: return emptyList()
        return (0 until elements.length()).mapNotNull { index ->
            val node = elements.getJSONObject(index)
            if (!node.has("lat") || !node.has("lon")) return@mapNotNull null
            Radar(
                id = node.getLong("id"),
                position = LatLng(node.getDouble("lat"), node.getDouble("lon")),
                maxSpeedKmh = node.optJSONObject("tags")?.optString("maxspeed")?.let(MaxSpeedParser::parse),
            )
        }
    }
}
