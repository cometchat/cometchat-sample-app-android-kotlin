package com.cometchat.uikit.core.constants

/**
 * Enum defining filter groups for visibility logic.
 *
 * When a filter from a group is selected, only filters from that group should be visible.
 * - CONVERSATION: Unread, Groups
 * - MEDIA: Photos, Videos
 * - DOCUMENT: Documents, Audio
 * - LINK: Links (standalone)
 */
enum class FilterGroup {
    CONVERSATION,
    MEDIA,
    DOCUMENT,
    LINK
}

/**
 * Enum defining search filter types for the CometChatSearch component.
 *
 * Filters are categorized into two types:
 * - Message filters: PHOTOS, VIDEOS, DOCUMENTS, LINKS, AUDIO
 * - Conversation filters: GROUPS, UNREAD
 *
 * Filters are also grouped for visibility logic:
 * - CONVERSATION group: UNREAD, GROUPS
 * - MEDIA group: PHOTOS, VIDEOS
 * - DOCUMENT group: DOCUMENTS, AUDIO
 * - LINK group: LINKS
 *
 * Use [isMessageFilter] and [isConversationFilter] helper methods to determine
 * the filter category. Use [group] property to get the filter's visibility group.
 */
enum class SearchFilter(val value: String, val group: FilterGroup) {
    /**
     * Filter for image messages.
     */
    PHOTOS("photos", FilterGroup.MEDIA),

    /**
     * Filter for video messages.
     */
    VIDEOS("videos", FilterGroup.MEDIA),

    /**
     * Filter for document/file messages.
     */
    DOCUMENTS("documents", FilterGroup.DOCUMENT),

    /**
     * Filter for text messages containing links.
     */
    LINKS("links", FilterGroup.LINK),

    /**
     * Filter for audio messages.
     */
    AUDIO("audio", FilterGroup.DOCUMENT),

    /**
     * Filter for group conversations.
     */
    GROUPS("groups", FilterGroup.CONVERSATION),

    /**
     * Filter for unread conversations.
     */
    UNREAD("unread", FilterGroup.CONVERSATION);

    /**
     * Checks if this filter is a message filter.
     *
     * Message filters include: PHOTOS, VIDEOS, DOCUMENTS, LINKS, AUDIO
     *
     * @return true if this filter applies to messages, false otherwise
     */
    fun isMessageFilter(): Boolean = this in listOf(PHOTOS, VIDEOS, DOCUMENTS, LINKS, AUDIO)

    /**
     * Checks if this filter is a conversation filter.
     *
     * Conversation filters include: GROUPS, UNREAD
     *
     * @return true if this filter applies to conversations, false otherwise
     */
    fun isConversationFilter(): Boolean = this in listOf(GROUPS, UNREAD)

    companion object {
        /**
         * Returns all filters belonging to the specified group.
         *
         * @param group The filter group to get filters for
         * @return List of SearchFilter values in the specified group
         */
        fun getFiltersInGroup(group: FilterGroup): List<SearchFilter> {
            return entries.filter { it.group == group }
        }
    }
}
