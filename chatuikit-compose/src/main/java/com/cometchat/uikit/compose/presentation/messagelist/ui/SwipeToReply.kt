package com.cometchat.uikit.compose.presentation.messagelist.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.theme.CometChatTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Swipe threshold in dp - matches the Kotlin implementation (72dp)
 */
private val SWIPE_THRESHOLD_DP = 72.dp

/**
 * Icon size in dp - matches the Kotlin implementation (24dp)
 */
private val ICON_SIZE_DP = 24.dp

/**
 * Icon margin from left edge - adjusted for better visual alignment
 */
private val ICON_MARGIN_DP = 24.dp

/**
 * Icon vertical offset - moves icon down from center for better alignment with message content
 */
private val ICON_VERTICAL_OFFSET_DP = 45.dp

/**
 * A composable wrapper that adds swipe-to-reply gesture to message items.
 *
 * This implements the same behavior as the Kotlin UIKit:
 * - Swipe left-to-right (LTR) to reveal reply icon
 * - When swipe threshold (72dp) is reached, triggers onSwipeToReply callback
 * - Reply icon fades in (alpha animation) with fixed size
 * - Circular background grows and fades in with swipe progress
 * - Message snaps back after swipe is released
 *
 * @param enabled Whether swipe-to-reply is enabled
 * @param onSwipeToReply Callback invoked when swipe threshold is reached
 * @param content The message item content to wrap
 */
@Composable
fun SwipeToReply(
    enabled: Boolean,
    onSwipeToReply: () -> Unit,
    content: @Composable () -> Unit
) {
    if (!enabled) {
        content()
        return
    }

    val context = LocalContext.current
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { SWIPE_THRESHOLD_DP.toPx() }
    val iconSizePx = with(density) { ICON_SIZE_DP.toPx() }
    val iconMarginPx = with(density) { ICON_MARGIN_DP.toPx() }
    val iconVerticalOffsetPx = with(density) { ICON_VERTICAL_OFFSET_DP.toPx() }
    val maxCircleRadius = iconSizePx * 0.85f
    
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    var swipeTriggered by remember { mutableStateOf(false) }
    
    // Calculate progress (0 to 1) based on swipe distance
    val progress = (offsetX.value / swipeThresholdPx).coerceIn(0f, 1f)
    
    // Get the reply icon drawable
    val replyIcon = remember {
        ContextCompat.getDrawable(context, R.drawable.cometchat_ic_reply_to_message)
    }
    
    // Colors from theme
    val iconTint = CometChatTheme.colorScheme.iconTintSecondary
    val circleBackground = CometChatTheme.colorScheme.neutralColor300
    
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Draw reply icon and circular background using Canvas (matches Kotlin's onChildDraw)
        if (progress > 0f) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                // Calculate center position for icon
                // Icon is positioned at: left margin + half icon size
                val centerX = iconMarginPx + iconSizePx / 2f
                // Add vertical offset to move icon down from center
                val centerY = size.height / 2f + iconVerticalOffsetPx
                
                // Draw growing circular background with alpha
                val circleRadius = maxCircleRadius * progress
                val circleAlpha = progress
                drawCircle(
                    color = circleBackground.copy(alpha = circleAlpha),
                    radius = circleRadius,
                    center = Offset(centerX, centerY)
                )
                
                // Draw reply icon with alpha (fixed size, only alpha changes)
                replyIcon?.let { icon ->
                    icon.setTint(iconTint.hashCode())
                    icon.alpha = (progress * 255).toInt()
                    val halfIcon = (iconSizePx / 2).toInt()
                    icon.setBounds(
                        (centerX - halfIcon).toInt(),
                        (centerY - halfIcon).toInt(),
                        (centerX + halfIcon).toInt(),
                        (centerY + halfIcon).toInt()
                    )
                    drawContext.canvas.nativeCanvas.let { canvas ->
                        icon.draw(canvas)
                    }
                }
            }
        }
        
        // Message content with horizontal offset
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(enabled) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            swipeTriggered = false
                        },
                        onDragEnd = {
                            // Animate back to original position
                            scope.launch {
                                offsetX.animateTo(0f, animationSpec = tween(200))
                            }
                            swipeTriggered = false
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetX.animateTo(0f, animationSpec = tween(200))
                            }
                            swipeTriggered = false
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            
                            // Only allow positive (LTR) swipe, clamped to threshold
                            val newOffset = (offsetX.value + dragAmount).coerceIn(0f, swipeThresholdPx)
                            
                            scope.launch {
                                offsetX.snapTo(newOffset)
                            }
                            
                            // Trigger reply when threshold is reached (only once per swipe)
                            if (newOffset >= swipeThresholdPx && !swipeTriggered) {
                                swipeTriggered = true
                                onSwipeToReply()
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}
