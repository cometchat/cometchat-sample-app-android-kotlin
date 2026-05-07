package com.cometchat.uikit.compose.preview.presentation.ui.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.shared.baseelements.badgecount.BadgeCountStyle
import com.cometchat.uikit.compose.presentation.shared.baseelements.badgecount.CometChatBadgeCount
import com.cometchat.uikit.compose.theme.CometChatTheme

// ============================================================================
// SECTION 1: COUNT VARIATIONS
// ============================================================================

/**
 * Preview showing badge with various counts.
 */
@Preview(showBackground = true, name = "BadgeCount - Various Counts")
@Composable
fun PreviewBadgeCountVariousCounts() {
    CometChatTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CometChatBadgeCount(count = 1)
            CometChatBadgeCount(count = 5)
            CometChatBadgeCount(count = 25)
            CometChatBadgeCount(count = 99)
            CometChatBadgeCount(count = 999)
            CometChatBadgeCount(count = 1000) // Shows "999+"
        }
    }
}

/**
 * Preview showing badge with labeled counts.
 */
@Preview(showBackground = true, name = "BadgeCount - Labeled")
@Composable
fun PreviewBadgeCountLabeled() {
    CometChatTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(1, 5, 25, 99, 500, 999, 1000).forEach { count ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CometChatBadgeCount(count = count)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Count: $count",
                        color = CometChatTheme.colorScheme.textColorPrimary
                    )
                }
            }
        }
    }
}

// ============================================================================
// SECTION 2: STYLE CUSTOMIZATION PREVIEWS
// ============================================================================

/**
 * Preview showing badge with custom colors.
 */
@Preview(showBackground = true, name = "BadgeCount - Custom Colors")
@Composable
fun PreviewBadgeCountCustomColors() {
    CometChatTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Default
            CometChatBadgeCount(count = 5)
            // Error color
            CometChatBadgeCount(
                count = 5,
                style = BadgeCountStyle.default(
                    backgroundColor = CometChatTheme.colorScheme.errorColor
                )
            )
            // Success color
            CometChatBadgeCount(
                count = 5,
                style = BadgeCountStyle.default(
                    backgroundColor = CometChatTheme.colorScheme.successColor
                )
            )
            // Warning color
            CometChatBadgeCount(
                count = 5,
                style = BadgeCountStyle.default(
                    backgroundColor = CometChatTheme.colorScheme.warningColor
                )
            )
        }
    }
}

/**
 * Preview showing badge with border.
 */
@Preview(showBackground = true, name = "BadgeCount - With Border")
@Composable
fun PreviewBadgeCountWithBorder() {
    CometChatTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CometChatBadgeCount(
                count = 10,
                style = BadgeCountStyle.default(
                    borderWidth = 2.dp,
                    borderColor = Color.White
                )
            )
        }
    }
}
