package com.cometchat.uikit.kotlin.presentation.emojikeyboard

import com.cometchat.uikit.kotlin.presentation.emojikeyboard.style.CometChatEmojiKeyboardStyle

/**
 * Configuration data class for the Emoji Keyboard component.
 *
 * Bundles optional click callbacks and style parameters that can be passed
 * through the component hierarchy to configure the emoji keyboard.
 *
 * @param onClick Callback invoked when a user taps an emoji, receiving the emoji unicode string
 * @param onLongClick Callback invoked when a user long-presses an emoji, receiving the emoji unicode string
 * @param style Optional style configuration for the emoji keyboard appearance
 */
data class EmojiKeyboardConfiguration(
    val onClick: ((String) -> Unit)? = null,
    val onLongClick: ((String) -> Unit)? = null,
    val style: CometChatEmojiKeyboardStyle? = null
)
