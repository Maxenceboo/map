package com.gamemaps.irl;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.car.app.Screen;
import androidx.car.app.Session;

/**
 * Session de connexion entre le smartphone et l'écran de la voiture.
 */
public class GameGpsCarSession extends Session {

    @NonNull
    @Override
    public Screen onCreateScreen(@NonNull Intent intent) {
        return new GameGpsCarScreen(getCarContext());
    }
}
