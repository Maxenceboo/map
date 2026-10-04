package com.gamemaps.irl.map.camera

import com.gamemaps.irl.data.settings.Perspective
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraConfigTest {

    @Test
    fun `la 3D garde la configuration d'origine`() {
        assertEquals(CameraConfig.PHONE, CameraConfig.PHONE.forPerspective(Perspective.COCKPIT_3D))
    }

    @Test
    fun `la 2D est à plat, un peu plus haute, véhicule moins bas`() {
        val topDown = CameraConfig.PHONE.forPerspective(Perspective.TOP_DOWN_2D)
        assertEquals(0.0, topDown.tilt, 0.0)
        assertTrue(topDown.zoom < CameraConfig.PHONE.zoom)
        assertTrue(topDown.topPaddingRatio < CameraConfig.PHONE.topPaddingRatio)
    }
}
