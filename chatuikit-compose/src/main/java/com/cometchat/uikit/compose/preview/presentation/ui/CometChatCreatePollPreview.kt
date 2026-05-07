package com.cometchat.uikit.compose.preview.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.cometchat.uikit.compose.presentation.createpoll.style.CometChatCreatePollStyle
import com.cometchat.uikit.compose.presentation.createpoll.ui.CometChatCreatePoll
import com.cometchat.uikit.compose.theme.CometChatTheme

// ============================================================================
// SECTION 1: DEFAULT PREVIEWS
// ============================================================================

/**
 * Preview showing the default create poll form.
 */
@Preview(showBackground = true, name = "CreatePoll - Default")
@Composable
fun PreviewCreatePollDefault() {
    CometChatTheme {
        CometChatCreatePoll(
            onSubmitClick = { _, _ -> },
            onBackPress = { }
        )
    }
}

/**
 * Preview showing create poll with custom title.
 */
@Preview(showBackground = true, name = "CreatePoll - Custom Title")
@Composable
fun PreviewCreatePollCustomTitle() {
    CometChatTheme {
        CometChatCreatePoll(
            title = "New Survey",
            onSubmitClick = { _, _ -> },
            onBackPress = { }
        )
    }
}

// ============================================================================
// SECTION 2: STATE PREVIEWS
// ============================================================================

/**
 * Preview showing create poll in submitting state.
 */
@Preview(showBackground = true, name = "CreatePoll - Submitting")
@Composable
fun PreviewCreatePollSubmitting() {
    CometChatTheme {
        CometChatCreatePoll(
            isSubmitting = true,
            onSubmitClick = { _, _ -> },
            onBackPress = { }
        )
    }
}

/**
 * Preview showing create poll with error message.
 */
@Preview(showBackground = true, name = "CreatePoll - Error")
@Composable
fun PreviewCreatePollError() {
    CometChatTheme {
        CometChatCreatePoll(
            errorMessage = "Failed to create poll. Please try again.",
            onSubmitClick = { _, _ -> },
            onBackPress = { }
        )
    }
}

// ============================================================================
// SECTION 3: VISIBILITY PREVIEWS
// ============================================================================

/**
 * Preview showing create poll without toolbar.
 */
@Preview(showBackground = true, name = "CreatePoll - No Toolbar")
@Composable
fun PreviewCreatePollNoToolbar() {
    CometChatTheme {
        CometChatCreatePoll(
            hideToolbar = true,
            onSubmitClick = { _, _ -> },
            onBackPress = { }
        )
    }
}

// ============================================================================
// SECTION 4: STYLE CUSTOMIZATION PREVIEWS
// ============================================================================

/**
 * Preview with custom background color.
 */
@Preview(showBackground = true, name = "CreatePoll - Custom Background")
@Composable
fun PreviewCreatePollCustomBackground() {
    CometChatTheme {
        CometChatCreatePoll(
            style = CometChatCreatePollStyle.default(
                backgroundColor = Color(0xFFF5F5F5)
            ),
            onSubmitClick = { _, _ -> },
            onBackPress = { }
        )
    }
}
