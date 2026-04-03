package com.cometchat.uikit.core.utils

import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.uikit.core.constants.UIKitConstants

/**
 * Returns the default list of message types to fetch for the message list.
 *
 * Includes all standard message types:
 * - Text messages
 * - Image messages
 * - Video messages
 * - Audio messages
 * - File messages
 * - Custom messages
 * - Meeting messages (group calls)
 * - Poll messages
 * - Sticker messages
 * - Collaborative document messages
 * - Collaborative whiteboard messages
 *
 * @return List of message type strings
 */
fun getDefaultMessagesTypes(): List<String> {
    return listOf(
        CometChatConstants.MESSAGE_TYPE_TEXT,
        CometChatConstants.MESSAGE_TYPE_IMAGE,
        CometChatConstants.MESSAGE_TYPE_VIDEO,
        CometChatConstants.MESSAGE_TYPE_AUDIO,
        CometChatConstants.MESSAGE_TYPE_FILE,
        CometChatConstants.MESSAGE_TYPE_CUSTOM,
        CometChatConstants.ActionKeys.ACTION_TYPE_GROUP_MEMBER,
        UIKitConstants.MessageType.MEETING,
        UIKitConstants.MessageType.EXTENSION_POLL,
        UIKitConstants.MessageType.EXTENSION_STICKER,
        UIKitConstants.MessageType.EXTENSION_DOCUMENT,
        UIKitConstants.MessageType.EXTENSION_WHITEBOARD
    )
}

/**
 * Returns the default list of message categories to fetch for the message list.
 *
 * Includes all standard message categories:
 * - Message category (standard messages like text, image, video, audio, file)
 * - Action category (group member actions like join, leave, kick, ban, scope change)
 * - Call category (audio and video call messages)
 * - Custom category (extension messages like polls, stickers)
 * - Interactive category (forms, cards, schedulers)
 *
 * @return List of message category strings
 */
fun getDefaultMessagesCategories(): List<String> {
    return listOf(
        CometChatConstants.CATEGORY_MESSAGE,
        CometChatConstants.CATEGORY_ACTION,
        CometChatConstants.CATEGORY_CALL,
        CometChatConstants.CATEGORY_CUSTOM,
        CometChatConstants.CATEGORY_INTERACTIVE
    )
}
