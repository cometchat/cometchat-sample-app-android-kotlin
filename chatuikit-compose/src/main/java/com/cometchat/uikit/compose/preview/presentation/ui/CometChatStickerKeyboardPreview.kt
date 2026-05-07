package com.cometchat.uikit.compose.preview.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.stickerkeyboard.style.CometChatStickerKeyboardStyle
import com.cometchat.uikit.compose.presentation.stickerkeyboard.ui.CometChatStickerKeyboard
import com.cometchat.uikit.compose.theme.CometChatTheme

// ============================================================================
// SECTION 1: DEFAULT PREVIEWS
// ============================================================================

/**
 * Preview showing the default sticker keyboard.
 */
@Preview(showBackground = true, name = "StickerKeyboard - Default")
@Composable
fun PreviewStickerKeyboardDefault() {
    CometChatTheme {
        CometChatStickerKeyboard(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            onStickerClick = { }
        )
    }
}

// ============================================================================
// SECTION 2: VISIBILITY PREVIEWS
// ============================================================================

/**
 * Preview with loading state hidden.
 */
@Preview(showBackground = true, name = "StickerKeyboard - No Loading State")
@Composable
fun PreviewStickerKeyboardNoLoadingState() {
    CometChatTheme {
        CometChatStickerKeyboard(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            hideLoadingState = true,
            onStickerClick = { }
        )
    }
}

/**
 * Preview with empty state hidden.
 */
@Preview(showBackground = true, name = "StickerKeyboard - No Empty State")
@Composable
fun PreviewStickerKeyboardNoEmptyState() {
    CometChatTheme {
        CometChatStickerKeyboard(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            hideEmptyState = true,
            onStickerClick = { }
        )
    }
}

/**
 * Preview with error state hidden.
 */
@Preview(showBackground = true, name = "StickerKeyboard - No Error State")
@Composable
fun PreviewStickerKeyboardNoErrorState() {
    CometChatTheme {
        CometChatStickerKeyboard(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            hideErrorState = true,
            onStickerClick = { }
        )
    }
}

// ============================================================================
// SECTION 3: CUSTOM VIEW OVERRIDES
// ============================================================================

/**
 * Preview with custom empty view.
 */
@Preview(showBackground = true, name = "StickerKeyboard - Custom Empty View")
@Composable
fun PreviewStickerKeyboardCustomEmptyView() {
    CometChatTheme {
        CometChatStickerKeyboard(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            emptyView = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No stickers available",
                        style = CometChatTheme.typography.heading3Medium,
                        color = CometChatTheme.colorScheme.textColorSecondary
                    )
                }
            },
            onStickerClick = { }
        )
    }
}

/**
 * Preview with custom error view.
 */
@Preview(showBackground = true, name = "StickerKeyboard - Custom Error View")
@Composable
fun PreviewStickerKeyboardCustomErrorView() {
    CometChatTheme {
        CometChatStickerKeyboard(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            errorView = { onRetry ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Failed to load stickers. Tap to retry.",
                        style = CometChatTheme.typography.bodyRegular,
                        color = CometChatTheme.colorScheme.errorColor
                    )
                }
            },
            onStickerClick = { }
        )
    }
}

// ============================================================================
// SECTION 4: STYLE CUSTOMIZATION PREVIEWS
// ============================================================================

/**
 * Preview with custom background color.
 */
@Preview(showBackground = true, name = "StickerKeyboard - Custom Background")
@Composable
fun PreviewStickerKeyboardCustomBackground() {
    CometChatTheme {
        CometChatStickerKeyboard(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            style = CometChatStickerKeyboardStyle.default(
                backgroundColor = Color(0xFFF5F5F5)
            ),
            onStickerClick = { }
        )
    }
}
