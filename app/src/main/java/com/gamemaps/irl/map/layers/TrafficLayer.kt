package com.gamemaps.irl.map.layers

import com.gamemaps.irl.data.routing.Route
import com.gamemaps.irl.data.traffic.TrafficSeverity
import com.gamemaps.irl.map.toGeoJsonPoint
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString

/**
 * Bouchons sur l'itinéraire : une ligne plus large que le tracé, glissée juste dessous.
 * Il n'en dépasse que les bords : orange pour un ralentissement, rouge pour un bouchon.
 * À installer après [RouteLayer].
 */
class TrafficLayer(private val style: Style) {

    private val source = GeoJsonSource(SOURCE_ID)

    fun install() {
        style.addSource(source)
        style.addLayerBelow(borderLayer(), RouteLayer.LINE_LAYER_ID)
    }

    fun update(route: Route?) {
        val features = route?.trafficSections.orEmpty().mapNotNull { section ->
            val points = section.pointsOf(route!!.geometry)
            if (points.isEmpty()) return@mapNotNull null
            Feature.fromGeometry(LineString.fromLngLats(points.map { it.toGeoJsonPoint() })).apply {
                addStringProperty(COLOR_PROPERTY, colorOf(section.severity))
            }
        }
        source.setGeoJson(FeatureCollection.fromFeatures(features))
    }

    private fun colorOf(severity: TrafficSeverity): String = when (severity) {
        TrafficSeverity.SLOW -> "#ff9f1c"
        TrafficSeverity.JAM -> "#ff2d2d"
    }

    private fun borderLayer() = LineLayer(LAYER_ID, SOURCE_ID).withProperties(
        PropertyFactory.lineColor(Expression.toColor(Expression.get(COLOR_PROPERTY))),
        PropertyFactory.lineWidth(16f), // le tracé fait 8 px : 4 px de bordure de chaque côté
        PropertyFactory.lineCap(Property.LINE_CAP_BUTT),
        PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
    )

    private companion object {
        const val SOURCE_ID = "traffic-source"
        const val LAYER_ID = "traffic-border"
        const val COLOR_PROPERTY = "color"
    }
}
