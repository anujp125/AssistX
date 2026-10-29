package com.nudge.assist.accessibility

import android.content.Context
import android.content.Intent
import com.nudge.assist.state.AccessibilityState

/**
 * Abstraction for observing Accessibility Service settings and connection state.
 */
interface AccessibilityAdapterContract {
    fun getAccessibilityState(context: Context?): AccessibilityState
    fun isSettingsEnabled(context: Context?): Boolean
    fun isServiceConnected(): Boolean
    fun createSettingsIntent(context: Context?): Intent
}
