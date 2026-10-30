package com.nudge.assist.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
        private val KEY_PAUSED_FOR_PAYMENTS = booleanPreferencesKey("paused_for_payments")
        private val KEY_POSITION_X = intPreferencesKey("button_position_x")
        private val KEY_POSITION_Y = intPreferencesKey("button_position_y")

        // User Customization Settings Keys
        private val KEY_BUTTON_SIZE_DP = intPreferencesKey("button_size_dp")
        private val KEY_IDLE_OPACITY_PERCENT = intPreferencesKey("idle_opacity_percent")
        private val KEY_IDLE_FADE_SECONDS = intPreferencesKey("idle_fade_seconds")
        private val KEY_POPOUT_DISMISS_SECONDS = intPreferencesKey("popout_dismiss_seconds")
        private val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback_enabled")
        private val KEY_SNAP_TO_EDGE = booleanPreferencesKey("snap_to_edge_enabled")
        private val KEY_ACCENT_HEX = stringPreferencesKey("accent_hex")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")

        // Floating Button Customization Keys
        private val KEY_BUTTON_SHAPE = stringPreferencesKey("button_shape")
        private val KEY_BUTTON_STYLE = stringPreferencesKey("button_style")
        private val KEY_BUTTON_COLOR_MODE = stringPreferencesKey("button_color_mode")

        // Popout Array Customization Keys
        private val KEY_POPOUT_BUTTON_SIZE_DP = intPreferencesKey("popout_button_size_dp")
        private val KEY_POPOUT_ORIENTATION = stringPreferencesKey("popout_orientation")
        private val KEY_POPOUT_ITEM_SPACING_DP = intPreferencesKey("popout_item_spacing_dp")
        private val KEY_POPOUT_MARGIN_DP = intPreferencesKey("popout_margin_dp")
        private val KEY_POPOUT_ORDER = stringPreferencesKey("popout_order")

        // Background Capsule Keys
        private val KEY_CAPSULE_ENABLED = booleanPreferencesKey("capsule_enabled")
        private val KEY_CAPSULE_STYLE = stringPreferencesKey("capsule_style")
        private val KEY_CAPSULE_CORNER_RADIUS = stringPreferencesKey("capsule_corner_radius")
    }

    override val desiredEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DESIRED_ENABLED] ?: false
    }

    override val isPausedForPaymentsFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_PAUSED_FOR_PAYMENTS] ?: false
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
            snapToEdgeEnabled = preferences[KEY_SNAP_TO_EDGE] ?: true,
            accentHex = preferences[KEY_ACCENT_HEX] ?: "#0050EF",
            themeMode = preferences[KEY_THEME_MODE] ?: "dark",

            buttonShape = preferences[KEY_BUTTON_SHAPE] ?: "CIRCLE",
            buttonStyle = preferences[KEY_BUTTON_STYLE] ?: "METRO_OUTLINE",
            buttonColorMode = preferences[KEY_BUTTON_COLOR_MODE] ?: "ACCENT",

            popoutButtonSizeDp = preferences[KEY_POPOUT_BUTTON_SIZE_DP] ?: 54,
            popoutOrientation = preferences[KEY_POPOUT_ORIENTATION] ?: "AUTO",
            popoutItemSpacingDp = preferences[KEY_POPOUT_ITEM_SPACING_DP] ?: 10,
            popoutMarginDp = preferences[KEY_POPOUT_MARGIN_DP] ?: 12,
            popoutOrder = preferences[KEY_POPOUT_ORDER] ?: "SCREENSHOT_VOLUME_POWER",

            capsuleEnabled = preferences[KEY_CAPSULE_ENABLED] ?: false,
            capsuleStyle = preferences[KEY_CAPSULE_STYLE] ?: "OBSIDIAN",
            capsuleCornerRadius = preferences[KEY_CAPSULE_CORNER_RADIUS] ?: "PILL"
        )
    }

    override suspend fun setDesiredEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DESIRED_ENABLED] = enabled
            if (enabled) {
                preferences[KEY_PAUSED_FOR_PAYMENTS] = false
            }
        }
    }

    override suspend fun setPausedForPayments(paused: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PAUSED_FOR_PAYMENTS] = paused
            if (paused) {
                preferences[KEY_DESIRED_ENABLED] = false
            }
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
            preferences[KEY_ACCENT_HEX] = settings.accentHex
            preferences[KEY_THEME_MODE] = settings.themeMode

            preferences[KEY_BUTTON_SHAPE] = settings.buttonShape
            preferences[KEY_BUTTON_STYLE] = settings.buttonStyle
            preferences[KEY_BUTTON_COLOR_MODE] = settings.buttonColorMode

            preferences[KEY_POPOUT_BUTTON_SIZE_DP] = settings.popoutButtonSizeDp
            preferences[KEY_POPOUT_ORIENTATION] = settings.popoutOrientation
            preferences[KEY_POPOUT_ITEM_SPACING_DP] = settings.popoutItemSpacingDp
            preferences[KEY_POPOUT_MARGIN_DP] = settings.popoutMarginDp
            preferences[KEY_POPOUT_ORDER] = settings.popoutOrder

            preferences[KEY_CAPSULE_ENABLED] = settings.capsuleEnabled
            preferences[KEY_CAPSULE_STYLE] = settings.capsuleStyle
            preferences[KEY_CAPSULE_CORNER_RADIUS] = settings.capsuleCornerRadius
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

    override suspend fun setAccentHex(hex: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ACCENT_HEX] = hex
        }
    }

    override suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    override suspend fun setButtonShape(shape: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BUTTON_SHAPE] = shape
        }
    }

    override suspend fun setButtonStyle(style: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BUTTON_STYLE] = style
        }
    }

    override suspend fun setButtonColorMode(colorMode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BUTTON_COLOR_MODE] = colorMode
        }
    }

    override suspend fun setPopoutButtonSizeDp(sizeDp: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_POPOUT_BUTTON_SIZE_DP] = sizeDp
        }
    }

    override suspend fun setPopoutOrientation(orientation: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_POPOUT_ORIENTATION] = orientation
        }
    }

    override suspend fun setPopoutItemSpacingDp(spacingDp: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_POPOUT_ITEM_SPACING_DP] = spacingDp
        }
    }

    override suspend fun setPopoutMarginDp(marginDp: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_POPOUT_MARGIN_DP] = marginDp
        }
    }

    override suspend fun setPopoutOrder(order: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_POPOUT_ORDER] = order
        }
    }

    override suspend fun setCapsuleEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CAPSULE_ENABLED] = enabled
        }
    }

    override suspend fun setCapsuleStyle(style: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CAPSULE_STYLE] = style
        }
    }

    override suspend fun setCapsuleCornerRadius(radius: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CAPSULE_CORNER_RADIUS] = radius
        }
    }
}
