package com.gamemaps.irl.data.network

import okhttp3.Interceptor
import okhttp3.Response

/** Ajoute un User-Agent identifiable : demandé par les services publics (OSRM, OSM, IGN). */
class UserAgentInterceptor(private val userAgent: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(chain.request().newBuilder().header("User-Agent", userAgent).build())
}
