package com.gamemaps.irl.map

import android.content.Context
import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.data.routing.Route
import com.gamemaps.irl.map.camera.CameraConfig
import com.gamemaps.irl.map.camera.FollowCamera
import com.gamemaps.irl.map.layers.AlternativeRoutesLayer
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
    private val alternativesLayer: AlternativeRoutesLayer,
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

    /** Le véhicule et la caméra glissent ensemble d'une position à la suivante. */
    private val vehicleAnimator = VehicleAnimator { fix ->
        vehicle3DLayer.update(fix)
        camera.follow(fix)
    }

    /** Nouvelle position : le véhicule la rejoint en douceur. */
    fun showVehicle(fix: GpsFix) {
        vehicleAnimator.moveTo(fix)
    }

    /** Tracé, bordures de trafic et épingle de destination (au bout du tracé). null efface tout. */
    fun showRoute(route: Route?) {
        routeLayer.update(route)
        trafficLayer.update(route)
        destinationLayer.update(route?.geometry?.lastOrNull())
    }

    /** Trajets proposés mais non choisis, en gris (aperçu). Liste vide : on les efface. */
    fun showAlternatives(routes: List<Route>) {
        alternativesLayer.update(routes)
    }

    /** Cadre tous les trajets proposés (aperçu avant le départ). */
    fun showOverview(routes: List<Route>) {
        camera.showOverview(routes.flatMap { it.geometry })
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

    /** Largeur de carte masquée à gauche par un panneau (écran de la voiture). */
    fun setLeftInset(px: Int) {
        camera.setLeftInset(px)
    }

    /** 3D cockpit ou 2D vue de dessus. */
    fun applyCameraConfig(config: CameraConfig) {
        camera.setConfig(config)
    }
}
