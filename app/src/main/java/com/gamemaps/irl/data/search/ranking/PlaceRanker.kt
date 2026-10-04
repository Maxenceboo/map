package com.gamemaps.irl.data.search.ranking

import com.gamemaps.irl.core.geo.GeoMath
import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.data.search.PlaceKind

/**
 * Classe les résultats de plusieurs sources (BAN + Photon) sur une même échelle.
 *
 * Score =
 * - texte : part des mots tapés présents dans le lieu (0–50), +25 si le nom commence par la requête ;
 * - proximité : jusqu'à 30, divisé par deux tous les 10 km environ ;
 * - utilité : catégorie du lieu ([CategoryPriority], 0–12) ;
 * - +10 pour une adresse précise quand la requête contient un numéro ("12 rue …").
 */
object PlaceRanker {

    fun rank(query: String, places: List<Place>, near: LatLng?): List<Place> {
        val queryTokens = TextNormalizer.tokens(query)
        val normalizedQuery = TextNormalizer.normalize(query)
        val hasNumber = query.any { it.isDigit() }
        return places.sortedByDescending { score(it, queryTokens, normalizedQuery, hasNumber, near) }
    }

    private fun score(place: Place, queryTokens: List<String>, normalizedQuery: String, hasNumber: Boolean, near: LatLng?): Double {
        val haystack = TextNormalizer.normalize("${place.name} ${place.subtitle}")
        val matched = if (queryTokens.isEmpty()) 0.0 else queryTokens.count { haystack.contains(it) }.toDouble() / queryTokens.size
        var score = matched * 50
        if (TextNormalizer.normalize(place.name).startsWith(normalizedQuery)) score += 25
        if (near != null) {
            val km = GeoMath.distanceMeters(near, place.position) / 1_000
            score += 30 / (1 + km / 10)
        }
        score += CategoryPriority.of(place) * 4
        if (hasNumber && place.kind == PlaceKind.ADDRESS) score += 10
        return score
    }
}
