package com.cometchat.uikit.kotlin.presentation.stickerkeyboard.listener

import com.cometchat.uikit.core.domain.model.Sticker

/**
 * Listener interface for sticker click events.
 *
 * Implement this interface to receive callbacks when a sticker is clicked
 * in the CometChatStickerKeyboard component.
 */
fun interface StickerClickListener {
    /**
     * Called when a sticker is clicked.
     *
     * @param sticker The sticker that was clicked
     */
    fun onStickerClick(sticker: Sticker)
}
