package com.gamemaps.irl.map.vehicle3d

import com.gamemaps.irl.core.geo.GeoMath
import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.map.vehicle3d.models.SportCar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VehicleGeometryTest {

    private val center = LatLng(44.84, -0.58)
    private val tenMetersAhead = GroundPoint(x = 0.0, z = 10.0)
    private val tenMetersRight = GroundPoint(x = 10.0, z = 0.0)

    @Test
    fun `cap nord - l'avant pointe au nord, la droite à l'est`() {
        val ahead = VehicleGeometry.toLatLng(tenMetersAhead, center, bearingDegrees = 0.0, scale = 1.0)
        val right = VehicleGeometry.toLatLng(tenMetersRight, center, bearingDegrees = 0.0, scale = 1.0)
        assertEquals(0.0, GeoMath.bearingDegrees(center, ahead), 0.5)
        assertEquals(90.0, GeoMath.bearingDegrees(center, right), 0.5)
        assertEquals(10.0, GeoMath.distanceMeters(center, ahead), 0.05)
    }

    @Test
    fun `cap est - l'avant pointe à l'est, la droite au sud`() {
        val ahead = VehicleGeometry.toLatLng(tenMetersAhead, center, bearingDegrees = 90.0, scale = 1.0)
        val right = VehicleGeometry.toLatLng(tenMetersRight, center, bearingDegrees = 90.0, scale = 1.0)
        assertEquals(90.0, GeoMath.bearingDegrees(center, ahead), 0.5)
        assertEquals(180.0, GeoMath.bearingDegrees(center, right), 0.5)
    }

    @Test
    fun `l'échelle agrandit distances et hauteurs`() {
        val ahead = VehicleGeometry.toLatLng(tenMetersAhead, center, bearingDegrees = 0.0, scale = 5.0)
        assertEquals(50.0, GeoMath.distanceMeters(center, ahead), 0.2)

        val parts = VehicleGeometry.place(SportCar.model, center, 0.0, scale = 5.0, color = VehicleColor.RED)
        assertEquals(SportCar.model.parts.size, parts.size)
        assertEquals(SportCar.model.parts[0].topMeters * 5, parts[0].topMeters, 1e-9)
        assertEquals(VehicleColor.RED.hex, parts[0].colorHex) // la carrosserie prend la couleur choisie
    }

    @Test
    fun `deux faisceaux devant une voiture, un seul pour une moto`() {
        val carBeams = VehicleGeometry.headlightBeams(SportCar.model, center, 0.0, 1.0)
        assertEquals(2, carBeams.size)
        assertTrue(carBeams.all { beam -> beam.all { it.lat > center.lat } }) // tout est devant (au nord)
        assertEquals(1, VehicleGeometry.headlightBeams(VehicleKind.MOTO.model, center, 0.0, 1.0).size)
    }

    @Test
    fun `le véhicule garde la même taille à l'écran quel que soit le zoom`() {
        val lengthDpAt = { zoom: Double ->
            SportCar.model.lengthMeters * VehicleScale.factor(zoom, center.lat) / VehicleScale.metersPerDp(zoom, center.lat)
        }
        assertEquals(lengthDpAt(17.0), lengthDpAt(12.0), 1e-6)
        assertEquals(61.0, lengthDpAt(17.0), 2.0) // ~64 dp pour une voiture de référence de 4,6 m
    }
}
