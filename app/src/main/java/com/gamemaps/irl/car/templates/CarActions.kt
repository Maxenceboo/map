package com.gamemaps.irl.car.templates

import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon

/** Boutons réutilisés par plusieurs templates. */
object CarActions {

    fun button(title: String, onClick: () -> Unit): Action =
        Action.Builder().setTitle(title).setOnClickListener { onClick() }.build()

    /** Croix rouge : annule le trajet (calcul, aperçu ou guidage en cours). */
    fun cancelTrip(onClick: () -> Unit): Action =
        iconButton(CarIcons.close, onClick)

    private fun iconButton(icon: CarIcon, onClick: () -> Unit): Action =
        Action.Builder().setIcon(icon).setOnClickListener { onClick() }.build()

    /** Ouvre la recherche de destination (clavier ou dictée). */
    fun search(onClick: () -> Unit): Action = iconButton(CarIcons.searchButton, onClick)

    /** Son : l'icône montre l'état actuel (haut-parleur, ou haut-parleur barré en rouge) ; un appui le change. */
    fun muteToggle(isMuted: Boolean, onToggle: () -> Unit): Action =
        iconButton(if (isMuted) CarIcons.volumeOff else CarIcons.volumeOn, onToggle)

    fun strip(vararg actions: Action): ActionStrip =
        ActionStrip.Builder().apply { actions.forEach { addAction(it) } }.build()
}
