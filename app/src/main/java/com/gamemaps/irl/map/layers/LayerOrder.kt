package com.gamemaps.irl.map.layers

import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.SymbolLayer

/** Aide au placement des calques dans la pile du style. */
object LayerOrder {

    /** Premier calque de texte : ce qu'on insère "en dessous" reste sous les noms de rues. */
    fun firstSymbolLayerId(style: Style): String? = style.layers.firstOrNull { it is SymbolLayer }?.id
}
