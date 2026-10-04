package com.gamemaps.irl.data.search.photon

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.data.search.PlaceKind
import org.json.JSONObject

/**
 * Transforme la réponse GeoJSON de Photon (OpenStreetMap) en liste de [Place].
 *
 * `properties = { osm_id, osm_key, osm_value, type, name, street, housenumber, postcode, city }`.
 */
object PhotonResponseParser {

    fun parse(json: String): List<Place> {
        val features = JSONObject(json).optJSONArray("features") ?: return emptyList()
        return (0 until features.length()).mapNotNull { index ->
            val feature = features.getJSONObject(index)
            val props = feature.optJSONObject("properties") ?: return@mapNotNull null
            val coords = feature.optJSONObject("geometry")?.optJSONArray("coordinates") ?: return@mapNotNull null
            val name = props.optString("name").ifBlank { addressLine(props) }.ifBlank { return@mapNotNull null }
            val kind = kindOf(props)
            Place(
                id = "photon_${props.optString("osm_type")}${props.optLong("osm_id")}",
                name = name,
                subtitle = subtitleOf(props, name),
                position = LatLng(lat = coords.getDouble(1), lng = coords.getDouble(0)),
                kind = kind,
                category = if (kind == PlaceKind.POI) "${props.optString("osm_key")}:${props.optString("osm_value")}" else null,
            )
        }
    }

    private fun kindOf(props: JSONObject): PlaceKind = when {
        props.optString("osm_key") == "place" -> PlaceKind.CITY
        props.optString("type") in setOf("city", "district", "locality") -> PlaceKind.CITY
        props.optString("osm_key") == "highway" && props.optString("type") == "street" -> PlaceKind.STREET
        props.optString("osm_key") in setOf("building", "place") && props.optString("name").isBlank() -> PlaceKind.ADDRESS
        else -> PlaceKind.POI
    }

    private fun addressLine(props: JSONObject): String =
        listOf(props.optString("housenumber"), props.optString("street")).filter { it.isNotBlank() }.joinToString(" ")

    /** "Parvis Louis Armand, 33800 Bordeaux" — sans répéter le nom s'il est déjà l'adresse. */
    private fun subtitleOf(props: JSONObject, name: String): String {
        val street = addressLine(props).takeIf { it != name }
        val city = listOf(props.optString("postcode"), props.optString("city")).filter { it.isNotBlank() }.joinToString(" ")
        return listOfNotNull(street?.ifBlank { null }, city.ifBlank { null }).joinToString(", ")
    }
}
