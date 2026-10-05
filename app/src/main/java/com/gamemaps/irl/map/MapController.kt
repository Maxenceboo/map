package com.gamemaps.irl.map

import android.content.Context
import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.data.routing.Route
import com.gamemaps.irl.map.camera.CameraConfig
import com.gamemaps.irl.map.camera.FollowCamera
import com.gamemaps.irl.map.layers.DestinationLayer
import com.gamemaps.irl.map.layers.DestinationPinBitmap
import com.gamemaps.irl.map.layers.RouteLayer
import com.gamemaps.irl.map.layers.TrafficLayer
import com.gamemaps.irl.map.theme.MapTheme
import com.gamemaps.irl.map.theme.MapThemeApplier
import com.gamemaps.irl.map.theme.ThemeTextureInstaller
import com.gamemaps.irl.map.vehicle3d.Vehicle3DLayer
import com.gamemaps.irl.map.vehicle3d.VehicleColor
import com.gamemaps.irl.map.vehicle3d.VehicleKind
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
    private val trafficLayer: TrafficLayer,
    private val destinationLayer: DestinationLayer,
    private val vehicle3DLayer: Vehicle3DLayer,
    private val camera: FollowCamera,
) {
    private var currentTheme: MapTheme? = null
    private var vehicleKind = VehicleKind.ARROW
    private var vehicleColor = VehicleColor.YELLOW
    private var headlights = true

    /** false quand l'utilisateur a déplacé la carte : afficher le bouton RECENTRER. */
    val isFollowing: StateFlow<Boolean> get() = camera.isFollowing

    fun showVehicle(fix: GpsFix) {
        vehicle3DLayer.update(fix)
        camera.follow(fix)
    }

    /** Tracé, bordures de trafic et épingle de destination (au bout du tracé). null efface tout. */
    fun showRoute(route: Route?) {
        routeLayer.update(route)
        trafficLayer.update(route)
        destinationLayer.update(route?.geometry?.lastOrNull())
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
        destinationLayer.setIcon(DestinationPinBitmap.create(theme.palette))
        refreshVehicle()
    }

    /** Véhicule choisi dans Paramètres > Véhicule : modèle 3D, couleur, phares. */
    fun applyVehicle(kind: VehicleKind, color: VehicleColor, headlights: Boolean) {
        vehicleKind = kind
        vehicleColor = color
        this.headlights = headlights
        refreshVehicle()
    }

    /** À appeler pendant les mouvements de caméra : le véhicule 3D garde sa taille à l'écran. */
    fun onCameraMoved() {
        vehicle3DLayer.onCameraMoved()
    }

    /**
     * Le véhicule est toujours un modèle 3D : celui du thème s'il en impose un (cochon Minecraft),
     * sinon celui choisi dans les Paramètres.
     */
    private fun refreshVehicle() {
        val model = currentTheme?.textures?.vehicleModel ?: vehicleKind.model
        vehicle3DLayer.configure(model, vehicleColor, headlights)
    }

    /** 3D cockpit ou 2D vue de dessus. */
    fun applyCameraConfig(config: CameraConfig) {
        camera.setConfig(config)
    }
}
