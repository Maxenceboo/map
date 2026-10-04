package com.gamemaps.irl.audio.announcers

import com.gamemaps.irl.TestFixtures
import com.gamemaps.irl.audio.cue.AudioCue
import com.gamemaps.irl.audio.cue.SoundEffect
import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.navigation.NavigationState
import com.gamemaps.irl.navigation.RouteProgressCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Itinéraire en L : ~1 km vers le nord, virage à droite, ~1 km vers l'est. */
class GuidanceAnnouncerTest {

    private val route = TestFixtures.lShapedRoute()
    private val calculator = RouteProgressCalculator()
    private val announcer = GuidanceAnnouncer()

    /** Position sur le premier tronçon, à [metersBeforeCorner] du virage. */
    private fun navigatingAt(metersBeforeCorner: Double, rerouting: Boolean = false): NavigationState.Navigating {
        val latPerMeter = (TestFixtures.CORNER.lat - TestFixtures.START.lat) / route.cumulativeDistances[1]
        val position = LatLng(TestFixtures.CORNER.lat - metersBeforeCorner * latPerMeter, TestFixtures.START.lng)
        return NavigationState.Navigating(TestFixtures.PLACE, route, calculator.compute(route, position), rerouting)
    }

    @Test
    fun `démarrage - carillon puis première instruction`() {
        val cues = announcer.onState(navigatingAt(900.0))
        assertEquals(
            listOf(AudioCue.Sound(SoundEffect.START), AudioCue.Speech("Partez sur Cours de l'Intendance")),
            cues,
        )
    }

    @Test
    fun `annonce de loin puis juste avant, une seule fois chacune`() {
        announcer.onState(navigatingAt(900.0))

        assertEquals(
            listOf(AudioCue.Speech("Dans 450 mètres, tournez à droite sur Rue Sainte-Catherine")),
            announcer.onState(navigatingAt(450.0)),
        )
        assertTrue(announcer.onState(navigatingAt(400.0)).isEmpty())

        assertEquals(
            listOf(AudioCue.Sound(SoundEffect.TURN), AudioCue.Speech("Tournez à droite sur Rue Sainte-Catherine")),
            announcer.onState(navigatingAt(60.0)),
        )
        assertTrue(announcer.onState(navigatingAt(40.0)).isEmpty())
    }

    @Test
    fun `recalcul annoncé une fois, sans annonce de manœuvre pendant le recalcul`() {
        announcer.onState(navigatingAt(900.0))
        assertEquals(listOf(AudioCue.Speech("Recalcul de l'itinéraire")), announcer.onState(navigatingAt(450.0, rerouting = true)))
        assertTrue(announcer.onState(navigatingAt(440.0, rerouting = true)).isEmpty())
    }

    @Test
    fun `arrivée - jingle et phrase une seule fois`() {
        announcer.onState(navigatingAt(900.0))
        val arrived = NavigationState.Arrived(TestFixtures.PLACE)
        assertEquals(
            listOf(AudioCue.Sound(SoundEffect.ARRIVAL), AudioCue.Speech("Vous êtes arrivé à destination")),
            announcer.onState(arrived),
        )
        assertTrue(announcer.onState(arrived).isEmpty())
    }

    @Test
    fun `nouveau trajet après l'arrivée - le carillon de départ revient`() {
        announcer.onState(navigatingAt(900.0))
        announcer.onState(NavigationState.Arrived(TestFixtures.PLACE))
        announcer.onState(NavigationState.Idle)
        assertEquals(AudioCue.Sound(SoundEffect.START), announcer.onState(navigatingAt(900.0)).first())
    }
}
