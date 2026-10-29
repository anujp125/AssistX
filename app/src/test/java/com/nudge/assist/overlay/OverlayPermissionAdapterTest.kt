package com.nudge.assist.overlay

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.nudge.assist.state.OverlayPermissionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class FakeOverlayPermissionAdapter(
    var state: OverlayPermissionState = OverlayPermissionState.GRANTED
) : OverlayPermissionAdapter {
    override fun checkPermission(context: Context): OverlayPermissionState = state
    override fun createSettingsIntent(context: Context): Intent {
        return Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
    }
}

class OverlayPermissionAdapterTest {

    @Test
    fun testFakePermissionAdapterReturnsConfiguredState() {
        val adapter = FakeOverlayPermissionAdapter(OverlayPermissionState.REQUIRED)
        assertEquals(OverlayPermissionState.REQUIRED, adapter.state)

        adapter.state = OverlayPermissionState.GRANTED
        assertEquals(OverlayPermissionState.GRANTED, adapter.state)
    }

    @Test
    fun testSettingsIntentActionConstant() {
        assertEquals("android.settings.action.MANAGE_OVERLAY_PERMISSION", Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
    }
}
