package com.gamemaps.irl.data.custom

import com.gamemaps.irl.map.theme.MapTheme
import com.gamemaps.irl.map.vehicle3d.PartRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomContentTest {

    private val theme = CustomThemeSpec("custom-theme-1", "Néon", MapTheme.GTA_RADAR.palette)
    private val vehicle = CustomVehicleSpec("custom-vehicle-1", "Pick-up", length = 5.2, width = 2.0, spoiler = true)

    @Test
    fun `couleurs - contrôle et assombrissement`() {
        assertEquals("#a1b2c3", ColorMath.normalizeHex(" A1B2C3 "))
        assertNull(ColorMath.normalizeHex("#12345"))
        assertNull(ColorMath.normalizeHex("rouge"))
        assertEquals("#800000", ColorMath.darken("#ff0000", 0.5))
    }

    @Test
    fun `changer le fond ou le tracé entraîne les couleurs qui en dépendent`() {
        val palette = ThemeColorSlot.BACKGROUND.write(theme.palette, "#000000")
        assertEquals("#000000", palette.labelHalo)
        val routed = ThemeColorSlot.ROUTE.write(palette, "#00ff00")
        assertEquals("#00ff00", ThemeColorSlot.ROUTE.read(routed))
        assertEquals(ColorMath.darken("#00ff00", 0.35), routed.routeCasing)
    }

    @Test
    fun `thèmes et véhicules survivent à l'enregistrement`() {
        assertEquals(listOf(theme), CustomContentSerializer.themesFromJson(CustomContentSerializer.themesToJson(listOf(theme))))
        assertEquals(listOf(vehicle), CustomContentSerializer.vehiclesFromJson(CustomContentSerializer.vehiclesToJson(listOf(vehicle))))
    }

    @Test
    fun `un fichier abîmé donne une liste vide plutôt qu'un plantage`() {
        assertTrue(CustomContentSerializer.themesFromJson("pas du json").isEmpty())
        assertTrue(CustomContentSerializer.themesFromJson("""[{"id":"x","name":"y","palette":{}}]""").isEmpty())
        assertTrue(CustomContentSerializer.vehiclesFromJson(null).isEmpty())
    }

    @Test
    fun `le véhicule personnalisé a les mesures demandées et reste symétrique`() {
        val model = CustomVehicleBuilder.build(vehicle)
        assertEquals(5.2, model.lengthMeters, 1e-9)
        val xs = model.parts.flatMap { it.footprint }.map { it.x }
        assertEquals(xs.max(), -xs.min(), 1e-9)
        assertTrue(model.parts.all { it.topMeters > it.baseMeters && it.baseMeters >= 0.0 })
        assertTrue(model.parts.map { it.role }.toSet().containsAll(setOf(PartRole.BODY, PartRole.GLASS, PartRole.TIRE, PartRole.TAILLIGHT)))
    }

    @Test
    fun `l'aileron ajoute des pièces et des mesures hors limites sont ramenées dans les bornes`() {
        val without = CustomVehicleBuilder.build(vehicle.copy(spoiler = false))
        assertTrue(CustomVehicleBuilder.build(vehicle).parts.size > without.parts.size)
        assertEquals(CustomVehicleSpec.LENGTH.endInclusive, CustomVehicleBuilder.build(vehicle.copy(length = 40.0)).lengthMeters, 1e-9)
    }
}
