package com.gamemaps.irl.map.theme

import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.BackgroundLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer

/**
 * Repeint un style OpenMapTiles existant avec une [MapPalette].
 *
 * Plutôt que d'écrire un style JSON complet par thème, on part d'un style de base
 * et on recolore chaque calque selon sa `source-layer` (water, transportation, building...).
 */
object MapThemeApplier {

    private val MAJOR_ROAD_KEYWORDS = listOf("motorway", "trunk", "primary")

    fun apply(style: Style, palette: MapPalette) {
        style.layers.forEach { layer ->
            // Nos propres calques (tracé, véhicule, radars…) n'ont pas de source-layer : on n'y touche pas,
            // ils ont leurs propres couleurs (sinon un changement de thème repeindrait le tracé en gris).
            if (isGameLayer(layer)) return@forEach
            when (layer) {
                is BackgroundLayer -> layer.setProperties(PropertyFactory.backgroundColor(palette.background))
                is FillLayer -> layer.setProperties(PropertyFactory.fillColor(fillColor(layer.sourceLayer, palette)))
                is LineLayer -> layer.setProperties(PropertyFactory.lineColor(lineColor(layer.id, layer.sourceLayer, palette)))
                is SymbolLayer -> layer.setProperties(
                    PropertyFactory.textColor(palette.label),
                    PropertyFactory.textHaloColor(palette.labelHalo),
                )
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

    private fun lineColor(id: String, sourceLayer: String, palette: MapPalette): String = when (sourceLayer) {
        "waterway" -> palette.water
        "boundary" -> palette.boundary
        "transportation" -> if (MAJOR_ROAD_KEYWORDS.any { id.contains(it) }) palette.majorRoad else palette.road
        else -> palette.road
    }
}
