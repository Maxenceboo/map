package com.gamemaps.irl.data.routing.tomtom

import com.gamemaps.irl.data.routing.ManeuverType
import org.junit.Assert.assertEquals
import org.junit.Test

class TomTomManeuverMapperTest {

    @Test
    fun `virages et demi-tour`() {
        assertEquals(ManeuverType.LEFT, TomTomManeuverMapper.map("TURN_LEFT"))
        assertEquals(ManeuverType.SLIGHT_RIGHT, TomTomManeuverMapper.map("BEAR_RIGHT"))
        assertEquals(ManeuverType.SHARP_LEFT, TomTomManeuverMapper.map("SHARP_LEFT"))
        assertEquals(ManeuverType.UTURN, TomTomManeuverMapper.map("MAKE_UTURN"))
    }

    @Test
    fun `départ, arrivée, rond-point, autoroute`() {
        assertEquals(ManeuverType.DEPART, TomTomManeuverMapper.map("DEPART"))
        assertEquals(ManeuverType.ARRIVE, TomTomManeuverMapper.map("ARRIVE_LEFT"))
        assertEquals(ManeuverType.ROUNDABOUT, TomTomManeuverMapper.map("ROUNDABOUT_CROSS"))
        assertEquals(ManeuverType.MERGE, TomTomManeuverMapper.map("ENTER_MOTORWAY"))
        assertEquals(ManeuverType.RAMP_RIGHT, TomTomManeuverMapper.map("TAKE_EXIT"))
        assertEquals(ManeuverType.FORK_LEFT, TomTomManeuverMapper.map("KEEP_LEFT"))
    }

    @Test
    fun `code inconnu - on continue tout droit`() {
        assertEquals(ManeuverType.STRAIGHT, TomTomManeuverMapper.map("FOLLOW"))
        assertEquals(ManeuverType.STRAIGHT, TomTomManeuverMapper.map(""))
    }
}
