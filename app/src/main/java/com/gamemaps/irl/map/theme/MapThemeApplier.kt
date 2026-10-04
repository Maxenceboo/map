package com.gamemaps.irl.map.theme

import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.BackgroundLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer

/**
 * Repeint un style OpenMapTiles existant avec une [MapPalette] et, si le thème en a, des motifs ([ThemePatterns]).
 *
 * Plutôt que d'écrire un style JSON complet par thème, on part d'un style de base
 * et on recolore chaque calque selon sa `source-layer` (water, transportation, building...).
 * Un motif à null retire le motif précédent (retour d'un thème Minecraft vers GTA, par exemple).
 */
object MapThemeApplier {

    private val MAJOR_ROAD_KEYWORDS = listOf("motorway", "trunk", "primary")
    private val WOODS_SOURCE_LAYERS = setOf("landcover", "park")
    private const val SHIELD_TEXT = "#1f2937"
    private const val SHIELD_HALO = "#ffffff"

    fun apply(style: Style, palette: MapPalette, patterns: ThemePatterns = ThemePatterns.NONE) {
        style.layers.forEach { layer ->
            // Nos propres calques (tracé, véhicule, radars…) n'ont pas de source-layer : on n'y touche pas,
            // ils ont leurs propres couleurs (sinon un changement de thème repeindrait le tracé en gris).
            if (isGameLayer(layer)) return@forEach
            when (layer) {
                is BackgroundLayer -> layer.setProperties(
                    PropertyFactory.backgroundColor(palette.background),
                    PropertyFactory.backgroundPattern(patterns.ground),
                )
                is FillLayer -> layer.setProperties(
                    PropertyFactory.fillColor(fillColor(layer.sourceLayer, palette)),
                    PropertyFactory.fillPattern(fillPattern(layer.sourceLayer, patterns)),
                )
                is LineLayer -> layer.setProperties(PropertyFactory.lineColor(lineColor(layer.id, layer.sourceLayer, palette)))
                // Les numéros de route ("D 207") sont dessinés sur un panneau blanc : texte sombre fixe,
                // sinon un thème à libellés clairs (Minecraft, Waze) les rendrait illisibles.
                is SymbolLayer -> if (layer.id.contains("shield")) {
                    layer.setProperties(PropertyFactory.textColor(SHIELD_TEXT), PropertyFactory.textHaloColor(SHIELD_HALO))
                } else {
                    layer.setProperties(PropertyFactory.textColor(palette.label), PropertyFactory.textHaloColor(palette.labelHalo))
                }
            }
        }
    }

    private fun isGameLayer(layer: Layer): Boolean = when (layer) {
        is FillLayer -> layer.sourceLayer.isEmpty()
        is LineLayer -> layer.sourceLayer.isEmpty()
        is SymbolLayer -> layer.sourceLayer.isEmpty()
        else -> false
    }

    private fun fillColor(sourceLayer: String, palette: MapPalette): String = when (sourceLayer) {
        "water" -> palette.water
        "building" -> palette.building
        else -> palette.landcover
    }

    /** Eau → texture d'eau, bois et parcs → arbres, zones habitées → herbe ; bâtiments : jamais de motif. */
    private fun fillPattern(sourceLayer: String, patterns: ThemePatterns): String? = when (sourceLayer) {
        "water" -> patterns.water
        in WOODS_SOURCE_LAYERS -> patterns.woods
        "landuse" -> patterns.ground
        else -> null
    }

    private fun lineColor(id: String, sourceLayer: String, palette: MapPalette): String = when (sourceLayer) {
        "waterway" -> palette.water
        "boundary" -> palette.boundary
        "transportation" -> if (MAJOR_ROAD_KEYWORDS.any { id.contains(it) }) palette.majorRoad else palette.road
        else -> palette.road
    }
}
