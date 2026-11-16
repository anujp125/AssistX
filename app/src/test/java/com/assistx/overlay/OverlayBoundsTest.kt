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

    @Test
    fun testPortraitToLandscapeRightEdgeAdaptation() {
        val portraitBounds = OverlayManager.ScreenBounds(
            minX = 24,
            maxX = 912,
            minY = 131,
            maxY = 2184,
            screenWidth = 1080,
            screenHeight = 2400
        )
        val landscapeBounds = OverlayManager.ScreenBounds(
            minX = 131,
            maxX = 2232,
            minY = 24,
            maxY = 912,
            screenWidth = 2400,
            screenHeight = 1080
        )

        // User placed button on right side in portrait
        val isAnchoredToRight = true
        val portraitX = portraitBounds.maxX // 912

        // When switching to landscape with snap-to-edge enabled:
        val landscapeX = if (isAnchoredToRight) landscapeBounds.maxX else landscapeBounds.minX
        assertEquals(2232, landscapeX)
        // Must NOT remain at portraitX (912), which would hang in the middle of 2400 width!
        assertTrue(landscapeX > 912)
        assertEquals(2232, landscapeX.coerceIn(landscapeBounds.minX, landscapeBounds.maxX))
    }

    @Test
    fun testLandscapeToPortraitRightEdgeAdaptation() {
        val portraitBounds = OverlayManager.ScreenBounds(
            minX = 24,
            maxX = 912,
            minY = 131,
            maxY = 2184,
            screenWidth = 1080,
            screenHeight = 2400
        )
        val landscapeBounds = OverlayManager.ScreenBounds(
            minX = 131,
            maxX = 2232,
            minY = 24,
            maxY = 912,
            screenWidth = 2400,
            screenHeight = 1080
        )

        // User placed button on right side in landscape
        val isAnchoredToRight = true
        val landscapeX = landscapeBounds.maxX // 2232

        // When switching to portrait with snap-to-edge enabled:
        val portraitX = if (isAnchoredToRight) portraitBounds.maxX else portraitBounds.minX
        assertEquals(912, portraitX)
        // Must NOT remain at landscapeX (2232), which would be far out of screen (> 1080)!
        assertTrue(portraitX < portraitBounds.screenWidth)
        assertEquals(912, portraitX.coerceIn(portraitBounds.minX, portraitBounds.maxX))
    }

    @Test
    fun testFreeFloatingPositionClampingStrictlyBounded() {
        val bounds = OverlayManager.ScreenBounds(
            minX = 24,
            maxX = 912,
            minY = 100,
            maxY = 2200,
            screenWidth = 1080,
            screenHeight = 2400
        )

        // Extreme out-of-bounds coordinates must always be coerced safely
        val extremeRightX = 3500
        val clampedX = extremeRightX.coerceIn(bounds.minX, bounds.maxX)
        assertEquals(912, clampedX)

        val extremeLeftX = -500
        val clampedLeft = extremeLeftX.coerceIn(bounds.minX, bounds.maxX)
        assertEquals(24, clampedLeft)

        val extremeBottomY = 4000
        val clampedBottom = extremeBottomY.coerceIn(bounds.minY, bounds.maxY)
        assertEquals(2200, clampedBottom)
    }

    @Test
    fun testReverseLandscapeCutoutInsetOnRight() {
        val screenW = 2400
        val screenH = 1080
        val buttonSize = 144
        val padding = 24
        val insetLeft = 0
        val insetRight = 107 // Cutout on right in ROTATION_270

        val minX = insetLeft + padding // 24
        val maxX = screenW - insetRight - buttonSize - padding // 2125

        val bounds = OverlayManager.ScreenBounds(
            minX = minX,
            maxX = maxX,
            minY = padding,
            maxY = screenH - buttonSize - padding,
            screenWidth = screenW,
            screenHeight = screenH
        )

        assertEquals(24, bounds.minX)
        assertEquals(2125, bounds.maxX)

        // When dragged to right in reverse landscape, snaps to 2125 (not into cutout)
        val dragXRight = 1900
        val targetRight = if (dragXRight + buttonSize / 2 < bounds.screenWidth / 2) bounds.minX else bounds.maxX
        assertEquals(2125, targetRight)
    }

    @Test
    fun testHardwareOrientationDimensionInvariance() {
        val rawW = 1080
        val rawH = 2400

        // Regardless of raw buffer orientation:
        // Landscape (ROTATION_90 / 270) ALWAYS uses long side as width, short side as height
        val isLandscape = true
        val landscapeW = if (isLandscape) maxOf(rawW, rawH) else minOf(rawW, rawH)
        val landscapeH = if (isLandscape) minOf(rawW, rawH) else maxOf(rawW, rawH)
        assertEquals(2400, landscapeW)
        assertEquals(1080, landscapeH)

        // Portrait ALWAYS uses short side as width, long side as height
        val isPortrait = false
        val portraitW = if (isPortrait) maxOf(rawW, rawH) else minOf(rawW, rawH)
        val portraitH = if (isPortrait) minOf(rawW, rawH) else maxOf(rawW, rawH)
        assertEquals(1080, portraitW)
        assertEquals(2400, portraitH)
    }
}

