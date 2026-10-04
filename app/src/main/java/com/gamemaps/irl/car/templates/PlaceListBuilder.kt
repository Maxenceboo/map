package com.gamemaps.irl.car.templates

import androidx.car.app.model.ItemList
import androidx.car.app.model.Row
import com.gamemaps.irl.core.format.DistanceFormatter
import com.gamemaps.irl.core.geo.GeoMath
import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.places.SavedPlaces
import com.gamemaps.irl.data.search.Place

/** Listes de lieux pour la voiture, limitées au nombre de lignes autorisé par l'hôte. */
object PlaceListBuilder {

    /** Résultats de recherche. [near] (position actuelle) ajoute la distance devant l'adresse. */
    fun build(places: List<Place>, near: LatLng?, maxItems: Int, onSelect: (Place) -> Unit): ItemList =
        buildLabeled(places.map { it.name to it }, near, maxItems, "Aucun résultat", onSelect)

    /** Maison, Travail puis favoris : proposés tant que rien n'est tapé. */
    fun buildSaved(saved: SavedPlaces, near: LatLng?, maxItems: Int, onSelect: (Place) -> Unit): ItemList {
        val entries = buildList {
            saved.home?.let { add("Maison" to it) }
            saved.work?.let { add("Travail" to it) }
            saved.favorites.forEach { add(it.name to it) }
        }
        return buildLabeled(entries, near, maxItems, "Tapez ou dictez une adresse ou un lieu", onSelect)
    }

    private fun buildLabeled(
        entries: List<Pair<String, Place>>,
        near: LatLng?,
        maxItems: Int,
        emptyMessage: String,
        onSelect: (Place) -> Unit,
    ): ItemList {
        val builder = ItemList.Builder().setNoItemsMessage(emptyMessage)
        entries.take(maxItems).forEach { (title, place) ->
            val detail = detailOf(title, place, near)
            val row = Row.Builder()
                .setTitle(title)
                .apply { if (detail.isNotBlank()) addText(detail) }
                .setOnClickListener { onSelect(place) }
                .build()
            builder.addItem(row)
        }
        return builder.build()
    }

    /** "45 km · Place des Compagnons, 33120 Arcachon" ; le nom du lieu est rappelé si le titre est "Maison" / "Travail". */
    internal fun detailOf(title: String, place: Place, near: LatLng?): String = listOfNotNull(
        near?.let { DistanceFormatter.format(GeoMath.distanceMeters(it, place.position)) },
        place.name.takeIf { it != title },
        place.subtitle,
    ).filter { it.isNotBlank() }.joinToString(" · ")
}
