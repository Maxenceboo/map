package com.gamemaps.irl.car.templates

import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip

/** Boutons réutilisés par plusieurs templates. */
object CarActions {

    fun button(title: String, onClick: () -> Unit): Action =
        Action.Builder().setTitle(title).setOnClickListener { onClick() }.build()

    /** Bascule son / muet : le titre indique l'action disponible. */
    fun muteToggle(isMuted: Boolean, onToggle: () -> Unit): Action =
        button(if (isMuted) "Son" else "Muet", onToggle)

    fun strip(vararg actions: Action): ActionStrip =
        ActionStrip.Builder().apply { actions.forEach { addAction(it) } }.build()
}
