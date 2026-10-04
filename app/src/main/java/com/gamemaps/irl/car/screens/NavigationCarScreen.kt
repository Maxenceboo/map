package com.gamemaps.irl.car.screens

import androidx.car.app.AppManager
import androidx.car.app.CarContext
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
import com.gamemaps.irl.map.theme.MapTheme
import com.gamemaps.irl.navigation.NavigationState
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

    private val tripReporter = CarTripReporter(carContext, onStopRequested = engine::stop)
    private val radarAlerter = CarRadarAlerter(carContext)
    private val mapSurface = CarMapSurface(carContext, MapTheme.GTA_RADAR) { controller ->
        mapController = controller
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
    }

    override fun onGetTemplate(): Template {
        if (!hasLocationPermission) return MessageTemplates.permissionRequired(::requestLocationPermission)
        return when (val state = navigation) {
            is NavigationState.Idle -> IdleTemplate.build(
                onSearch = ::openSearch,
                isMuted = audio.muted.value,
                onToggleMute = audio::toggleMuted,
            )
            is NavigationState.Calculating -> CalculatingTemplate.build(onStop = engine::stop)
            is NavigationState.Previewing -> PreviewTemplate.build(state, onStart = engine::confirm, onCancel = engine::stop)
            is NavigationState.Navigating -> NavigatingTemplate.build(
                state = state,
                isMuted = audio.muted.value,
                onToggleMute = audio::toggleMuted,
                onStop = engine::stop,
            )
            is NavigationState.Arrived -> MessageTemplates.arrived(state.destination.name, onDone = engine::stop)
            is NavigationState.Failed -> MessageTemplates.failed(
                reason = state.message,
                onRetry = { engine.start(state.destination, autoStart = true) },
                onDone = engine::stop,
            )
        }
    }

    private fun observeNavigation() {
        lifecycleScope.launch {
            engine.state.collect { state ->
                navigation = state
                tripReporter.onStateChanged(state)
                refreshMap()
                invalidate()
            }
        }
    }

    private fun observeLocation() {
        lifecycleScope.launch {
            container.locationRepository.fixes.filterNotNull().collect { mapController?.showVehicle(it) }
        }
    }

    /** Le bouton Son / Muet change de titre : il faut redessiner le template. */
    private fun observeMute() {
        lifecycleScope.launch { audio.muted.collect { invalidate() } }
    }

    private fun observeRadars() {
        lifecycleScope.launch {
            container.radarRepository.radars.collect { mapController?.showRadars(it) }
        }
        lifecycleScope.launch {
            container.radarRepository.alert.collect(radarAlerter::onAlert)
        }
    }

    private fun refreshMap() {
        val controller = mapController ?: return
        val route = (navigation as? NavigationState.Navigating)?.route ?: (navigation as? NavigationState.Previewing)?.route
        controller.showRoute(route)
        controller.showRadars(container.radarRepository.radars.value)
        container.locationRepository.fixes.value?.let(controller::showVehicle)
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
