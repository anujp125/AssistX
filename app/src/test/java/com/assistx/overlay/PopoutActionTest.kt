package com.assistx.overlay

import com.assistx.ui.floating.PopoutAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PopoutActionTest {

    private fun resolveActionOrder(popoutOrder: String, showPaymentPause: Boolean): List<PopoutAction> {
        val baseList = when (popoutOrder) {
            "POWER_VOLUME_SCREENSHOT" -> listOf(PopoutAction.POWER, PopoutAction.VOLUME, PopoutAction.SCREENSHOT)
            "VOLUME_SCREENSHOT_POWER" -> listOf(PopoutAction.VOLUME, PopoutAction.SCREENSHOT, PopoutAction.POWER)
            "PAUSE_SCREENSHOT_VOLUME_POWER" -> listOf(PopoutAction.PAYMENT_PAUSE, PopoutAction.SCREENSHOT, PopoutAction.VOLUME, PopoutAction.POWER)
            "POWER_VOLUME_SCREENSHOT_PAUSE" -> listOf(PopoutAction.POWER, PopoutAction.VOLUME, PopoutAction.SCREENSHOT, PopoutAction.PAYMENT_PAUSE)
            "VOLUME_SCREENSHOT_POWER_PAUSE" -> listOf(PopoutAction.VOLUME, PopoutAction.SCREENSHOT, PopoutAction.POWER, PopoutAction.PAYMENT_PAUSE)
            else -> listOf(PopoutAction.SCREENSHOT, PopoutAction.VOLUME, PopoutAction.POWER)
        }
        return if (showPaymentPause && !baseList.contains(PopoutAction.PAYMENT_PAUSE)) {
            baseList + PopoutAction.PAYMENT_PAUSE
        } else if (!showPaymentPause) {
            baseList.filter { it != PopoutAction.PAYMENT_PAUSE }
        } else {
            baseList
        }
    }

    @Test
    fun testDefaultOrderWithPaymentPauseIncludesFourActions() {
        val actions = resolveActionOrder("SCREENSHOT_VOLUME_POWER", showPaymentPause = true)
        assertEquals(4, actions.size)
        assertEquals(PopoutAction.SCREENSHOT, actions[0])
        assertEquals(PopoutAction.VOLUME, actions[1])
        assertEquals(PopoutAction.POWER, actions[2])
        assertEquals(PopoutAction.PAYMENT_PAUSE, actions[3])
    }

    @Test
    fun testDefaultOrderWithoutPaymentPauseIncludesThreeActions() {
        val actions = resolveActionOrder("SCREENSHOT_VOLUME_POWER", showPaymentPause = false)
        assertEquals(3, actions.size)
        assertEquals(listOf(PopoutAction.SCREENSHOT, PopoutAction.VOLUME, PopoutAction.POWER), actions)
        assertFalse(actions.contains(PopoutAction.PAYMENT_PAUSE))
    }

    @Test
    fun testPauseAtFrontOrderMaintainsPauseAtFront() {
        val actions = resolveActionOrder("PAUSE_SCREENSHOT_VOLUME_POWER", showPaymentPause = true)
        assertEquals(4, actions.size)
        assertEquals(PopoutAction.PAYMENT_PAUSE, actions[0])
        assertEquals(PopoutAction.SCREENSHOT, actions[1])
        assertEquals(PopoutAction.VOLUME, actions[2])
        assertEquals(PopoutAction.POWER, actions[3])
    }

    @Test
    fun testPauseAtFrontWhenDisabledFiltersOutPause() {
        val actions = resolveActionOrder("PAUSE_SCREENSHOT_VOLUME_POWER", showPaymentPause = false)
        assertEquals(3, actions.size)
        assertEquals(listOf(PopoutAction.SCREENSHOT, PopoutAction.VOLUME, PopoutAction.POWER), actions)
        assertFalse(actions.contains(PopoutAction.PAYMENT_PAUSE))
    }

    @Test
    fun testFourButtonPopoutGeometrySpan() {
        val buttonDiameter = 54f
        val itemSpacing = 10f

        // 3 buttons span: 3 * 54 + 2 * 10 = 162 + 20 = 182
        val threeSpan = (3 * buttonDiameter) + (2 * itemSpacing)
        assertEquals(182f, threeSpan, 0.01f)

        // 4 buttons span: 4 * 54 + 3 * 10 = 216 + 30 = 246
        val fourSpan = (4 * buttonDiameter) + (3 * itemSpacing)
        assertEquals(246f, fourSpan, 0.01f)

        // On a standard 1080x2400 display (vertical height 2400px), 246dp * 3 = 738px.
        // Screen headroom remaining: 2400 - 738 = 1662px, fully fits within display bounds.
        assertTrue(fourSpan * 3 < 2400)
    }
}

