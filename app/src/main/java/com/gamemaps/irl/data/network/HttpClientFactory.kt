package com.gamemaps.irl.data.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/** Crée le client HTTP unique de l'application. */
object HttpClientFactory {

    private const val USER_AGENT = "GameMapsIRL/0.1 (Android; https://github.com/Maxenceboo/map)"

    fun create(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(UserAgentInterceptor(USER_AGENT))
        .build()
}
