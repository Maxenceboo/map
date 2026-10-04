package com.gamemaps.irl.data.network

import java.io.IOException

/** Réponse HTTP non 2xx. [url] = hôte + chemin uniquement, jamais la requête (position GPS). */
class HttpException(val code: Int, url: String) : IOException("HTTP $code sur $url")
