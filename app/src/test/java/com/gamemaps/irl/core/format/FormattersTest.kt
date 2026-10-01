package com.gamemaps.irl.core.format

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class FormattersTest {

    @Test
    fun `distances`() {
        assertEquals("250 m", DistanceFormatter.format(248.0))
        assertEquals("0 m", DistanceFormatter.format(2.0))
        assertEquals("1,2 km", DistanceFormatter.format(1_234.0))
        assertEquals("14 km", DistanceFormatter.format(14_200.0))
    }

    @Test
    fun `durées`() {
        assertEquals("1 min", DurationFormatter.format(10.0))
        assertEquals("8 min", DurationFormatter.format(480.0))
        assertEquals("1 h 05", DurationFormatter.format(3_900.0))
    }

    @Test
    fun `heure d'arrivée`() {
        val now = LocalDateTime.of(2026, 10, 1, 18, 30).toInstant(ZoneOffset.UTC).toEpochMilli()
        assertEquals("18:42", ArrivalTimeFormatter.format(now, 12 * 60.0, ZoneOffset.UTC))
    }
}
