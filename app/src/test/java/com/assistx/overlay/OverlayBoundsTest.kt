package com.assistx.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayBoundsTest {

    @Test
    fun testLandscapeEdgeSnappingCalculation() {
        val screenW = 2400
        val screenH = 1080
        val buttonSize = 144
        val padding = 24
        val insetLeft = 107
        val insetRight = 0

        val minX = insetLeft + padding // 131
        val maxX = screenW - insetRight - buttonSize - padding // 2232

        val bounds = OverlayManager.ScreenBounds(
            minX = minX,
            maxX = maxX,
            minY = padding,
            maxY = screenH - buttonSize - padding,
            screenWidth = screenW,
            screenHeight = screenH
        )

        // When dragged to right half in landscape:
        val dragXRight = 1800
        val targetRight = if (dragXRight + buttonSize / 2 < bounds.screenWidth / 2) bounds.minX else bounds.maxX
        assertEquals(2232, targetRight)

        // When dragged to left half in landscape:
        val dragXLeft = 400
        val targetLeft = if (dragXLeft + buttonSize / 2 < bounds.screenWidth / 2) bounds.minX else bounds.maxX
        assertEquals(131, targetLeft)
    }

    @Test
    fun testPortraitEdgeSnappingCalculation() {
        val screenW = 1080
        val screenH = 2400
        val buttonSize = 144
        val padding = 24
        val insetLeft = 0
        val insetRight = 0

        val minX = insetLeft + padding // 24
        val maxX = screenW - insetRight - buttonSize - padding // 912

        val bounds = OverlayManager.ScreenBounds(
            minX = minX,
            maxX = maxX,
            minY = 107 + padding,
            maxY = screenH - 48 - buttonSize - padding,
            screenWidth = screenW,
            screenHeight = screenH
        )

        // When dragged to right half in portrait:
        val dragXRight = 800
        val targetRight = if (dragXRight + buttonSize / 2 < bounds.screenWidth / 2) bounds.minX else bounds.maxX
        assertEquals(912, targetRight)

        // When dragged to left half in portrait:
        val dragXLeft = 100
        val targetLeft = if (dragXLeft + buttonSize / 2 < bounds.screenWidth / 2) bounds.minX else bounds.maxX
        assertEquals(24, targetLeft)
    }

    @Test
    fun testRelativeYPreservationAcrossOrientationChange() {
        val portraitHeight = 2400
        val landscapeHeight = 1080
        val initialY = 1200 // Exactly 50% down

        val relativeY = initialY.toFloat() / portraitHeight.toFloat()
        assertEquals(0.50f, relativeY, 0.001f)

        val landscapeY = (relativeY * landscapeHeight).toInt()
        assertEquals(540, landscapeY) // Stays exactly at 50% in landscape

        val restoredY = ((landscapeY.toFloat() / landscapeHeight.toFloat()) * portraitHeight).toInt()
        assertEquals(1200, restoredY) // Restores back to 1200 in portrait
    }
}

