package com.cometchat.sampleapp.compose.navigation

import kotlinx.serialization.Serializable

/**
 * Navigation routes for the CometChat Sample App (Jetpack Compose).
 *
 * This file defines type-safe navigation routes using Kotlin Serialization.
 * Each route is a serializable object or data class that represents a
 * destination in the navigation graph.
 *
 * ## Usage:
 * ```kotlin
 * // Navigate to splash (start destination)
 * navController.navigate(SplashRoute)
 *
 * // Navigate to login
 * navController.navigate(LoginRoute)
 *
 * // Navigate to home
 * navController.navigate(HomeRoute)
 *
 * // Navigate to messages with a user
 * navController.navigate(MessagesRoute(userId = "user123", groupId = null))
 *
 * // Navigate to messages with a group
 * navController.navigate(MessagesRoute(userId = null, groupId = "group123"))
 * ```
 *
 * @see NavGraph for the navigation host setup
 */

/**
 * Route for the splash screen.
 *
 * This is the entry point (start destination) for the app.
 * Handles SDK initialization, credential checking, and auto-login routing.
 *
 * Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5, 2.1, 2.2, 2.3, 2.4, 3.1, 3.2, 3.3, 3.4, 13.1
 */
@Serializable
object SplashRoute

/**
 * Route for the app credentials screen.
 *
 * This screen is shown when no App ID is found in SharedPreferences.
 * Allows users to enter CometChat credentials (App ID, Auth Key, Region).
 *
 * Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 13.2
 */
@Serializable
object AppCredentialsRoute

/**
 * Route for the login screen.
 *
 * This is the entry point for unauthenticated users.
 * Displays sample users for quick login and manual UID entry.
 */
@Serializable
object LoginRoute

/**
 * Route for the home screen with bottom navigation.
 *
 * Contains three tabs: Chats, Users, and Groups.
 * This is the main screen after successful login.
 */
@Serializable
object HomeRoute

/**
 * Route for the messages screen.
 *
 * Displays the chat interface for a specific conversation.
 * Either [userId] or [groupId] should be provided, not both.
 *
 * @property userId The UID of the user for one-on-one chat (null for group chat)
 * @property groupId The GUID of the group for group chat (null for one-on-one chat)
 */
@Serializable
data class MessagesRoute(
    val userId: String? = null,
    val groupId: String? = null
)

/**
 * Route for the user details screen.
 *
 * Displays detailed information about a specific user including
 * profile, actions, and shared media.
 *
 * @property userId The UID of the user to display
 */
@Serializable
data class UserDetailsRoute(
    val userId: String
)

/**
 * Route for the group details screen.
 *
 * Displays detailed information about a group including
 * members, settings, and shared media.
 *
 * @property groupId The GUID of the group to display
 */
@Serializable
data class GroupDetailsRoute(
    val groupId: String
)

/**
 * Route for the thread messages screen.
 *
 * Displays thread replies for a specific parent message.
 *
 * @property parentMessageId The ID of the parent message
 */
@Serializable
data class ThreadRoute(
    val parentMessageId: Long
)

/**
 * Route for the search screen.
 *
 * Provides global and contextual search across conversations and messages.
 * Either [userId] or [groupId] can be provided for contextual search,
 * or both can be null for global search.
 *
 * @property userId Optional user ID for contextual search within a user conversation
 * @property groupId Optional group ID for contextual search within a group conversation
 *
 * Validates: Requirements 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 6.1, 6.2, 6.3, 6.4, 13.3
 */
@Serializable
data class SearchRoute(
    val userId: String? = null,
    val groupId: String? = null
)

/**
 * Route for the new chat screen.
 *
 * Displays a tabbed interface with Users and Groups tabs for starting
 * new conversations. Users can select a contact or group to navigate
 * to the messages screen.
 *
 * Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 13.4
 */
@Serializable
object NewChatRoute

/**
 * Route for the call details screen.
 *
 * Displays detailed information about a call log including:
 * - Call recipient information
 * - Call type (incoming/outgoing/missed)
 * - Call date and duration
 *
 * @property callLogJson The serialized CallLog JSON string for passing call data
 *
 * Validates: Requirements 9.2, 13.5
 */
@Serializable
data class CallDetailsRoute(
    val callLogJson: String
)

/**
 * Route for the add members screen.
 *
 * Displays a full-screen user list with multi-selection mode
 * for adding members to a group.
 *
 * @property groupId The GUID of the group to add members to
 *
 * Validates: Requirements 11.5
 */
@Serializable
data class AddMembersRoute(
    val groupId: String
)

/**
 * Route for the group members screen.
 *
 * Displays a full-screen list of group members.
 *
 * @property groupId The GUID of the group to show members for
 *
 * Validates: Requirements 11.4
 */
@Serializable
data class GroupMembersRoute(
    val groupId: String
)

/**
 * Route for the banned members screen.
 *
 * Displays a full-screen list of banned group members with unban option.
 *
 * @property groupId The GUID of the group to show banned members for
 *
 * Validates: Requirements 11.4
 */
@Serializable
data class BannedMembersRoute(
    val groupId: String
)
