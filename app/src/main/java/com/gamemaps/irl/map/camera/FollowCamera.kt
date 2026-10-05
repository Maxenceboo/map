package com.gamemaps.irl.map.camera

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.map.toMapLibre
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap

/**
 * Caméra de la carte.
 *
 * - **Suivi** : inclinée derrière le véhicule, orientée dans le sens de la marche.
 *   Elle est déplacée à chaque image par l'animation du véhicule (voir `VehicleAnimator`).
 * - **Pause** : dès que l'utilisateur déplace la carte au doigt ([pause]), on arrête de suivre
 *   (sinon la caméra lui reprendrait la main). [isFollowing] permet d'afficher le bouton RECENTRER.
 * - **Vue d'ensemble** : cadre tout un itinéraire (aperçu avant le départ).
 */
class FollowCamera(
    private val map: MapLibreMap,
    private val viewHeightPx: () -> Int,
    private var config: CameraConfig,
) {
    private var hasPositioned = false
    private var lastFix: GpsFix? = null

    private val _isFollowing = MutableStateFlow(true)
    val isFollowing: StateFlow<Boolean> = _isFollowing.asStateFlow()

    fun follow(fix: GpsFix) {
        lastFix = fix
        if (!_isFollowing.value) return
        // Appelée à chaque image de l'animation du véhicule : un simple déplacement suffit, il est déjà fluide.
        map.moveCamera(CameraUpdateFactory.newCameraPosition(positionFor(fix)))
        hasPositioned = true
    }

    /** Changement de perspective (3D / 2D) : appliqué tout de suite si on suit le véhicule. */
    fun setConfig(newConfig: CameraConfig) {
        config = newConfig
        if (_isFollowing.value) recenter()
    }

    fun pause() {
        _isFollowing.value = false
    }

    /** Reprend le suivi et revient immédiatement sur le véhicule. */
    fun recenter() {
        _isFollowing.value = true
        lastFix?.let { map.animateCamera(CameraUpdateFactory.newCameraPosition(positionFor(it)), RECENTER_MILLIS) }
    }

    /** Cadre tous les [points] (vue de dessus), en laissant de la place aux panneaux du HUD. */
    fun showOverview(points: List<LatLng>) {
        if (points.size < 2) return
        pause()
        val bounds = LatLngBounds.Builder().includes(points.map { it.toMapLibre() }).build()
        val height = viewHeightPx()
        val margins = intArrayOf(
            OVERVIEW_SIDE_PADDING_PX,
            (height * 0.10).toInt(),
            OVERVIEW_SIDE_PADDING_PX,
            (height * 0.48).toInt(), // la fiche d'aperçu occupe presque la moitié basse de l'écran
        )
        val framed = map.getCameraForLatLngBounds(bounds, margins, 0.0, 0.0) ?: return
        // Vue de dessus, nord en haut, et sans le décalage vers le haut du mode suivi
        // (il resterait appliqué et pousserait le trajet sous le panneau).
        val overview = CameraPosition.Builder(framed)
            .bearing(0.0)
            .tilt(0.0)
            .padding(0.0, 0.0, 0.0, 0.0)
            .build()
        map.animateCamera(CameraUpdateFactory.newCameraPosition(overview), RECENTER_MILLIS)
    }

    private fun positionFor(fix: GpsFix): CameraPosition = CameraPosition.Builder()
        .target(fix.position.toMapLibre())
        .zoom(config.zoom)
        .tilt(config.tilt)
        .bearing(fix.bearingDegrees?.toDouble() ?: map.cameraPosition.bearing)
        .padding(0.0, viewHeightPx() * config.topPaddingRatio, 0.0, 0.0)
        .build()

    private companion object {
        const val RECENTER_MILLIS = 800
        const val OVERVIEW_SIDE_PADDING_PX = 120
    }
}
