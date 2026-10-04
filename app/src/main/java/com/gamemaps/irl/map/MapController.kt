package com.gamemaps.irl.map

import android.content.Context
import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.data.radar.Radar
import com.gamemaps.irl.data.routing.Route
import com.gamemaps.irl.map.camera.CameraConfig
import com.gamemaps.irl.map.camera.FollowCamera
import com.gamemaps.irl.map.layers.DestinationLayer
import com.gamemaps.irl.map.layers.DestinationPinBitmap
import com.gamemaps.irl.map.layers.RadarLayer
import com.gamemaps.irl.map.layers.RouteLayer
import com.gamemaps.irl.map.layers.VehicleIconFactory
import com.gamemaps.irl.map.layers.VehicleMarkerLayer
import com.gamemaps.irl.map.theme.MapTheme
import com.gamemaps.irl.map.theme.MapThemeApplier
import com.gamemaps.irl.map.theme.ThemeTextureInstaller
import kotlinx.coroutines.flow.StateFlow
import org.maplibre.android.maps.Style

/**
 * Façade simple sur une carte prête à l'emploi.
 * Le téléphone et Android Auto n'appellent que ces méthodes.
 */
class MapController internal constructor(
    private val context: Context,
    private val style: Style,
    private val routeLayer: RouteLayer,
    private val destinationLayer: DestinationLayer,
    private val radarLayer: RadarLayer,
    private val vehicleLayer: VehicleMarkerLayer,
    private val camera: FollowCamera,
) {
    private var currentTheme: MapTheme? = null

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

    /** Repeint la carte et nos calques aux couleurs d'un autre thème, sans recharger la carte. */
    fun applyTheme(theme: MapTheme) {
        if (theme == currentTheme) return
        currentTheme = theme
        val patterns = ThemeTextureInstaller.install(context, style, theme.textures)
        MapThemeApplier.apply(style, theme.palette, patterns)
        routeLayer.applyPalette(theme.palette)
        vehicleLayer.setIcon(VehicleIconFactory.create(context, theme), rotates = theme.textures.vehicleSprite == null)
        destinationLayer.setIcon(DestinationPinBitmap.create(theme.palette))
    }

    /** 3D cockpit ou 2D vue de dessus. */
    fun applyCameraConfig(config: CameraConfig) {
        camera.setConfig(config)
    }
}
