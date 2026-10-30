package com.nudge.assist.accessibility

/**
 * Centralized classification of all AssistX gesture, popout, and system actions,
 * along with their explicit Accessibility Service dependencies.
 */
enum class AssistAction(
    val id: String,
    val displayName: String,
    val requiresAccessibility: Boolean
) {
    /**
     * Primary action: Screen Lock / Sleep via GLOBAL_ACTION_LOCK_SCREEN.
     */
    LOCK_SCREEN(
        id = "LOCK_SCREEN",
        displayName = "Screen off",
        requiresAccessibility = true
    ),

    /**
     * Navigation action: Go Home via GLOBAL_ACTION_HOME.
     */
    HOME(
        id = "HOME",
        displayName = "Home",
        requiresAccessibility = true
    ),

    /**
     * Navigation action: Back via GLOBAL_ACTION_BACK.
     */
    BACK(
        id = "BACK",
        displayName = "Back",
        requiresAccessibility = true
    ),

    /**
     * Navigation action: Recent Apps via GLOBAL_ACTION_RECENTS.
     */
    RECENT_APPS(
        id = "RECENT_APPS",
        displayName = "Recent apps",
        requiresAccessibility = true
    ),

    /**
     * System action: Screenshot capture via GLOBAL_ACTION_TAKE_SCREENSHOT.
     */
    TAKE_SCREENSHOT(
        id = "TAKE_SCREENSHOT",
        displayName = "Screenshot",
        requiresAccessibility = true
    ),

    /**
     * System action: System power / lock menu via GLOBAL_ACTION_POWER_DIALOG.
     */
    POWER_DIALOG(
        id = "POWER_DIALOG",
        displayName = "Power dialog",
        requiresAccessibility = true
    ),

    /**
     * System action: Adjust volume slider via AudioManager (independent of Accessibility).
     */
    VOLUME_CONTROL(
        id = "VOLUME_CONTROL",
        displayName = "Volume controls",
        requiresAccessibility = false
    ),

    /**
     * Overlay action: Open / dismiss action buttons popout array.
     */
    TOGGLE_POPOUT(
        id = "TOGGLE_POPOUT",
        displayName = "Action popout",
        requiresAccessibility = false
    ),

    /**
     * Overlay action: Drag and reposition floating button across screen.
     */
    DRAG_REPOSITION(
        id = "DRAG_REPOSITION",
        displayName = "Drag and reposition",
        requiresAccessibility = false
    ),

    /**
     * Overlay action: Magnetic edge snapping upon release.
     */
    EDGE_SNAP(
        id = "EDGE_SNAP",
        displayName = "Edge snap",
        requiresAccessibility = false
    ),

    /**
     * Overlay action: Idle opacity fading after inactivity delay.
     */
    IDLE_FADE(
        id = "IDLE_FADE",
        displayName = "Idle fade",
        requiresAccessibility = false
    ),

    /**
     * Security action: Payment safety mode pause.
     */
    PAYMENT_SAFETY_MODE(
        id = "PAYMENT_SAFETY_MODE",
        displayName = "Payment safety mode",
        requiresAccessibility = false
    );

    companion object {
        fun requiresAccessibility(action: AssistAction): Boolean = action.requiresAccessibility
    }
}

