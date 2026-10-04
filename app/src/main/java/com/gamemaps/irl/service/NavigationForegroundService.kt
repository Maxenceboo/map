package com.gamemaps.irl.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.gamemaps.irl.di.appContainer
import com.gamemaps.irl.service.notification.NavigationNotificationBuilder
import com.gamemaps.irl.service.notification.NavigationNotificationChannel
import com.gamemaps.irl.service.notification.NavigationNotificationContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Service "au premier plan" de type localisation, actif pendant un trajet.
 *
 * Tant qu'il tourne, Android garde l'application en vie et continue de lui donner la position,
 * même écran éteint ou app en arrière-plan : le guidage et la voix continuent.
 * Il affiche une notification permanente (prochaine manœuvre, arrivée, bouton "Arrêter").
 */
class NavigationForegroundService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var observeJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            appContainer.navigationEngine.stop() // Le contrôleur arrêtera ensuite ce service.
            return START_NOT_STICKY
        }
        NavigationNotificationChannel.ensureCreated(this)
        val initial = NavigationNotificationContent.from(appContainer.navigationEngine.state.value)
        ServiceCompat.startForeground(this, NOTIFICATION_ID, NavigationNotificationBuilder.build(this, initial), foregroundType())
        observeNavigation()
        return START_NOT_STICKY
    }

    /** Met la notification à jour seulement quand son texte change (pas à chaque position GPS). */
    private fun observeNavigation() {
        observeJob?.cancel() // onStartCommand peut être appelé plusieurs fois.
        val manager = getSystemService(NotificationManager::class.java)
        observeJob = scope.launch {
            appContainer.navigationEngine.state
                .map { NavigationNotificationContent.from(it) }
                .distinctUntilChanged()
                .collect { manager.notify(NOTIFICATION_ID, NavigationNotificationBuilder.build(this@NavigationForegroundService, it)) }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun foregroundType(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0

    companion object {
        private const val NOTIFICATION_ID = 42
        private const val ACTION_STOP = "com.gamemaps.irl.action.STOP_NAVIGATION"

        fun startIntent(context: Context) = Intent(context, NavigationForegroundService::class.java)

        fun stopIntent(context: Context) = Intent(context, NavigationForegroundService::class.java).setAction(ACTION_STOP)
    }
}
