package com.gamemaps.irl.data.routing.osrm

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.network.getText
import com.gamemaps.irl.data.routing.Route
import com.gamemaps.irl.data.routing.RoutingService
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

/**
 * Client du serveur OSRM public (sans trafic temps réel).
 * Moteur de secours quand TomTom n'est pas disponible, derrière la même interface [RoutingService].
 */
class OsrmClient(private val http: OkHttpClient) : RoutingService {

    override suspend fun route(from: LatLng, to: LatLng): Route =
        OsrmResponseParser.parse(http.getText(buildUrl(from, to)))

    private fun buildUrl(from: LatLng, to: LatLng): HttpUrl = HttpUrl.Builder()
        .scheme("https")
        .host("router.project-osrm.org")
        .addPathSegments("route/v1/driving")
        // OSRM attend "lng,lat;lng,lat".
        .addPathSegment("${from.lng},${from.lat};${to.lng},${to.lat}")
        .addQueryParameter("overview", "full")
        .addQueryParameter("geometries", "geojson")
        .addQueryParameter("steps", "true")
        .addQueryParameter("alternatives", "false")
        .build()
}
