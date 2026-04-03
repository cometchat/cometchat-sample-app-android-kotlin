package com.cometchat.uikit.compose.presentation.shared.suggestionlist

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.shared.baseelements.avatar.CometChatAvatar
import com.cometchat.uikit.compose.presentation.shared.formatters.SuggestionItem
import com.cometchat.uikit.compose.presentation.shared.shimmer.config.CometChatShimmerConfig
import com.cometchat.uikit.compose.presentation.shared.shimmer.utils.ProvideShimmerAnimation
import com.cometchat.uikit.compose.presentation.shared.shimmer.utils.useShimmerBrush

/**
 * CometChatSuggestionList displays a list of suggestions for mentions or other autocomplete features.
 * 
 * Features:
 * - Shimmer loading state while fetching suggestions
 * - Item click handling for selection
 * - Max height limit to prevent taking too much screen space
 * - Custom item view support
 * - Scroll to bottom detection for pagination
 *
 * ## Basic Usage
 *
 * ```kotlin
 * @Composable
 * fun SuggestionOverlay(
 *     suggestions: List<SuggestionItem>,
 *     isLoading: Boolean,
 *     onItemClick: (SuggestionItem) -> Unit
 * ) {
 *     CometChatSuggestionList(
 *         suggestions = suggestions,
 *         isLoading = isLoading,
 *         onItemClick = onItemClick,
 *         onScrollToBottom = {
 *             // Load more suggestions for pagination
 *         }
 *     )
 * }
 * ```
 *
 * ## Custom Item View
 *
 * ```kotlin
 * CometChatSuggestionList(
 *     suggestions = suggestions,
 *     itemView = { suggestion ->
 *         Row(
 *             modifier = Modifier.padding(16.dp),
 *             verticalAlignment = Alignment.CenterVertically
 *         ) {
 *             AsyncImage(
 *                 model = suggestion.leadingIconUrl,
 *                 contentDescription = null,
 *                 modifier = Modifier.size(40.dp).clip(CircleShape)
 *             )
 *             Spacer(modifier = Modifier.width(12.dp))
 *             Text(text = suggestion.name)
 *         }
 *     },
 *     onItemClick = { suggestion -> handleSelection(suggestion) }
 * )
 * ```
 *
 * ## Style Configuration
 *
 * ```kotlin
 * val style = CometChatSuggestionListStyle(
 *     backgroundColor = Color.White,
 *     strokeColor = Color.LightGray,
 *     strokeWidth = 1.dp,
 *     cornerRadius = 8.dp,
 *     maxHeight = 250.dp,
 *     itemTextColor = Color.Black,
 *     itemTextStyle = MaterialTheme.typography.bodyMedium
 * )
 *
 * CometChatSuggestionList(
 *     suggestions = suggestions,
 *     style = style,
 *     onItemClick = { /* ... */ }
 * )
 * ```
 * 
 * @param modifier Modifier for the suggestion list
 * @param suggestions List of suggestion items to display
 * @param isLoading Whether to show shimmer loading state
 * @param style Style configuration for the suggestion list
 * @param showAvatar Whether to show avatars in suggestion items
 * @param itemView Custom composable for rendering each suggestion item
 * @param onItemClick Callback when a suggestion item is clicked
 * @param onScrollToBottom Callback when user scrolls to the bottom (for pagination)
 *
 * @see CometChatSuggestionListStyle Style configuration class
 * @see SuggestionItem Data model for suggestion items
 */
@Composable
fun CometChatSuggestionList(
    modifier: Modifier = Modifier,
    suggestions: List<SuggestionItem> = emptyList(),
    isLoading: Boolean = false,
    style: CometChatSuggestionListStyle = CometChatSuggestionListStyle.default(),
    showAvatar: Boolean = true,
    itemView: (@Composable (SuggestionItem) -> Unit)? = null,
    onItemClick: ((SuggestionItem) -> Unit)? = null,
    onScrollToBottom: (() -> Unit)? = null
) {
    val listState = rememberLazyListState()
    
    // Detect scroll to bottom for pagination
    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItem >= totalItems - 1
        }
    }
    
    LaunchedEffect(isAtBottom) {
        if (isAtBottom && !isLoading && suggestions.isNotEmpty()) {
            onScrollToBottom?.invoke()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = style.maxHeight)
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
            .semantics { contentDescription = "Suggestion List" }
    ) {
        if (isLoading && suggestions.isEmpty()) {
            // Show shimmer loading state
            SuggestionListShimmer(
                style = style,
                itemCount = 3
            )
        } else if (suggestions.isNotEmpty()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(
                    items = suggestions,
                    key = { index, item -> "${item.id}_$index" }
                ) { index, suggestion ->
                    if (itemView != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemClick?.invoke(suggestion) }
                        ) {
                            itemView(suggestion)
                        }
                    } else {
                        CometChatSuggestionListItem(
                            suggestion = suggestion,
                            style = style,
                            showAvatar = showAvatar,
                            onClick = { onItemClick?.invoke(suggestion) }
                        )
                    }
                    
                    // Separator between items (except last)
                    if (index < suggestions.lastIndex) {
                        Divider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = style.separatorColor,
                            thickness = style.separatorHeight
                        )
                    }
                }
                
                // Show shimmer at bottom if loading more
                if (isLoading && suggestions.isNotEmpty()) {
                    item {
                        ProvideShimmerAnimation(
                            config = CometChatShimmerConfig.default(
                                baseColor = style.shimmerBaseColor,
                                highlightColor = style.shimmerHighlightColor
                            )
                        ) {
                            SuggestionItemShimmer()
                        }
                    }
                }
            }
        }
    }
}

/**
 * Default suggestion list item composable.
 */
@Composable
private fun CometChatSuggestionListItem(
    suggestion: SuggestionItem,
    style: CometChatSuggestionListStyle,
    showAvatar: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                role = Role.Button
            )
            .background(style.itemBackgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics {
                contentDescription = "Suggestion: ${suggestion.name}"
                role = Role.Button
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        if (showAvatar && !suggestion.hideLeadingIcon) {
            CometChatAvatar(
                modifier = Modifier.size(40.dp),
                avatarUrl = suggestion.leadingIconUrl,
                name = suggestion.name,
                style = style.avatarStyle
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        
        // Text content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // Name/Title
            Text(
                text = suggestion.name,
                color = style.itemTextColor,
                style = style.itemTextStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Info text (if available from data)
            suggestion.data?.optString("infoText")?.takeIf { it.isNotEmpty() }?.let { infoText ->
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = infoText,
                    color = style.itemInfoTextColor,
                    style = style.itemInfoTextStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Shimmer loading state for the suggestion list.
 * Uses shared shimmer animation for synchronized effect across all items.
 */
@Composable
private fun SuggestionListShimmer(
    style: CometChatSuggestionListStyle,
    itemCount: Int = 3
) {
    // Create shimmer config from style colors
    val shimmerConfig = CometChatShimmerConfig.default(
        baseColor = style.shimmerBaseColor,
        highlightColor = style.shimmerHighlightColor
    )

    // Provide shared animation for all shimmer items
    ProvideShimmerAnimation(config = shimmerConfig) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(itemCount) { index ->
                SuggestionItemShimmer()
                if (index < itemCount - 1) {
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = style.separatorColor,
                        thickness = style.separatorHeight
                    )
                }
            }
        }
    }
}

/**
 * Shimmer loading state for a single suggestion item.
 * Uses shared shimmer brush for synchronized animation.
 */
@Composable
private fun SuggestionItemShimmer() {
    // Use shared shimmer brush from ProvideShimmerAnimation
    val shimmerBrush = useShimmerBrush()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar shimmer
        Box(
            modifier = Modifier
                .size(40.dp)
                .graphicsLayer { }
                .clip(CircleShape)
                .background(shimmerBrush)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Name shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp)
                    .graphicsLayer { }
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Info shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(12.dp)
                    .graphicsLayer { }
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
        }
    }
}
