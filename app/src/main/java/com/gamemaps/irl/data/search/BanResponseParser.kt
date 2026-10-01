package com.gamemaps.irl.data.search

import com.gamemaps.irl.core.geo.LatLng
import org.json.JSONObject

/**
 * Transforme la réponse GeoJSON de la BAN en liste de [Place].
 *
 * Format attendu : `features[].geometry.coordinates = [lng, lat]` et
 * `features[].properties = { id, label, name, postcode, city, context }`.
 */
object BanResponseParser {

    fun parse(json: String): List<Place> {
        val features = JSONObject(json).optJSONArray("features") ?: return emptyList()
        return (0 until features.length()).mapNotNull { index ->
            val feature = features.getJSONObject(index)
            val props = feature.optJSONObject("properties") ?: return@mapNotNull null
            val coords = feature.optJSONObject("geometry")?.optJSONArray("coordinates") ?: return@mapNotNull null
            val label = props.optString("label")
            Place(
                id = props.optString("id").ifBlank { label },
                name = props.optString("name").ifBlank { label },
                subtitle = subtitleOf(props),
                position = LatLng(lat = coords.getDouble(1), lng = coords.getDouble(0)),
            )
        }
    }

    private fun subtitleOf(props: JSONObject): String {
        val cityLine = listOf(props.optString("postcode"), props.optString("city"))
            .filter { it.isNotBlank() }
            .joinToString(" ")
        return cityLine.ifBlank { props.optString("context") }
    }
}
