package com.gamemaps.irl.map.vehicle3d

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Vérifie que chaque modèle du catalogue est cohérent (évite un véhicule invisible ou difforme). */
class VehicleCatalogTest {

    private val models = VehicleKind.entries.mapNotNull { kind -> kind.model?.let { kind to it } }

    @Test
    fun `la flèche n'a pas de modèle 3D, les autres en ont un`() {
        assertNull(VehicleKind.ARROW.model)
        assertEquals(VehicleKind.entries.size - 1, models.size)
    }

    @Test
    fun `chaque pièce est un volume valide`() {
        models.forEach { (kind, model) ->
            model.parts.forEach { part ->
                assertTrue("$kind : contour à moins de 3 points", part.footprint.size >= 3)
                assertTrue("$kind : pièce sans épaisseur", part.topMeters > part.baseMeters)
                assertTrue("$kind : pièce sous le sol", part.baseMeters >= 0.0)
            }
        }
    }

    @Test
    fun `dimensions réalistes et véhicule centré`() {
        models.forEach { (kind, model) ->
            assertTrue("$kind : longueur ${model.lengthMeters} m", model.lengthMeters in 2.0..6.0)
            val xs = model.parts.flatMap { it.footprint }.map { it.x }
            assertEquals("$kind : pas symétrique gauche/droite", xs.max(), -xs.min(), 1e-9)
            assertTrue("$kind : hauteur", model.parts.maxOf { it.topMeters } in 0.8..2.0)
        }
    }

    @Test
    fun `chaque véhicule a des pneus, une carrosserie et un feu arrière`() {
        models.forEach { (kind, model) ->
            val roles = model.parts.map { it.role }.toSet()
            assertTrue("$kind", roles.containsAll(setOf(PartRole.BODY, PartRole.TIRE, PartRole.TAILLIGHT)))
        }
    }
}
