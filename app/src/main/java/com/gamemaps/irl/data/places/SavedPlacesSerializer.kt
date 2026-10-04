package com.gamemaps.irl.data.places

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.data.search.PlaceKind
import org.json.JSONArray
import org.json.JSONObject

/** Conversion [SavedPlaces] ⇄ JSON, pour la sauvegarde sur le téléphone. */
object SavedPlacesSerializer {

    fun toJson(saved: SavedPlaces): String = JSONObject().apply {
        saved.home?.let { put("home", placeToJson(it)) }
        saved.work?.let { put("work", placeToJson(it)) }
        put("favorites", JSONArray().apply { saved.favorites.forEach { put(placeToJson(it)) } })
    }.toString()

    /** Un contenu illisible (version future, corruption) donne une liste vide plutôt qu'un plantage. */
    fun fromJson(json: String?): SavedPlaces {
        if (json.isNullOrBlank()) return SavedPlaces()
        return runCatching {
            val root = JSONObject(json)
            val favorites = root.optJSONArray("favorites") ?: JSONArray()
            SavedPlaces(
                home = root.optJSONObject("home")?.let(::placeFromJson),
                work = root.optJSONObject("work")?.let(::placeFromJson),
                favorites = (0 until favorites.length()).mapNotNull { placeFromJson(favorites.getJSONObject(it)) },
            )
        }.getOrDefault(SavedPlaces())
    }

    private fun placeToJson(place: Place) = JSONObject().apply {
        put("id", place.id)
        put("name", place.name)
        put("subtitle", place.subtitle)
        put("lat", place.position.lat)
        put("lng", place.position.lng)
        put("kind", place.kind.name)
        place.category?.let { put("category", it) }
    }

    private fun placeFromJson(json: JSONObject): Place? = runCatching {
        Place(
            id = json.getString("id"),
            name = json.getString("name"),
            subtitle = json.optString("subtitle"),
            position = LatLng(json.getDouble("lat"), json.getDouble("lng")),
            kind = runCatching { PlaceKind.valueOf(json.optString("kind")) }.getOrDefault(PlaceKind.ADDRESS),
            category = if (json.has("category")) json.getString("category") else null,
        )
    }.getOrNull()
}
