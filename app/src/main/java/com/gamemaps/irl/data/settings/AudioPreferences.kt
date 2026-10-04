package com.gamemaps.irl.data.settings

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Préférence "son coupé", mémorisée entre deux lancements de l'application. */
class AudioPreferences(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    private val _muted = MutableStateFlow(prefs.getBoolean(KEY_MUTED, false))
    val muted: StateFlow<Boolean> = _muted.asStateFlow()

    fun toggleMuted() {
        val newValue = !_muted.value
        prefs.edit().putBoolean(KEY_MUTED, newValue).apply()
        _muted.value = newValue
    }

    private companion object {
        const val FILE_NAME = "audio"
        const val KEY_MUTED = "muted"
    }
}
