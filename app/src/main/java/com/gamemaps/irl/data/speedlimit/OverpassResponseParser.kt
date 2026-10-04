package com.gamemaps.irl.data.speedlimit

import com.gamemaps.irl.core.geo.LatLng
import org.json.JSONObject

/**
 * Lit la réponse Overpass (`out tags geom`) : chaque élément est une route avec
 * `tags.maxspeed` et `geometry = [{lat, lon}, ...]`.
 */
object OverpassResponseParser {

    fun parse(json: String): List<RoadSegment> {
        val elements = JSONObject(json).optJSONArray("elements") ?: return emptyList()
        return (0 until elements.length()).mapNotNull { index ->
            val way = elements.getJSONObject(index)
            val maxSpeed = way.optJSONObject("tags")?.optString("maxspeed")?.let(MaxSpeedParser::parse)
                ?: return@mapNotNull null
            val geometry = way.optJSONArray("geometry") ?: return@mapNotNull null
            val points = (0 until geometry.length()).map { i ->
                val node = geometry.getJSONObject(i)
                LatLng(node.getDouble("lat"), node.getDouble("lon"))
            }
            if (points.size < 2) null else RoadSegment(way.getLong("id"), maxSpeed, points)
        }
    }
}
