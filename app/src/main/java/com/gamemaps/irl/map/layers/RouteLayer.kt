package com.gamemaps.irl.map.layers

import com.gamemaps.irl.data.routing.Route
import com.gamemaps.irl.map.theme.MapPalette
import com.gamemaps.irl.map.toGeoJsonPoint
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString

/**
 * Tracé de l'itinéraire : un liseré sombre (casing) sous une ligne violette de 8 px
 * (cahier des charges §5.2). Les bordures de trafic sont dans [TrafficLayer].
 */
class RouteLayer(private val style: Style, private var palette: MapPalette) {

    private val source = GeoJsonSource(SOURCE_ID)

    fun install() {
        style.addSource(source)
        addBelowLabels(casingLayer())
        addBelowLabels(lineLayer())
    }

    fun update(route: Route?) {
        if (route == null) {
            source.setGeoJson(FeatureCollection.fromFeatures(emptyList<Feature>()))
        } else {
            source.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(route.geometry.map { it.toGeoJsonPoint() })))
        }
    }

    /** Changement de thème : nouvelles couleurs du tracé, sans recréer les calques. */
    fun applyPalette(newPalette: MapPalette) {
        palette = newPalette
        style.getLayer(CASING_LAYER_ID)?.setProperties(PropertyFactory.lineColor(newPalette.routeCasing))
        style.getLayer(LINE_LAYER_ID)?.setProperties(PropertyFactory.lineColor(newPalette.route))
    }

    private fun addBelowLabels(layer: Layer) {
        val labelsId = LayerOrder.firstSymbolLayerId(style)
        if (labelsId != null) style.addLayerBelow(layer, labelsId) else style.addLayer(layer)
    }

    private fun casingLayer() = LineLayer(CASING_LAYER_ID, SOURCE_ID).withProperties(
        PropertyFactory.lineColor(palette.routeCasing),
        PropertyFactory.lineWidth(12f),
        PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
        PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
    )

    private fun lineLayer() = LineLayer(LINE_LAYER_ID, SOURCE_ID).withProperties(
        PropertyFactory.lineColor(palette.route),
        PropertyFactory.lineWidth(8f),
        PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
        PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
    )

    companion object {
        /** Identifiant de la ligne violette : [TrafficLayer] se glisse juste dessous. */
        const val LINE_LAYER_ID = "route-line"
        private const val SOURCE_ID = "route-source"
        private const val CASING_LAYER_ID = "route-casing"
    }
}
