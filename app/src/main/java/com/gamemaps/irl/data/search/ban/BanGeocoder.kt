package com.gamemaps.irl.data.search.ban

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.network.getText
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.data.search.PlaceSearch
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

/**
 * Géocodage via la Base Adresse Nationale (service IGN Géoplateforme), gratuit et sans clé.
 * Couvre très bien les adresses françaises ; les POI (OSM) viendront dans une étape suivante.
 */
class BanGeocoder(private val http: OkHttpClient) : PlaceSearch {

    override suspend fun search(query: String, near: LatLng?): List<Place> {
        if (query.trim().length < MIN_QUERY_LENGTH) return emptyList()
        val body = http.getText(buildUrl(query.trim(), near))
        return BanResponseParser.parse(body)
    }

    private fun buildUrl(query: String, near: LatLng?): HttpUrl {
        val builder = HttpUrl.Builder()
            .scheme("https")
            .host("data.geopf.fr")
            .addPathSegments("geocodage/search")
            .addQueryParameter("q", query)
            .addQueryParameter("limit", RESULT_LIMIT.toString())
        if (near != null) {
            builder.addQueryParameter("lat", near.lat.toString())
            builder.addQueryParameter("lon", near.lng.toString())
        }
        return builder.build()
    }

    companion object {
        /** Le service refuse les requêtes de moins de 3 caractères. */
        const val MIN_QUERY_LENGTH = 3
        private const val RESULT_LIMIT = 8
    }
}
