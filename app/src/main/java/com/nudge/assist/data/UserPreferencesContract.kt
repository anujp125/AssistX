package com.nudge.assist.data

import kotlinx.coroutines.flow.Flow

/**
 * Encapsulates user-configurable preferences for the floating assistant overlay and popouts.
 */
data class OverlaySettings(
    val buttonSizeDp: Int = 56,
    val idleOpacityPercent: Int = 75,
    val idleFadeSeconds: Int = 3,
    val popoutDismissSeconds: Int = 4,
    val hapticFeedbackEnabled: Boolean = true,
    val snapToEdgeEnabled: Boolean = true
)

/**
 * Abstraction for persistent user preferences.
 */
interface UserPreferencesContract {
    val desiredEnabledFlow: Flow<Boolean>
    val positionFlow: Flow<OverlayPosition>
    val settingsFlow: Flow<OverlaySettings>

    suspend fun setDesiredEnabled(enabled: Boolean)
    suspend fun savePosition(x: Int, y: Int)
    suspend fun updateSettings(settings: OverlaySettings)
    suspend fun setButtonSizeDp(sizeDp: Int)
    suspend fun setIdleOpacityPercent(opacityPercent: Int)
    suspend fun setIdleFadeSeconds(seconds: Int)
    suspend fun setPopoutDismissSeconds(seconds: Int)
    suspend fun setHapticFeedbackEnabled(enabled: Boolean)
    suspend fun setSnapToEdgeEnabled(enabled: Boolean)
}


