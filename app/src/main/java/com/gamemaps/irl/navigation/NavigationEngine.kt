package com.gamemaps.irl.navigation

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.data.location.LocationRepository
import com.gamemaps.irl.data.routing.RoutingService
import com.gamemaps.irl.data.search.Place
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Cerveau du guidage, unique pour toute l'application.
 *
 * - [start] : calcule l'itinéraire depuis la position actuelle puis suit le conducteur.
 * - À chaque position GPS : met à jour la progression, détecte l'arrivée et la sortie de route.
 * - [stop] : arrête tout et revient à [NavigationState.Idle].
 *
 * Le téléphone et Android Auto observent le même [state].
 */
class NavigationEngine(
    private val scope: CoroutineScope,
    private val location: LocationRepository,
    private val routing: RoutingService,
    private val progressCalculator: RouteProgressCalculator = RouteProgressCalculator(),
    private val offRouteDetector: OffRouteDetector = OffRouteDetector(),
    private val arrivalDetector: ArrivalDetector = ArrivalDetector(),
) {
    private val _state = MutableStateFlow<NavigationState>(NavigationState.Idle)
    val state: StateFlow<NavigationState> = _state.asStateFlow()

    private var routeJob: Job? = null
    private var trackingJob: Job? = null

    fun start(destination: Place) {
        stop()
        _state.value = NavigationState.Calculating(destination)
        routeJob = scope.launch {
            val origin = location.fixes.filterNotNull().first().position
            computeRoute(destination, origin)
        }
        trackingJob = scope.launch {
            location.fixes.filterNotNull().collect(::onFix)
        }
    }

    fun stop() {
        routeJob?.cancel()
        trackingJob?.cancel()
        offRouteDetector.reset()
        _state.value = NavigationState.Idle
    }

    private suspend fun computeRoute(destination: Place, origin: LatLng) {
        try {
            val route = routing.route(origin, destination.position)
            offRouteDetector.reset()
            _state.value = NavigationState.Navigating(destination, route, progressCalculator.compute(route, origin))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val current = _state.value
            _state.value = if (current is NavigationState.Navigating) {
                // Échec d'un recalcul : on garde l'ancien itinéraire plutôt que de tout perdre.
                current.copy(isRerouting = false)
            } else {
                NavigationState.Failed(destination, e.message ?: "Itinéraire introuvable")
            }
        }
    }

    private fun onFix(fix: GpsFix) {
        val current = _state.value as? NavigationState.Navigating ?: return
        val progress = progressCalculator.compute(current.route, fix.position)

        if (arrivalDetector.hasArrived(progress)) {
            _state.value = NavigationState.Arrived(current.destination)
            return
        }
        _state.update { (it as? NavigationState.Navigating)?.copy(progress = progress) ?: it }

        if (!current.isRerouting && offRouteDetector.update(progress.distanceFromRouteMeters, fix.timeMillis)) {
            reroute(current.destination, fix.position)
        }
    }

    private fun reroute(destination: Place, origin: LatLng) {
        _state.update { (it as? NavigationState.Navigating)?.copy(isRerouting = true) ?: it }
        routeJob?.cancel()
        routeJob = scope.launch { computeRoute(destination, origin) }
    }
}
