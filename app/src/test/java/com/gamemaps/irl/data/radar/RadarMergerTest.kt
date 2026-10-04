package com.gamemaps.irl.data.radar

import com.gamemaps.irl.core.geo.LatLng
import org.junit.Assert.assertEquals
import org.junit.Test

class RadarMergerTest {

    private val official = Radar("fr_1", LatLng(44.8600, -0.5600), RadarType.RED_LIGHT, maxSpeedKmh = 50)

    @Test
    fun `un radar OSM au même endroit est un doublon`() {
        val duplicate = Radar("osm_1", LatLng(44.8603, -0.5600), RadarType.SPEED, maxSpeedKmh = null) // ~33 m
        assertEquals(listOf(official), RadarMerger.merge(listOf(official), listOf(duplicate)))
    }

    @Test
    fun `un radar OSM ailleurs complète la base officielle`() {
        val other = Radar("osm_2", LatLng(44.8700, -0.5600), RadarType.SPEED, maxSpeedKmh = 90) // ~1,1 km
        assertEquals(listOf(official, other), RadarMerger.merge(listOf(official), listOf(other)))
    }
}
