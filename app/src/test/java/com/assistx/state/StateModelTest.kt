package com.assistx.state

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class StateModelTest {

    @Test
    fun testDefaultSnapshotValues() {
        val snapshot = AppStateSnapshot()
        assertEquals(DesiredState.DISABLED, snapshot.desiredState)
        assertEquals(OverlayPermissionState.UNKNOWN, snapshot.overlayPermissionState)
        assertEquals(OverlayState.DETACHED, snapshot.overlayState)
        assertEquals(RuntimeState.STOPPED, snapshot.runtimeState)
        assertEquals(AccessibilityState.UNKNOWN, snapshot.accessibilityState)
    }

    @Test
    fun testSnapshotImmutabilityAndCopy() {
        val initial = AppStateSnapshot()
        val modified = initial.copy(
            desiredState = DesiredState.ENABLED,
            overlayState = OverlayState.ATTACHED
        )

        assertEquals(DesiredState.DISABLED, initial.desiredState)
        assertEquals(DesiredState.ENABLED, modified.desiredState)
        assertEquals(OverlayState.ATTACHED, modified.overlayState)
        assertNotEquals(initial, modified)
    }

    @Test
    fun testOverlayAndAccessibilityDimensionsAreIndependent() {
        val snapshot = AppStateSnapshot(
            desiredState = DesiredState.ENABLED,
            overlayPermissionState = OverlayPermissionState.GRANTED,
            overlayState = OverlayState.ATTACHED,
            runtimeState = RuntimeState.ACTIVE,
            accessibilityState = AccessibilityState.DISABLED
        )

        // Overlay is attached and healthy, even though Accessibility is disabled
        assertEquals(OverlayState.ATTACHED, snapshot.overlayState)
        assertEquals(AccessibilityState.DISABLED, snapshot.accessibilityState)
    }
}

