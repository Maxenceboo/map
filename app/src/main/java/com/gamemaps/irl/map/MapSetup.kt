package com.gamemaps.irl.map

import com.gamemaps.irl.map.camera.CameraConfig
import com.gamemaps.irl.map.camera.FollowCamera
import com.gamemaps.irl.map.layers.RouteLayer
import com.gamemaps.irl.map.layers.VehicleArrowBitmap
import com.gamemaps.irl.map.layers.VehicleMarkerLayer
import com.gamemaps.irl.map.style.MapStyleSource
import com.gamemaps.irl.map.theme.MapTheme
import com.gamemaps.irl.map.theme.MapThemeApplier
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style

/**
 * Prépare une carte MapLibre : style, thème, calques, caméra.
 * Même code pour l'écran du téléphone et pour la surface Android Auto.
 */
object MapSetup {

    fun load(
        map: MapLibreMap,
        theme: MapTheme,
        cameraConfig: CameraConfig,
        viewHeightPx: () -> Int,
        interactive: Boolean,
        onReady: (MapController) -> Unit,
    ) {
        configureUi(map, interactive)
        map.setStyle(Style.Builder().fromUri(MapStyleSource.BASE_STYLE_URL)) { style ->
            MapThemeApplier.apply(style, theme.palette)
            val routeLayer = RouteLayer(style, theme.palette).apply { install() }
            val vehicleLayer = VehicleMarkerLayer(style, VehicleArrowBitmap.create(theme.palette)).apply { install() }
            onReady(MapController(routeLayer, vehicleLayer, FollowCamera(map, viewHeightPx, cameraConfig)))
        }
    }

    private fun configureUi(map: MapLibreMap, interactive: Boolean) {
        map.uiSettings.isCompassEnabled = false
        map.uiSettings.isLogoEnabled = false
        // L'attribution OpenStreetMap reste visible : elle est obligatoire (licence ODbL).
        if (!interactive) map.uiSettings.setAllGesturesEnabled(false)
    }
}
