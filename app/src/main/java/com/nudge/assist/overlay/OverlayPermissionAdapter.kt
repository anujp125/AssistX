package com.nudge.assist.overlay

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.nudge.assist.state.OverlayPermissionState

/**
 * Encapsulates checking Android's special overlay permission (SYSTEM_ALERT_WINDOW)
 * and creating deep-link navigation intents to the system settings page.
 */
interface OverlayPermissionAdapter {
    /** Checks the current overlay permission state. */
    fun checkPermission(context: Context?): OverlayPermissionState

    /** Creates an Intent leading directly to the overlay permission toggle for AssistX. */
    fun createSettingsIntent(context: Context?): Intent
}

class AndroidOverlayPermissionAdapter : OverlayPermissionAdapter {

    override fun checkPermission(context: Context?): OverlayPermissionState {
        if (context == null) return OverlayPermissionState.UNKNOWN
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(context)) {
                    OverlayPermissionState.GRANTED
                } else {
                    OverlayPermissionState.REQUIRED
                }
            } else {
                // Prior to Marshmallow, permission is granted at install time
                OverlayPermissionState.GRANTED
            }
        } catch (e: Exception) {
            OverlayPermissionState.UNKNOWN
        }
    }

    override fun createSettingsIntent(context: Context?): Intent {
        val pkg = context?.packageName
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && pkg != null) {
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$pkg")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }
}
