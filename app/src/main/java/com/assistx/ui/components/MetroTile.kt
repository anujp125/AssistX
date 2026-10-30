package com.assistx.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.assistx.ui.haptics.MetroHaptics
import com.assistx.ui.theme.LocalMetroAccent
import com.assistx.ui.theme.LocalMetroIsDark
import com.assistx.ui.theme.MetroBlack
import com.assistx.ui.theme.MetroDarkBorder
import com.assistx.ui.theme.MetroDarkTile
import com.assistx.ui.theme.MetroTypography
import com.assistx.ui.theme.MetroWhite

enum class MetroTileStyle {
    STANDARD,      // Dark tile base
    ACCENT,        // Solid accent background
    OUTLINED       // Transparent with 2dp border
}

/**
 * Modern Windows Phone "Metro" Live Tile.
 *
 * Characteristics:
 * - Square corners (0dp radius).
 * - 2dp solid outline.
 * - Flat solid background (no elevation, shadows, or gradients).
 * - Press feedback: subtle scale-down (0.965f) coupled with Metro haptic tap.
 * - Clean typographic hierarchy: monoline icon/center content + left-aligned labels.
 * - Strict contrast compliance: on ACCENT tiles, all foreground elements are Pure White.
 * - Zero artificial dots or decorative AI badges.
 */
@Composable
fun MetroTile(
    label: String,
    modifier: Modifier = Modifier,
    height: Dp = 120.dp,
    style: MetroTileStyle = MetroTileStyle.STANDARD,
    statusText: String? = null,
    statusColor: Color = Color.Unspecified,
    isCompact: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { MetroHaptics(context) }
    val accentColor = LocalMetroAccent.current.color
    val isDark = LocalMetroIsDark.current

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Trigger haptic tap when pressed down
    LaunchedEffect(isPressed) {
        if (isPressed && onClick != null) {
            haptics.tap()
        }
    }

    // Fast, purposeful tilt/scale-down animation (Metro style)
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.965f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "tile_scale"
    )

    val bgColor = when (style) {
        MetroTileStyle.ACCENT -> accentColor
        MetroTileStyle.STANDARD -> if (isDark) MetroDarkTile else Color(0xFFF2F2F2)
        MetroTileStyle.OUTLINED -> Color.Transparent
    }

    val borderColor = when (style) {
        MetroTileStyle.ACCENT -> accentColor
        MetroTileStyle.STANDARD -> if (isDark) MetroDarkBorder else Color(0xFFE0E0E0)
        MetroTileStyle.OUTLINED -> if (isDark) MetroDarkBorder else Color(0xFFD0D0D0)
    }

    val contentColor = when (style) {
        MetroTileStyle.ACCENT -> MetroWhite
        else -> if (isDark) MetroWhite else MetroBlack
    }

    // Contrast Rule: On ACCENT tiles, status is always pure white (passes WCAG AA > 4.5:1).
    // On dark neutral surfaces, use semantic status color if provided.
    val effectiveStatusColor = when {
        style == MetroTileStyle.ACCENT -> MetroWhite
        statusColor != Color.Unspecified -> statusColor
        else -> contentColor.copy(alpha = 0.85f)
    }

    var boxModifier = modifier
        .height(height)
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .border(2.dp, borderColor, RectangleShape)
        .background(bgColor, RectangleShape)

    if (onClick != null) {
        boxModifier = boxModifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    }

    Box(
        modifier = boxModifier.padding(12.dp)
    ) {
        // Tile Center Content (Icon, primary metric, or custom composition)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = if (isCompact) Alignment.TopStart else Alignment.Center
        ) {
            content()
        }

        // Bottom Typography Spine
        if (isCompact) {
            // Stacked column: ensures long status strings get the full width of the tile
            // and never wrap words awkwardly into multiple lines.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
            ) {
                Text(
                    text = label.lowercase(),
                    style = MetroTypography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = contentColor.copy(alpha = 0.65f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (statusText != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = statusText.lowercase(),
                        style = MetroTypography.bodySmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = effectiveStatusColor
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            // Wide Tile Layout: label on bottom-left, status on bottom-right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = label.lowercase(),
                    style = MetroTypography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor.copy(alpha = 0.85f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (statusText != null) {
                    Text(
                        text = statusText.lowercase(),
                        style = MetroTypography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = effectiveStatusColor
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
