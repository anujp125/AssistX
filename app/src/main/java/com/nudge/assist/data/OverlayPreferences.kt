package com.nudge.assist.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "assistx_preferences")

data class OverlayPosition(
    val x: Int = -1,
    val y: Int = -1
)

class OverlayPreferences(private val context: Context) : UserPreferencesContract {

    companion object {
        private val KEY_DESIRED_ENABLED = booleanPreferencesKey("desired_enabled")
        private val KEY_POSITION_X = intPreferencesKey("button_position_x")
        private val KEY_POSITION_Y = intPreferencesKey("button_position_y")

        // User Customization Settings Keys
        private val KEY_BUTTON_SIZE_DP = intPreferencesKey("button_size_dp")
        private val KEY_IDLE_OPACITY_PERCENT = intPreferencesKey("idle_opacity_percent")
        private val KEY_IDLE_FADE_SECONDS = intPreferencesKey("idle_fade_seconds")
        private val KEY_POPOUT_DISMISS_SECONDS = intPreferencesKey("popout_dismiss_seconds")
        private val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback_enabled")
        private val KEY_SNAP_TO_EDGE = booleanPreferencesKey("snap_to_edge_enabled")
    }

    override val desiredEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DESIRED_ENABLED] ?: false
    }

    override val positionFlow: Flow<OverlayPosition> = context.dataStore.data.map { preferences ->
        OverlayPosition(
            x = preferences[KEY_POSITION_X] ?: -1,
            y = preferences[KEY_POSITION_Y] ?: -1
        )
    }

    override val settingsFlow: Flow<OverlaySettings> = context.dataStore.data.map { preferences ->
        OverlaySettings(
            buttonSizeDp = preferences[KEY_BUTTON_SIZE_DP] ?: 56,
            idleOpacityPercent = preferences[KEY_IDLE_OPACITY_PERCENT] ?: 75,
            idleFadeSeconds = preferences[KEY_IDLE_FADE_SECONDS] ?: 3,
            popoutDismissSeconds = preferences[KEY_POPOUT_DISMISS_SECONDS] ?: 4,
            hapticFeedbackEnabled = preferences[KEY_HAPTIC_FEEDBACK] ?: true,
            snapToEdgeEnabled = preferences[KEY_SNAP_TO_EDGE] ?: true
        )
    }

    override suspend fun setDesiredEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DESIRED_ENABLED] = enabled
        }
    }

    override suspend fun savePosition(x: Int, y: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_POSITION_X] = x
            preferences[KEY_POSITION_Y] = y
        }
    }

    override suspend fun updateSettings(settings: OverlaySettings) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BUTTON_SIZE_DP] = settings.buttonSizeDp
            preferences[KEY_IDLE_OPACITY_PERCENT] = settings.idleOpacityPercent
            preferences[KEY_IDLE_FADE_SECONDS] = settings.idleFadeSeconds
            preferences[KEY_POPOUT_DISMISS_SECONDS] = settings.popoutDismissSeconds
            preferences[KEY_HAPTIC_FEEDBACK] = settings.hapticFeedbackEnabled
            preferences[KEY_SNAP_TO_EDGE] = settings.snapToEdgeEnabled
        }
    }

    override suspend fun setButtonSizeDp(sizeDp: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BUTTON_SIZE_DP] = sizeDp
        }
    }

    override suspend fun setIdleOpacityPercent(opacityPercent: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IDLE_OPACITY_PERCENT] = opacityPercent
        }
    }

    override suspend fun setIdleFadeSeconds(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IDLE_FADE_SECONDS] = seconds
        }
    }

    override suspend fun setPopoutDismissSeconds(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_POPOUT_DISMISS_SECONDS] = seconds
        }
    }

    override suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HAPTIC_FEEDBACK] = enabled
        }
    }

    override suspend fun setSnapToEdgeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SNAP_TO_EDGE] = enabled
        }
    }
}

