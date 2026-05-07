package com.cometchat.uikit.compose.preview.presentation.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cometchat.uikit.compose.presentation.emojikeyboard.style.CometChatEmojiKeyboardStyle
import com.cometchat.uikit.compose.presentation.emojikeyboard.ui.CometChatEmojiKeyboard
import com.cometchat.uikit.compose.theme.CometChatTheme

// ============================================================================
// SECTION 1: DEFAULT PREVIEWS
// ============================================================================

/**
 * Preview showing the default emoji keyboard.
 */
@Preview(showBackground = true, name = "EmojiKeyboard - Default")
@Composable
fun PreviewEmojiKeyboardDefault() {
    CometChatTheme {
        CometChatEmojiKeyboard(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            onClick = { }
        )
    }
}

/**
 * Preview showing emoji keyboard with long-press callback.
 */
@Preview(showBackground = true, name = "EmojiKeyboard - With Long Click")
@Composable
fun PreviewEmojiKeyboardWithLongClick() {
    CometChatTheme {
        CometChatEmojiKeyboard(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            onClick = { },
            onLongClick = { }
        )
    }
}

// ============================================================================
// SECTION 2: STYLE CUSTOMIZATION PREVIEWS
// ============================================================================

/**
 * Preview with custom background color.
 */
@Preview(showBackground = true, name = "EmojiKeyboard - Custom Background")
@Composable
fun PreviewEmojiKeyboardCustomBackground() {
    CometChatTheme {
        CometChatEmojiKeyboard(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            style = CometChatEmojiKeyboardStyle.default(
                backgroundColor = Color(0xFFF5F5F5)
            ),
            onClick = { }
        )
    }
}
