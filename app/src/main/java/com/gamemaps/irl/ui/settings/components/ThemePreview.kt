package com.gamemaps.irl.ui.settings.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.gamemaps.irl.map.theme.MapPalette

/**
 * Aperçu d'un thème : une petite carte inventée, toujours la même, peinte avec ses couleurs
 * (fond, végétation, eau, bâtiments, routes, grand axe, itinéraire et véhicule).
 */
@Composable
fun ThemePreview(palette: MapPalette, modifier: Modifier = Modifier) {
    Canvas(modifier.clip(RoundedCornerShape(12.dp))) { drawMiniMap(palette) }
}

private fun color(hex: String) = Color(android.graphics.Color.parseColor(hex))

/** Tout est placé en proportions de la zone (0 à 1), pour servir de vignette comme de grand aperçu. */
private fun DrawScope.drawMiniMap(palette: MapPalette) {
    val w = size.width
    val h = size.height
    fun at(x: Float, y: Float) = Offset(x * w, y * h)
    // Ligne brisée donnée par ses points : x1, y1, x2, y2…
    fun path(vararg xy: Float) = Path().apply {
        xy.toList().chunked(2).forEachIndexed { i, (x, y) -> if (i == 0) moveTo(x * w, y * h) else lineTo(x * w, y * h) }
    }
    val thin = Stroke(width = h * 0.045f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val wide = Stroke(width = h * 0.085f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    drawRect(color(palette.background))

    // Végétation en haut à droite, eau en bas à gauche.
    drawPath(path(0.58f, 0f, 1f, 0f, 1f, 0.42f, 0.72f, 0.34f).apply { close() }, color(palette.landcover))
    drawPath(path(0f, 0.62f, 0.2f, 0.7f, 0.3f, 1f, 0f, 1f).apply { close() }, color(palette.water))

    // Bâtiments.
    listOf(0.1f to 0.14f, 0.26f to 0.2f, 0.44f to 0.1f, 0.66f to 0.62f, 0.82f to 0.7f).forEach { (x, y) ->
        drawRect(color(palette.building), topLeft = at(x, y), size = Size(w * 0.1f, h * 0.14f))
    }

    // Rues, puis le grand axe par-dessus.
    drawPath(path(0.38f, 0f, 0.4f, 1f), color(palette.road), style = thin)
    drawPath(path(0f, 0.36f, 0.6f, 0.42f, 1f, 0.56f), color(palette.road), style = thin)
    drawPath(path(0f, 0.9f, 0.5f, 0.78f, 1f, 0.9f), color(palette.majorRoad), style = wide)

    // Itinéraire avec son liseré, et le véhicule au bout.
    val route = path(0.4f, 0.98f, 0.39f, 0.4f, 0.6f, 0.42f, 0.86f, 0.5f)
    drawPath(route, color(palette.routeCasing), style = Stroke(width = h * 0.11f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(route, color(palette.route), style = Stroke(width = h * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Un nom de rue stylisé : un trait de la couleur des textes.
    drawLine(color(palette.label), at(0.5f, 0.24f), at(0.68f, 0.24f), strokeWidth = h * 0.03f, cap = StrokeCap.Round)
}
