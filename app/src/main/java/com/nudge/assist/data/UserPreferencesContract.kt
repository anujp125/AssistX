package com.nudge.assist.data

import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for persistent user preferences.
 */
interface UserPreferencesContract {
    val desiredEnabledFlow: Flow<Boolean>
    val positionFlow: Flow<OverlayPosition>
    suspend fun setDesiredEnabled(enabled: Boolean)
    suspend fun savePosition(x: Int, y: Int)
}

