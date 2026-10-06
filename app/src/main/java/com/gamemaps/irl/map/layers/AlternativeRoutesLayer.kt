package com.gamemaps.irl.map.layers

import com.gamemaps.irl.data.routing.Route
import com.gamemaps.irl.map.toGeoJsonPoint
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString

/**
 * Dans l'aperçu, les trajets proposés mais non choisis : des lignes grises sous le tracé coloré.
 * À installer après [RouteLayer].
 */
class AlternativeRoutesLayer(private val style: Style) {

    private val source = GeoJsonSource(SOURCE_ID)

    fun install() {
        style.addSource(source)
        style.addLayerBelow(
            LineLayer(LAYER_ID, SOURCE_ID).withProperties(
                PropertyFactory.lineColor(GREY),
                PropertyFactory.lineWidth(7f),
                PropertyFactory.lineOpacity(0.85f),
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
            ),
            RouteLayer.CASING_LAYER_ID,
        )
    }

    fun update(routes: List<Route>) {
        val features = routes.map { route ->
            Feature.fromGeometry(LineString.fromLngLats(route.geometry.map { it.toGeoJsonPoint() }))
        }
        source.setGeoJson(FeatureCollection.fromFeatures(features))
    }

    private companion object {
        const val SOURCE_ID = "route-alternatives-source"
        const val LAYER_ID = "route-alternatives"
        const val GREY = "#7b8494"
    }
}
