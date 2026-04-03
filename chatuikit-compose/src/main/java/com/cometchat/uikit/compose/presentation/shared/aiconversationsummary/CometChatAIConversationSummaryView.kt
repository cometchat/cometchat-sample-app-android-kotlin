package com.cometchat.uikit.compose.presentation.shared.aiconversationsummary

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import com.cometchat.uikit.core.state.ConversationSummaryUIState

/**
 * CometChatAIConversationSummaryView displays an AI-generated conversation summary.
 *
 * This component is typically shown at the top of the message list when there are
 * many unread messages (above the configured threshold) to help users quickly
 * catch up on the conversation history.
 *
 * Features:
 * - Displays a title and close icon in the header
 * - Shows the AI-generated summary text in a card
 * - Supports loading state with shimmer effect
 * - Supports error state with customizable error message
 * - Customizable styling for container and content
 * - Close callback for dismissing the summary
 *
 * Usage example:
 * ```kotlin
 * CometChatAIConversationSummaryView(
 *     uiState = ConversationSummaryUIState.Loaded("This conversation discussed..."),
 *     onCloseClick = { /* Handle dismiss */ }
 * )
 * ```
 *
 * @param modifier Modifier to be applied to the component
 * @param uiState The current UI state (Idle, Loading, Loaded, or Error)
 * @param style Style configuration for the component
 * @param title Title text to display in the header
 * @param errorText Custom error message to display in error state
 * @param loadingView Custom composable for loading state (replaces shimmer)
 * @param errorView Custom composable for error state
 * @param summaryView Custom composable for rendering the summary content
 * @param onCloseClick Callback when the close icon is clicked
 */
@Composable
fun CometChatAIConversationSummaryView(
    modifier: Modifier = Modifier,
    uiState: ConversationSummaryUIState = ConversationSummaryUIState.Idle,
    style: CometChatAIConversationSummaryStyle = CometChatAIConversationSummaryStyle.default(),
    title: String? = null,
    errorText: String? = null,
    loadingView: (@Composable () -> Unit)? = null,
    errorView: (@Composable () -> Unit)? = null,
    summaryView: (@Composable (summary: String) -> Unit)? = null,
    onCloseClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    
    // Don't render anything in Idle state
    if (uiState is ConversationSummaryUIState.Idle) {
        return
    }
    
    val displayTitle = title ?: context.getString(R.string.cometchat_conversation_summary)
    
    Box(
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
            .semantics { contentDescription = "AI Conversation Summary" }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header with title and close icon
            ConversationSummaryHeader(
                title = displayTitle,
                style = style,
                onCloseClick = onCloseClick
            )
            
            // Content based on state
            when (uiState) {
                is ConversationSummaryUIState.Loading -> {
                    if (loadingView != null) {
                        loadingView()
                    } else {
                        ConversationSummaryShimmer(style = style)
                    }
                }
                
                is ConversationSummaryUIState.Error -> {
                    if (errorView != null) {
                        errorView()
                    } else {
                        ConversationSummaryErrorView(
                            errorText = errorText ?: context.getString(R.string.cometchat_something_went_wrong),
                            style = style
                        )
                    }
                }
                
                is ConversationSummaryUIState.Loaded -> {
                    if (summaryView != null) {
                        summaryView(uiState.summary)
                    } else {
                        ConversationSummaryContent(
                            summary = uiState.summary,
                            style = style
                        )
                    }
                }
                
                is ConversationSummaryUIState.Idle -> {
                    // Already handled above, but needed for exhaustive when
                }
            }
        }
    }
}

/**
 * Header component with title and close icon.
 */
@Composable
private fun ConversationSummaryHeader(
    title: String,
    style: CometChatAIConversationSummaryStyle,
    onCloseClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = style.titleTextColor,
            style = style.titleTextStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = title }
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Icon(
            painter = painterResource(id = R.drawable.cometchat_ic_close),
            contentDescription = "Close conversation summary",
            tint = style.closeIconTint,
            modifier = Modifier
                .size(24.dp)
                .clickable(
                    onClick = { onCloseClick?.invoke() },
                    role = Role.Button
                )
                .semantics {
                    contentDescription = "Close conversation summary"
                    role = Role.Button
                }
        )
    }
}

/**
 * Content component displaying the summary text.
 */
@Composable
private fun ConversationSummaryContent(
    summary: String,
    style: CometChatAIConversationSummaryStyle
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
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
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = summary,
            color = style.itemTextColor,
            style = style.itemTextStyle,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .semantics { contentDescription = "Summary: $summary" }
        )
    }
}

/**
 * Error view for conversation summary.
 */
@Composable
private fun ConversationSummaryErrorView(
    errorText: String,
    style: CometChatAIConversationSummaryStyle
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
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
 * Shimmer loading state for conversation summary.
 */
@Composable
private fun ConversationSummaryShimmer(
    style: CometChatAIConversationSummaryStyle
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
        start = Offset(shimmerProgress * 1000f - 500f, 0f),
        end = Offset(shimmerProgress * 1000f + 500f, 0f)
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
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
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // First line shimmer (full width)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
            
            // Second line shimmer (full width)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
            
            // Third line shimmer (partial width)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
            
            // Fourth line shimmer (partial width)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
        }
    }
}
