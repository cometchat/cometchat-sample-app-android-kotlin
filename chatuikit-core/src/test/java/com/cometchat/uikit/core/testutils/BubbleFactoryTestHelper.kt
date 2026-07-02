package com.cometchat.uikit.core.testutils

import com.cometchat.chat.models.BaseMessage
import com.cometchat.uikit.core.constants.UIKitConstants

/**
 * Test-only helper that replicates BubbleFactory's static routing logic.
 *
 * The real BubbleFactory lives in chatuikit-kotlin which is not a dependency
 * of chatuikit-core. This helper mirrors the companion object methods so that
 * chatuikit-core unit tests can verify routing without a cross-module dependency.
 */
object BubbleFactoryTestHelper {

    /** Special key used for deleted messages. */
    const val DELETED_KEY = "deleted"

    /** Special key used for card messages (category-only, type is arbitrary). */
    const val CARD_KEY = "card"

    /**
     * Returns the factory key for a given message.
     *
     * The key format is `category_type` (e.g., "message_text", "custom_extension_poll").
     * Deleted messages return [DELETED_KEY] regardless of original type.
     * Card messages return [CARD_KEY] regardless of type.
     */
    @JvmStatic
    fun getFactoryKey(message: BaseMessage): String {
        return if (message.deletedAt > 0) {
            DELETED_KEY
        } else if (message.category == UIKitConstants.MessageCategory.CARD) {
            CARD_KEY
        } else {
            "${message.category}_${message.type}"
        }
    }

    /**
     * Creates a factory key from category and type.
     */
    @JvmStatic
    fun getKey(category: String, type: String): String {
        return "${category}_${type}"
    }
}
