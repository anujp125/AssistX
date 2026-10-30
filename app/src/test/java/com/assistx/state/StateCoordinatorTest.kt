package com.assistx.state

import org.junit.Assert.assertEquals
import org.junit.Test

class StateCoordinatorTest {

    @Test
    fun testInitialCoordinatorState() {
        val coordinator = StateCoordinator()
        assertEquals(DesiredState.DISABLED, coordinator.currentSnapshot.desiredState)
        assertEquals(OverlayState.DETACHED, coordinator.currentSnapshot.overlayState)
        assertEquals(RuntimeState.STOPPED, coordinator.currentSnapshot.runtimeState)
        assertEquals(UserFacingStatus.DISABLED, coordinator.currentStatus)
        assertEquals(UserFacingStatus.DISABLED, coordinator.userFacingStatus.value)
    }

    @Test
    fun testUpdateDesiredState() {
        val coordinator = StateCoordinator()
        coordinator.updateDesiredState(DesiredState.ENABLED)
        assertEquals(DesiredState.ENABLED, coordinator.currentSnapshot.desiredState)

        coordinator.updateDesiredState(DesiredState.DISABLED)
        assertEquals(DesiredState.DISABLED, coordinator.currentSnapshot.desiredState)
    }

    @Test
    fun testDisablingCancelsActiveRecovery() {
        val coordinator = StateCoordinator(
            initialSnapshot = AppStateSnapshot(
                desiredState = DesiredState.ENABLED,
                runtimeState = RuntimeState.RECOVERING
            )
        )

        assertEquals(RuntimeState.RECOVERING, coordinator.currentSnapshot.runtimeState)

        // When user disables AssistX, recovery must stop immediately
        coordinator.updateDesiredState(DesiredState.DISABLED)
        assertEquals(DesiredState.DISABLED, coordinator.currentSnapshot.desiredState)
        assertEquals(RuntimeState.STOPPED, coordinator.currentSnapshot.runtimeState)
    }

    @Test
    fun testRecoverySuppressedWhenDisabled() {
        val coordinator = StateCoordinator(
            initialSnapshot = AppStateSnapshot(desiredState = DesiredState.DISABLED)
        )

        // Attempting to set RECOVERING while user intent is DISABLED must be rejected
        coordinator.updateRuntimeState(RuntimeState.RECOVERING)
        assertEquals(RuntimeState.STOPPED, coordinator.currentSnapshot.runtimeState)
    }

    @Test
    fun testResetRuntimeObservationsPreservesDesiredState() {
        val coordinator = StateCoordinator(
            initialSnapshot = AppStateSnapshot(
                desiredState = DesiredState.ENABLED,
                overlayPermissionState = OverlayPermissionState.GRANTED,
                overlayState = OverlayState.ATTACHED,
                runtimeState = RuntimeState.ACTIVE,
                accessibilityState = AccessibilityState.ENABLED_CONNECTED
            )
        )

        coordinator.resetRuntimeObservations()

        assertEquals(DesiredState.ENABLED, coordinator.currentSnapshot.desiredState)
        assertEquals(OverlayPermissionState.GRANTED, coordinator.currentSnapshot.overlayPermissionState)
        assertEquals(OverlayState.DETACHED, coordinator.currentSnapshot.overlayState)
        assertEquals(RuntimeState.STOPPED, coordinator.currentSnapshot.runtimeState)
        assertEquals(AccessibilityState.UNKNOWN, coordinator.currentSnapshot.accessibilityState)
    }

    @Test
    fun testReactiveUserFacingStatusEmission() {
        val coordinator = StateCoordinator()
        val statusFlow = coordinator.userFacingStatus

        assertEquals(UserFacingStatus.DISABLED, statusFlow.value)
        assertEquals(UserFacingStatus.DISABLED, coordinator.currentStatus)

        coordinator.updateDesiredState(DesiredState.ENABLED)
        coordinator.updateOverlayPermission(OverlayPermissionState.GRANTED)
        coordinator.updateOverlayState(OverlayState.ATTACHED)
        coordinator.updateRuntimeState(RuntimeState.ACTIVE)
        coordinator.updateAccessibilityState(AccessibilityState.ENABLED_CONNECTED)

        assertEquals(UserFacingStatus.ACTIVE, coordinator.currentStatus)
        assertEquals(UserFacingStatus.ACTIVE, statusFlow.value)
        assertEquals(ActionReadiness.READY, coordinator.currentActionReadiness)
    }
}
