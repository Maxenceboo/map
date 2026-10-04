package com.gamemaps.irl.service.notification

import com.gamemaps.irl.TestFixtures
import com.gamemaps.irl.navigation.NavigationState
import com.gamemaps.irl.navigation.RouteProgressCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class NavigationNotificationContentTest {

    private val route = TestFixtures.lShapedRoute()
    private val navigating = NavigationState.Navigating(
        TestFixtures.PLACE,
        route,
        RouteProgressCalculator().compute(route, TestFixtures.START),
    )

    @Test
    fun `guidage - manœuvre dans le titre, arrivée et restant dans le texte`() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        val content = NavigationNotificationContent.from(navigating, nowMillis = 0)
        assertTrue(content.title, content.title.endsWith("· Tournez à droite sur Rue Sainte-Catherine"))
        assertTrue(content.text, content.text.startsWith("Arrivée 00:02 · "))
        assertTrue(content.text, content.text.endsWith("· 2 min"))
    }

    @Test
    fun `recalcul affiché à la place de la manœuvre`() {
        val content = NavigationNotificationContent.from(navigating.copy(isRerouting = true))
        assertEquals("Recalcul de l'itinéraire…", content.title)
    }

    @Test
    fun `autres états`() {
        assertEquals("Calcul de l'itinéraire…", NavigationNotificationContent.from(NavigationState.Calculating(TestFixtures.PLACE)).title)
        assertEquals("Vous êtes arrivé", NavigationNotificationContent.from(NavigationState.Arrived(TestFixtures.PLACE)).title)
    }
}
