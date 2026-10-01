package com.gamemaps.irl.car.templates

import androidx.car.app.model.ItemList
import androidx.car.app.model.Row
import com.gamemaps.irl.data.search.Place

/** Liste de résultats de recherche pour la voiture, limitée au nombre de lignes autorisé par l'hôte. */
object PlaceListBuilder {

    fun build(places: List<Place>, maxItems: Int, onSelect: (Place) -> Unit): ItemList {
        val builder = ItemList.Builder().setNoItemsMessage("Aucun résultat")
        places.take(maxItems).forEach { place ->
            val row = Row.Builder()
                .setTitle(place.name)
                .apply { if (place.subtitle.isNotBlank()) addText(place.subtitle) }
                .setOnClickListener { onSelect(place) }
                .build()
            builder.addItem(row)
        }
        return builder.build()
    }
}
