package com.gamemaps.irl.data.speedlimit

import com.gamemaps.irl.core.geo.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Un carrefour : une avenue nord-sud à 50 et une rue est-ouest à 30. */
class SpeedLimitIndexTest {

    private val crossing = LatLng(44.8600, -0.5600)
    private val avenueNorthSouth = RoadSegment(
        wayId = 1,
        maxSpeedKmh = 50,
        points = listOf(LatLng(44.8550, -0.5600), LatLng(44.8650, -0.5600)),
    )
    private val streetEastWest = RoadSegment(
        wayId = 2,
        maxSpeedKmh = 30,
        points = listOf(LatLng(44.8600, -0.5650), LatLng(44.8600, -0.5550)),
    )
    private val index = SpeedLimitIndex(listOf(avenueNorthSouth, streetEastWest))

    @Test
    fun `sur l'avenue loin du carrefour`() {
        assertEquals(50, index.limitAt(LatLng(44.8580, -0.5600), bearingDegrees = 0f))
    }

    @Test
    fun `au carrefour, le cap départage les deux routes`() {
        assertEquals(50, index.limitAt(crossing, bearingDegrees = 2f))
        assertEquals(30, index.limitAt(crossing, bearingDegrees = 88f))
        assertEquals(30, index.limitAt(crossing, bearingDegrees = 271f)) // vers l'ouest : même rue
    }

    @Test
    fun `loin de toute route`() {
        assertNull(index.limitAt(LatLng(44.8700, -0.5500), bearingDegrees = 0f))
    }
}
