package com.cometchat.uikit.compose.presentation.emojikeyboard.model

import com.cometchat.uikit.compose.presentation.emojikeyboard.style.CometChatEmojiKeyboardStyle

/**
 * Configuration data class for the CometChatEmojiKeyboard component.
 *
 * Bundles optional callback and style parameters for passing keyboard settings
 * through the component hierarchy.
 *
 * @param onClick Callback invoked with the emoji unicode string when a user taps an emoji
 * @param onLongClick Callback invoked with the emoji unicode string when a user long-presses an emoji
 * @param style Optional style configuration for the emoji keyboard appearance
 */
data class EmojiKeyboardConfiguration(
    val onClick: ((String) -> Unit)? = null,
    val onLongClick: ((String) -> Unit)? = null,
    val style: CometChatEmojiKeyboardStyle? = null
)
