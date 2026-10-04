package com.gamemaps.irl.data.radar.official

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.radar.Radar
import com.gamemaps.irl.data.radar.RadarType
import org.json.JSONArray

/**
 * Lit la base embarquée `assets/radars_france.json` (format compact) :
 * `{ id, t: type, c: [lng, lat], s: vitesse, r?: route, p?: lieu, d?: sens }`.
 */
object OfficialRadarParser {

    fun parse(json: String): List<Radar> {
        val array = JSONArray(json)
        return (0 until array.length()).mapNotNull { index ->
            val item = array.getJSONObject(index)
            val coords = item.optJSONArray("c") ?: return@mapNotNull null
            val type = typeOf(item.optString("t")) ?: return@mapNotNull null
            Radar(
                id = "fr_${item.getString("id")}",
                position = LatLng(lat = coords.getDouble(1), lng = coords.getDouble(0)),
                type = type,
                maxSpeedKmh = item.optInt("s").takeIf { it > 0 },
                road = item.optString("r").ifBlank { null },
            )
        }
    }

    private fun typeOf(code: String): RadarType? = when (code) {
        "speed" -> RadarType.SPEED
        "red_light" -> RadarType.RED_LIGHT
        "discriminant" -> RadarType.DISCRIMINANT
        "section" -> RadarType.SECTION
        "crossing" -> RadarType.LEVEL_CROSSING
        else -> null
    }
}
