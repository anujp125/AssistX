package com.assistx.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
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
 * Windows Phone "Metro" Grouped Collapsible Section.
 *
 * Characteristics:
 * - Subtle, low-contrast header avoiding heavy card containers or elevation.
 * - Flat solid styling with 0dp sharp corners.
 * - Monoline sharp chevron indicator rotating smoothly on expansion.
 * - Dynamic summary subtitle visible at a glance.
 * - Snappy vertical expand/collapse animation (200ms).
 * - Haptic tap tactile feedback via MetroHaptics on toggle.
 */
@Composable
fun MetroCollapsibleSection(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    initiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { MetroHaptics(context) }
    val isDark = LocalMetroIsDark.current
    val accentColor = LocalMetroAccent.current.color
    val textColor = if (isDark) MetroWhite else MetroBlack
    val borderColor = if (isDark) MetroDarkBorder else Color(0xFFE2E2E2)

    var expanded by remember { mutableStateOf(initiallyExpanded) }

    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "chevron_rotation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // --- 1. Subtle Metro Header Row ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    haptics.tap()
                    expanded = !expanded
                }
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Subtle vertical accent indicator when expanded
                if (expanded) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(20.dp)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Column {
                    Text(
                        text = title.lowercase(),
                        style = MetroTypography.headlineSmall.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle.lowercase(),
                            style = MetroTypography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = textColor.copy(alpha = 0.55f)
                            )
                        )
                    }
                }
            }

            // Monoline sharp chevron indicator
            Canvas(
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer { rotationZ = chevronRotation }
            ) {
                val strokeW = 1.8.dp.toPx()
                val iconColor = if (expanded) accentColor else textColor.copy(alpha = 0.6f)
                val w = size.width
                val h = size.height

                val path = Path().apply {
                    moveTo(w * 0.28f, h * 0.40f)
                    lineTo(w * 0.50f, h * 0.62f)
                    lineTo(w * 0.72f, h * 0.40f)
                }
                drawPath(
                    path = path,
                    color = iconColor,
                    style = Stroke(
                        width = strokeW,
                        cap = StrokeCap.Square,
                        join = StrokeJoin.Miter
                    )
                )
            }
        }

        // Subtle thin 1dp separator under the header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(borderColor)
        )

        // --- 2. Expandable Section Content ---
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(
                animationSpec = tween(200),
                expandFrom = Alignment.Top
            ) + fadeIn(animationSpec = tween(200)),
            exit = shrinkVertically(
                animationSpec = tween(200),
                shrinkTowards = Alignment.Top
            ) + fadeOut(animationSpec = tween(150))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                content()
            }
        }
    }
}

