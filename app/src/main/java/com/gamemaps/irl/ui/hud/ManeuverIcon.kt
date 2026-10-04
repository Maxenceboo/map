package com.gamemaps.irl.ui.hud

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.gamemaps.irl.data.routing.ManeuverType

/**
 * Icône de la prochaine manœuvre, dessinée en vectoriel : flèche qui tourne, demi-tour,
 * rond-point avec le numéro de sortie au centre, repère à l'arrivée.
 */
@Composable
fun ManeuverIcon(maneuver: ManeuverType, roundaboutExit: Int?, color: Color, modifier: Modifier = Modifier) {
    when (maneuver) {
        ManeuverType.ARRIVE -> Icon(Icons.Filled.Place, contentDescription = null, tint = color, modifier = modifier)
        ManeuverType.ROUNDABOUT -> Box(modifier, contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) { drawRoundabout(color) }
            roundaboutExit?.let { Text(it.toString(), color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
        }
        else -> Canvas(modifier) {
            val shape = ManeuverArrowShape.of(maneuver)
            scale(scaleX = if (shape.mirrored) -1f else 1f, scaleY = 1f) { drawArrow(shape.points, color) }
        }
    }
}

/** Taille d'une case de la grille 24 × 24 en pixels. */
private val DrawScope.unit: Float get() = size.minDimension / 24f

private fun DrawScope.drawArrow(points: List<Offset>, color: Color) {
    val line = Path().apply {
        points.forEachIndexed { i, p -> if (i == 0) moveTo(p.x * unit, p.y * unit) else lineTo(p.x * unit, p.y * unit) }
    }
    drawPath(line, color, style = Stroke(width = 3.2f * unit, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawHead(from = points[points.lastIndex - 1], to = points.last(), color = color)
}

/** Pointe triangulaire au bout de la flèche, orientée dans le sens du dernier segment. */
private fun DrawScope.drawHead(from: Offset, to: Offset, color: Color) {
    val direction = (to - from).let { it / it.getDistance() }
    val side = Offset(-direction.y, direction.x)
    val tip = to + direction * 4f
    val left = to + side * 4f
    val right = to - side * 4f
    val head = Path().apply {
        moveTo(tip.x * unit, tip.y * unit)
        lineTo(left.x * unit, left.y * unit)
        lineTo(right.x * unit, right.y * unit)
        close()
    }
    drawPath(head, color)
    drawPath(head, color, style = Stroke(width = 1.2f * unit, join = StrokeJoin.Round))
}

/** Anneau centré, entrée par le bas, sortie fléchée vers le haut. */
private fun DrawScope.drawRoundabout(color: Color) {
    val stroke = Stroke(width = 2.6f * unit, cap = StrokeCap.Round)
    drawCircle(color, radius = 5.5f * unit, center = Offset(12f * unit, 13f * unit), style = stroke)
    drawLine(color, Offset(12f * unit, 22.5f * unit), Offset(12f * unit, 19f * unit), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawHead(from = Offset(12f, 7f), to = Offset(12f, 5.5f), color = color)
}
