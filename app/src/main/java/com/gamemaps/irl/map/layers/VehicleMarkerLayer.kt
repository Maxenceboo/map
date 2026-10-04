package com.gamemaps.irl.map.layers

import android.graphics.Bitmap
import com.gamemaps.irl.data.location.GpsFix
import com.gamemaps.irl.map.toGeoJsonPoint
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature

/**
 * Marqueur du véhicule : une icône posée à plat sur la carte et tournée selon le cap GPS.
 */
class VehicleMarkerLayer(private val style: Style, private val icon: Bitmap) {

    private val source = GeoJsonSource(SOURCE_ID)

    fun install() {
        style.addImage(ICON_ID, icon)
        style.addSource(source)
        style.addLayer(
            SymbolLayer(LAYER_ID, SOURCE_ID).withProperties(
                PropertyFactory.iconImage(ICON_ID),
                PropertyFactory.iconSize(0.6f),
                PropertyFactory.iconRotate(Expression.get(BEARING_PROPERTY)),
                // "map" : l'icône suit l'orientation de la carte et se couche avec l'inclinaison.
                PropertyFactory.iconRotationAlignment(Property.ICON_ROTATION_ALIGNMENT_MAP),
                PropertyFactory.iconPitchAlignment(Property.ICON_PITCH_ALIGNMENT_MAP),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true),
            ),
        )
    }

    /**
     * Remplace l'icône (changement de thème) : même identifiant, donc le calque suit tout seul.
     * [rotates] = false pour un sprite vu de côté (cochon Minecraft) : il reste droit au lieu de tourner avec le cap.
     */
    fun setIcon(newIcon: Bitmap, rotates: Boolean = true) {
        style.addImage(ICON_ID, newIcon)
        style.getLayer(LAYER_ID)?.setProperties(
            PropertyFactory.iconRotate(if (rotates) Expression.get(BEARING_PROPERTY) else Expression.literal(0f)),
            PropertyFactory.iconRotationAlignment(if (rotates) Property.ICON_ROTATION_ALIGNMENT_MAP else Property.ICON_ROTATION_ALIGNMENT_VIEWPORT),
            PropertyFactory.iconPitchAlignment(if (rotates) Property.ICON_PITCH_ALIGNMENT_MAP else Property.ICON_PITCH_ALIGNMENT_VIEWPORT),
        )
    }

    /** Masqué quand un véhicule 3D est affiché à la place. */
    fun setVisible(visible: Boolean) {
        style.getLayer(LAYER_ID)?.setProperties(PropertyFactory.visibility(if (visible) Property.VISIBLE else Property.NONE))
    }

    fun update(fix: GpsFix) {
        val feature = Feature.fromGeometry(fix.position.toGeoJsonPoint())
        feature.addNumberProperty(BEARING_PROPERTY, fix.bearingDegrees ?: 0f)
        source.setGeoJson(feature)
    }

    private companion object {
        const val SOURCE_ID = "vehicle-source"
        const val LAYER_ID = "vehicle-layer"
        const val ICON_ID = "vehicle-arrow"
        const val BEARING_PROPERTY = "bearing"
    }
}
