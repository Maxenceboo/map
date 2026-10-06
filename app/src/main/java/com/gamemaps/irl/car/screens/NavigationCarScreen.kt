package com.gamemaps.irl.car.screens

import com.gamemaps.irl.data.routing.Route
import androidx.car.app.constraints.ConstraintManager
import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.gamemaps.irl.car.alerts.CarRadarAlerter
import com.gamemaps.irl.car.surface.CarMapSurface
import com.gamemaps.irl.car.templates.CalculatingTemplate
import com.gamemaps.irl.car.templates.IdleTemplate
import com.gamemaps.irl.car.templates.MessageTemplates
import com.gamemaps.irl.car.templates.NavigatingTemplate
import com.gamemaps.irl.car.templates.PreviewTemplate
import com.gamemaps.irl.car.trip.CarTripReporter
import com.gamemaps.irl.data.location.LocationPermissions
import com.gamemaps.irl.di.AppContainer
import com.gamemaps.irl.map.MapController
import com.gamemaps.irl.map.camera.CameraConfig
import com.gamemaps.irl.navigation.NavigationState
import com.gamemaps.irl.navigation.trip.toMissionPassedModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * Écran principal Android Auto : la carte (sur la Surface) + le template qui correspond
 * à l'état du guidage. Observe le même [AppContainer] que le téléphone.
 */
class NavigationCarScreen(
    carContext: CarContext,
    private val container: AppContainer,
) : Screen(carContext) {

    private val engine = container.navigationEngine
    private val audio = container.audioPreferences
    private var navigation: NavigationState = engine.state.value
    private var hasLocationPermission = LocationPermissions.isGranted(carContext)
    private var mapController: MapController? = null

    /** Trajets actuellement cadrés sur la carte (aperçu) ; null hors aperçu. */
    private var framedRoutes: List<Route>? = null

    /** Menu des destinations affiché à gauche de la carte quand il n'y a pas de trajet. */
    private var menuVisible = true

    private val maxPlaces: Int =
        carContext.getCarService(ConstraintManager::class.java).getContentLimit(ConstraintManager.CONTENT_LIMIT_TYPE_PLACE_LIST)

    private val maxRoutes: Int =
        carContext.getCarService(ConstraintManager::class.java).getContentLimit(ConstraintManager.CONTENT_LIMIT_TYPE_ROUTE_LIST)

    private val tripReporter = CarTripReporter(carContext, onStopRequested = engine::stop)
    private val radarAlerter = CarRadarAlerter(carContext)
    private val mapSurface = CarMapSurface(carContext, container.settingsRepository.settings.value.theme) { controller ->
        mapController = controller
        // Nouvelle surface (plein écran ⇄ vue partagée) : l'aperçu doit être recadré à la nouvelle taille.
        framedRoutes = null
        refreshMap()
    }

    init {
        carContext.getCarService(AppManager::class.java).setSurfaceCallback(mapSurface)
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) = release()
        })
        observeNavigation()
        observeLocation()
        observeRadars()
        observeMute()
        observeSavedPlaces()
        observeSettings()
    }

    override fun onGetTemplate(): Template {
        if (!hasLocationPermission) return MessageTemplates.permissionRequired(::requestLocationPermission)
        return when (val state = navigation) {
            is NavigationState.Idle -> if (menuVisible) {
                IdleTemplate.menu(
                    saved = container.savedPlacesRepository.saved.value,
                    near = container.locationRepository.fixes.value?.position,
                    maxRows = maxPlaces,
                    isMuted = audio.muted.value,
                    onSearch = ::openSearch,
                    onPlace = engine::start,
                    onUndefined = ::explainUndefinedPlace,
                    onHideMenu = { showMenu(false) },
                    onToggleMute = audio::toggleMuted,
                )
            } else {
                IdleTemplate.mapOnly(onShowMenu = { showMenu(true) }, isMuted = audio.muted.value, onToggleMute = audio::toggleMuted)
            }
            is NavigationState.Calculating -> CalculatingTemplate.build(onStop = engine::stop)
            is NavigationState.Previewing -> PreviewTemplate.build(
                state = state,
                maxRoutes = maxRoutes,
                onSelect = engine::selectRoute,
                onStart = engine::confirm,
                onCancel = engine::stop,
            )
            is NavigationState.Navigating -> NavigatingTemplate.build(
                state = state,
                isMuted = audio.muted.value,
                onSearch = ::openSearch,
                onToggleMute = audio::toggleMuted,
                onStop = engine::stop,
            )
            is NavigationState.Arrived -> {
                val mission = state.toMissionPassedModel()
                MessageTemplates.arrived(
                    destinationName = mission.destinationName,
                    summary = "${mission.distance} · ${mission.duration} · moyenne ${mission.averageSpeed}",
                    onDone = engine::stop,
                )
            }
            is NavigationState.Failed -> MessageTemplates.failed(
                reason = state.message,
                onRetry = { engine.start(state.destination) },
                onDone = engine::stop,
            )
        }
    }

    private fun observeNavigation() {
        lifecycleScope.launch {
            engine.state.collect { state ->
                // Après un trajet, on retrouve le menu des destinations.
                if (state !is NavigationState.Idle) menuVisible = true
                navigation = state
                tripReporter.onStateChanged(state)
                refreshMap()
                invalidate()
            }
        }
    }

    private fun observeLocation() {
        lifecycleScope.launch {
            container.displayFixes.filterNotNull().collect { mapController?.showVehicle(it) }
        }
        // Compteur dessiné sur la carte : vitesse et limitation de la route.
        lifecycleScope.launch {
            combine(container.displayFixes, container.speedLimitRepository.limit) { fix, limit -> (fix?.speedKmh ?: 0) to limit }
                .collect { (speed, limit) -> mapSurface.showSpeed(speed, limit) }
        }
    }

    /** Le bouton Son / Muet change de titre : il faut redessiner le template. */
    /** Lieux enregistrés modifiés sur le téléphone, ou fin d'un trajet : le menu doit être à jour et de retour. */
    private fun observeSavedPlaces() {
        lifecycleScope.launch { container.savedPlacesRepository.saved.collect { invalidate() } }
    }

    private fun observeMute() {
        lifecycleScope.launch { audio.muted.collect { invalidate() } }
    }

    private fun observeRadars() {
        lifecycleScope.launch {
            container.radarAlerts.collect(radarAlerter::onAlert)
        }
    }

    /** Thème et perspective choisis dans les Paramètres du téléphone, appliqués aussi à la voiture. */
    private fun observeSettings() {
        lifecycleScope.launch { container.settingsRepository.settings.collect { refreshMap() } }
    }

    private fun refreshMap() {
        val controller = mapController ?: return
        val settings = container.settingsRepository.settings.value
        controller.applyTheme(settings.theme)
        controller.applyCameraConfig(CameraConfig.CAR.forPerspective(settings.perspective))
        controller.applyVehicle(settings.vehicle, settings.vehicleColor, settings.headlights)
        val preview = navigation as? NavigationState.Previewing
        val route = (navigation as? NavigationState.Navigating)?.route ?: preview?.route
        controller.showRoute(route)
        // Aperçu : les autres trajets en gris, et la carte cadrée sur l'ensemble. Sinon, retour derrière le véhicule.
        controller.showAlternatives(preview?.alternatives.orEmpty().filter { it !== preview?.route })
        if (preview?.alternatives !== framedRoutes) {
            framedRoutes = preview?.alternatives
            if (preview != null) controller.showOverview(preview.alternatives) else controller.recenter()
        }
        container.displayFixes.value?.let(controller::showVehicle)
    }

    private fun showMenu(visible: Boolean) {
        menuVisible = visible
        invalidate()
    }

    /** Maison ou Travail touché alors qu'il n'est pas défini : on indique où le faire. */
    private fun explainUndefinedPlace(label: String) {
        CarToast.makeText(carContext, "$label n'est pas défini : choisissez-le sur le téléphone, dans Paramètres > Lieux enregistrés", CarToast.LENGTH_LONG).show()
    }

    private fun openSearch() {
        screenManager.push(CarSearchScreen(carContext, container))
    }

    /** La demande s'affiche sur le téléphone ; la voiture attend la réponse. */
    private fun requestLocationPermission() {
        carContext.requestPermissions(LocationPermissions.REQUIRED.toList()) { _, _ ->
            hasLocationPermission = LocationPermissions.isGranted(carContext)
            if (hasLocationPermission) container.locationRepository.start()
            invalidate()
        }
    }

    private fun release() {
        tripReporter.release()
        mapSurface.release()
        carContext.getCarService(AppManager::class.java).setSurfaceCallback(null)
    }
}
