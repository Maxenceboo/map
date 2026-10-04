package com.gamemaps.irl.car.intent

import com.gamemaps.irl.core.geo.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NavigationRequestParserTest {

    @Test
    fun `un point`() {
        assertEquals(NavigationRequest.ToPosition(LatLng(44.84, -0.58), null), NavigationRequestParser.parse("geo:44.84,-0.58"))
    }

    @Test
    fun `un point avec un nom dans q`() {
        assertEquals(
            NavigationRequest.ToPosition(LatLng(44.84, -0.58), "Maison"),
            NavigationRequestParser.parse("geo:0,0?q=44.84,-0.58(Maison)"),
        )
    }

    @Test
    fun `un texte à chercher`() {
        assertEquals(NavigationRequest.ToQuery("gare d'Arcachon"), NavigationRequestParser.parse("geo:0,0?q=gare+d%27Arcachon"))
        assertEquals(NavigationRequest.ToQuery("12 rue Sainte-Catherine Bordeaux"), NavigationRequestParser.parse("google.navigation:q=12+rue+Sainte-Catherine+Bordeaux&mode=d"))
    }

    @Test
    fun `adresses sans destination`() {
        assertNull(NavigationRequestParser.parse(null))
        assertNull(NavigationRequestParser.parse("geo:0,0"))
        assertNull(NavigationRequestParser.parse("geo:0,0?q="))
        assertNull(NavigationRequestParser.parse("geo:120.0,500.0"))
        assertNull(NavigationRequestParser.parse("https://example.com/?q=test"))
    }
}
