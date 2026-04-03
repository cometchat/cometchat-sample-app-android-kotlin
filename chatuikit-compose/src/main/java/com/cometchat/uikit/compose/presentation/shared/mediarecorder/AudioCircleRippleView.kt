package com.cometchat.uikit.compose.presentation.shared.mediarecorder

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A custom Compose component that displays a circular ripple effect originating from a
 * central point. The ripples expand outward and can be animated to create a visual effect.
 */
@Composable
fun AudioCircleRippleView(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    isAnimating: Boolean = false,
    rippleCount: Int = 3,
    animationDuration: Int = 1800
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ripple")
    
    val animatedRadius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationDuration,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "radius"
    )

    val innerCircleRadius = size.value / 2.8f
    val maxRippleRadius = size.value / 2f

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
        ) {
            val centerX = this.size.width / 2f
            val centerY = this.size.height / 2f

            // Draw the inner circle
            drawCircle(
                color = color,
                radius = innerCircleRadius,
                center = Offset(centerX, centerY)
            )

            // Draw ripples if animating
            if (isAnimating) {
                for (i in 0 until rippleCount) {
                    val rippleAlpha = (0.1f + (i * 0.05f)) * (1f - animatedRadius)
                    val currentRippleRadius = innerCircleRadius + 
                        (maxRippleRadius - innerCircleRadius) * animatedRadius * (i + 3f) / rippleCount

                    if (rippleAlpha > 0f && currentRippleRadius > innerCircleRadius) {
                        drawCircle(
                            color = color.copy(alpha = rippleAlpha),
                            radius = currentRippleRadius,
                            center = Offset(centerX, centerY)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Preview version of AudioCircleRippleView for development and testing
 */
@Composable
fun AudioCircleRippleViewPreview(
    modifier: Modifier = Modifier,
    isAnimating: Boolean = true
) {
    AudioCircleRippleView(
        modifier = modifier,
        size = 120.dp,
        color = MaterialTheme.colorScheme.primary,
        isAnimating = isAnimating
    )
}