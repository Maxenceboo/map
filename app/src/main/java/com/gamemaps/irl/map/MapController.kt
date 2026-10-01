package com.gamemaps.irl.map

import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.data.routing.Route
import com.gamemaps.irl.map.camera.FollowCamera
import com.gamemaps.irl.map.layers.RouteLayer
import com.gamemaps.irl.map.layers.VehicleMarkerLayer

/**
 * Façade simple sur une carte prête à l'emploi.
 * Le téléphone et Android Auto n'appellent que ces deux méthodes.
 */
class MapController internal constructor(
    private val routeLayer: RouteLayer,
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
}
