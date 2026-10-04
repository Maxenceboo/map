package com.gamemaps.irl.data.search.ban

import com.gamemaps.irl.data.search.PlaceKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BanResponseParserTest {

    @Test
    fun `lit nom, sous-titre et coordonnées`() {
        val json = """
            { "type": "FeatureCollection", "features": [{
                "type": "Feature",
                "geometry": { "type": "Point", "coordinates": [-0.5736, 44.8412] },
                "properties": { "id": "33063_1234_00012", "label": "12 Rue Sainte-Catherine 33000 Bordeaux", "type": "housenumber",
                  "name": "12 Rue Sainte-Catherine", "postcode": "33000", "city": "Bordeaux",
                  "context": "33, Gironde, Nouvelle-Aquitaine" }
            }]}
        """.trimIndent()

        val place = BanResponseParser.parse(json).single()
        assertEquals("ban_33063_1234_00012", place.id)
        assertEquals(PlaceKind.ADDRESS, place.kind)
        assertEquals("12 Rue Sainte-Catherine", place.name)
        assertEquals("33000 Bordeaux", place.subtitle)
        assertEquals(44.8412, place.position.lat, 1e-9)
        assertEquals(-0.5736, place.position.lng, 1e-9)
    }

    @Test
    fun `réponse sans résultats`() {
        assertTrue(BanResponseParser.parse("""{ "features": [] }""").isEmpty())
        assertTrue(BanResponseParser.parse("""{ }""").isEmpty())
    }
}
