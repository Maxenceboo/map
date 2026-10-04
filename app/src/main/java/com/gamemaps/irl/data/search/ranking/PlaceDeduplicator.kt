package com.gamemaps.irl.data.search.ranking

import com.gamemaps.irl.core.geo.GeoMath
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.data.search.PlaceKind

/**
 * Supprime les doublons : même nom (aux accents près) et positions proches.
 *
 * - Lieux ordinaires : moins de [RADIUS_METERS]. Typique d'OpenStreetMap, où la gare,
 *   son arrêt de tram et son parvis portent le même nom.
 * - Villes : moins de [CITY_RADIUS_METERS]. Une même ville existe en plusieurs objets
 *   (point central, limite administrative, BAN + OSM) distants de quelques kilomètres.
 *
 * On garde le lieu le plus utile ([CategoryPriority]).
 */
object PlaceDeduplicator {

    private const val RADIUS_METERS = 400.0
    private const val CITY_RADIUS_METERS = 15_000.0

    fun deduplicate(places: List<Place>): List<Place> {
        val kept = mutableListOf<Place>()
        places.sortedByDescending(CategoryPriority::of).forEach { candidate ->
            val key = TextNormalizer.normalize(candidate.name)
            val duplicate = kept.any { other ->
                TextNormalizer.normalize(other.name) == key &&
                    GeoMath.distanceMeters(other.position, candidate.position) < radiusFor(other, candidate)
            }
            if (!duplicate) kept += candidate
        }
        return kept
    }

    private fun radiusFor(a: Place, b: Place): Double =
        if (a.kind == PlaceKind.CITY && b.kind == PlaceKind.CITY) CITY_RADIUS_METERS else RADIUS_METERS
}
