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

    /** Trajet en un appui vers la maison. */
    fun home(onClick: () -> Unit): Action = iconButton(CarIcons.home, onClick)

    /** Trajet en un appui vers le travail. */
    fun work(onClick: () -> Unit): Action = iconButton(CarIcons.work, onClick)

    private fun iconButton(icon: CarIcon, onClick: () -> Unit): Action =
        Action.Builder().setIcon(icon).setOnClickListener { onClick() }.build()

    /** Ouvre la recherche de destination (clavier ou dictée). */
    fun search(onClick: () -> Unit): Action = button("Où aller ?", onClick)

    /** Bascule son / muet : le titre indique l'action disponible. */
    fun muteToggle(isMuted: Boolean, onToggle: () -> Unit): Action =
        button(if (isMuted) "Son" else "Muet", onToggle)

    fun strip(vararg actions: Action): ActionStrip =
        ActionStrip.Builder().apply { actions.forEach { addAction(it) } }.build()
}
