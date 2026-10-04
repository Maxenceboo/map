package com.gamemaps.irl.car.intent

import com.gamemaps.irl.core.geo.LatLng
import java.net.URLDecoder

/**
 * Lit l'adresse d'une demande de navigation envoyée par Android Auto.
 * Formats reconnus :
 * - `geo:44.84,-0.58` — un point ;
 * - `geo:0,0?q=44.84,-0.58(Maison)` — un point avec un nom ;
 * - `geo:0,0?q=gare+d'Arcachon` ou `google.navigation:q=gare+d'Arcachon` — un texte à chercher.
 */
object NavigationRequestParser {

    private val COORDINATES = Regex("""^\s*(-?\d+(?:\.\d+)?)\s*,\s*(-?\d+(?:\.\d+)?)\s*(?:\((.*)\))?\s*$""")

    fun parse(uri: String?): NavigationRequest? {
        if (uri.isNullOrBlank()) return null
        val scheme = uri.substringBefore(':', missingDelimiterValue = "").lowercase()
        val rest = uri.substringAfter(':')
        return when (scheme) {
            "geo" -> parseQuery(rest.substringAfter('?', "")) ?: parseCoordinates(rest.substringBefore('?'))
            "google.navigation" -> parseQuery(rest)
            else -> null
        }
    }

    /** Paramètre `q` : des coordonnées ou un texte libre. */
    private fun parseQuery(query: String): NavigationRequest? {
        val raw = query.split('&').firstOrNull { it.startsWith("q=") }?.removePrefix("q=") ?: return null
        val text = runCatching { URLDecoder.decode(raw, "UTF-8") }.getOrDefault(raw).trim()
        if (text.isEmpty()) return null
        return parseCoordinates(text) ?: NavigationRequest.ToQuery(text)
    }

    /** "lat,lng" ou "lat,lng(nom)". Le point 0,0 veut dire "pas de position" dans une adresse geo. */
    private fun parseCoordinates(text: String): NavigationRequest.ToPosition? {
        val match = COORDINATES.matchEntire(text) ?: return null
        val lat = match.groupValues[1].toDouble()
        val lng = match.groupValues[2].toDouble()
        if (lat !in -90.0..90.0 || lng !in -180.0..180.0 || (lat == 0.0 && lng == 0.0)) return null
        return NavigationRequest.ToPosition(LatLng(lat, lng), match.groupValues[3].trim().ifEmpty { null })
    }
}
