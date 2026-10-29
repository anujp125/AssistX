package com.nudge.assist.ui

import android.content.Context
import android.os.Process
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nudge.assist.data.OverlaySettings
import com.nudge.assist.data.diagnostics.DiagnosticEventEntity
import com.nudge.assist.state.AccessibilityState
import com.nudge.assist.state.AppStateSnapshot
import com.nudge.assist.state.DesiredState
import com.nudge.assist.state.OverlayPermissionState
import com.nudge.assist.state.OverlayState
import com.nudge.assist.state.UserFacingStatus
import com.nudge.assist.ui.theme.StatusAmber
import com.nudge.assist.ui.theme.StatusBlue
import com.nudge.assist.ui.theme.StatusGreen
import com.nudge.assist.ui.theme.StatusRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class StudioTab(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    SETTINGS("Settings", Icons.Default.Tune),
    DIAGNOSTICS("Diagnostics", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(viewModel: StudioViewModel? = null) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(StudioTab.DASHBOARD) }

    val snapshot by viewModel?.snapshot?.collectAsState() ?: remember {
        mutableStateOf(AppStateSnapshot())
    }
    val userFacingStatus by viewModel?.userFacingStatus?.collectAsState() ?: remember {
        mutableStateOf(UserFacingStatus.DISABLED)
    }
    val settings by viewModel?.settings?.collectAsState() ?: remember {
        mutableStateOf(OverlaySettings())
    }
    val recentDiagnostics by viewModel?.recentDiagnostics?.collectAsState() ?: remember {
        mutableStateOf(emptyList<DiagnosticEventEntity>())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AssistX", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            when (currentTab) {
                                StudioTab.DASHBOARD -> "Control & Subsystem Dashboard"
                                StudioTab.SETTINGS -> "Preferences & Customization"
                                StudioTab.DIAGNOSTICS -> "Local Diagnostics & Runtime Health"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                StudioTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                StudioTab.DASHBOARD -> DashboardTabContent(
                    viewModel = viewModel,
                    snapshot = snapshot,
                    userFacingStatus = userFacingStatus,
                    context = context
                )
                StudioTab.SETTINGS -> SettingsTabContent(
                    viewModel = viewModel,
                    settings = settings
                )
                StudioTab.DIAGNOSTICS -> DiagnosticsTabContent(
                    viewModel = viewModel,
                    snapshot = snapshot,
                    recentDiagnostics = recentDiagnostics
                )
            }
        }
    }
}

@Composable
fun DashboardTabContent(
    viewModel: StudioViewModel?,
    snapshot: AppStateSnapshot,
    userFacingStatus: UserFacingStatus,
    context: Context
) {
    val isEnabled = snapshot.desiredState == DesiredState.ENABLED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Master Activation Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Assistive Overlay",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        StatusPill(status = userFacingStatus)
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { viewModel?.onToggleMaster(it) }
                    )
                }
            }
        }

        // 2. Subsystem Readiness
        Text(
            text = "Subsystem Readiness",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Overlay Permission Check
        val isOverlayGranted = snapshot.overlayPermissionState == OverlayPermissionState.GRANTED
        SubsystemCard(
            title = "Overlay Permission",
            statusText = when (snapshot.overlayPermissionState) {
                OverlayPermissionState.GRANTED -> "Granted — Display over apps active"
                OverlayPermissionState.REQUIRED -> "Action Required — Overlay access needed"
                OverlayPermissionState.UNKNOWN -> "Checking..."
            },
            statusColor = if (isOverlayGranted) StatusGreen else StatusAmber,
            icon = if (isOverlayGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
            actionButton = if (!isOverlayGranted) {
                {
                    Button(
                        onClick = { viewModel?.openOverlaySettings(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Grant")
                    }
                }
            } else null
        )

        // Accessibility Service Check
        val isAccConnected = snapshot.accessibilityState == AccessibilityState.ENABLED_CONNECTED
        SubsystemCard(
            title = "Accessibility Service",
            statusText = when (snapshot.accessibilityState) {
                AccessibilityState.ENABLED_CONNECTED -> "Connected — Screen lock & Home actions ready"
                AccessibilityState.ENABLED_DISCONNECTED -> "Disconnected — Tap to reconnect in settings"
                AccessibilityState.DISABLED -> "Disabled — Required for screen lock action"
                AccessibilityState.NOT_REQUIRED -> "Not required"
                AccessibilityState.UNKNOWN -> "Checking..."
            },
            statusColor = when (snapshot.accessibilityState) {
                AccessibilityState.ENABLED_CONNECTED -> StatusGreen
                AccessibilityState.ENABLED_DISCONNECTED -> StatusAmber
                AccessibilityState.DISABLED -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.outline
            },
            icon = when (snapshot.accessibilityState) {
                AccessibilityState.ENABLED_CONNECTED -> Icons.Default.CheckCircle
                AccessibilityState.ENABLED_DISCONNECTED -> Icons.Default.Warning
                AccessibilityState.DISABLED -> Icons.Default.Error
                else -> Icons.Default.Info
            },
            actionButton = if (!isAccConnected) {
                {
                    OutlinedButton(
                        onClick = { viewModel?.openAccessibilitySettings(context) }
                    ) {
                        Text("Configure")
                    }
                }
            } else null
        )

        // Floating Overlay Window State
        val isOverlayAttached = snapshot.overlayState == OverlayState.ATTACHED
        SubsystemCard(
            title = "Floating Window",
            statusText = when (snapshot.overlayState) {
                OverlayState.ATTACHED -> "Attached on screen"
                OverlayState.ATTACHING -> "Attaching..."
                OverlayState.DETACHING -> "Detaching..."
                OverlayState.DETACHED -> "Detached (Not visible)"
                OverlayState.ERROR -> "Window error encountered"
                OverlayState.UNKNOWN -> "Unknown"
            },
            statusColor = if (isOverlayAttached) StatusGreen else MaterialTheme.colorScheme.outline,
            icon = if (isOverlayAttached) Icons.Default.CheckCircle else Icons.Default.Info
        )

        // 3. Gestures Reference
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Configured Gestures & Popout Actions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Single-Tap: Opens circular popout (Screenshot, Volume, Power Dialog)\n• Double-Tap: Immediate screen lock (via Accessibility)\n• Long-Press (500ms): Return to Home\n• Drag: Reposition anywhere with automatic edge snap",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun SettingsTabContent(
    viewModel: StudioViewModel?,
    settings: OverlaySettings
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Appearance & Sizing",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Button Size Picker Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Floating Button Size",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Current: ${settings.buttonSizeDp} dp",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sizes = listOf(48 to "Compact (48dp)", 56 to "Standard (56dp)", 64 to "Large (64dp)")
                    sizes.forEach { (sizeDp, label) ->
                        val selected = settings.buttonSizeDp == sizeDp
                        if (selected) {
                            Button(
                                onClick = { viewModel?.updateButtonSize(sizeDp) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(label, fontSize = 12.sp)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel?.updateButtonSize(sizeDp) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(label, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Idle Opacity Slider Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Idle Opacity",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${settings.idleOpacityPercent}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Transparency when button is inactive",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = settings.idleOpacityPercent.toFloat(),
                    onValueChange = { viewModel?.updateIdleOpacity(it.toInt()) },
                    valueRange = 20f..100f,
                    steps = 15
                )
            }
        }

        // Idle Fade Delay Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Idle Fade Delay",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Wait ${settings.idleFadeSeconds}s before fading to idle opacity",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 2, 3, 5).forEach { seconds ->
                        val selected = settings.idleFadeSeconds == seconds
                        if (selected) {
                            Button(
                                onClick = { viewModel?.updateIdleFadeDelay(seconds) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("${seconds}s")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel?.updateIdleFadeDelay(seconds) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("${seconds}s")
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = "Behavior & Interactions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Popout Dismiss Timeout Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Popout Menu Auto-Dismiss Timeout",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Dismisses after ${settings.popoutDismissSeconds}s of inactivity",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(2, 3, 4, 6).forEach { seconds ->
                        val selected = settings.popoutDismissSeconds == seconds
                        if (selected) {
                            Button(
                                onClick = { viewModel?.updatePopoutDismissDelay(seconds) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("${seconds}s")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel?.updatePopoutDismissDelay(seconds) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("${seconds}s")
                            }
                        }
                    }
                }
            }
        }

        // Haptic Feedback Switch Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Haptic Feedback",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Vibrate on button taps and gesture releases",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.hapticFeedbackEnabled,
                    onCheckedChange = { viewModel?.updateHapticFeedback(it) }
                )
            }
        }

        // Snap to Edge Switch Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Snap to Screen Edge",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Automatically snap to left or right screen edge when released",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.snapToEdgeEnabled,
                    onCheckedChange = { viewModel?.updateSnapToEdge(it) }
                )
            }
        }

        // Reset to Defaults Button
        OutlinedButton(
            onClick = { viewModel?.resetSettingsToDefault() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reset All Preferences to Defaults")
        }
    }
}

@Composable
fun DiagnosticsTabContent(
    viewModel: StudioViewModel?,
    snapshot: AppStateSnapshot,
    recentDiagnostics: List<DiagnosticEventEntity>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "System & Runtime Health",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Runtime Stats Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Process ID (PID)", style = MaterialTheme.typography.bodySmall)
                    Text("${Process.myPid()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Runtime State", style = MaterialTheme.typography.bodySmall)
                    Text("${snapshot.runtimeState}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Overlay Window State", style = MaterialTheme.typography.bodySmall)
                    Text("${snapshot.overlayState}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Accessibility Service", style = MaterialTheme.typography.bodySmall)
                    Text("${snapshot.accessibilityState}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Local Diagnostic Events",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Stored locally in Room database • Zero network transmission",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = { viewModel?.onClearDiagnostics() }) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            if (recentDiagnostics.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No diagnostic events recorded yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                Column(modifier = Modifier.padding(8.dp)) {
                    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    recentDiagnostics.take(25).forEachIndexed { index, event ->
                        DiagnosticEventRow(event = event, dateFormat = dateFormat)
                        if (index < recentDiagnostics.take(25).lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusPill(status: UserFacingStatus) {
    val (bgColor, textColor, text, icon) = when (status) {
        UserFacingStatus.ACTIVE -> Quad(StatusGreen.copy(alpha = 0.15f), StatusGreen, "Active", Icons.Default.CheckCircle)
        UserFacingStatus.DISABLED -> Quad(Color.Gray.copy(alpha = 0.15f), Color.DarkGray, "Disabled", Icons.Default.Info)
        UserFacingStatus.STARTING -> Quad(StatusBlue.copy(alpha = 0.15f), StatusBlue, "Starting...", Icons.Default.Refresh)
        UserFacingStatus.OVERLAY_PERMISSION_REQUIRED -> Quad(StatusAmber.copy(alpha = 0.15f), StatusAmber, "Permission Required", Icons.Default.Warning)
        UserFacingStatus.ACCESSIBILITY_DISABLED -> Quad(StatusAmber.copy(alpha = 0.15f), StatusAmber, "Accessibility Disabled", Icons.Default.Warning)
        UserFacingStatus.ACCESSIBILITY_DISCONNECTED -> Quad(StatusAmber.copy(alpha = 0.15f), StatusAmber, "Accessibility Disconnected", Icons.Default.Warning)
        UserFacingStatus.INTERRUPTED -> Quad(StatusAmber.copy(alpha = 0.15f), StatusAmber, "Interrupted", Icons.Default.Warning)
        UserFacingStatus.RECOVERING -> Quad(StatusBlue.copy(alpha = 0.15f), StatusBlue, "Recovering...", Icons.Default.Refresh)
        UserFacingStatus.RECOVERY_REQUIRED -> Quad(StatusRed.copy(alpha = 0.15f), StatusRed, "Recovery Required", Icons.Default.Error)
        UserFacingStatus.CHECKING_STATUS -> Quad(Color.Gray.copy(alpha = 0.15f), Color.Gray, "Checking Status", Icons.Default.Info)
    }

    Row(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun SubsystemCard(
    title: String,
    statusText: String,
    statusColor: Color,
    icon: ImageVector,
    actionButton: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (actionButton != null) {
                actionButton()
            }
        }
    }
}

@Composable
fun DiagnosticEventRow(
    event: DiagnosticEventEntity,
    dateFormat: SimpleDateFormat
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dateFormat.format(Date(event.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "[${event.component}]",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = event.eventType,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (event.details.isNotBlank()) {
                Text(
                    text = event.details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = event.result,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (event.result == "SUCCESS") StatusGreen else StatusRed
        )
    }
}
