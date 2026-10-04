package com.gamemaps.irl.map

import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.data.radar.Radar
import com.gamemaps.irl.data.routing.Route
import com.gamemaps.irl.map.camera.FollowCamera
import com.gamemaps.irl.map.layers.DestinationLayer
import com.gamemaps.irl.map.layers.RadarLayer
import com.gamemaps.irl.map.layers.RouteLayer
import com.gamemaps.irl.map.layers.VehicleMarkerLayer
import kotlinx.coroutines.flow.StateFlow

/**
 * Façade simple sur une carte prête à l'emploi.
 * Le téléphone et Android Auto n'appellent que ces méthodes.
 */
class MapController internal constructor(
    private val routeLayer: RouteLayer,
    private val destinationLayer: DestinationLayer,
    private val radarLayer: RadarLayer,
    private val vehicleLayer: VehicleMarkerLayer,
    private val camera: FollowCamera,
) {
    /** false quand l'utilisateur a déplacé la carte : afficher le bouton RECENTRER. */
    val isFollowing: StateFlow<Boolean> get() = camera.isFollowing

    fun showVehicle(fix: GpsFix) {
        vehicleLayer.update(fix)
        camera.follow(fix)
    }

    /** Tracé + épingle de destination (au bout du tracé). null efface les deux. */
    fun showRoute(route: Route?) {
        routeLayer.update(route)
        destinationLayer.update(route?.geometry?.lastOrNull())
    }

    fun showRadars(radars: List<Radar>) {
        radarLayer.update(radars)
    }

    /** Cadre tout l'itinéraire (aperçu avant le départ). */
    fun showOverview(route: Route) {
        camera.showOverview(route.geometry)
    }

    fun recenter() {
        camera.recenter()
    }
}
