package com.gamemaps.irl.navigation

import com.gamemaps.irl.TestFixtures
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
        assertEquals(NavigationState.Arrived(TestFixtures.PLACE), engine.state.value)

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
}
