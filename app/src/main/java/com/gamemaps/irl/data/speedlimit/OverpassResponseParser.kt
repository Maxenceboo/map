package com.gamemaps.irl.data.speedlimit

import com.gamemaps.irl.core.geo.LatLng
import org.json.JSONObject

/**
 * Lit la réponse Overpass (`out tags geom`) : chaque élément est une route avec ses étiquettes
 * et `geometry = [{lat, lon}, ...]`. La limitation vient de `maxspeed` ; à défaut, elle est
 * estimée d'après le type de route (voir [DefaultSpeedLimits]).
 */
object OverpassResponseParser {

    fun parse(json: String): List<RoadSegment> {
        val elements = JSONObject(json).optJSONArray("elements") ?: return emptyList()
        return (0 until elements.length()).mapNotNull { index ->
            val way = elements.getJSONObject(index)
            val tags = tagsOf(way)
            val signed = tags["maxspeed"]?.let(MaxSpeedParser::parse)
            val limit = signed ?: DefaultSpeedLimits.estimate(tags) ?: return@mapNotNull null

            val geometry = way.optJSONArray("geometry") ?: return@mapNotNull null
            val points = (0 until geometry.length()).map { i ->
                val node = geometry.getJSONObject(i)
                LatLng(node.getDouble("lat"), node.getDouble("lon"))
            }
            if (points.size < 2) null else RoadSegment(way.getLong("id"), limit, points, estimated = signed == null)
        }
    }

    private fun tagsOf(way: JSONObject): Map<String, String> {
        val tags = way.optJSONObject("tags") ?: return emptyMap()
        return tags.keys().asSequence().associateWith { tags.optString(it) }
    }
}
