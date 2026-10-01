package com.gamemaps.irl.car

import android.content.pm.ApplicationInfo
import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

/**
 * Point d'entrée Android Auto, déclaré dans le manifeste (catégorie NAVIGATION).
 * Android Auto se connecte à ce service puis demande une [Session].
 */
class GameMapsCarAppService : CarAppService() {

    /**
     * Qui a le droit d'afficher notre app ?
     * En debug : tout le monde (indispensable pour le Desktop Head Unit).
     * En release : uniquement les hôtes officiels (Android Auto, Android Automotive).
     */
    override fun createHostValidator(): HostValidator =
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
        } else {
            HostValidator.Builder(applicationContext)
                .addAllowedHosts(androidx.car.app.R.array.hosts_allowlist_sample)
                .build()
        }

    override fun onCreateSession(): Session = GameMapsCarSession()
}
