package com.assistx.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue
import com.assistx.R
import com.assistx.data.OverlaySettings
import com.assistx.data.diagnostics.DiagnosticEventEntity
import com.assistx.state.AccessibilityState
import com.assistx.state.AppStateSnapshot
import com.assistx.state.DesiredState
import com.assistx.state.OverlayPermissionState
import com.assistx.state.OverlayState
import com.assistx.state.UserFacingStatus
import com.assistx.ui.components.MetroAccentPicker
import com.assistx.ui.components.MetroAppBar
import com.assistx.ui.components.MetroAppBarItem
import com.assistx.ui.components.MetroButton
import com.assistx.ui.components.MetroCollapsibleSection
import com.assistx.ui.components.MetroConfirmationDialog
import com.assistx.ui.components.MetroHeader
import com.assistx.ui.components.MetroOption
import com.assistx.ui.components.MetroSegmentedPicker
import com.assistx.ui.components.MetroSlider
import com.assistx.ui.components.MetroTile
import com.assistx.ui.components.MetroTileStyle
import com.assistx.ui.components.MetroToggle
import com.assistx.ui.haptics.MetroHaptics
import com.assistx.ui.theme.LocalMetroAccent
import com.assistx.ui.theme.LocalMetroIsDark
import com.assistx.ui.theme.MetroBlack
import com.assistx.ui.theme.MetroDarkBorder
import com.assistx.ui.theme.MetroDarkTile
import com.assistx.ui.theme.MetroTypography
import com.assistx.ui.theme.MetroWhite
import com.assistx.ui.theme.StatusAmber
import com.assistx.ui.theme.StatusGreen
import com.assistx.ui.theme.StatusRed
import com.assistx.ui.theme.ThemeMode

enum class MetroAppScreen {
    STUDIO,
    DIAGNOSTICS
}

enum class StudioPivotSection {
    OVERVIEW,
    SETTINGS
}

/**
 * Classic Windows Phone Metro easing curve: Fast-in, slow-out deceleration.
 * Rapidly initiates motion with responsive urgency, then smoothly glides into the resting position.
 */
private val MetroPivotEasing = CubicBezierEasing(0.10f, 0.90f, 0.20f, 1.0f)

private val MetroPivotAnimationSpec = tween<Float>(
    durationMillis = 380,
    easing = MetroPivotEasing
)

/**
 * Merged Windows Phone "Metro" Studio Screen.
 *
 * Characteristics:
 * - Studio merges Dashboard and Settings into a Metro Pivot layout ("overview" | "settings").
 * - Modern Metro Live Tiles grid on Overview (2 columns, 8dp gutters, 24dp left-aligned grid).
 * - Polished Customization settings on Settings pivot (20 accent swatches, 3-way theme, live button preview).
 * - Custom bottom Metro app bar with circular outlined icons and edge-to-edge insets.
 */
@Composable
fun StudioScreen(viewModel: StudioViewModel? = null) {
    val context = LocalContext.current
    val haptics = remember { MetroHaptics(context) }
    val accentColor = LocalMetroAccent.current.color
    val isDark = LocalMetroIsDark.current

    var currentScreen by remember { mutableStateOf(MetroAppScreen.STUDIO) }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { StudioPivotSection.values().size }
    )
    val coroutineScope = rememberCoroutineScope()

    var isInitialLaunch by remember { mutableStateOf(true) }
    LaunchedEffect(pagerState.currentPage) {
        if (isInitialLaunch) {
            isInitialLaunch = false
        } else {
            haptics.select()
        }
    }

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

    var showPauseConfirmDialog by remember { mutableStateOf(false) }

    var settingsEntryKey by remember { mutableIntStateOf(0) }
    var wasInSettings by remember { mutableStateOf(false) }

    val isCurrentlyInSettings = currentScreen == MetroAppScreen.STUDIO && pagerState.currentPage == 1

    LaunchedEffect(isCurrentlyInSettings) {
        if (isCurrentlyInSettings && !wasInSettings) {
            settingsEntryKey++
        }
        wasInSettings = isCurrentlyInSettings
    }

    // Configure haptics master state from settings
    haptics.isEnabled = settings.hapticFeedbackEnabled

    val navItems = remember {
        listOf(
            MetroAppBarItem(
                id = MetroAppScreen.STUDIO.name,
                label = "studio",
                iconVector = Icons.Default.Widgets
            ),
            MetroAppBarItem(
                id = MetroAppScreen.DIAGNOSTICS.name,
                label = "diagnostics",
                iconPainter = null
            )
        )
    }

    if (showPauseConfirmDialog) {
        MetroConfirmationDialog(
            title = "Pause AssistX for payments?",
            message = "AssistX will detach its floating button and attempt to stop its Accessibility service. Screen-off and Home gestures will be unavailable while AssistX is paused.",
            warningText = "Note: To restore these gestures, you may need to re-enable AssistX in Android Accessibility settings.",
            confirmButtonText = "Pause AssistX",
            cancelButtonText = "Cancel",
            isDangerOrWarning = true,
            onConfirm = {
                showPauseConfirmDialog = false
                viewModel?.onPauseForPayments()
            },
            onDismiss = {
                showPauseConfirmDialog = false
            }
        )
    }

    val scaffoldBg = if (isDark) MetroBlack else MetroWhite

    Scaffold(
        containerColor = scaffoldBg,
        topBar = {
            MetroHeader(
                title = if (currentScreen == MetroAppScreen.STUDIO) "studio" else "diagnostics"
            )
        },
        bottomBar = {
            MetroAppBar(
                items = listOf(
                    MetroAppBarItem(
                        id = MetroAppScreen.STUDIO.name,
                        label = "studio",
                        iconVector = Icons.Default.Widgets
                    ),
                    MetroAppBarItem(
                        id = MetroAppScreen.DIAGNOSTICS.name,
                        label = "diagnostics",
                        iconPainter = painterResource(id = R.drawable.ic_diag_runtime_health)
                    )
                ),
                selectedId = currentScreen.name,
                onItemSelected = { id ->
                    val targetScreen = MetroAppScreen.valueOf(id)
                    if (currentScreen == MetroAppScreen.STUDIO && targetScreen == MetroAppScreen.STUDIO && pagerState.currentPage != 0) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0, animationSpec = MetroPivotAnimationSpec)
                        }
                    }
                    currentScreen = targetScreen
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                MetroAppScreen.STUDIO -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Continuous scroll fraction for silky-smooth panoramic header tracking
                        val continuousPosition by remember {
                            derivedStateOf {
                                pagerState.currentPage + pagerState.currentPageOffsetFraction
                            }
                        }

                        // Metro Pivot Headers: "overview" | "settings" with panoramic parallax
                        MetroPivotBar(
                            currentPosition = continuousPosition,
                            onSelect = { section ->
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(
                                        page = section.ordinal,
                                        animationSpec = MetroPivotAnimationSpec
                                    )
                                }
                            }
                        )

                        // Panoramic Horizontal Pager with native fling behavior and layered depth
                        val flingBehavior = PagerDefaults.flingBehavior(
                            state = pagerState,
                            snapAnimationSpec = MetroPivotAnimationSpec
                        )

                        HorizontalPager(
                            state = pagerState,
                            flingBehavior = flingBehavior,
                            pageSpacing = 16.dp,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) { page ->
                            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                            val parallaxFraction = pageOffset.coerceIn(-1f, 1f)

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        // Subtle panoramic parallax translation (0.12x speed differential)
                                        translationX = parallaxFraction * size.width * 0.12f
                                        // Elegant focus dimming as the section recedes
                                        alpha = (1f - (parallaxFraction.absoluteValue * 0.35f)).coerceIn(0.55f, 1f)
                                    }
                            ) {
                                when (StudioPivotSection.values()[page]) {
                                    StudioPivotSection.OVERVIEW -> StudioOverviewContent(
                                        snapshot = snapshot,
                                        userFacingStatus = userFacingStatus,
                                        settings = settings,
                                        onToggleMaster = {
                                            viewModel?.onToggleMaster(snapshot.desiredState != DesiredState.ENABLED)
                                        },
                                        onPauseForPayments = {
                                            showPauseConfirmDialog = true
                                        },
                                        onResumeFromPayments = {
                                            viewModel?.onResumeFromPayments()
                                        },
                                        onFixOverlay = {
                                            val intent = Intent(
                                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                Uri.parse("package:${context.packageName}")
                                            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                            context.startActivity(intent)
                                        },
                                        onFixAccessibility = {
                                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(intent)
                                        }
                                    )

                                    StudioPivotSection.SETTINGS -> key(settingsEntryKey) {
                                        StudioSettingsContent(
                                            settings = settings,
                                            snapshot = snapshot,
                                            viewModel = viewModel,
                                            onRequestPauseForPayments = {
                                                showPauseConfirmDialog = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                MetroAppScreen.DIAGNOSTICS -> {
                    DiagnosticsScreen(
                        snapshot = snapshot,
                        userFacingStatus = userFacingStatus,
                        recentDiagnostics = recentDiagnostics,
                        onClearLogs = { viewModel?.onClearDiagnostics() }
                    )
                }
            }
        }
    }
}

/**
 * Metro Pivot Bar ("overview" | "settings").
 * - Fluid continuous parallax drift matching classic Windows Phone Panorama viewports.
 * - Dynamic alpha and typographic focus cross-fade linked directly to scroll offset.
 */
@Composable
private fun MetroPivotBar(
    currentPosition: Float,
    onSelect: (StudioPivotSection) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = remember { MetroHaptics(context) }
    val isDark = LocalMetroIsDark.current
    val textColor = if (isDark) MetroWhite else MetroBlack

    val maxIndex = (StudioPivotSection.values().size - 1).toFloat()
    val clampedPos = currentPosition.coerceIn(0f, maxIndex)
    // Parallax drift: the header row smoothly shifts by up to 28dp to the left as you navigate to settings
    val headerParallaxOffset = (-clampedPos * 28).dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .offset(x = headerParallaxOffset)
            .padding(start = 24.dp, end = 24.dp, top = 6.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StudioPivotSection.values().forEachIndexed { index, section ->
            val distance = (clampedPos - index).absoluteValue
            val selectionWeight = (1f - distance).coerceIn(0f, 1f)
            val alpha = 0.38f + (selectionWeight * 0.62f)
            val scale = 0.96f + (selectionWeight * 0.06f)
            val isSelected = distance < 0.5f

            Text(
                text = section.name.lowercase(),
                style = MetroTypography.headlineMedium.copy(
                    fontSize = 24.sp,
                    fontWeight = if (selectionWeight > 0.5f) FontWeight.SemiBold else FontWeight.Light,
                    color = textColor.copy(alpha = alpha)
                ),
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(0f, 0.5f)
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (!isSelected) {
                            haptics.select()
                            onSelect(section)
                        }
                    }
                    .padding(vertical = 4.dp)
            )
        }
    }
}

/**
 * Overview Section: 2-Column Metro Live Tiles Grid.
 */
@Composable
private fun StudioOverviewContent(
    snapshot: AppStateSnapshot,
    userFacingStatus: UserFacingStatus,
    settings: OverlaySettings,
    onToggleMaster: () -> Unit,
    onPauseForPayments: () -> Unit,
    onResumeFromPayments: () -> Unit,
    onFixOverlay: () -> Unit,
    onFixAccessibility: () -> Unit
) {
    val scrollState = rememberScrollState()
    val isDark = LocalMetroIsDark.current
    val accentColor = LocalMetroAccent.current.color
    val isAssistantOn = snapshot.desiredState == DesiredState.ENABLED
    val isPausedForPayments = snapshot.desiredState == DesiredState.PAUSED_FOR_PAYMENTS

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) MetroBlack else MetroWhite)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Tile 1: Master State Tile (Wide 2x1)
        MetroTile(
            label = if (isPausedForPayments) "payment safety mode" else "assistant desired state",
            height = 124.dp,
            style = if (isPausedForPayments) MetroTileStyle.STANDARD else (if (isAssistantOn) MetroTileStyle.ACCENT else MetroTileStyle.STANDARD),
            statusText = if (isPausedForPayments) "paused" else (if (isAssistantOn) "on" else "off"),
            statusColor = if (isPausedForPayments) StatusAmber else (if (isAssistantOn) MetroWhite else Color.Gray),
            onClick = onToggleMaster
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_action_power),
                    contentDescription = "power",
                    colorFilter = ColorFilter.tint(if (isPausedForPayments) StatusAmber else MetroWhite),
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = if (isPausedForPayments) "paused for payments" else (if (isAssistantOn) "assistant enabled" else "assistant disabled"),
                        style = MetroTypography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = if (isPausedForPayments) StatusAmber else MetroWhite
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isPausedForPayments) "tap tile or resume button below" else (if (isAssistantOn) "tap tile to disable runtime" else "tap tile to enable runtime"),
                        style = MetroTypography.bodySmall.copy(
                            color = (if (isPausedForPayments) (if (isDark) MetroWhite else MetroBlack) else MetroWhite).copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        if (isPausedForPayments) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, StatusAmber)
                    .background(if (isDark) MetroDarkTile else Color(0xFFFFFBEB))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "accessibility service disabled",
                        style = MetroTypography.labelSmall.copy(
                            color = StatusAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    )
                    Text(
                        text = "AssistX turned off its accessibility service and detached the floating overlay to minimize accessibility flags while running financial apps.",
                        style = MetroTypography.bodySmall.copy(
                            color = if (isDark) MetroWhite else MetroBlack,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )
                    Text(
                        text = "re-enable AssistX in Android settings to restore screen lock & home gestures:",
                        style = MetroTypography.bodySmall.copy(
                            color = (if (isDark) MetroWhite else MetroBlack).copy(alpha = 0.65f),
                            fontSize = 11.sp
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetroButton(
                            text = "open settings",
                            onClick = onFixAccessibility,
                            modifier = Modifier.weight(1f)
                        )
                        MetroButton(
                            text = "resume assistant",
                            onClick = onResumeFromPayments,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Row 2: Subsystem Health & Overlay Permission (2 columns)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tile 2: Runtime Health (1x1)
            val isHealthy = userFacingStatus == UserFacingStatus.ACTIVE
            MetroTile(
                label = "runtime health",
                modifier = Modifier.weight(1f),
                height = 116.dp,
                isCompact = true,
                statusText = if (isHealthy) "active" else (if (isPausedForPayments) "paused" else if (!isAssistantOn) "disabled" else "attention"),
                statusColor = if (isHealthy) StatusGreen else (if (isPausedForPayments) StatusAmber else if (!isAssistantOn) Color.Gray else StatusAmber)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_diag_runtime_health),
                    contentDescription = "runtime health",
                    tint = if (isHealthy) StatusGreen else (if (isDark) MetroWhite else MetroBlack),
                    modifier = Modifier.size(32.dp)
                )
            }

            // Tile 3: Overlay Permission (1x1)
            val isOverlayGranted = snapshot.overlayPermissionState == OverlayPermissionState.GRANTED
            MetroTile(
                label = "overlay permission",
                modifier = Modifier.weight(1f),
                height = 116.dp,
                isCompact = true,
                statusText = if (isOverlayGranted) "ready" else "action needed",
                statusColor = if (isOverlayGranted) StatusGreen else StatusAmber,
                onClick = if (!isOverlayGranted) onFixOverlay else null
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_diag_overlay_perm),
                    contentDescription = "overlay permission",
                    tint = if (isOverlayGranted) (if (isDark) MetroWhite else MetroBlack) else StatusAmber,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Row 3: Accessibility Connection & Floating Window (2 columns)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tile 4: Accessibility Service (1x1)
            val isA11yConnected = snapshot.accessibilityState == AccessibilityState.ENABLED_CONNECTED
            MetroTile(
                label = "accessibility",
                modifier = Modifier.weight(1f),
                height = 116.dp,
                isCompact = true,
                statusText = if (isA11yConnected) "connected" else "action needed",
                statusColor = if (isA11yConnected) StatusGreen else StatusAmber,
                onClick = if (!isA11yConnected) onFixAccessibility else null
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_diag_accessibility_connected),
                    contentDescription = "accessibility",
                    tint = if (isA11yConnected) (if (isDark) MetroWhite else MetroBlack) else StatusAmber,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Tile 5: Floating Window Attachment (1x1)
            val isOverlayAttached = snapshot.overlayState == OverlayState.ATTACHED
            MetroTile(
                label = "floating window",
                modifier = Modifier.weight(1f),
                height = 116.dp,
                isCompact = true,
                statusText = if (isOverlayAttached) "attached" else "detached",
                statusColor = if (isOverlayAttached) StatusGreen else StatusAmber
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_diag_overlay_attached),
                    contentDescription = "floating window",
                    tint = if (isOverlayAttached) StatusGreen else (if (isDark) MetroWhite else MetroBlack),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        if (!isPausedForPayments) {
            // Payment Safety Mode Live Tile (Wide 2x1)
            MetroTile(
                label = "payment safety mode",
                height = 104.dp,
                style = MetroTileStyle.OUTLINED,
                statusText = "pause",
                statusColor = StatusAmber,
                onClick = onPauseForPayments
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_action_pause),
                        contentDescription = "pause AssistX for payments",
                        tint = StatusAmber,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "pause AssistX for payments",
                            style = MetroTypography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isDark) MetroWhite else MetroBlack
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Temporarily pause AssistX before opening a payment app.",
                            style = MetroTypography.bodySmall.copy(
                                color = (if (isDark) MetroWhite else MetroBlack).copy(alpha = 0.65f),
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        )
                    }
                }
            }
        }

        // Tile 6: Gestures Guide (Wide 2x1)
        MetroTile(
            label = "active gesture shortcuts",
            height = 120.dp,
            style = MetroTileStyle.OUTLINED
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetroGestureItem(title = "double tap", desc = "screen lock")
                MetroGestureItem(title = "long press", desc = "home screen")
                MetroGestureItem(title = "single tap", desc = "quick popout")
                MetroGestureItem(title = "drag", desc = "move & snap")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MetroGestureItem(title: String, desc: String) {
    val isDark = LocalMetroIsDark.current
    val textColor = if (isDark) MetroWhite else MetroBlack

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title.lowercase(),
            style = MetroTypography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = textColor
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = desc.lowercase(),
            style = MetroTypography.bodySmall.copy(
                fontSize = 11.sp,
                color = textColor.copy(alpha = 0.6f)
            )
        )
    }
}

/**
 * Grouped Collapsible Settings Sections: Theme, Floating Button, Action Popout, Capsule & Behavior.
 */
@Composable
private fun StudioSettingsContent(
    settings: OverlaySettings,
    snapshot: AppStateSnapshot,
    viewModel: StudioViewModel?,
    onRequestPauseForPayments: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val isDark = LocalMetroIsDark.current
    val accentColor = LocalMetroAccent.current.color
    val textColor = if (isDark) MetroWhite else MetroBlack

    val effectiveButtonColor = remember(settings.buttonColorMode, accentColor) {
        if (settings.buttonColorMode.startsWith("#")) {
            try {
                Color(android.graphics.Color.parseColor(settings.buttonColorMode))
            } catch (e: Exception) {
                accentColor
            }
        } else {
            accentColor
        }
    }

    val buttonShape = when (settings.buttonShape.uppercase()) {
        "SQUARE" -> RectangleShape
        "ROUNDED" -> RoundedCornerShape(12.dp)
        else -> CircleShape
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) MetroBlack else MetroWhite)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // =========================================================================
        // 1. THEME & APPEARANCE
        // =========================================================================
        MetroCollapsibleSection(
            title = "theme & appearance",
            subtitle = "${settings.themeMode.lowercase()} mode · ${settings.accentHex}",
            initiallyExpanded = false
        ) {
            // 1.1 Metro Accent Color (20 Swatches)
            Column {
                Text(
                    text = "accent color",
                    style = MetroTypography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = textColor
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                MetroAccentPicker(
                    selectedHex = settings.accentHex,
                    onAccentSelected = { hex ->
                        viewModel?.updateAccent(hex)
                    }
                )
            }

            // 1.2 Theme Mode (Dark / Light / System)
            Column {
                Text(
                    text = "theme mode",
                    style = MetroTypography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = textColor
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                val themeOptions = remember {
                    listOf(
                        MetroOption(ThemeMode.DARK, "dark"),
                        MetroOption(ThemeMode.LIGHT, "light"),
                        MetroOption(ThemeMode.SYSTEM, "system")
                    )
                }
                val currentMode = remember(settings.themeMode) {
                    when (settings.themeMode.lowercase()) {
                        "light" -> ThemeMode.LIGHT
                        "system" -> ThemeMode.SYSTEM
                        else -> ThemeMode.DARK
                    }
                }
                MetroSegmentedPicker(
                    options = themeOptions,
                    selectedValue = currentMode,
                    onOptionSelected = { mode ->
                        viewModel?.updateThemeMode(mode.label)
                    }
                )
            }
        }

        // =========================================================================
        // 2. FLOATING BUTTON
        // =========================================================================
        MetroCollapsibleSection(
            title = "floating button",
            subtitle = "${settings.buttonShape.lowercase()} · ${settings.buttonSizeDp}dp · ${settings.idleOpacityPercent}% opacity",
            initiallyExpanded = false
        ) {
            // 2.1 Floating Button Live Preview Tile
            Column {
                Text(
                    text = "floating button live preview",
                    style = MetroTypography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = textColor
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .border(1.dp, if (isDark) MetroDarkBorder else Color(0xFFE0E0E0), RectangleShape)
                        .background(if (isDark) MetroDarkTile else Color(0xFFF7F7F7)),
                    contentAlignment = Alignment.Center
                ) {
                    val previewSizeDp = settings.buttonSizeDp.dp
                    val previewAlpha = (settings.idleOpacityPercent / 100f).coerceIn(0.2f, 1f)
                    val isLight = (effectiveButtonColor == Color.White || (effectiveButtonColor.red > 0.85f && effectiveButtonColor.green > 0.85f && effectiveButtonColor.blue > 0.85f))

                    when (settings.buttonStyle.uppercase()) {
                        "SOLID_ACCENT" -> {
                            Box(
                                modifier = Modifier
                                    .size(previewSizeDp)
                                    .border(2.dp, if (isLight) MetroBlack else MetroWhite, buttonShape)
                                    .background(effectiveButtonColor.copy(alpha = previewAlpha), buttonShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(previewSizeDp * 0.60f)
                                        .border(1.2.dp, if (isLight) MetroBlack.copy(alpha = 0.4f) else MetroWhite.copy(alpha = 0.5f), buttonShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(previewSizeDp * 0.22f)
                                            .background(if (isLight) MetroBlack else MetroWhite, buttonShape)
                                    )
                                }
                            }
                        }
                        "MONOLINE_RING" -> {
                            Box(
                                modifier = Modifier
                                    .size(previewSizeDp)
                                    .border(2.dp, effectiveButtonColor.copy(alpha = previewAlpha), buttonShape)
                                    .background(Color.Transparent, buttonShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(previewSizeDp * 0.60f)
                                        .border(1.2.dp, effectiveButtonColor.copy(alpha = previewAlpha * 0.7f), buttonShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(previewSizeDp * 0.22f)
                                            .background(effectiveButtonColor.copy(alpha = previewAlpha), buttonShape)
                                    )
                                }
                            }
                        }
                        else -> { // "METRO_OUTLINE"
                            Box(
                                modifier = Modifier
                                    .size(previewSizeDp)
                                    .border(2.5.dp, effectiveButtonColor, buttonShape)
                                    .background(MetroBlack.copy(alpha = previewAlpha), buttonShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(previewSizeDp * 0.60f)
                                        .border(1.5.dp, effectiveButtonColor.copy(alpha = previewAlpha), buttonShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(previewSizeDp * 0.22f)
                                            .background(if (isLight) effectiveButtonColor else MetroWhite.copy(alpha = previewAlpha), buttonShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2.2 Button Shape Selector
            Column {
                Text(
                    text = "floating button shape",
                    style = MetroTypography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = textColor
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                val shapeOptions = remember {
                    listOf(
                        MetroOption("CIRCLE", "circle"),
                        MetroOption("SQUARE", "square (metro)"),
                        MetroOption("ROUNDED", "rounded")
                    )
                }
                MetroSegmentedPicker(
                    options = shapeOptions,
                    selectedValue = settings.buttonShape.uppercase(),
                    onOptionSelected = { shape ->
                        viewModel?.updateButtonShape(shape)
                    }
                )
            }

            // 2.3 Button Design Style
            Column {
                Text(
                    text = "floating button design style",
                    style = MetroTypography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = textColor
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                val styleOptions = remember {
                    listOf(
                        MetroOption("METRO_OUTLINE", "metro outline"),
                        MetroOption("SOLID_ACCENT", "solid fill"),
                        MetroOption("MONOLINE_RING", "monoline ring")
                    )
                }
                MetroSegmentedPicker(
                    options = styleOptions,
                    selectedValue = settings.buttonStyle.uppercase(),
                    onOptionSelected = { style ->
                        viewModel?.updateButtonStyle(style)
                    }
                )
            }

            // 2.4 Button Standalone Color Override
            Column {
                Text(
                    text = "floating button color override",
                    style = MetroTypography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = textColor
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                val colorOptions = remember {
                    listOf(
                        MetroOption("ACCENT", "theme"),
                        MetroOption("#FFFFFF", "white"),
                        MetroOption("#647687", "steel"),
                        MetroOption("#0050EF", "cobalt"),
                        MetroOption("#107C10", "emerald"),
                        MetroOption("#E51400", "crimson")
                    )
                }
                MetroSegmentedPicker(
                    options = colorOptions,
                    selectedValue = settings.buttonColorMode,
                    onOptionSelected = { colorMode ->
                        viewModel?.updateButtonColorMode(colorMode)
                    }
                )
            }

            // 2.5 Button Size Selector
            Column {
                Text(
                    text = "button size",
                    style = MetroTypography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = textColor
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                val sizeOptions = remember {
                    listOf(
                        MetroOption(48, "compact (48dp)"),
                        MetroOption(56, "standard (56dp)"),
                        MetroOption(64, "large (64dp)")
                    )
                }
                MetroSegmentedPicker(
                    options = sizeOptions,
                    selectedValue = settings.buttonSizeDp,
                    onOptionSelected = { size ->
                        viewModel?.updateButtonSize(size)
                    }
                )
            }

            // 2.6 Idle Opacity Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "idle opacity",
                        style = MetroTypography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = textColor
                        )
                    )
                    Text(
                        text = "${settings.idleOpacityPercent}%",
                        style = MetroTypography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = accentColor
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                MetroSlider(
                    value = settings.idleOpacityPercent.toFloat(),
                    onValueChange = { viewModel?.updateIdleOpacity(it.toInt()) },
                    valueRange = 20f..100f,
                    steps = 7
                )
            }

            // 2.7 Idle Fade Delay
            Column {
                Text(
                    text = "idle fade delay",
                    style = MetroTypography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = textColor
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                val fadeOptions = remember {
                    listOf(
                        MetroOption(1, "1 sec"),
                        MetroOption(2, "2 sec"),
                        MetroOption(3, "3 sec"),
                        MetroOption(5, "5 sec")
                    )
                }
                MetroSegmentedPicker(
                    options = fadeOptions,
                    selectedValue = settings.idleFadeSeconds,
                    onOptionSelected = { seconds ->
                        viewModel?.updateIdleFadeDelay(seconds)
                    }
                )
            }

            // 2.8 Popout Auto-Dismiss Timeout
            Column {
                Text(
                    text = "popout auto-dismiss timeout",
                    style = MetroTypography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = textColor
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                val timeoutOptions = remember {
                    listOf(
                        MetroOption(2, "2 sec"),
                        MetroOption(3, "3 sec"),
                        MetroOption(4, "4 sec"),
                        MetroOption(6, "6 sec")
                    )
                }
                MetroSegmentedPicker(
                    options = timeoutOptions,
                    selectedValue = settings.popoutDismissSeconds,
                    onOptionSelected = { seconds ->
                        viewModel?.updatePopoutDismissDelay(seconds)
                    }
                )
            }
        }

        // =========================================================================
        // 3. ACTION POPOUT & ARRAY
        // =========================================================================
        MetroCollapsibleSection(
            title = "action popout & array",
            subtitle = "${settings.popoutButtonSizeDp}dp buttons · ${settings.popoutOrientation.lowercase()} orientation",
            initiallyExpanded = false
        ) {
            // 3.1 Live Popout Array Preview Box
            val isCurrentHorizontal = settings.popoutOrientation.contains("HORIZ") ||
                    settings.popoutOrientation == "LEFT" ||
                    settings.popoutOrientation == "RIGHT"

            Column {
                Text(
                    text = "popout array preview",
                    style = MetroTypography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = textColor
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isCurrentHorizontal) 130.dp else 210.dp)
                        .border(1.dp, if (isDark) MetroDarkBorder else Color(0xFFE0E0E0), RectangleShape)
                        .background(if (isDark) MetroDarkTile else Color(0xFFF7F7F7)),
                    contentAlignment = Alignment.Center
                ) {
                    val actions = when (settings.popoutOrder.uppercase()) {
                        "POWER_VOLUME_SCREENSHOT" -> listOf("POWER", "VOLUME", "SCREENSHOT")
                        "VOLUME_SCREENSHOT_POWER" -> listOf("VOLUME", "SCREENSHOT", "POWER")
                        else -> listOf("SCREENSHOT", "VOLUME", "POWER")
                    }

                    val capsuleShape = when (settings.capsuleCornerRadius.uppercase()) {
                        "METRO_SQUARE" -> RectangleShape
                        "ROUNDED" -> RoundedCornerShape(14.dp)
                        else -> RoundedCornerShape(28.dp)
                    }

                    val capsuleBgColor = when (settings.capsuleStyle.uppercase()) {
                        "METRO_BLACK" -> MetroBlack
                        "SOLID_ACCENT" -> accentColor.copy(alpha = 0.9f)
                        "OUTLINE_ONLY" -> Color.Transparent
                        else -> Color(0xE5121216)
                    }

                    val capsuleBorderColor = when (settings.capsuleStyle.uppercase()) {
                        "METRO_BLACK", "OUTLINE_ONLY" -> accentColor
                        "SOLID_ACCENT" -> MetroWhite
                        else -> Color(0x55FFFFFF)
                    }

                    val capsuleModifier = if (settings.capsuleEnabled) {
                        Modifier
                            .border(1.5.dp, capsuleBorderColor, capsuleShape)
                            .background(capsuleBgColor, capsuleShape)
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                    } else {
                        Modifier.padding(4.dp)
                    }

                    if (isCurrentHorizontal) {
                        Row(
                            modifier = capsuleModifier,
                            horizontalArrangement = Arrangement.spacedBy(settings.popoutItemSpacingDp.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            actions.forEach { action ->
                                val isPower = action == "POWER"
                                val iconRes = when (action) {
                                    "SCREENSHOT" -> R.drawable.ic_action_screenshot
                                    "VOLUME" -> R.drawable.ic_action_volume
                                    else -> R.drawable.ic_action_power
                                }
                                val iconTint = if (isPower) Color(0xFFF87171) else MetroWhite
                                val btnBorderColor = if (isPower) Color(0x60EF4444) else Color(0x35FFFFFF)
                                val btnSize = (settings.popoutButtonSizeDp * 0.82f).dp

                                Box(
                                    modifier = Modifier
                                        .size(btnSize)
                                        .border(1.2.dp, btnBorderColor, CircleShape)
                                        .background(Color(0xF016161A), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = iconRes),
                                        contentDescription = action.lowercase(),
                                        colorFilter = ColorFilter.tint(iconTint),
                                        modifier = Modifier.size(btnSize * 0.45f)
                                    )
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = capsuleModifier,
                            verticalArrangement = Arrangement.spacedBy(settings.popoutItemSpacingDp.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            actions.forEach { action ->
                                val isPower = action == "POWER"
                                val iconRes = when (action) {
                                    "SCREENSHOT" -> R.drawable.ic_action_screenshot
                                    "VOLUME" -> R.drawable.ic_action_volume
                                    else -> R.drawable.ic_action_power
                                }
                                val iconTint = if (isPower) Color(0xFFF87171) else MetroWhite
                                val btnBorderColor = if (isPower) Color(0x60EF4444) else Color(0x35FFFFFF)
                                val btnSize = (settings.popoutButtonSizeDp * 0.82f).dp

                                Box(
                                    modifier = Modifier
                                        .size(btnSize)
                                        .border(1.2.dp, btnBorderColor, CircleShape)
                                        .background(Color(0xF016161A), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = iconRes),
                                        contentDescription = action.lowercase(),
                                        colorFilter = ColorFilter.tint(iconTint),
                                        modifier = Modifier.size(btnSize * 0.45f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3.2 Action Buttons Size
            Column {
                Text(
                    text = "action button size",
                    style = MetroTypography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = textColor
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                val popoutSizeOptions = remember {
                    listOf(
                        MetroOption(46, "compact (46dp)"),
                        MetroOption(54, "standard (54dp)"),
                        MetroOption(62, "large (62dp)")
                    )
                }
                MetroSegmentedPicker(
                    options = popoutSizeOptions,
                    selectedValue = settings.popoutButtonSizeDp,
                    onOptionSelected = { size ->
                        viewModel?.updatePopoutButtonSize(size)
                    }
                )
            }

            // 3.3 Array Alignment & Orientation
            Column {
                Text(
                    text = "array layout alignment",
                    style = MetroTypography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = textColor
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                val alignmentOptions = remember {
                    listOf(
                        MetroOption(false, "vertical"),
                        MetroOption(true, "horizontal")
                    )
                }
                MetroSegmentedPicker(
                    options = alignmentOptions,
                    selectedValue = isCurrentHorizontal,
                    onOptionSelected = { isHoriz ->
                        if (isHoriz) {
                            viewModel?.updatePopoutOrientation("HORIZONTAL")
                        } else {
                            viewModel?.updatePopoutOrientation("AUTO")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "expansion direction",
                    style = MetroTypography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = textColor
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                val directionOptions = if (isCurrentHorizontal) {
                    remember {
                        listOf(
                            MetroOption("HORIZONTAL", "auto (adaptive)"),
                            MetroOption("LEFT", "to left"),
                            MetroOption("RIGHT", "to right")
                        )
                    }
                } else {
                    remember {
                        listOf(
                            MetroOption("AUTO", "auto (adaptive)"),
                            MetroOption("DOWN", "downwards"),
                            MetroOption("UP", "upwards")
                        )
                    }
                }
                MetroSegmentedPicker(
                    options = directionOptions,
                    selectedValue = settings.popoutOrientation.uppercase(),
                    onOptionSelected = { orientation ->
                        viewModel?.updatePopoutOrientation(orientation)
                    }
                )
            }

            // 3.4 Spacing between Buttons
            Column {
                Text(
                    text = "button spacing",
                    style = MetroTypography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = textColor
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                val spacingOptions = remember {
                    listOf(
                        MetroOption(6, "6dp"),
                        MetroOption(10, "10dp (def)"),
                        MetroOption(14, "14dp"),
                        MetroOption(18, "18dp")
                    )
                }
                MetroSegmentedPicker(
                    options = spacingOptions,
                    selectedValue = settings.popoutItemSpacingDp,
                    onOptionSelected = { spacing ->
                        viewModel?.updatePopoutItemSpacing(spacing)
                    }
                )
            }

            // 3.5 Margin from Floating Button
            Column {
                Text(
                    text = "margin from floating button",
                    style = MetroTypography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = textColor
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                val marginOptions = remember {
                    listOf(
                        MetroOption(8, "8dp"),
                        MetroOption(12, "12dp (def)"),
                        MetroOption(16, "16dp"),
                        MetroOption(20, "20dp")
                    )
                }
                MetroSegmentedPicker(
                    options = marginOptions,
                    selectedValue = settings.popoutMarginDp,
                    onOptionSelected = { margin ->
                        viewModel?.updatePopoutMargin(margin)
                    }
                )
            }

            // 3.6 Action Buttons Order
            Column {
                Text(
                    text = "action buttons order",
                    style = MetroTypography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = textColor
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                val orderOptions = remember {
                    listOf(
                        MetroOption("SCREENSHOT_VOLUME_POWER", "shot · vol · power"),
                        MetroOption("POWER_VOLUME_SCREENSHOT", "power · vol · shot"),
                        MetroOption("VOLUME_SCREENSHOT_POWER", "vol · shot · power")
                    )
                }
                MetroSegmentedPicker(
                    options = orderOptions,
                    selectedValue = settings.popoutOrder.uppercase(),
                    onOptionSelected = { order ->
                        viewModel?.updatePopoutOrder(order)
                    }
                )
            }
        }

        // =========================================================================
        // 4. BACKGROUND CAPSULE ELEMENT
        // =========================================================================
        MetroCollapsibleSection(
            title = "background capsule element",
            subtitle = if (settings.capsuleEnabled) "${settings.capsuleStyle.lowercase()} · ${settings.capsuleCornerRadius.lowercase()}" else "disabled",
            initiallyExpanded = false
        ) {
            // 4.1 Background Capsule Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "enable background capsule",
                        style = MetroTypography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = textColor
                        )
                    )
                    Text(
                        text = "wraps the action buttons in a unified container capsule",
                        style = MetroTypography.bodySmall.copy(
                            color = textColor.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    )
                }
                MetroToggle(
                    checked = settings.capsuleEnabled,
                    onCheckedChange = { isEnabled ->
                        viewModel?.updateCapsuleEnabled(isEnabled)
                    }
                )
            }

            if (settings.capsuleEnabled) {
                // 4.2 Capsule Style
                Column {
                    Text(
                        text = "capsule style",
                        style = MetroTypography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = textColor
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val styleOptions = remember {
                        listOf(
                            MetroOption("OBSIDIAN", "obsidian"),
                            MetroOption("METRO_BLACK", "metro black"),
                            MetroOption("SOLID_ACCENT", "solid accent"),
                            MetroOption("OUTLINE_ONLY", "outline only")
                        )
                    }
                    MetroSegmentedPicker(
                        options = styleOptions,
                        selectedValue = settings.capsuleStyle.uppercase(),
                        onOptionSelected = { style ->
                            viewModel?.updateCapsuleStyle(style)
                        }
                    )
                }

                // 4.3 Capsule Corner Radius
                Column {
                    Text(
                        text = "capsule corner radius",
                        style = MetroTypography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = textColor
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val radiusOptions = remember {
                        listOf(
                            MetroOption("PILL", "pill (round)"),
                            MetroOption("METRO_SQUARE", "square (0dp)"),
                            MetroOption("ROUNDED", "rounded (14dp)")
                        )
                    }
                    MetroSegmentedPicker(
                        options = radiusOptions,
                        selectedValue = settings.capsuleCornerRadius.uppercase(),
                        onOptionSelected = { radius ->
                            viewModel?.updateCapsuleCornerRadius(radius)
                        }
                    )
                }
            }
        }

        // =========================================================================
        // 5. INTERACTION & BEHAVIOR
        // =========================================================================
        MetroCollapsibleSection(
            title = "interaction & behavior",
            subtitle = "haptics ${if (settings.hapticFeedbackEnabled) "on" else "off"} · snap ${if (settings.snapToEdgeEnabled) "on" else "off"}",
            initiallyExpanded = false
        ) {
            // 5.1 Haptic Feedback Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "haptic feedback",
                        style = MetroTypography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = textColor
                        )
                    )
                    Text(
                        text = "tactile click on touch, drag, and tiles",
                        style = MetroTypography.bodySmall.copy(
                            color = textColor.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    )
                }
                MetroToggle(
                    checked = settings.hapticFeedbackEnabled,
                    onCheckedChange = { isEnabled ->
                        viewModel?.updateHapticFeedback(isEnabled)
                    }
                )
            }

            // 5.2 Snap to Edge Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "snap to edge",
                        style = MetroTypography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = textColor
                        )
                    )
                    Text(
                        text = "snap button to closest screen margin on release",
                        style = MetroTypography.bodySmall.copy(
                            color = textColor.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    )
                }
                MetroToggle(
                    checked = settings.snapToEdgeEnabled,
                    onCheckedChange = { isEnabled ->
                        viewModel?.updateSnapToEdge(isEnabled)
                    }
                )
            }
        }

        // =========================================================================
        // 6. PAYMENT SAFETY MODE
        // =========================================================================
        val isPausedInSettings = snapshot.desiredState == DesiredState.PAUSED_FOR_PAYMENTS
        val context = LocalContext.current
        MetroCollapsibleSection(
            title = "payment safety mode",
            subtitle = if (isPausedInSettings) "paused for payments" else "safe payment protection",
            initiallyExpanded = false
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "payment apps (e.g. BHIM, Google Pay, PhonePe) may restrict access or block transactions if third-party accessibility services or floating overlays are running.",
                    style = MetroTypography.bodySmall.copy(
                        color = textColor.copy(alpha = 0.75f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                )
                Text(
                    text = "tapping pause below cleanly removes the floating overlay and turns off AssistX's accessibility service before you make a payment. you will need to re-enable accessibility in Android settings when you are finished.",
                    style = MetroTypography.bodySmall.copy(
                        color = textColor.copy(alpha = 0.55f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                )
                if (isPausedInSettings) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetroButton(
                            text = "open accessibility settings",
                            onClick = { viewModel?.openAccessibilitySettings(context) },
                            modifier = Modifier.weight(1f)
                        )
                        MetroButton(
                            text = "resume assistant",
                            onClick = { viewModel?.onResumeFromPayments() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    MetroButton(
                        text = "pause AssistX for payments",
                        onClick = onRequestPauseForPayments,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // =========================================================================
        // 7. SYSTEM & RESET
        // =========================================================================
        MetroCollapsibleSection(
            title = "system & reset",
            subtitle = "restore default configurations",
            initiallyExpanded = false
        ) {
            MetroButton(
                text = "reset to defaults",
                isDanger = true,
                onClick = {
                    viewModel?.resetSettingsToDefault()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
