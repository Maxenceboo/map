package com.gamemaps.irl.ui.settings.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.gamemaps.irl.map.vehicle3d.GroundPoint
import com.gamemaps.irl.map.vehicle3d.VehicleColor
import com.gamemaps.irl.map.vehicle3d.VehicleModel
import com.gamemaps.irl.map.vehicle3d.VehiclePalette
import com.gamemaps.irl.map.vehicle3d.VehiclePart
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Aperçu 3D d'un véhicule, dessiné sans la carte : le modèle est vu de trois quarts arrière,
 * comme pendant le guidage. Chaque pièce est un volume dont on dessine le dessus et les flancs visibles.
 */
@Composable
fun VehiclePreview(model: VehicleModel, color: VehicleColor, modifier: Modifier = Modifier) {
    // Les faces ne sont recalculées que si le modèle ou la couleur change.
    val faces = remember(model, color) { facesOf(model, color) }
    Canvas(modifier) { drawFaces(faces) }
}

/** Une face prête à dessiner : son contour dans le plan de l'écran (unités du modèle) et sa couleur ombrée. */
private class Face(val outline: List<Offset>, val color: Color)

/** Angle de vue : tourné de 32° vers la gauche du véhicule, caméra inclinée de 38° vers le bas. */
private val YAW = Math.toRadians(32.0)
private val PITCH = Math.toRadians(38.0)

/** Luminosité du dessus et des flancs : les flancs latéraux sont un peu plus sombres que l'arrière. */
private const val TOP_SHADE = 1.0f
private const val REAR_SHADE = 0.78f
private const val SIDE_SHADE = 0.6f

private fun facesOf(model: VehicleModel, bodyColor: VehicleColor): List<Face> =
    // Les pièces les plus éloignées de la caméra d'abord : les plus proches les recouvrent.
    model.parts.sortedByDescending { depthOf(it) }.flatMap { part ->
        val base = Color(android.graphics.Color.parseColor(part.colorHex ?: VehiclePalette.hexFor(part.role, bodyColor)))
        val ring = part.footprint
        val clockwise = signedArea(ring) < 0
        val sides = ring.indices.mapNotNull { i ->
            val a = ring[i]
            val b = ring[(i + 1) % ring.size]
            // Normale du flanc, vers l'extérieur de la pièce.
            val normal = if (clockwise) GroundPoint(-(b.z - a.z), b.x - a.x) else GroundPoint(b.z - a.z, -(b.x - a.x))
            // La caméra regarde dans la direction (sin YAW, cos YAW) : un flanc qui lui tourne le dos est caché.
            val facing = normal.x * sin(YAW) + normal.z * cos(YAW)
            if (facing >= 0) return@mapNotNull null
            val lateral = kotlin.math.abs(normal.x) > kotlin.math.abs(normal.z)
            Face(
                outline = listOf(project(a, part.baseMeters), project(b, part.baseMeters), project(b, part.topMeters), project(a, part.topMeters)),
                color = shade(base, if (lateral) SIDE_SHADE else REAR_SHADE),
            )
        }
        sides + Face(ring.map { project(it, part.topMeters) }, shade(base, TOP_SHADE))
    }

/** Point du modèle (x à droite, z vers l'avant, hauteur) → plan de l'écran (y vers le bas). */
private fun project(point: GroundPoint, height: Double): Offset {
    val across = point.x * cos(YAW) - point.z * sin(YAW)
    val away = point.x * sin(YAW) + point.z * cos(YAW)
    return Offset(across.toFloat(), -(height * cos(PITCH) + away * sin(PITCH)).toFloat())
}

/** Éloignement du centre de la pièce par rapport à la caméra (plus grand = plus loin). */
private fun depthOf(part: VehiclePart): Double {
    val x = part.footprint.map { it.x }.average()
    val z = part.footprint.map { it.z }.average()
    val away = x * sin(YAW) + z * cos(YAW)
    return away * cos(PITCH) - (part.baseMeters + part.topMeters) / 2 * sin(PITCH)
}

private fun signedArea(ring: List<GroundPoint>): Double =
    ring.indices.sumOf { i ->
        val a = ring[i]
        val b = ring[(i + 1) % ring.size]
        a.x * b.z - b.x * a.z
    }

private fun shade(color: Color, factor: Float) = Color(color.red * factor, color.green * factor, color.blue * factor, 1f)

/** Met le dessin à l'échelle de la zone disponible, centré, avec une petite marge. */
private fun DrawScope.drawFaces(faces: List<Face>) {
    val points = faces.flatMap { it.outline }
    if (points.isEmpty()) return
    val minX = points.minOf { it.x }
    val maxX = points.maxOf { it.x }
    val minY = points.minOf { it.y }
    val maxY = points.maxOf { it.y }
    val margin = 0.88f
    val scale = min(size.width / (maxX - minX), size.height / (maxY - minY)) * margin
    val offsetX = (size.width - (maxX - minX) * scale) / 2 - minX * scale
    val offsetY = (size.height - (maxY - minY) * scale) / 2 - minY * scale

    faces.forEach { face ->
        val path = Path().apply {
            face.outline.forEachIndexed { i, p ->
                val x = p.x * scale + offsetX
                val y = p.y * scale + offsetY
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        drawPath(path, face.color)
    }
}
