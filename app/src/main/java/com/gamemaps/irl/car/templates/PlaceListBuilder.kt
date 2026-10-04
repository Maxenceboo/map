package com.gamemaps.irl.car.templates

import androidx.car.app.model.ItemList
import androidx.car.app.model.Row
import com.gamemaps.irl.data.places.SavedPlaces
import com.gamemaps.irl.data.search.Place

/** Listes de lieux pour la voiture, limitées au nombre de lignes autorisé par l'hôte. */
object PlaceListBuilder {

    /** Résultats de recherche. */
    fun build(places: List<Place>, maxItems: Int, onSelect: (Place) -> Unit): ItemList =
        buildLabeled(places.map { it.name to it }, maxItems, "Aucun résultat", onSelect)

    /** Maison, Travail puis favoris : proposés tant que rien n'est tapé. */
    fun buildSaved(saved: SavedPlaces, maxItems: Int, onSelect: (Place) -> Unit): ItemList {
        val entries = buildList {
            saved.home?.let { add("Maison" to it) }
            saved.work?.let { add("Travail" to it) }
            saved.favorites.forEach { add("★ ${it.name}" to it) }
        }
        return buildLabeled(entries, maxItems, "Tapez une adresse ou un lieu", onSelect)
    }

    private fun buildLabeled(entries: List<Pair<String, Place>>, maxItems: Int, emptyMessage: String, onSelect: (Place) -> Unit): ItemList {
        val builder = ItemList.Builder().setNoItemsMessage(emptyMessage)
        entries.take(maxItems).forEach { (title, place) ->
            val row = Row.Builder()
                .setTitle(title)
                .apply {
                    val detail = if (title == place.name) place.subtitle else listOf(place.name, place.subtitle).filter { it.isNotBlank() }.joinToString(" · ")
                    if (detail.isNotBlank()) addText(detail)
                }
                .setOnClickListener { onSelect(place) }
                .build()
            builder.addItem(row)
        }
        return builder.build()
    }
}
