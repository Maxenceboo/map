package com.gamemaps.irl.data.routing.tomtom

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.network.getText
import com.gamemaps.irl.data.routing.Route
import com.gamemaps.irl.data.routing.RoutingService
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

/**
 * Calcul d'itinéraire TomTom : le trajet le plus rapide compte tenu des bouchons du moment,
 * avec la liste des portions ralenties.
 * Référence : https://developer.tomtom.com/routing-api/documentation/tomtom-maps/calculate-route
 */
class TomTomClient(private val http: OkHttpClient, private val apiKey: String) : RoutingService {

    override suspend fun route(from: LatLng, to: LatLng): Route =
        TomTomResponseParser.parse(http.getText(buildUrl(from, to, extraRoutes = 0)))

    override suspend fun alternatives(from: LatLng, to: LatLng): List<Route> =
        TomTomResponseParser.parseAll(http.getText(buildUrl(from, to, extraRoutes = 2)))

    private fun buildUrl(from: LatLng, to: LatLng, extraRoutes: Int): HttpUrl = HttpUrl.Builder()
        .scheme("https")
        .host("api.tomtom.com")
        .addPathSegments("routing/1/calculateRoute")
        // TomTom attend "lat,lng:lat,lng".
        .addPathSegment("${from.lat},${from.lng}:${to.lat},${to.lng}")
        .addPathSegment("json")
        .addQueryParameter("traffic", "true")
        .addQueryParameter("maxAlternatives", extraRoutes.toString())
        .addQueryParameter("routeType", "fastest")
        .addQueryParameter("travelMode", "car")
        .addQueryParameter("sectionType", "traffic")
        // "coded" : codes de manœuvre sans phrases ; nos phrases françaises sont construites par l'app.
        .addQueryParameter("instructionsType", "coded")
        .addQueryParameter("key", apiKey)
        .build()
}
