package com.cometchat.sampleapp.compose.push.appflow.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for the application.
 * These routes use Kotlin Serialization for Navigation Compose type-safe navigation.
 * 
 * Note: Complex SDK objects (User, Group, BaseMessage, CallLog) cannot be serialized directly.
 * Instead, we pass IDs and resolve entities at the destination.
 */

// ============================================================================
// App Flow Routes
// ============================================================================

/**
 * App flow home screen with bottom navigation tabs (Chats, Calls, Users, Groups)
 */
@Serializable
object AppFlowHomeRoute

/**
 * Messages screen for a conversation.
 * Either userId or groupId should be provided (mutually exclusive).
 * 
 * @param userId The user ID for one-on-one conversations (null for group chats)
 * @param groupId The group ID for group conversations (null for one-on-one chats)
 * @param goToMessageId Optional message ID to scroll to
 * @param lastMessageId The last message ID in the conversation (for passing to details screen)
 */
@Serializable
data class MessagesRoute(
    val userId: String? = null,
    val groupId: String? = null,
    val goToMessageId: Long? = null,
    val lastMessageId: Long? = null,
    val parentMessageId: Long? = null
)

/**
 * User details screen
 * 
 * @param userId The user ID to display details for
 * @param lastMessageId The last message ID in the conversation (for delete chat option)
 */
@Serializable
data class UserDetailsRoute(
    val userId: String,
    val lastMessageId: Long? = null
)

/**
 * Group details screen
 * 
 * @param groupId The group ID to display details for
 * @param lastMessageId The last message ID in the conversation (for delete chat option)
 */
@Serializable
data class GroupDetailsRoute(
    val groupId: String,
    val lastMessageId: Long? = null
)

/**
 * Call details screen
 * 
 * @param callSessionId The call session ID to display details for
 */
@Serializable
data class CallDetailsRoute(
    val callSessionId: String
)

/**
 * New chat screen for starting a new conversation
 */
@Serializable
object NewChatRoute

/**
 * Thread message screen for viewing and replying to threaded messages.
 * Either userId or groupId should be provided to identify the conversation context.
 * 
 * @param parentMessageId The ID of the parent message of the thread
 * @param userId The user ID for one-on-one conversations (null for group chats)
 * @param groupId The group ID for group conversations (null for one-on-one chats)
 */
@Serializable
data class ThreadMessageRoute(
    val parentMessageId: Long,
    val userId: String? = null,
    val groupId: String? = null
)

/**
 * Search screen for searching within conversations.
 * Either userId or groupId should be provided to scope the search.
 * 
 * @param userId The user ID for one-on-one conversations (null for group chats)
 * @param groupId The group ID for group conversations (null for one-on-one chats)
 */
@Serializable
data class SearchRoute(
    val userId: String? = null,
    val groupId: String? = null
)

/**
 * Add members screen for adding users to a group.
 * 
 * @param groupId The group ID to add members to
 */
@Serializable
data class AddMembersRoute(
    val groupId: String
)

/**
 * Chat history screen for viewing AI assistant chat history.
 * 
 * @param userId The user ID whose AI chat history to display
 */
@Serializable
data class ChatHistoryRoute(
    val userId: String
)
