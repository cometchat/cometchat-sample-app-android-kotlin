package com.cometchat.uikit.compose.presentation.shared.aismartreplies

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.core.state.SmartRepliesUIState

/**
 * CometChatAISmartRepliesView displays AI-generated smart reply suggestions in a horizontal scrollable row.
 *
 * This component is typically shown above the message composer when the user receives
 * a text message from another user, providing quick reply suggestions.
 *
 * Features:
 * - Displays a horizontal scrollable list of smart reply suggestions
 * - Supports loading state with shimmer effect
 * - Supports error state with customizable error message
 * - Close icon to dismiss the suggestions
 * - Customizable styling for container and items
 * - Click callback for reply selection
 * - Close callback for dismissal
 *
 * Usage example:
 * ```kotlin
 * CometChatAISmartRepliesView(
 *     uiState = SmartRepliesUIState.Loaded(listOf("Sure!", "Thanks!", "Let me check.")),
 *     onClick = { reply, position -> /* Handle selection */ },
 *     onCloseClick = { /* Handle dismissal */ }
 * )
 * ```
 *
 * @param modifier Modifier to be applied to the component
 * @param uiState The current UI state (Idle, Loading, Loaded, or Error)
 * @param style Style configuration for the component
 * @param title Optional title text to display above the replies
 * @param errorText Custom error message to display in error state
 * @param loadingView Custom composable for loading state (replaces shimmer)
 * @param errorView Custom composable for error state
 * @param itemView Custom composable for rendering each reply item
 * @param onClick Callback when a smart reply is clicked
 * @param onCloseClick Callback when the close icon is clicked
 */
@Composable
fun CometChatAISmartRepliesView(
    modifier: Modifier = Modifier,
    uiState: SmartRepliesUIState = SmartRepliesUIState.Idle,
    style: CometChatAISmartRepliesStyle = CometChatAISmartRepliesStyle.default(),
    title: String? = null,
    errorText: String? = null,
    loadingView: (@Composable () -> Unit)? = null,
    errorView: (@Composable () -> Unit)? = null,
    itemView: (@Composable (reply: String, position: Int) -> Unit)? = null,
    onClick: ((reply: String, position: Int) -> Unit)? = null,
    onCloseClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    
    // Don't render anything in Idle state
    if (uiState is SmartRepliesUIState.Idle) {
        return
    }

    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (style.maxHeight > 0.dp) {
                    Modifier.heightIn(max = style.maxHeight)
                } else Modifier
            )
            .clip(RoundedCornerShape(style.cornerRadius))
            .background(
                color = style.backgroundColor,
                shape = RoundedCornerShape(style.cornerRadius)
            )
            .then(
                if (style.strokeWidth > 0.dp) {
                    Modifier.border(
                        width = style.strokeWidth,
                        color = style.strokeColor,
                        shape = RoundedCornerShape(style.cornerRadius)
                    )
                } else Modifier
            )
            .semantics { contentDescription = "AI Smart Replies" }
    ) {
        // Header with title and close icon
        SmartRepliesHeader(
            title = title ?: context.getString(R.string.cometchat_smart_replies),
            style = style,
            onCloseClick = onCloseClick
        )
        
        // Content based on state
        when (uiState) {
            is SmartRepliesUIState.Loading -> {
                if (loadingView != null) {
                    loadingView()
                } else {
                    SmartRepliesShimmer(style = style)
                }
            }
            
            is SmartRepliesUIState.Error -> {
                if (errorView != null) {
                    errorView()
                } else {
                    SmartRepliesErrorView(
                        errorText = errorText ?: context.getString(R.string.cometchat_something_went_wrong),
                        style = style
                    )
                }
            }
            
            is SmartRepliesUIState.Loaded -> {
                if (uiState.replies.isNotEmpty()) {
                    SmartRepliesList(
                        replies = uiState.replies,
                        style = style,
                        itemView = itemView,
                        onClick = onClick
                    )
                }
            }
            
            is SmartRepliesUIState.Idle -> {
                // Already handled above, but needed for exhaustive when
            }
        }
    }
}

/**
 * Header component with title and close icon.
 */
@Composable
private fun SmartRepliesHeader(
    title: String,
    style: CometChatAISmartRepliesStyle,
    onCloseClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = style.titleTextColor,
            style = style.titleTextStyle,
            modifier = Modifier.weight(1f)
        )
        
        IconButton(
            onClick = { onCloseClick?.invoke() },
            modifier = Modifier
                .size(24.dp)
                .semantics {
                    contentDescription = "Close smart replies"
                    role = Role.Button
                }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.cometchat_ic_close),
                contentDescription = "Close",
                tint = style.closeIconTint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Displays the horizontal list of smart reply suggestions.
 */
@Composable
private fun SmartRepliesList(
    replies: List<String>,
    style: CometChatAISmartRepliesStyle,
    itemView: (@Composable (reply: String, position: Int) -> Unit)?,
    onClick: ((reply: String, position: Int) -> Unit)?
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = replies,
            key = { index, reply -> "$index-$reply" }
        ) { index, reply ->
            if (itemView != null) {
                Box(
                    modifier = Modifier.clickable { onClick?.invoke(reply, index) }
                ) {
                    itemView(reply, index)
                }
            } else {
                SmartReplyItem(
                    reply = reply,
                    position = index,
                    style = style,
                    onClick = onClick
                )
            }
        }
    }
}


/**
 * Single smart reply item chip.
 */
@Composable
private fun SmartReplyItem(
    reply: String,
    position: Int,
    style: CometChatAISmartRepliesStyle,
    onClick: ((reply: String, position: Int) -> Unit)?
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(style.itemCornerRadius))
            .background(
                color = style.itemBackgroundColor,
                shape = RoundedCornerShape(style.itemCornerRadius)
            )
            .then(
                if (style.itemStrokeWidth > 0.dp) {
                    Modifier.border(
                        width = style.itemStrokeWidth,
                        color = style.itemStrokeColor,
                        shape = RoundedCornerShape(style.itemCornerRadius)
                    )
                } else Modifier
            )
            .clickable(
                onClick = { onClick?.invoke(reply, position) },
                role = Role.Button
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .semantics {
                contentDescription = "Smart reply: $reply"
                role = Role.Button
            }
    ) {
        Text(
            text = reply,
            color = style.itemTextColor,
            style = style.itemTextStyle,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Error view for smart replies.
 */
@Composable
private fun SmartRepliesErrorView(
    errorText: String,
    style: CometChatAISmartRepliesStyle
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = errorText,
            color = style.errorStateTextColor,
            style = style.errorStateTextStyle,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics {
                contentDescription = "Error: $errorText"
            }
        )
    }
}

/**
 * Shimmer loading state for smart replies.
 */
@Composable
private fun SmartRepliesShimmer(
    style: CometChatAISmartRepliesStyle,
    itemCount: Int = 3
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false
    ) {
        items(itemCount) {
            SmartReplyItemShimmer(style = style)
        }
    }
}

/**
 * Shimmer loading state for a single smart reply item.
 */
@Composable
private fun SmartReplyItemShimmer(
    style: CometChatAISmartRepliesStyle
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_progress"
    )
    
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            style.shimmerBaseColor,
            style.shimmerHighlightColor,
            style.shimmerBaseColor
        ),
        start = Offset(shimmerProgress * 500f - 250f, 0f),
        end = Offset(shimmerProgress * 500f + 250f, 0f)
    )
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(style.itemCornerRadius))
            .background(
                color = style.itemBackgroundColor,
                shape = RoundedCornerShape(style.itemCornerRadius)
            )
            .then(
                if (style.itemStrokeWidth > 0.dp) {
                    Modifier.border(
                        width = style.itemStrokeWidth,
                        color = style.itemStrokeColor,
                        shape = RoundedCornerShape(style.itemCornerRadius)
                    )
                } else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Shimmer placeholder for text
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerBrush)
        )
    }
}
