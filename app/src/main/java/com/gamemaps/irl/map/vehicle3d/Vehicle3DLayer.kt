package com.gamemaps.irl.map.vehicle3d

import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.map.toGeoJsonPoint
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.FillExtrusionLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Polygon
import kotlin.math.abs

/**
 * Véhicule en vraie 3D, dessiné par la carte elle-même : chaque pièce est un volume extrudé
 * (`fill-extrusion`) posé à la position GPS, tourné selon le cap. Fonctionne à l'identique
 * sur le téléphone et sur la surface Android Auto (aucun second moteur de rendu).
 *
 * La lumière des phares a deux calques sous le véhicule : une lueur arrondie au sol
 * et un volume translucide qui lui donne de l'épaisseur (voir [HeadlightBeams]).
 *
 * @param zoom zoom actuel de la carte, pour garder le véhicule à taille constante à l'écran.
 */
class Vehicle3DLayer(private val style: Style, private val zoom: () -> Double) {

    private val source = GeoJsonSource(SOURCE_ID)
    private var model: VehicleModel? = null
    private var color = VehicleColor.YELLOW
    private var headlights = true
    private var lastFix: GpsFix? = null
    private var renderedZoom = Double.NaN

    fun install() {
        style.addSource(source)
        style.addLayer(
            FillLayer(BEAMS_LAYER_ID, SOURCE_ID)
                .withFilter(Expression.eq(Expression.get(KIND), Expression.literal(KIND_BEAM)))
                .withProperties(PropertyFactory.fillColor(BEAM_COLOR), PropertyFactory.fillOpacity(Expression.get(OPACITY))),
        )
        style.addLayer(
            FillExtrusionLayer(BEAM_VOLUME_LAYER_ID, SOURCE_ID)
                .withFilter(Expression.eq(Expression.get(KIND), Expression.literal(KIND_BEAM_VOLUME)))
                .withProperties(
                    PropertyFactory.fillExtrusionColor(BEAM_COLOR),
                    PropertyFactory.fillExtrusionHeight(Expression.get(TOP)),
                    PropertyFactory.fillExtrusionBase(Expression.get(BASE)),
                    PropertyFactory.fillExtrusionOpacity(BEAM_VOLUME_OPACITY),
                ),
        )
        style.addLayer(
            FillExtrusionLayer(PARTS_LAYER_ID, SOURCE_ID)
                .withFilter(Expression.eq(Expression.get(KIND), Expression.literal(KIND_PART)))
                .withProperties(
                    PropertyFactory.fillExtrusionColor(Expression.toColor(Expression.get(COLOR))),
                    PropertyFactory.fillExtrusionHeight(Expression.get(TOP)),
                    PropertyFactory.fillExtrusionBase(Expression.get(BASE)),
                    PropertyFactory.fillExtrusionOpacity(1f),
                ),
        )
    }

    /** [model] null = aucun véhicule affiché. */
    fun configure(model: VehicleModel?, color: VehicleColor, headlights: Boolean) {
        this.model = model
        this.color = color
        this.headlights = headlights
        render()
    }

    fun update(fix: GpsFix) {
        lastFix = fix
        render()
    }

    /** Appelé pendant les mouvements de caméra : redessine seulement si le zoom a vraiment changé. */
    fun onCameraMoved() {
        if (model != null && abs(zoom() - renderedZoom) > ZOOM_EPSILON) render()
    }

    private fun render() {
        val model = model
        val fix = lastFix
        if (model == null || fix == null) {
            source.setGeoJson(FeatureCollection.fromFeatures(emptyList<Feature>()))
            return
        }
        renderedZoom = zoom()
        val scale = VehicleScale.factor(renderedZoom, fix.position.lat)
        val bearing = (fix.bearingDegrees ?: 0f).toDouble()

        val parts = VehicleGeometry.place(model, fix.position, bearing, scale, color).map { part ->
            polygon(part.ring).apply {
                addStringProperty(KIND, KIND_PART)
                addStringProperty(COLOR, part.colorHex)
                addNumberProperty(BASE, part.baseMeters)
                addNumberProperty(TOP, part.topMeters)
            }
        }
        val beams = if (!headlights) emptyList() else {
            beamFeatures(HeadlightBeams.glow(model), KIND_BEAM, fix, bearing, scale) +
                beamFeatures(HeadlightBeams.volume(model), KIND_BEAM_VOLUME, fix, bearing, scale)
        }
        source.setGeoJson(FeatureCollection.fromFeatures(beams + parts))
    }

    private fun beamFeatures(shapes: List<BeamShape>, kind: String, fix: GpsFix, bearing: Double, scale: Double): List<Feature> =
        VehicleGeometry.placeBeams(shapes, fix.position, bearing, scale).map { beam ->
            polygon(beam.ring).apply {
                addStringProperty(KIND, kind)
                addNumberProperty(OPACITY, beam.opacity)
                addNumberProperty(BASE, beam.baseMeters)
                addNumberProperty(TOP, beam.topMeters)
            }
        }

    /** Un polygone GeoJSON doit être fermé : on répète le premier point à la fin. */
    private fun polygon(ring: List<LatLng>): Feature =
        Feature.fromGeometry(Polygon.fromLngLats(listOf((ring + ring.first()).map { it.toGeoJsonPoint() })))

    private companion object {
        const val SOURCE_ID = "vehicle3d-source"
        const val PARTS_LAYER_ID = "vehicle3d-parts"
        const val BEAMS_LAYER_ID = "vehicle3d-beams"
        const val BEAM_VOLUME_LAYER_ID = "vehicle3d-beam-volume"
        const val KIND = "kind"
        const val KIND_PART = "part"
        const val KIND_BEAM = "beam"
        const val KIND_BEAM_VOLUME = "beam-volume"
        const val OPACITY = "opacity"
        const val COLOR = "color"
        const val BASE = "base"
        const val TOP = "top"
        const val BEAM_COLOR = "#fff3c4"
        const val BEAM_VOLUME_OPACITY = 0.16f
        const val ZOOM_EPSILON = 0.03
    }
}
