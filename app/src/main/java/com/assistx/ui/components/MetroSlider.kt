package com.assistx.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.assistx.ui.haptics.MetroHaptics
import com.assistx.ui.theme.LocalMetroAccent
import com.assistx.ui.theme.LocalMetroIsDark
import com.assistx.ui.theme.MetroBlack
import com.assistx.ui.theme.MetroDarkBorder
import com.assistx.ui.theme.MetroWhite
import kotlin.math.roundToInt

/**
 * Windows Phone "Metro" Flat Slider.
 *
 * Characteristics:
 * - Square corners everywhere (0dp radius).
 * - Thin 4dp flat rectangular track (solid accent fill on active portion).
 * - Discrete step indicators as 2dp x 8dp vertical rectangular notches (no round dots).
 * - Vertical rectangular thumb (10dp x 24dp) with 2dp solid outline.
 * - Haptic tick feedback on step changes.
 * - 44dp height touch target with instant tap-to-reposition and drag scrubbing.
 */
@Composable
fun MetroSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val haptics = remember { MetroHaptics(context) }
    val accentColor = LocalMetroAccent.current.color
    val isDark = LocalMetroIsDark.current
    val density = LocalDensity.current

    var isDragging by remember { mutableStateOf(false) }

    val rangeSpan = valueRange.endInclusive - valueRange.start
    val currentFraction = if (rangeSpan > 0f) {
        ((value - valueRange.start) / rangeSpan).coerceIn(0f, 1f)
    } else 0f

    val thumbWidthDp = 10.dp
    val thumbHeightDp = 24.dp
    val trackHeightDp = 4.dp

    val inactiveTrackColor = if (isDark) MetroDarkBorder else Color(0xFFD0D0D0)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .pointerInput(enabled, valueRange, steps) {
                if (!enabled) return@pointerInput

                detectTapGestures(
                    onPress = { offset ->
                        val widthPx = size.width.toFloat()
                        if (widthPx > 0) {
                            val rawFraction = (offset.x / widthPx).coerceIn(0f, 1f)
                            val newFraction = if (steps > 0) {
                                val stepCount = steps + 1
                                (rawFraction * stepCount).roundToInt().toFloat() / stepCount
                            } else rawFraction

                            val newValue = valueRange.start + (newFraction * rangeSpan)
                            if (newValue != value) {
                                haptics.light()
                                onValueChange(newValue)
                            }
                        }
                        tryAwaitRelease()
                        onValueChangeFinished?.invoke()
                    }
                )
            }
            .pointerInput(enabled, valueRange, steps) {
                if (!enabled) return@pointerInput

                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val widthPx = size.width.toFloat()
                        if (widthPx > 0) {
                            val rawFraction = (offset.x / widthPx).coerceIn(0f, 1f)
                            val newFraction = if (steps > 0) {
                                val stepCount = steps + 1
                                (rawFraction * stepCount).roundToInt().toFloat() / stepCount
                            } else rawFraction

                            val newValue = valueRange.start + (newFraction * rangeSpan)
                            if (newValue != value) {
                                haptics.light()
                                onValueChange(newValue)
                            }
                        }
                    },
                    onDragEnd = {
                        isDragging = false
                        onValueChangeFinished?.invoke()
                    },
                    onDragCancel = {
                        isDragging = false
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val widthPx = size.width.toFloat()
                        if (widthPx > 0) {
                            val rawFraction = (change.position.x / widthPx).coerceIn(0f, 1f)
                            val newFraction = if (steps > 0) {
                                val stepCount = steps + 1
                                (rawFraction * stepCount).roundToInt().toFloat() / stepCount
                            } else rawFraction

                            val newValue = valueRange.start + (newFraction * rangeSpan)
                            if (newValue != value) {
                                haptics.light()
                                onValueChange(newValue)
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val totalWidthPx = constraints.maxWidth.toFloat()
        val thumbWidthPx = with(density) { thumbWidthDp.toPx() }
        val trackAvailablePx = (totalWidthPx - thumbWidthPx).coerceAtLeast(0f)
        val thumbOffsetXPx = trackAvailablePx * currentFraction

        // 1. Inactive Track (Full Width)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeightDp)
                .background(inactiveTrackColor, RectangleShape)
        )

        // 2. Active Track (From start to center of thumb)
        val activeWidthPx = thumbOffsetXPx + (thumbWidthPx / 2f)
        val activeWidthDp = with(density) { activeWidthPx.toDp() }
        Box(
            modifier = Modifier
                .size(width = activeWidthDp, height = trackHeightDp)
                .background(accentColor, RectangleShape)
        )

        // 3. Discrete Step Ticks (if steps > 0)
        if (steps > 0 && trackAvailablePx > 0) {
            val stepCount = steps + 1
            for (i in 1..steps) {
                val stepFraction = i.toFloat() / stepCount
                val tickCenterXPx = (trackAvailablePx * stepFraction) + (thumbWidthPx / 2f)
                val tickLeftDp = with(density) { (tickCenterXPx - 1f).toDp() }
                val isPassed = stepFraction <= currentFraction
                val tickColor = if (isPassed) {
                    if (isDark) MetroBlack else MetroWhite
                } else {
                    if (isDark) MetroWhite.copy(alpha = 0.35f) else MetroBlack.copy(alpha = 0.35f)
                }

                Box(
                    modifier = Modifier
                        .offset { IntOffset(x = with(density) { tickLeftDp.roundToPx() }, y = 0) }
                        .size(width = 2.dp, height = 8.dp)
                        .background(tickColor, RectangleShape)
                )
            }
        }

        // 4. Metro Flat Vertical Rectangular Thumb
        val thumbBgColor = if (isDragging) MetroWhite else accentColor
        val thumbBorderColor = if (isDragging) accentColor else (if (isDark) MetroWhite else MetroBlack)

        Box(
            modifier = Modifier
                .offset { IntOffset(x = thumbOffsetXPx.roundToInt(), y = 0) }
                .size(width = thumbWidthDp, height = thumbHeightDp)
                .border(2.dp, thumbBorderColor, RectangleShape)
                .background(thumbBgColor, RectangleShape)
        )
    }
}
