package com.gamemaps.irl.map.camera

import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.map.toMapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.maps.MapLibreMap

/** Caméra qui suit le véhicule, inclinée, orientée dans le sens de la marche. */
class FollowCamera(
    private val map: MapLibreMap,
    private val viewHeightPx: () -> Int,
    private val config: CameraConfig,
) {
    fun follow(fix: GpsFix) {
        val position = CameraPosition.Builder()
            .target(fix.position.toMapLibre())
            .zoom(config.zoom)
            .tilt(config.tilt)
            .bearing(fix.bearingDegrees?.toDouble() ?: map.cameraPosition.bearing)
            .padding(0.0, viewHeightPx() * config.topPaddingRatio, 0.0, 0.0)
            .build()
        map.animateCamera(CameraUpdateFactory.newCameraPosition(position), config.animationMillis)
    }
}
