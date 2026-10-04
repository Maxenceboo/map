package com.gamemaps.irl.map

import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.data.radar.Radar
import com.gamemaps.irl.data.routing.Route
import com.gamemaps.irl.map.camera.FollowCamera
import com.gamemaps.irl.map.layers.RadarLayer
import com.gamemaps.irl.map.layers.RouteLayer
import com.gamemaps.irl.map.layers.VehicleMarkerLayer

/**
 * Façade simple sur une carte prête à l'emploi.
 * Le téléphone et Android Auto n'appellent que ces méthodes.
 */
class MapController internal constructor(
    private val routeLayer: RouteLayer,
    private val radarLayer: RadarLayer,
    private val vehicleLayer: VehicleMarkerLayer,
    private val camera: FollowCamera,
) {
    fun showVehicle(fix: GpsFix) {
        vehicleLayer.update(fix)
        camera.follow(fix)
    }

    fun showRoute(route: Route?) {
        routeLayer.update(route)
    }

    fun showRadars(radars: List<Radar>) {
        radarLayer.update(radars)
    }
}
