package com.nudge.assist.ui

import android.os.Process
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nudge.assist.R
import com.nudge.assist.data.diagnostics.DiagnosticEventEntity
import com.nudge.assist.state.AccessibilityState
import com.nudge.assist.state.AppStateSnapshot
import com.nudge.assist.state.DesiredState
import com.nudge.assist.state.OverlayPermissionState
import com.nudge.assist.state.OverlayState
import com.nudge.assist.state.UserFacingStatus
import com.nudge.assist.ui.components.MetroButton
import com.nudge.assist.ui.haptics.MetroHaptics
import com.nudge.assist.ui.theme.LocalMetroAccent
import com.nudge.assist.ui.theme.LocalMetroIsDark
import com.nudge.assist.ui.theme.MetroBlack
import com.nudge.assist.ui.theme.MetroDarkBorder
import com.nudge.assist.ui.theme.MetroDarkTile
import com.nudge.assist.ui.theme.MetroTypography
import com.nudge.assist.ui.theme.MetroWhite
import com.nudge.assist.ui.theme.StatusAmber
import com.nudge.assist.ui.theme.StatusGreen
import com.nudge.assist.ui.theme.StatusRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Metro Modern UI Diagnostics Screen.
 *
 * Characteristics:
 * - Evidence-focused diagnostic telemetry.
 * - Consistent monoline thin-line icon family (ic_diag_*).
 * - Flat status indicator badges (green / amber / red / dimmed).
 * - Text-led rows with primary and secondary lines (no heavy cards or drop shadows).
 */
@Composable
fun DiagnosticsScreen(
    snapshot: AppStateSnapshot,
    userFacingStatus: UserFacingStatus,
    recentDiagnostics: List<DiagnosticEventEntity>,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = remember { MetroHaptics(context) }
    val accentColor = LocalMetroAccent.current.color
    val isDark = LocalMetroIsDark.current

    val textColor = if (isDark) MetroWhite else MetroBlack
    val borderColor = if (isDark) MetroDarkBorder else Color(0xFFE0E0E0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section: Subsystem Readiness Facts
        item {
            Text(
                text = "subsystem telemetry",
                style = MetroTypography.headlineSmall.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor.copy(alpha = 0.85f)
                ),
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        // Subsystem Telemetry Group (Single cohesive flat container with hairline dividers)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, borderColor, RectangleShape)
                    .background(if (isDark) MetroDarkTile else Color(0xFFF7F7F7), RectangleShape)
            ) {
                // 1. Overlay Permission
                val isGranted = snapshot.overlayPermissionState == OverlayPermissionState.GRANTED
                MetroDiagRowContent(
                    icon = painterResource(id = R.drawable.ic_diag_overlay_perm),
                    title = "system alert window",
                    subtitle = if (isGranted) "granted (SYSTEM_ALERT_WINDOW active)" else "permission missing (ACTION_MANAGE_OVERLAY_PERMISSION required)",
                    statusText = if (isGranted) "ready" else "missing",
                    statusColor = if (isGranted) StatusGreen else StatusAmber
                )

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(borderColor.copy(alpha = 0.35f)))

                // 2. Floating Window Attachment
                val isAttached = snapshot.overlayState == OverlayState.ATTACHED
                MetroDiagRowContent(
                    icon = painterResource(id = R.drawable.ic_diag_overlay_attached),
                    title = "floating overlay view",
                    subtitle = if (isAttached) "attached to WindowManager (active on screen)" else "detached (${snapshot.overlayState.name.lowercase()})",
                    statusText = if (isAttached) "attached" else "detached",
                    statusColor = if (isAttached) StatusGreen else StatusAmber
                )

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(borderColor.copy(alpha = 0.35f)))

                // 3. Accessibility Service Connection
                val isConnected = snapshot.accessibilityState == AccessibilityState.ENABLED_CONNECTED
                MetroDiagRowContent(
                    icon = painterResource(id = R.drawable.ic_diag_accessibility_connected),
                    title = "accessibility service",
                    subtitle = if (isConnected) "connected and receiving gesture dispatch events" else "disconnected (${snapshot.accessibilityState.name.lowercase()})",
                    statusText = if (isConnected) "connected" else "disconnected",
                    statusColor = if (isConnected) StatusGreen else StatusAmber
                )

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(borderColor.copy(alpha = 0.35f)))

                // 4. Runtime Service & Desired State
                val isDesiredOn = snapshot.desiredState == DesiredState.ENABLED
                val isPaused = snapshot.desiredState == DesiredState.PAUSED_FOR_PAYMENTS
                val isHealthy = userFacingStatus == UserFacingStatus.ACTIVE
                val desiredLabel = when (snapshot.desiredState) {
                    DesiredState.ENABLED -> "on"
                    DesiredState.PAUSED_FOR_PAYMENTS -> "paused for payments"
                    DesiredState.DISABLED -> "off"
                }
                MetroDiagRowContent(
                    icon = painterResource(id = R.drawable.ic_diag_runtime_health),
                    title = "runtime state engine",
                    subtitle = "desired: $desiredLabel | user status: ${userFacingStatus.label.lowercase()}",
                    statusText = if (isHealthy) "healthy" else (if (isPaused) "paused" else if (!isDesiredOn) "disabled" else "attention"),
                    statusColor = if (isHealthy) StatusGreen else (if (isPaused) StatusAmber else if (!isDesiredOn) textColor.copy(alpha = 0.5f) else StatusAmber)
                )

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(borderColor.copy(alpha = 0.35f)))

                // 5. Hardware & Process Telemetry
                val runtime = Runtime.getRuntime()
                val usedMemMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
                val maxMemMb = runtime.maxMemory() / (1024 * 1024)
                MetroDiagRowContent(
                    icon = painterResource(id = R.drawable.ic_diag_memory),
                    title = "process memory",
                    subtitle = "pid: ${Process.myPid()} | heap used: ${usedMemMb}MB / ${maxMemMb}MB",
                    statusText = "nominal",
                    statusColor = StatusGreen
                )
            }
        }

        // Section: Local Room Diagnostics Log
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "event log (${recentDiagnostics.size})",
                    style = MetroTypography.headlineSmall.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor.copy(alpha = 0.85f)
                    )
                )

                if (recentDiagnostics.isNotEmpty()) {
                    Text(
                        text = "clear",
                        style = MetroTypography.labelSmall.copy(
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        modifier = Modifier
                            .clickable {
                                haptics.tap()
                                onClearLogs()
                            }
                            .padding(4.dp)
                    )
                }
            }
        }

        if (recentDiagnostics.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, borderColor, RectangleShape)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "no diagnostic events logged yet",
                        style = MetroTypography.bodyMedium.copy(
                            color = textColor.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        } else {
            items(recentDiagnostics) { event ->
                val timeFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.US) }
                val timeStr = remember(event.timestamp) {
                    timeFormat.format(Date(event.timestamp))
                }

                val eventColor = when (event.severity.uppercase()) {
                    "ERROR" -> StatusRed
                    "WARN", "WARNING" -> StatusAmber
                    else -> textColor.copy(alpha = 0.8f)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = timeStr,
                                style = MetroTypography.labelSmall.copy(
                                    color = textColor.copy(alpha = 0.5f),
                                    fontSize = 11.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            )
                            Text(
                                text = event.eventType.lowercase(),
                                style = MetroTypography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = eventColor,
                                    fontSize = 13.sp
                                )
                            )
                        }
                        Text(
                            text = event.severity.lowercase(),
                            style = MetroTypography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = eventColor,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = event.details,
                        style = MetroTypography.bodyMedium.copy(
                            color = textColor.copy(alpha = 0.75f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(borderColor.copy(alpha = 0.25f))
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Text-led Metro Diagnostics Telemetry Row Content.
 * Zero artificial circle dots or pill badges.
 */
@Composable
private fun MetroDiagRowContent(
    icon: Painter,
    title: String,
    subtitle: String,
    statusText: String,
    statusColor: Color
) {
    val isDark = LocalMetroIsDark.current
    val textColor = if (isDark) MetroWhite else MetroBlack

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = title,
            tint = textColor.copy(alpha = 0.8f),
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title.lowercase(),
                style = MetroTypography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    fontSize = 15.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MetroTypography.bodyMedium.copy(
                    color = textColor.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Status Text (No colored dots - clean Metro label)
        Text(
            text = statusText.lowercase(),
            style = MetroTypography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = statusColor,
                fontSize = 12.sp
            )
        )
    }
}
