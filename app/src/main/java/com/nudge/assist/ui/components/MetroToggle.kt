package com.nudge.assist.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nudge.assist.ui.haptics.MetroHaptics
import com.nudge.assist.ui.theme.LocalMetroAccent
import com.nudge.assist.ui.theme.LocalMetroIsDark
import com.nudge.assist.ui.theme.MetroBlack
import com.nudge.assist.ui.theme.MetroDarkBorder
import com.nudge.assist.ui.theme.MetroTypography
import com.nudge.assist.ui.theme.MetroWhite

/**
 * Authentic Windows Phone "Metro" Flat Toggle Switch.
 *
 * Characteristics:
 * - Square corners (0dp radius).
 * - 2dp solid rectangular boundary (44dp x 22dp).
 * - Square inner thumb (14dp x 14dp) sliding horizontally.
 * - Solid accent color fill when ON; transparent/dark when OFF.
 * - Explicit lowercase "on" / "off" label next to the toggle.
 * - Integrated Metro haptic feedback.
 */
@Composable
fun MetroToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    val haptics = remember { MetroHaptics(context) }
    val accentColor = LocalMetroAccent.current.color
    val isDark = LocalMetroIsDark.current

    val borderColor = if (checked) accentColor else (if (isDark) MetroDarkBorder else Color(0xFFB0B0B0))
    val bgColor = if (checked) accentColor else Color.Transparent
    val thumbColor = if (checked) MetroWhite else (if (isDark) MetroWhite else MetroBlack)
    val textColor = if (isDark) MetroWhite else MetroBlack

    // Horizontal offset for the square thumb: 3dp (left) to 25dp (right)
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 25.dp else 3.dp,
        animationSpec = tween(durationMillis = 120),
        label = "thumb_offset"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled
            ) {
                haptics.tap()
                onCheckedChange(!checked)
            }
            .padding(vertical = 4.dp)
    ) {
        // Toggle Outer Track
        Box(
            modifier = Modifier
                .width(46.dp)
                .height(24.dp)
                .border(2.dp, borderColor, RectangleShape)
                .background(bgColor, RectangleShape),
            contentAlignment = Alignment.CenterStart
        ) {
            // Square Slider Thumb
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(14.dp)
                    .background(thumbColor, RectangleShape)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // On / Off Text Label
        Text(
            text = if (checked) "on" else "off",
            style = MetroTypography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = if (checked) accentColor else textColor.copy(alpha = 0.65f),
                fontSize = 15.sp
            )
        )
    }
}
