package com.gamemaps.irl.audio.announcers

import org.junit.Assert.assertEquals
import org.junit.Test

class SpeechDistanceFormatterTest {

    @Test
    fun `distances prononcées`() {
        assertEquals("50 mètres", SpeechDistanceFormatter.format(12.0))
        assertEquals("300 mètres", SpeechDistanceFormatter.format(310.0))
        assertEquals("1 kilomètre", SpeechDistanceFormatter.format(1_020.0))
        assertEquals("1,5 kilomètres", SpeechDistanceFormatter.format(1_480.0))
        assertEquals("12 kilomètres", SpeechDistanceFormatter.format(12_300.0))
    }
}
