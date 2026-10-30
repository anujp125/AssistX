package com.nudge.assist.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nudge.assist.R
import com.nudge.assist.ui.theme.LocalMetroAccent
import com.nudge.assist.ui.theme.LocalMetroIsDark
import com.nudge.assist.ui.theme.MetroBlack
import com.nudge.assist.ui.theme.MetroTypography
import com.nudge.assist.ui.theme.MetroWhite

/**
 * Reusable Metro Modern UI Header.
 *
 * Characteristics:
 * - Edge-to-edge status bar handling.
 * - Flat geometric logo mark using the dynamic Metro accent color.
 * - Small uppercase product name: "ASSISTX" (letter-spaced).
 * - Large lowercase page title (40sp Light Selawik), left-aligned to the 24dp Metro grid.
 */
@Composable
fun MetroHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    val accentColor = LocalMetroAccent.current.color
    val isDark = LocalMetroIsDark.current
    val textColor = if (isDark) MetroWhite else MetroBlack

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp)
    ) {
        // Product Branding Row: Logo + Small Uppercase "ASSISTX"
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_assistx_logo),
                contentDescription = "AssistX Logo",
                colorFilter = ColorFilter.tint(accentColor),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ASSISTX",
                style = MetroTypography.labelSmall.copy(
                    letterSpacing = 2.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor.copy(alpha = 0.65f)
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Large Lowercase Page Title (Metro Light 40sp)
        Text(
            text = title.lowercase(),
            style = MetroTypography.displayLarge.copy(
                fontSize = 42.sp,
                color = textColor
            )
        )
    }
}
