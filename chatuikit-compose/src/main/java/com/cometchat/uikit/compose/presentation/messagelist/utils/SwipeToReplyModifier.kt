package com.cometchat.uikit.compose.presentation.messagelist.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.theme.CometChatTheme
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Configuration for the swipe-to-reply gesture.
 *
 * @param swipeThresholdDp The distance in dp that triggers the reply action
 * @param iconSizeDp The size of the reply icon in dp
 * @param iconMarginDp The margin from the edge for the reply icon in dp
 */
data class SwipeToReplyConfig(
    val swipeThresholdDp: Float = 72f,
    val iconSizeDp: Float = 24f,
    val iconMarginDp: Float = 16f
)

/**
 * Checks if a message is eligible for swipe-to-reply.
 *
 * Messages are NOT eligible if:
 * - Category is ACTION (system messages like "User joined the group")
 * - Category is CALL (call-related messages)
 * - Message is deleted (deletedAt > 0)
 * - Message is not yet sent (sentAt == 0 or id == 0)
 *
 * @param message The message to check
 * @return true if the message can be swiped to reply, false otherwise
 */
fun isSwipeToReplyEligible(message: BaseMessage): Boolean {
    // Disable for ACTION and CALL category messages
    val category = message.category
    if (category.equals(CometChatConstants.CATEGORY_ACTION, ignoreCase = true) ||
        category.equals(CometChatConstants.CATEGORY_CALL, ignoreCase = true)) {
        return false
    }
    
    // Disable for deleted messages and messages not yet sent
    if (message.deletedAt > 0 || message.sentAt == 0L || message.id == 0L) {
        return false
    }
    
    return true
}

/**
 * A composable wrapper that adds swipe-to-reply functionality to its content.
 *
 * This implements WhatsApp-style swipe-to-reply:
 * - Swipe from start to end (left-to-right in LTR, right-to-left in RTL)
 * - Shows a reply icon with growing circular background during swipe
 * - Triggers reply action when swipe threshold is reached
 * - Snaps back to original position after release
 *
 * The swipe gesture is disabled for:
 * - ACTION category messages (system messages)
 * - CALL category messages
 * - Deleted messages
 * - Unsent messages
 *
 * @param message The message being displayed
 * @param enabled Whether swipe-to-reply is enabled
 * @param onReply Callback invoked when swipe threshold is reached
 * @param config Configuration for swipe behavior
 * @param modifier Modifier for the wrapper
 * @param content The message content to wrap
 */
@Composable
fun SwipeToReplyWrapper(
    message: BaseMessage,
    enabled: Boolean,
    onReply: (BaseMessage) -> Unit,
    config: SwipeToReplyConfig = SwipeToReplyConfig(),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Check if this message is eligible for swipe-to-reply
    val isEligible = remember(message.id, message.category, message.deletedAt, message.sentAt) {
        isSwipeToReplyEligible(message)
    }
    
    // If not enabled or not eligible, just render content without swipe
    if (!enabled || !isEligible) {
        Box(modifier = modifier) {
            content()
        }
        return
    }
    
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl
    val scope = rememberCoroutineScope()
    
    // Convert dp to px
    val swipeThresholdPx = with(density) { config.swipeThresholdDp.dp.toPx() }
    val iconSizePx = with(density) { config.iconSizeDp.dp.toPx() }
    val iconMarginPx = with(density) { config.iconMarginDp.dp.toPx() }
    val maxCircleRadius = iconSizePx * 0.85f
    
    // State for tracking swipe offset
    var offsetX by remember { mutableFloatStateOf(0f) }
    var swipeTriggered by remember { mutableStateOf(false) }
    
    // Animatable for smooth snap-back animation
    val animatedOffset = remember { Animatable(0f) }
    
    // Theme colors
    val circleColor = CometChatTheme.colorScheme.neutralColor300
    val iconTint = CometChatTheme.colorScheme.iconTintSecondary
    
    // Reply icon painter
    val replyIconPainter = painterResource(id = R.drawable.cometchat_ic_reply_to_message)
    
    // Calculate progress (0 to 1)
    val progress = (abs(offsetX) / swipeThresholdPx).coerceIn(0f, 1f)
    
    Box(
        modifier = modifier
            .pointerInput(message.id, enabled, isEligible) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        swipeTriggered = false
                    },
                    onDragEnd = {
                        // Animate back to original position
                        scope.launch {
                            animatedOffset.snapTo(offsetX)
                            animatedOffset.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(durationMillis = 200)
                            )
                            offsetX = 0f
                        }
                        swipeTriggered = false
                    },
                    onDragCancel = {
                        // Animate back to original position
                        scope.launch {
                            animatedOffset.snapTo(offsetX)
                            animatedOffset.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(durationMillis = 200)
                            )
                            offsetX = 0f
                        }
                        swipeTriggered = false
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        
                        // Calculate new offset
                        val newOffset = offsetX + dragAmount
                        
                        // In LTR: allow positive (right) swipe only
                        // In RTL: allow negative (left) swipe only
                        val isValidDirection = if (isRtl) newOffset <= 0 else newOffset >= 0
                        
                        if (isValidDirection) {
                            // Clamp to threshold
                            offsetX = if (isRtl) {
                                newOffset.coerceIn(-swipeThresholdPx, 0f)
                            } else {
                                newOffset.coerceIn(0f, swipeThresholdPx)
                            }
                            
                            // Trigger reply when threshold is reached (only once per swipe)
                            if (abs(offsetX) >= swipeThresholdPx && !swipeTriggered) {
                                swipeTriggered = true
                                onReply(message)
                            }
                        }
                    }
                )
            }
            .drawBehind {
                if (progress > 0f) {
                    drawSwipeIndicator(
                        progress = progress,
                        isRtl = isRtl,
                        iconSizePx = iconSizePx,
                        iconMarginPx = iconMarginPx,
                        maxCircleRadius = maxCircleRadius,
                        circleColor = circleColor,
                        iconTint = iconTint,
                        replyIconPainter = replyIconPainter
                    )
                }
            }
    ) {
        // Content with offset
        Box(
            modifier = Modifier.offset {
                IntOffset(
                    x = if (animatedOffset.isRunning) animatedOffset.value.roundToInt() else offsetX.roundToInt(),
                    y = 0
                )
            }
        ) {
            content()
        }
    }
}

/**
 * Draws the swipe indicator (circle background + reply icon) behind the message.
 */
private fun DrawScope.drawSwipeIndicator(
    progress: Float,
    isRtl: Boolean,
    iconSizePx: Float,
    iconMarginPx: Float,
    maxCircleRadius: Float,
    circleColor: Color,
    iconTint: Color,
    replyIconPainter: Painter
) {
    val centerY = size.height / 2f
    
    // Position icon at the start edge (left in LTR, right in RTL)
    val centerX = if (isRtl) {
        size.width - iconMarginPx - iconSizePx / 2f
    } else {
        iconMarginPx + iconSizePx / 2f
    }
    
    // Draw growing circular background
    val circleRadius = maxCircleRadius * progress
    val circleAlpha = progress
    drawCircle(
        color = circleColor.copy(alpha = circleAlpha),
        radius = circleRadius,
        center = Offset(centerX, centerY)
    )
    
    // Draw reply icon
    val iconAlpha = progress
    val halfIcon = iconSizePx / 2f
    
    translate(
        left = centerX - halfIcon,
        top = centerY - halfIcon
    ) {
        with(replyIconPainter) {
            draw(
                size = androidx.compose.ui.geometry.Size(iconSizePx, iconSizePx),
                alpha = iconAlpha,
                colorFilter = ColorFilter.tint(iconTint)
            )
        }
    }
}
