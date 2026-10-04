package com.gamemaps.irl.data.routing

import com.gamemaps.irl.core.geo.LatLng
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Test
import java.io.IOException

class TrafficAwareRoutingServiceTest {

    private val from = LatLng(44.84, -0.58)
    private val to = LatLng(44.85, -0.57)
    private val trafficRoute = Route(listOf(from, to), emptyList(), 100.0)
    private val plainRoute = Route(listOf(from, to), emptyList(), 80.0)

    private val withTraffic = RoutingService { _, _ -> trafficRoute }
    private val failing = RoutingService { _, _ -> throw IOException("HTTP 403") }
    private val fallback = RoutingService { _, _ -> plainRoute }

    @Test
    fun `trafic activé - itinéraire avec trafic`() = runTest {
        assertSame(trafficRoute, TrafficAwareRoutingService(withTraffic, fallback) { true }.route(from, to))
    }

    @Test
    fun `trafic désactivé dans les paramètres - moteur de secours`() = runTest {
        assertSame(plainRoute, TrafficAwareRoutingService(withTraffic, fallback) { false }.route(from, to))
    }

    @Test
    fun `pas de clé - moteur de secours`() = runTest {
        assertSame(plainRoute, TrafficAwareRoutingService(null, fallback) { true }.route(from, to))
    }

    @Test
    fun `le moteur avec trafic échoue - on retombe sur le moteur de secours`() = runTest {
        assertSame(plainRoute, TrafficAwareRoutingService(failing, fallback) { true }.route(from, to))
    }
}
