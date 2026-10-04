package com.gamemaps.irl.data.search

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.search.ranking.PlaceDeduplicator
import com.gamemaps.irl.data.search.ranking.PlaceRanker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Interroge plusieurs moteurs en parallèle (BAN pour les adresses, Photon pour les lieux),
 * puis fusionne : doublons retirés, classement commun, [maxResults] meilleurs résultats.
 *
 * Si un moteur échoue, les autres suffisent ; on n'échoue que si tous échouent.
 */
class HybridPlaceSearch(
    private val sources: List<PlaceSearch>,
    private val maxResults: Int = 8,
) : PlaceSearch {

    override suspend fun search(query: String, near: LatLng?): List<Place> = coroutineScope {
        val outcomes = sources.map { source -> async { runSource(source, query, near) } }.awaitAll()
        if (outcomes.all { it.isFailure }) throw outcomes.first().exceptionOrNull()!!
        val merged = outcomes.flatMap { it.getOrDefault(emptyList()) }
        PlaceRanker.rank(query, PlaceDeduplicator.deduplicate(merged), near).take(maxResults)
    }

    private suspend fun runSource(source: PlaceSearch, query: String, near: LatLng?): Result<List<Place>> = try {
        Result.success(source.search(query, near))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
