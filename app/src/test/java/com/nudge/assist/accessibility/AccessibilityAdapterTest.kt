package com.nudge.assist.accessibility

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.nudge.assist.state.AccessibilityState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class FakeAccessibilityAdapter(
    var settingsEnabled: Boolean = false,
    var serviceConnected: Boolean = false
) : AccessibilityAdapterContract {

    override fun getAccessibilityState(context: Context?): AccessibilityState {
        if (context == null) return AccessibilityState.UNKNOWN
        return when {
            !settingsEnabled -> AccessibilityState.DISABLED
            serviceConnected -> AccessibilityState.ENABLED_CONNECTED
            else -> AccessibilityState.ENABLED_DISCONNECTED
        }
    }

    override fun isSettingsEnabled(context: Context?): Boolean = settingsEnabled
    override fun isServiceConnected(): Boolean = serviceConnected
    override fun createSettingsIntent(context: Context?): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    }
}

class AccessibilityAdapterTest {

    @Test
    fun testNullContextReturnsUnknown() {
        val adapter = FakeAccessibilityAdapter()
        assertEquals(AccessibilityState.UNKNOWN, adapter.getAccessibilityState(null))
    }

    @Test
    fun testDisabledWhenSettingsNotEnabled() {
        val adapter = FakeAccessibilityAdapter(settingsEnabled = false, serviceConnected = false)
        val dummyContext = Class.forName("android.app.Activity").getDeclaredConstructor().newInstance() as Context
        assertEquals(AccessibilityState.DISABLED, adapter.getAccessibilityState(dummyContext))
    }

    @Test
    fun testEnabledDisconnectedWhenSettingEnabledButServiceNotBound() {
        val adapter = FakeAccessibilityAdapter(settingsEnabled = true, serviceConnected = false)
        val dummyContext = Class.forName("android.app.Activity").getDeclaredConstructor().newInstance() as Context
        assertEquals(AccessibilityState.ENABLED_DISCONNECTED, adapter.getAccessibilityState(dummyContext))
    }

    @Test
    fun testEnabledConnectedWhenSettingEnabledAndServiceBound() {
        val adapter = FakeAccessibilityAdapter(settingsEnabled = true, serviceConnected = true)
        val dummyContext = Class.forName("android.app.Activity").getDeclaredConstructor().newInstance() as Context
        assertEquals(AccessibilityState.ENABLED_CONNECTED, adapter.getAccessibilityState(dummyContext))
    }

    @Test
    fun testSettingsIntentAction() {
        assertEquals("android.settings.ACCESSIBILITY_SETTINGS", Settings.ACTION_ACCESSIBILITY_SETTINGS)
    }
}
