package com.assistx.service

import android.accessibilityservice.AccessibilityService
import android.content.res.Configuration
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import com.assistx.AssistApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AssistAccessibilityService : AccessibilityService() {

    companion object {
        private val _isConnected = MutableStateFlow(false)
        val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

        private var instance: AssistAccessibilityService? = null

        fun setConnectedForTesting(connected: Boolean) {
            _isConnected.value = connected
        }

        fun lockScreen(): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                instance?.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN) ?: false
            } else {
                false
            }
        }

        fun takeScreenshot(): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val res = instance?.performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT) ?: false
                android.util.Log.i("AssistX", "takeScreenshot performed, result: $res")
                res
            } else {
                false
            }
        }

        fun openPowerDialog(): Boolean {
            val res = instance?.performGlobalAction(GLOBAL_ACTION_POWER_DIALOG) ?: false
            android.util.Log.i("AssistX", "openPowerDialog performed, result: $res")
            return res
        }

        fun performHome(): Boolean {
            return instance?.performGlobalAction(GLOBAL_ACTION_HOME) ?: false
        }

        fun performBack(): Boolean {
            return instance?.performGlobalAction(GLOBAL_ACTION_BACK) ?: false
        }

        fun disableSelfSafely(): Boolean {
            val active = instance ?: return false
            return try {
                active.disableSelf()
                instance = null
                _isConnected.value = false
                android.util.Log.i("AssistX", "AssistAccessibilityService: disableSelf executed successfully")
                true
            } catch (e: Exception) {
                android.util.Log.e("AssistX", "AssistAccessibilityService: disableSelf failed", e)
                false
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isConnected.value = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No event interception required for AssistX
    }

    override fun onInterrupt() {
        android.util.Log.w("AssistX", "AssistAccessibilityService: System interrupted accessibility service")
        _isConnected.value = false
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        try {
            AssistApplication.instance.overlayManager.onConfigurationChanged()
        } catch (e: Exception) {
            // Ignored during rotation
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
        _isConnected.value = false
    }
}

