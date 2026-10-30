package com.assistx.accessibility

import android.content.Context
import android.content.Intent
import com.assistx.state.AccessibilityState
import kotlinx.coroutines.flow.StateFlow

/**
 * Abstraction for observing Accessibility Service settings and connection state.
 */
interface AccessibilityAdapterContract {
    fun getAccessibilityState(context: Context?): AccessibilityState
    fun isSettingsEnabled(context: Context?): Boolean
    fun isServiceConnected(): Boolean
    fun createSettingsIntent(context: Context?): Intent
}

/**
 * Centralized Accessibility State Manager contract.
 * Exposes real-time observable state, synchronous validation, and settings launcher.
 */
interface AccessibilityStateManagerContract : AccessibilityAdapterContract {
    val state: StateFlow<AccessibilityState>
    fun checkCurrentState(context: Context? = null): AccessibilityState
    fun openAccessibilitySettings(context: Context): Boolean
}


