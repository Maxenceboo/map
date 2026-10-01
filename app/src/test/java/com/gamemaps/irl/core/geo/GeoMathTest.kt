package com.gamemaps.irl.core.geo

import org.junit.Assert.assertEquals
import org.junit.Test

class GeoMathTest {

    @Test
    fun `distance Bordeaux - Paris est d'environ 500 km`() {
        val bordeaux = LatLng(44.8378, -0.5792)
        val paris = LatLng(48.8566, 2.3522)
        assertEquals(499_000.0, GeoMath.distanceMeters(bordeaux, paris), 5_000.0)
    }

    @Test
    fun `distance d'un point a lui-meme est nulle`() {
        val p = LatLng(44.0, -0.5)
        assertEquals(0.0, GeoMath.distanceMeters(p, p), 1e-9)
    }

    @Test
    fun `cap vers le nord vaut 0 et vers l'est vaut 90`() {
        val origin = LatLng(0.0, 0.0)
        assertEquals(0.0, GeoMath.bearingDegrees(origin, LatLng(1.0, 0.0)), 1e-6)
        assertEquals(90.0, GeoMath.bearingDegrees(origin, LatLng(0.0, 1.0)), 1e-6)
        assertEquals(270.0, GeoMath.bearingDegrees(origin, LatLng(0.0, -1.0)), 1e-6)
    }
}
