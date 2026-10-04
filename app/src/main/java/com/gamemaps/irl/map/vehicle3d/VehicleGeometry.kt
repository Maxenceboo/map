package com.gamemaps.irl.map.vehicle3d

import com.gamemaps.irl.core.geo.GeoMath
import com.gamemaps.irl.core.geo.LatLng
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pose un véhicule sur la carte : passe du repère du véhicule (mètres, +Z = avant, +X = droite)
 * aux coordonnées GPS, en tenant compte de la position, du cap et de l'échelle. Calcul pur, testable.
 */
object VehicleGeometry {

    /** Chaque pièce du [model], placée autour de [center], tournée selon [bearingDegrees], agrandie de [scale]. */
    fun place(model: VehicleModel, center: LatLng, bearingDegrees: Double, scale: Double, color: VehicleColor): List<PlacedPart> =
        model.parts.map { part ->
            PlacedPart(
                ring = part.footprint.map { toLatLng(it, center, bearingDegrees, scale) },
                baseMeters = part.baseMeters * scale,
                topMeters = part.topMeters * scale,
                colorHex = part.colorHex ?: VehiclePalette.hexFor(part.role, color),
            )
        }

    /** Lumière des phares (voir [HeadlightBeams]), placée devant le véhicule comme ses pièces. */
    fun placeBeams(shapes: List<BeamShape>, center: LatLng, bearingDegrees: Double, scale: Double): List<PlacedBeam> =
        shapes.map { shape ->
            PlacedBeam(
                ring = shape.footprint.map { toLatLng(it, center, bearingDegrees, scale) },
                baseMeters = shape.baseMeters * scale,
                topMeters = shape.topMeters * scale,
                opacity = shape.opacity,
            )
        }

    /**
     * Le cap est mesuré dans le sens horaire depuis le nord :
     * l'avant (+Z) pointe vers (sin cap, cos cap) en (est, nord), la droite (+X) vers (cos cap, -sin cap).
     */
    fun toLatLng(point: GroundPoint, center: LatLng, bearingDegrees: Double, scale: Double): LatLng {
        val bearing = Math.toRadians(bearingDegrees)
        val x = point.x * scale
        val z = point.z * scale
        val east = x * cos(bearing) + z * sin(bearing)
        val north = -x * sin(bearing) + z * cos(bearing)
        val latitude = center.lat + Math.toDegrees(north / GeoMath.EARTH_RADIUS_METERS)
        val longitude = center.lng + Math.toDegrees(east / (GeoMath.EARTH_RADIUS_METERS * cos(Math.toRadians(center.lat))))
        return LatLng(latitude, longitude)
    }
}
