package com.gamemaps.irl.navigation

import com.gamemaps.irl.TestFixtures
import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.routing.ManeuverType
import org.junit.Assert.assertEquals
import org.junit.Test

class RouteProgressCalculatorTest {

    private val route = TestFixtures.lShapedRoute()
    private val calculator = RouteProgressCalculator()

    @Test
    fun `au départ, la prochaine manœuvre est le virage à droite`() {
        val progress = calculator.compute(route, TestFixtures.START)
        assertEquals(ManeuverType.RIGHT, progress.nextStep?.maneuver)
        assertEquals(route.cumulativeDistances[1], progress.distanceToNextStepMeters, 1.0)
        assertEquals(route.lengthMeters, progress.remainingDistanceMeters, 1.0)
        assertEquals(120.0, progress.remainingDurationSeconds, 0.5)
    }

    @Test
    fun `à mi-chemin du premier tronçon`() {
        val middle = LatLng((TestFixtures.START.lat + TestFixtures.CORNER.lat) / 2, TestFixtures.START.lng)
        val progress = calculator.compute(route, middle)
        assertEquals(ManeuverType.RIGHT, progress.nextStep?.maneuver)
        assertEquals(route.cumulativeDistances[1] / 2, progress.distanceToNextStepMeters, 2.0)
        assertEquals(0.0, progress.distanceFromRouteMeters, 1.0)
    }

    @Test
    fun `après le virage, la prochaine étape est l'arrivée`() {
        val afterCorner = LatLng(TestFixtures.CORNER.lat, -0.5750)
        val progress = calculator.compute(route, afterCorner)
        assertEquals(ManeuverType.ARRIVE, progress.nextStep?.maneuver)
    }
}
