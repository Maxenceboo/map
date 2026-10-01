package com.gamemaps.irl.core.geo

import com.gamemaps.irl.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PolylineProjectorTest {

    private val line = listOf(TestFixtures.START, TestFixtures.CORNER, TestFixtures.END)
    private val cumulative = PolylineProjector.cumulativeDistances(line)

    @Test
    fun `un point sur le tracé est à distance nulle`() {
        val projection = PolylineProjector.project(TestFixtures.CORNER, line, cumulative)!!
        assertEquals(0.0, projection.distanceToLineMeters, 0.5)
        assertEquals(cumulative[1], projection.distanceAlongMeters, 0.5)
    }

    @Test
    fun `un point décalé d'environ 50 m est projeté sur le bon segment`() {
        // Milieu du premier segment, décalé vers l'ouest (~50 m).
        val offset = LatLng((TestFixtures.START.lat + TestFixtures.CORNER.lat) / 2, -0.58063)
        val projection = PolylineProjector.project(offset, line, cumulative)!!
        assertEquals(0, projection.segmentIndex)
        assertEquals(50.0, projection.distanceToLineMeters, 3.0)
        assertEquals(cumulative[1] / 2, projection.distanceAlongMeters, 3.0)
    }

    @Test
    fun `un tracé d'un seul point ne peut pas être projeté`() {
        assertNull(PolylineProjector.project(TestFixtures.START, listOf(TestFixtures.START), doubleArrayOf(0.0)))
    }
}
