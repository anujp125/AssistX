package com.assistx.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.assistx.ui.haptics.MetroHaptics
import com.assistx.ui.theme.LocalMetroAccent
import com.assistx.ui.theme.LocalMetroIsDark
import com.assistx.ui.theme.MetroBlack
import com.assistx.ui.theme.MetroDarkBorder
import com.assistx.ui.theme.MetroTypography
import com.assistx.ui.theme.MetroWhite

/**
 * Modern Windows Phone "Metro" Flat Button.
 *
 * Characteristics:
 * - Square corners (0dp radius).
 * - 2dp solid outline.
 * - Transparent fill by default, switches to solid accent fill on press.
 * - Monoline text / icon in lowercase.
 * - Haptic tap on press.
 */
@Composable
fun MetroButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isDanger: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val context = LocalContext.current
    val haptics = remember { MetroHaptics(context) }
    val accentColor = LocalMetroAccent.current.color
    val isDark = LocalMetroIsDark.current

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed && enabled) {
            haptics.tap()
        }
    }

    val activeColor = if (isDanger) Color(0xFFE51400) else accentColor
    val defaultBorderColor = if (isDark) MetroWhite.copy(alpha = 0.7f) else MetroBlack.copy(alpha = 0.7f)
    val borderColor = if (isPressed) activeColor else defaultBorderColor

    val bgColor = if (isPressed) activeColor else Color.Transparent
    val contentColor = if (isPressed) {
        MetroWhite
    } else {
        if (isDark) MetroWhite else MetroBlack
    }

    Box(
        modifier = modifier
            .height(44.dp)
            .border(2.dp, if (enabled) borderColor else borderColor.copy(alpha = 0.3f), RectangleShape)
            .background(bgColor, RectangleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MetroTypography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (enabled) contentColor else contentColor.copy(alpha = 0.3f)
                )
            )
        }
    }
}
