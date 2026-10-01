package com.gamemaps.irl.di

import android.content.Context
import com.gamemaps.irl.GameMapsApp

/** Accès au conteneur depuis n'importe quel Context (Activity, CarContext...). */
val Context.appContainer: AppContainer
    get() = (applicationContext as GameMapsApp).container
