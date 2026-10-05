package com.gamemaps.irl.map

import android.content.Context
import com.gamemaps.irl.map.camera.CameraConfig
import com.gamemaps.irl.map.camera.FollowCamera
import com.gamemaps.irl.map.layers.DestinationLayer
import com.gamemaps.irl.map.layers.DestinationPinBitmap
import com.gamemaps.irl.map.layers.RouteLayer
import com.gamemaps.irl.map.layers.TrafficLayer
import com.gamemaps.irl.map.style.MapStyleSource
import com.gamemaps.irl.map.theme.MapTheme
import com.gamemaps.irl.map.vehicle3d.Vehicle3DLayer
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style

/**
 * Prépare une carte MapLibre : style, thème, calques, caméra.
 * Même code pour l'écran du téléphone et pour la surface Android Auto.
 */
object MapSetup {

    fun load(
        context: Context,
        map: MapLibreMap,
        theme: MapTheme,
        cameraConfig: CameraConfig,
        viewHeightPx: () -> Int,
        interactive: Boolean,
        onReady: (MapController) -> Unit,
    ) {
        configureUi(map, interactive)
        map.setStyle(Style.Builder().fromUri(MapStyleSource.BASE_STYLE_URL)) { style ->
            // Ordre d'empilement : tracé (et trafic sous la ligne), destination, puis véhicule tout en haut.
            val routeLayer = RouteLayer(style, theme.palette).apply { install() }
            val trafficLayer = TrafficLayer(style).apply { install() }
            val destinationLayer = DestinationLayer(style, DestinationPinBitmap.create(theme.palette)).apply { install() }
            val vehicle3DLayer = Vehicle3DLayer(style, zoom = { map.cameraPosition.zoom }).apply { install() }
            val camera = FollowCamera(map, viewHeightPx, cameraConfig)
            if (interactive) pauseFollowingOnUserGesture(map, camera)
            // applyTheme repeint le fond de carte, installe les textures et choisit le véhicule.
            val controller = MapController(context, style, routeLayer, trafficLayer, destinationLayer, vehicle3DLayer, camera)
            controller.applyTheme(theme)
            map.addOnCameraMoveListener(controller::onCameraMoved)
            onReady(controller)
        }
    }

    private fun configureUi(map: MapLibreMap, interactive: Boolean) {
        map.uiSettings.isCompassEnabled = false
        map.uiSettings.isLogoEnabled = false
        // L'attribution OpenStreetMap reste visible : elle est obligatoire (licence ODbL).
        if (!interactive) map.uiSettings.setAllGesturesEnabled(false)
    }

    /** Un glissement / pincement du doigt met le suivi en pause (les mouvements programmés, non). */
    private fun pauseFollowingOnUserGesture(map: MapLibreMap, camera: FollowCamera) {
        map.addOnCameraMoveStartedListener { reason ->
            if (reason == MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE) camera.pause()
        }
    }
}
