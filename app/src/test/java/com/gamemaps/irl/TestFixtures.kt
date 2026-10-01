package com.gamemaps.irl

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.data.routing.ManeuverType
import com.gamemaps.irl.data.routing.Route
import com.gamemaps.irl.data.routing.RouteStep
import com.gamemaps.irl.data.search.Place

/** Données de test partagées : un itinéraire en "L" d'environ 2 km à Bordeaux. */
object TestFixtures {

    val START = LatLng(44.8400, -0.5800)
    val CORNER = LatLng(44.8490, -0.5800) // ~1 km au nord
    val END = LatLng(44.8490, -0.5673) // ~1 km à l'est

    val PLACE = Place(id = "dest", name = "Destination", subtitle = "33000 Bordeaux", position = END)

    /** Départ vers le nord, tourner à droite au coin, arrivée. */
    fun lShapedRoute(): Route {
        val geometry = listOf(START, CORNER, END)
        val leg1 = com.gamemaps.irl.core.geo.GeoMath.distanceMeters(START, CORNER)
        val leg2 = com.gamemaps.irl.core.geo.GeoMath.distanceMeters(CORNER, END)
        return Route(
            geometry = geometry,
            steps = listOf(
                RouteStep(ManeuverType.DEPART, START, "Cours de l'Intendance", null, 0.0, leg1, 60.0),
                RouteStep(ManeuverType.RIGHT, CORNER, "Rue Sainte-Catherine", null, leg1, leg2, 60.0),
                RouteStep(ManeuverType.ARRIVE, END, "", null, leg1 + leg2, 0.0, 0.0),
            ),
            durationSeconds = 120.0,
        )
    }

    fun fix(position: LatLng, timeMillis: Long = 0L) = GpsFix(
        position = position,
        speedMetersPerSecond = 10f,
        bearingDegrees = 0f,
        accuracyMeters = 5f,
        timeMillis = timeMillis,
    )
}
