package com.gamemaps.irl.map.camera

import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.map.toMapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.maps.MapLibreMap

/**
 * Caméra qui suit le véhicule, inclinée, orientée dans le sens de la marche.
 *
 * - Première position : saut immédiat (sinon on voit un long dézoom depuis la vue par défaut).
 * - Ensuite : glissement linéaire d'une position à la suivante, sans accélération, pour un
 *   suivi fluide quand les positions arrivent toutes les ~500 ms.
 */
class FollowCamera(
    private val map: MapLibreMap,
    private val viewHeightPx: () -> Int,
    private val config: CameraConfig,
) {
    private var hasPositioned = false

    fun follow(fix: GpsFix) {
        val update = CameraUpdateFactory.newCameraPosition(positionFor(fix))
        if (!hasPositioned) {
            map.moveCamera(update)
            hasPositioned = true
        } else {
            map.easeCamera(update, config.animationMillis, false)
        }
    }

    private fun positionFor(fix: GpsFix): CameraPosition = CameraPosition.Builder()
        .target(fix.position.toMapLibre())
        .zoom(config.zoom)
        .tilt(config.tilt)
        .bearing(fix.bearingDegrees?.toDouble() ?: map.cameraPosition.bearing)
        .padding(0.0, viewHeightPx() * config.topPaddingRatio, 0.0, 0.0)
        .build()
}
