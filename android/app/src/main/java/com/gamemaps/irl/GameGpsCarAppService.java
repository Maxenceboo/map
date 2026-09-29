package com.gamemaps.irl;

import androidx.annotation.NonNull;
import androidx.car.app.CarAppService;
import androidx.car.app.Session;
import androidx.car.app.validation.HostValidator;

/**
 * Service Android Auto officiel pour Game Maps IRL.
 * Détecté automatiquement par l'autoradio de la voiture lors du branchement USB ou sans-fil.
 */
public class GameGpsCarAppService extends CarAppService {

    @NonNull
    @Override
    public HostValidator createHostValidator() {
        // Permet l'exécution sur Android Auto en mode développeur (Sources inconnues)
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR;
    }

    @NonNull
    @Override
    public Session onCreateSession() {
        return new GameGpsCarSession();
    }
}
