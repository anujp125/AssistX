package com.nudge.assist.state

/**
 * Represents the user's explicit saved intention regarding whether AssistX should be running.
 * This is an expression of intent, distinct from whether runtime components are currently healthy.
 */
enum class DesiredState {
    /** The user wants AssistX to be active. */
    ENABLED,

    /** The user has explicitly turned AssistX off. Automatic recovery must be suppressed. */
    DISABLED,

    /** The user has explicitly paused AssistX for payment apps. Overlay is detached, Accessibility disabled. */
    PAUSED_FOR_PAYMENTS
}

/**
 * Observed state of Android's special SYSTEM_ALERT_WINDOW permission.
 */
enum class OverlayPermissionState {
    /** System confirms the overlay permission is granted. */
    GRANTED,

    /** System reports the overlay permission is missing / revoked. */
    REQUIRED,

    /** Permission status has not yet been verified or is being queried. */
    UNKNOWN
}

/**
 * Physical window attachment status of the floating button overlay.
 */
enum class OverlayState {
    /** The overlay window is not currently attached to WindowManager. */
    DETACHED,

    /** Attachment to WindowManager is currently in progress. */
    ATTACHING,

    /** WindowManager confirms the single overlay window is attached and visible. */
    ATTACHED,

    /** Overlay window is currently being removed from WindowManager. */
    DETACHING,

    /** An error occurred during attach/detach operations (e.g. BadTokenException). */
    ERROR,

    /** Attachment status is currently unverified. */
    UNKNOWN
}

/**
 * Overall runtime lifecycle state of the managed assistant process and services.
 */
enum class RuntimeState {
    /** Components are stopped or inactive. */
    STOPPED,

    /** Component startup / initialization is actively in progress. */
    STARTING,

    /** Components are actively operating according to observations. */
    ACTIVE,

    /** Controlled shutdown is in progress. */
    STOPPING,

    /** An unexpected interruption (e.g. memory pressure kill or crash) occurred. */
    INTERRUPTED,

    /** A bounded automatic recovery attempt is actively executing. */
    RECOVERING,

    /** Startup or recovery failed and retry attempts are exhausted. */
    FAILED,

    /** Runtime health cannot be verified. */
    UNKNOWN
}

/**
 * Connection and enablement status of AssistAccessibilityService.
 */
enum class AccessibilityState {
    /** Accessibility is not required for the configured actions. */
    NOT_REQUIRED,

    /** Accessibility setting is disabled in Android System Settings. */
    DISABLED,

    /** Setting is enabled AND the active service instance is bound and responsive. */
    ENABLED_CONNECTED,

    /** Setting is enabled in Android Settings, but the service is currently unbound/disconnected. */
    ENABLED_DISCONNECTED,

    /** Accessibility state is actively being verified. */
    CHECKING,

    /** Accessibility state is unverified. */
    UNKNOWN;

    val isConnected: Boolean get() = this == ENABLED_CONNECTED
    val isEnabledInSettings: Boolean get() = this == ENABLED_CONNECTED || this == ENABLED_DISCONNECTED
}

/**
 * Usability status of the configured primary action (e.g. Screen Lock or Navigation).
 */
enum class ActionReadiness {
    /** All prerequisites are met; action can execute immediately on trigger. */
    READY,

    /** A prerequisite is missing (e.g. Accessibility or Overlay permission). */
    BLOCKED,

    /** Prerequisites are undetermined. */
    UNKNOWN,

    /** No action is configured. */
    NOT_APPLICABLE
}

/**
 * Complete, immutable snapshot of all independent state dimensions at a single instant.
 */
data class AppStateSnapshot(
    val desiredState: DesiredState = DesiredState.DISABLED,
    val overlayPermissionState: OverlayPermissionState = OverlayPermissionState.UNKNOWN,
    val overlayState: OverlayState = OverlayState.DETACHED,
    val runtimeState: RuntimeState = RuntimeState.STOPPED,
    val accessibilityState: AccessibilityState = AccessibilityState.UNKNOWN
)

