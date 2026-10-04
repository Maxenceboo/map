package com.gamemaps.irl.data.search.photon

import com.gamemaps.irl.data.search.PlaceKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PhotonResponseParserTest {

    private val json = """
        { "type": "FeatureCollection", "features": [
          { "geometry": { "coordinates": [-0.5560, 44.8256] },
            "properties": { "osm_type": "N", "osm_id": 1, "osm_key": "railway", "osm_value": "station", "type": "house",
                            "name": "Gare Saint-Jean", "street": "Rue Charles Domercq", "postcode": "33800", "city": "Bordeaux" } },
          { "geometry": { "coordinates": [-0.5792, 44.8378] },
            "properties": { "osm_type": "R", "osm_id": 2, "osm_key": "place", "osm_value": "city", "type": "city",
                            "name": "Bordeaux", "postcode": "33000", "country": "France" } },
          { "geometry": { "coordinates": [-0.5700, 44.8400] },
            "properties": { "osm_type": "W", "osm_id": 3, "osm_key": "highway", "osm_value": "residential", "type": "street",
                            "name": "Rue Sainte-Catherine", "postcode": "33000", "city": "Bordeaux" } },
          { "geometry": { "coordinates": [0, 0] }, "properties": { "osm_type": "N", "osm_id": 4 } }
        ]}
    """.trimIndent()

    @Test
    fun `point d'intérêt avec catégorie et adresse`() {
        val station = PhotonResponseParser.parse(json)[0]
        assertEquals("photon_N1", station.id)
        assertEquals("Gare Saint-Jean", station.name)
        assertEquals("Rue Charles Domercq, 33800 Bordeaux", station.subtitle)
        assertEquals(PlaceKind.POI, station.kind)
        assertEquals("railway:station", station.category)
        assertEquals(44.8256, station.position.lat, 1e-9)
    }

    @Test
    fun `ville et rue reconnues, éléments sans nom ignorés`() {
        val places = PhotonResponseParser.parse(json)
        assertEquals(3, places.size)
        assertEquals(PlaceKind.CITY, places[1].kind)
        assertNull(places[1].category)
        assertEquals(PlaceKind.STREET, places[2].kind)
    }
}
