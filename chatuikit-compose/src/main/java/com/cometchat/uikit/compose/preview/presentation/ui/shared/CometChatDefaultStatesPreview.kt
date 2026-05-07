package com.cometchat.uikit.compose.preview.presentation.ui.shared

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatEmptyState
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatEmptyStateStyle
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatErrorState
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatErrorStateStyle
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatLoadingState
import com.cometchat.uikit.compose.presentation.shared.defaultstates.CometChatLoadingStateStyle
import com.cometchat.uikit.compose.theme.CometChatTheme

// ============================================================================
// SECTION 1: LOADING STATE PREVIEWS
// ============================================================================

/**
 * Preview showing the default loading state.
 */
@Preview(showBackground = true, name = "DefaultStates - Loading - Default")
@Composable
fun PreviewLoadingStateDefault() {
    CometChatTheme {
        CometChatLoadingState(modifier = Modifier.fillMaxSize())
    }
}

/**
 * Preview showing loading state with custom item count.
 */
@Preview(showBackground = true, name = "DefaultStates - Loading - 3 Items")
@Composable
fun PreviewLoadingState3Items() {
    CometChatTheme {
        CometChatLoadingState(
            modifier = Modifier.fillMaxSize(),
            style = CometChatLoadingStateStyle.default(itemCount = 3)
        )
    }
}

/**
 * Preview showing loading state with custom background.
 */
@Preview(showBackground = true, name = "DefaultStates - Loading - Custom Background")
@Composable
fun PreviewLoadingStateCustomBackground() {
    CometChatTheme {
        CometChatLoadingState(
            modifier = Modifier.fillMaxSize(),
            style = CometChatLoadingStateStyle.default(
                backgroundColor = Color(0xFFF5F5F5)
            )
        )
    }
}

// ============================================================================
// SECTION 2: EMPTY STATE PREVIEWS
// ============================================================================

/**
 * Preview showing the default empty state.
 */
@Preview(showBackground = true, name = "DefaultStates - Empty - Default")
@Composable
fun PreviewEmptyStateDefault() {
    CometChatTheme {
        CometChatEmptyState(modifier = Modifier.fillMaxSize())
    }
}

/**
 * Preview showing empty state with custom text.
 */
@Preview(showBackground = true, name = "DefaultStates - Empty - Custom Text")
@Composable
fun PreviewEmptyStateCustomText() {
    CometChatTheme {
        CometChatEmptyState(
            modifier = Modifier.fillMaxSize(),
            title = "No conversations yet",
            subtitle = "Start a new chat to begin messaging"
        )
    }
}

/**
 * Preview showing empty state with custom background.
 */
@Preview(showBackground = true, name = "DefaultStates - Empty - Custom Background")
@Composable
fun PreviewEmptyStateCustomBackground() {
    CometChatTheme {
        CometChatEmptyState(
            modifier = Modifier.fillMaxSize(),
            style = CometChatEmptyStateStyle.default(
                backgroundColor = Color(0xFFF5F5F5)
            )
        )
    }
}

// ============================================================================
// SECTION 3: ERROR STATE PREVIEWS
// ============================================================================

/**
 * Preview showing the default error state.
 */
@Preview(showBackground = true, name = "DefaultStates - Error - Default")
@Composable
fun PreviewErrorStateDefault() {
    CometChatTheme {
        CometChatErrorState(
            modifier = Modifier.fillMaxSize(),
            onRetry = { }
        )
    }
}

/**
 * Preview showing error state with custom text.
 */
@Preview(showBackground = true, name = "DefaultStates - Error - Custom Text")
@Composable
fun PreviewErrorStateCustomText() {
    CometChatTheme {
        CometChatErrorState(
            modifier = Modifier.fillMaxSize(),
            title = "Connection failed",
            subtitle = "Please check your internet connection and try again.",
            onRetry = { }
        )
    }
}

/**
 * Preview showing error state with custom background.
 */
@Preview(showBackground = true, name = "DefaultStates - Error - Custom Background")
@Composable
fun PreviewErrorStateCustomBackground() {
    CometChatTheme {
        CometChatErrorState(
            modifier = Modifier.fillMaxSize(),
            style = CometChatErrorStateStyle.default(
                backgroundColor = Color(0xFFF5F5F5)
            ),
            onRetry = { }
        )
    }
}
