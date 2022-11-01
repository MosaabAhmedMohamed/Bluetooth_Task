package com.example.presentation.central.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.example.domain.central.repository.CentralRepository
import com.example.presentation.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CentralService : LifecycleService() {



    @Inject
    lateinit var userService: CentralRepository

    private lateinit var notificationManager: NotificationManager
    private val binder = LocalBinder()

    private val channelID = "Central Notification"
    private val centralNotificationId = 12345678
    /**
     * Returns the [NotificationCompat] used as part of the foreground service.
     */
    private val notification: Notification by lazy {
        NotificationCompat.Builder(this, channelID)
            .apply {
                setContentText(getString(R.string.text_state))
                    .setContentTitle(getString(R.string.app_name))
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    //.setSmallIcon(R.mipmap.ic_launcher)
                    .setSound(null)
                    .setOnlyAlertOnce(true)
                    .setTicker(getString(R.string.app_name))
                    .setWhen(System.currentTimeMillis())
            }.build()
    }


    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            // Create the channel for the notification
            val mChannel = NotificationChannel(channelID, name, NotificationManager.IMPORTANCE_DEFAULT)
            mChannel.setSound(null, null)

            // Set the Notification Channel for the Notification Manager.
            notificationManager.createNotificationChannel(mChannel)
        }
        startForeground(centralNotificationId, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        // Tells the system try to recreate the service after it has been killed.
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        stopForeground(true)
        return binder
    }

    override fun onRebind(intent: Intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        stopForeground(true)
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        startForeground(centralNotificationId, notification)
        return true // Ensures onRebind() is called when a client re-binds.
    }


    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        val service: CentralService = this@CentralService
    }
}