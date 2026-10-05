package com.gamemaps.irl.navigation.snap

import com.gamemaps.irl.TestFixtures
import com.gamemaps.irl.core.geo.GeoMath
import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.speedlimit.RoadSegment
import com.gamemaps.irl.data.speedlimit.SpeedLimitIndex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoadSnapperTest {

    private val route = TestFixtures.lShapedRoute() // d'abord plein nord depuis START

    /** ~10 m à l'est du premier tronçon, 300 m après le départ. */
    private val besideRoute = LatLng(TestFixtures.START.lat + 0.0027, TestFixtures.START.lng + 0.000127)

    private fun driving(position: LatLng, bearing: Float = 10f) =
        TestFixtures.fix(position).copy(speedMetersPerSecond = 10f, bearingDegrees = bearing)

    private val noRoad = { _: LatLng, _: Float? -> null }

    @Test
    fun `pendant le guidage, le véhicule est ramené sur l'itinéraire et orienté dans son axe`() {
        val snapped = RoadSnapper().snap(driving(besideRoute), route, noRoad)
        assertEquals(TestFixtures.START.lng, snapped.position.lng, 1e-6)
        assertEquals(besideRoute.lat, snapped.position.lat, 1e-5)
        assertEquals(0f, snapped.bearingDegrees!!, 0.5f)
    }

    @Test
    fun `trop loin de l'itinéraire, on garde la position du GPS`() {
        val far = LatLng(besideRoute.lat, TestFixtures.START.lng + 0.002) // ~160 m à l'est
        assertEquals(far, RoadSnapper().snap(driving(far), route, noRoad).position)
    }

    @Test
    fun `sans guidage, on colle à la route la plus proche, dans notre sens de marche`() {
        val road = RoadSegment(1, 50, listOf(LatLng(44.86, -0.56), LatLng(44.85, -0.56))) // tracée du nord vers le sud
        val index = SpeedLimitIndex(listOf(road))
        val beside = LatLng(44.855, -0.5601)
        val snapped = RoadSnapper().snap(driving(beside, bearing = 5f), null, index::snapAt)
        assertEquals(-0.56, snapped.position.lng, 1e-6)
        assertEquals(0f, snapped.bearingDegrees!!, 0.5f) // vers le nord, comme nous, et non le sens du tracé
    }

    @Test
    fun `à l'arrêt après avoir roulé, on reste sur la route - garé à l'écart, on n'y est pas tiré`() {
        val snapper = RoadSnapper()
        snapper.snap(driving(besideRoute), route, noRoad)
        val stopped = TestFixtures.fix(besideRoute).copy(speedMetersPerSecond = 0f, bearingDegrees = 10f)
        assertTrue(GeoMath.distanceMeters(besideRoute, snapper.snap(stopped, route, noRoad).position) > 5)

        // Un autre véhicule, à l'arrêt depuis le lancement : pas d'aimantation.
        assertEquals(besideRoute, RoadSnapper().snap(stopped, route, noRoad).position)
    }
}
