package com.gamemaps.irl.data.search.photon

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.network.getText
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.data.search.PlaceSearch
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

/**
 * Recherche de lieux (gares, stations-service, magasins…) via Photon, l'index OpenStreetMap de Komoot.
 * Complète la BAN, qui ne connaît que les adresses.
 */
class PhotonGeocoder(private val http: OkHttpClient) : PlaceSearch {

    override suspend fun search(query: String, near: LatLng?): List<Place> =
        PhotonResponseParser.parse(http.getText(buildUrl(query.trim(), near)))

    private fun buildUrl(query: String, near: LatLng?): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme("https")
            .host("photon.komoot.io")
            .addPathSegment("api")
            .addQueryParameter("q", query)
            .addQueryParameter("limit", RESULT_LIMIT.toString())
            .addQueryParameter("lang", "fr")
        if (near != null) {
            builder.addQueryParameter("lat", near.lat.toString())
            builder.addQueryParameter("lon", near.lng.toString())
        }
        // Objets sans intérêt comme destination : on les exclut dès le serveur.
        EXCLUDED_TAGS.forEach { builder.addQueryParameter("osm_tag", "!$it") }
        return builder.build()
    }

    private companion object {
        const val RESULT_LIMIT = 15
        val EXCLUDED_TAGS = listOf(
            "amenity:vending_machine",
            "amenity:waste_basket",
            "amenity:bench",
            "amenity:recycling",
            "amenity:bicycle_parking",
            "amenity:post_box",
            "amenity:telephone",
            "amenity:bicycle_rental",
            "amenity:taxi",
            "railway:platform",
            "highway:street_lamp",
            "entrance",
        )
    }
}
