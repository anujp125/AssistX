package com.nudge.assist.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.nudge.assist.AssistApplication
import com.nudge.assist.R
import com.nudge.assist.ui.MainActivity

class AssistForegroundService : Service() {

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    android.util.Log.i("AssistX", "AssistForegroundService: ACTION_SCREEN_OFF received")
                    try {
                        AssistApplication.instance.overlayManager.onScreenTurnedOff()
                    } catch (e: Exception) {
                        android.util.Log.w("AssistX", "Error handling screen off in OverlayManager", e)
                    }
                }
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                    android.util.Log.i("AssistX", "AssistForegroundService: Screen wake received (${intent.action})")
                    try {
                        AssistApplication.instance.overlayManager.onScreenWoken()
                    } catch (e: Exception) {
                        android.util.Log.w("AssistX", "Error handling screen wake in OverlayManager", e)
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenStateReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(screenStateReceiver)
        } catch (e: Exception) {
            // Unregistered
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        try {
            AssistApplication.instance.overlayManager.onConfigurationChanged()
        } catch (e: Exception) {
            // Ignored during orientation transition
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else -> {
                startForeground(NOTIFICATION_ID, buildNotification())
            }
        }
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java).let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }

        return NotificationCompat.Builder(this, AssistApplication.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_content))
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
}

