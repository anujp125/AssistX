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

class OverlayPreferences(private val context: Context) {

    companion object {
        private val KEY_DESIRED_ENABLED = booleanPreferencesKey("desired_enabled")
        private val KEY_POSITION_X = intPreferencesKey("button_position_x")
        private val KEY_POSITION_Y = intPreferencesKey("button_position_y")
    }

    val desiredEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DESIRED_ENABLED] ?: false
    }

    val positionFlow: Flow<OverlayPosition> = context.dataStore.data.map { preferences ->
        OverlayPosition(
            x = preferences[KEY_POSITION_X] ?: -1,
            y = preferences[KEY_POSITION_Y] ?: -1
        )
    }

    suspend fun setDesiredEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DESIRED_ENABLED] = enabled
        }
    }

    suspend fun savePosition(x: Int, y: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_POSITION_X] = x
            preferences[KEY_POSITION_Y] = y
        }
    }
}

