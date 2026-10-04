package com.gamemaps.irl.data.network

import java.io.IOException

/** Réponse HTTP non 2xx. [host] = nom du serveur uniquement, jamais le chemin ni la requête (position GPS, clé d'API). */
class HttpException(val code: Int, host: String) : IOException("HTTP $code sur $host")
