package com.gamemaps.irl.navigation

import com.gamemaps.irl.TestFixtures
import com.gamemaps.irl.core.geo.GeoMath
import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.data.location.LocationRepository
import com.gamemaps.irl.data.location.LocationSource
import com.gamemaps.irl.data.routing.ManeuverType
import com.gamemaps.irl.data.routing.RoutingException
import com.gamemaps.irl.data.routing.RoutingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NavigationEngineTest {

    private val gps = MutableSharedFlow<GpsFix>()

    private fun engine(scope: CoroutineScope, routing: RoutingService): NavigationEngine {
        val location = LocationRepository(LocationSource { gps }, scope).apply { start() }
        return NavigationEngine(scope, location, routing)
    }

    @Test
    fun `cycle complet - calcul, guidage, arrivée`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val engine = engine(scope) { _, _ -> TestFixtures.lShapedRoute() }

        engine.start(TestFixtures.PLACE)
        assertTrue(engine.state.value is NavigationState.Calculating)

        gps.emit(TestFixtures.fix(TestFixtures.START))
        assertTrue(engine.state.value is NavigationState.Previewing)

        engine.confirm()
        assertTrue(engine.state.value is NavigationState.Navigating)

        gps.emit(TestFixtures.fix(TestFixtures.END))
        val arrived = engine.state.value as NavigationState.Arrived
        assertEquals(TestFixtures.PLACE, arrived.destination)
        // Deux positions GPS : départ puis arrivée, soit la diagonale du "L".
        assertEquals(GeoMath.distanceMeters(TestFixtures.START, TestFixtures.END), arrived.stats.distanceMeters, 1.0)

        engine.stop()
        assertEquals(NavigationState.Idle, engine.state.value)
        scope.cancel()
    }

    @Test
    fun `échec du calcul`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val engine = engine(scope) { _, _ -> throw RoutingException("NoRoute") }

        engine.start(TestFixtures.PLACE)
        gps.emit(TestFixtures.fix(TestFixtures.START))

        assertEquals(NavigationState.Failed(TestFixtures.PLACE, "NoRoute"), engine.state.value)
        scope.cancel()
    }

    @Test
    fun `sortie d'itinéraire prolongée déclenche un recalcul`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        var routeRequests = 0
        val engine = engine(scope) { _, _ ->
            routeRequests++
            TestFixtures.lShapedRoute()
        }
        val farAway = LatLng(TestFixtures.START.lat, -0.5900) // ~800 m à l'ouest du tracé

        engine.start(TestFixtures.PLACE, autoStart = true)
        gps.emit(TestFixtures.fix(TestFixtures.START, timeMillis = 0))
        gps.emit(TestFixtures.fix(farAway, timeMillis = 1_000))
        gps.emit(TestFixtures.fix(farAway, timeMillis = 4_500))

        assertEquals(2, routeRequests)
        scope.cancel()
    }

    @Test
    fun `pendant l'aperçu, rouler ne déclenche ni arrivée ni recalcul`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        var routeRequests = 0
        val engine = engine(scope) { _, _ ->
            routeRequests++
            TestFixtures.lShapedRoute()
        }

        engine.start(TestFixtures.PLACE)
        gps.emit(TestFixtures.fix(TestFixtures.START, timeMillis = 0))
        gps.emit(TestFixtures.fix(LatLng(TestFixtures.START.lat, -0.5900), timeMillis = 10_000))
        gps.emit(TestFixtures.fix(TestFixtures.END, timeMillis = 20_000))

        assertTrue(engine.state.value is NavigationState.Previewing)
        assertEquals(1, routeRequests)
        scope.cancel()
    }

    @Test
    fun `le départ part de la position actuelle`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val engine = engine(scope) { _, _ -> TestFixtures.lShapedRoute() }

        engine.start(TestFixtures.PLACE)
        gps.emit(TestFixtures.fix(TestFixtures.START))
        gps.emit(TestFixtures.fix(TestFixtures.CORNER)) // on a avancé pendant l'aperçu
        engine.confirm()

        val navigating = engine.state.value as NavigationState.Navigating
        assertEquals(ManeuverType.ARRIVE, navigating.progress.nextStep?.maneuver)
        scope.cancel()
    }

    @Test
    fun `avec le trafic, l'itinéraire est rafraîchi en silence pendant le trajet`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        var now = 0L
        var calls = 0
        val location = LocationRepository(LocationSource { gps }, scope).apply { start() }
        val routing = RoutingService { _, _ ->
            calls++
            TestFixtures.lShapedRoute().copy(hasLiveTraffic = true)
        }
        val engine = NavigationEngine(scope, location, routing, clock = { now }, trafficRefreshMillis = 300_000L)

        engine.start(TestFixtures.PLACE)
        gps.emit(TestFixtures.fix(TestFixtures.START))
        engine.confirm()
        assertEquals(1, calls)

        // Trop tôt : pas de nouveau calcul.
        now = 120_000L
        gps.emit(TestFixtures.fix(TestFixtures.START, timeMillis = now))
        assertEquals(1, calls)

        // Cinq minutes après le premier calcul : un rafraîchissement, sans passer par "recalcul".
        now = 301_000L
        gps.emit(TestFixtures.fix(TestFixtures.START, timeMillis = now))
        assertEquals(2, calls)
        val state = engine.state.value as NavigationState.Navigating
        assertTrue(!state.isRerouting)
        scope.cancel()
    }

    @Test
    fun `sans trafic, pas de rafraîchissement`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        var now = 0L
        var calls = 0
        val location = LocationRepository(LocationSource { gps }, scope).apply { start() }
        val routing = RoutingService { _, _ ->
            calls++
            TestFixtures.lShapedRoute()
        }
        val engine = NavigationEngine(scope, location, routing, clock = { now }, trafficRefreshMillis = 300_000L)

        engine.start(TestFixtures.PLACE)
        gps.emit(TestFixtures.fix(TestFixtures.START))
        engine.confirm()
        now = 900_000L
        gps.emit(TestFixtures.fix(TestFixtures.START, timeMillis = now))
        assertEquals(1, calls)
        scope.cancel()
    }
}
