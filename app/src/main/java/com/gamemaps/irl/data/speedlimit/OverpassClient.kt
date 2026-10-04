package com.gamemaps.irl.data.speedlimit

import com.gamemaps.irl.core.geo.BoundingBox
import com.gamemaps.irl.data.network.getText
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

/**
 * Récupère, via l'API Overpass (OpenStreetMap), toutes les routes d'une zone qui ont une
 * vitesse maximale renseignée. Une requête couvre ~1 km² : on ne rappelle le serveur
 * qu'en sortant de la zone déjà chargée.
 */
class OverpassClient(private val http: OkHttpClient) {

    suspend fun roadsWithMaxSpeed(box: BoundingBox): List<RoadSegment> =
        OverpassResponseParser.parse(http.getText(buildUrl(box)))

    private fun buildUrl(box: BoundingBox): HttpUrl {
        // Overpass attend la bbox dans l'ordre (sud, ouest, nord, est).
        val bbox = "${box.south},${box.west},${box.north},${box.east}"
        val query = "[out:json][timeout:15];way($bbox)[highway][maxspeed];out tags geom;"
        return HttpUrl.Builder()
            .scheme("https")
            .host("overpass-api.de")
            .addPathSegments("api/interpreter")
            .addQueryParameter("data", query)
            .build()
    }
}
