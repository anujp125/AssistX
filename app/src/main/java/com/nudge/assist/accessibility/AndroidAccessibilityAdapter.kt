package com.nudge.assist.accessibility

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.nudge.assist.service.AssistAccessibilityService
import com.nudge.assist.state.AccessibilityState

/**
 * Concrete implementation that inspects Android settings and service binding state.
 */
class AndroidAccessibilityAdapter : AccessibilityAdapterContract {

    override fun getAccessibilityState(context: Context?): AccessibilityState {
        if (context == null) return AccessibilityState.UNKNOWN
        val enabledInSettings = isSettingsEnabled(context)
        val connected = isServiceConnected()

        return when {
            !enabledInSettings -> AccessibilityState.DISABLED
            connected -> AccessibilityState.ENABLED_CONNECTED
            else -> AccessibilityState.ENABLED_DISCONNECTED
        }
    }

    override fun isSettingsEnabled(context: Context?): Boolean {
        if (context == null) return false
        return try {
            val expectedComponentName = ComponentName(context, AssistAccessibilityService::class.java).flattenToString()
            val shortComponentName = ComponentName(context, AssistAccessibilityService::class.java).flattenToShortString()
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            enabledServices.split(":").any {
                it.equals(expectedComponentName, ignoreCase = true) ||
                        it.equals(shortComponentName, ignoreCase = true)
            }
        } catch (e: Exception) {
            false
        }
    }

    override fun isServiceConnected(): Boolean {
        return AssistAccessibilityService.isConnected.value
    }

    override fun createSettingsIntent(context: Context?): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
