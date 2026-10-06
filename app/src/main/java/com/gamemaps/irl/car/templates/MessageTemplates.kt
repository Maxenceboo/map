package com.gamemaps.irl.car.templates

import androidx.car.app.model.Action
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template

/** Écrans de message simples : arrivée, erreur, permission manquante. */
object MessageTemplates {

    fun arrived(destinationName: String, summary: String, onDone: () -> Unit): Template =
        message("Mission accomplie", "$destinationName\n$summary", CarActions.button("Terminer", onDone))

    fun failed(reason: String, onRetry: () -> Unit, onDone: () -> Unit): Template =
        message(
            "Itinéraire impossible",
            reason,
            CarActions.button("Réessayer", onRetry),
            CarActions.button("Fermer", onDone),
        )

    fun permissionRequired(onRequest: () -> Unit): Template =
        message(
            "Localisation requise",
            "Autorisez l'accès à la position sur votre téléphone.",
            CarActions.button("Autoriser", onRequest),
        )

    private fun message(title: String, text: String, vararg actions: Action): Template =
        MessageTemplate.Builder(text)
            .setTitle(title)
            .setHeaderAction(Action.APP_ICON)
            .apply { actions.forEach { addAction(it) } }
            .build()
}
