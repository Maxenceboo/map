package com.gamemaps.irl.data.osm

import com.gamemaps.irl.data.network.HttpException
import com.gamemaps.irl.data.network.getText
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

/**
 * Exécute une requête sur l'API Overpass (lecture des données OpenStreetMap) et renvoie le JSON brut.
 * Les requêtes elles-mêmes sont écrites dans [OverpassQueries].
 *
 * Le serveur public refuse souvent les requêtes simultanées d'une même adresse (HTTP 429 / 504) :
 * - une seule requête à la fois ([mutex]) ;
 * - en cas de refus temporaire, on réessaie après [retryDelaysMillis].
 */
class OverpassClient(
    private val http: OkHttpClient,
    private val retryDelaysMillis: List<Long> = listOf(2_000, 5_000),
) {
    private val mutex = Mutex()

    suspend fun run(query: String): String = mutex.withLock {
        val url = urlFor(query)
        for (delayMillis in retryDelaysMillis) {
            try {
                return@withLock http.getText(url)
            } catch (e: HttpException) {
                if (e.code !in TEMPORARY_ERRORS) throw e
                delay(delayMillis)
            }
        }
        http.getText(url) // Dernière tentative : son erreur éventuelle remonte à l'appelant.
    }

    private fun urlFor(query: String): HttpUrl = HttpUrl.Builder()
        .scheme("https")
        .host("overpass-api.de")
        .addPathSegments("api/interpreter")
        .addQueryParameter("data", query)
        .build()

    private companion object {
        /** Trop de requêtes / serveur surchargé : ça vaut la peine de réessayer. */
        val TEMPORARY_ERRORS = setOf(429, 502, 503, 504)
    }
}
