package com.assistx.service

import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Abstraction for starting and stopping the foreground service.
 */
interface ServiceLauncher {
    fun startForegroundService(): Boolean
    fun stopForegroundService(): Boolean
}

class AndroidServiceLauncher(private val context: Context?) : ServiceLauncher {
    override fun startForegroundService(): Boolean {
        if (context == null) return false
        return try {
            val serviceIntent = Intent(context, AssistForegroundService::class.java).apply {
                action = AssistForegroundService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun stopForegroundService(): Boolean {
        if (context == null) return false
        return try {
            val serviceIntent = Intent(context, AssistForegroundService::class.java).apply {
                action = AssistForegroundService.ACTION_STOP
            }
            context.startService(serviceIntent)
            true
        } catch (e: Exception) {
            false
        }
    }
}

