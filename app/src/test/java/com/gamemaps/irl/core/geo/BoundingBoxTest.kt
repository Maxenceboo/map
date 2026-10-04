package com.gamemaps.irl.core.geo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BoundingBoxTest {

    private val center = LatLng(44.86, -0.555)

    @Test
    fun `un carré de 600 m fait bien 1200 m de côté`() {
        val box = BoundingBox.around(center, 600.0)
        assertEquals(1_200.0, GeoMath.distanceMeters(LatLng(box.south, center.lng), LatLng(box.north, center.lng)), 5.0)
        assertEquals(1_200.0, GeoMath.distanceMeters(LatLng(center.lat, box.west), LatLng(center.lat, box.east)), 5.0)
    }

    @Test
    fun `contient le centre mais pas un point à 1 km`() {
        val box = BoundingBox.around(center, 600.0)
        assertTrue(box.contains(center))
        assertFalse(box.contains(LatLng(center.lat + 0.009, center.lng))) // ~1 km au nord
    }

    @Test
    fun `rétrécir exclut les bords`() {
        val box = BoundingBox.around(center, 600.0)
        val nearEdge = LatLng(center.lat + 0.0050, center.lng) // ~555 m au nord
        assertTrue(box.contains(nearEdge))
        assertFalse(box.shrink(150.0).contains(nearEdge))
    }
}
