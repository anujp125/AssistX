package com.nudge.assist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

data class MetroOption<T>(
    val value: T,
    val label: String
)

/**
 * Windows Phone "Metro" Flat Segmented Option Picker.
 *
 * Characteristics:
 * - Square corners (0dp radius).
 * - 2dp solid outline.
 * - Solid accent fill on active segment; transparent on inactive.
 * - Haptic select tick on change.
 */
@Composable
fun <T> MetroSegmentedPicker(
    options: List<MetroOption<T>>,
    selectedValue: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = remember { MetroHaptics(context) }
    val accentColor = LocalMetroAccent.current.color
    val isDark = LocalMetroIsDark.current

    val borderColor = if (isDark) MetroDarkBorder else Color(0xFFD0D0D0)
    val inactiveTextColor = if (isDark) MetroWhite.copy(alpha = 0.6f) else MetroBlack.copy(alpha = 0.6f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .border(2.dp, borderColor, RectangleShape),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = option.value == selectedValue
            val bgColor = if (isSelected) accentColor else Color.Transparent
            val textColor = if (isSelected) MetroWhite else inactiveTextColor

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(bgColor, RectangleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (option.value != selectedValue) {
                            haptics.select()
                            onOptionSelected(option.value)
                        }
                    }
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option.label.lowercase(),
                    style = MetroTypography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp,
                        color = textColor
                    ),
                    maxLines = 1
                )
            }
        }
    }
}
