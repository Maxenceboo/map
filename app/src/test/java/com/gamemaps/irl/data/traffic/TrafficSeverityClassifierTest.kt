package com.gamemaps.irl.data.traffic

import com.gamemaps.irl.core.geo.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrafficSeverityClassifierTest {

    @Test
    fun `ralentissement modéré`() {
        assertEquals(TrafficSeverity.SLOW, TrafficSeverityClassifier.classify(speedKmh = 35.0, delaySeconds = 40.0, magnitude = 1))
        assertEquals(TrafficSeverity.SLOW, TrafficSeverityClassifier.classify(speedKmh = null, delaySeconds = 0.0, magnitude = 0))
    }

    @Test
    fun `bouchon - vitesse très basse, gros retard ou magnitude élevée`() {
        assertEquals(TrafficSeverity.JAM, TrafficSeverityClassifier.classify(speedKmh = 12.0, delaySeconds = 10.0, magnitude = 1))
        assertEquals(TrafficSeverity.JAM, TrafficSeverityClassifier.classify(speedKmh = 40.0, delaySeconds = 180.0, magnitude = 1))
        assertEquals(TrafficSeverity.JAM, TrafficSeverityClassifier.classify(speedKmh = 40.0, delaySeconds = 10.0, magnitude = 3))
    }

    @Test
    fun `une portion ne retient que ses points du tracé`() {
        val geometry = (0..5).map { LatLng(44.84 + it * 0.001, -0.58) }
        val section = TrafficSection(startIndex = 2, endIndex = 4, severity = TrafficSeverity.JAM, delaySeconds = 60.0)
        assertEquals(geometry.subList(2, 5), section.pointsOf(geometry))
        // Indices hors du tracé (réponse incohérente) : rien à dessiner plutôt qu'un plantage.
        assertTrue(section.copy(startIndex = 9, endIndex = 12).pointsOf(geometry).isEmpty())
        assertEquals(geometry.subList(4, 6), section.copy(startIndex = 4, endIndex = 12).pointsOf(geometry))
    }
}
