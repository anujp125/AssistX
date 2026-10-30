package com.nudge.assist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
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
import com.nudge.assist.ui.theme.LocalMetroIsDark
import com.nudge.assist.ui.theme.MetroAccent
import com.nudge.assist.ui.theme.MetroDarkBorder
import com.nudge.assist.ui.theme.MetroTypography
import com.nudge.assist.ui.theme.MetroWhite

/**
 * Windows Phone "Metro" 20-Accent Color Swatch Grid.
 *
 * Characteristics:
 * - 4 columns x 5 rows of 0-radius square color swatches.
 * - 2dp solid border (white in dark mode, accent highlight when selected).
 * - Crisp white checkmark on active swatch.
 * - Direct tactile haptic tick on swatch tap.
 */
@Composable
fun MetroAccentPicker(
    selectedHex: String,
    onAccentSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = remember { MetroHaptics(context) }
    val isDark = LocalMetroIsDark.current

    val accents = remember { MetroAccent.values().toList() }
    // 5 rows of 4 swatches = 20 swatches
    val chunkedAccents = remember { accents.chunked(4) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chunkedAccents.forEach { rowAccents ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowAccents.forEach { accentDef ->
                    val isSelected = accentDef.hex.equals(selectedHex, ignoreCase = true)
                    val swatchBorderColor = if (isSelected) MetroWhite else Color.Transparent

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .border(if (isSelected) 2.5.dp else 1.dp, swatchBorderColor, RectangleShape)
                            .background(accentDef.color, RectangleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (!isSelected) {
                                    haptics.select()
                                    onAccentSelected(accentDef.hex)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "selected",
                                tint = MetroWhite,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
