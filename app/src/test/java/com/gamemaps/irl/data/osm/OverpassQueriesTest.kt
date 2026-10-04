package com.gamemaps.irl.data.osm

import com.gamemaps.irl.core.geo.BoundingBox
import org.junit.Assert.assertEquals
import org.junit.Test

class OverpassQueriesTest {

    private val box = BoundingBox(south = 44.0, west = -1.0, north = 45.0, east = 0.5)

    @Test
    fun `la bbox est dans l'ordre sud, ouest, nord, est`() {
        assertEquals(
            "[out:json][timeout:15];node(44.0,-1.0,45.0,0.5)[highway=speed_camera];out;",
            OverpassQueries.speedCameras(box),
        )
        assertEquals(
            "[out:json][timeout:15];way(44.0,-1.0,45.0,0.5)[highway][maxspeed];out tags geom;",
            OverpassQueries.roadsWithMaxSpeed(box),
        )
    }
}
