package com.cometchat.uikit.compose.preview.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatEmptyState
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatErrorState
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatLoadingState
import com.cometchat.uikit.compose.presentation.shared.searchbox.CometChatSearchBox
import com.cometchat.uikit.compose.theme.CometChatTheme

/**
 * NOTE: CometChatSearch cannot be used directly in @Preview because it
 * internally creates CometChatMentionsFormatter and CometChatRichTextFormatter
 * which may call SDK methods during initialization.
 *
 * These previews simulate the Search appearance using basic Compose primitives.
 */

// ============================================================================
// Helper: Simulated Search Result Item
// ============================================================================

@Composable
private fun SimulatedSearchResultItem(
    name: String,
    initials: String,
    subtitle: String,
    time: String = "12:30 PM"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CometChatTheme.colorScheme.extendedPrimaryColor500),
            contentAlignment = Alignment.Center
        ) {
            Text(text = initials, color = Color.White, style = CometChatTheme.typography.caption1Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, style = CometChatTheme.typography.heading4Medium, color = CometChatTheme.colorScheme.textColorPrimary)
            Text(text = subtitle, style = CometChatTheme.typography.caption1Regular, color = CometChatTheme.colorScheme.textColorSecondary, maxLines = 1)
        }
        Text(text = time, style = CometChatTheme.typography.caption2Regular, color = CometChatTheme.colorScheme.textColorTertiary)
    }
}

@Composable
private fun SimulatedFilterChips(
    chips: List<String> = listOf("Unread", "Groups", "Photos", "Videos", "Links", "Documents", "Audio"),
    selectedIndex: Int = -1
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(chips.size) { index ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) CometChatTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else CometChatTheme.colorScheme.backgroundColor3
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = chips[index],
                    style = CometChatTheme.typography.caption1Medium,
                    color = if (isSelected) CometChatTheme.colorScheme.primary
                           else CometChatTheme.colorScheme.textColorPrimary
                )
            }
        }
    }
}

// ============================================================================
// SECTION 1: DEFAULT PREVIEWS
// ============================================================================

@Preview(showBackground = true, name = "Search - Default (Initial State)")
@Composable
fun PreviewSearchDefault() {
    CometChatTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CometChatTheme.colorScheme.backgroundColor1)
        ) {
            CometChatSearchBox(
                modifier = Modifier.padding(16.dp),
                placeholderText = "Search",
                onTextChange = { }
            )
            SimulatedFilterChips()
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Search for messages, users, or groups",
                    style = CometChatTheme.typography.bodyRegular,
                    color = CometChatTheme.colorScheme.textColorTertiary
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Search - With Results")
@Composable
fun PreviewSearchWithResults() {
    CometChatTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CometChatTheme.colorScheme.backgroundColor1)
        ) {
            CometChatSearchBox(
                modifier = Modifier.padding(16.dp),
                text = "Alice",
                onTextChange = { },
                onClear = { }
            )
            SimulatedFilterChips()
            HorizontalDivider(color = CometChatTheme.colorScheme.strokeColorLight)

            // Conversations section
            Text(
                text = "Conversations",
                style = CometChatTheme.typography.caption1Bold,
                color = CometChatTheme.colorScheme.textColorSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            SimulatedSearchResultItem("Alice Smith", "AS", "Hey! Are you coming to the meeting?", "10:30 AM")
            HorizontalDivider(color = CometChatTheme.colorScheme.strokeColorLight, modifier = Modifier.padding(start = 68.dp))

            // Messages section
            Text(
                text = "Messages",
                style = CometChatTheme.typography.caption1Bold,
                color = CometChatTheme.colorScheme.textColorSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            SimulatedSearchResultItem("Alice Smith", "AS", "Alice mentioned you in Engineering Team", "Yesterday")
            HorizontalDivider(color = CometChatTheme.colorScheme.strokeColorLight, modifier = Modifier.padding(start = 68.dp))
            SimulatedSearchResultItem("Alice Smith", "AS", "Thanks Alice for the update!", "Mon")
        }
    }
}

// ============================================================================
// SECTION 2: STATE PREVIEWS
// ============================================================================

@Preview(showBackground = true, name = "Search - Loading")
@Composable
fun PreviewSearchLoading() {
    CometChatTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CometChatTheme.colorScheme.backgroundColor1)
        ) {
            CometChatSearchBox(
                modifier = Modifier.padding(16.dp),
                text = "searching...",
                onTextChange = { }
            )
            SimulatedFilterChips()
            CometChatLoadingState()
        }
    }
}

@Preview(showBackground = true, name = "Search - Empty Results")
@Composable
fun PreviewSearchEmptyResults() {
    CometChatTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CometChatTheme.colorScheme.backgroundColor1)
        ) {
            CometChatSearchBox(
                modifier = Modifier.padding(16.dp),
                text = "xyznonexistent",
                onTextChange = { }
            )
            SimulatedFilterChips()
            CometChatEmptyState(
                title = "No Results",
                subtitle = "Try a different search term"
            )
        }
    }
}

@Preview(showBackground = true, name = "Search - Error")
@Composable
fun PreviewSearchError() {
    CometChatTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CometChatTheme.colorScheme.backgroundColor1)
        ) {
            CometChatSearchBox(
                modifier = Modifier.padding(16.dp),
                text = "test",
                onTextChange = { }
            )
            SimulatedFilterChips()
            CometChatErrorState(
                title = "Search failed",
                subtitle = "Please check your connection and try again.",
                onRetry = { }
            )
        }
    }
}

// ============================================================================
// SECTION 3: FILTER PREVIEWS
// ============================================================================

@Preview(showBackground = true, name = "Search - Filter Selected")
@Composable
fun PreviewSearchFilterSelected() {
    CometChatTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CometChatTheme.colorScheme.backgroundColor1)
        ) {
            CometChatSearchBox(
                modifier = Modifier.padding(16.dp),
                text = "photo",
                onTextChange = { }
            )
            SimulatedFilterChips(selectedIndex = 2) // Photos selected
            HorizontalDivider(color = CometChatTheme.colorScheme.strokeColorLight)
            SimulatedSearchResultItem("Alice Smith", "AS", "📷 Photo", "Yesterday")
            HorizontalDivider(color = CometChatTheme.colorScheme.strokeColorLight, modifier = Modifier.padding(start = 68.dp))
            SimulatedSearchResultItem("Bob Johnson", "BJ", "📷 Photo", "Mon")
        }
    }
}

@Preview(showBackground = true, name = "Search - No Filter Chips")
@Composable
fun PreviewSearchNoFilterChips() {
    CometChatTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CometChatTheme.colorScheme.backgroundColor1)
        ) {
            CometChatSearchBox(
                modifier = Modifier.padding(16.dp),
                text = "Alice",
                onTextChange = { }
            )
            // No filter chips
            HorizontalDivider(color = CometChatTheme.colorScheme.strokeColorLight)
            SimulatedSearchResultItem("Alice Smith", "AS", "Hey! Are you coming?", "10:30 AM")
        }
    }
}

// ============================================================================
// SECTION 4: VISIBILITY PREVIEWS
// ============================================================================

@Preview(showBackground = true, name = "Search - No Search Bar")
@Composable
fun PreviewSearchNoSearchBar() {
    CometChatTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CometChatTheme.colorScheme.backgroundColor1)
        ) {
            // No search bar
            SimulatedFilterChips()
            HorizontalDivider(color = CometChatTheme.colorScheme.strokeColorLight)
            SimulatedSearchResultItem("Alice Smith", "AS", "Recent conversation", "10:30 AM")
            HorizontalDivider(color = CometChatTheme.colorScheme.strokeColorLight, modifier = Modifier.padding(start = 68.dp))
            SimulatedSearchResultItem("Bob Johnson", "BJ", "Another conversation", "Yesterday")
        }
    }
}
