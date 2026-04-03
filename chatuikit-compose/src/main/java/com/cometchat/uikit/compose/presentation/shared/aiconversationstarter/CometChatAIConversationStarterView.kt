package com.cometchat.uikit.compose.presentation.shared.aiconversationstarter

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.core.state.ConversationStarterUIState

/**
 * CometChatAIConversationStarterView displays AI-generated conversation starter suggestions.
 *
 * This component is typically shown when a message list is empty to help users
 * begin conversations with pre-defined AI-generated suggestions.
 *
 * Features:
 * - Displays a vertical list of conversation starter suggestions
 * - Supports loading state with shimmer effect
 * - Supports error state with customizable error message
 * - Customizable styling for container and items
 * - Click callback for starter selection
 *
 * Usage example:
 * ```kotlin
 * CometChatAIConversationStarterView(
 *     uiState = ConversationStarterUIState.Loaded(listOf("Hi!", "How are you?")),
 *     onClick = { starter, position -> /* Handle selection */ }
 * )
 * ```
 *
 * @param modifier Modifier to be applied to the component
 * @param uiState The current UI state (Idle, Loading, Loaded, or Error)
 * @param style Style configuration for the component
 * @param errorText Custom error message to display in error state
 * @param loadingView Custom composable for loading state (replaces shimmer)
 * @param errorView Custom composable for error state
 * @param itemView Custom composable for rendering each starter item
 * @param onClick Callback when a conversation starter is clicked
 */
@Composable
fun CometChatAIConversationStarterView(
    modifier: Modifier = Modifier,
    uiState: ConversationStarterUIState = ConversationStarterUIState.Idle,
    style: CometChatAIConversationStarterStyle = CometChatAIConversationStarterStyle.default(),
    errorText: String? = null,
    loadingView: (@Composable () -> Unit)? = null,
    errorView: (@Composable () -> Unit)? = null,
    itemView: (@Composable (starter: String, position: Int) -> Unit)? = null,
    onClick: ((starter: String, position: Int) -> Unit)? = null
) {
    val context = LocalContext.current
    
    // Don't render anything in Idle state
    if (uiState is ConversationStarterUIState.Idle) {
        return
    }
    
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
            .semantics { contentDescription = "AI Conversation Starters" }
    ) {
        when (uiState) {
            is ConversationStarterUIState.Loading -> {
                if (loadingView != null) {
                    loadingView()
                } else {
                    ConversationStarterShimmer(style = style)
                }
            }
            
            is ConversationStarterUIState.Error -> {
                if (errorView != null) {
                    errorView()
                } else {
                    ConversationStarterErrorView(
                        errorText = errorText ?: context.getString(R.string.cometchat_something_went_wrong),
                        style = style
                    )
                }
            }
            
            is ConversationStarterUIState.Loaded -> {
                if (uiState.starters.isNotEmpty()) {
                    ConversationStarterList(
                        starters = uiState.starters,
                        style = style,
                        itemView = itemView,
                        onClick = onClick
                    )
                }
            }
            
            is ConversationStarterUIState.Idle -> {
                // Already handled above, but needed for exhaustive when
            }
        }
    }
}

/**
 * Displays the list of conversation starters.
 */
@Composable
private fun ConversationStarterList(
    starters: List<String>,
    style: CometChatAIConversationStarterStyle,
    itemView: (@Composable (starter: String, position: Int) -> Unit)?,
    onClick: ((starter: String, position: Int) -> Unit)?
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = starters,
            key = { index, starter -> "$index-$starter" }
        ) { index, starter ->
            if (itemView != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clickable { onClick?.invoke(starter, index) }
                ) {
                    itemView(starter, index)
                }
            } else {
                ConversationStarterItem(
                    starter = starter,
                    position = index,
                    style = style,
                    onClick = onClick
                )
            }
        }
    }
}

/**
 * Single conversation starter item.
 */
@Composable
private fun ConversationStarterItem(
    starter: String,
    position: Int,
    style: CometChatAIConversationStarterStyle,
    onClick: ((starter: String, position: Int) -> Unit)?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
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
                onClick = { onClick?.invoke(starter, position) },
                role = Role.Button
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics {
                contentDescription = "Conversation starter: $starter"
                role = Role.Button
            }
    ) {
        Text(
            text = starter,
            color = style.itemTextColor,
            style = style.itemTextStyle,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Error view for conversation starters.
 */
@Composable
private fun ConversationStarterErrorView(
    errorText: String,
    style: CometChatAIConversationStarterStyle
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
 * Shimmer loading state for conversation starters.
 */
@Composable
private fun ConversationStarterShimmer(
    style: CometChatAIConversationStarterStyle,
    itemCount: Int = 3
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(itemCount) {
            ConversationStarterItemShimmer(style = style)
        }
    }
}

/**
 * Shimmer loading state for a single conversation starter item.
 */
@Composable
private fun ConversationStarterItemShimmer(
    style: CometChatAIConversationStarterStyle
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
            .padding(horizontal = 8.dp)
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
            modifier = Modifier.fillMaxWidth()
        ) {
            // First line shimmer (full width)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Second line shimmer (partial width)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
        }
    }
}
