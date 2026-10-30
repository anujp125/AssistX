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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nudge.assist.ui.haptics.MetroHaptics
import com.nudge.assist.ui.theme.LocalMetroAccent
import com.nudge.assist.ui.theme.LocalMetroIsDark
import com.nudge.assist.ui.theme.MetroBlack
import com.nudge.assist.ui.theme.MetroDarkBorder
import com.nudge.assist.ui.theme.MetroTypography
import com.nudge.assist.ui.theme.MetroWhite

data class MetroAppBarItem(
    val id: String,
    val label: String,
    val iconPainter: Painter? = null,
    val iconVector: ImageVector? = null
)

/**
 * Custom Metro-style Bottom App Bar.
 *
 * Characteristics:
 * - Minimal row of circular outlined icons (36dp circle, 1.5dp stroke).
 * - Accent-colored active state.
 * - Minimalist lowercase labels.
 * - Edge-to-edge navigation bar padding.
 * - Flat solid background, zero elevation or drop shadows.
 */
@Composable
fun MetroAppBar(
    items: List<MetroAppBarItem>,
    selectedId: String,
    onItemSelected: (String) -> Unit,
    onOverflowClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = LocalMetroIsDark.current
    val bgColor = if (isDark) MetroBlack else MetroWhite
    val topBorderColor = if (isDark) Color(0xFF1F1F1F) else Color(0xFFE5E5E5)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
    ) {
        // Thin 1dp Metro divider line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(topBorderColor)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                MetroAppBarButton(
                    item = item,
                    isSelected = item.id == selectedId,
                    onClick = { onItemSelected(item.id) }
                )
            }

            if (onOverflowClick != null) {
                MetroAppBarOverflowButton(onClick = onOverflowClick)
            }
        }
    }
}

@Composable
private fun MetroAppBarButton(
    item: MetroAppBarItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { MetroHaptics(context) }
    val accentColor = LocalMetroAccent.current.color
    val isDark = LocalMetroIsDark.current

    val inactiveBorderColor = if (isDark) MetroDarkBorder else Color(0xFFD0D0D0)
    val inactiveContentColor = if (isDark) MetroWhite.copy(alpha = 0.55f) else MetroBlack.copy(alpha = 0.55f)

    val itemBorderColor = if (isSelected) accentColor else inactiveBorderColor
    val itemContentColor = if (isSelected) accentColor else inactiveContentColor
    val itemBgColor = if (isSelected) accentColor.copy(alpha = 0.12f) else Color.Transparent

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptics.select()
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        // Circular Outlined Button (36dp)
        Box(
            modifier = Modifier
                .size(36.dp)
                .border(1.5.dp, itemBorderColor, CircleShape)
                .background(itemBgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (item.iconPainter != null) {
                Icon(
                    painter = item.iconPainter,
                    contentDescription = item.label,
                    tint = itemContentColor,
                    modifier = Modifier.size(18.dp)
                )
            } else if (item.iconVector != null) {
                Icon(
                    imageVector = item.iconVector,
                    contentDescription = item.label,
                    tint = itemContentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.label.lowercase(),
            style = MetroTypography.labelSmall.copy(
                fontSize = 11.sp,
                color = itemContentColor
            )
        )
    }
}

@Composable
private fun MetroAppBarOverflowButton(
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val haptics = remember { MetroHaptics(context) }
    val isDark = LocalMetroIsDark.current

    val inactiveBorderColor = if (isDark) MetroDarkBorder else Color(0xFFD0D0D0)
    val inactiveContentColor = if (isDark) MetroWhite.copy(alpha = 0.55f) else MetroBlack.copy(alpha = 0.55f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptics.tap()
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .border(1.5.dp, inactiveBorderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = "more",
                tint = inactiveContentColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "more",
            style = MetroTypography.labelSmall.copy(
                fontSize = 11.sp,
                color = inactiveContentColor
            )
        )
    }
}
