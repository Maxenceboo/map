package com.gamemaps.irl.car.templates

import android.text.SpannableString
import android.text.Spanned
import androidx.car.app.model.DistanceSpan
import androidx.car.app.model.ItemList
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.car.app.navigation.model.PlaceListNavigationTemplate
import com.gamemaps.irl.car.mapping.CarDistanceMapper
import com.gamemaps.irl.core.geo.GeoMath
import com.gamemaps.irl.core.geo.LatLng
import com.gamemaps.irl.data.places.SavedPlaces
import com.gamemaps.irl.data.search.Place

/**
 * Pas de guidage : un menu reste affiché à gauche de la carte, avec la recherche, Maison, Travail
 * et les favoris. (Les boutons posés sur la carte, eux, sont masqués par Android Auto après
 * quelques secondes : on ne peut pas y mettre les choix de destination.)
 */
object IdleTemplate {

    /**
     * @param near position actuelle, pour afficher la distance de chaque lieu.
     * @param maxRows nombre de lignes que l'écran de la voiture accepte.
     * @param onUndefined appui sur Maison ou Travail alors que le lieu n'est pas défini.
     */
    fun menu(
        saved: SavedPlaces,
        near: LatLng?,
        maxRows: Int,
        isMuted: Boolean,
        onSearch: () -> Unit,
        onPlace: (Place) -> Unit,
        onUndefined: (String) -> Unit,
        onHideMenu: () -> Unit,
        onToggleMute: () -> Unit,
    ): Template {
        val rows = buildList {
            add(Row.Builder().setTitle("Rechercher une destination").setBrowsable(true).setOnClickListener { onSearch() }.build())
            add(savedRow("Maison", saved.home, near, onPlace, onUndefined))
            add(savedRow("Travail", saved.work, near, onPlace, onUndefined))
            saved.favorites.forEach { add(placeRow(it.name, it, near, onPlace)) }
        }
        val list = ItemList.Builder()
        rows.take(maxRows.coerceAtLeast(3)).forEach(list::addItem)
        return PlaceListNavigationTemplate.Builder()
            .setTitle("Où aller ?")
            .setItemList(list.build())
            .setActionStrip(CarActions.strip(CarActions.button("Carte", onHideMenu), CarActions.muteToggle(isMuted, onToggleMute)))
            .build()
    }

    /** Carte seule, sans le menu : un bouton le fait revenir (toucher la carte réaffiche les boutons). */
    fun mapOnly(onShowMenu: () -> Unit, isMuted: Boolean, onToggleMute: () -> Unit): Template = NavigationTemplate.Builder()
        .setActionStrip(CarActions.strip(CarActions.button("Où aller ?", onShowMenu), CarActions.muteToggle(isMuted, onToggleMute)))
        .build()

    private fun savedRow(label: String, place: Place?, near: LatLng?, onPlace: (Place) -> Unit, onUndefined: (String) -> Unit): Row =
        if (place != null) {
            placeRow(label, place, near, onPlace)
        } else {
            Row.Builder().setTitle(label).addText("À définir sur le téléphone").setBrowsable(true).setOnClickListener { onUndefined(label) }.build()
        }

    private fun placeRow(title: String, place: Place, near: LatLng?, onPlace: (Place) -> Unit): Row {
        val builder = Row.Builder().setTitle(title).setOnClickListener { onPlace(place) }
        if (near == null) {
            // Sans position, pas de distance à afficher : la ligne devient un simple lien.
            builder.setBrowsable(true)
            if (place.subtitle.isNotBlank()) builder.addText(place.subtitle)
        } else {
            builder.addText(distanceText(GeoMath.distanceMeters(near, place.position)))
        }
        return builder.build()
    }

    /** Distance mise en forme par Android Auto (marqueur obligatoire dans les listes de lieux). */
    private fun distanceText(meters: Double): CharSequence =
        SpannableString(" ").apply { setSpan(DistanceSpan.create(CarDistanceMapper.map(meters)), 0, 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE) }
}
