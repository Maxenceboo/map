package com.gamemaps.irl.navigation

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.data.location.LocationRepository
import com.gamemaps.irl.data.routing.RoutingService
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.navigation.trip.TripRecorder
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
 * - [start] : calcule l'itinéraire depuis la position actuelle, puis l'affiche en aperçu
 *   (ou démarre directement si `autoStart`, utilisé par Android Auto).
 * - [selectRoute] : dans l'aperçu, le conducteur choisit un des trajets proposés.
 * - [confirm] : le conducteur valide l'aperçu, le guidage commence.
 * - À chaque position GPS : met à jour la progression, détecte l'arrivée et la sortie de route.
 * - Avec un itinéraire qui tient compte du trafic : il est recalculé en silence toutes les
 *   [trafficRefreshMillis], pour suivre l'évolution des bouchons.
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
    private val clock: () -> Long = System::currentTimeMillis,
    private val trafficRefreshMillis: Long = 5 * 60_000L,
) {
    private val _state = MutableStateFlow<NavigationState>(NavigationState.Idle)
    val state: StateFlow<NavigationState> = _state.asStateFlow()

    private var routeJob: Job? = null
    private var trackingJob: Job? = null
    private var refreshJob: Job? = null

    /** Heure du dernier calcul d'itinéraire (réussi ou non), pour espacer les rafraîchissements du trafic. */
    private var routeComputedAt = 0L
    private val tripRecorder = TripRecorder()

    fun start(destination: Place, autoStart: Boolean = false) {
        stop()
        _state.value = NavigationState.Calculating(destination)
        routeJob = scope.launch {
            val origin = location.fixes.filterNotNull().first().position
            computeRoute(destination, origin, autoStart)
        }
        trackingJob = scope.launch {
            location.fixes.filterNotNull().collect(::onFix)
        }
    }

    /** Dans l'aperçu, choisit un autre trajet parmi ceux proposés. */
    fun selectRoute(index: Int) {
        _state.update { state ->
            val preview = state as? NavigationState.Previewing ?: return@update state
            preview.alternatives.getOrNull(index)?.let { preview.copy(route = it) } ?: state
        }
    }

    /** Passe de l'aperçu au guidage, en partant de la position actuelle. */
    fun confirm() {
        val preview = _state.value as? NavigationState.Previewing ?: return
        val position = location.fixes.value?.position ?: preview.route.geometry.first()
        offRouteDetector.reset()
        tripRecorder.start(clock(), position)
        _state.value = NavigationState.Navigating(preview.destination, preview.route, progressCalculator.compute(preview.route, position))
    }

    fun stop() {
        routeJob?.cancel()
        trackingJob?.cancel()
        refreshJob?.cancel()
        offRouteDetector.reset()
        _state.value = NavigationState.Idle
    }

    private suspend fun computeRoute(destination: Place, origin: LatLng, autoStart: Boolean) {
        try {
            // Avant un départ depuis l'aperçu, on demande plusieurs trajets pour laisser le choix.
            val forPreview = !autoStart && _state.value !is NavigationState.Navigating
            val routes = if (forPreview) routing.alternatives(origin, destination.position) else listOf(routing.route(origin, destination.position))
            val route = routes.first()
            routeComputedAt = clock()
            offRouteDetector.reset()
            // Un recalcul en cours de route repart directement en guidage, sans nouvel aperçu.
            val isReroute = _state.value is NavigationState.Navigating
            if (autoStart && !isReroute) tripRecorder.start(clock(), origin)
            _state.value = if (autoStart || isReroute) {
                NavigationState.Navigating(destination, route, progressCalculator.compute(route, origin))
            } else {
                NavigationState.Previewing(destination, route, routes)
            }
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
        tripRecorder.onPosition(fix.position, fix.timeMillis)

        if (arrivalDetector.hasArrived(progress)) {
            _state.value = NavigationState.Arrived(current.destination, tripRecorder.finish(clock()))
            return
        }
        _state.update { (it as? NavigationState.Navigating)?.copy(progress = progress) ?: it }

        if (!current.isRerouting && offRouteDetector.update(progress.distanceFromRouteMeters, fix.timeMillis, fix.speedMetersPerSecond)) {
            reroute(current.destination, fix.position)
        } else if (shouldRefreshTraffic(current)) {
            refreshTraffic(current.destination, fix.position)
        }
    }

    private fun shouldRefreshTraffic(current: NavigationState.Navigating): Boolean =
        current.route.hasLiveTraffic && !current.isRerouting && refreshJob?.isActive != true &&
            clock() - routeComputedAt >= trafficRefreshMillis

    /**
     * Recalcule l'itinéraire depuis la position actuelle sans rien afficher ni annoncer.
     * En cas d'échec (réseau), on garde l'itinéraire en cours et on réessaiera plus tard.
     */
    private fun refreshTraffic(destination: Place, origin: LatLng) {
        routeComputedAt = clock()
        refreshJob = scope.launch {
            val fresh = try {
                routing.route(origin, destination.position)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                return@launch
            }
            // Pendant le calcul, le conducteur a pu arriver, s'arrêter ou sortir de la route : on ne touche alors à rien.
            _state.update { state ->
                val navigating = state as? NavigationState.Navigating
                if (navigating == null || navigating.isRerouting || navigating.destination != destination) return@update state
                val position = location.fixes.value?.position ?: origin
                navigating.copy(route = fresh, progress = progressCalculator.compute(fresh, position))
            }
        }
    }

    private fun reroute(destination: Place, origin: LatLng) {
        _state.update { (it as? NavigationState.Navigating)?.copy(isRerouting = true) ?: it }
        routeJob?.cancel()
        routeJob = scope.launch { computeRoute(destination, origin, autoStart = true) }
    }
}
