package com.gamemaps.irl.data.network

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * GET asynchrone qui renvoie le corps en texte.
 * Annuler la coroutine annule aussi la requête réseau (utile quand l'utilisateur tape vite).
 */
suspend fun OkHttpClient.getText(url: HttpUrl): String = suspendCancellableCoroutine { continuation ->
    val call = newCall(Request.Builder().url(url).get().build())
    continuation.invokeOnCancellation { call.cancel() }
    call.enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            continuation.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                if (!it.isSuccessful) {
                    continuation.resumeWithException(HttpException(it.code, url.toString()))
                } else {
                    continuation.resume(it.body?.string().orEmpty())
                }
            }
        }
    })
}
