package com.gamemaps.irl.data.speedlimit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MaxSpeedParserTest {

    @Test
    fun `valeurs numériques`() {
        assertEquals(50, MaxSpeedParser.parse("50"))
        assertEquals(30, MaxSpeedParser.parse(" 30 "))
        assertEquals(48, MaxSpeedParser.parse("30 mph"))
    }

    @Test
    fun `valeurs implicites françaises`() {
        assertEquals(50, MaxSpeedParser.parse("FR:urban"))
        assertEquals(80, MaxSpeedParser.parse("FR:rural"))
        assertEquals(130, MaxSpeedParser.parse("FR:motorway"))
        assertEquals(30, MaxSpeedParser.parse("FR:zone30"))
    }

    @Test
    fun `valeurs inexploitables`() {
        assertNull(MaxSpeedParser.parse("none"))
        assertNull(MaxSpeedParser.parse("signals"))
        assertNull(MaxSpeedParser.parse(""))
    }
}
