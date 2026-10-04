package com.gamemaps.irl.data.radar

import com.gamemaps.irl.core.geo.GeoMath

/**
 * Fusionne la base officielle et OpenStreetMap.
 *
 * La base officielle fait foi (type, vitesse) ; un radar OSM à moins de [duplicateMeters]
 * d'un radar officiel est considéré comme le même et ignoré. Les autres radars OSM
 * (installés depuis, ou à l'étranger) complètent la liste.
 */
object RadarMerger {

    private const val DUPLICATE_METERS = 60.0

    fun merge(official: List<Radar>, osm: List<Radar>, duplicateMeters: Double = DUPLICATE_METERS): List<Radar> {
        val extras = osm.filter { candidate ->
            official.none { GeoMath.distanceMeters(it.position, candidate.position) < duplicateMeters }
        }
        return official + extras
    }
}
