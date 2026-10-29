package com.nudge.assist.state

/**
 * User-facing high-level summary derived deterministically from the multidimensional state.
 * This label represents the truthful state shown in the Studio dashboard.
 */
enum class UserFacingStatus(val label: String) {
    DISABLED("Disabled"),
    STARTING("Starting"),
    ACTIVE("Active"),
    OVERLAY_PERMISSION_REQUIRED("Overlay Permission Required"),
    ACCESSIBILITY_DISABLED("Accessibility Disabled"),
    ACCESSIBILITY_DISCONNECTED("Accessibility Disconnected"),
    INTERRUPTED("Interrupted"),
    RECOVERING("Recovering"),
    RECOVERY_REQUIRED("Recovery Required"),
    CHECKING_STATUS("Checking Status")
}

object StatusEvaluator {

    /**
     * Pure function that deterministically maps an [AppStateSnapshot] to a [UserFacingStatus].
     *
     * Invariants guaranteed:
     * 1. A desired state of DISABLED always produces DISABLED (suppressing recovery and error noise).
     * 2. Missing permissions or disconnected services never produce an ACTIVE status.
     * 3. Unknown observations never produce a false ACTIVE status.
     */
    fun deriveUserFacingStatus(snapshot: AppStateSnapshot): UserFacingStatus {
        // Priority 1: User explicitly disabled AssistX
        if (snapshot.desiredState == DesiredState.DISABLED) {
            return UserFacingStatus.DISABLED
        }

        // Priority 2: Permission Blocker (user action needed in system settings)
        if (snapshot.overlayPermissionState == OverlayPermissionState.REQUIRED) {
            return UserFacingStatus.OVERLAY_PERMISSION_REQUIRED
        }

        // Priority 3: Recovery / Failure state
        if (snapshot.runtimeState == RuntimeState.FAILED) {
            return UserFacingStatus.RECOVERY_REQUIRED
        }
        if (snapshot.runtimeState == RuntimeState.RECOVERING) {
            return UserFacingStatus.RECOVERING
        }
        if (snapshot.runtimeState == RuntimeState.INTERRUPTED || snapshot.overlayState == OverlayState.ERROR) {
            return UserFacingStatus.INTERRUPTED
        }

        // Priority 4: In-flight Startup
        if (snapshot.runtimeState == RuntimeState.STARTING || snapshot.overlayState == OverlayState.ATTACHING) {
            return UserFacingStatus.STARTING
        }

        // Priority 5: Accessibility Subsystem Health (conditional on requirement)
        when (snapshot.accessibilityState) {
            AccessibilityState.DISABLED -> return UserFacingStatus.ACCESSIBILITY_DISABLED
            AccessibilityState.ENABLED_DISCONNECTED -> return UserFacingStatus.ACCESSIBILITY_DISCONNECTED
            else -> { /* NOT_REQUIRED, ENABLED_CONNECTED, or UNKNOWN */ }
        }

        // Priority 6: Full Active Health Check
        val isOverlayActive = snapshot.overlayState == OverlayState.ATTACHED &&
                snapshot.overlayPermissionState == OverlayPermissionState.GRANTED
        val isRuntimeActive = snapshot.runtimeState == RuntimeState.ACTIVE
        val isAccessibilityReady = snapshot.accessibilityState == AccessibilityState.ENABLED_CONNECTED ||
                snapshot.accessibilityState == AccessibilityState.NOT_REQUIRED

        if (isOverlayActive && isRuntimeActive && isAccessibilityReady) {
            return UserFacingStatus.ACTIVE
        }

        // Priority 7: Incomplete or ambiguous observations
        return UserFacingStatus.CHECKING_STATUS
    }

    /**
     * Determines whether the primary physical button action (e.g. Lock Screen) is currently executable.
     */
    fun deriveActionReadiness(snapshot: AppStateSnapshot): ActionReadiness {
        if (snapshot.desiredState == DesiredState.DISABLED) {
            return ActionReadiness.BLOCKED
        }
        if (snapshot.overlayState != OverlayState.ATTACHED) {
            return ActionReadiness.BLOCKED
        }
        if (snapshot.accessibilityState == AccessibilityState.DISABLED ||
            snapshot.accessibilityState == AccessibilityState.ENABLED_DISCONNECTED) {
            return ActionReadiness.BLOCKED
        }
        if (snapshot.accessibilityState == AccessibilityState.UNKNOWN ||
            snapshot.overlayState == OverlayState.UNKNOWN) {
            return ActionReadiness.UNKNOWN
        }
        return ActionReadiness.READY
    }
}

