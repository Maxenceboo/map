package com.gamemaps.irl.data.search.ranking

import java.text.Normalizer

/** Compare des textes sans se soucier des accents, majuscules et tirets : "Saint-Jean" ≈ "saint jean". */
object TextNormalizer {

    private val DIACRITICS = Regex("\\p{Mn}+")
    private val SEPARATORS = Regex("[\\s\\-'’,.]+")

    fun normalize(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(DIACRITICS, "")
            .lowercase()
            .replace(SEPARATORS, " ")
            .trim()

    fun tokens(text: String): List<String> = normalize(text).split(' ').filter { it.isNotBlank() }
}
