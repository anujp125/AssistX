package com.assistx.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.assistx.ui.theme.LocalMetroIsDark
import com.assistx.ui.theme.MetroBlack
import com.assistx.ui.theme.MetroDarkBorder
import com.assistx.ui.theme.MetroDarkTile
import com.assistx.ui.theme.MetroTypography
import com.assistx.ui.theme.MetroWhite
import com.assistx.ui.theme.StatusAmber

/**
 * Modern UI (Metro) Flat Confirmation Dialog.
 *
 * Characteristics:
 * - Full-width flat panel with square corners (0dp radius).
 * - 2dp solid outline.
 * - Bold lowercase title.
 * - Explanatory message and optional warning container.
 * - Flat rectangular MetroButtons for primary confirmation and dismissal.
 */
@Composable
fun MetroConfirmationDialog(
    title: String,
    message: String,
    warningText: String? = null,
    confirmButtonText: String = "confirm",
    cancelButtonText: String = "cancel",
    isDangerOrWarning: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = LocalMetroIsDark.current
    val borderColor = if (isDangerOrWarning) StatusAmber else (if (isDark) MetroDarkBorder else Color(0xFFD0D0D0))
    val bgColor = if (isDark) MetroBlack else MetroWhite
    val textColor = if (isDark) MetroWhite else MetroBlack

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .border(2.dp, borderColor, RectangleShape)
                .background(bgColor, RectangleShape)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title
                Text(
                    text = title,
                    style = MetroTypography.headlineSmall.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDangerOrWarning) StatusAmber else textColor
                    )
                )

                // Message
                Text(
                    text = message,
                    style = MetroTypography.bodySmall.copy(
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = textColor.copy(alpha = 0.85f)
                    )
                )

                // Optional Warning Text (e.g. Accessibility warning)
                if (warningText != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, StatusAmber.copy(alpha = 0.6f), RectangleShape)
                            .background(if (isDark) MetroDarkTile else Color(0xFFFFFBEB))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = warningText,
                            style = MetroTypography.bodySmall.copy(
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = if (isDark) MetroWhite.copy(alpha = 0.9f) else Color(0xFF92400E)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetroButton(
                        text = cancelButtonText,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    MetroButton(
                        text = confirmButtonText,
                        onClick = onConfirm,
                        isDanger = isDangerOrWarning,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

