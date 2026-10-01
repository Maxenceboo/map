package com.gamemaps.irl.data.routing.osrm

import com.gamemaps.irl.data.routing.ManeuverType
import org.junit.Assert.assertEquals
import org.junit.Test

class OsrmManeuverMapperTest {

    @Test
    fun `types spéciaux`() {
        assertEquals(ManeuverType.DEPART, OsrmManeuverMapper.map("depart", "right"))
        assertEquals(ManeuverType.ARRIVE, OsrmManeuverMapper.map("arrive", null))
        assertEquals(ManeuverType.ROUNDABOUT, OsrmManeuverMapper.map("roundabout", "right"))
        assertEquals(ManeuverType.ROUNDABOUT, OsrmManeuverMapper.map("rotary", null))
    }

    @Test
    fun `bretelles et embranchements selon le côté`() {
        assertEquals(ManeuverType.RAMP_LEFT, OsrmManeuverMapper.map("off ramp", "slight left"))
        assertEquals(ManeuverType.RAMP_RIGHT, OsrmManeuverMapper.map("on ramp", "right"))
        assertEquals(ManeuverType.FORK_LEFT, OsrmManeuverMapper.map("fork", "left"))
    }

    @Test
    fun `virages selon le modificateur`() {
        assertEquals(ManeuverType.LEFT, OsrmManeuverMapper.map("turn", "left"))
        assertEquals(ManeuverType.SHARP_RIGHT, OsrmManeuverMapper.map("turn", "sharp right"))
        assertEquals(ManeuverType.UTURN, OsrmManeuverMapper.map("continue", "uturn"))
        assertEquals(ManeuverType.STRAIGHT, OsrmManeuverMapper.map("new name", null))
    }
}
