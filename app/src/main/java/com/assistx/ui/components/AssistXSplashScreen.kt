package com.assistx.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.assistx.ui.theme.MetroTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Animated Metro Splash Screen for AssistX.
 *
 * Implements a dynamic 2x2 geometric tile animation:
 * 1. Shrinks smoothly from the initial icon scale (1.28 -> 1.0) while morphing from
 *    neutral grey into the user's active theme accent color.
 * 2. Cascades the accent color through the 4 boxes with distinct opacities:
 *    - Tile 1 (Top-Left): 1.0f (100% solid accent)
 *    - Tile 2 (Top-Right): 0.70f (70% opacity)
 *    - Tile 4 (Bottom-Right): 0.85f (85% opacity)
 *    - Tile 3 (Bottom-Left): 0.50f (50% opacity)
 * 3. Plays a fluid clockwise following wave (1 -> 2 -> 4 -> 3) around the boxes.
 * 4. Seamlessly dissolves into the prepared main Studio screen.
 */
@Composable
fun AssistXSplashScreen(
    accentColor: Color,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier,
    tileSize: Dp = 26.dp,
    tileGap: Dp = 5.dp
) {
    // Master shrink scale: smoothly shrinks from 1.28 to 1.0
    val masterScale = remember { Animatable(1.28f) }

    // Color morph progress: 0f = neutral subtle grey, 1f = vibrant accent color
    val colorMorph = remember { Animatable(0f) }

    // Distinct opacities and scales for the 4 boxes
    val tile1Alpha = remember { Animatable(0.25f) }
    val tile2Alpha = remember { Animatable(0.25f) }
    val tile4Alpha = remember { Animatable(0.25f) }
    val tile3Alpha = remember { Animatable(0.25f) }

    val tile1Scale = remember { Animatable(1.0f) }
    val tile2Scale = remember { Animatable(1.0f) }
    val tile4Scale = remember { Animatable(1.0f) }
    val tile3Scale = remember { Animatable(1.0f) }

    val textAlpha = remember { Animatable(0f) }
    val overallAlpha = remember { Animatable(1f) }

    val baseNeutral = Color(0xFF6B7280)

    LaunchedEffect(Unit) {
        // --- PHASE 1: Shrink from initial size and morph color into boxes ---
        launch {
            masterScale.animateTo(1.0f, tween(260, easing = FastOutSlowInEasing))
        }
        launch {
            colorMorph.animateTo(1.0f, tween(240, easing = FastOutSlowInEasing))
        }

        // Box 1 (Top-Left) illuminates
        launch {
            tile1Alpha.animateTo(1.0f, tween(240, easing = FastOutSlowInEasing))
        }

        delay(70)
        // Box 2 (Top-Right) illuminates
        launch {
            tile2Alpha.animateTo(0.70f, tween(240, easing = FastOutSlowInEasing))
        }

        delay(70)
        // Box 4 (Bottom-Right) illuminates
        launch {
            tile4Alpha.animateTo(0.85f, tween(240, easing = FastOutSlowInEasing))
        }

        delay(70)
        // Box 3 (Bottom-Left) illuminates
        launch {
            tile3Alpha.animateTo(0.50f, tween(240, easing = FastOutSlowInEasing))
        }

        // Title text fades in smoothly
        launch {
            delay(80)
            textAlpha.animateTo(1.0f, tween(280, easing = FastOutSlowInEasing))
        }

        // --- PHASE 2: Fluid Clockwise Wave Ripple Following the Boxes ---
        delay(160)
        // Wave reaches Box 1
        launch {
            tile1Scale.animateTo(1.08f, tween(100))
            tile1Scale.animateTo(1.0f, tween(100))
        }
        delay(60)
        // Wave reaches Box 2
        launch {
            tile2Alpha.animateTo(0.95f, tween(90))
            tile2Scale.animateTo(1.08f, tween(100))
            tile2Alpha.animateTo(0.70f, tween(100))
            tile2Scale.animateTo(1.0f, tween(100))
        }
        delay(60)
        // Wave reaches Box 4
        launch {
            tile4Alpha.animateTo(1.0f, tween(90))
            tile4Scale.animateTo(1.08f, tween(100))
            tile4Alpha.animateTo(0.85f, tween(100))
            tile4Scale.animateTo(1.0f, tween(100))
        }
        delay(60)
        // Wave reaches Box 3
        launch {
            tile3Alpha.animateTo(0.75f, tween(90))
            tile3Scale.animateTo(1.08f, tween(100))
            tile3Alpha.animateTo(0.50f, tween(100))
            tile3Scale.animateTo(1.0f, tween(100))
        }

        // --- PHASE 3: Seamless Dissolve into Main Studio Screen ---
        delay(220)
        overallAlpha.animateTo(0f, tween(180, easing = FastOutSlowInEasing))
        onAnimationComplete()
    }

    val activeAccent = lerp(baseNeutral, accentColor, colorMorph.value)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .graphicsLayer { alpha = overallAlpha.value },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 2x2 Metro Geometric Mark with Master Shrink Scale
            Column(
                modifier = Modifier.graphicsLayer {
                    scaleX = masterScale.value
                    scaleY = masterScale.value
                },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(tileGap)
            ) {
                // Top Row: Tile 1 & Tile 2
                Row(
                    horizontalArrangement = Arrangement.spacedBy(tileGap),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tile 1 (Top-Left)
                    Box(
                        modifier = Modifier
                            .size(tileSize)
                            .graphicsLayer {
                                scaleX = tile1Scale.value
                                scaleY = tile1Scale.value
                            }
                            .background(activeAccent.copy(alpha = tile1Alpha.value))
                    )
                    // Tile 2 (Top-Right)
                    Box(
                        modifier = Modifier
                            .size(tileSize)
                            .graphicsLayer {
                                scaleX = tile2Scale.value
                                scaleY = tile2Scale.value
                            }
                            .background(activeAccent.copy(alpha = tile2Alpha.value))
                    )
                }

                // Bottom Row: Tile 3 & Tile 4
                Row(
                    horizontalArrangement = Arrangement.spacedBy(tileGap),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tile 3 (Bottom-Left)
                    Box(
                        modifier = Modifier
                            .size(tileSize)
                            .graphicsLayer {
                                scaleX = tile3Scale.value
                                scaleY = tile3Scale.value
                            }
                            .background(activeAccent.copy(alpha = tile3Alpha.value))
                    )
                    // Tile 4 (Bottom-Right)
                    Box(
                        modifier = Modifier
                            .size(tileSize)
                            .graphicsLayer {
                                scaleX = tile4Scale.value
                                scaleY = tile4Scale.value
                            }
                            .background(activeAccent.copy(alpha = tile4Alpha.value))
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Minimalist Metro Header Title
            Text(
                text = "ASSISTX",
                style = MetroTypography.labelSmall.copy(
                    fontSize = 13.sp,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = textAlpha.value * 0.85f)
                )
            )
        }
    }
}
