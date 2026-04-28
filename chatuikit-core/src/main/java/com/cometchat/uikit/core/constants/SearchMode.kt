package com.cometchat.uikit.core.constants

/**
 * Enum defining the search mode for the CometChatSearch component.
 *
 * The search mode determines which types of data are fetched during a search operation:
 * - [BOTH]: Search both conversations and messages simultaneously
 * - [CONVERSATIONS]: Search only conversations
 * - [MESSAGES]: Search only messages
 * - [NONE]: No search is performed (used when search criteria are invalid)
 *
 * The search mode is determined internally by the ViewModel based on:
 * - Configured search scopes
 * - Selected filters (message filters vs conversation filters)
 * - Presence of search text
 * - UID/GUID configuration (forces MESSAGES mode)
 */
enum class SearchMode {
    /**
     * Search mode for fetching both conversations and messages.
     * Used when search text is provided without specific filters
     * and both scopes are configured.
     */
    BOTH,

    /**
     * Search mode for fetching only conversations.
     * Used when conversation filters are selected (Groups, Unread)
     * or when only CONVERSATIONS scope is configured.
     */
    CONVERSATIONS,

    /**
     * Search mode for fetching only messages.
     * Used when message filters are selected (Photos, Videos, Documents, Links, Audio),
     * when UID/GUID is set, or when only MESSAGES scope is configured.
     */
    MESSAGES,

    /**
     * No search mode - no data will be fetched.
     * Used when search criteria are invalid or conflicting.
     */
    NONE
}
