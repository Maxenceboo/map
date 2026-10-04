package com.gamemaps.irl.data.search.ranking

import com.gamemaps.irl.core.geo.GeoMath
import com.gamemaps.irl.data.search.Place

/**
 * Supprime les doublons : même nom (aux accents près) à moins de [radiusMeters].
 * Typique d'OpenStreetMap, où la gare, son arrêt de tram et son parvis portent le même nom.
 * On garde le lieu le plus utile ([CategoryPriority]).
 */
object PlaceDeduplicator {

    private const val RADIUS_METERS = 400.0

    fun deduplicate(places: List<Place>, radiusMeters: Double = RADIUS_METERS): List<Place> {
        val kept = mutableListOf<Place>()
        places.sortedByDescending(CategoryPriority::of).forEach { candidate ->
            val key = TextNormalizer.normalize(candidate.name)
            val duplicate = kept.any {
                TextNormalizer.normalize(it.name) == key &&
                    GeoMath.distanceMeters(it.position, candidate.position) < radiusMeters
            }
            if (!duplicate) kept += candidate
        }
        return kept
    }
}
