package com.gamemaps.irl.car.screens

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.constraints.ConstraintManager
import androidx.car.app.model.Action
import androidx.car.app.model.SearchTemplate
import androidx.car.app.model.Template
import androidx.lifecycle.lifecycleScope
import com.gamemaps.irl.car.templates.PlaceListBuilder
import com.gamemaps.irl.data.search.Place
import com.gamemaps.irl.di.AppContainer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Recherche de destination depuis la voiture (clavier ou dictée fournis par Android Auto).
 * [initialQuery] : texte déjà demandé à l'assistant, cherché dès l'ouverture.
 * Choisir un résultat lance le guidage et revient à l'écran de navigation.
 */
class CarSearchScreen(
    carContext: CarContext,
    private val container: AppContainer,
    private val initialQuery: String = "",
) : Screen(carContext) {

    private var query = ""
    private var results: List<Place> = emptyList()
    private var isLoading = false

    /** La dernière recherche a échoué (réseau) : à ne pas confondre avec "aucun résultat". */
    private var failed = false
    private var searchJob: Job? = null

    private val maxItems: Int =
        carContext.getCarService(ConstraintManager::class.java)
            .getContentLimit(ConstraintManager.CONTENT_LIMIT_TYPE_LIST)

    override fun onGetTemplate(): Template {
        val builder = SearchTemplate.Builder(object : SearchTemplate.SearchCallback {
            override fun onSearchTextChanged(searchText: String) = search(searchText)
            override fun onSearchSubmitted(searchText: String) = search(searchText)
        })
            .setHeaderAction(Action.BACK)
            .setSearchHint("Où aller ?")
            // Avec une demande vocale, on montre directement les résultats plutôt que le clavier.
            .setShowKeyboardByDefault(initialQuery.isBlank())
            .apply { if (initialQuery.isNotBlank()) setInitialSearchText(initialQuery) }

        // Un SearchTemplate ne peut pas être "en chargement" et avoir une liste à la fois.
        when {
            isLoading -> builder.setLoading(true)
            query.isBlank() -> builder.setItemList(
                PlaceListBuilder.buildSaved(container.savedPlacesRepository.saved.value, position, maxItems, ::onPlaceSelected),
            )
            else -> builder.setItemList(
                PlaceListBuilder.build(
                    places = results,
                    near = position,
                    maxItems = maxItems,
                    emptyMessage = if (failed) "Recherche impossible. Vérifiez la connexion du téléphone." else "Aucun résultat",
                    onSelect = ::onPlaceSelected,
                ),
            )
        }
        return builder.build()
    }

    private val position get() = container.locationRepository.fixes.value?.position

    init {
        if (initialQuery.isNotBlank()) search(initialQuery)
    }

    private fun search(text: String) {
        if (text == query && (isLoading || results.isNotEmpty())) return // même texte renvoyé par l'hôte
        query = text
        searchJob?.cancel()
        if (text.isBlank()) {
            results = emptyList()
            isLoading = false
            invalidate()
            return
        }
        searchJob = lifecycleScope.launch {
            delay(DEBOUNCE_MS)
            isLoading = true
            invalidate()
            val found = try {
                container.placeSearch.search(text, position)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null // réseau absent ou serveur injoignable
            }
            failed = found == null
            results = found.orEmpty()
            isLoading = false
            invalidate()
        }
    }

    private fun onPlaceSelected(place: Place) {
        container.navigationEngine.start(place)
        screenManager.pop()
    }

    private companion object {
        const val DEBOUNCE_MS = 400L
    }
}
