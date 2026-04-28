package com.cometchat.uikit.core.constants

/**
 * Enum defining the search scope for the CometChatSearch component.
 *
 * The search scope determines which types of data are searched:
 * - [MESSAGES]: Search only messages
 * - [CONVERSATIONS]: Search only conversations
 *
 * When both scopes are configured, the search component will search
 * both messages and conversations simultaneously.
 */
enum class SearchScope {
    /**
     * Search scope for messages.
     * When selected, the search will include message results.
     */
    MESSAGES,

    /**
     * Search scope for conversations.
     * When selected, the search will include conversation results.
     */
    CONVERSATIONS
}
