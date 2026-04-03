package com.cometchat.uikit.compose.presentation.reactionlist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cometchat.chat.models.ReactionCount
import com.cometchat.uikit.compose.R
import com.cometchat.uikit.compose.presentation.reactionlist.style.CometChatReactionListStyle

/**
 * Internal constant representing the "All" tab identifier.
 * This is used to distinguish the "All" tab from individual emoji tabs.
 */
internal const val ALL_TAB_IDENTIFIER = "All"

/**
 * ReactionHeaderTabs displays a horizontal scrollable row of reaction tabs.
 * 
 * The first tab is always "All" showing the total count of all reactions,
 * followed by individual emoji tabs with their respective counts.
 * 
 * Features:
 * - Horizontal scrollable tabs using LazyRow
 * - "All" tab with total reaction count as first item
 * - Individual emoji tabs with counts
 * - Active tab indicator (underline)
 * - Tab click handling with callback
 * - Accessibility support with content descriptions
 *
 * @param reactionCounts List of ReactionCount objects from the message
 * @param activeTabIndex The currently selected tab index (0 = All, 1+ = emoji tabs)
 * @param onTabSelected Callback invoked when a tab is selected with (index, reaction)
 *                      where reaction is "All" for the first tab or the emoji string
 * @param style Style configuration for the tabs
 * @param modifier Modifier applied to the container
 */
@Composable
internal fun ReactionHeaderTabs(
    reactionCounts: List<ReactionCount>,
    activeTabIndex: Int,
    onTabSelected: (Int, String) -> Unit,
    style: CometChatReactionListStyle,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    
    // Calculate total count for "All" tab
    val totalCount = reactionCounts.sumOf { it.count }
    
    // Build the tab list: "All" tab first, then individual emoji tabs
    val allTabText = "${context.getString(R.string.cometchat_all)} $totalCount"
    
    // Auto-scroll to active tab when it changes
    LaunchedEffect(activeTabIndex) {
        if (activeTabIndex >= 0) {
            listState.animateScrollToItem(activeTabIndex)
        }
    }
    
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        horizontalArrangement = Arrangement.Start,
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        // "All" tab (index 0)
        item {
            ReactionTab(
                text = allTabText,
                isActive = activeTabIndex == 0,
                onClick = { onTabSelected(0, ALL_TAB_IDENTIFIER) },
                style = style,
                contentDescription = context.getString(R.string.cometchat_all) + ", $totalCount reactions"
            )
        }
        
        // Individual emoji tabs (index 1+)
        itemsIndexed(reactionCounts) { index, reactionCount ->
            val tabIndex = index + 1 // Offset by 1 because "All" is at index 0
            val tabText = "${reactionCount.reaction} ${reactionCount.count}"
            
            ReactionTab(
                text = tabText,
                isActive = activeTabIndex == tabIndex,
                onClick = { onTabSelected(tabIndex, reactionCount.reaction) },
                style = style,
                contentDescription = "${reactionCount.reaction}, ${reactionCount.count} reactions"
            )
        }
    }
}

/**
 * Individual reaction tab composable.
 * 
 * Displays the emoji/text with count and an underline indicator when active.
 * Layout matches the Kotlin XML implementation (cometchat_reaction_header_item.xml)
 * where text is centered and indicator is at the absolute bottom of the tab,
 * aligned with the header separator below.
 *
 * @param text The tab text (e.g., "All 8" or "👍 5")
 * @param isActive Whether this tab is currently selected
 * @param onClick Callback invoked when the tab is clicked
 * @param style Style configuration for the tab
 * @param contentDescription Accessibility description for the tab
 */
@Composable
private fun ReactionTab(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    style: CometChatReactionListStyle,
    contentDescription: String
) {
    // Match Kotlin XML dimensions: 66dp width, 48dp height
    val tabWidth = 66.dp
    val tabHeight = 48.dp
    // Indicator height matches Kotlin XML: 2dp (cometchat_2dp)
    val indicatorHeight = 2.dp
    
    Box(
        modifier = Modifier
            .width(tabWidth)
            .height(tabHeight)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            )
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Tab
                this.selected = isActive
            }
    ) {
        // Tab text - centered in the box (matching Kotlin XML ConstraintLayout constraints)
        Text(
            text = text,
            style = style.tabTextStyle,
            color = if (isActive) style.tabTextActiveColor else style.tabTextColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 4.dp)
        )
        
        // Active tab indicator (underline) - at the absolute bottom, matching Kotlin XML
        // layout_constraintBottom_toBottomOf="parent" - this aligns with the header separator
        if (isActive) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(indicatorHeight)
                    .align(Alignment.BottomCenter)
                    .background(style.tabActiveIndicatorColor)
            )
        }
    }
}
