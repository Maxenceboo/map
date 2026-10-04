package com.gamemaps.irl.map.layers

import com.gamemaps.irl.data.radar.Radar
import com.gamemaps.irl.data.radar.RadarType
import com.gamemaps.irl.map.toGeoJsonPoint
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection

/**
 * Radars affichés sur la carte, toujours face à l'écran (même quand la carte est inclinée).
 * Chaque type a son icône, choisie via la propriété "type" de chaque point.
 */
class RadarLayer(private val style: Style) {

    private val source = GeoJsonSource(SOURCE_ID)

    fun install() {
        RadarType.entries.forEach { style.addImage(iconId(it), RadarIconBitmap.create(it)) }
        style.addSource(source)
        style.addLayer(
            SymbolLayer(LAYER_ID, SOURCE_ID).withProperties(
                // L'icône s'appelle "radar-<TYPE>" : on la construit à partir de la propriété du point.
                PropertyFactory.iconImage(Expression.concat(Expression.literal(ICON_PREFIX), Expression.get(TYPE_PROPERTY))),
                PropertyFactory.iconSize(0.55f),
                PropertyFactory.iconPitchAlignment(Property.ICON_PITCH_ALIGNMENT_VIEWPORT),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true),
            ),
        )
    }

    fun update(radars: List<Radar>) {
        val features = radars.map { radar ->
            Feature.fromGeometry(radar.position.toGeoJsonPoint()).apply { addStringProperty(TYPE_PROPERTY, radar.type.name) }
        }
        source.setGeoJson(FeatureCollection.fromFeatures(features))
    }

    private fun iconId(type: RadarType) = "$ICON_PREFIX${type.name}"

    private companion object {
        const val SOURCE_ID = "radar-source"
        const val LAYER_ID = "radar-layer"
        const val ICON_PREFIX = "radar-"
        const val TYPE_PROPERTY = "type"
    }
}
