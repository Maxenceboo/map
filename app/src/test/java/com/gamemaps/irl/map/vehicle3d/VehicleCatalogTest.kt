package com.gamemaps.irl.map.vehicle3d

import com.gamemaps.irl.map.vehicle3d.models.MinecraftPig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Vérifie que chaque modèle du catalogue est cohérent (évite un véhicule invisible ou difforme). */
class VehicleCatalogTest {

    /** Tous les modèles affichables : ceux des Paramètres et celui imposé par le thème Minecraft. */
    private val models = VehicleKind.entries.map { it.name to it.model } + ("MINECRAFT_PIG" to MinecraftPig.model)

    /** Les vrais véhicules, avec des roues (ni la flèche ni le cochon). */
    private val wheeled = VehicleKind.entries.filter { it != VehicleKind.ARROW }

    @Test
    fun `chaque pièce est un volume valide`() {
        models.forEach { (name, model) ->
            model.parts.forEach { part ->
                assertTrue("$name : contour à moins de 3 points", part.footprint.size >= 3)
                assertTrue("$name : pièce sans épaisseur", part.topMeters > part.baseMeters)
                assertTrue("$name : pièce sous le sol", part.baseMeters >= 0.0)
            }
        }
    }

    @Test
    fun `dimensions réalistes et véhicule centré`() {
        models.forEach { (name, model) ->
            assertTrue("$name : longueur ${model.lengthMeters} m", model.lengthMeters in 2.0..6.0)
            val xs = model.parts.flatMap { it.footprint }.map { it.x }
            assertEquals("$name : pas symétrique gauche/droite", xs.max(), -xs.min(), 1e-9)
            assertTrue("$name : hauteur", model.parts.maxOf { it.topMeters } in 0.5..2.0)
        }
    }

    @Test
    fun `la flèche pointe vers l'avant`() {
        val body = VehicleKind.ARROW.model.parts.last().footprint
        val tip = body.maxBy { it.z }
        assertEquals(0.0, tip.x, 1e-9) // la pointe est sur l'axe
        assertTrue(body.count { it.z < 0 } >= 3) // les deux ailes et l'encoche sont à l'arrière
    }

    @Test
    fun `chaque véhicule roulant a des pneus, une carrosserie et un feu arrière`() {
        wheeled.forEach { kind ->
            val roles = kind.model.parts.map { it.role }.toSet()
            assertTrue("$kind", roles.containsAll(setOf(PartRole.BODY, PartRole.TIRE, PartRole.TAILLIGHT)))
        }
    }

    @Test
    fun `le cochon garde ses couleurs quelle que soit la carrosserie choisie`() {
        assertTrue(MinecraftPig.model.parts.all { it.colorHex != null })
    }
}
