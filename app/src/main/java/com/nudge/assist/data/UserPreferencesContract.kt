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
    val snapToEdgeEnabled: Boolean = true,
    val accentHex: String = "#0050EF",
    val themeMode: String = "dark",

    // Floating Button Customization
    val buttonShape: String = "CIRCLE",          // CIRCLE, SQUARE, ROUNDED
    val buttonStyle: String = "METRO_OUTLINE",   // METRO_OUTLINE, SOLID_ACCENT, MONOLINE_RING
    val buttonColorMode: String = "ACCENT",      // ACCENT (follow theme) or specific hex

    // Popout Action Buttons Array Settings
    val popoutButtonSizeDp: Int = 54,            // 46, 54, 62
    val popoutOrientation: String = "AUTO",      // AUTO, DOWN, UP
    val popoutItemSpacingDp: Int = 10,           // 6, 10, 14, 18
    val popoutMarginDp: Int = 12,                // 8, 12, 16, 20
    val popoutOrder: String = "SCREENSHOT_VOLUME_POWER", // SCREENSHOT_VOLUME_POWER, POWER_VOLUME_SCREENSHOT, VOLUME_SCREENSHOT_POWER

    // Popout Background Capsule Element
    val capsuleEnabled: Boolean = false,
    val capsuleStyle: String = "OBSIDIAN",       // OBSIDIAN, METRO_BLACK, SOLID_ACCENT, OUTLINE_ONLY
    val capsuleCornerRadius: String = "PILL"     // PILL, METRO_SQUARE, ROUNDED
)

/**
 * Abstraction for persistent user preferences.
 */
interface UserPreferencesContract {
    val desiredEnabledFlow: Flow<Boolean>
    val isPausedForPaymentsFlow: Flow<Boolean>
    val positionFlow: Flow<OverlayPosition>
    val settingsFlow: Flow<OverlaySettings>

    suspend fun setDesiredEnabled(enabled: Boolean)
    suspend fun setPausedForPayments(paused: Boolean)
    suspend fun savePosition(x: Int, y: Int)
    suspend fun updateSettings(settings: OverlaySettings)
    suspend fun setButtonSizeDp(sizeDp: Int)
    suspend fun setIdleOpacityPercent(opacityPercent: Int)
    suspend fun setIdleFadeSeconds(seconds: Int)
    suspend fun setPopoutDismissSeconds(seconds: Int)
    suspend fun setHapticFeedbackEnabled(enabled: Boolean)
    suspend fun setSnapToEdgeEnabled(enabled: Boolean)
    suspend fun setAccentHex(hex: String)
    suspend fun setThemeMode(mode: String)

    // Floating Button Customization
    suspend fun setButtonShape(shape: String)
    suspend fun setButtonStyle(style: String)
    suspend fun setButtonColorMode(colorMode: String)

    // Popout Array Customization
    suspend fun setPopoutButtonSizeDp(sizeDp: Int)
    suspend fun setPopoutOrientation(orientation: String)
    suspend fun setPopoutItemSpacingDp(spacingDp: Int)
    suspend fun setPopoutMarginDp(marginDp: Int)
    suspend fun setPopoutOrder(order: String)

    // Background Capsule Element
    suspend fun setCapsuleEnabled(enabled: Boolean)
    suspend fun setCapsuleStyle(style: String)
    suspend fun setCapsuleCornerRadius(radius: String)
}
