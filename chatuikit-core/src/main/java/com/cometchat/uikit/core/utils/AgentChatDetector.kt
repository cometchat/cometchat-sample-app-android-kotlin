package com.cometchat.uikit.core.utils

import android.util.Log
import com.cometchat.chat.models.User
import com.cometchat.uikit.core.constants.UIKitConstants

/**
 * Shared utility for determining whether a [User] represents an AI agent chat.
 * Provides a single contract so detection logic is not duplicated across ViewModels.
 *
 * Detection checks both the user's UID prefix and the user's role against the
 * [UIKitConstants.AIConstants.AGENTIC_USER] constant (`"@agentic"`).
 */
object AgentChatDetector {

    /**
     * Checks if the given user represents an AI agent chat.
     *
     * Returns `true` when either:
     * - The user's UID starts with the agentic user prefix, **or**
     * - The user's role equals the agentic user constant.
     *
     * @param user The user to check
     * @return true if the user is an agentic user, false otherwise
     */
    fun isAgentChat(user: User): Boolean {
        val prefix = UIKitConstants.AIConstants.AGENTIC_USER
        Log.e("isAgentChat", "isAgentChat: ${prefix.equals(user.role, ignoreCase = true)}")
        return prefix.equals(user.role, ignoreCase = true)
    }
}
