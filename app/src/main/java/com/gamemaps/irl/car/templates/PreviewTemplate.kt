package com.gamemaps.irl.car.templates

import android.text.SpannableString
import android.text.Spanned
import androidx.car.app.model.Action
import androidx.car.app.model.DistanceSpan
import androidx.car.app.model.DurationSpan
import androidx.car.app.model.ItemList
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.RoutePreviewNavigationTemplate
import com.gamemaps.irl.car.mapping.CarDistanceMapper
import com.gamemaps.irl.core.format.ArrivalTimeFormatter
import com.gamemaps.irl.navigation.NavigationState

/**
 * Choix du trajet sur l'écran de la voiture : la liste des trajets proposés (durée, distance,
 * heure d'arrivée), le trajet choisi tracé en couleur sur la carte, un bouton "Démarrer"
 * et la croix pour annuler.
 */
object PreviewTemplate {

    /** @param maxRoutes nombre de trajets que l'écran de la voiture accepte d'afficher. */
    fun build(
        state: NavigationState.Previewing,
        maxRoutes: Int,
        onSelect: (Int) -> Unit,
        onStart: () -> Unit,
        onCancel: () -> Unit,
    ): Template {
        val routes = state.alternatives.take(maxRoutes.coerceAtLeast(1))
        val now = System.currentTimeMillis()
        val list = ItemList.Builder()
            .setOnSelectedListener(onSelect)
            // Le trajet choisi peut être hors de la liste si l'écran en affiche moins : on retombe sur le premier.
            .setSelectedIndex(routes.indexOfFirst { it === state.route }.coerceAtLeast(0))
        routes.forEachIndexed { index, route ->
            list.addItem(
                Row.Builder()
                    .setTitle(durationAndDistance(route.durationSeconds, route.lengthMeters))
                    .addText("${labelOf(index, route.lengthMeters, routes.minOf { it.lengthMeters })} · arrivée ${ArrivalTimeFormatter.format(now, route.durationSeconds)}")
                    .build(),
            )
        }
        return RoutePreviewNavigationTemplate.Builder()
            .setTitle(state.destination.name)
            .setItemList(list.build())
            .setNavigateAction(Action.Builder().setTitle("Démarrer").setOnClickListener { onStart() }.build())
            .setActionStrip(CarActions.strip(CarActions.cancelTrip(onCancel)))
            .build()
    }

    /** "14 min · 5,4 km" : Android Auto impose des marqueurs de durée et de distance, qu'il met lui-même en forme. */
    private fun durationAndDistance(durationSeconds: Double, lengthMeters: Double): CharSequence {
        val text = SpannableString("  ·  ")
        text.setSpan(DurationSpan.create(durationSeconds.toLong()), 0, 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        text.setSpan(DistanceSpan.create(CarDistanceMapper.map(lengthMeters)), 4, 5, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        return text
    }

    private fun labelOf(index: Int, lengthMeters: Double, shortestMeters: Double): String = when {
        index == 0 -> "Conseillé"
        lengthMeters == shortestMeters -> "Plus court"
        else -> "Autre trajet"
    }
}
