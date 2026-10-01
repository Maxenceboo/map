package com.gamemaps.irl.data.network

import java.io.IOException

/** Réponse HTTP non 2xx. */
class HttpException(val code: Int, url: String) : IOException("HTTP $code sur $url")
