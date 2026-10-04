package com.gamemaps.irl.map.layers

import android.graphics.Bitmap
import com.gamemaps.irl.data.radar.Radar
import com.gamemaps.irl.map.toGeoJsonPoint
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection

/** Radars affichés sur la carte, toujours face à l'écran (même quand la carte est inclinée). */
class RadarLayer(private val style: Style, private val icon: Bitmap) {

    private val source = GeoJsonSource(SOURCE_ID)

    fun install() {
        style.addImage(ICON_ID, icon)
        style.addSource(source)
        style.addLayer(
            SymbolLayer(LAYER_ID, SOURCE_ID).withProperties(
                PropertyFactory.iconImage(ICON_ID),
                PropertyFactory.iconSize(0.55f),
                PropertyFactory.iconPitchAlignment(Property.ICON_PITCH_ALIGNMENT_VIEWPORT),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true),
            ),
        )
    }

    fun update(radars: List<Radar>) {
        source.setGeoJson(FeatureCollection.fromFeatures(radars.map { Feature.fromGeometry(it.position.toGeoJsonPoint()) }))
    }

    private companion object {
        const val SOURCE_ID = "radar-source"
        const val LAYER_ID = "radar-layer"
        const val ICON_ID = "radar-icon"
    }
}
