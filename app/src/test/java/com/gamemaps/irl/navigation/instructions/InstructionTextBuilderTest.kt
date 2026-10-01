package com.gamemaps.irl.navigation.instructions

import com.gamemaps.irl.TestFixtures
import com.gamemaps.irl.data.routing.ManeuverType
import org.junit.Assert.assertEquals
import org.junit.Test

class InstructionTextBuilderTest {

    private val base = TestFixtures.lShapedRoute().steps[1]

    @Test
    fun `virage avec nom de rue`() {
        assertEquals("Tournez à droite sur Rue Sainte-Catherine", InstructionTextBuilder.build(base))
    }

    @Test
    fun `virage sans nom de rue`() {
        assertEquals("Tournez à gauche", InstructionTextBuilder.build(base.copy(maneuver = ManeuverType.LEFT, roadName = "")))
    }

    @Test
    fun `rond-point avec numéro de sortie`() {
        val step = base.copy(maneuver = ManeuverType.ROUNDABOUT, roundaboutExit = 1, roadName = "D1010")
        assertEquals("Au rond-point, prenez la 1re sortie vers D1010", InstructionTextBuilder.build(step))
        assertEquals("Au rond-point, prenez la 3e sortie vers D1010", InstructionTextBuilder.build(step.copy(roundaboutExit = 3)))
    }

    @Test
    fun `arrivée`() {
        assertEquals("Vous êtes arrivé à destination", InstructionTextBuilder.build(base.copy(maneuver = ManeuverType.ARRIVE)))
    }
}
