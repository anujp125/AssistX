package com.nudge.assist.state

import org.junit.Assert.assertEquals
import org.junit.Test

class StatusEvaluatorTest {

    @Test
    fun testDisabledDesiredStateAlwaysYieldsDisabled() {
        val snapshot = AppStateSnapshot(
            desiredState = DesiredState.DISABLED,
            overlayPermissionState = OverlayPermissionState.REQUIRED,
            overlayState = OverlayState.ATTACHED,
            runtimeState = RuntimeState.ACTIVE,
            accessibilityState = AccessibilityState.DISABLED
        )
        val status = StatusEvaluator.deriveUserFacingStatus(snapshot)
        assertEquals(UserFacingStatus.DISABLED, status)

        val readiness = StatusEvaluator.deriveActionReadiness(snapshot)
        assertEquals(ActionReadiness.BLOCKED, readiness)
    }

    @Test
    fun testMissingOverlayPermissionYieldsOverlayPermissionRequired() {
        val snapshot = AppStateSnapshot(
            desiredState = DesiredState.ENABLED,
            overlayPermissionState = OverlayPermissionState.REQUIRED,
            overlayState = OverlayState.DETACHED,
            runtimeState = RuntimeState.STARTING
        )
        val status = StatusEvaluator.deriveUserFacingStatus(snapshot)
        assertEquals(UserFacingStatus.OVERLAY_PERMISSION_REQUIRED, status)
    }

    @Test
    fun testFailedRuntimeYieldsRecoveryRequired() {
        val snapshot = AppStateSnapshot(
            desiredState = DesiredState.ENABLED,
            overlayPermissionState = OverlayPermissionState.GRANTED,
            overlayState = OverlayState.DETACHED,
            runtimeState = RuntimeState.FAILED
        )
        val status = StatusEvaluator.deriveUserFacingStatus(snapshot)
        assertEquals(UserFacingStatus.RECOVERY_REQUIRED, status)
    }

    @Test
    fun testActiveRecoveryYieldsRecovering() {
        val snapshot = AppStateSnapshot(
            desiredState = DesiredState.ENABLED,
            overlayPermissionState = OverlayPermissionState.GRANTED,
            overlayState = OverlayState.DETACHED,
            runtimeState = RuntimeState.RECOVERING
        )
        val status = StatusEvaluator.deriveUserFacingStatus(snapshot)
        assertEquals(UserFacingStatus.RECOVERING, status)
    }

    @Test
    fun testInterruptedRuntimeYieldsInterrupted() {
        val snapshot = AppStateSnapshot(
            desiredState = DesiredState.ENABLED,
            overlayPermissionState = OverlayPermissionState.GRANTED,
            overlayState = OverlayState.DETACHED,
            runtimeState = RuntimeState.INTERRUPTED
        )
        val status = StatusEvaluator.deriveUserFacingStatus(snapshot)
        assertEquals(UserFacingStatus.INTERRUPTED, status)
    }

    @Test
    fun testOverlayErrorYieldsInterrupted() {
        val snapshot = AppStateSnapshot(
            desiredState = DesiredState.ENABLED,
            overlayPermissionState = OverlayPermissionState.GRANTED,
            overlayState = OverlayState.ERROR,
            runtimeState = RuntimeState.ACTIVE
        )
        val status = StatusEvaluator.deriveUserFacingStatus(snapshot)
        assertEquals(UserFacingStatus.INTERRUPTED, status)
    }

    @Test
    fun testStartingRuntimeYieldsStarting() {
        val snapshot = AppStateSnapshot(
            desiredState = DesiredState.ENABLED,
            overlayPermissionState = OverlayPermissionState.GRANTED,
            overlayState = OverlayState.ATTACHING,
            runtimeState = RuntimeState.STARTING
        )
        val status = StatusEvaluator.deriveUserFacingStatus(snapshot)
        assertEquals(UserFacingStatus.STARTING, status)
    }

    @Test
    fun testAccessibilityDisabledYieldsAccessibilityDisabled() {
        val snapshot = AppStateSnapshot(
            desiredState = DesiredState.ENABLED,
            overlayPermissionState = OverlayPermissionState.GRANTED,
            overlayState = OverlayState.ATTACHED,
            runtimeState = RuntimeState.ACTIVE,
            accessibilityState = AccessibilityState.DISABLED
        )
        val status = StatusEvaluator.deriveUserFacingStatus(snapshot)
        assertEquals(UserFacingStatus.ACCESSIBILITY_DISABLED, status)

        val readiness = StatusEvaluator.deriveActionReadiness(snapshot)
        assertEquals(ActionReadiness.BLOCKED, readiness)
    }

    @Test
    fun testAccessibilityDisconnectedYieldsAccessibilityDisconnected() {
        val snapshot = AppStateSnapshot(
            desiredState = DesiredState.ENABLED,
            overlayPermissionState = OverlayPermissionState.GRANTED,
            overlayState = OverlayState.ATTACHED,
            runtimeState = RuntimeState.ACTIVE,
            accessibilityState = AccessibilityState.ENABLED_DISCONNECTED
        )
        val status = StatusEvaluator.deriveUserFacingStatus(snapshot)
        assertEquals(UserFacingStatus.ACCESSIBILITY_DISCONNECTED, status)

        val readiness = StatusEvaluator.deriveActionReadiness(snapshot)
        assertEquals(ActionReadiness.BLOCKED, readiness)
    }

    @Test
    fun testFullHealthyStateYieldsActive() {
        val snapshot = AppStateSnapshot(
            desiredState = DesiredState.ENABLED,
            overlayPermissionState = OverlayPermissionState.GRANTED,
            overlayState = OverlayState.ATTACHED,
            runtimeState = RuntimeState.ACTIVE,
            accessibilityState = AccessibilityState.ENABLED_CONNECTED
        )
        val status = StatusEvaluator.deriveUserFacingStatus(snapshot)
        assertEquals(UserFacingStatus.ACTIVE, status)

        val readiness = StatusEvaluator.deriveActionReadiness(snapshot)
        assertEquals(ActionReadiness.READY, readiness)
    }

    @Test
    fun testUnknownSubsystemYieldsCheckingStatus() {
        val snapshot = AppStateSnapshot(
            desiredState = DesiredState.ENABLED,
            overlayPermissionState = OverlayPermissionState.GRANTED,
            overlayState = OverlayState.ATTACHED,
            runtimeState = RuntimeState.ACTIVE,
            accessibilityState = AccessibilityState.UNKNOWN
        )
        val status = StatusEvaluator.deriveUserFacingStatus(snapshot)
        assertEquals(UserFacingStatus.CHECKING_STATUS, status)

        val readiness = StatusEvaluator.deriveActionReadiness(snapshot)
        assertEquals(ActionReadiness.UNKNOWN, readiness)
    }
}

